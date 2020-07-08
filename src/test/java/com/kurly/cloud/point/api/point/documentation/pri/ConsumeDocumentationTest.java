package com.kurly.cloud.point.api.point.documentation.pri;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.config.SpringSecurityTestConfig;
import com.kurly.cloud.point.api.point.documentation.ApiDocumentUtils;
import com.kurly.cloud.point.api.point.domain.consume.BulkConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.CancelOrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.ConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.OrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.port.in.ConsumePointPort;
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
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Import(SpringSecurityTestConfig.class)
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@DisplayName("PrivateConsumeDocumentationTest")
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

  @Test
  @DisplayName("RestDoc - 적립금 사용")
  void consume() throws Exception {
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders.post("/v1/consume")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(givenConsumeRequest()))
    );

    resultActions
        .andExpect(status().isNoContent())
        .andDo(
            MockMvcRestDocumentation.document("point/pri/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                , requestFields(
                    fieldWithPath("memberNumber").type(JsonFieldType.NUMBER).description("회원번호")
                    , fieldWithPath("point").type(JsonFieldType.NUMBER).description("사용 금액")
                    , fieldWithPath("historyType").type(JsonFieldType.NUMBER).description("사유 번호")
                    , fieldWithPath("detail").type(JsonFieldType.STRING).description("사용 사유명(고객용)")
                    , fieldWithPath("orderNumber").type(JsonFieldType.NUMBER)
                        .description("주문번호").optional()
                    , fieldWithPath("settle").type(JsonFieldType.BOOLEAN)
                        .description("유상여부 - true이면 유상적립금만 사용합니다").optional()
                    , fieldWithPath("memo").type(JsonFieldType.STRING).description("사용 사유명(내부용)")
                        .optional()
                    , fieldWithPath("actionMemberNumber").type(JsonFieldType.NUMBER)
                        .description("작업지 회원 번호")
                )
            )
        );
  }

  ConsumePointRequest givenConsumeRequest() {
    return ConsumePointRequest.builder()
        .memberNumber(givenMemberNumber())
        .historyType(HistoryType.TYPE_101.getValue())
        .detail("사용사유(고객용)")
        .memo("사용사유(내부용)")
        .point(1000)
        .settle(false)
        .build();
  }

  @Test
  @DisplayName("RestDoc - 적립금 대량 사용")
  void bulkConsume() throws Exception {
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders.post("/v1/consume/bulk")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(givenBulkRequest()))
    );

    resultActions
        .andExpect(status().isOk())
        .andDo(
            document("point/pri/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                , requestFields(
                    fieldWithPath("[].jobSeq").type(JsonFieldType.NUMBER)
                        .description("작업번호 - 각 발급의 고유 작업 번호")
                    , fieldWithPath("[].memberNumber").ignored()
                    , fieldWithPath("[].point").ignored()
                    , fieldWithPath("[].historyType").ignored()
                    , fieldWithPath("[].orderNumber").ignored()
                    , fieldWithPath("[].settle").ignored()
                    , fieldWithPath("[].memo").ignored()
                    , fieldWithPath("[].detail").ignored()
                    , fieldWithPath("[].actionMemberNumber").ignored()
                )
                , responseFields(
                    beneathPath("data").withSubsectionId("data")
                    , fieldWithPath("succeed").type(JsonFieldType.ARRAY).description("성공한 작업 번호")
                    , fieldWithPath("failed").type(JsonFieldType.ARRAY).description("실패한 작업 번호")
                    , fieldWithPath("resultIds").ignored()
                )
            )
        );
  }

  List<BulkConsumePointRequest> givenBulkRequest() {
    ArrayList<BulkConsumePointRequest> requests = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      BulkConsumePointRequest request = new BulkConsumePointRequest();
      request.setJobSeq(i + 1);
      request.setMemberNumber(givenMemberNumber());
      request.setPoint(100);
      request.setHistoryType(HistoryType.TYPE_100.getValue());
      request.setDetail("발급 사유명(고객용)");
      requests.add(request);
    }
    return requests;
  }

  @Test
  @DisplayName("RestDoc - 주문 적립금 사용")
  void orderConsume() throws Exception {
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders.post("/v1/consume/order")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(givenOrderConsumeRequest()))
    );

    resultActions
        .andExpect(status().isNoContent())
        .andDo(
            document("point/pri/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                // PUBLIC 과 동
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

  @Test
  @DisplayName("RestDoc - 주문 적립금 사용 취소")
  void cancelConsume() throws Exception {
    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders.post("/v1/consume/cancel")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(givenCancelConsumeRequest()))
    );

    resultActions
        .andExpect(status().isNoContent())
        .andDo(
            document("point/pri/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                , requestFields(
                    fieldWithPath("memberNumber").type(JsonFieldType.NUMBER).description("회원번호")
                    , fieldWithPath("point").type(JsonFieldType.NUMBER).description("취소 금액")
                    , fieldWithPath("orderNumber").type(JsonFieldType.NUMBER).description("주문번호")
                    , fieldWithPath("actionMemberNumber").type(JsonFieldType.NUMBER)
                        .description("작업자 회원 번호")
                )
            )
        );
  }

  CancelOrderConsumePointRequest givenCancelConsumeRequest() {
    return CancelOrderConsumePointRequest.builder()
        .orderNumber(givenOrderNumber())
        .memberNumber(givenMemberNumber())
        .point(1000)
        .build();
  }
}
