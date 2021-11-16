package com.kurly.cloud.point.api.point.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mk_point_reservation",
        indexes = {
        @Index(columnList = "memberNumber")
    }
)
public class PointReservationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long memberNumber;
    private long point;
    private int historyType;
    private boolean payment;
    private boolean settle;
    private LocalDateTime expireDate;
    @Builder.Default
    private String memo = "";
    private String detail;
    private long actionMemberNumber;
    private boolean hidden;

    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    private Point pointEntity;
    @Builder.Default
    private boolean applied = false;
    private LocalDateTime startedAt;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void apply(Point point) {
        this.pointEntity = point;
        this.applied = true;
        this.updatedAt = LocalDateTime.now();
    }
}
