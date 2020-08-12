package com.kurly.cloud.point.api.recommend.entity.converter;

import com.kurly.cloud.point.api.recommend.domain.RecommendationDelayType;
import javax.persistence.AttributeConverter;

public class RecommendationDelayTypeConverter implements
    AttributeConverter<RecommendationDelayType, Integer> {
  @Override public Integer convertToDatabaseColumn(RecommendationDelayType attribute) {
    return attribute.getValue();
  }

  @Override public RecommendationDelayType convertToEntityAttribute(Integer dbData) {
    return RecommendationDelayType.getByValue(dbData);
  }
}
