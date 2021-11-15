package com.kurly.cloud.point.api.point.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
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
    private long point;
    private int historyType;
    private boolean payment;
    private boolean settle;
    private LocalDateTime expireDate;
    private String memo;
    private String detail;
    private long actionMemberNumber;
    private boolean hidden;

    @OneToOne(fetch = FetchType.LAZY)
    private Point pointEntity;
    @ManyToOne(fetch = FetchType.LAZY)
    private PointReservationEntity pointReservationEntity;

    private boolean applied;
    private LocalDateTime startedAt;
    @CreatedDate
    private LocalDateTime createdAt;

    public static PointReservationHistoryEntity from(PointReservationEntity entity) {
        return new PointReservationHistoryEntity(
                0L,
                entity.getMemberNumber(),
                entity.getPoint(),
                entity.getHistoryType(),
                entity.isPayment(),
                entity.isSettle(),
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
