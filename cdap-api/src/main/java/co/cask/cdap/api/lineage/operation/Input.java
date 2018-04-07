/*
 * Copyright © 2018 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.api.lineage.operation;

/**
 * Represent the input with name and the origin(operation) which created this input.
 */
public class Input {
  private final String origin;
  private final String name;

  private Input(String origin, String name) {
    this.origin = origin;
    this.name = name;
  }

  /**
   * @return the name of the operation which created this input
   */
  public String getOrigin() {
    return origin;
  }

  /**
   * @return the name of the input
   */
  public String getName() {
    return name;
  }

  /**
   * Get the instance of an input.
   * @param origin the name of the operation which created this input
   * @param name the associated with the input
   * @return the {@link Input}
   */
  public static Input of(String origin, String name) {
    return new Input(origin, name);
  }
}
