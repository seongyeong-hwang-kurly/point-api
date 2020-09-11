package com.kurly.cloud.point.api.point.controller.pri;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.common.ControllerTest;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.BulkPublishPointRequest;
import com.kurly.cloud.point.api.point.domain.publish.CancelPublishOrderPointRequest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.port.in.PublishPointPort;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("PrivatePublishControllerTest")
public class PublishControllerTest implements CommonTestGiven {

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
  @DisplayName("적립금 발급을 호출 할 때")
  class DescribePublish {

    PublishPointRequest givenRequest() {
      return PublishPointRequest.builder()
          .memberNumber(givenMemberNumber())
          .point(1000L)
          .historyType(HistoryType.TYPE_1.getValue())
          .build();
    }

    @Nested
    @DisplayName("적립금 발급을 호출하면")
    @ControllerTest
    class Context {

      @Test
      @DisplayName("적립금을 발급하고 응답코드는 200를 반환한다")
      void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/v1/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(givenRequest())))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk());
      }
    }

    @Nested
    @DisplayName("필수값이 없으면")
    @ControllerTest
    class Context1 {

      @Test
      @DisplayName("응답 코드는 422를 반환하고 필드명을 반환한다")
      void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/v1/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PublishPointRequest()))
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is(422));
      }
    }
  }

  @Nested
  @DisplayName("적립금 대량 발급을 호출 할 때")
  @ControllerTest
  class DescribeBulkPublish {

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    PublishPointPort publishPointPort;

    @Test
    @DisplayName("발급에 실패하면 실패한 요청 아이디를 리턴한다")
    void test() throws Exception {
      List<BulkPublishPointRequest> bulkPublishPointRequests = givenRequest();
      doThrow(new RuntimeException()).when(publishPointPort).publish(any());

      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/v1/publish/bulk")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(bulkPublishPointRequests))
          )
          .andDo(MockMvcResultHandlers.print())
          .andExpect(content().json(
              "{\"success\":true,\"message\":null,\"data\":{\"succeed\":[],\"failed\":[1,2,3]}}"
          ))
          .andExpect(status().is(200));
    }

    List<BulkPublishPointRequest> givenRequest() {
      ArrayList<BulkPublishPointRequest> requests = new ArrayList<>();
      for (int i = 0; i < 3; i++) {
        BulkPublishPointRequest request = new BulkPublishPointRequest();
        request.setJobSeq(i + 1);
        request.setMemberNumber(givenMemberNumber());
        request.setPoint(100L);
        request.setHistoryType(HistoryType.TYPE_1.getValue());
        requests.add(request);
      }
      return requests;
    }

    @Test
    @DisplayName("발급에 성공하면 성공한 요청 아이디를 리턴한다")
    void test1() throws Exception {
      List<BulkPublishPointRequest> bulkPublishPointRequests = givenRequest();
      given(publishPointPort.publish(any())).willReturn(
          Point.builder().seq(1).build()
          , Point.builder().seq(2).build()
          , Point.builder().seq(3).build()
      );
      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/v1/publish/bulk")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(bulkPublishPointRequests))
          )
          .andDo(MockMvcResultHandlers.print())
          .andExpect(content().json(
              "{\"success\":true,\"message\":null,\"data\":{\"succeed\":[1,2,3],\"failed\":[],\"resultIds\":[{\"jobId\":1,\"pointSeq\":1},{\"jobId\":2,\"pointSeq\":2},{\"jobId\":3,\"pointSeq\":3}]}}"
          ))
          .andExpect(status().is(200));
    }
  }

  @Nested
  @DisplayName("적립금 주문 발급 취소를 호출 할 때")
  class DescribeCancelOrderPublish {

    CancelPublishOrderPointRequest givenRequest() {
      return CancelPublishOrderPointRequest.builder()
          .memberNumber(givenMemberNumber())
          .orderNumber(givenOrderNumber())
          .point(1000L)
          .actionMemberNumber(0L)
          .build();
    }

    @Nested
    @DisplayName("값이 올바르면")
    @ControllerTest
    class Context0 {

      @MockBean
      PublishPointPort publishPointPort;

      @Test
      @DisplayName("적립금 발급을 취소하고 204를 반환한다")
      void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/v1/publish/order-cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(givenRequest())))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNoContent());
      }
    }
  }
}
