package com.kurly.cloud.point.api.point.web;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.common.ControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("PrivateMemberPointControllerTest")
@ControllerTest
public class MemberPointControllerTest implements CommonTestGiven {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp(WebApplicationContext webApplicationContext) {
    this.mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .addFilters(new CharacterEncodingFilter("UTF-8", true))
        .build();
  }

  @Test
  @DisplayName("회원 적립금 이력을 조회 한다")
  void test() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/v1/history/{memberNumber}", givenMemberNumber())
                .param("regDateTimeFrom", "2020-05-10T10:00:00+09:00")
                .param("regDateTimeTo", "2020-05-10T10:00:00+09:00")
        )
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("회원 적립금 요약을 조회 한다")
  void test1() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/v1/summary/{memberNumber}", givenMemberNumber()))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("회원 사용 가능 적립금을 조회 한다")
  void test2() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/v1/available/{memberNumber}", givenMemberNumber()))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("회원이 적립금을 사용 가능한지 조회 한다")
  void test3() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/v1/is-available/{memberNumber}", givenMemberNumber())
                .param("point", "1000")
        )
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk());

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/v1/is-available/{memberNumber}", givenMemberNumber())
                .param("point", "1000")
                .param("settle", "true")
        )
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk());
  }

}
