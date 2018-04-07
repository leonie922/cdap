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

package co.cask.cdap.data2.metadata.writer;

import co.cask.cdap.common.conf.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * A container for messages in the metadata topic configured by {@link Constants.Metadata#MESSAGING_TOPIC}.
 * It carries the message type and the payload as {@link JsonElement}.
 */
public final class MetadataMessage {

  /**
   * The message type.
   */
  public enum Type {
    LINEAGE,
    USAGE
  }

  private final Type type;
  private final JsonElement payload;

  public MetadataMessage(Type type, JsonElement payload) {
    this.type = type;
    this.payload = payload;
  }

  /**
   * Returns the type of the message.
   */
  public Type getType() {
    return type;
  }

  /**
   * Returns the payload by decoding the json to the given type.
   *
   * @param gson the {@link Gson} for decoding the json element
   * @param objType the resulting object type
   * @param <T> the resulting object type
   * @return the decode object
   */
  public <T> T getPayload(Gson gson, java.lang.reflect.Type objType) {
    return gson.fromJson(payload, objType);
  }

  /**
   * Returns the payload as the raw {@link JsonElement}.
   */
  public JsonElement getRawPayload() {
    return payload;
  }
}
