package com.kurly.cloud.point.api.point.web;

import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest;
import com.kurly.cloud.point.api.point.service.MemberPointUseCase;
import com.kurly.cloud.point.api.point.service.impl.PointReservationDomainService;
import com.kurly.cloud.point.api.point.web.dto.AvailableMemberPointDto;
import com.kurly.cloud.point.api.point.web.dto.MemberPointHistoryDto;
import com.kurly.cloud.point.api.point.web.dto.MemberPointSummaryDto;
import com.kurly.cloud.point.api.point.web.dto.SimplePageImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;

import static java.time.LocalDateTime.now;

@Validated
@RequiredArgsConstructor
@RestController("PrivateMemberPointController")
public class MemberPointController {
  private final PointReservationDomainService pointReservationDomainService;
  private final MemberPointUseCase memberPointUseCase;

  @GetMapping("/v1/history/{memberNumber}")
  SimplePageImpl<MemberPointHistoryDto> getMemberHistoryList(
      @PathVariable long memberNumber,
      MemberPointHistoryListRequest request) {
    request.setMemberNumber(memberNumber);
    request.setIncludeHidden(true);
    request.setIncludeMemo(true);
    return SimplePageImpl.transform(memberPointUseCase.getMemberHistoryList(request));
  }

  @GetMapping("/v1/summary/{memberNumber}")
  MemberPointSummaryDto getMemberPointSummary(@PathVariable long memberNumber) {
    return MemberPointSummaryDto.fromEntity(memberPointUseCase.getMemberPointSummary(memberNumber));
  }

  @GetMapping("/v1/available/{memberNumber}")
  AvailableMemberPointDto getMemberPoint(@PathVariable long memberNumber) {
    pointReservationDomainService.transformIfReservedPointBefore(memberNumber, now());
    return AvailableMemberPointDto.fromEntity(memberPointUseCase.getMemberPoint(memberNumber));
  }

  @GetMapping("/v1/is-available/{memberNumber}")
  Boolean getMemberPoint(@PathVariable long memberNumber,
                         @RequestParam @Min(1) long point,
                         @RequestParam(required = false, defaultValue = "false") boolean settle) {
    return memberPointUseCase.getMemberPoint(memberNumber).isEnough(point, settle);
  }
}
