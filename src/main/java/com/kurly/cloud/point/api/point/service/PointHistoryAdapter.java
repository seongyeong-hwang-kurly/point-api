package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.adapter.out.dto.PointHistoryDto;
import com.kurly.cloud.point.api.point.domain.history.PublishPointHistoryRequest;
import com.kurly.cloud.point.api.point.port.out.PointHistoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointHistoryAdapter implements PointHistoryPort {

  private final PointHistoryService pointHistoryService;

  @Override public Page<PointHistoryDto> getPublishHistoryList(
      PublishPointHistoryRequest request) {

    if (request.getHistoryType().isEmpty() && request.getActionMemberNumber().isEmpty()) {
      return pointHistoryService.getPublishedByRegTime(
          request.getRegDateTimeFrom(),
          request.getRegDateTimeTo(),
          PageRequest.of(request.getPage(), request.getSize())
      ).map(PointHistoryDto::fromEntity);
    }

    if (request.getHistoryType().isEmpty()) {
      return pointHistoryService.getPublishedByActionMemberNumbers(
          request.getRegDateTimeFrom(),
          request.getRegDateTimeTo(),
          request.getActionMemberNumber(),
          PageRequest.of(request.getPage(), request.getSize())
      ).map(PointHistoryDto::fromEntity);
    }

    if (request.getActionMemberNumber().isEmpty()) {
      return pointHistoryService.getPublishedByHistoryTypes(
          request.getRegDateTimeFrom(),
          request.getRegDateTimeTo(),
          request.getHistoryType(),
          PageRequest.of(request.getPage(), request.getSize())
      ).map(PointHistoryDto::fromEntity);
    }

    return pointHistoryService.getPublishedByHistoryTypesAndActionMemberNumbers(
        request.getRegDateTimeFrom(),
        request.getRegDateTimeTo(),
        request.getHistoryType(),
        request.getActionMemberNumber(),
        PageRequest.of(request.getPage(), request.getSize())
    ).map(PointHistoryDto::fromEntity);
  }
}
