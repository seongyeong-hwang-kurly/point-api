package com.kurly.cloud.point.api.point.web;

import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
import com.kurly.cloud.point.api.point.domain.BulkJobResult;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.BulkPublishPointRequest;
import com.kurly.cloud.point.api.point.domain.publish.CancelPublishOrderPointRequest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.exception.HistoryTypeNotFoundException;
import com.kurly.cloud.point.api.point.service.PublishPointUseCase;
import com.kurly.cloud.point.api.point.util.SlackBot;
import com.kurly.cloud.point.api.point.util.VersionUtil;
import com.kurly.cloud.point.api.point.web.dto.PublishResultDto;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController("PrivatePublishController")
public class PublishController {

  private final PublishPointUseCase publishPointUseCase;
  private final PublishPointUseCase publishPointUseCaseV2;
  private final SlackBot slackBot;

  @Value("${notification.slack.bot.publish.channel:}")
  private String publishNotificationChannel;

  @Value("${notification.slack.bot.cc.channel:}")
  private String ccNotificationChannel;


  /**
   * 기본생성자.
   */
  public PublishController(
      @Qualifier("publishPointService") PublishPointUseCase publishPointUseCase,
      @Qualifier("publishPointServiceV2") PublishPointUseCase publishPointUseCaseV2,
      SlackBot slackBot) {
    this.publishPointUseCase = publishPointUseCase;
    this.publishPointUseCaseV2 = publishPointUseCaseV2;
    this.slackBot = slackBot;
  }

  @PostMapping(value = "/{version}/publish", consumes = MediaType.APPLICATION_JSON_VALUE)
  PublishResultDto publish(
      @PathVariable @Pattern(regexp = VersionUtil.VERSION_PATTERN) String version,
      @RequestBody @Valid PublishPointRequest request
  ) {
    PublishPointUseCase delegate = getDelegate(version);
    Point publish = delegate.publish(request);
    reportPublish(publishNotificationChannel, request);
    return PublishResultDto.fromEntity(publish);
  }

  /**
   * 수기 발급건에 대해서만 슬랙 알림을 보낸다.
   */
  private void reportPublish(String channel, PublishPointRequest request) {
    if (request.getActionMemberNumber() == 0
        || request.getOrderNumber() != 0
        || request.getActionMemberNumber() == request.getMemberNumber()) {
      return;
    }

    List<String> messages = new ArrayList<>();
    messages.add("*수기발급이 완료 되었습니다*");
    messages
        .add(MessageFormat.format(">관리자 회원 번호 : {0}", request.getActionMemberNumber()));
    messages
        .add(MessageFormat.format(">수령자 회원 번호 : {0}", request.getMemberNumber()));
    messages
        .add(MessageFormat.format(">발급 사유 : {0}", getHistoryTypeString(request.getHistoryType())));
    messages
        .add(MessageFormat.format(">사유 상세 : {0}", request.getDetail()));
    messages.add(MessageFormat.format(">발급 수량 : {0}", request.getPoint()));
    if (!StringUtils.isEmpty(channel)) {
      slackBot.postMessage(channel, String.join("\n", messages));
      return;
    }
    slackBot.postMessage(String.join("\n", messages));
  }

  @PostMapping(value = "/{version}/publish/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
  BulkJobResult bulkPublish(
      @PathVariable @Pattern(regexp = VersionUtil.VERSION_PATTERN) String version,
      @RequestBody List<@Valid BulkPublishPointRequest> requests) {
    PublishPointUseCase delegate = getDelegate(version);
    BulkJobResult result = new BulkJobResult();
    Map<String, Map<Integer, Long>> jobSummary = new HashMap<>();
    jobSummary.put("hit", new HashMap<>());
    jobSummary.put("amount", new HashMap<>());
    requests.forEach(request -> {
      try {
        Executors.newSingleThreadExecutor().submit(() -> {
          try {
            Point publish = delegate.publish(request);
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
      messages.add(MessageFormat.format(">발급 사유 : {0}", getHistoryTypeString(type)));
      messages.add(MessageFormat.format(">발급 건수 : {0}", jobSummary.get("hit").get(type)));
      messages.add(MessageFormat.format(">발급 수량 : {0}", amount));
      messages.add("");
    });

    slackBot.postMessage(String.join("\n", messages));
    if (!StringUtils.isEmpty(ccNotificationChannel)) {
      slackBot.postMessage(ccNotificationChannel, String.join("\n", messages));
    }

  }

  private String getHistoryTypeString(Integer type) {
    try {
      HistoryType historyType = HistoryType.getByValue(type);
      return MessageFormat
          .format("{0} ({1})", historyType.getDesc(), historyType.getValue());
    } catch (HistoryTypeNotFoundException e) {
      return MessageFormat.format("{0}", type);
    }
  }

  @PostMapping(value = "/{version}/publish/order-cancel")
  ResponseEntity<?> cancelPublish(
      @PathVariable @Pattern(regexp = VersionUtil.VERSION_PATTERN) String version,
      @RequestBody @Valid CancelPublishOrderPointRequest request) {
    PublishPointUseCase delegate = getDelegate(version);
    delegate.cancelPublishByOrder(request);
    return ResponseEntity.noContent().build();
  }

  private PublishPointUseCase getDelegate(String version) {
    if (VersionUtil.V2.equals(version)) {
      return publishPointUseCaseV2;
    }
    return publishPointUseCase;
  }
}
