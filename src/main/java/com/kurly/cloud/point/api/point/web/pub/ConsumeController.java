package com.kurly.cloud.point.api.point.web.pub;

import com.kurly.cloud.api.common.config.KurlyUserPrincipal;
import com.kurly.cloud.api.common.domain.exception.ApiErrorResponse;
import com.kurly.cloud.point.api.point.domain.consume.OrderConsumePointRequest;
import com.kurly.cloud.point.api.point.exception.NotEnoughPointException;
import com.kurly.cloud.point.api.point.service.ConsumePointUseCase;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController("PublicConsumeController")
public class ConsumeController {

  private final ConsumePointUseCase consumePointUseCase;

  @Secured("ROLE_USER")
  @PostMapping(value = "/public/v1/consume/order", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<?> consumeByOrder(@RequestBody @Valid OrderConsumePointRequest request,
                                   @AuthenticationPrincipal KurlyUserPrincipal principal) {
    if (!request.getMemberNumber().equals(principal.getNo())) {
      throw new AccessDeniedException("권한이 없습니다");
    }
    try {
      consumePointUseCase.consumeByOrder(request);
    } catch (NotEnoughPointException e) {
      throw new ApiErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    return ResponseEntity.noContent().build();
  }
}
