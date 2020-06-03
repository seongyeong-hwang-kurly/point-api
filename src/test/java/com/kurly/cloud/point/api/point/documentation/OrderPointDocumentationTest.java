/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.documentation;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.config.SpringSecurityTestConfig;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.exception.AlreadyPublishedException;
import com.kurly.cloud.point.api.point.port.in.PublishPointPort;
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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@Import(SpringSecurityTestConfig.class)
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@Transactional
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "gateway.cloud.dev.kurly.services/point", uriPort = 443)
public class OrderPointDocumentationTest implements CommonTestGiven {

  MockMvc mockMvc;

  @Autowired
  PublishPointPort publishPointPort;

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
  @DisplayName("RestDoc - 주문 적립금 조회")
  void orderPublishedAmount() throws Exception {
    givenOrderPoint();
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders.get("/public/v1/order-published-amount/{orderNumber}"
            , givenOrderNumber())
    );

    resultActions
        .andExpect(status().isOk())
        .andDo(
            document("point/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                , pathParameters(
                    parameterWithName("orderNumber").description("주문 번호")
                )
                , responseFields(
                    beneathPath("data").withSubsectionId("data")
                    , fieldWithPath("seq").type(JsonFieldType.NUMBER).description("적립금 번호")
                    , fieldWithPath("memberNumber").type(JsonFieldType.NUMBER).description("회원 번호")
                    , fieldWithPath("orderNumber").type(JsonFieldType.NUMBER).description("주문 번호")
                    , fieldWithPath("charge").type(JsonFieldType.NUMBER).description("지급 적립금")
                    , fieldWithPath("remain").type(JsonFieldType.NUMBER).description("남은 적립금")
                    , fieldWithPath("pointRatio").type(JsonFieldType.NUMBER).description("적립률")
                    ,
                    fieldWithPath("historyType").type(JsonFieldType.NUMBER).description("적립 사유 번호")
                    , fieldWithPath("refundType").ignored()
                    , fieldWithPath("payment").type(JsonFieldType.BOOLEAN).description("결제 여부")
                    , fieldWithPath("settle").type(JsonFieldType.BOOLEAN).description("유상 여부")
                    , fieldWithPath("regTime").type(JsonFieldType.STRING)
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ss"))
                        .description("등록 시각")
                    , fieldWithPath("expireTime").type(JsonFieldType.STRING)
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ss"))
                        .description("만료 시각")
                )
            )
        );
  }

  void givenOrderPoint() throws AlreadyPublishedException {
    publishPointPort.publishByOrder(PublishPointRequest.builder()
        .memberNumber(givenMemberNumber())
        .orderNumber(givenOrderNumber())
        .point(1000)
        .build());
  }
}
