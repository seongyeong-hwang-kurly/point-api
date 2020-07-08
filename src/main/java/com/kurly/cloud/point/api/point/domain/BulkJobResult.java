package com.kurly.cloud.point.api.point.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;

@Getter
public class BulkJobResult {
  Set<Integer> succeed = new HashSet<>();
  Set<Integer> failed = new HashSet<>();
  List<JobAndResultId> resultIds = new ArrayList<>();

  public void addSuccess(int seq) {
    this.succeed.add(seq);
  }

  public void addSuccess(int seq, long resultSeq) {
    this.succeed.add(seq);
    this.resultIds.add(new JobAndResultId(seq, resultSeq));
  }

  public void addFailed(int seq) {
    this.failed.add(seq);
  }

  @Getter
  class JobAndResultId {
    int jobId;
    long pointSeq;

    JobAndResultId(int jobId, long pointSeq) {
      this.jobId = jobId;
      this.pointSeq = pointSeq;
    }
  }
}
