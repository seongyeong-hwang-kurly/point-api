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
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithUserDetails;
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
@DisplayName("PublicMemberPointDocumentationTest")
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
        .point(1000)
        .memberNumber(givenMemberNumber())
        .historyType(HistoryType.TYPE_12.getValue())
        .actionMemberNumber(givenMemberNumber())
        .expireDate(LocalDateTime.now())
        .detail("지급")
        .memo("메모")
        .build());
  }

  @WithUserDetails
  @Test
  @DisplayName("RestDoc - 적립금 이력 조회")
  void history() throws Exception {
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders
            .get("/public/v1/history/{memberNumber}", givenMemberNumber())
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
                    parameterWithName("memberNumber").description("회원번호")
                )
                , requestParameters(
                    parameterWithName("regDateTimeFrom")
                        .optional()
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                        .description("등록일 검색 일시 시작 조건")
                    , parameterWithName("regDateTimeTo")
                        .optional()
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                        .description("등록일 검색 일시 끝 조건")
                    , parameterWithName("size").optional().description("페이지 사이즈")
                    , parameterWithName("page").optional().description("페이지 번호")
                )
                , responseFields(
                    beneathPath("data").withSubsectionId("data")
                    , fieldWithPath("content").type(JsonFieldType.ARRAY).description("이력")
                    , fieldWithPath("content[].seq").type(JsonFieldType.NUMBER)
                        .description("이력 번호")
                    , fieldWithPath("content[].orderNumber").type(JsonFieldType.NUMBER)
                        .description("연관 주문번호")
                    , fieldWithPath("content[].point").type(JsonFieldType.NUMBER)
                        .description("적립금")
                    , fieldWithPath("content[].detail").type(JsonFieldType.STRING)
                        .description("이력 내용(고객용)")
                    , fieldWithPath("content[].memo").optional().type(JsonFieldType.STRING)
                        .description("이력 내용(내부용) - 관리자 권한 일때만 노출 됩니다")
                    , fieldWithPath("content[].regDateTime").type(JsonFieldType.STRING)
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                        .description("등록 시각")
                    , fieldWithPath("content[].regTimestamp").type(JsonFieldType.NUMBER)
                        .attributes(key("format").value("Timestamp"))
                        .description("등록 시각")
                    , fieldWithPath("content[].expireDateTime").type(JsonFieldType.STRING)
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                        .description("만료 시각")
                    , fieldWithPath("content[].expireTimestamp").type(JsonFieldType.NUMBER)
                        .attributes(key("format").value("Timestamp"))
                        .description("만료 시각")
                    , fieldWithPath("totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수")
                    , fieldWithPath("totalElements").type(JsonFieldType.NUMBER)
                        .description("전체 컨텐츠 수")
                    , fieldWithPath("size").type(JsonFieldType.NUMBER).description("페이지 사이즈")
                    , fieldWithPath("numberOfElements").type(JsonFieldType.NUMBER)
                        .description("현재 페이지 컨텐츠 수")
                    , fieldWithPath("number").type(JsonFieldType.NUMBER).description("현재 페이지 번호")
                    , fieldWithPath("last").type(JsonFieldType.BOOLEAN).description("첫번째 페이지 여부")
                    , fieldWithPath("first").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부")
                )
            )
        );
  }

  @WithUserDetails
  @Test
  @DisplayName("RestDoc - 적립금 요약 조회")
  void summary() throws Exception {
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders
            .get("/public/v1/summary/{memberNumber}", givenMemberNumber())
    );

    resultActions
        .andExpect(status().isOk())
        .andDo(
            document("point/pub/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                , pathParameters(
                    parameterWithName("memberNumber").description("회원번호")
                )
                , responseFields(
                    beneathPath("data").withSubsectionId("data")
                    , fieldWithPath("amount").type(JsonFieldType.NUMBER).description("총 보유 적립금")
                    , fieldWithPath("nextExpireDate").type(JsonFieldType.STRING)
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                        .description("다음 만료 시각")
                    , fieldWithPath("nextExpireTimestamp").type(JsonFieldType.NUMBER)
                        .attributes(key("format").value("Timestamp"))
                        .description("다음 만료 시각")
                    , fieldWithPath("nextExpireAmount").type(JsonFieldType.NUMBER)
                        .description("다음 만료 적립금")
                )
            )
        );
  }
}
