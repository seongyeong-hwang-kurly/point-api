package com.kurly.cloud.point.api.point.domain.publish;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

@Getter
public class BulkPublishPointResult {
  Set<Integer> succeed = new HashSet<>();
  Set<Integer> failed = new HashSet<>();

  public void addSuccess(int seq) {
    this.succeed.add(seq);
  }

  public void addFailed(int seq) {
    this.failed.add(seq);
  }
}
