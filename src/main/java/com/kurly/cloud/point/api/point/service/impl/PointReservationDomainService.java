package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointReservationRequestVO;
import com.kurly.cloud.point.api.point.entity.PointReservationEntity;
import com.kurly.cloud.point.api.point.repository.PointReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointReservationDomainService {
    private final PointReservationRepository pointReservationRepository;
    private final PublishPointServiceV2 publishPointServiceV2;

    public void reserve(PublishPointReservationRequestVO publishPointReservationRequestVO) {
        pointReservationRepository.save(publishPointReservationRequestVO.convertToEntity());
    }

    public long sumReservedPoint(long memberNumber) {
        List<PointReservationEntity> points = pointReservationRepository.findAllByMemberNumber(memberNumber);
        return points.stream().mapToLong(PointReservationEntity::getPoint).sum();
    }

    @Transactional
    public void transform(long memberNumber, LocalDateTime criterionAt) {
        pointReservationRepository.findAllByMemberNumber(memberNumber).stream()
                .filter(it->it.getStartedAt().isBefore(criterionAt))
                .forEach(it-> publishPointServiceV2.publish(PublishPointRequest.builder().build()));
    }


}
