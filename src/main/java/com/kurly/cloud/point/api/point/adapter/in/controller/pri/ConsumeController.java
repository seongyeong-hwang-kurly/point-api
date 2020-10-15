package com.kurly.cloud.point.api.point.adapter.in.controller.pri;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController("PrivateConsumeController")
public class ConsumeController {

  private final ConsumePointPort consumePointPort;

  @PostMapping(value = "/v1/consume/order", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<?> consumeByOrder(@RequestBody @Valid OrderConsumePointRequest request) {
    try {
      consumePointPort.consumeByOrder(request);
    } catch (NotEnoughPointException e) {
      throw new ApiErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    return ResponseEntity.noContent().build();
  }
  
  @PostMapping(value = "/v1/consume", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<?> consume(@RequestBody @Valid ConsumePointRequest request) {
    try {
      consumePointPort.consume(request);
    } catch (NotEnoughPointException e) {
      throw new ApiErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "/v1/consume/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
  BulkJobResult bulkConsume(
      @RequestBody List<@Valid BulkConsumePointRequest> requests) {
    BulkJobResult result = new BulkJobResult();
    requests.forEach(request -> {
      try {
        Executors.newSingleThreadExecutor().submit(() -> {
          try {
            consumePointPort.consume(request);
            result.addSuccess(request.getJobSeq());
          } catch (Exception e) {
            result.addFailed(request.getJobSeq());
            FileBeatLogger.error(e);
          }
        }).get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    });
    return result;
  }

  @PostMapping(value = "/v1/consume/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<?> cancelConsume(@RequestBody @Valid CancelOrderConsumePointRequest request) {
    request.setActionMemberNumber(request.getMemberNumber());
    try {
      consumePointPort.cancelConsumeByOrder(request);
    } catch (CancelAmountExceedException e) {
      throw new ApiErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    return ResponseEntity.noContent().build();
  }

}
