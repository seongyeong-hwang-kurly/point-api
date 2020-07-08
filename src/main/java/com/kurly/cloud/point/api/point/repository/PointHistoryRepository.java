package com.kurly.cloud.point.api.point.repository;

import com.kurly.cloud.point.api.point.entity.PointHistory;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
  List<PointHistory> findAllByPoint_Seq(long pointSeq, Sort sort);

  List<PointHistory> findAllByOrderNumberAndHistoryType(long orderNumber, int historyType,
                                                        Sort sort);
}
