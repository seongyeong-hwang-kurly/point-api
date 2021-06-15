package com.kurly.cloud.point.api.point.util;

public class VersionUtil {
  public static final String V1 = "v1";
  public static final String V2 = "v2";
  public static final String VERSION_PATTERN = V1 + "|" + V2;
  /**
   * V2는 Transactional Timeout 이 적용된다.
   */
  public static final int V2_TIMEOUT_SECONDS = 5;
}
