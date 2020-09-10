package com.kurly.cloud.point.api.batch.publish;

import com.kurly.cloud.point.api.order.entity.Order;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import org.springframework.batch.item.ItemProcessor;

public class PointOrderPublishItemProcessor implements ItemProcessor<Order, PublishPointRequest> {
  @Override public PublishPointRequest process(Order item) throws Exception {
    float pointRatio = Float.parseFloat(item.getOrderDynamicColumns().get(0).getValue());
    return PublishPointRequest.builder()
        .pointRatio(pointRatio)
        .memberNumber(item.getMemberNumber())
        .orderNumber(item.getOrderNumber())
        .point(item.getPublishPoint())
        .build();
  }
}
