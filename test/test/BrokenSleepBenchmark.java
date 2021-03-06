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

package test;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

/**
 * Should fail with a measurement error.
 */
public class BrokenSleepBenchmark extends SimpleBenchmark {
  // And look, IDEA tries to warn you
  @SuppressWarnings({"UnusedDeclaration", "UnusedParameters"})
  public void timeSleepOneSecond(int reps) {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ignored) {
    }
  }

  public static void main(String[] args) throws Exception {
    Runner.main(BrokenSleepBenchmark.class, args);
  }
}
