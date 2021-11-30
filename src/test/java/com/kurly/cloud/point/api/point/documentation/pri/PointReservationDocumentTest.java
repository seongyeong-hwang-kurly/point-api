package com.kurly.cloud.point.api.point.documentation.pri;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.config.SpringSecurityTestConfig;
import com.kurly.cloud.point.api.point.documentation.ApiDocumentUtils;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.ReservationResultVO;
import com.kurly.cloud.point.api.point.domain.publish.ReservePointRequestDTO;
import com.kurly.cloud.point.api.point.service.impl.PointReservationDomainService;
import com.kurly.cloud.point.api.point.web.dto.ReservationResultDTO;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
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
  @DisplayName("RestDoc - 적립금 예약")
  void reservePoint() throws Exception {
    ReservePointRequestDTO reservationRequest = givenReservationRequestDTO();
    ReservationResultVO reservationResultVO = givenReservationResultVO(reservationRequest, 1);
    ReservationResultDTO reservationResultDTO = givenReservationResultDTO(reservationResultVO);

      given(pointReservationDomainService.reserve(any()))
            .willReturn(reservationResultVO);

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
                    fieldWithPath("memberNumber").type(JsonFieldType.NUMBER).description("회원번호")
                    , fieldWithPath("point").type(JsonFieldType.NUMBER).description("예약 적립 금액")
                    , fieldWithPath("historyType").type(JsonFieldType.NUMBER).description("사유 번호")
                    , fieldWithPath("payment").type(JsonFieldType.BOOLEAN)
                        .description("결제여부 - 실제로 결제를 한 적립금인지를 판단합니다.").optional()
                    , fieldWithPath("settle").type(JsonFieldType.BOOLEAN)
                        .description("유상여부 - 무상을 제외한 모든 적립금은 유상입니다.").optional()
                    , fieldWithPath("unlimitedDate").type(JsonFieldType.BOOLEAN)
                        .description("기한 무제한 여부").optional()
                    , fieldWithPath("expireDate").type(JsonFieldType.STRING)
                        .description("만료일 - 만료일")
                        .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                        .optional()
                    , fieldWithPath("memo").type(JsonFieldType.STRING).description("발급 사유명(내부용)")
                        .optional()
                    , fieldWithPath("detail").type(JsonFieldType.STRING).description("발급 사유명(고객용)")
                        .optional()
                    , fieldWithPath("actionMemberNumber").type(JsonFieldType.NUMBER)
                        .description("작업자 회원 번호")
                    , fieldWithPath("hidden").type(JsonFieldType.BOOLEAN).description("이력 숨김 여부")
                        .optional()
                    , fieldWithPath("applied").type(JsonFieldType.BOOLEAN).description("유류정산 여부")
                        .optional()
                    , fieldWithPath("startDate").type(JsonFieldType.STRING)
                            .description("시작일 - 포인트 유효 시작일")
                            .attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX"))
                            .optional()
                    )
                , responseFields(
                    beneathPath("data").withSubsectionId("data")
                    , fieldWithPath("id").type(JsonFieldType.NUMBER).description("예약 번호")
                    , fieldWithPath("memberNumber").type(JsonFieldType.NUMBER).description("회원 번호")
                    , fieldWithPath("reservedPoint").type(JsonFieldType.NUMBER).description("예약 포인트")
                    , fieldWithPath("historyType").type(JsonFieldType.NUMBER).description("사유 번호")
                    , fieldWithPath("payment").type(JsonFieldType.BOOLEAN).description("결제여부 - 실제로 결제를 한 적립금인지를 판단합니다.")
                    , fieldWithPath("settle").type(JsonFieldType.BOOLEAN).description("유상여부 - 무상을 제외한 모든 적립금은 유상입니다.")
                    , fieldWithPath("applied").type(JsonFieldType.BOOLEAN).description("전환 여부 - 예약 상태에서 실 포인트로의 전환여부 입니다.")
                    , fieldWithPath("startDateTime").type(JsonFieldType.STRING).attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX")).description("등록시각")
                    , fieldWithPath("createDateTime").type(JsonFieldType.STRING).attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX")).description("등록시각")
                    , fieldWithPath("expireDateTime").type(JsonFieldType.STRING).attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ssXXX")).description("만료시각")
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
            "블랙 프라이 데이 포인트 적립 이벤트",
            0,
            false,
            ZonedDateTime.now().plusDays(1)
    );
  }

  ReservationResultVO givenReservationResultVO(ReservePointRequestDTO requestVO, long id) {
      return ReservationResultVO.create(
              id,
              requestVO.getMemberNumber(),
              requestVO.getPoint(),
              requestVO.getHistoryType(),
              requestVO.isPayment(),
              requestVO.isSettle(),
              false,
              requestVO.getStartDate().toLocalDateTime(),
              LocalDateTime.now(),
              Objects.requireNonNull(requestVO.getExpireDate()).toLocalDateTime()
      );
  }

  ReservationResultDTO givenReservationResultDTO(ReservationResultVO resultVO){
      ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");
      return ReservationResultDTO.create(
              resultVO.getId(),
              resultVO.getMemberNumber(),
              resultVO.getReservedPoint(),
              resultVO.getHistoryType(),
              resultVO.isPayment(),
              resultVO.isSettle(),
              resultVO.isApplied(),
              ZonedDateTime.of(resultVO.getStartedAt(), ZONE_ID),
              ZonedDateTime.now(),
              ZonedDateTime.of(resultVO.getExpiredAt(), ZONE_ID)
      );
  }
}
