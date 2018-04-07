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
  private final Constraint constraint;
  private final Type joinType;
  private final List<String> outputs;

  /**
   * Create an instance of join operation.
   * @param name the name of the join operation
   * @param description the description of the join operation
   * @param constraint the constraint for the join operation
   * @param joinType the type of the join being performed
   * @param outputs the array of outputs created by the join operation. This array simply contains
   *                the outputs from both the entities on which join operation is being performed,
   *                since join operation itself does not change any output. So it is assumed that
   *                all outputs from the joined entities should have unique name.
   */
  public Join(String name, String description, Constraint constraint, Type joinType, String... outputs) {
    this(name, description, constraint, joinType, Arrays.asList(outputs));
  }

  /**
   * Create an instance of join operation.
   * @param name the name of the join operation
   * @param description the description of the join operation
   * @param constraint the constraint for the join operation
   * @param joinType the type of the join being performed
   * @param outputs the list of outputs created by the join operation. This array simply contains
   *                the outputs from both the entities on which join operation is being performed,
   *                since join operation itself does not change any output. So it is assumed that
   *                all outputs from the joined entities should have unique name.
   */
  public Join(String name, String description, Constraint constraint, Type joinType, List<String> outputs) {
    super(name, co.cask.cdap.api.lineage.operation.Type.JOIN, description);
    this.joinType = joinType;
    this.constraint = constraint;
    this.outputs = Collections.unmodifiableList(new ArrayList<>(outputs));
  }

  /**
   * @return join constraint
   */
  public Constraint getConstraint() {
    return constraint;
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

  /**
   * Class to represent the join constraint
   */
  public static class Constraint {
    List<Input> left;
    List<Input> right;

    /**
     * Create join constraint from the list of {@link Input}s participating in
     * the join condition from left and right side.
     * @param left list of inputs from the left side
     * @param right list of inputs from the right side
     */
    public Constraint(List<Input> left, List<Input> right) {
      this.left = Collections.unmodifiableList(new ArrayList<>(left));
      this.right = Collections.unmodifiableList(new ArrayList<>(right));
    }

    /**
     * @return the list of inputs participating in the join condition from the left side
     */
    public List<Input> getLeft() {
      return left;
    }

    /**
     * @return the list of inputs participating in the join condition from the right side
     */
    public List<Input> getRight() {
      return right;
    }
  }
}
