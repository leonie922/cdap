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

import java.util.ArrayList;
import java.util.List;

/**
 * Contains list of messages for a topic
 */
public class TopicMessage {
  private final String topic;
  private final List<MonitorMessage> messages;

  public TopicMessage(String topic, List<MonitorMessage> messages) {
    this.topic = topic;
    this.messages = new ArrayList<>(messages);
  }

  public String getTopic() {
    return topic;
  }

  public List<MonitorMessage> getMessages() {
    return messages;
  }
}
