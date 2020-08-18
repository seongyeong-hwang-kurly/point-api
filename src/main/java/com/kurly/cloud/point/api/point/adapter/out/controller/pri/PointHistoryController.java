package com.kurly.cloud.point.api.point.adapter.out.controller.pri;

import com.kurly.cloud.point.api.point.adapter.out.dto.PointHistoryDto;
import com.kurly.cloud.point.api.point.adapter.out.dto.SimplePageImpl;
import com.kurly.cloud.point.api.point.domain.history.PublishPointHistoryRequest;
import com.kurly.cloud.point.api.point.port.out.PointHistoryPort;
import java.util.Collections;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("PrivatePointHistoryController")
public class PointHistoryController {

  private final PointHistoryPort pointHistoryPort;

  @GetMapping("/v1/point-history/publish")
  SimplePageImpl<PointHistoryDto> getPublishHistoryList(
      @RequestBody PublishPointHistoryRequest request) {
    if (Objects.isNull(request.getActionMemberNumber())) {
      request.setActionMemberNumber(Collections.emptyList());
    }
    if (Objects.isNull(request.getHistoryType())) {
      request.setHistoryType(Collections.emptyList());
    }
    return SimplePageImpl.transform(pointHistoryPort.getPublishHistoryList(request));
  }
}
