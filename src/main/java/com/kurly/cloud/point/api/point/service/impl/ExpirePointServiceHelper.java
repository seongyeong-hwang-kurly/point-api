package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.entity.Point;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class ExpirePointServiceHelper {
    public static LocalDateTime getLatestExpiredAt(List<Point> points) {
        return points.stream()
                .filter(it -> it.getExpiredAt() != null)
                .max(Comparator.comparing(Point::getExpiredAt))
                .map(Point::getExpiredAt)
                .orElse(LocalDateTime.now());
    }
}
