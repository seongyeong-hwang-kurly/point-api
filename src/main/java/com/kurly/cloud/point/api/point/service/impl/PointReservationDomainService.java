package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.domain.publish.PublishPointReservationRequestVO;
import com.kurly.cloud.point.api.point.repository.PointReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointReservationDomainService {
    private final PointReservationRepository pointReservationRepository;

    public void reserve(PublishPointReservationRequestVO publishPointReservationRequestVO) {
        publishPointReservationRequestVO.toEntity();
    }

    public void sumReservedPoint(long memberNo) {
    }
}
