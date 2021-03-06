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
import com.kurly.cloud.point.api.point.service.ConsumePointUseCase;
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
  ConsumePointUseCase consumePointUseCase;

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
  @DisplayName("RestDoc - ????????? ??????")
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
                    fieldWithPath("memberNumber").type(JsonFieldType.NUMBER).description("????????????")
                    , fieldWithPath("point").type(JsonFieldType.NUMBER).description("?????? ??????")
                    , fieldWithPath("historyType").type(JsonFieldType.NUMBER).description("?????? ??????")
                    , fieldWithPath("detail").type(JsonFieldType.STRING).description("?????? ?????????(?????????)")
                    , fieldWithPath("orderNumber").type(JsonFieldType.NUMBER)
                        .description("????????????").optional()
                    , fieldWithPath("settle").type(JsonFieldType.BOOLEAN)
                        .description("???????????? - true?????? ?????????????????? ???????????????").optional()
                    , fieldWithPath("memo").type(JsonFieldType.STRING).description("?????? ?????????(?????????)")
                        .optional()
                    , fieldWithPath("actionMemberNumber").type(JsonFieldType.NUMBER)
                        .description("????????? ?????? ??????")
                )
            )
        );
  }

  ConsumePointRequest givenConsumeRequest() {
    return ConsumePointRequest.builder()
        .memberNumber(givenMemberNumber())
        .historyType(HistoryType.TYPE_101.getValue())
        .detail("????????????(?????????)")
        .memo("????????????(?????????)")
        .point(1000L)
        .settle(false)
        .build();
  }

  @Test
  @DisplayName("RestDoc - ????????? ?????? ??????")
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
                        .description("???????????? - ??? ????????? ?????? ?????? ??????")
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
                    , fieldWithPath("succeed").type(JsonFieldType.ARRAY).description("????????? ?????? ??????")
                    , fieldWithPath("failed").type(JsonFieldType.ARRAY).description("????????? ?????? ??????")
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
      request.setPoint(100L);
      request.setHistoryType(HistoryType.TYPE_100.getValue());
      request.setDetail("?????? ?????????(?????????)");
      requests.add(request);
    }
    return requests;
  }

  @Test
  @DisplayName("RestDoc - ?????? ????????? ??????")
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
                // PUBLIC ??? ???
            )
        );
  }

  OrderConsumePointRequest givenOrderConsumeRequest() {
    return OrderConsumePointRequest.builder()
        .point(100L)
        .memberNumber(givenMemberNumber())
        .orderNumber(givenOrderNumber())
        .build();
  }

  @Test
  @DisplayName("RestDoc - ?????? ????????? ?????? ??????")
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
                    fieldWithPath("memberNumber").type(JsonFieldType.NUMBER).description("????????????")
                    , fieldWithPath("point").type(JsonFieldType.NUMBER).description("?????? ??????")
                    , fieldWithPath("orderNumber").type(JsonFieldType.NUMBER).description("????????????")
                    , fieldWithPath("actionMemberNumber").type(JsonFieldType.NUMBER)
                        .description("????????? ?????? ??????")
                )
            )
        );
  }

  CancelOrderConsumePointRequest givenCancelConsumeRequest() {
    return CancelOrderConsumePointRequest.builder()
        .orderNumber(givenOrderNumber())
        .memberNumber(givenMemberNumber())
        .point(1000L)
        .build();
  }
}
