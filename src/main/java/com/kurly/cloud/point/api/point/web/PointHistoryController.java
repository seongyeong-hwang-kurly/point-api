package com.kurly.cloud.point.api.point.web;

import com.kurly.cloud.point.api.point.domain.history.PublishPointHistoryRequest;
import com.kurly.cloud.point.api.point.service.PointHistoryUseCase;
import com.kurly.cloud.point.api.point.web.dto.PointHistoryDto;
import com.kurly.cloud.point.api.point.web.dto.SimplePageImpl;
import java.util.Collections;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("PrivatePointHistoryController")
public class PointHistoryController {

  private final PointHistoryUseCase pointHistoryUseCase;

  @RequestMapping(value = "/v1/point-history/publish",
      method = {RequestMethod.GET, RequestMethod.POST})
  SimplePageImpl<PointHistoryDto> getPublishHistoryList(
      @RequestBody PublishPointHistoryRequest request) {
    if (Objects.isNull(request.getActionMemberNumber())) {
      request.setActionMemberNumber(Collections.emptyList());
    }
    if (Objects.isNull(request.getHistoryType())) {
      request.setHistoryType(Collections.emptyList());
    }
    return SimplePageImpl.transform(pointHistoryUseCase.getPublishHistoryList(request));
  }
}
