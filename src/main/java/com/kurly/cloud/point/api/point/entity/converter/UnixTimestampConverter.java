/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.entity.converter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.persistence.AttributeConverter;

public class UnixTimestampConverter implements AttributeConverter<LocalDateTime, Long> {
  @Override
  public Long convertToDatabaseColumn(LocalDateTime attribute) {
    return attribute != null ? Timestamp.valueOf(attribute).getTime() / 1000 : null;
  }

  @Override
  public LocalDateTime convertToEntityAttribute(Long dbData) {
    return new Timestamp(dbData * 1000).toLocalDateTime();
  }
}
