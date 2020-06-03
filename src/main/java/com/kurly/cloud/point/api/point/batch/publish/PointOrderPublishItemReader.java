/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.batch.publish;

import com.kurly.cloud.point.api.order.entity.Order;
import com.kurly.cloud.point.api.order.repository.OrderRepository;
import com.kurly.cloud.point.api.point.batch.publish.config.PointOrderPublishJobConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.data.domain.PageRequest;

public class PointOrderPublishItemReader extends AbstractPagingItemReader<Order> {

  private final OrderRepository orderRepository;
  private final LocalDate publishDate;
  private final LocalDateTime fromDateTime;
  private final LocalDateTime toDateTime;

  /**
   * 기본 생성자.
   */
  public PointOrderPublishItemReader(OrderRepository orderRepository,
                                     int pageSize,
                                     String publishDate) {
    setName("pointOrderPublishItemReader");
    setPageSize(pageSize);
    this.orderRepository = orderRepository;
    this.publishDate = LocalDate.parse(publishDate, PointOrderPublishJobConfig.DATE_TIME_FORMATTER);
    this.fromDateTime = LocalDateTime.of(this.publishDate.minusDays(2), LocalTime.of(23, 0, 0));
    this.toDateTime = this.fromDateTime.plusDays(1);
  }

  @Override protected void doReadPage() {
    if (results == null) {
      results = new ArrayList<>();
    } else {
      results.clear();
    }

    List<Order> orders = orderRepository.findAllPointPublishableOrder(
        fromDateTime, toDateTime, PageRequest.of(getPage(), getPageSize()));

    results.addAll(orders);
  }

  @Override protected void doJumpToPage(int itemIndex) {
  }

}
