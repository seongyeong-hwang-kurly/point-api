package com.kurly.cloud.point.api.point.adapter.in.controller.pri;

import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
import com.kurly.cloud.point.api.point.adapter.in.dto.PublishResultDto;
import com.kurly.cloud.point.api.point.domain.BulkJobResult;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.BulkPublishPointRequest;
import com.kurly.cloud.point.api.point.domain.publish.CancelPublishOrderPointRequest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.exception.HistoryTypeNotFoundException;
import com.kurly.cloud.point.api.point.port.in.PublishPointPort;
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

  private final SlackBot slackBot;

  @PostMapping(value = "/v1/publish", consumes = MediaType.APPLICATION_JSON_VALUE)
  PublishResultDto publish(@RequestBody @Valid PublishPointRequest request) {
    return PublishResultDto.fromEntity(publishPointPort.publish(request));
  }

  @PostMapping(value = "/v1/publish/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
  BulkJobResult bulkPublish(
      @RequestBody List<@Valid BulkPublishPointRequest> requests) {
    BulkJobResult result = new BulkJobResult();
    Map<String, Map<Integer, Long>> jobSummary = new HashMap<>();
    jobSummary.put("hit", new HashMap<>());
    jobSummary.put("amount", new HashMap<>());
    requests.forEach(request -> {
      try {
        Executors.newSingleThreadExecutor().submit(() -> {
          try {
            Point publish = publishPointPort.publish(request);
            putSummary(jobSummary, publish);
            result.addSuccess(request.getJobSeq(), publish.getSeq());
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

  private void putSummary(Map<String, Map<Integer, Long>> jobSummary, Point publish) {
    jobSummary.get("amount").compute(publish.getHistoryType(), (type, amount) -> {
      amount = Objects.requireNonNullElse(amount, 0L);
      amount += publish.getCharge();
      return amount;
    });
    jobSummary.get("hit").compute(publish.getHistoryType(), (type, amount) -> {
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
    messages.add("*대량발급이 완료 되었습니다*");

    jobSummary.get("amount").forEach((type, amount) -> {
      try {
        HistoryType historyType = HistoryType.getByValue(type);
        messages.add(MessageFormat.format(">발급 사유 : {0} ({1})",
            historyType.getDesc(), historyType.getValue()));
      } catch (HistoryTypeNotFoundException e) {
        messages.add(MessageFormat.format(">발급 사유 : {0}", type));
      }
      messages.add(MessageFormat.format(">발급 건수 : {0}", jobSummary.get("hit").get(type)));
      messages.add(MessageFormat.format(">발금 수량 : {0}", amount));
      messages.add("");
    });

    slackBot.postMessage(String.join("\n", messages));
  }

  @PostMapping(value = "/v1/publish/order-cancel")
  ResponseEntity<?> cancelPublish(@RequestBody @Valid CancelPublishOrderPointRequest request) {
    publishPointPort.cancelPublishByOrder(request);
    return ResponseEntity.noContent().build();
  }
}
