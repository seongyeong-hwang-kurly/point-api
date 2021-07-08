package com.kurly.cloud.point.api.point.web;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@SpringBootTest
class ReserveCalculateControllerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp(WebApplicationContext webApplicationContext) {
    this.mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .addFilters(new CharacterEncodingFilter("UTF-8", true))
        .build();
  }

  public static String givenRequest() throws IOException {
    return Files
        .readString(Paths.get(new ClassPathResource("reserve-calculate-request.json").getURI()));
  }

  @Test
  @DisplayName("적립 예정 적립금 계산 컨트롤러 테스트")
  void calculate() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/v1/reserve/calculate")
        .contentType(MediaType.APPLICATION_JSON)
        .content(givenRequest())
    )
        .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk());
  }
}