package com.kurly.cloud.point.api.point.repository;

import com.kurly.cloud.point.api.point.entity.PointHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
  List<PointHistory> findAllByPoint_Seq(long pointSeq, Sort sort);

  List<PointHistory> findAllByOrderNumberAndHistoryType(long orderNumber, int historyType,
                                                        Sort sort);

  Page<PointHistory> findAllByAmountGreaterThanAndRegTimeBetween(long amount,
                                                                 LocalDateTime regTimeFrom,
                                                                 LocalDateTime regTimeTo,
                                                                 Pageable pageable);

  Page<PointHistory> findAllByAmountGreaterThanAndRegTimeBetweenAndActionMemberNumberIn(
      long amount,
      LocalDateTime regTimeFrom,
      LocalDateTime regTimeTo,
      List<Long> actionMemberNumber,
      Pageable pageable
  );

  Page<PointHistory> findAllByAmountGreaterThanAndRegTimeBetweenAndHistoryTypeIn(
      long amount,
      LocalDateTime regTimeFrom,
      LocalDateTime regTimeTo,
      List<Integer> historyType,
      Pageable pageable
  );

  @SuppressWarnings("checkstyle:LineLength")
  Page<PointHistory> findAllByAmountGreaterThanAndRegTimeBetweenAndActionMemberNumberInAndHistoryTypeIn(
      long amount,
      LocalDateTime regTimeFrom,
      LocalDateTime regTimeTo,
      List<Long> actionMemberNumber,
      List<Integer> historyType,
      Pageable pageable
  );

  List<PointHistory> findAllByPointSeq(long poing);
}
