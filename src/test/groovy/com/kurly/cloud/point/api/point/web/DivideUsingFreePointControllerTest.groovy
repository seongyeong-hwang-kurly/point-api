package com.kurly.cloud.point.api.point.web

import com.kurly.cloud.point.api.point.param.DealProductRequestParam
import com.kurly.cloud.point.api.point.param.DealProductResponseParam
import com.kurly.cloud.point.api.point.param.DivideUsingFreePointRequestParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter
import spock.lang.Specification

@SpringBootTest
class DivideUsingFreePointControllerTest extends Specification {
    @Autowired
    private DivideUsingFreePointController controller

    private MockMvc mockMvc;

    void setup(WebApplicationContext webApplicationContext){
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }



    def '적립금을 단수처리해서 배분한다'(){
        given:
        DivideUsingFreePointRequestParam param = DivideUsingFreePointRequestParam.create(
                17900, 2010, 0,
                [
                        DealProductRequestParam.create(1, 1894),
                        DealProductRequestParam.create(2, 10938),
                        DealProductRequestParam.create(3, 4262),
                        DealProductRequestParam.create(4,806)
                ]
        );
        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/divide", param);
        when:
        List<DealProductResponseParam> dealProducts = this.mockMvc.perform(MockHttpServletRequestBuilder).andDo(MockMvcResultHandlers.print());
        then:
        dealProducts.size() == 4
        dealProducts.get(0).getUsedFreePoint() == 213
        dealProducts.get(1).getUsedFreePoint() == 1229
        dealProducts.get(2).getUsedFreePoint() == 478
        dealProducts.get(3).getUsedFreePoint() == 90
        dealProducts.stream().mapToInt({ it -> it.getUsedFreePoint() }).sum() == 2010
    }

}
