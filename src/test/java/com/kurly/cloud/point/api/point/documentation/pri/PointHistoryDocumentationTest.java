package com.kurly.cloud.point.api.point.documentation.pri;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.config.SpringSecurityTestConfig;
import com.kurly.cloud.point.api.point.documentation.ApiDocumentUtils;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.port.in.PublishPointPort;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@Import(SpringSecurityTestConfig.class)
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@Transactional
@DisplayName("PrivatePointHistoryDocumentationTest")
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "gateway.cloud.dev.kurly.services/point", uriPort = 443)
public class PointHistoryDocumentationTest implements CommonTestGiven {
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  PublishPointPort publishPointPort;

  @Autowired
  EntityManager em;

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

  void givenPoint() {
    publishPointPort.publish(PublishPointRequest.builder()
        .historyType(HistoryType.TYPE_1.getValue())
        .point(1000L)
        .memberNumber(givenMemberNumber())
        .orderNumber(givenOrderNumber())
        .actionMemberNumber(givenMemberNumber())
        .build());
    em.flush();
    em.clear();
  }

  Map getParams() {
    HashMap<Object, Object> params = new HashMap<>();
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    params.put("actionMemberNumber", Arrays.asList(givenMemberNumber()));
    params.put("historyType", Arrays.asList(HistoryType.TYPE_1.getValue()));
    params.put("regDateTimeFrom", ZonedDateTime.now().minusSeconds(1).format(dateTimeFormatter));
    params.put("regDateTimeTo", ZonedDateTime.now().plusSeconds(1).format(dateTimeFormatter));
    return params;
  }

  @Test
  @DisplayName("RestDoc - 발급 적립금 내역 조회")
  void publishHistory() throws Exception {
    givenPoint();
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders.get("/v1/point-history/publish")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(getParams()))
    );

    resultActions
        .andExpect(status().isOk())
        .andDo(
            MockMvcRestDocumentation.document("point/pri/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                , requestFields(
                    fieldWithPath("actionMemberNumber").type(JsonFieldType.ARRAY).optional()
                        .description("지급자 회원번호")
                    , fieldWithPath("historyType").type(JsonFieldType.ARRAY).optional()
                        .description("사유 번호")
                    , fieldWithPath("regDateTimeFrom").type(JsonFieldType.STRING)
                        .description("등록일 검색 일시 시작 조건")
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                    , fieldWithPath("regDateTimeTo").type(JsonFieldType.STRING)
                        .description("등록일 검색 일시  조건")
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                )
                , responseFields(
                    beneathPath("data").withSubsectionId("data")
                    , fieldWithPath("content").type(JsonFieldType.ARRAY).description("이력")
                    , fieldWithPath("content[].seq").type(JsonFieldType.NUMBER).description("이력 번호")
                    , fieldWithPath("content[].pointSeq").type(JsonFieldType.NUMBER)
                        .description("적립금 번호")
                    , fieldWithPath("content[].orderNumber").type(JsonFieldType.NUMBER)
                        .description("주문 번호")
                    , fieldWithPath("content[].amount").type(JsonFieldType.NUMBER)
                        .description("지급 포인트")
                    , fieldWithPath("content[].historyType").type(JsonFieldType.NUMBER)
                        .description("사유 번호")
                    , fieldWithPath("content[].historyTypeDesc").type(JsonFieldType.STRING)
                        .description("사유 설명")
                    , fieldWithPath("content[].detail").type(JsonFieldType.STRING)
                        .description("발급 사유명(고객용)")
                    , fieldWithPath("content[].memo").type(JsonFieldType.STRING)
                        .description("사용 사유명(내부용)")
                    , fieldWithPath("content[].settle").type(JsonFieldType.BOOLEAN)
                        .description("유상여부 - 무상을 제외한 모든 적립금은 유상입니다.")
                    , fieldWithPath("content[].memberNumber").type(JsonFieldType.NUMBER)
                        .description("회원 번호")
                    , fieldWithPath("content[].actionMemberNumber").type(JsonFieldType.NUMBER)
                        .description("지급자 회원 번호")
                    , fieldWithPath("content[].regDateTime").type(JsonFieldType.STRING)
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                        .description("등록시각")
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


}
