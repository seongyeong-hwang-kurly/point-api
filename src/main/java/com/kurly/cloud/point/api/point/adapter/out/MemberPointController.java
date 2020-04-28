/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.adapter.out;

import com.kurly.cloud.api.common.config.KurlyUserPrincipal;
import com.kurly.cloud.point.api.point.adapter.out.dto.SimplePageImpl;
import com.kurly.cloud.point.api.point.domain.MemberPointSummary;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryDto;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest;
import com.kurly.cloud.point.api.point.port.out.MemberPointPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MemberPointController {

  private final MemberPointPort memberPointPort;

  @PreAuthorize("@userSecurity.checkMemberNo(principal,#memberNumber)")
  @GetMapping("/public/v1/history/{memberNumber}")
  SimplePageImpl<MemberPointHistoryDto> getMemberHistoryList(@PathVariable long memberNumber,
                                                             MemberPointHistoryListRequest request,
                                                             @AuthenticationPrincipal
                                                                 KurlyUserPrincipal principal) {
    if (principal.isAdmin()) {
      request.setIncludeHidden(true);
    }
    request.setMemberNumber(memberNumber);
    return SimplePageImpl.transform(memberPointPort.getMemberHistoryList(request));
  }

  @PreAuthorize("@userSecurity.checkMemberNo(principal,#memberNumber)")
  @GetMapping("/public/v1/summary/{memberNumber}")
  MemberPointSummary getMemberPointSummary(@PathVariable long memberNumber) {
    return memberPointPort.getMemberPointSummary(memberNumber);
  }
}
