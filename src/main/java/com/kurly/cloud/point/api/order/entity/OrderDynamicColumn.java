package com.kurly.cloud.point.api.order.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

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

  @Column(name = "`column`")
  String column;

  @Column(name = "value")
  String value;
}