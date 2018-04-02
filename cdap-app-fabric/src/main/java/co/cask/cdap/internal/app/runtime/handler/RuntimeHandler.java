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

package co.cask.cdap.internal.app.runtime.handler;

import co.cask.cdap.api.dataset.lib.CloseableIterator;
import co.cask.cdap.api.messaging.Message;
import co.cask.cdap.api.messaging.MessageFetcher;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.internal.app.runtime.monitor.MonitorConsumeRequest;
import co.cask.cdap.internal.app.runtime.monitor.MonitorMessage;
import co.cask.cdap.proto.id.NamespaceId;
import co.cask.http.AbstractHttpHandler;
import co.cask.http.HttpResponder;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Singleton;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * {@link co.cask.http.HttpHandler} for exposing metadata of a runtime.
 */
@Singleton
@Path(Constants.Gateway.API_VERSION_3 + "/runtime")
public class RuntimeHandler extends AbstractHttpHandler {
  private static final Logger LOG = LoggerFactory.getLogger(RuntimeHandler.class);
  private static final Gson GSON = new Gson();
  private static final Type TOKEN_TYPE_SET_STRING = new TypeToken<Set<String>>() {
  }.getType();
  private static final Type MAP_STRING_CONSUME_REQUEST_TYPE = new com.google.common.reflect.TypeToken<Map<String,
    MonitorConsumeRequest>>() {
  }.getType();
  private static final JsonParser JSON_PARSER = new JsonParser();

  private final CConfiguration cConf;
  private final MessageFetcher messageFetcher;

  public RuntimeHandler(CConfiguration cConf, MessageFetcher messageFetcher) {
    this.cConf = cConf;
    this.messageFetcher = messageFetcher;
  }

  /**
   * Returns map of topic config -> topic name to be monitored
   */
  @POST
  @Path("/monitor/topics")
  public void topics(FullHttpRequest request, HttpResponder responder) throws Exception {
    try {
      String data = request.content().toString(StandardCharsets.UTF_8);

      if (!isValidJSON(data)) {
        responder.sendJson(HttpResponseStatus.BAD_REQUEST, "Invalid JSON in body");
        return;
      }

      Set<String> topics = GSON.fromJson(data, TOKEN_TYPE_SET_STRING);
      Map<String, String> returnMap = new HashMap<>();

      for (String topic : topics) {
        returnMap.put(topic, cConf.get(topic));
      }

      responder.sendJson(HttpResponseStatus.OK, GSON.toJson(returnMap));
    } catch (Exception ex) {
      responder.sendString(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Error: " + ex.getMessage());
    }
  }

  /**
   * Returns runtime related metadata.
   */
  @POST
  @Path("/metadata")
  public void metadata(FullHttpRequest request, HttpResponder responder) throws Exception {
    try {
      String data = request.content().toString(StandardCharsets.UTF_8);

      if (!isValidJSON(data)) {
        responder.sendJson(HttpResponseStatus.BAD_REQUEST, "Invalid JSON in body");
        return;
      }

      Map<String, MonitorConsumeRequest> topicsToFetch = GSON.fromJson(data, MAP_STRING_CONSUME_REQUEST_TYPE);
      Map<String, List<MonitorMessage>> returnMap = new HashMap<>();

      for (Map.Entry<String, MonitorConsumeRequest> topicToFetch : topicsToFetch.entrySet()) {
        List<MonitorMessage> list = new LinkedList<>();
        try (CloseableIterator<Message> iter = messageFetcher.fetch(NamespaceId.SYSTEM.getNamespace(),
                                                                    cConf.get(topicToFetch.getKey()),
                                                                    topicToFetch.getValue().getLimit(),
                                                                    topicToFetch.getValue().getMessageId())) {
          while (iter.hasNext()) {
            Message message = iter.next();
            list.add(new MonitorMessage(message.getId(), message.getPayloadAsString(StandardCharsets.UTF_8)));
          }
        }

        returnMap.put(topicToFetch.getKey(), list);
      }

      responder.sendJson(HttpResponseStatus.OK, GSON.toJson(returnMap));
    } catch (Exception ex) {
      responder.sendString(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Error: " + ex.getMessage());
    }
  }

  private boolean isValidJSON(String json) {
    try {
      JSON_PARSER.parse(json);
    } catch (JsonSyntaxException ex) {
      return false;
    }
    return true;
  }
}
