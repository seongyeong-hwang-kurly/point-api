package com.kurly.cloud.point.api.batch.publish;

import com.kurly.cloud.point.api.batch.config.PointBatchConfig;
import com.kurly.cloud.point.api.batch.order.entity.Order;
import org.springframework.batch.item.database.JpaPagingItemReader;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class PointOrderPublishItemReader extends JpaPagingItemReader<Order> {

  /**
   * 기본 생성자.
   */
  public PointOrderPublishItemReader(EntityManagerFactory entityManagerFactory,
                                     int pageSize,
                                     String publishDateStr) {
    setName("pointOrderPublishItemReader");
    setEntityManagerFactory(entityManagerFactory);
    setPageSize(pageSize);
    setQueryString(getQueryString());
    setSaveState(false);
    LocalDateTime publishDate =
        LocalDateTime.parse(publishDateStr, PointBatchConfig.DATE_TIME_FORMATTER);
    LocalDateTime from = publishDate.minusDays(2).withHour(0).withMinute(0).withSecond(0).withNano(0);
    LocalDateTime to = publishDate.minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(0);
    setParameterValues(getParameterValues(from, to));
  }

  Map<String, Object> getParameterValues(LocalDateTime from, LocalDateTime to) {
    HashMap<String, Object> params = new HashMap<>();
    params.put("from", from);
    params.put("to", to);
    return params;
  }

  private String getQueryString() {
    String query = "SELECT DISTINCT o FROM Order o"
        + " LEFT JOIN o.orderDynamicColumn ON o.orderDynamicColumn.column = 'point_ratio'"
        + " WHERE o.memberNumber <> 0 "
        + " AND o.publishPoint > 0 "
        + " AND o.orderStatus = 4 "
        + " AND o.orderProcessCode IN (0, 21, 22) "
        + " AND o.deliveredDateTime BETWEEN :from AND :to ";
    return query;
  }

}
