package com.kurly.cloud.point.api.batch.recommend.service.impl;

import com.kurly.cloud.point.api.batch.member.entity.DormantMember;
import com.kurly.cloud.point.api.batch.member.entity.Member;
import com.kurly.cloud.point.api.batch.member.repository.DormantMemberRepository;
import com.kurly.cloud.point.api.batch.member.repository.MemberRepository;
import com.kurly.cloud.point.api.batch.order.entity.Order;
import com.kurly.cloud.point.api.batch.order.repository.OrderRepository;
import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationDataType;
import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationDelayType;
import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationPointReason;
import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationPointStatus;
import com.kurly.cloud.point.api.batch.recommend.entity.RecommendationPointHistory;
import com.kurly.cloud.point.api.batch.recommend.repository.RecommendationPointHistoryRepository;
import com.kurly.cloud.point.api.batch.recommend.service.RecommendationPointHistoryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RefreshScope
@RequiredArgsConstructor
@Service
public class RecommendationPointHistoryService implements RecommendationPointHistoryUseCase {
  private final RecommendationPointHistoryRepository recommendRepository;
  private final OrderRepository orderRepository;
  private final MemberRepository memberRepository;
  private final DormantMemberRepository dormantMemberRepository;

  private static final int MAX_ADDRESS_DISTANCE = 2;
  private static final int MAX_ADDRESS_PAID_COUNT = 3;
  private static final int PAID_POINT = 5000;
  
  private LocalDate promotionStartDate;
  private LocalDate promotionEndDate;
  private int promotionPaidPoint = PAID_POINT;

  public void save(RecommendationPointHistory history) {
    recommendRepository.save(history);
  }

  /**
   * 프로모션 지급 시작날짜를 설정한다.
   * 실제 지급일보다 하루 전날 이어야 함
   *
   * @param promotionStartDate 프로모션 시작 일 문자열
   */
  @Value("${promotion.startDate:}")
  public void setPromotionStartDate(String promotionStartDate) {
    if (!StringUtils.isEmpty(promotionStartDate)) {
      this.promotionStartDate = LocalDate.parse(promotionStartDate);
    }
  }

  /**
   * 프로모션 지급 종료날짜를 설정한다.
   *
   * @param promotionEndDate 프로모션 종료일 문자열
   */
  @Value("${promotion.endDate:}")
  public void setPromotionEndDate(String promotionEndDate) {
    if (!StringUtils.isEmpty(promotionEndDate)) {
      this.promotionEndDate = LocalDate.parse(promotionEndDate);
    }
  }

  @Value("${promotion.paidPoint:5000}")
  public void setPromotionPaidPoint(int promotionPaidPoint) {
    this.promotionPaidPoint = promotionPaidPoint;
  }

  public int getPaidPoint() {
    return getPaidPoint(null);
  }

  public int getPaidPoint(@Nullable LocalDateTime payDateTime) {
    return isPromotionActive(payDateTime) ? promotionPaidPoint : PAID_POINT;
  }

  /**
   * 프로모션이 진행중인지 확인한다.
   */
  public boolean isPromotionActive(LocalDateTime payDateTime) {
    if (Objects.isNull(promotionStartDate) || Objects.isNull(promotionEndDate)
        || Objects.isNull(payDateTime)) {
      return false;
    }

    return (promotionStartDate.isBefore(payDateTime.toLocalDate())
        || promotionStartDate.isEqual(payDateTime.toLocalDate()))
        && (promotionEndDate.isAfter(payDateTime.toLocalDate())
        || promotionEndDate.isEqual(payDateTime.toLocalDate()));
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
        .orderPhoneNumber(Objects.requireNonNullElse(order.getMobile(), ""))
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
    if (orderRepository.countMemberDeliveredOrder(order.getMemberNumber(), deliveredDate) > 0) {
      return false;
    }

    if (isCheckedOrder(order.getOrderNumber())) {
      return false;
    }

    if (isPaidMember(order.getMemberNumber())) {
      return false;
    }

    return true;
  }

  private boolean isCheckedOrder(long orderNumber) {
    return recommendRepository.findFirstByOrderNumberAndDelayTypeAndType(
        orderNumber, RecommendationDelayType.CHECKED, RecommendationDataType.PRODUCTION_DATA
    ).isPresent();
  }

  private boolean isPaidMember(long memberNumber) {
    List<RecommendationPointHistory> paidHistory = recommendRepository
        .findAllByOrderMemberNumberAndStatusAndRecommendationMemberNumberIsNotNull(memberNumber, RecommendationPointStatus.PAID);

    if (paidHistory.size() == 0) {
      return false;
    }

    for (RecommendationPointHistory history : paidHistory) {
      Optional<RecommendationPointHistory> deducted =
          recommendRepository.findFirstByOrderNumberAndOrderMemberNumberAndStatus(
              history.getOrderNumber(),
              memberNumber,
              RecommendationPointStatus.DEDUCTED
          );
      if (deducted.isEmpty()) {
        return true;
      }
    }

    return false;
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
    history
        .setRecommendationPhoneNumber(Objects.requireNonNullElse(recommendMember.getMobile(), ""));
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
        left = order.getRoadFullAddress();
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
    history.setPoint(getPaidPoint(order.getPayDateTime()));
    history.setStatus(RecommendationPointStatus.PAID);
    history.setOrderMemberName(orderMember.getName());

    return history;
  }

  private boolean isBlacklistAddress(Order order) {
    if ("경기 남양주시 오남읍 진건오남로 735-10 (한국아파트)".equals(order.getAddress())) {
      return order.getAddressSub().replaceAll("[^\\d]", "").equals("1041802");
    }
    if ("서울 도봉구 덕릉로 349 (주공4단지아파트)".equals(order.getRoadAddress())) {
      return order.getAddressSub().replaceAll("[^\\d]", "").equals("410810");
    }
    return false;
  }

  private RecommendationPointReason validateMobile(String orderMobile, String recommendMobile) {
    orderMobile = Objects.requireNonNullElse(orderMobile, "").replace("-", "");
    recommendMobile = Objects.requireNonNullElse(recommendMobile, "").replace("-", "");

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
        .findFirstByRecommendationMemberNumberOrderByCreateDateTimeDesc(memberNumber)
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
