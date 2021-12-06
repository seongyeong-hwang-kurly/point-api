package com.kurly.cloud.point.api.batch.order.entity;

import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "column='point_ratio'")
@Entity
@Table(name = "gd_order_dynamic_column")
public class OrderDynamicColumn {
  @Id
  @Column(name = "ordno")
  long orderNumber;

  @Column(name = "column")
  String column;

  @Column(name = "value")
  String value;
}