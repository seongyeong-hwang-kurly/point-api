/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.order.entity;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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
