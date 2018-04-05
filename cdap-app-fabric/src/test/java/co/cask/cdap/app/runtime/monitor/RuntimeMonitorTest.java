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

package co.cask.cdap.app.runtime.monitor;

import co.cask.cdap.api.data.schema.UnsupportedTypeException;
import co.cask.cdap.api.dataset.lib.CloseableIterator;
import co.cask.cdap.api.messaging.Message;
import co.cask.cdap.api.messaging.MessageFetcher;
import co.cask.cdap.api.messaging.MessagePublisher;
import co.cask.cdap.api.messaging.MessagingContext;
import co.cask.cdap.api.messaging.TopicNotFoundException;
import co.cask.cdap.client.config.ClientConfig;
import co.cask.cdap.client.config.ConnectionConfig;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.utils.Tasks;
import co.cask.cdap.internal.app.runtime.monitor.RuntimeMonitor;
import co.cask.cdap.internal.app.services.RuntimeServer;
import co.cask.cdap.internal.guice.AppFabricTestModule;
import co.cask.cdap.messaging.MessagingService;
import co.cask.cdap.messaging.context.MultiThreadMessagingContext;
import co.cask.cdap.proto.ProgramType;
import co.cask.cdap.proto.id.NamespaceId;
import co.cask.cdap.proto.id.ProgramRunId;
import com.google.common.util.concurrent.Service;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Runtime Monitor Test
 */
public class RuntimeMonitorTest {
  private static final Logger LOG = LoggerFactory.getLogger(RuntimeMonitorTest.class);

  protected static Injector injector;
  protected static CConfiguration cConf;
  protected static MessagingService messagingService;
  private InetAddress address;
  private RuntimeServer runtimeServer;
  private MessagingContext messagingContext;

  @Before
  public void init() throws IOException, UnsupportedTypeException {
    cConf = CConfiguration.create();
    cConf.set(Constants.AppFabric.OUTPUT_DIR, System.getProperty("java.io.tmpdir"));
    cConf.set(Constants.AppFabric.TEMP_DIR, System.getProperty("java.io.tmpdir"));
    cConf.set(Constants.AppFabric.PROGRAM_STATUS_RECORD_EVENT_TOPIC, "program-status");
    cConf.set(Constants.RuntimeHandler.SERVER_PORT, "1234");
    cConf.set(Constants.RuntimeMonitor.BATCH_LIMIT, "2");
    cConf.set(Constants.RuntimeMonitor.POLL_TIME_MS, "200");
    cConf.set(Constants.RuntimeMonitor.TOPICS_CONFIGS, Constants.AppFabric.PROGRAM_STATUS_RECORD_EVENT_TOPIC);
    injector = Guice.createInjector(new AppFabricTestModule(cConf));
    messagingService = injector.getInstance(MessagingService.class);
    if (messagingService instanceof Service) {
      ((Service) messagingService).startAndWait();
    }
    address = InetAddress.getLoopbackAddress();
    messagingContext = new MultiThreadMessagingContext(messagingService);
    runtimeServer = new RuntimeServer(cConf, address, messagingContext.getMessageFetcher());
    runtimeServer.startAndWait();
  }

  @After
  public void stop() throws Exception {
    if (messagingService instanceof Service) {
      ((Service) messagingService).stopAndWait();
    }
    runtimeServer.stopAndWait();
  }

  @Test
  public void testRunTimeMonitor() throws Exception {
    publishProgramMessages();

    ConnectionConfig connectionConfig = ConnectionConfig.builder()
      .setHostname(address.getHostAddress())
      .setPort(1234)
      .setSSLEnabled(false)
      .build();
    ClientConfig.Builder clientConfigBuilder = ClientConfig.builder()
      .setDefaultReadTimeout(20000)
      .setApiVersion("v1")
      .setConnectionConfig(connectionConfig);

    // change topic name because cdap config is different than runtime config
    cConf.set(Constants.AppFabric.PROGRAM_STATUS_RECORD_EVENT_TOPIC, "cdap-program-status");

    RuntimeMonitor runtimeMonitor = new RuntimeMonitor(new ProgramRunId("test", "app1", ProgramType.WORKFLOW, "p1",
                                                                        "run1"),
                                                       CConfiguration.copy(cConf),
                                                       messagingContext.getMessagePublisher(),
                                                       clientConfigBuilder.build());
    runtimeMonitor.startAndWait();

    // reset
    cConf.set(Constants.AppFabric.PROGRAM_STATUS_RECORD_EVENT_TOPIC, "program-status");


    HashSet<String> expected = new LinkedHashSet<>();
    expected.add("message1");
    expected.add("message2");
    expected.add("message3");
    expected.add("message4");
    expected.add("message5");
    expected.add("message6");
    expected.add("message7");
    expected.add("message8");
    expected.add("message9");
    expected.add("message10");

    HashSet<String> actual = new LinkedHashSet<>();
    final String[] messageId = {null};

    MessageFetcher fetcher = messagingContext.getMessageFetcher();

    Tasks.waitFor(
      true,
      new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {

          try (CloseableIterator<Message> iter =
                 fetcher.fetch(NamespaceId.SYSTEM.getNamespace(),
                               cConf.get(Constants.AppFabric.PROGRAM_STATUS_RECORD_EVENT_TOPIC), 2, messageId[0])) {
            while (iter.hasNext()) {
              Message message = iter.next();
              messageId[0] = message.getId();
              actual.add(message.getPayloadAsString());
            }
          }

          return expected.size() == actual.size() && expected.equals(actual);
        }
      }, 5, TimeUnit.MINUTES);

    runtimeMonitor.stopAndWait();
  }

  private void publishProgramMessages() throws TopicNotFoundException, IOException {
    MessagePublisher messagePublisher = messagingContext.getMessagePublisher();
    messagePublisher.publish(NamespaceId.SYSTEM.getNamespace(),
                             cConf.get(Constants.AppFabric.PROGRAM_STATUS_RECORD_EVENT_TOPIC), "message1");
    messagePublisher.publish(NamespaceId.SYSTEM.getNamespace(),
                             cConf.get(Constants.AppFabric.PROGRAM_STATUS_RECORD_EVENT_TOPIC), "message2");
    messagePublisher.publish(NamespaceId.SYSTEM.getNamespace(),
                             cConf.get(Constants.AppFabric.PROGRAM_STATUS_RECORD_EVENT_TOPIC), "message3");
    messagePublisher.publish(NamespaceId.SYSTEM.getNamespace(),
                             cConf.get(Constants.AppFabric.PROGRAM_STATUS_RECORD_EVENT_TOPIC), "message4");
    messagePublisher.publish(NamespaceId.SYSTEM.getNamespace(),
                             cConf.get(Constants.AppFabric.PROGRAM_STATUS_RECORD_EVENT_TOPIC), "message5");
    messagePublisher.publish(NamespaceId.SYSTEM.getNamespace(),
                             cConf.get(Constants.AppFabric.PROGRAM_STATUS_RECORD_EVENT_TOPIC), "message6");
    messagePublisher.publish(NamespaceId.SYSTEM.getNamespace(),
                             cConf.get(Constants.AppFabric.PROGRAM_STATUS_RECORD_EVENT_TOPIC), "message7");
    messagePublisher.publish(NamespaceId.SYSTEM.getNamespace(),
                             cConf.get(Constants.AppFabric.PROGRAM_STATUS_RECORD_EVENT_TOPIC), "message8");
    messagePublisher.publish(NamespaceId.SYSTEM.getNamespace(),
                             cConf.get(Constants.AppFabric.PROGRAM_STATUS_RECORD_EVENT_TOPIC), "message9");
    messagePublisher.publish(NamespaceId.SYSTEM.getNamespace(),
                             cConf.get(Constants.AppFabric.PROGRAM_STATUS_RECORD_EVENT_TOPIC), "message10");
  }
}
