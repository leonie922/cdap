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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents join operation.
 */
public class Join extends Operation {
  private final List<Input> constraints;
  private final Type joinType;
  private final List<String> outputs;

  /**
   * Create an instance of join operation.
   * @param name the name of the join operation
   * @param description the description of the join operation
   * @param constraints the list of inputs participate in the join condition
   * @param joinType the type of the join being performed
   * @param outputs the array of outputs created by the join operation
   */
  public Join(String name, String description, List<Input> constraints, Type joinType, String... outputs) {
    this(name, description, constraints, joinType, Arrays.asList(outputs));
  }

  /**
   * Create an instance of join operation.
   * @param name the name of the join operation
   * @param description the description of the join operation
   * @param constraints the list of inputs participate in the join condition
   * @param joinType the type of the join being performed
   * @param outputs the list of outputs created by the join operation
   */
  public Join(String name, String description, List<Input> constraints, Type joinType, List<String> outputs) {
    super(name, co.cask.cdap.api.lineage.operation.Type.JOIN, description);
    this.joinType = joinType;
    this.constraints = Collections.unmodifiableList(new ArrayList<>(constraints));
    this.outputs = Collections.unmodifiableList(new ArrayList<>(outputs));
  }

  /**
   * @return the list of inputs participate in the join condition
   */
  public List<Input> getConstraints() {
    return constraints;
  }

  /**
   * @return the type of join being performed
   */
  public Type getJoinType() {
    return joinType;
  }

  /**
   * @return list of outputs created by the join operation
   */
  public List<String> getOutputs() {
    return outputs;
  }

  /**
   * Enum to hold different join types
   */
  public enum Type {
    INNER,
    OUTER,
    LEFT,
    RIGHT
  }
}
