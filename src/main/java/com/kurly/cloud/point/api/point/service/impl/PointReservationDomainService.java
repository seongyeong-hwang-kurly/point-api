package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.domain.publish.PublishPointReservationRequestVO;
import com.kurly.cloud.point.api.point.entity.PointReservation;
import com.kurly.cloud.point.api.point.repository.PointReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointReservationDomainService {
    private final PointReservationRepository pointReservationRepository;

    public void reserve(PublishPointReservationRequestVO publishPointReservationRequestVO) {
        pointReservationRepository.save(publishPointReservationRequestVO.convertToEntity());
    }

    public long sumReservedPoint(long memberNumber) {
        List<PointReservation> points = pointReservationRepository.findAllByMemberNumber(memberNumber);
        return points.stream().mapToLong(PointReservation::getRemain).sum();
    }
}
