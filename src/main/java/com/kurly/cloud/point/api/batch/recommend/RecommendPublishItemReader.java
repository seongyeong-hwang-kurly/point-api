package com.kurly.cloud.point.api.batch.recommend;

import com.kurly.cloud.point.api.batch.config.PointBatchConfig;
import com.kurly.cloud.point.api.batch.order.entity.Order;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaPagingItemReader;

public class RecommendPublishItemReader extends JpaPagingItemReader<Order> {

  /**
   * 기본 생성자.
   */
  public RecommendPublishItemReader(EntityManagerFactory entityManagerFactory, int pageSize,
                                    String deliveredDate) {
    setName("recommendPublishItemReader");
    setEntityManagerFactory(entityManagerFactory);
    setPageSize(pageSize);
    setQueryString(getQueryString());
    setSaveState(false);
    LocalDateTime date = LocalDateTime.parse(deliveredDate, PointBatchConfig.DATE_TIME_FORMATTER);
    LocalDateTime from = date.withHour(0).withMinute(0).withSecond(0).withNano(0);
    LocalDateTime to = date.withHour(23).withMinute(59).withSecond(59).withNano(0);
    setParameterValues(getParameterValues(from, to));
  }

  Map<String, Object> getParameterValues(LocalDateTime from, LocalDateTime to) {
    HashMap<String, Object> params = new HashMap<>();
    params.put("from", from);
    params.put("to", to);
    return params;
  }

  private String getQueryString() {
    String query = "SELECT o FROM Order o"
        + " JOIN FETCH o.member "
        + " WHERE o.member.recommendMemberId <> '' "
        + " AND o.orderStatus = 4 "
        + " AND o.orderProcessCode IN (0, 21, 22) "
        + " AND o.deliveredDateTime BETWEEN :from AND :to "
        + " GROUP BY o.memberNumber";
    return query;
  }
}
