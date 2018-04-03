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
 * Abstract base class to represent Operation.
 */
public abstract class Operation {
  private final String name;
  private final Type type;
  private final String description;

  protected Operation(String name, Type type, String description) {
    this.name = name;
    this.type = type;
    this.description = description;
  }

  /**
   * @return the name of the operation for example, "read" or "concatenate".
   */
  public String getName() {
    return name;
  }

  /**
   * @return the type of the operation
   */
  public Type getType() {
    return type;
  }

  /**
   * @return the description associated with the operation
   */
  public String getDescription() {
    return description;
  }
}
