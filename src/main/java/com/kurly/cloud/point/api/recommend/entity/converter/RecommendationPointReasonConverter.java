package com.kurly.cloud.point.api.recommend.entity.converter;

import com.kurly.cloud.point.api.recommend.domain.RecommendationPointReason;
import javax.persistence.AttributeConverter;

public class RecommendationPointReasonConverter implements
    AttributeConverter<RecommendationPointReason, Integer> {
  @Override public Integer convertToDatabaseColumn(RecommendationPointReason attribute) {
    return attribute.getValue();
  }

  @Override public RecommendationPointReason convertToEntityAttribute(Integer dbData) {
    return RecommendationPointReason.getByValue(dbData);
  }
}
