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
import co.cask.cdap.proto.id.NamespaceId;
import co.cask.http.AbstractHttpHandler;
import co.cask.http.ChunkResponder;
import co.cask.http.HttpResponder;
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
import java.util.Map;
import javax.annotation.Nullable;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * {@link co.cask.http.HttpHandler} for exposing metadata of a runtime.
 */
@Path("/v1/runtime")
public class RuntimeHandler extends AbstractHttpHandler {
  private static final Logger LOG = LoggerFactory.getLogger(RuntimeHandler.class);
  private static final Gson GSON = new Gson();
  private static final Type MAP_STRING_CONSUME_REQUEST_TYPE = new TypeToken<Map<String,
    MonitorConsumeRequest>>() { }.getType();
  private static final int CHUNK_SIZE = 8192;

  private final CConfiguration cConf;
  private final MessageFetcher messageFetcher;
  private final Runnable shutdownRunnable;

  public RuntimeHandler(CConfiguration cConf, MessageFetcher messageFetcher, Runnable shutdownRunnable) {
    this.cConf = cConf;
    this.messageFetcher = messageFetcher;
    this.shutdownRunnable = shutdownRunnable;
  }

  /**
   * Gets list of topics along with offsets and limit as request and returns list of messages
   */
  @POST
  @Path("/metadata")
  public void metadata(FullHttpRequest request, HttpResponder responder) throws Exception {
    String requestBody = request.content().toString(StandardCharsets.UTF_8);

    Map<String, MonitorConsumeRequest> consumeRequests = GSON.fromJson(requestBody, MAP_STRING_CONSUME_REQUEST_TYPE);

    ChunkResponder chunkResponder = responder.sendChunkStart(
      HttpResponseStatus.OK, new DefaultHttpHeaders().set(HttpHeaderNames.CONTENT_TYPE,
                                                          "application/json; charset=utf-8"));

    ByteBuf buffer = Unpooled.buffer();
    JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(new ByteBufOutputStream(buffer),
                                                                  StandardCharsets.UTF_8));
    jsonWriter.beginObject();

    for (Map.Entry<String, MonitorConsumeRequest> entry : consumeRequests.entrySet()) {
      jsonWriter.name(entry.getKey());
      jsonWriter.beginArray();

      writeMessages(jsonWriter, buffer, chunkResponder, cConf.get(entry.getKey()), entry.getValue().getLimit(),
                    entry.getValue().getMessageId());

      jsonWriter.endArray();
    }

    jsonWriter.endObject();
    jsonWriter.close();

    if (buffer.isReadable()) {
      chunkResponder.sendChunk(buffer.copy());
    }

    Closeables.closeQuietly(chunkResponder);
  }

  @POST
  @Path("/shutdown")
  public void shutdown(FullHttpRequest request, HttpResponder responder) throws Exception {
    responder.sendString(HttpResponseStatus.OK, "Triggering shutdown down Runtime Http Server.");
    shutdownRunnable.run();
  }

  private void writeMessages(JsonWriter jsonWriter, ByteBuf buffer, ChunkResponder chunkResponder, String topic,
                             int limit, @Nullable String fromMessage) throws TopicNotFoundException, IOException {
    try (CloseableIterator<Message> iter = messageFetcher.fetch(NamespaceId.SYSTEM.getNamespace(), topic, limit,
                                                                fromMessage)) {
      while (iter.hasNext()) {
        Message message = iter.next();
        GSON.toJson(new MonitorMessage(message.getId(), message.getPayloadAsString(StandardCharsets.UTF_8)),
                    MonitorMessage.class, jsonWriter);
        if (buffer.readableBytes() >= CHUNK_SIZE) {
          chunkResponder.sendChunk(buffer.copy());
          buffer.clear();
        }
      }
    }
  }
}
