package com.kurly.cloud.point.api.point.config;

import com.kurly.cloud.point.api.point.util.SlackBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackBotConfig {
  @Value("${notification.slack.bot.token}")
  private String token;
  @Value("${notification.slack.bot.channel}")
  private String channel;

  @Bean
  SlackBot slackBot() {
    return new SlackBot(token, channel);
  }
}
