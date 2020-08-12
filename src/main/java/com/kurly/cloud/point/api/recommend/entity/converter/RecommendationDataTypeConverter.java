package com.kurly.cloud.point.api.recommend.entity.converter;

import com.kurly.cloud.point.api.recommend.domain.RecommendationDataType;
import javax.persistence.AttributeConverter;

public class RecommendationDataTypeConverter implements
    AttributeConverter<RecommendationDataType, Integer> {
  @Override public Integer convertToDatabaseColumn(RecommendationDataType attribute) {
    return attribute.getValue();
  }

  @Override public RecommendationDataType convertToEntityAttribute(Integer dbData) {
    return RecommendationDataType.getByValue(dbData);
  }
}
