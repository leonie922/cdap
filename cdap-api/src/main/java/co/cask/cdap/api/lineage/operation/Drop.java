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
 * Represents the drop operation on the given list of inputs.
 */
public class Drop extends Operation {
  private final List<Input> inputs;

  /**
   * Create an instance of drop operation.
   * @param name the name of the operation
   * @param description the description associated with the operation
   * @param inputs the array of inputs to drop
   */
  public Drop(String name, String description, Input ... inputs) {
    this(name, description, Arrays.asList(inputs));
  }

  /**
   * Create an instance of drop operation.
   * @param name the name of the operation
   * @param description the description associated with the operation
   * @param inputs the list of inputs to drop
   */
  public Drop(String name, String description, List<Input> inputs) {
    super(name, Type.DROP, description);
    this.inputs = Collections.unmodifiableList(new ArrayList<>(inputs));
  }

  /**
   * @return the list of inputs being dropped
   */
  public List<Input> getInputs() {
    return inputs;
  }
}
