package com.kurly.cloud.point.api.recommend.service;

import com.kurly.cloud.point.api.member.entity.DormantMember;
import com.kurly.cloud.point.api.member.entity.Member;
import com.kurly.cloud.point.api.member.repository.DormantMemberRepository;
import com.kurly.cloud.point.api.member.repository.MemberRepository;
import com.kurly.cloud.point.api.order.entity.Order;
import com.kurly.cloud.point.api.order.repository.OrderRepository;
import com.kurly.cloud.point.api.recommend.domain.RecommendationDataType;
import com.kurly.cloud.point.api.recommend.domain.RecommendationDelayType;
import com.kurly.cloud.point.api.recommend.domain.RecommendationPointReason;
import com.kurly.cloud.point.api.recommend.domain.RecommendationPointStatus;
import com.kurly.cloud.point.api.recommend.entity.RecommendationPointHistory;
import com.kurly.cloud.point.api.recommend.repository.RecommendationPointHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RecommendationPointHistoryService {
  private final RecommendationPointHistoryRepository recommendRepository;
  private final OrderRepository orderRepository;
  private final MemberRepository memberRepository;
  private final DormantMemberRepository dormantMemberRepository;

  private static final int MAX_ADDRESS_DISTANCE = 2;
  private static final int MAX_ADDRESS_PAID_COUNT = 3;
  public static final int PAID_POINT = 5000;

  public void save(RecommendationPointHistory history) {
    recommendRepository.save(history);
  }

  /**
   * 주문으로 이력을 생성한다.
   */
  @Transactional(readOnly = true)
  public Optional<RecommendationPointHistory> generateByOrder(Order order) {
    if (!isValidOrder(order)) {
      return Optional.empty();
    }

    RecommendationPointHistory history = RecommendationPointHistory.builder()
        .orderNumber(order.getOrderNumber())
        .orderPhoneNumber(order.getMobile())
        .orderAddress(order.getFullAddress())
        .status(RecommendationPointStatus.NON_PAID)
        .orderMemberNumber(order.getMemberNumber())
        .delayType(RecommendationDelayType.CHECKED)
        .type(RecommendationDataType.PRODUCTION_DATA)
        .createDateTime(LocalDateTime.now())
        .updateDateTime(LocalDateTime.now())
        .orderDeliveredDateTime(order.getDeliveredDateTime())
        .build();

    return Optional.of(validateAbusing(order, history));
  }

  /**
   * 주문의 상태를 아래와 같이 검증한다.
   * - 첫주문 인지
   * - 주문 번호로 이력이 있는지
   * - 주문 회원 번호로 이력이 있는지
   */
  public boolean isValidOrder(Order order) {
    LocalDateTime deliveredDate =
        order.getDeliveredDateTime().withHour(0).withMinute(0).withSecond(0).withNano(0);
    if (orderRepository.countMemberDeliveredOrder(order.getMemberNumber(), deliveredDate) > 1) {
      return false;
    }

    if (getByOrder(order.getOrderNumber()).isPresent()) {
      return false;
    }

    Optional<RecommendationPointHistory> history = getByOrderMember(order.getMemberNumber());
    if (history.isPresent() && history.get().isCheckedHistory()) {
      return false;
    }

    return true;
  }

  private Optional<RecommendationPointHistory> getByOrder(long orderNumber) {
    return recommendRepository.findFirstByOrderNumberAndDelayTypeAndType(
        orderNumber, RecommendationDelayType.CHECKED, RecommendationDataType.PRODUCTION_DATA
    );
  }

  private Optional<RecommendationPointHistory> getByOrderMember(long memberNumber) {
    return recommendRepository.findFirstByOrderMemberNumberAndDelayTypeAndType(
        memberNumber, RecommendationDelayType.CHECKED, RecommendationDataType.PRODUCTION_DATA
    );
  }

  private RecommendationPointHistory validateAbusing(Order order,
                                                     RecommendationPointHistory history) {
    // 추천인 검증
    Member orderMember = order.getMember();
    Optional<Member> recommendMemberOptional =
        getRecommendationMember(orderMember.getRecommendMemberId());

    if (recommendMemberOptional.isEmpty()) {
      history.setReason(RecommendationPointReason.NOT_EXIST_RECOMMENDER);
      return history;
    }

    // 전화번호 검증
    Member recommendMember = recommendMemberOptional.get();
    history.setRecommendationMemberNumber(recommendMember.getMemberNumber());
    history.setRecommendationPhoneNumber(recommendMember.getMobile());
    history.setRecommendationAddressPaidCount(
        getRecommendationPaidCount(recommendMember.getMemberNumber()));

    RecommendationPointReason mobileValidateResult
        = validateMobile(order.getMobile(), recommendMember.getMobile());

    if (!RecommendationPointReason.DEFAULT.equals(mobileValidateResult)) {
      history.setReason(mobileValidateResult);
      return history;
    }

    // 블랙리스트 주소 검증
    if (isBlacklistAddress(order)) {
      history.setReason(RecommendationPointReason.BLACKLIST);
      return history;
    }

    // 추천인, 피추천인 주문 주소 유사도 검증
    List<Order> recommenderMemberOrders = orderRepository
        .findMemberOrderedOrder(recommendMember.getMemberNumber(), PageRequest.of(0, 1));

    String left = "";
    String right = "";

    if (recommenderMemberOrders.size() > 0) {
      Order recommenderOrder = recommenderMemberOrders.get(0);
      if (!order.getJibunFullAddress().isEmpty()
          && !recommenderOrder.getJibunFullAddress().isEmpty()) {
        left = order.getJibunFullAddress();
        right = recommenderOrder.getJibunFullAddress();
      }
      if (!order.getRoadFullAddress().isEmpty()
          && !recommenderOrder.getRoadFullAddress().isEmpty()) {
        left = recommenderOrder.getRoadFullAddress();
        right = recommenderOrder.getRoadFullAddress();
      }
    }

    if (!left.isEmpty() && !right.isEmpty()) {
      history.setRecommendationAddress(right);

      Integer distance = LevenshteinDistance.getDefaultInstance()
          .apply(left.replace(" ", ""), right.replace(" ", ""));

      if (distance <= MAX_ADDRESS_DISTANCE) {
        if (history.getRecommendationAddressPaidCount() > MAX_ADDRESS_PAID_COUNT) {
          history.setReason(RecommendationPointReason.DUPLICATE_ADDRESS);
          return history;
        }
        history.plusAddressPaidCount(1);
      }
    }

    history.setReason(RecommendationPointReason.DEFAULT);
    history.setPoint(PAID_POINT);
    history.setStatus(RecommendationPointStatus.PAID);
    history.setOrderMemberName(orderMember.getName());

    return history;
  }

  private boolean isBlacklistAddress(Order order) {
    if (order.getAddress().equals("경기 남양주시 오남읍 진건오남로 735-10 (한국아파트)")) {
      return order.getAddressSub().replaceAll("[^\\d]", "").equals("1041802");
    }
    return false;
  }

  private RecommendationPointReason validateMobile(String orderMobile, String recommendMobile) {
    orderMobile = orderMobile.replace("-", "");
    recommendMobile = recommendMobile.replace("-", "");

    if (orderMobile.equals(recommendMobile)) {
      return RecommendationPointReason.SAME_PHONE_NUMBER;
    }

    String pattern = "^(?:010|011|016|017|018|019|070)[^01]\\d+";

    if (!orderMobile.matches(pattern)) {
      if (!recommendMobile.matches(pattern)) {
        return RecommendationPointReason.INVALID_PHONE_NUMBER;
      }
      return RecommendationPointReason.INVALID_RECOMMENDEE_PHONE_NUMBER;
    }

    if (!recommendMobile.matches(pattern)) {
      return RecommendationPointReason.INVALID_RECOMMENDER_PHONE_NUMBER;
    }

    return RecommendationPointReason.DEFAULT;
  }

  private int getRecommendationPaidCount(long memberNumber) {
    return recommendRepository
        .findFirstByRecommendationMemberNumber(memberNumber)
        .orElseGet(RecommendationPointHistory::new)
        .getRecommendationAddressPaidCount();
  }

  private Optional<Member> getRecommendationMember(String memberId) {
    Optional<Member> memberOptional = memberRepository.findByMemberId(memberId);

    if (memberOptional.isPresent()) {
      return memberOptional;
    }

    Optional<DormantMember> dormantMemberOptional =
        dormantMemberRepository.findByMemberId(memberId);

    return dormantMemberOptional.map(DormantMember::toMember);
  }
}
