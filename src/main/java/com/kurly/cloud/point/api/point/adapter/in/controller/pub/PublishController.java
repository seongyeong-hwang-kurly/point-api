package com.kurly.cloud.point.api.point.adapter.in.controller.pub;

import com.kurly.cloud.api.common.config.KurlyUserPrincipal;
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
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController("PublicPublishController")
public class PublishController {

  private final PublishPointPort publishPointPort;

  @Secured("ROLE_ADMIN")
  @PostMapping(value = "/public/v1/publish", consumes = MediaType.APPLICATION_JSON_VALUE)
  PublishResultDto publish(@RequestBody @Valid PublishPointRequest request,
                           @AuthenticationPrincipal KurlyUserPrincipal principal) {
    request.setActionMemberNumber(principal.getNo());
    return PublishResultDto.fromEntity(publishPointPort.publish(request));
  }

  @Secured("ROLE_ADMIN")
  @PostMapping(value = "/public/v1/publish/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
  BulkJobResult bulkPublish(@RequestBody List<@Valid BulkPublishPointRequest> requests,
                            @AuthenticationPrincipal KurlyUserPrincipal principal) {
    BulkJobResult result = new BulkJobResult();
    requests.forEach(request -> {
      try {
        request.setActionMemberNumber(principal.getNo());
        Point publish = publishPointPort.publish(request);
        result.addSuccess(request.getJobSeq(), publish.getSeq());
      } catch (Exception e) {
        result.addFailed(request.getJobSeq());
        FileBeatLogger.error(e);
      }
    });
    return result;
  }

  @Secured("ROLE_ADMIN")
  @PostMapping(value = "/public/v1/publish/order-cancel")
  ResponseEntity cancelPublish(CancelPublishOrderPointRequest request,
                               @AuthenticationPrincipal KurlyUserPrincipal principal) {
    request.setActionMemberNumber(principal.getNo());
    publishPointPort.cancelPublishByOrder(request);
    return ResponseEntity.noContent().build();
  }
}
