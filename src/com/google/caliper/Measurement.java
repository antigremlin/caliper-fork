/*
 * Copyright (C) 2010 Google Inc.
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a measurement of a single run of a benchmark.
 */
@SuppressWarnings("serial")
public final class Measurement
  implements Serializable /* for GWT */ {

  public static final Comparator<Measurement> SORT_BY_NANOS = new Comparator<Measurement>() {
    @Override public int compare(Measurement a, Measurement b) {
      double aNanos = a.getNanosPerRep();
      double bNanos = b.getNanosPerRep();
      return Double.compare(aNanos, bNanos);
    }
  };

  public static final Comparator<Measurement> SORT_BY_UNITS = new Comparator<Measurement>() {
    @Override public int compare(Measurement a, Measurement b) {
      double aNanos = a.getUnitsPerRep();
      double bNanos = b.getUnitsPerRep();
      return Double.compare(aNanos, bNanos);
    }
  };

  private /*final*/ double nanosPerRep;
  private /*final*/ double unitsPerRep;
  private /*final*/ Map<String, Integer> unitNames;

  public Measurement(Map<String, Integer> unitNames, double nanosPerRep, double unitsPerRep) {
    this.unitNames = new HashMap<String, Integer>(unitNames);
    this.nanosPerRep = nanosPerRep;
    this.unitsPerRep = unitsPerRep;
  }

  public Map<String, Integer> getUnitNames() {
    return new HashMap<String, Integer>(unitNames);
  }

  public double getNanosPerRep() {
    return nanosPerRep;
  }

  public double getUnitsPerRep() {
    return unitsPerRep;
  }

  private Measurement() {} /* for GWT */
}
