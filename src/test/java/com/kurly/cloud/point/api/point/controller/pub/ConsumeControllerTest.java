package com.kurly.cloud.point.api.point.controller.pub;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.common.ControllerTest;
import com.kurly.cloud.point.api.point.domain.consume.OrderConsumePointRequest;
import com.kurly.cloud.point.api.point.port.in.ConsumePointPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
@DisplayName("PublicConsumeControllerTest")
public class ConsumeControllerTest implements CommonTestGiven {

  @Autowired
  ObjectMapper objectMapper;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp(WebApplicationContext webApplicationContext) {
    this.mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .addFilters(new CharacterEncodingFilter("UTF-8", true))
        .build();
  }

  @Nested
  @DisplayName("주문 적립금 사용을 호출 할 때")
  class DescribeOrderConsume {

    @Nested
    @DisplayName("값이 올바르면")
    @ControllerTest
    class Context0 {
      @MockBean
      ConsumePointPort consumePointPort;

      @WithUserDetails
      @Test
      @DisplayName("적립금을 사용하고 응답코드는 204를 반환한다")
      void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/public/v1/consume/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(givenRequest())))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNoContent());
      }

      OrderConsumePointRequest givenRequest() {
        return OrderConsumePointRequest.builder()
            .memberNumber(givenMemberNumber())
            .orderNumber(givenOrderNumber())
            .point(100L)
            .build();
      }
    }

    @Nested
    @DisplayName("다른사용자의 적립금 사용을 호출하면")
    @ControllerTest
    class Context1 {
      @WithUserDetails
      @Test
      @DisplayName("응답코드 401을 반환한다")
      void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/public/v1/consume/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(givenRequest())))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isUnauthorized());
      }

      OrderConsumePointRequest givenRequest() {
        return OrderConsumePointRequest.builder()
            .memberNumber(givenMemberNumber() - 1)
            .orderNumber(givenOrderNumber())
            .point(100L)
            .build();
      }
    }
  }
}
