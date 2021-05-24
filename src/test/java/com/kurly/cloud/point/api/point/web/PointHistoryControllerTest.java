package com.kurly.cloud.point.api.point.web;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.common.ControllerTest;
import com.kurly.cloud.point.api.point.service.PublishPointUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
@DisplayName("PrivatePointHistoryControllerTest")
public class PointHistoryControllerTest implements CommonTestGiven {
  @Autowired
  ObjectMapper objectMapper;
  private MockMvc mockMvc;

  @Autowired
  PublishPointUseCase publishPointUseCase;

  @BeforeEach
  void setUp(WebApplicationContext webApplicationContext) {
    this.mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .addFilters(new CharacterEncodingFilter("UTF-8", true))
        .build();
  }

  @ControllerTest
  @Nested
  @DisplayName("적립금 지급 이력 조회를 호출 할 때")
  class DescribePublishHistory {

    @Test
    @DisplayName("응답 코드는 200를 반환하고 지급 이력을 반환한다")
    void test() throws Exception {
      mockMvc
          .perform(MockMvcRequestBuilders.get("/v1/point-history/publish")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"actionMemberNumber\":[1],\"" +
                  "historyType\":[1],\"" +
                  "regDateTimeFrom\":\"2020-08-18T15:21:46+09:00\"," +
                  "\"regDateTimeTo\":\"2020-08-18T15:21:48+09:00\",\"page\":0,\"size\":10}")
          )
          .andDo(MockMvcResultHandlers.print())
          .andExpect(status().is(200));
    }
  }
}
