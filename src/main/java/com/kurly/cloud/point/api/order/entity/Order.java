package com.kurly.cloud.point.api.order.entity;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gd_order")
public class Order {
  @Id
  @Column(name = "ordno")
  long orderNumber;

  @Column(name = "m_no")
  long memberNumber;

  @Column(name = "cdt")
  LocalDateTime payDateTime;

  @Column(name = "prn_settleprice")
  int payPrice;

  @Column(name = "step")
  int orderStatus;

  @Column(name = "step2")
  int orderProcessCode;

  @OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
  List<OrderDynamicColumn> orderDynamicColumns;

}
