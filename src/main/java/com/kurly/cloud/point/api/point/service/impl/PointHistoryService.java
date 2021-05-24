package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.domain.history.PublishPointHistoryRequest;
import com.kurly.cloud.point.api.point.service.PointHistoryUseCase;
import com.kurly.cloud.point.api.point.web.dto.PointHistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointHistoryService implements PointHistoryUseCase {

  private final PointHistoryDomainService pointHistoryDomainService;

  @Transactional(readOnly = true)
  @Override
  public Page<PointHistoryDto> getPublishHistoryList(
      PublishPointHistoryRequest request) {

    if (request.getHistoryType().isEmpty() && request.getActionMemberNumber().isEmpty()) {
      return pointHistoryDomainService.getPublishedByRegTime(
          request.getRegDateTimeFrom(),
          request.getRegDateTimeTo(),
          PageRequest.of(request.getPage(), request.getSize())
      ).map(PointHistoryDto::fromEntity);
    }

    if (request.getHistoryType().isEmpty()) {
      return pointHistoryDomainService.getPublishedByActionMemberNumbers(
          request.getRegDateTimeFrom(),
          request.getRegDateTimeTo(),
          request.getActionMemberNumber(),
          PageRequest.of(request.getPage(), request.getSize())
      ).map(PointHistoryDto::fromEntity);
    }

    if (request.getActionMemberNumber().isEmpty()) {
      return pointHistoryDomainService.getPublishedByHistoryTypes(
          request.getRegDateTimeFrom(),
          request.getRegDateTimeTo(),
          request.getHistoryType(),
          PageRequest.of(request.getPage(), request.getSize())
      ).map(PointHistoryDto::fromEntity);
    }

    return pointHistoryDomainService.getPublishedByHistoryTypesAndActionMemberNumbers(
        request.getRegDateTimeFrom(),
        request.getRegDateTimeTo(),
        request.getHistoryType(),
        request.getActionMemberNumber(),
        PageRequest.of(request.getPage(), request.getSize())
    ).map(PointHistoryDto::fromEntity);
  }
}
