package com.kurly.cloud.point.api.order.entity;

import com.kurly.cloud.point.api.member.entity.Member;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

  @Column(name = "m_no", insertable = false, updatable = false)
  long memberNumber;

  @JoinColumn(name = "m_no")
  @ManyToOne(fetch = FetchType.LAZY)
  Member member;

  @Column(name = "cdt")
  LocalDateTime payDateTime;

  @Column(name = "prn_settleprice")
  int payPrice;

  @Column(name = "step")
  int orderStatus;

  @Column(name = "step2")
  int orderProcessCode;

  @OneToMany(mappedBy = "order")
  List<OrderDynamicColumn> orderDynamicColumns;

  @Column(name = "confirmdt")
  LocalDateTime deliveredDateTime;

  @Column(name = "mobileorder")
  String mobile;

  @Column(name = "address")
  String jibunFullAddress;

  @Column(name = "road_address")
  String roadFullAddress;

  @Column(name = "addr_divide1")
  String address;

  @Column(name = "addr_divide2")
  String addressSub;

  public String getFullAddress() {
    return roadFullAddress.isEmpty() ? jibunFullAddress : roadFullAddress;
  }
}
