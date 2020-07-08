package com.kurly.cloud.point.api.point.service;

import static org.assertj.core.api.Assertions.assertThat;


import com.kurly.cloud.point.api.point.domain.consume.ConsumeOrderComparator;
import com.kurly.cloud.point.api.point.entity.Point;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConsumeOrderTest {
  @Test
  @DisplayName("적립금 사용 우선 순위 반영을 위한 정렬 검증")
  void test() {
    Point.PointBuilder builder = Point.builder();

    //무상 1월1일 만료
    Point p1 =
        builder.payment(false).settle(false).expireTime(LocalDateTime.of(2020, 1, 1, 1, 1)).build();
    //유상 미결제 1월1일 만료
    Point p2 =
        builder.payment(false).settle(true).expireTime(LocalDateTime.of(2020, 1, 1, 1, 1)).build();
    //유상 결제 1월1일 만료
    Point p3 =
        builder.payment(true).settle(true).expireTime(LocalDateTime.of(2020, 1, 1, 1, 1)).build();
    //무상 2월 1일 만료
    Point p4 =
        builder.payment(false).settle(false).expireTime(LocalDateTime.of(2020, 2, 1, 1, 1)).build();
    //유상 미결제 2월1일 만료
    Point p5 =
        builder.payment(false).settle(true).expireTime(LocalDateTime.of(2020, 2, 1, 1, 1)).build();
    //유상 결제 2월1일 만료
    Point p6 =
        builder.payment(true).settle(true).expireTime(LocalDateTime.of(2020, 2, 1, 1, 1)).build();
    //유상 미결제 만료일 없음
    Point p7 = builder.payment(false).settle(true).expireTime(null).build();
    //유상 결제 만료일 없음
    Point p8 = builder.payment(true).settle(true).expireTime(null).build();

    List<Point> pointList = Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8);

    for (int i = 0; i < 1000; i++) {
      Collections.shuffle(pointList);
      pointList.sort(ConsumeOrderComparator.getInstance());
      assertThat(pointList.get(0)).isEqualTo(p1);
      assertThat(pointList.get(1)).isEqualTo(p2);
      assertThat(pointList.get(2)).isEqualTo(p3);
      assertThat(pointList.get(3)).isEqualTo(p4);
      assertThat(pointList.get(4)).isEqualTo(p5);
      assertThat(pointList.get(5)).isEqualTo(p6);
      assertThat(pointList.get(6)).isEqualTo(p7);
      assertThat(pointList.get(7)).isEqualTo(p8);
    }
  }
}
