package com.kurly.cloud.point.api.point.adapter.in;

import com.kurly.cloud.api.common.config.KurlyUserPrincipal;
import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
import com.kurly.cloud.point.api.point.domain.BulkJobResult;
import com.kurly.cloud.point.api.point.domain.publish.BulkPublishPointRequest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
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
@RestController
public class PublishController {

  private final PublishPointPort publishPointPort;

  @Secured("ROLE_ADMIN")
  @PostMapping(value = "/public/v1/publish", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity publish(@RequestBody @Valid PublishPointRequest request,
                         @AuthenticationPrincipal KurlyUserPrincipal kurlyUserPrincipal) {
    request.setActionMemberNumber(kurlyUserPrincipal.getNo());
    publishPointPort.publish(request);
    return ResponseEntity.noContent().build();
  }

  @Secured("ROLE_ADMIN")
  @PostMapping(value = "/public/v1/publish/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
  BulkJobResult bulkPublish(@RequestBody List<@Valid BulkPublishPointRequest> requests,
                            @AuthenticationPrincipal KurlyUserPrincipal kurlyUserPrincipal) {
    BulkJobResult result = new BulkJobResult();
    requests.forEach(request -> {
      try {
        request.setActionMemberNumber(kurlyUserPrincipal.getNo());
        publishPointPort.publish(request);
        result.addSuccess(request.getJobSeq());
      } catch (Exception e) {
        result.addFailed(request.getJobSeq());
        FileBeatLogger.error(e);
      }
    });
    return result;
  }
}
