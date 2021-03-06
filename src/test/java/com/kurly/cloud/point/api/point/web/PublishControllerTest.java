package com.kurly.cloud.point.api.point.web;

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
import com.kurly.cloud.point.api.point.service.PublishPointUseCase;
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
  @DisplayName("????????? ????????? ?????? ??? ???")
  class DescribePublish {

    PublishPointRequest givenRequest() {
      return PublishPointRequest.builder()
          .memberNumber(givenMemberNumber())
          .point(1000L)
          .historyType(HistoryType.TYPE_1.getValue())
          .build();
    }

    @Nested
    @DisplayName("????????? ????????? ????????????")
    @ControllerTest
    class Context {

      @Test
      @DisplayName("???????????? ???????????? ??????????????? 200??? ????????????")
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
    @DisplayName("???????????? ?????????")
    @ControllerTest
    class Context1 {

      @Test
      @DisplayName("?????? ????????? 422??? ???????????? ???????????? ????????????")
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

    @Nested
    @DisplayName("?????????????????? ????????? ?????????")
    @ControllerTest
    class Context2 {

      @Test
      @DisplayName("??????????????? 400??? ????????????")
      void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/v1/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"memberNumber\":999999999,\"point\":\"99,999\",\"historyType\":1}")
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest());
      }
    }
  }

  @Nested
  @DisplayName("????????? ?????? ????????? ?????? ??? ???")
  @ControllerTest
  class DescribeBulkPublish {

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    PublishPointUseCase publishPointUseCase;

    @Test
    @DisplayName("????????? ???????????? ????????? ?????? ???????????? ????????????")
    void test() throws Exception {
      List<BulkPublishPointRequest> bulkPublishPointRequests = givenRequest();
      doThrow(new RuntimeException()).when(publishPointUseCase).publish(any());

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
    @DisplayName("????????? ???????????? ????????? ?????? ???????????? ????????????")
    void test1() throws Exception {
      List<BulkPublishPointRequest> bulkPublishPointRequests = givenRequest();
      given(publishPointUseCase.publish(any())).willReturn(
          Point.builder().seq(1).charge(1L).historyType(1).build()
          , Point.builder().seq(2).charge(2L).historyType(1).build()
          , Point.builder().seq(3).charge(3L).historyType(1).build()
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
  @DisplayName("????????? ?????? ?????? ????????? ?????? ??? ???")
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
    @DisplayName("?????? ????????????")
    @ControllerTest
    class Context0 {

      @MockBean
      PublishPointUseCase publishPointUseCase;

      @Test
      @DisplayName("????????? ????????? ???????????? 204??? ????????????")
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
