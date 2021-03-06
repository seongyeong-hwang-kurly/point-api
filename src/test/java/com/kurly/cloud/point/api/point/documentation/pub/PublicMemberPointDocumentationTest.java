package com.kurly.cloud.point.api.point.documentation.pub;

import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.config.SpringSecurityTestConfig;
import com.kurly.cloud.point.api.point.documentation.ApiDocumentUtils;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
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
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("local")
@Import(SpringSecurityTestConfig.class)
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@Transactional
@SpringBootTest
@DisplayName("PublicMemberPointDocumentationTest")
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "gateway.cloud.dev.kurly.services/point", uriPort = 443)
public class PublicMemberPointDocumentationTest implements CommonTestGiven {

  MockMvc mockMvc;

  @Autowired
  PublishPointUseCase publishPointUseCase;

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
    publishPointUseCase.publish(PublishPointRequest.builder()
        .point(1000L)
        .memberNumber(givenStaticMemberNumber())
        .historyType(HistoryType.TYPE_12.getValue())
        .actionMemberNumber(givenStaticMemberNumber())
        .expireDate(LocalDateTime.now())
        .detail("??????")
        .memo("??????")
        .build());
  }

  @WithUserDetails
  @Test
  @DisplayName("RestDoc - ????????? ?????? ??????")
  void history() throws Exception {
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders
            .get("/public/v1/history/{memberNumber}", givenStaticMemberNumber())
            .param("regDateTimeFrom", "2020-01-01T00:00:00+09:00")
            .param("regDateTimeTo", "2030-01-01T00:00:00+09:00")
            .param("size", "10")
            .param("page", "0")
    );

    resultActions
        .andExpect(status().isOk())
        .andDo(
            MockMvcRestDocumentation.document("point/pub/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                , pathParameters(
                    parameterWithName("memberNumber").description("????????????")
                )
                , requestParameters(
                    parameterWithName("regDateTimeFrom")
                        .optional()
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                        .description("????????? ?????? ?????? ?????? ??????")
                    , parameterWithName("regDateTimeTo")
                        .optional()
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                        .description("????????? ?????? ?????? ??? ??????")
                    , parameterWithName("size").optional().description("????????? ?????????")
                    , parameterWithName("page").optional().description("????????? ??????")
                )
                , responseFields(
                    beneathPath("data").withSubsectionId("data")
                    , fieldWithPath("content").type(JsonFieldType.ARRAY).description("??????")
                    , fieldWithPath("content[].seq").type(JsonFieldType.NUMBER)
                        .description("?????? ??????")
                    , fieldWithPath("content[].orderNumber").type(JsonFieldType.NUMBER)
                        .description("?????? ????????????")
                    , fieldWithPath("content[].point").type(JsonFieldType.NUMBER)
                        .description("?????????")
                    , fieldWithPath("content[].detail").type(JsonFieldType.STRING)
                        .description("?????? ??????(?????????)")
                    , fieldWithPath("content[].memo").optional().type(JsonFieldType.STRING)
                        .description("?????? ??????(?????????) - ????????? ?????? ????????? ?????? ?????????")
                    , fieldWithPath("content[].regDateTime").type(JsonFieldType.STRING)
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                        .description("?????? ??????")
                    , fieldWithPath("content[].regTimestamp").type(JsonFieldType.NUMBER)
                        .attributes(key("format").value("Timestamp"))
                        .description("?????? ??????")
                    , fieldWithPath("content[].expireDateTime").type(JsonFieldType.STRING)
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                        .description("?????? ??????")
                    , fieldWithPath("content[].expireTimestamp").type(JsonFieldType.NUMBER)
                        .attributes(key("format").value("Timestamp"))
                        .description("?????? ??????")
                    , fieldWithPath("totalPages").type(JsonFieldType.NUMBER).description("?????? ????????? ???")
                    , fieldWithPath("totalElements").type(JsonFieldType.NUMBER)
                        .description("?????? ????????? ???")
                    , fieldWithPath("size").type(JsonFieldType.NUMBER).description("????????? ?????????")
                    , fieldWithPath("numberOfElements").type(JsonFieldType.NUMBER)
                        .description("?????? ????????? ????????? ???")
                    , fieldWithPath("number").type(JsonFieldType.NUMBER).description("?????? ????????? ??????")
                    , fieldWithPath("last").type(JsonFieldType.BOOLEAN).description("????????? ????????? ??????")
                    , fieldWithPath("first").type(JsonFieldType.BOOLEAN).description("????????? ????????? ??????")
                )
            )
        );
  }

  @WithUserDetails
  @Test
  @DisplayName("RestDoc - ????????? ?????? ??????")
  void summary() throws Exception {
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders
            .get("/public/v1/summary/{memberNumber}", givenStaticMemberNumber())
    );

    resultActions
        .andExpect(status().isOk())
        .andDo(
            document("point/pub/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                , pathParameters(
                    parameterWithName("memberNumber").description("????????????")
                )
                , responseFields(
                    beneathPath("data").withSubsectionId("data")
                    , fieldWithPath("amount").type(JsonFieldType.NUMBER).description("??? ?????? ?????????")
                    , fieldWithPath("nextExpireDate").type(JsonFieldType.STRING)
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                        .description("?????? ?????? ??????")
                    , fieldWithPath("nextExpireTimestamp").type(JsonFieldType.NUMBER)
                        .attributes(key("format").value("Timestamp"))
                        .description("?????? ?????? ??????")
                    , fieldWithPath("nextExpireAmount").type(JsonFieldType.NUMBER)
                        .description("?????? ?????? ?????????")
                )
            )
        );
  }

  @WithUserDetails
  @Test
  @DisplayName("RestDoc - ???????????? ????????? ??????")
  void available() throws Exception {
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders
            .get("/public/v1/available/{memberNumber}", givenStaticMemberNumber())
    );

    resultActions
        .andExpect(status().isOk())
        .andDo(
            document("point/pub/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                , pathParameters(
                    parameterWithName("memberNumber").description("????????????")
                )
                , responseFields(
                    beneathPath("data").withSubsectionId("data")
                    , fieldWithPath("total").type(JsonFieldType.NUMBER).description("??? ?????? ?????????")
                    , fieldWithPath("free").type(JsonFieldType.NUMBER).description("??? ?????? ?????? ?????????")
                    , fieldWithPath("cash").type(JsonFieldType.NUMBER).description("??? ?????? ?????? ?????????")
                )
            )
        );
  }
}
