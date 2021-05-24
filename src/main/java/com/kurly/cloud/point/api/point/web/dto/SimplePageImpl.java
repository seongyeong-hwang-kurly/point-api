package com.kurly.cloud.point.api.point.web.dto;

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
