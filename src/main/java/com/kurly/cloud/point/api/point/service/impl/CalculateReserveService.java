package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.domain.calculate.ReserveCalculateRequest;
import com.kurly.cloud.point.api.point.domain.calculate.ReserveCalculateResponse;
import com.kurly.cloud.point.api.point.service.CalculateReserveUseCase;
import com.kurly.cloud.point.api.point.util.PointReserveCalculator;
import com.kurly.cloud.point.api.point.util.PointReserveCalculator.Result;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CalculateReserveService implements CalculateReserveUseCase {

  @Override
  public ReserveCalculateResponse calculate(ReserveCalculateRequest request) {

    List<ReserveCalculateResponse.Item> calculatedItems = calculateProducts(request);

    return new ReserveCalculateResponse(calculatedItems,
        calculatedItems.stream()
            .mapToInt(ReserveCalculateResponse.Item::getTotalReserve)
            .sum());
  }

  private List<ReserveCalculateResponse.Item> calculateProducts(ReserveCalculateRequest request) {
    return request.getProducts().stream().map(item -> {
      Result calculated = PointReserveCalculator.calculate(
          item.getProductReserveType(),
          item.getProductReserveValue(),
          item.getPrice(),
          request.getMemberReserveRatio());

      return new ReserveCalculateResponse.Item(item.getPrice(),
          item.getQuantity(),
          item.getTotalPrice(),
          item.getContentProductNo(),
          item.getDealProductNo(),
          calculated.getReserveType(),
          calculated.getReserveValue(),
          calculated.getReserve(),
          calculated.getReserve() * item.getQuantity());
    }).collect(Collectors.toUnmodifiableList());
  }
}
