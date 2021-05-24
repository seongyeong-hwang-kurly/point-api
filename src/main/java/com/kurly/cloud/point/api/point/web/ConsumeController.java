package com.kurly.cloud.point.api.point.web;

import com.kurly.cloud.api.common.domain.exception.ApiErrorResponse;
import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
import com.kurly.cloud.point.api.point.domain.BulkJobResult;
import com.kurly.cloud.point.api.point.domain.consume.BulkConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.CancelOrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.ConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.OrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.exception.CancelAmountExceedException;
import com.kurly.cloud.point.api.point.exception.HistoryTypeNotFoundException;
import com.kurly.cloud.point.api.point.exception.NotEnoughPointException;
import com.kurly.cloud.point.api.point.service.ConsumePointUseCase;
import com.kurly.cloud.point.api.point.util.SlackBot;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

  private final ConsumePointUseCase consumePointUseCase;

  private final SlackBot slackBot;

  @PostMapping(value = "/v1/consume/order", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<?> consumeByOrder(@RequestBody @Valid OrderConsumePointRequest request) {
    try {
      consumePointUseCase.consumeByOrder(request);
    } catch (NotEnoughPointException e) {
      throw new ApiErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "/v1/consume", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<?> consume(@RequestBody @Valid ConsumePointRequest request) {
    try {
      consumePointUseCase.consume(request);
    } catch (NotEnoughPointException e) {
      throw new ApiErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "/v1/consume/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
  BulkJobResult bulkConsume(
      @RequestBody List<@Valid BulkConsumePointRequest> requests) {
    BulkJobResult result = new BulkJobResult();
    Map<String, Map<Integer, Long>> jobSummary = new HashMap<>();
    jobSummary.put("hit", new HashMap<>());
    jobSummary.put("amount", new HashMap<>());
    requests.forEach(request -> {
      try {
        Executors.newSingleThreadExecutor().submit(() -> {
          try {
            consumePointUseCase.consume(request);
            putSummary(jobSummary, request);
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
    reportSummary(jobSummary);
    return result;
  }

  private void putSummary(Map<String, Map<Integer, Long>> jobSummary,
                          BulkConsumePointRequest request) {
    jobSummary.get("amount").compute(request.getHistoryType(), (type, amount) -> {
      amount = Objects.requireNonNullElse(amount, 0L);
      amount += request.getPoint();
      return amount;
    });
    jobSummary.get("hit").compute(request.getHistoryType(), (type, amount) -> {
      amount = Objects.requireNonNullElse(amount, 0L);
      amount += 1;
      return amount;
    });
  }

  private void reportSummary(Map<String, Map<Integer, Long>> jobSummary) {
    if (jobSummary.get("amount").size() == 0) {
      return;
    }
    List<String> messages = new ArrayList<>();
    messages.add("*대량차감이 완료 되었습니다*");

    jobSummary.get("amount").forEach((type, amount) -> {
      try {
        HistoryType historyType = HistoryType.getByValue(type);
        messages.add(MessageFormat.format(">차감 사유 : {0} ({1})",
            historyType.getDesc(), historyType.getValue()));
      } catch (HistoryTypeNotFoundException e) {
        messages.add(MessageFormat.format(">차감 사유 : {0}", type));
      }
      messages.add(MessageFormat.format(">차감 건수 : {0}", jobSummary.get("hit").get(type)));
      messages.add(MessageFormat.format(">차감 수량 : {0}", amount));
      messages.add("");
    });

    slackBot.postMessage(String.join("\n", messages));
  }

  @PostMapping(value = "/v1/consume/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<?> cancelConsume(@RequestBody @Valid CancelOrderConsumePointRequest request) {
    request.setActionMemberNumber(request.getMemberNumber());
    try {
      consumePointUseCase.cancelConsumeByOrder(request);
    } catch (CancelAmountExceedException e) {
      throw new ApiErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    return ResponseEntity.noContent().build();
  }

}
