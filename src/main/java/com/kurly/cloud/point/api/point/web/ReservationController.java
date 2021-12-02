package com.kurly.cloud.point.api.point.web;

import com.kurly.cloud.point.api.point.domain.publish.ReservationResultVO;
import com.kurly.cloud.point.api.point.domain.publish.ReservePointRequestDTO;
import com.kurly.cloud.point.api.point.service.impl.PointReservationDomainService;
import com.kurly.cloud.point.api.point.web.dto.ReservationResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequiredArgsConstructor
public class ReservationController {
    private final PointReservationDomainService pointReservationDomainService;

    @PostMapping(value = "/v2/reserve", consumes = MediaType.APPLICATION_JSON_VALUE)
    ReservationResultDTO reserve(
            @RequestBody @Valid ReservePointRequestDTO request
    ) {
        ReservationResultVO vo = pointReservationDomainService.reserve(request.toVO());
        return ReservationResultDTO.from(vo);
    }

    @GetMapping(value = "/v2/members/{memberNumber}/reserved-points", consumes = MediaType.APPLICATION_JSON_VALUE)
    List<ReservationResultDTO> getReservedPoints(@PathVariable("memberNumber") long memberNumber) {
        List<ReservationResultVO> reservedPoints = pointReservationDomainService.getReservedPoints(memberNumber);
        return reservedPoints.stream().map(ReservationResultDTO::from).collect(Collectors.toList());
    }
}
