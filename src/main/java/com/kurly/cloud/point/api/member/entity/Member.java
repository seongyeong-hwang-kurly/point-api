package com.kurly.cloud.point.api.member.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
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
@Table(name = "gd_member")
public class Member {
  @Id
  @Column(name = "m_no")
  long memberNumber;

  @Column(name = "m_id")
  String memberId;

  @Column(name = "recommid")
  String recommendMemberId;

  @Column(name = "mobile")
  String mobile;

  @Column(name = "address")
  String address;

  @Column(name = "address_sub")
  String addressSub;

  @Column(name = "road_address")
  String roadAddress;

  @Column(name = "name")
  String name;

  @Transient
  public String getJibunFullAddress() {
    return address + " " + addressSub;
  }

  @Transient
  public String getRoadFullAddress() {
    return roadAddress + " " + addressSub;
  }
}
