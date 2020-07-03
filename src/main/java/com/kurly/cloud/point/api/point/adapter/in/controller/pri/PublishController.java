package com.kurly.cloud.point.api.point.adapter.in.controller.pri;

import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
import com.kurly.cloud.point.api.point.adapter.in.dto.PublishResultDto;
import com.kurly.cloud.point.api.point.domain.BulkJobResult;
import com.kurly.cloud.point.api.point.domain.publish.BulkPublishPointRequest;
import com.kurly.cloud.point.api.point.domain.publish.CancelPublishOrderPointRequest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.port.in.PublishPointPort;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController("PrivatePublishController")
public class PublishController {

  private final PublishPointPort publishPointPort;

  @PostMapping(value = "/v1/publish", consumes = MediaType.APPLICATION_JSON_VALUE)
  PublishResultDto publish(@RequestBody @Valid PublishPointRequest request) {
    return PublishResultDto.fromEntity(publishPointPort.publish(request));
  }

  @PostMapping(value = "/v1/publish/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
  BulkJobResult bulkPublish(@RequestBody List<@Valid BulkPublishPointRequest> requests) {
    BulkJobResult result = new BulkJobResult();
    requests.forEach(request -> {
      try {
        Point publish = publishPointPort.publish(request);
        result.addSuccess(request.getJobSeq(), publish.getSeq());
      } catch (Exception e) {
        result.addFailed(request.getJobSeq());
        FileBeatLogger.error(e);
      }
    });
    return result;
  }

  @PostMapping(value = "/v1/publish/order-cancel")
  ResponseEntity<?> cancelPublish(@RequestBody @Valid CancelPublishOrderPointRequest request) {
    publishPointPort.cancelPublishByOrder(request);
    return ResponseEntity.noContent().build();
  }
}
