package com.kurly.cloud.point.api.point.documentation.pri;

import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.config.SpringSecurityTestConfig;
import com.kurly.cloud.point.api.point.documentation.ApiDocumentUtils;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.exception.AlreadyPublishedException;
import com.kurly.cloud.point.api.point.service.PublishPointUseCase;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@ActiveProfiles("local")
@Import(SpringSecurityTestConfig.class)
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@DisplayName("PrivateOrderPointDocumentationTest")
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "gateway.cloud.dev.kurly.services/point", uriPort = 443)
public class OrderPointDocumentationTest implements CommonTestGiven {

  MockMvc mockMvc;

  @Autowired
  PublishPointUseCase publishPointUseCase;

  @BeforeEach
  void setUp(WebApplicationContext webApplicationContext,
             RestDocumentationContextProvider restDocumentation) {
    this.mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(documentationConfiguration(restDocumentation))
        .alwaysDo(print())
        .build();
  }

  @Test
  @DisplayName("RestDoc - ?????? ????????? ??????")
  void orderPublishedAmount() throws Exception {
    givenOrderPoint();
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders.get("/v1/order-published-amount/{orderNumber}"
            , givenOrderNumber())
    );

    resultActions
        .andExpect(status().isOk())
        .andDo(
            MockMvcRestDocumentation.document("point/pri/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                , pathParameters(
                    parameterWithName("orderNumber").description("?????? ??????")
                )
                , responseFields(
                    beneathPath("data").withSubsectionId("data")
                    , fieldWithPath("seq").type(JsonFieldType.NUMBER).description("????????? ??????")
                    , fieldWithPath("memberNumber").type(JsonFieldType.NUMBER).description("?????? ??????")
                    , fieldWithPath("orderNumber").type(JsonFieldType.NUMBER).description("?????? ??????")
                    , fieldWithPath("charge").type(JsonFieldType.NUMBER).description("?????? ?????????")
                    , fieldWithPath("remain").type(JsonFieldType.NUMBER).description("?????? ?????????")
                    , fieldWithPath("pointRatio").type(JsonFieldType.NUMBER).description("?????????")
                    ,
                    fieldWithPath("historyType").type(JsonFieldType.NUMBER).description("?????? ?????? ??????")
                    , fieldWithPath("refundType").ignored()
                    , fieldWithPath("payment").type(JsonFieldType.BOOLEAN).description("?????? ??????")
                    , fieldWithPath("settle").type(JsonFieldType.BOOLEAN).description("?????? ??????")
                    , fieldWithPath("regTime").type(JsonFieldType.STRING)
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                        .description("?????? ??????")
                    , fieldWithPath("regTimestamp").type(JsonFieldType.NUMBER)
                        .attributes(key("format").value("Timestamp"))
                        .description("?????? ??????")
                    , fieldWithPath("expireTime").type(JsonFieldType.STRING)
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                        .description("?????? ??????")
                    , fieldWithPath("expireTimestamp").type(JsonFieldType.NUMBER)
                        .attributes(key("format").value("Timestamp"))
                        .description("?????? ??????")
                )
            )
        );
  }

  void givenOrderPoint() throws AlreadyPublishedException {
    publishPointUseCase.publishByOrder(PublishPointRequest.builder()
        .memberNumber(givenMemberNumber())
        .orderNumber(givenOrderNumber())
        .point(1000L)
        .build());
  }
}
