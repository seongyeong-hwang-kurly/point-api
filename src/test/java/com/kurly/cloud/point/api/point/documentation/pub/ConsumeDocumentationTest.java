/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.documentation.pub;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.config.SpringSecurityTestConfig;
import com.kurly.cloud.point.api.point.documentation.ApiDocumentUtils;
import com.kurly.cloud.point.api.point.domain.consume.OrderConsumePointRequest;
import com.kurly.cloud.point.api.point.port.in.ConsumePointPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Import(SpringSecurityTestConfig.class)
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@DisplayName("PublicConsumeDocumentationTest")
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "gateway.cloud.dev.kurly.services/point", uriPort = 443)
public class ConsumeDocumentationTest implements CommonTestGiven {
  MockMvc mockMvc;

  @MockBean
  ConsumePointPort consumePointPort;

  @Autowired
  ObjectMapper objectMapper;

  @BeforeEach
  void setUp(WebApplicationContext webApplicationContext,
             RestDocumentationContextProvider restDocumentation) {
    this.mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(documentationConfiguration(restDocumentation))
        .alwaysDo(print())
        .build();
  }

  @WithUserDetails
  @Test
  @DisplayName("RestDoc - 주문 적립금 사용")
  void orderConsume() throws Exception {
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders.post("/public/v1/consume/order")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(givenOrderConsumeRequest()))
    );

    resultActions
        .andExpect(status().isNoContent())
        .andDo(
            document("point/pub/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                , requestFields(
                    fieldWithPath("memberNumber").type(JsonFieldType.NUMBER).description("회원번호")
                    , fieldWithPath("point").type(JsonFieldType.NUMBER).description("사용 금액")
                    , fieldWithPath("orderNumber").type(JsonFieldType.NUMBER).description("주문번호")
                    , fieldWithPath("settle").type(JsonFieldType.BOOLEAN)
                        .description("유상여부 - true이면 유상적립금만 사용합니다").optional()
                )
            )
        );
  }

  OrderConsumePointRequest givenOrderConsumeRequest() {
    return OrderConsumePointRequest.builder()
        .point(100)
        .memberNumber(givenMemberNumber())
        .orderNumber(givenOrderNumber())
        .build();
  }
}
