package com.kurly.cloud.point.api.point.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.lang.Nullable;

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
    private long id;

    private long memberNumber;
    private long orderNumber;
    private long point;
    private float pointRatio;
    private int historyType;
    private boolean payment;
    private boolean settle;
    private boolean unlimitedDate;
    private LocalDateTime expireDate;
    @Builder.Default
    private String memo = "";
    private String detail;
    private long actionMemberNumber;
    private boolean hidden;

    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    private Point pointEntity;
    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private PointReservationEntity pointReservationEntity;

    @Builder.Default
    private boolean applied = false;
    private LocalDateTime startedAt;
    @CreatedDate
    private LocalDateTime createdAt;

    public static PointReservationHistoryEntity from(PointReservationEntity entity) {
        return new PointReservationHistoryEntity(
                0,
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
