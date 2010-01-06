/**
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.caliper;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The complete result of a benchmark suite run.
 *
 * <p>Gwt-safe.
 */
public final class Run
    implements Serializable /* for GWT */ {

  private /*final*/ Map<Scenario, Double> measurements;
  private /*final*/ String benchmarkName;
  private /*final*/ String executedByUuid;
  private /*final*/ long executedTimestamp;

  // TODO: add more run properites such as checksums of the executed code

  public Run(Map<Scenario, Double> measurements,
      String benchmarkName, String executedByUuid, Date executedTimestamp) {
    if (benchmarkName == null || executedByUuid == null || executedTimestamp == null) {
      throw new NullPointerException();
    }

    this.measurements = new LinkedHashMap<Scenario, Double>(measurements);
    this.benchmarkName = benchmarkName;
    this.executedByUuid = executedByUuid;
    this.executedTimestamp = executedTimestamp.getTime();
  }

  public Map<Scenario, Double> getMeasurements() {
    return measurements;
  }

  public String getBenchmarkName() {
    return benchmarkName;
  }

  public String getExecutedByUuid() {
    return executedByUuid;
  }

  public Date getExecutedTimestamp() {
    return new Date(executedTimestamp);
  }

  @Override public boolean equals(Object o) {
    if (o instanceof Run) {
      Run that = (Run) o;
      return measurements.equals(that.measurements)
          && benchmarkName.equals(that.benchmarkName)
          && executedByUuid.equals(that.executedByUuid)
          && executedTimestamp == that.executedTimestamp;
    }

    return false;
  }

  @Override public int hashCode() {
    int result = measurements.hashCode();
    result = result * 37 + benchmarkName.hashCode();
    result = result * 37 + executedByUuid.hashCode();
    result = result * 37 + (int) ((executedTimestamp >> 32) ^ executedTimestamp);
    return result;
  }

  @Override public String toString() {
    return measurements.toString();
  }

  private Run() {} // for GWT
}
