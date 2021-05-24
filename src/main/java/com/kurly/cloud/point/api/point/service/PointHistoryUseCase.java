package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.domain.history.PublishPointHistoryRequest;
import com.kurly.cloud.point.api.point.web.dto.PointHistoryDto;
import org.springframework.data.domain.Page;

public interface PointHistoryUseCase {
  Page<PointHistoryDto> getPublishHistoryList(PublishPointHistoryRequest request);
}
