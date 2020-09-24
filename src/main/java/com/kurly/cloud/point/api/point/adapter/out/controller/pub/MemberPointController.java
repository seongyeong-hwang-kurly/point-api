package com.kurly.cloud.point.api.point.adapter.out.controller.pub;

import com.kurly.cloud.point.api.point.adapter.out.dto.AvailableMemberPointDto;
import com.kurly.cloud.point.api.point.adapter.out.dto.MemberPointHistoryDto;
import com.kurly.cloud.point.api.point.adapter.out.dto.MemberPointSummaryDto;
import com.kurly.cloud.point.api.point.adapter.out.dto.SimplePageImpl;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest;
import com.kurly.cloud.point.api.point.port.out.MemberPointPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("PublicMemberPointController")
public class MemberPointController {

  private final MemberPointPort memberPointPort;

  @PreAuthorize("@userSecurity.checkMemberNo(principal,#memberNumber)")
  @GetMapping("/public/v1/history/{memberNumber}")
  SimplePageImpl<MemberPointHistoryDto> getMemberHistoryList(
      @PathVariable long memberNumber,
      MemberPointHistoryListRequest request) {
    request.setMemberNumber(memberNumber);
    request.setIncludeHidden(false);
    request.setIncludeMemo(false);
    return SimplePageImpl.transform(memberPointPort.getMemberHistoryList(request));
  }

  @PreAuthorize("@userSecurity.checkMemberNo(principal,#memberNumber)")
  @GetMapping("/public/v1/summary/{memberNumber}")
  MemberPointSummaryDto getMemberPointSummary(@PathVariable long memberNumber) {
    return MemberPointSummaryDto.fromEntity(memberPointPort.getMemberPointSummary(memberNumber));
  }

  @PreAuthorize("@userSecurity.checkMemberNo(principal,#memberNumber)")
  @GetMapping("/public/v1/available/{memberNumber}")
  AvailableMemberPointDto getMemberPoint(@PathVariable long memberNumber) {
    return AvailableMemberPointDto.fromEntity(memberPointPort.getMemberPoint(memberNumber));
  }
}
