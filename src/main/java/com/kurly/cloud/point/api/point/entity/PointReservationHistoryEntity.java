package com.kurly.cloud.point.api.point.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@Table(name = "mk_point_reservation_history",
        indexes = {
        @Index(columnList = "memberNumber, startedAt")
    }
)
public class PointReservationHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final long id;

    private final long memberNumber;
    private final long orderNumber;
    private final long point;
    private final float pointRatio;
    private final int historyType;
    private final boolean payment;
    private final boolean settle;
    private final boolean unlimitedDate;
    private final LocalDateTime expireDate;
    private final String memo;
    private final String detail;
    private final long actionMemberNumber;
    private final boolean hidden;

    @OneToOne(fetch = FetchType.LAZY)
    private final Point pointEntity;
    @ManyToOne(fetch = FetchType.LAZY)
    private final PointReservationEntity pointReservationEntity;

    private final boolean applied;
    private final LocalDateTime startedAt;
    @CreatedDate
    private final LocalDateTime createdAt;

    public static PointReservationHistoryEntity from(PointReservationEntity entity) {
        return new PointReservationHistoryEntity(
                0L,
                entity.getMemberNumber(),
                entity.getOrderNumber(),
                entity.getPoint(),
                entity.getPointRatio(),
                entity.getHistoryType(),
                entity.isPayment(),
                entity.isSettle(),
                entity.isUnlimitedDate(),
                entity.getExpireDate(),
                entity.getMemo(),
                entity.getDetail(),
                entity.getActionMemberNumber(),
                entity.isHidden(),

                entity.getPointEntity(),
                entity,

                entity.isApplied(),
                entity.getStartedAt(),
                LocalDateTime.now());
    }
}
