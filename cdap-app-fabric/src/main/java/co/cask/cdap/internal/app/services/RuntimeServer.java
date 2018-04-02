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

package co.cask.cdap.internal.app.services;

import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.http.CommonNettyHttpServiceBuilder;
import co.cask.cdap.common.logging.LoggingContextAccessor;
import co.cask.cdap.common.logging.ServiceLoggingContext;
import co.cask.cdap.internal.app.runtime.monitor.RuntimeMonitor;
import co.cask.cdap.proto.id.NamespaceId;
import co.cask.http.HttpHandler;
import co.cask.http.NettyHttpService;
import com.google.common.util.concurrent.AbstractIdleService;
import org.apache.twill.common.Cancellable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Set;

/**
 * Runtime Server which starts netty-http service to expose metadata to {@link RuntimeMonitor}
 */
public class RuntimeServer extends AbstractIdleService {

  private static final Logger LOG = LoggerFactory.getLogger(RuntimeServer.class);

  private final InetAddress hostname;
  private final int port;
  private final CConfiguration cConf;
  private Cancellable cancelHttpService;
  private Set<HttpHandler> handlers;

  public RuntimeServer(CConfiguration cConf, InetAddress hostname, int port, Set<HttpHandler> handlers) {
    this.hostname = hostname;
    this.port = port;
    this.handlers = handlers;
    this.cConf = cConf;
  }

  @Override
  protected void startUp() throws Exception {
    LoggingContextAccessor.setLoggingContext(new ServiceLoggingContext(NamespaceId.SYSTEM.getNamespace(),
                                                                       Constants.Logging.COMPONENT_NAME,
                                                                       Constants.Service.RUNTIME_HTTP));
    NettyHttpService.Builder httpServiceBuilder = new CommonNettyHttpServiceBuilder(cConf,
                                                                                    Constants.Service.RUNTIME_HTTP)
      .setHost(hostname.getCanonicalHostName())
      .setHttpHandlers(handlers);
    httpServiceBuilder.setPort(cConf.getInt(Constants.RuntimeHandler.SERVER_PORT));

    cancelHttpService = startHttpService(httpServiceBuilder.build());
  }

  @Override
  protected void shutDown() throws Exception {
    cancelHttpService.cancel();
  }

  private Cancellable startHttpService(final NettyHttpService httpService) throws Exception {
    httpService.start();

    return new Cancellable() {
      @Override
      public void cancel() {
        LOG.debug("Stopping Runtime HTTP service.");

        try {
          httpService.stop();
        } catch (Exception e) {
          LOG.warn("Exception raised when stopping Runtime HTTP service", e);
        }

        LOG.info("Runtime HTTP service stopped.");
      }
    };
  }
}
