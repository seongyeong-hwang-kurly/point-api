package com.kurly.cloud.point.api.point.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.BulkPublishPointRequest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
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
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class PublishControllerTest {

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

  @Nested
  @DisplayName("포인트 발급을 호출 할 때")
  class DescribePublish {

    PublishPointRequest givenRequest() {
      return PublishPointRequest.builder()
          .memberNumber(givenMemberNumber())
          .point(1000)
          .historyType(HistoryType.TYPE_1.getValue())
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
            .perform(MockMvcRequestBuilders.post("/public/v1/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(givenRequest())))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isUnauthorized());
      }
    }

    @Nested
    @DisplayName("관리자가 호출하면")
    class Context1 extends AbstractControllerTest {

      @WithUserDetails("admin")
      @Test
      @DisplayName("포인트를 발급하고 응답코드는 204를 반환한다")
      void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.post("/public/v1/publish")
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
            .perform(MockMvcRequestBuilders.post("/public/v1/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PublishPointRequest()))
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is(422));
      }
    }
  }

  @Nested
  @DisplayName("포인트 대량 발급을 호출 할 때")
  class DescribeBulkPublish extends AbstractControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    PublishPointPort publishPointPort;

    List<BulkPublishPointRequest> givenRequest() {
      ArrayList<BulkPublishPointRequest> requests = new ArrayList<>();
      for (int i = 0; i < 3; i++) {
        BulkPublishPointRequest request = new BulkPublishPointRequest();
        request.setJobSeq(i + 1);
        request.setMemberNumber(givenMemberNumber());
        request.setPoint(100);
        request.setHistoryType(HistoryType.TYPE_1.getValue());
        requests.add(request);
      }
      return requests;
    }

    @WithUserDetails("admin")
    @Test
    @DisplayName("발급에 실패하면 실패한 요청 아이디를 리턴한다")
    void test() throws Exception {
      List<BulkPublishPointRequest> bulkPublishPointRequests = givenRequest();
      doThrow(new RuntimeException()).when(publishPointPort).publish(any());

      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/public/v1/publish/bulk")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(bulkPublishPointRequests))
          )
          .andDo(MockMvcResultHandlers.print())
          .andExpect(content().json(
              "{\"success\":true,\"message\":null,\"data\":{\"succeed\":[],\"failed\":[1,2,3]}}"
          ))
          .andExpect(status().is(200));
    }

    @WithUserDetails("admin")
    @Test
    @DisplayName("발급에 성공하면 성공한 요청 아이디를 리턴한다")
    void test1() throws Exception {
      List<BulkPublishPointRequest> bulkPublishPointRequests = givenRequest();

      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/public/v1/publish/bulk")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(bulkPublishPointRequests))
          )
          .andDo(MockMvcResultHandlers.print())
          .andExpect(content().json(
              "{\"success\":true,\"message\":null,\"data\":{\"succeed\":[1,2,3],\"failed\":[]}}"
          ))
          .andExpect(status().is(200));
    }
  }
}
