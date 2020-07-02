package com.kurly.cloud.point.api.point.util;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SlackBot {

  private MethodsClient client;
  private String defaultChannel;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  public SlackBot(String token, String channel) {
    this.client = Slack.getInstance().methods(token);
    this.defaultChannel = channel;
  }

  public Future<?> postMessage(String message) {
    return postMessage(defaultChannel, message);
  }

  /**
   * 슬랙 메시지 전송.
   */
  public Future<?> postMessage(String channel, String message) {
    return executorService.submit(() -> {
      try {
        client.chatPostMessage(req -> req.channel(channel).text(message));
      } catch (IOException | SlackApiException e) {
        e.printStackTrace();
      }
    });
  }

}
