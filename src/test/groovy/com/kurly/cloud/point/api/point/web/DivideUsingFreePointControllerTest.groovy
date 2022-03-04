package com.kurly.cloud.point.api.point.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.kurly.cloud.api.common.domain.ApiResponseModel
import com.kurly.cloud.point.api.point.web.dto.DealProductRequestDto
import com.kurly.cloud.point.api.point.web.dto.DivideUsingFreePointRequestDto
import com.kurly.cloud.point.api.point.web.dto.DivideUsingFreePointResponseDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import spock.lang.Subject

@SpringBootTest
class DivideUsingFreePointControllerTest extends Specification {


    @Autowired
    ObjectMapper objectMapper;
    private MockMvc mockMvc;
    @Subject
    DivideUsingFreePointController controller

    def setup() {
        controller = new DivideUsingFreePointController()
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }


    def '적립금을 단수처리해서 배분한다'(){
        given:
        DivideUsingFreePointRequestDto param = DivideUsingFreePointRequestDto.create(
                17900, 2010, 0,

                [
                        DealProductRequestDto.create(1, 1894),
                        DealProductRequestDto.create(2, 10938),
                        DealProductRequestDto.create(3, 4262),
                        DealProductRequestDto.create(4,806)
                ]
        );

        when:
        MvcResult response = this.mockMvc.perform(MockMvcRequestBuilders.post("/v1/divide")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(param )))
                .andDo(MockMvcResultHandlers.print()).andReturn();
        then:
        ApiResponseModel<List<DivideUsingFreePointResponseDto>> result = objectMapper.readValue(response.getResponse().getContentAsString(), ApiResponseModel.class)
        List<DivideUsingFreePointResponseDto> dealProducts = result.getData().get("dealProducts");
        dealProducts.size() == 4
        dealProducts.get(0).get("usedFreePoint") == 213
        dealProducts.get(1).get("usedFreePoint") == 1229
        dealProducts.get(2).get("usedFreePoint") == 478
        dealProducts.get(3).get("usedFreePoint") == 90
        dealProducts.stream().mapToInt({ it -> it.get("usedFreePoint") }).sum() == 2010
    }


}
