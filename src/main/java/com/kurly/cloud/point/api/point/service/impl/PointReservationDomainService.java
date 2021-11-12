package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.domain.publish.ReservationRequestVO;
import com.kurly.cloud.point.api.point.domain.publish.ReservationResultVO;
import com.kurly.cloud.point.api.point.entity.PointReservationEntity;
import com.kurly.cloud.point.api.point.repository.PointReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static com.kurly.cloud.point.api.point.service.helper.PointReservationHelper.convert;

@Service
@RequiredArgsConstructor
public class PointReservationDomainService {
    private final PointReservationRepository pointReservationRepository;
    private final PublishPointServiceV2 publishPointServiceV2;

    public ReservationResultVO reserve(
            ReservationRequestVO reservationRequestVO) {
        PointReservationEntity saved = pointReservationRepository.save(reservationRequestVO.convertToEntity());

        return ReservationResultVO.from(saved);
    }

    public long sumReservedPoint(long memberNumber) {
        return findPointReservations(memberNumber).stream()
                .mapToLong(PointReservationEntity::getPoint).sum();
    }

    public List<PointReservationEntity> findPointReservations(long memberNumber) {
        return pointReservationRepository.findAllByMemberNumber(memberNumber);
    }

    @Transactional
    public void transformIfReservedPointBefore(long memberNumber, LocalDateTime criterionAt) {
        findPointReservations(memberNumber).stream()
                .filter(it->it.getStartedAt().isBefore(criterionAt) && !it.isApplied())
                .forEach(this::transform);
    }

    private void transform(PointReservationEntity reservedOne) {
        reservedOne.apply(publishPointServiceV2.publish(convert(reservedOne)));
    }
}
