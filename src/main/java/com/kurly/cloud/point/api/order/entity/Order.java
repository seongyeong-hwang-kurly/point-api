package com.kurly.cloud.point.api.order.entity;

import com.kurly.cloud.point.api.member.entity.Member;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
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

  @Column(name = "reserve")
  long publishPoint;

  @OneToOne
  @JoinColumn(name = "ordno")
  OrderDynamicColumn orderDynamicColumn;

  @Column(name = "confirmdt")
  LocalDateTime deliveredDateTime;

  @Column(name = "mobileorder")
  String mobile;

  @Builder.Default
  @Column(name = "address")
  String jibunFullAddress = "";

  @Builder.Default
  @Column(name = "road_address")
  String roadFullAddress = "";

  @Builder.Default
  @Column(name = "addr_divide1")
  String address = "";

  @Builder.Default
  @Column(name = "addr_divide2")
  String addressSub = "";

  public String getFullAddress() {
    return roadFullAddress.isEmpty() ? jibunFullAddress : roadFullAddress;
  }
}
