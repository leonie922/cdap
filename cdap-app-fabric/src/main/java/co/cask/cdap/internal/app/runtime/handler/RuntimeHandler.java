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
import co.cask.cdap.api.messaging.TopicNotFoundException;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.internal.app.runtime.monitor.MonitorConsumeRequest;
import co.cask.cdap.internal.app.runtime.monitor.MonitorMessage;
import co.cask.cdap.internal.app.runtime.monitor.TopicMessage;
import co.cask.cdap.proto.id.NamespaceId;
import co.cask.http.AbstractHttpHandler;
import co.cask.http.ChunkResponder;
import co.cask.http.HttpResponder;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * {@link co.cask.http.HttpHandler} for exposing metadata of a runtime.
 */
@Path("v1/runtime")
public class RuntimeHandler extends AbstractHttpHandler {
  private static final Logger LOG = LoggerFactory.getLogger(RuntimeHandler.class);
  private static final Gson GSON = new Gson();
  private static final int MAX_MESSAGES_PER_READ = 100;
  private static final int CHUNK_SIZE = 8192;
  private static final Type TOKEN_TYPE_SET_STRING = new TypeToken<Set<String>>() { }.getType();
  private static final Type MAP_STRING_CONSUME_REQUEST_TYPE = new TypeToken<Map<String,
    MonitorConsumeRequest>>() { }.getType();

  private final CConfiguration cConf;
  private final MessageFetcher messageFetcher;

  public RuntimeHandler(CConfiguration cConf, MessageFetcher messageFetcher) {
    this.cConf = cConf;
    this.messageFetcher = messageFetcher;
  }

  /**
   * Gets list of topics to be monitored from client and responds with actual corresponding names using which runtime
   * has been initialized.
   */
  @POST
  @Path("/monitor/topics")
  public void topics(FullHttpRequest request, HttpResponder responder) throws Exception {
    String data = request.content().toString(StandardCharsets.UTF_8);

    Set<String> topics = GSON.fromJson(data, TOKEN_TYPE_SET_STRING);
    Map<String, String> returnMap = new HashMap<>();

    for (String topic : topics) {
      returnMap.put(topic, cConf.get(topic));
    }

    responder.sendJson(HttpResponseStatus.OK, GSON.toJson(returnMap));
  }

  /**
   * Gets topics along with offsets and limit in the request and returns all the messages excluding the last message
   * read.
   */
  @POST
  @Path("/metadata")
  public void metadata(FullHttpRequest request, HttpResponder responder) throws Exception {
    String data = request.content().toString(StandardCharsets.UTF_8);

    Map<String, MonitorConsumeRequest> topicsToFetch = GSON.fromJson(data, MAP_STRING_CONSUME_REQUEST_TYPE);

    ChunkResponder chunkResponder = responder.sendChunkStart(
      HttpResponseStatus.OK, new DefaultHttpHeaders().set(HttpHeaderNames.CONTENT_TYPE,
                                                          "application/json; charset=utf-8"));

    ByteBuf buffer = Unpooled.buffer();
    JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(new ByteBufOutputStream(buffer),
                                                                  StandardCharsets.UTF_8));
    jsonWriter.name("messages");
    jsonWriter.beginArray();

    for (Map.Entry<String, MonitorConsumeRequest> entry : topicsToFetch.entrySet()) {
      String lastMessageId = null;
      int limit = entry.getValue().getLimit();
      int batchSize = Math.min(MAX_MESSAGES_PER_READ, limit);
      List<MonitorMessage> messages = Lists.newArrayListWithCapacity(MAX_MESSAGES_PER_READ);

      // read first batch of messages
      int eventsRead = readMessages(messages, cConf.get(entry.getKey()), batchSize, lastMessageId);

      while (limit > 0 && eventsRead > 0) {
        GSON.toJson(new TopicMessage(entry.getKey(), messages), TopicMessage.class, jsonWriter);
        jsonWriter.flush();

        if (buffer.readableBytes() >= CHUNK_SIZE) {
          chunkResponder.sendChunk(buffer.copy());
          buffer.clear();
        }

        limit -= eventsRead;
        batchSize = Math.min(MAX_MESSAGES_PER_READ, limit);

        if (limit > 0) {
          lastMessageId = messages.get(eventsRead - 1).getMessageId();
          messages.clear();
          eventsRead = readMessages(messages, cConf.get(entry.getKey()), batchSize, lastMessageId);
        }
      }

      if (buffer.isReadable()) {
        chunkResponder.sendChunk(buffer.copy());
        buffer.clear();
      }
    }

    jsonWriter.endArray();
    jsonWriter.close();

    Closeables.closeQuietly(chunkResponder);
  }

  private int readMessages(List<MonitorMessage> messages, String topic, int limit, @Nullable String fromMessage)
    throws TopicNotFoundException, IOException {
    int count = 0;
    try (CloseableIterator<Message> iter = messageFetcher.fetch(NamespaceId.SYSTEM.getNamespace(), topic, limit,
                                                                fromMessage)) {
      while (iter.hasNext()) {
        Message message = iter.next();
        messages.add(new MonitorMessage(message.getId(), message.getPayloadAsString(StandardCharsets.UTF_8)));
        count++;
      }
    }

    return count;
  }
}
