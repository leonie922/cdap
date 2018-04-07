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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents merge between multiple origins(operations). Outputs of merge operation
 * are implicit and is union of outputs of all the origins that are being merged.
 */
public class Merge extends Operation {
  private final Set<String> origins;

  /**
   * Create instance of merge operation
   * @param name the name of the merge operation
   * @param description the description of the merge operation
   * @param origins the array of name of operations to be merge
   */
  public Merge(String name, String description, String ... origins) {
    this(name, description, new HashSet<>(Arrays.asList(origins)));
  }

  /**
   * Create instance of merge operation
   * @param name the name of the merge operation
   * @param description the description of the merge operation
   * @param origins the set of name of operations to be merge
   */
  public Merge(String name, String description, Set<String> origins) {
    super(name, Type.MERGE, description);
    this.origins = Collections.unmodifiableSet(new HashSet<>(origins));
  }

  /**
   * @return the set of name of operations to be merged
   */
  public Set<String> getOrigins() {
    return origins;
  }
}
