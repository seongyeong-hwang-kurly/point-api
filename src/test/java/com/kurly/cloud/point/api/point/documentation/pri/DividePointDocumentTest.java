package com.kurly.cloud.point.api.point.documentation.pri;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.config.SpringSecurityTestConfig;
import com.kurly.cloud.point.api.point.documentation.ApiDocumentUtils;
import com.kurly.cloud.point.api.point.service.DivideUsingFreePointService;
import com.kurly.cloud.point.api.point.web.dto.DealProductRequestDto;
import com.kurly.cloud.point.api.point.web.dto.DivideUsingFreePointRequestDto;
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

import java.util.ArrayList;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Import(SpringSecurityTestConfig.class)
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@DisplayName("DividePointDocument")
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "gateway.cloud.dev.kurly.services/point", uriPort = 443)
public class DividePointDocumentTest implements CommonTestGiven {

    MockMvc mockMvc;

    @MockBean
    DivideUsingFreePointService divideUsingFreePointService;

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
    @DisplayName("RestDoc - ?????? ????????? ?????? ")
    void divideFreePoint() throws Exception {
        List<DealProductRequestDto> dealList = new ArrayList<>();
        dealList.add(DealProductRequestDto.create(1L, null,1894));
        dealList.add(DealProductRequestDto.create(2L, null, 10938));
        dealList.add(DealProductRequestDto.create(3L, null, 4262));
        dealList.add(DealProductRequestDto.create(4L, null,806));
        DivideUsingFreePointRequestDto param = DivideUsingFreePointRequestDto.create(
                17900, 2010, 0,
                dealList
        );

        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders.post("/v1/divide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(param ))
        );

        resultActions
                .andExpect(status().isOk())
                .andDo(
                        MockMvcRestDocumentation.document("point/pri/{method-name}"
                                , ApiDocumentUtils.getDocumentRequest()
                                , ApiDocumentUtils.getDocumentResponse()
                                , requestFields(
                                        fieldWithPath("totalDealProductPrice").type(JsonFieldType.NUMBER).description("??? ????????????")
                                        , fieldWithPath("usingFreePoint").type(JsonFieldType.NUMBER).description("??? ?????? ?????? ?????????")
                                        , fieldWithPath("usingPaidPoint").type(JsonFieldType.NUMBER).description("??? ?????? ?????? ?????????")
                                        , fieldWithPath("dealProducts").type(JsonFieldType.ARRAY).description("???????????????")
                                        , fieldWithPath("dealProducts[].dealProductNo").type(JsonFieldType.NUMBER)
                                                .description("??? ????????????")
                                        , fieldWithPath("dealProducts[].contentsProductNo").type(JsonFieldType.NUMBER)
                                                .description("????????? ?????? ??????").optional()
                                        ,fieldWithPath("dealProducts[].sellingPrice").type(JsonFieldType.NUMBER)
                                                .description("??? ????????????")
                                )
                                , responseFields(
                                        beneathPath("data").withSubsectionId("data")
                                        , fieldWithPath("dealProducts").type(JsonFieldType.ARRAY).description("????????? ???????????? ?????? ???????????????")
                                        , fieldWithPath("dealProducts[].dealProductNo").type(JsonFieldType.NUMBER).description("??? ????????????")
                                        , fieldWithPath("dealProducts[].contentsProductNo").type(JsonFieldType.NUMBER)
                                                .description("????????? ?????? ??????").optional()
                                        , fieldWithPath("dealProducts[].usedFreePoint").type(JsonFieldType.NUMBER).description("????????? ????????? ?????? ?????????")
                                        , fieldWithPath("dealProducts[].usedPaidPoint").type(JsonFieldType.NUMBER).description("????????? ????????? ???????????????")
                                )
                        )
                );

    }


}
