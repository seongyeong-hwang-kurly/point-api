/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class MemberPointControllerTest extends AbstractControllerTest {

  private MockMvc mockMvc;

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

  @WithUserDetails
  @Test
  @DisplayName("회원 포인트 이력을 조회 한다")
  void test() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/public/v1/history/{memberNumber}", givenMemberNumber()))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk());
  }

  @WithUserDetails
  @Test
  @DisplayName("회원 포인트 요약을 조회 한다")
  void test1() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/public/v1/summary/{memberNumber}", givenMemberNumber()))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk());
  }

}