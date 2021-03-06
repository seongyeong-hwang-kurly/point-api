package com.kurly.cloud.point.api.point.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.common.ControllerTest;
import com.kurly.cloud.point.api.point.domain.consume.BulkConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.CancelOrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.ConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.service.ConsumePointUseCase;
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
@DisplayName("PrivateConsumeControllerTest")
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
  @DisplayName("????????? ????????? ?????? ??? ???")
  class DescribeConsume {

    ConsumePointRequest givenRequest() {
      return ConsumePointRequest.builder()
          .memberNumber(givenMemberNumber())
          .historyType(HistoryType.TYPE_101.getValue())
          .detail("????????????(?????????)")
          .memo("????????????(?????????)")
          .point(1000L)
          .settle(false)
          .build();
    }

    @Nested
    @DisplayName("????????? ????????? ????????????")
    @ControllerTest
    class Context1 {

      @MockBean
      ConsumePointUseCase consumePointUseCase;

      @Test
      @DisplayName("???????????? ???????????? ??????????????? 204??? ????????????")
      void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/v1/consume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(givenRequest())))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNoContent());
      }
    }

    @Nested
    @DisplayName("???????????? ?????????")
    @ControllerTest
    class Context2 {

      @Test
      @DisplayName("?????? ????????? 422??? ???????????? ???????????? ????????????")
      void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/v1/consume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ConsumePointRequest()))
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is(422));
      }
    }

    @Nested
    @DisplayName("???????????? ????????????")
    @ControllerTest
    class Context3 {

      @Test
      @DisplayName("400??? ????????????")
      void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/v1/consume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(givenRequest())))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest());
      }
    }
  }

  @Nested
  @DisplayName("????????? ?????? ????????? ?????? ??? ???")
  @ControllerTest
  class DescribeBulkConsume {

    @MockBean
    ConsumePointUseCase consumePointUseCase;

    @Test
    @DisplayName("????????? ???????????? ????????? ?????? ???????????? ????????????")
    void test() throws Exception {
      List<BulkConsumePointRequest> bulkConsumePointRequests = givenRequest();
      doThrow(new RuntimeException()).when(consumePointUseCase).consume(any());

      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/v1/consume/bulk")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(bulkConsumePointRequests))
          )
          .andDo(MockMvcResultHandlers.print())
          .andExpect(content().json(
              "{\"success\":true,\"message\":null,\"data\":{\"succeed\":[],\"failed\":[1,2,3]}}"
          ))
          .andExpect(status().is(200));
    }

    List<BulkConsumePointRequest> givenRequest() {
      ArrayList<BulkConsumePointRequest> requests = new ArrayList<>();
      for (int i = 0; i < 3; i++) {
        BulkConsumePointRequest request = new BulkConsumePointRequest();
        request.setJobSeq(i + 1);
        request.setMemberNumber(givenMemberNumber());
        request.setPoint(100L);
        request.setHistoryType(HistoryType.TYPE_100.getValue());
        request.setDetail("??????");
        requests.add(request);
      }
      return requests;
    }

    @Test
    @DisplayName("????????? ???????????? ????????? ?????? ???????????? ????????????")
    void test1() throws Exception {
      List<BulkConsumePointRequest> bulkConsumePointRequests = givenRequest();

      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/v1/consume/bulk")
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
  @DisplayName("????????? ??????????????? ?????? ??? ???")
  class DescribeCancelConsume {

    @Nested
    @DisplayName("?????? ????????????")
    @ControllerTest
    class Context0 {
      @MockBean
      ConsumePointUseCase consumePointUseCase;

      @Test
      @DisplayName("???????????? ???????????? ?????? ??????????????? 204??? ????????????")
      void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/v1/consume/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(givenRequest())))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNoContent());
      }

      CancelOrderConsumePointRequest givenRequest() {
        return CancelOrderConsumePointRequest.builder()
            .orderNumber(givenOrderNumber())
            .memberNumber(givenMemberNumber())
            .point(100L)
            .build();
      }
    }
  }
}
