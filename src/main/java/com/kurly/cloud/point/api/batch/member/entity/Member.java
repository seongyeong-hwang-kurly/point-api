package com.kurly.cloud.point.api.batch.member.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "gd_member")
public class Member {
  @Id
  @Column(name = "m_no")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  long memberNumber;

  @Column(name = "m_id")
  String memberId;

  @Column(name = "m_uuid")
  String memberUuid;

  @Column(name = "recommid")
  String recommendMemberId;

  @Column(name = "mobile")
  String mobile;

  @Column(name = "name")
  String name;

}
