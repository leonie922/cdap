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
 * Represents a write operation from a collection of inputs into a data sink.
 */
public class Write extends Operation {
  private final List<Input> inputs;
  private final EndPoint sink;

  /***
   * Create an instance of write operation.
   * @param name the name of the operation
   * @param description the description associated with the operation
   * @param sink the sink for the operation
   * @param inputs the array of inputs to be written
   */
  public Write(String name, String description, EndPoint sink, Input ... inputs) {
    this(name, description, sink, Arrays.asList(inputs));
  }

  /**
   * Create an instance of write operation.
   * @param name the name of the operation
   * @param description the description associated with the operation
   * @param sink the sink for the operation
   * @param inputs the list of inputs to be written
   */
  public Write(String name, String description, EndPoint sink, List<Input> inputs) {
    super(name, Type.WRITE, description);
    this.sink = sink;
    this.inputs = Collections.unmodifiableList(new ArrayList<>(inputs));
  }

  /**
   * @return the sink where this operation writes
   */
  public EndPoint getSink() {
    return sink;
  }

  /**
   * Get the list of inputs consumed by this write operation.
   * @return the list of inputs
   */
  public List<Input> getInputs() {
    return inputs;
  }
}
