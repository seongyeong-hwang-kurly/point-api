package com.kurly.cloud.point.api.point.adapter.out.controller.pri;

import com.kurly.cloud.point.api.point.adapter.out.dto.AvailableMemberPointDto;
import com.kurly.cloud.point.api.point.adapter.out.dto.MemberPointHistoryDto;
import com.kurly.cloud.point.api.point.adapter.out.dto.MemberPointSummaryDto;
import com.kurly.cloud.point.api.point.adapter.out.dto.SimplePageImpl;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest;
import com.kurly.cloud.point.api.point.port.out.MemberPointPort;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController("PrivateMemberPointController")
public class MemberPointController {

  private final MemberPointPort memberPointPort;

  @GetMapping("/v1/history/{memberNumber}")
  SimplePageImpl<MemberPointHistoryDto> getMemberHistoryList(
      @PathVariable long memberNumber,
      MemberPointHistoryListRequest request) {
    request.setMemberNumber(memberNumber);
    request.setIncludeHidden(true);
    request.setIncludeMemo(true);
    return SimplePageImpl.transform(memberPointPort.getMemberHistoryList(request));
  }

  @GetMapping("/v1/summary/{memberNumber}")
  MemberPointSummaryDto getMemberPointSummary(@PathVariable long memberNumber) {
    return MemberPointSummaryDto.fromEntity(memberPointPort.getMemberPointSummary(memberNumber));
  }

  @GetMapping("/v1/available/{memberNumber}")
  AvailableMemberPointDto getMemberPoint(@PathVariable long memberNumber) {
    return AvailableMemberPointDto.fromEntity(memberPointPort.getMemberPoint(memberNumber));
  }

  @GetMapping("/v1/is-available/{memberNumber}")
  Boolean getMemberPoint(@PathVariable long memberNumber,
                         @RequestParam @Min(1) long point,
                         @RequestParam(required = false, defaultValue = "false") boolean settle) {
    return memberPointPort.getMemberPoint(memberNumber).isEnough(point, settle);
  }
}
