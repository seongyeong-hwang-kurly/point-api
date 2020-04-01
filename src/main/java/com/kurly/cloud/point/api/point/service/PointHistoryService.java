package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.domain.PointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.entity.PointHistory;
import com.kurly.cloud.point.api.point.repository.PointHistoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
class PointHistoryService {

  private final PointHistoryRepository pointHistoryRepository;

  PointHistory insertHistory(PointHistoryInsertRequest request) {
    return pointHistoryRepository.save(request.toEntity());
  }

  List<PointHistory> getByPointSeq(long pointSeq) {
    return pointHistoryRepository.findAllByPoint_Seq(pointSeq,
        Sort.by(Sort.Direction.DESC, "regTime"));
  }

  List<PointHistory> getByOrderNumber(long orderNumber) {
    return pointHistoryRepository.findAllByOrderNumber(orderNumber,
        Sort.by(Sort.Direction.DESC, "regTime"));
  }
}
