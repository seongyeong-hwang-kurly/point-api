package com.kurly.cloud.point.api.point.port.out;

import com.kurly.cloud.point.api.point.adapter.out.dto.PointHistoryDto;
import com.kurly.cloud.point.api.point.domain.history.PublishPointHistoryRequest;
import org.springframework.data.domain.Page;

public interface PointHistoryPort {
  Page<PointHistoryDto> getPublishHistoryList(PublishPointHistoryRequest request);
}
