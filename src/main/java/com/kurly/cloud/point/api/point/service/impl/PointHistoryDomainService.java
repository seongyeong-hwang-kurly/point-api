package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.history.PointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.entity.PointHistory;
import com.kurly.cloud.point.api.point.repository.PointHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
class PointHistoryDomainService {

  private final PointHistoryRepository pointHistoryRepository;

  PointHistory insertHistory(PointHistoryInsertRequest request) {
    return pointHistoryRepository.save(request.toEntity());
  }

  List<PointHistory> getByPointSeq(long pointSeq) {
    return pointHistoryRepository.findAllByPoint_Seq(pointSeq,
        Sort.by(Sort.Direction.DESC, "regTime"));
  }

  List<PointHistory> getConsumedByOrderNumber(long orderNumber) {
    return pointHistoryRepository.findAllByOrderNumberAndHistoryType(orderNumber,
        HistoryType.TYPE_100.getValue(),
        Sort.by(Sort.Direction.DESC, "regTime"));
  }

  Page<PointHistory> getPublishedByRegTime(LocalDateTime from, LocalDateTime to,
                                           Pageable pageable) {
    return pointHistoryRepository
        .findAllByAmountGreaterThanAndRegTimeBetween(0, from, to, pageable);
  }

  Page<PointHistory> getPublishedByHistoryTypes(LocalDateTime from, LocalDateTime to,
                                                List<Integer> historyType, Pageable pageable) {
    return pointHistoryRepository
        .findAllByAmountGreaterThanAndRegTimeBetweenAndHistoryTypeIn(0,
            from, to, historyType, pageable);
  }

  Page<PointHistory> getPublishedByActionMemberNumbers(LocalDateTime from, LocalDateTime to,
                                                       List<Long> actionMemberNumber,
                                                       Pageable pageable) {
    return pointHistoryRepository
        .findAllByAmountGreaterThanAndRegTimeBetweenAndActionMemberNumberIn(0,
            from, to, actionMemberNumber, pageable);
  }

  Page<PointHistory> getPublishedByHistoryTypesAndActionMemberNumbers(LocalDateTime from,
                                                                      LocalDateTime to,
                                                                      List<Integer> historyType,
                                                                      List<Long> actionMemberNumber,
                                                                      Pageable pageable) {
    return pointHistoryRepository
        .findAllByAmountGreaterThanAndRegTimeBetweenAndActionMemberNumberInAndHistoryTypeIn(0,
            from, to, actionMemberNumber, historyType, pageable);
  }
}
