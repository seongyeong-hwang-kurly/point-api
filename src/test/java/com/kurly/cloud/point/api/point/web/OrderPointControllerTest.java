package com.kurly.cloud.point.api.point.web;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.common.ControllerTest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.exception.AlreadyPublishedException;
import com.kurly.cloud.point.api.point.service.PublishPointUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ControllerTest
@DisplayName("PrivateOrderPointControllerTest")
public class OrderPointControllerTest implements CommonTestGiven {
  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  PublishPointUseCase publishPointUseCase;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp(WebApplicationContext webApplicationContext) {
    this.mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .addFilters(new CharacterEncodingFilter("UTF-8", true))
        .build();
  }

  @WithUserDetails
  @Test
  @DisplayName("적립되지 않은 주문 적립금 조회")
  void test() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/v1/order-published-amount/{orderNumber}"
            , givenOrderNumber()))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isNotFound());
  }

  @WithUserDetails
  @Test
  @DisplayName("적립된 주문 적립금 조회")
  void test1() throws Exception {
    givenOrderPoint();
    mockMvc
        .perform(MockMvcRequestBuilders.get("/v1/order-published-amount/{orderNumber}"
            , givenOrderNumber()))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk());
  }

  void givenOrderPoint() throws AlreadyPublishedException {
    publishPointUseCase.publishByOrder(PublishPointRequest.builder()
        .memberNumber(givenMemberNumber())
        .orderNumber(givenOrderNumber())
        .point(1000L)
        .build());
  }
}
