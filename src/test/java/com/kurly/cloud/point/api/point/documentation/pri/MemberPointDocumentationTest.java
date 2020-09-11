package com.kurly.cloud.point.api.point.documentation.pri;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.config.SpringSecurityTestConfig;
import com.kurly.cloud.point.api.point.documentation.ApiDocumentUtils;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.port.in.PublishPointPort;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@Import(SpringSecurityTestConfig.class)
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@Transactional
@SpringBootTest
@DisplayName("PrivateMemberPointDocumentationTest")
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "gateway.cloud.dev.kurly.services/point", uriPort = 443)
public class MemberPointDocumentationTest implements CommonTestGiven {

  MockMvc mockMvc;

  @Autowired
  PublishPointPort publishPointPort;

  @BeforeEach
  void setUp(WebApplicationContext webApplicationContext,
             RestDocumentationContextProvider restDocumentation) {
    this.mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(documentationConfiguration(restDocumentation))
        .addFilters(new CharacterEncodingFilter("UTF-8", true))
        .alwaysDo(print())
        .build();
  }

  @BeforeEach
  void givenPoint() {
    publishPointPort.publish(PublishPointRequest.builder()
        .point(1000L)
        .memberNumber(givenMemberNumber())
        .historyType(HistoryType.TYPE_12.getValue())
        .actionMemberNumber(givenMemberNumber())
        .expireDate(LocalDateTime.now())
        .detail("지급")
        .memo("메모")
        .build());
  }

  @Test
  @DisplayName("RestDoc - 적립금 이력 조회")
  void history() throws Exception {
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders
            .get("/v1/history/{memberNumber}", givenMemberNumber())
            .param("regDateTimeFrom", "2020-01-01T00:00:00+09:00")
            .param("regDateTimeTo", "2030-01-01T00:00:00+09:00")
            .param("size", "10")
            .param("page", "0")
    );

    resultActions
        .andExpect(status().isOk())
        .andDo(
            MockMvcRestDocumentation.document("point/pri/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                // PUBLIC 과 같음
            )
        );
  }

  @Test
  @DisplayName("RestDoc - 적립금 요약 조회")
  void summary() throws Exception {
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders
            .get("/v1/summary/{memberNumber}", givenMemberNumber())
    );

    resultActions
        .andExpect(status().isOk())
        .andDo(
            document("point/pri/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                // PUBLIC 과 같음
            )
        );
  }
}
