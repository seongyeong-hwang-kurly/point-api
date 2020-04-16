/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.batch.expire;

import com.kurly.cloud.point.api.point.batch.expire.config.PointExpireJobConfig;
import com.kurly.cloud.point.api.point.repository.PointRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.data.domain.PageRequest;

public class PointExpireItemReader extends AbstractPagingItemReader<Long> {

  private final PointRepository pointRepository;
  private final LocalDateTime expireTime;

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
