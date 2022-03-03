package com.kurly.cloud.point.api.point.web;

import com.kurly.cloud.point.api.point.service.DivideUsingFreePointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class DivideUsingFreePointController {

    private final DivideUsingFreePointService divideService;



}
