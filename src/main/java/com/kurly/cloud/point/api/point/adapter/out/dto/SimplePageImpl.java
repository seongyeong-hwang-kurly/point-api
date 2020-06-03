/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.adapter.out.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
public class SimplePageImpl<T> {

  List<T> content;
  int totalPages;
  long totalElements;
  int size;
  long numberOfElements;
  int number;
  boolean last;
  boolean first;

  /**
   * Page 객체를 SimplePageImpl로 변환.
   */
  public static <T> SimplePageImpl<T> transform(Page<T> page) {
    SimplePageImpl<T> simplePage = new SimplePageImpl<>();
    simplePage.setContent(page.getContent());
    simplePage.setTotalPages(page.getTotalPages());
    simplePage.setTotalElements(page.getTotalElements());
    simplePage.setLast(page.isLast());
    simplePage.setSize(page.getSize());
    simplePage.setNumberOfElements(page.getNumberOfElements());
    simplePage.setFirst(page.isFirst());
    simplePage.setNumber(page.getNumber());
    return simplePage;
  }

}
