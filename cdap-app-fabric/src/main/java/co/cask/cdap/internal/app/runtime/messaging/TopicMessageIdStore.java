/*
 * Copyright © 2017 Cask Data, Inc.
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

package co.cask.cdap.internal.app.runtime.messaging;

import javax.annotation.Nullable;

/**
 * Defines the interface for retrieving and persisting a TMS topic name and associated
 * message id pairs. This interface is used to fetch the last read message id for a given topic so that a
 * subscriber only begins fetching notifications past that message id.
 */
public interface TopicMessageIdStore {
  /**
   * Gets the id of the last fetched message that was set for a subscriber of the given TMS topic
   *
   * @param topic the topic to lookup the last message id
   * @param subscriber the subscriber name
   * @return the id of the last fetched message for this subscriber on this topic,
   *         or {@code null} if no message id was stored before
   */
  @Nullable
  String retrieveSubscriberState(String topic, String subscriber);

  /**
   * Updates the given topic's last fetched message id with the given message id for the given subscriber.
   *
   * @param topic the topic to persist the message id
   * @param subscriber the subscriber name
   * @param messageId the most recently processed message id
   */
  void persistSubscriberState(String topic, String subscriber, String messageId);
}
