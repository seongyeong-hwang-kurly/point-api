package com.kurly.cloud.point.api.point.batch.publish;

import com.kurly.cloud.api.common.util.SlackNotifier;
import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.exception.AlreadyPublishedException;
import com.kurly.cloud.point.api.point.port.in.PublishPointPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@StepScope
public class PointOrderPublishItemWriter implements ItemWriter<PublishPointRequest> {

  private final PublishPointPort publishPointPort;
  private StepExecution stepExecution;

  @Override public void write(List<? extends PublishPointRequest> items) throws Exception {
    items.parallelStream().forEach(publishPointRequest -> {
      try {
        publishPointPort.publishByOrder(publishPointRequest);

        ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();
        long totalPublishCount = executionContext.getLong("totalPublishCount", 0);
        long totalPublishPointAmount = executionContext.getLong("totalPublishPointAmount", 0);

        executionContext.putLong("totalPublishCount", totalPublishCount + 1);
        executionContext.putLong("totalPublishPointAmount",
            totalPublishPointAmount + publishPointRequest.getPoint());

      } catch (AlreadyPublishedException e) {
        FileBeatLogger.error(e);
        SlackNotifier.notify(e);
      }
    });
  }

  @BeforeStep
  public void saveStepExecution(StepExecution stepExecution) {
    this.stepExecution = stepExecution;
  }
}
