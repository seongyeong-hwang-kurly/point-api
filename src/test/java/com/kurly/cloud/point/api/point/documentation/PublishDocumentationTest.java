package com.kurly.cloud.point.api.point.documentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurly.cloud.point.api.point.config.SpringSecurityTestConfig;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.BulkPublishPointRequest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.port.in.PublishPointPort;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SpringSecurityTestConfig.class)
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "gateway.cloud.dev.kurly.services/point", uriPort = 443)
public class PublishDocumentationTest {

  MockMvc mockMvc;

  @MockBean
  PublishPointPort publishPointPort;

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

  long givenMemberNumber() {
    return 999999999;
  }

  PublishPointRequest givenPublishRequest() {
    return PublishPointRequest.builder()
        .memberNumber(givenMemberNumber())
        .point(1000)
        .historyType(HistoryType.TYPE_1.getValue())
        .payment(false)
        .settle(false)
        .unlimitedDate(false)
        .expireDate(LocalDateTime.of(2020, 12, 31, 0, 0, 0))
        .memo("발급사유(내부)")
        .detail("발급사유(고객용)")
        .hidden(false)
        .build();
  }

  @WithUserDetails("admin")
  @Test
  @DisplayName("RestDoc - 포인트 발급")
  void publish() throws Exception {
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders.post("/public/v1/publish")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(givenPublishRequest()))
    );

    resultActions
        .andExpect(status().isNoContent())
        .andDo(
            document("point/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                , requestFields(
                    fieldWithPath("memberNumber").type(JsonFieldType.NUMBER).description("회원번호")
                    , fieldWithPath("point").type(JsonFieldType.NUMBER).description("적립 금액")
                    , fieldWithPath("historyType").type(JsonFieldType.NUMBER).description("사유 번호")
                    , fieldWithPath("payment").type(JsonFieldType.BOOLEAN)
                        .description("결제여부 - 실제로 결제를 한 적립금인지를 판단합니다.").optional()
                    , fieldWithPath("settle").type(JsonFieldType.BOOLEAN)
                        .description("유상여부 - 무상을 제외한 모든 포인트는 유상입니다.").optional()
                    , fieldWithPath("expireDate").type(JsonFieldType.STRING)
                        .description("만료일 - 만료일을 따로 지정하고 싶은경우 입력합니다.")
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ss"))
                        .optional()
                    , fieldWithPath("memo").type(JsonFieldType.STRING).description("발급 사유명(내부용)").optional()
                    , fieldWithPath("detail").type(JsonFieldType.STRING).description("발급 사유명(고객용)").optional()
                    , fieldWithPath("hidden").type(JsonFieldType.BOOLEAN).description("이력 숨김 여부").optional()
                    , fieldWithPath("actionMemberNumber").ignored()
                    , fieldWithPath("pointRatio").ignored()
                    , fieldWithPath("unlimitedDate").ignored()
                    , fieldWithPath("orderNumber").ignored()
                )
            )
        );
  }

  List<BulkPublishPointRequest> givenBulkRequest() {
    ArrayList<BulkPublishPointRequest> requests = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      BulkPublishPointRequest request = new BulkPublishPointRequest();
      request.setJobSeq(i + 1);
      request.setMemberNumber(givenMemberNumber());
      request.setPoint(100);
      request.setHistoryType(HistoryType.TYPE_1.getValue());
      requests.add(request);
    }
    return requests;
  }

  @WithUserDetails("admin")
  @Test
  @DisplayName("RestDoc - 포인트 대량 발급")
  void bulkPublish() throws Exception {
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders.post("/public/v1/publish/bulk")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(givenBulkRequest()))
    );

    resultActions
        .andExpect(status().isOk())
        .andDo(
            document("point/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                , requestFields(
                    fieldWithPath("[].jobSeq").type(JsonFieldType.NUMBER)
                        .description("작업번호 - 각 발급의 고유 작업 번호")
                    , fieldWithPath("[].memberNumber").ignored()
                    , fieldWithPath("[].point").ignored()
                    , fieldWithPath("[].historyType").ignored()
                    , fieldWithPath("[].payment").ignored()
                    , fieldWithPath("[].settle").ignored()
                    , fieldWithPath("[].expireDate").ignored()
                    , fieldWithPath("[].memo").ignored()
                    , fieldWithPath("[].detail").ignored()
                    , fieldWithPath("[].actionMemberNumber").ignored()
                    , fieldWithPath("[].hidden").ignored()
                    , fieldWithPath("[].pointRatio").ignored()
                    , fieldWithPath("[].unlimitedDate").ignored()
                    , fieldWithPath("[].orderNumber").ignored()
                )
                , responseFields(
                    beneathPath("data").withSubsectionId("data")
                    , fieldWithPath("succeed").type(JsonFieldType.ARRAY).description("성공한 작업 번호")
                    , fieldWithPath("failed").type(JsonFieldType.ARRAY).description("실패한 작업 번호")
                )
            )
        );
  }
}
