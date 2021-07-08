package com.kurly.cloud.point.api.point.documentation.pri;

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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
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
@DisplayName("ReserveCalculateDocumentationTest")
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "gateway.cloud.dev.kurly.services/point", uriPort = 443)
public class ReserveCalculateDocumentationTest implements CommonTestGiven {

  MockMvc mockMvc;

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

  public static String givenRequest() throws IOException {
    return Files
        .readString(Paths.get(new ClassPathResource("reserve-calculate-request.json").getURI()));
  }

  @Test
  @DisplayName("RestDoc - 발생예정 적립금 계산")
  void calculate() throws Exception {

    ResultActions resultActions = mockMvc.perform(
        RestDocumentationRequestBuilders.post("/v1/reserve/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(givenRequest())
    );

    resultActions
        .andExpect(status().isOk())
        .andDo(
            MockMvcRestDocumentation.document("point/pri/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                , requestFields(
                    fieldWithPath("memberReserveRatio").type(JsonFieldType.NUMBER)
                        .optional().description("회원 혜택 적립률")
                    , fieldWithPath("products").type(JsonFieldType.ARRAY)
                        .description("적립 대상 상품 목록")
                    , fieldWithPath("products.[].price").type(JsonFieldType.NUMBER)
                        .optional().description("상품 개별 가격")
                    , fieldWithPath("products.[].quantity").type(JsonFieldType.NUMBER)
                        .optional().description("상품 수량")
                    , fieldWithPath("products.[].totalPrice").type(JsonFieldType.NUMBER)
                        .optional().description("상품 총 가격")
                    , fieldWithPath("products.[].contentProductNo").type(JsonFieldType.NUMBER)
                        .optional().description("컨텐트 상품 번호")
                    , fieldWithPath("products.[].dealProductNo").type(JsonFieldType.NUMBER)
                        .optional().description("딜 상품 번호")
                    , fieldWithPath("products.[].productReserveType").type(JsonFieldType.NUMBER)
                        .optional().description("상품 적립 타입 (상수값 또는 상수이름) <<ProductReserveType,설명>>")
                    , fieldWithPath("products.[].productReserveValue").type(JsonFieldType.NUMBER)
                        .optional().description("상품 적립 값")
                )
                , responseFields(
                    beneathPath("data").withSubsectionId("data")
                    , fieldWithPath("totalReserve").type(JsonFieldType.NUMBER)
                        .description("총 적립 예정 금액")
                    , fieldWithPath("products").type(JsonFieldType.ARRAY)
                        .description("적립 대상 상품 목록")
                    , fieldWithPath("products.[].price").type(JsonFieldType.NUMBER)
                        .optional().description("상품 개별 가격")
                    , fieldWithPath("products.[].quantity").type(JsonFieldType.NUMBER)
                        .optional().description("상품 수량")
                    , fieldWithPath("products.[].totalPrice").type(JsonFieldType.NUMBER)
                        .optional().description("상품 총 가격")
                    , fieldWithPath("products.[].contentProductNo").type(JsonFieldType.NUMBER)
                        .optional().description("컨텐트 상품 번호")
                    , fieldWithPath("products.[].dealProductNo").type(JsonFieldType.NUMBER)
                        .optional().description("딜 상품 번호")
                    , fieldWithPath("products.[].reserveType").type(JsonFieldType.STRING)
                        .optional().description("적용 된 적립 타입 <<ProductReserveType,설명>>")
                    , fieldWithPath("products.[].reserveValue").type(JsonFieldType.NUMBER)
                        .optional().description("적용 된 적립 값")
                    , fieldWithPath("products.[].reserve").type(JsonFieldType.NUMBER)
                        .optional().description("상품 예상 적립 적립금 (개별)")
                    , fieldWithPath("products.[].totalReserve").type(JsonFieldType.NUMBER)
                        .optional().description("상품 총 예상 적립 적립금")
                )
            )
        );
  }
}
