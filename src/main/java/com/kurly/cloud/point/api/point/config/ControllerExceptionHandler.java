package com.kurly.cloud.point.api.point.config;

import com.kurly.cloud.api.common.domain.ApiResponseModel;
import java.text.MessageFormat;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerExceptionHandler {

  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ExceptionHandler({BindException.class,
      MethodArgumentNotValidException.class,
      ConstraintViolationException.class})
  public ApiResponseModel serverErrorHandler(Exception e) {
    String errorFields = "";

    if (e instanceof BindException) {
      errorFields = ((BindException) e).getFieldErrors()
          .stream().map(FieldError::getField).collect(Collectors.joining(","));
    } else if (e instanceof MethodArgumentNotValidException) {
      errorFields = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors()
          .stream().map(FieldError::getField).collect(Collectors.joining(","));
    } else if (e instanceof ConstraintViolationException) {
      errorFields = ((ConstraintViolationException) e).getConstraintViolations()
          .stream().map(ConstraintViolation::getPropertyPath)
          .map(Path::toString).collect(Collectors.joining(","));
    }

    e.printStackTrace();
    return ApiResponseModel
        .builder()
        .message(MessageFormat.format("입력값이 올바르지 않습니다. [{0}]", errorFields)).build();
  }
}
