package com.kurly.cloud.point.api.point.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mk_point_reservation",
        indexes = {
                @Index(columnList = "started_at")
        }
)
public class PointReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "m_no")
    private long memberNumber;

    @Column(name = "ordno")
    private long orderNumber;

    @NotNull
    @Column(name = "charge")
    private Long charge;

    @NotNull
    @Column(name = "remain")
    private Long remain;

    @Column(name = "point_ratio")
    private float pointRatio;

    @NotNull
    @Column(name = "point_type")
    private Integer historyType;

    @Column(name = "refund_type")
    private int refundType;

    @Type(type = "numeric_boolean")
    @Column(name = "is_payment")
    private boolean payment;

    @Column(name = "settle_flag")
    private boolean settle;

    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    private Point point;

    @Builder.Default
    private boolean applied = false;

    @NotNull
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @NotNull
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    LocalDateTime updatedAt;


    public static PointReservation from(Point point) {
        return null;
    }

    public void apply(Point point) {
        this.point = point;
        this.applied = true;
    }
}
