package com.kurly.cloud.point.api.batch.recommend.entity.converter;

import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationPointStatus;
import javax.persistence.AttributeConverter;

public class RecommendationPointStatusConverter implements
    AttributeConverter<RecommendationPointStatus, Integer> {
  @Override public Integer convertToDatabaseColumn(RecommendationPointStatus attribute) {
    return attribute.getValue();
  }

  @Override public RecommendationPointStatus convertToEntityAttribute(Integer dbData) {
    return RecommendationPointStatus.getByValue(dbData);
  }
}
