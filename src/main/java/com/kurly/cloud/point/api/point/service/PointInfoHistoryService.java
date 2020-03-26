/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.domain.PointInfoHistoryDto;
import com.kurly.cloud.point.api.point.domain.PointInfoHistoryInsertRequestDto;
import com.kurly.cloud.point.api.point.domain.PointInfoHistoryListRequestDto;
import com.kurly.cloud.point.api.point.entity.PointInfoHistory;
import com.kurly.cloud.point.api.point.repository.PointInfoHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PointInfoHistoryService {

  private final PointInfoHistoryRepository pointInfoHistoryRepository;

  public PointInfoHistoryDto insertHistory(PointInfoHistoryInsertRequestDto request) {
    PointInfoHistory pointInfoHistory = pointInfoHistoryRepository.save(request.toEntity());
    return PointInfoHistoryDto.fromEntity(pointInfoHistory);
  }

  public Page<PointInfoHistoryDto> getHistoryList(PointInfoHistoryListRequestDto request) {
    Page<PointInfoHistory> pointInfoHistories;

    if (request.isIncludeHidden()) {
      pointInfoHistories = pointInfoHistoryRepository.getAllByMemberNumber(
          request.getMemberNumber()
          , PageRequest.of(request.getPage(), request.getSize(), request.getSort())
      );
    } else {
      pointInfoHistories = pointInfoHistoryRepository.getAllByMemberNumberAndHidden(
          request.getMemberNumber()
          , false
          , PageRequest.of(request.getPage(), request.getSize(), request.getSort())
      );
    }

    return pointInfoHistories.map(PointInfoHistoryDto::fromEntity);
  }

}
