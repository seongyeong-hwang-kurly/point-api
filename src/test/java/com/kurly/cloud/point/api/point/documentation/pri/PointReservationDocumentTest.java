package com.kurly.cloud.point.api.point.documentation.pri;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.config.SpringSecurityTestConfig;
import com.kurly.cloud.point.api.point.documentation.ApiDocumentUtils;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.ReservationResultParam;
import com.kurly.cloud.point.api.point.domain.publish.ReservePointRequestDTO;
import com.kurly.cloud.point.api.point.service.impl.PointReservationDomainService;
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

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SpringSecurityTestConfig.class)
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@DisplayName("PointReservationDocument")
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "gateway.cloud.dev.kurly.services/point", uriPort = 443)
public class PointReservationDocumentTest implements CommonTestGiven {

  MockMvc mockMvc;

  @MockBean
  PointReservationDomainService pointReservationDomainService;

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
  @DisplayName("RestDoc - ????????? ?????? ??????")
  void getReservedPoints() throws Exception {
    //given:
    ReservePointRequestDTO reservationRequest = givenReservationRequestDTO();
    ReservationResultParam reservationResultParam = givenReservationResultVO(reservationRequest);
    List<ReservationResultParam> reservedParams = new ArrayList<>(Collections.singletonList(reservationResultParam));

    given(pointReservationDomainService.getReservedPoints(anyLong()))
            .willReturn(reservedParams);

    ResultActions resultActions = mockMvc.perform(
            RestDocumentationRequestBuilders.get("/v2/members/{memberNumber}/reserved-points", 100000)
                    .contentType(MediaType.APPLICATION_JSON)
    );

    resultActions
            .andExpect(status().isOk())
            .andDo(
                    MockMvcRestDocumentation.document("point/pri/{method-name}"
                            , ApiDocumentUtils.getDocumentRequest()
                            , ApiDocumentUtils.getDocumentResponse()
                            , pathParameters(
                                    parameterWithName("memberNumber").description("????????????")
                            )
                            , responseFields(
                                    beneathPath("data").withSubsectionId("data")
                                    , fieldWithPath("id").type(JsonFieldType.NUMBER).description("?????? ??????")
                                    , fieldWithPath("memberNumber").type(JsonFieldType.NUMBER).description("?????? ??????")
                                    , fieldWithPath("reservedPoint").type(JsonFieldType.NUMBER).description("?????? ?????????")
                                    , fieldWithPath("historyType").type(JsonFieldType.NUMBER).description("?????? ??????")
                                    , fieldWithPath("payment").type(JsonFieldType.BOOLEAN).description("???????????? - ????????? ????????? ??? ?????????????????? ???????????????.")
                                    , fieldWithPath("settle").type(JsonFieldType.BOOLEAN).description("???????????? - ????????? ????????? ?????? ???????????? ???????????????.")
                                    , fieldWithPath("applied").type(JsonFieldType.BOOLEAN).description("?????? ?????? - ?????? ???????????? ??? ??????????????? ???????????? ?????????.")
                                    , fieldWithPath("startDateTime").type(JsonFieldType.STRING).attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX")).description("????????????")
                                    , fieldWithPath("createDateTime").type(JsonFieldType.STRING).attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX")).description("????????????")
                                    , fieldWithPath("expireDateTime").type(JsonFieldType.STRING).attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX")).description("????????????")
                            )
                    )
            );

  }

  @Test
  @DisplayName("RestDoc - ????????? ??????")
  void reservePoint() throws Exception {
    ReservePointRequestDTO reservationRequest = givenReservationRequestDTO();
    ReservationResultParam reservationResultParam = givenReservationResultVO(reservationRequest);

      given(pointReservationDomainService.reserve(any()))
            .willReturn(reservationResultParam);

      ResultActions resultActions = mockMvc.perform(
              RestDocumentationRequestBuilders.post("/v2/reserve")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(reservationRequest))
      );

    resultActions
        .andExpect(status().isOk())
        .andDo(
            MockMvcRestDocumentation.document("point/pri/{method-name}"
                , ApiDocumentUtils.getDocumentRequest()
                , ApiDocumentUtils.getDocumentResponse()
                , requestFields(
                    fieldWithPath("memberNumber").type(JsonFieldType.NUMBER).description("????????????")
                    , fieldWithPath("point").type(JsonFieldType.NUMBER).description("?????? ?????? ??????")
                    , fieldWithPath("historyType").type(JsonFieldType.NUMBER).description("?????? ??????")
                    , fieldWithPath("payment").type(JsonFieldType.BOOLEAN)
                        .description("???????????? - ????????? ????????? ??? ?????????????????? ???????????????.").optional()
                    , fieldWithPath("settle").type(JsonFieldType.BOOLEAN)
                        .description("???????????? - ????????? ????????? ?????? ???????????? ???????????????.").optional()
                    , fieldWithPath("unlimitedDate").type(JsonFieldType.BOOLEAN)
                        .description("?????? ????????? ??????").optional()
                    , fieldWithPath("expireDate").type(JsonFieldType.STRING)
                        .description("????????? - ?????????")
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                        .optional()
                    , fieldWithPath("memo").type(JsonFieldType.STRING).description("?????? ?????????(?????????)")
                        .optional()
                    , fieldWithPath("detail").type(JsonFieldType.STRING).description("?????? ?????????(?????????)")
                        .optional()
                    , fieldWithPath("actionMemberNumber").type(JsonFieldType.NUMBER)
                        .description("????????? ?????? ??????")
                    , fieldWithPath("hidden").type(JsonFieldType.BOOLEAN).description("?????? ?????? ??????")
                        .optional()
                    , fieldWithPath("applied").type(JsonFieldType.BOOLEAN).description("???????????? ??????")
                        .optional()
                    , fieldWithPath("startDate").type(JsonFieldType.STRING)
                            .description("????????? - ????????? ?????? ?????????")
                            .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                            .optional()
                    )
                , responseFields(
                    beneathPath("data").withSubsectionId("data")
                    , fieldWithPath("id").type(JsonFieldType.NUMBER).description("?????? ??????")
                    , fieldWithPath("memberNumber").type(JsonFieldType.NUMBER).description("?????? ??????")
                    , fieldWithPath("reservedPoint").type(JsonFieldType.NUMBER).description("?????? ?????????")
                    , fieldWithPath("historyType").type(JsonFieldType.NUMBER).description("?????? ??????")
                    , fieldWithPath("payment").type(JsonFieldType.BOOLEAN).description("???????????? - ????????? ????????? ??? ?????????????????? ???????????????.")
                    , fieldWithPath("settle").type(JsonFieldType.BOOLEAN).description("???????????? - ????????? ????????? ?????? ???????????? ???????????????.")
                    , fieldWithPath("applied").type(JsonFieldType.BOOLEAN).description("?????? ?????? - ?????? ???????????? ??? ??????????????? ???????????? ?????????.")
                    , fieldWithPath("startDateTime").type(JsonFieldType.STRING).attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX")).description("????????????")
                    , fieldWithPath("createDateTime").type(JsonFieldType.STRING).attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX")).description("????????????")
                    , fieldWithPath("expireDateTime").type(JsonFieldType.STRING).attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX")).description("????????????")
                )
            )
        );
  }

    ReservePointRequestDTO givenReservationRequestDTO() {
    return new ReservePointRequestDTO(
            givenMemberNumber(),
            0,
            1000L,
            0.1f,
            HistoryType.TYPE_12.getValue(),
            false,
            false,
            false,
            ZonedDateTime.now().plusYears(1),
            "black priday event reservation",
            "?????? ????????? ?????? ????????? ?????? ?????????",
            0,
            false,
            ZonedDateTime.now().plusDays(1)
    );
  }

  ReservationResultParam givenReservationResultVO(ReservePointRequestDTO requestParam) {
      return ReservationResultParam.create(
              1,
              requestParam.getMemberNumber(),
              requestParam.getPoint(),
              requestParam.getHistoryType(),
              requestParam.isPayment(),
              requestParam.isSettle(),
              false,
              requestParam.getStartDate().toLocalDateTime(),
              LocalDateTime.now(),
              Objects.requireNonNull(requestParam.getExpireDate()).toLocalDateTime()
      );
  }
}
