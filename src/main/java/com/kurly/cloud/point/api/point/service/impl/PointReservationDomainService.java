package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.domain.publish.ReservationRequestParam;
import com.kurly.cloud.point.api.point.domain.publish.ReservationResultParam;
import com.kurly.cloud.point.api.point.entity.PointReservationEntity;
import com.kurly.cloud.point.api.point.entity.PointReservationHistoryEntity;
import com.kurly.cloud.point.api.point.repository.PointReservationHistoryRepository;
import com.kurly.cloud.point.api.point.repository.PointReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.kurly.cloud.point.api.point.service.helper.PointReservationHelper.convert;

@Service
@RequiredArgsConstructor
public class PointReservationDomainService {
    private final PublishPointServiceV2 publishPointServiceV2;
    private final PointReservationRepository pointReservationRepository;
    private final PointReservationHistoryRepository pointReservationHistoryRepository;

    @Transactional
    public ReservationResultParam reserve(
            ReservationRequestParam reservationRequestParam) {
        PointReservationEntity saved = pointReservationRepository.save(reservationRequestParam.convertToEntity());
        pointReservationHistoryRepository.save(PointReservationHistoryEntity.from(saved));
        return ReservationResultParam.from(saved);
    }

    public long sumReservedPoint(long memberNumber) {
        return findPointReservations(memberNumber).stream()
                .mapToLong(PointReservationEntity::getPoint).sum();
    }

    public List<PointReservationEntity> findPointReservations(long memberNumber) {
        return pointReservationRepository.findAllByMemberNumberAndAppliedIsFalse(memberNumber);
    }

    @Transactional
    public void transformIfReservedPointBefore(long memberNumber, LocalDateTime criterionAt) {
        findPointReservations(memberNumber).stream()
                .filter(it->it.getStartedAt().isBefore(criterionAt) && !it.isApplied())
                .forEach(this::transform);
    }

    private void transform(PointReservationEntity reservedOne) {
        reservedOne.apply(publishPointServiceV2.publish(convert(reservedOne)));
        pointReservationHistoryRepository.save(PointReservationHistoryEntity.from(reservedOne));
    }

    public List<ReservationResultParam> getReservedPoints(long memberNumber) {
        return Collections.emptyList();
    }
}
