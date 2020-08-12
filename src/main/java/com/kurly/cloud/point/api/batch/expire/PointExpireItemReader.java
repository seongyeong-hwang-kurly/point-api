package com.kurly.cloud.point.api.batch.expire;

import com.kurly.cloud.point.api.batch.expire.config.PointExpireJobConfig;
import com.kurly.cloud.point.api.point.repository.PointRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.data.domain.PageRequest;

public class PointExpireItemReader extends AbstractPagingItemReader<Long> {

  private final PointRepository pointRepository;
  private final LocalDateTime expireTime;

  /**
   * 기본 생성자.
   */
  public PointExpireItemReader(PointRepository pointRepository,
                               int pageSize,
                               String expireTime) {
    setName("expirePointMemberNumberReader");
    setPageSize(pageSize);
    this.pointRepository = pointRepository;
    this.expireTime = LocalDateTime.parse(expireTime, PointExpireJobConfig.DATE_TIME_FORMATTER);
  }

  @Override protected void doReadPage() {
    if (results == null) {
      results = new ArrayList<>();
    } else {
      results.clear();
    }

    List<Long> expiredMembers = pointRepository
        .findAllMemberNumberHasExpiredPoint(expireTime, PageRequest.of(0, getPageSize()));

    results.addAll(expiredMembers);
  }

  @Override protected void doJumpToPage(int itemIndex) {
  }

}
