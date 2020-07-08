package com.kurly.cloud.point.api.point.entity.converter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.persistence.AttributeConverter;

public class UnixTimestampConverter implements AttributeConverter<LocalDateTime, Long> {
  @Override
  public Long convertToDatabaseColumn(LocalDateTime attribute) {
    return attribute != null ? Timestamp.valueOf(attribute).getTime() / 1000 : 0;
  }

  @Override
  public LocalDateTime convertToEntityAttribute(Long dbData) {
    return dbData == 0 ? null : new Timestamp(dbData * 1000).toLocalDateTime();
  }
}
