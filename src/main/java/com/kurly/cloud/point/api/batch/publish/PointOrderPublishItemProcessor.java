package com.kurly.cloud.point.api.batch.publish;

import com.kurly.cloud.point.api.batch.order.entity.Order;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import java.util.Objects;
import org.springframework.batch.item.ItemProcessor;

public class PointOrderPublishItemProcessor implements ItemProcessor<Order, PublishPointRequest> {
  @Override public PublishPointRequest process(Order item) throws Exception {
    float pointRatio = 0;
    if (Objects.nonNull(item.getOrderDynamicColumn())) {
      pointRatio = Float.parseFloat(item.getOrderDynamicColumn().getValue());
    }
    return PublishPointRequest.builder()
        .pointRatio(pointRatio)
        .memberNumber(item.getMemberNumber())
        .orderNumber(item.getOrderNumber())
        .point(item.getPublishPoint())
        .build();
  }
}
