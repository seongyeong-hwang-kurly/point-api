package com.kurly.cloud.point.api.point.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurly.cloud.point.api.point.domain.consume.BulkConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.CancelOrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.ConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.OrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.port.in.ConsumePointPort;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ConsumeControllerTest {

  private MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @BeforeEach
  void setUp(WebApplicationContext webApplicationContext) {
    this.mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .addFilters(new CharacterEncodingFilter("UTF-8", true))
        .build();
  }

  long givenMemberNumber() {
    return 999999999;
  }

  long givenOrderNumber() {
    return 888888888;
  }

  @Nested
  @DisplayName("적립금 사용을 호출 할 때")
  class DescribeConsume {

    ConsumePointRequest givenRequest() {
      return ConsumePointRequest.builder()
          .memberNumber(givenMemberNumber())
          .historyType(HistoryType.TYPE_101.getValue())
          .detail("사용사유(고객용)")
          .memo("사용사유(내부용)")
          .point(1000)
          .settle(false)
          .build();
    }

    @Nested
    @DisplayName("일반 회원이 호출하면")
    class Context0 extends AbstractControllerTest {

      @WithUserDetails
      @Test
      @DisplayName("응답코드 401을 반환한다")
      void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/public/v1/consume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(givenRequest())))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isUnauthorized());
      }
    }

    @Nested
    @DisplayName("관리자가 호출하면")
    class Context1 extends AbstractControllerTest {

      @MockBean
      ConsumePointPort consumePointPort;

      @WithUserDetails("admin")
      @Test
      @DisplayName("포인트를 사용하고 응답코드는 204를 반환한다")
      void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/public/v1/consume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(givenRequest())))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNoContent());
      }
    }

    @Nested
    @DisplayName("필수값이 없으면")
    class Context2 extends AbstractControllerTest {

      @WithUserDetails("admin")
      @Test
      @DisplayName("응답 코드는 422를 반환하고 필드명을 반환한다")
      void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/public/v1/consume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ConsumePointRequest()))
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is(422));
      }
    }

    @Nested
    @DisplayName("포인트가 모자르면")
    class Context3 extends AbstractControllerTest {

      @WithUserDetails("admin")
      @Test
      @DisplayName("400을 리턴한다")
      void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/public/v1/consume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(givenRequest())))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest());
      }
    }
  }

  @Nested
  @DisplayName("적립금 대량 사용을 호출 할 때")
  class DescribeBulkConsume extends AbstractControllerTest {

    @MockBean
    ConsumePointPort consumePointPort;

    List<BulkConsumePointRequest> givenRequest() {
      ArrayList<BulkConsumePointRequest> requests = new ArrayList<>();
      for (int i = 0; i < 3; i++) {
        BulkConsumePointRequest request = new BulkConsumePointRequest();
        request.setJobSeq(i + 1);
        request.setMemberNumber(givenMemberNumber());
        request.setPoint(100);
        request.setHistoryType(HistoryType.TYPE_100.getValue());
        request.setDetail("사유");
        requests.add(request);
      }
      return requests;
    }

    @WithUserDetails("admin")
    @Test
    @DisplayName("사용에 실패하면 실패한 요청 아이디를 리턴한다")
    void test() throws Exception {
      List<BulkConsumePointRequest> bulkConsumePointRequests = givenRequest();
      doThrow(new RuntimeException()).when(consumePointPort).consume(any());

      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/public/v1/consume/bulk")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(bulkConsumePointRequests))
          )
          .andDo(MockMvcResultHandlers.print())
          .andExpect(content().json(
              "{\"success\":true,\"message\":null,\"data\":{\"succeed\":[],\"failed\":[1,2,3]}}"
          ))
          .andExpect(status().is(200));
    }

    @WithUserDetails("admin")
    @Test
    @DisplayName("사용에 성공하면 성공한 요청 아이디를 리턴한다")
    void test1() throws Exception {
      List<BulkConsumePointRequest> bulkConsumePointRequests = givenRequest();

      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/public/v1/consume/bulk")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(bulkConsumePointRequests))
          )
          .andDo(MockMvcResultHandlers.print())
          .andExpect(content().json(
              "{\"success\":true,\"message\":null,\"data\":{\"succeed\":[1,2,3],\"failed\":[]}}"
          ))
          .andExpect(status().is(200));
    }
  }

  @Nested
  @DisplayName("주문 적립금 사용을 호출 할 때")
  class DescribeOrderConsume {

    @Nested
    @DisplayName("값이 올바르면")
    class Context0 extends AbstractControllerTest {
      @MockBean
      ConsumePointPort consumePointPort;

      OrderConsumePointRequest givenRequest() {
        return OrderConsumePointRequest.builder()
            .memberNumber(givenMemberNumber())
            .orderNumber(givenOrderNumber())
            .point(100)
            .build();
      }

      @WithUserDetails
      @Test
      @DisplayName("포인트를 사용하고 응답코드는 204를 반환한다")
      void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/public/v1/consume/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(givenRequest())))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNoContent());
      }
    }

    @Nested
    @DisplayName("다른사용자의 적립금 사용을 호출하면")
    class Context1 extends AbstractControllerTest {
      OrderConsumePointRequest givenRequest() {
        return OrderConsumePointRequest.builder()
            .memberNumber(givenMemberNumber() - 1)
            .orderNumber(givenOrderNumber())
            .point(100)
            .build();
      }

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
    }
  }

  @Nested
  @DisplayName("적립금 사용취소를 호출 할 때")
  class DescribeCancelConsume {
    @Nested
    @DisplayName("값이 올바르면")
    class Context0 extends AbstractControllerTest {
      @MockBean
      ConsumePointPort consumePointPort;

      CancelOrderConsumePointRequest givenRequest() {
        return CancelOrderConsumePointRequest.builder()
            .orderNumber(givenOrderNumber())
            .memberNumber(givenMemberNumber())
            .point(100)
            .build();
      }

      @WithUserDetails("admin")
      @Test
      @DisplayName("포인트를 사용취소 하고 응답코드는 204를 반환한다")
      void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/public/v1/consume/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(givenRequest())))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNoContent());
      }
    }
  }

}
