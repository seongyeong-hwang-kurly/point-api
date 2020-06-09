package com.kurly.cloud.point.api.point.adapter.in.controller.pub;

import com.kurly.cloud.api.common.config.KurlyUserPrincipal;
import com.kurly.cloud.api.common.domain.exception.ApiErrorResponse;
import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
import com.kurly.cloud.point.api.point.domain.BulkJobResult;
import com.kurly.cloud.point.api.point.domain.consume.BulkConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.CancelOrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.ConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.OrderConsumePointRequest;
import com.kurly.cloud.point.api.point.exception.CancelAmountExceedException;
import com.kurly.cloud.point.api.point.exception.NotEnoughPointException;
import com.kurly.cloud.point.api.point.port.in.ConsumePointPort;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController("PublicConsumeController")
public class ConsumeController {

  private final ConsumePointPort consumePointPort;

  @Secured("ROLE_ADMIN")
  @PostMapping(value = "/public/v1/consume", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity consume(@RequestBody @Valid ConsumePointRequest request,
                         @AuthenticationPrincipal KurlyUserPrincipal principal) {
    request.setActionMemberNumber(principal.getNo());
    try {
      consumePointPort.consume(request);
    } catch (NotEnoughPointException e) {
      throw new ApiErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    return ResponseEntity.noContent().build();
  }

  @Secured("ROLE_ADMIN")
  @PostMapping(value = "/public/v1/consume/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
  BulkJobResult bulkConsume(@RequestBody List<@Valid BulkConsumePointRequest> requests,
                            @AuthenticationPrincipal KurlyUserPrincipal principal) {
    BulkJobResult result = new BulkJobResult();
    requests.forEach(request -> {
      try {
        request.setActionMemberNumber(principal.getNo());
        consumePointPort.consume(request);
        result.addSuccess(request.getJobSeq());
      } catch (Exception e) {
        result.addFailed(request.getJobSeq());
        FileBeatLogger.error(e);
      }
    });
    return result;
  }

  @Secured("ROLE_ADMIN")
  @PostMapping(value = "/public/v1/consume/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity cancelConsume(CancelOrderConsumePointRequest request,
                               @AuthenticationPrincipal KurlyUserPrincipal principal)
      throws CancelAmountExceedException {
    request.setActionMemberNumber(principal.getNo());
    consumePointPort.cancelConsumeByOrder(request);
    return ResponseEntity.noContent().build();
  }

  @Secured("ROLE_USER")
  @PostMapping(value = "/public/v1/consume/order", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity consumeByOrder(@RequestBody @Valid OrderConsumePointRequest request,
                                @AuthenticationPrincipal KurlyUserPrincipal principal) {
    if (!request.getMemberNumber().equals(principal.getNo())) {
      throw new AccessDeniedException("권한이 없습니다");
    }
    try {
      consumePointPort.consumeByOrder(request);
    } catch (NotEnoughPointException e) {
      throw new ApiErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    return ResponseEntity.noContent().build();
  }
}
