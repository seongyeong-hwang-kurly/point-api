package com.kurly.cloud.point.api.point.domain.history;

import com.fasterxml.jackson.annotation.JsonValue;
import com.kurly.cloud.point.api.point.exception.HistoryTypeNotFoundException;
import java.text.MessageFormat;
import java.util.Arrays;

public enum HistoryType {

  TYPE_1(1, "주문 시 적립", "[구매적립] 주문({0}) {1}% 적립"),
  TYPE_2(2, "적립금 사용 후 주문취소", "[사용취소] 주문({0}) 결제 시 사용한 적립금 반환"),
  TYPE_11(11, "불편공감", ""),
  TYPE_12(12, "추천인지급", "[이벤트적립금] 친구초대적립금"),
  TYPE_13(13, "피추천인지급", "[이벤트적립금] 친구초대적립금 ({0})"),
  TYPE_14(14, "방송협찬", ""),
  TYPE_15(15, "일반후기", ""),
  TYPE_16(16, "사진후기", ""),
  TYPE_17(17, "일반후기x2", ""),
  TYPE_18(18, "사진후기x2", ""),
  TYPE_19(19, "베스트상품후기", ""),
  TYPE_20(20, "인스타VIP/협찬", ""),
  TYPE_21(21, "인스타후기이벤트", ""),
  TYPE_22(22, "파워블로거/VIP", ""),
  TYPE_23(23, "블로그후기이벤트", ""),
  TYPE_24(24, "고객설문", ""),
  TYPE_25(25, "인터뷰", ""),
  TYPE_26(26, "컨텐츠 촬영", ""),
  TYPE_27(27, "HR 복지금", ""),
  TYPE_28(28, "기타", ""),
  TYPE_29(29, "이벤트", ""),
  TYPE_30(30, "쇼핑지원금", ""),
  TYPE_50(50, "채무 적립금 상환", "[적립지급] 마이너스 적립금 상쇄"),
  TYPE_100(100, "주문 시 사용", "[사용] 주문({0}) 결제 시 사용"),
  TYPE_101(101, "주문 취소", "[적립취소] 주문({0}) 취소"),
  TYPE_102(102, "기타", ""),
  TYPE_103(103, "소멸", "[적립금소멸] 적립금 유효기간 만료"),
  TYPE_104(104, "탈퇴 소멸", ""),
  TYPE_105(105, "마이너스 적립금 상쇄", "[적립차감] 이전 차감 시 잔여적립금 부족액에 대한 차감");

  private final int value;
  private final String desc;
  private final String msg;

  HistoryType(int value, String desc, String msg) {
    this.value = value;
    this.desc = desc;
    this.msg = msg;
  }

  @JsonValue
  public int getValue() {
    return value;
  }

  public String getDesc() {
    return desc;
  }

  /**
   * GetByValue.
   */
  public static HistoryType getByValue(int value) throws HistoryTypeNotFoundException {
    return Arrays.stream(HistoryType.values())
        .filter(deliveryPolicyType -> deliveryPolicyType.getValue() == value)
        .findAny()
        .orElseThrow(() -> new HistoryTypeNotFoundException(value));
  }

  public String buildMessage(Object... params) {
    return MessageFormat.format(msg, params);
  }

  public String buildMessage() {
    return msg;
  }

}
