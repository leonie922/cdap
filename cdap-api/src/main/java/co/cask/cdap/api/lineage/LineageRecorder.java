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

package co.cask.cdap.api.lineage;

import co.cask.cdap.api.lineage.operation.Operation;
import co.cask.cdap.api.lineage.operation.Read;
import co.cask.cdap.api.lineage.operation.Write;

import java.util.Collection;

/**
 * Interface for recording the lineage information.
 */
public interface LineageRecorder {
  /**
   * Record the lineage operations.
   * @param operations the collection of operations to be recorded. All operations should have unique names.
   *                   For completeness of the linage information, this collection should have at least
   *                   one operation of type {@link Read} and one operation of type {@link Write}. 
   */
  void record(Collection<? extends Operation> operations);
}
