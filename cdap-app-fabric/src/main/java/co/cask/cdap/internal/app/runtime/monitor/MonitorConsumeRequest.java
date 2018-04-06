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

package co.cask.cdap.internal.app.runtime.monitor;

import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Runtime Monitor consume request
 */
public class MonitorConsumeRequest {
  @Nullable
  private final String messageId;
  private final int limit;

  public MonitorConsumeRequest(@Nullable String messageId, int limit) {
    this.messageId = messageId;
    this.limit = limit;
  }

  @Nullable
  public String getMessageId() {
    return messageId;
  }

  public int getLimit() {
    return limit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MonitorConsumeRequest that = (MonitorConsumeRequest) o;

    return limit == that.limit && Objects.equals(messageId, that.messageId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(messageId, limit);
  }
}
