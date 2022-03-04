package com.kurly.cloud.point.api.point.exception.advice;

import com.kurly.cloud.api.common.domain.ApiResponseModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PointControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    ApiResponseModel<?> handelPointNotMatchException(IllegalArgumentException e) {
        return new ApiResponseModel(false, e.getMessage(), null);
    }


}
