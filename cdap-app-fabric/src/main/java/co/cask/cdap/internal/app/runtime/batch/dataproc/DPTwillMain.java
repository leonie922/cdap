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

package co.cask.cdap.internal.app.runtime.batch.dataproc;

import com.jcraft.jsch.JSchException;
import org.apache.twill.api.ClassAcceptor;
import org.apache.twill.api.TwillController;
import org.apache.twill.api.TwillPreparer;
import org.apache.twill.api.logging.PrinterLogHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

/**
 *
 */
public class DPTwillMain {

  static class HadoopClassExcluder extends ClassAcceptor {
    @Override
    public boolean accept(String className, URL classUrl, URL classPathUrl) {
      // exclude hadoop but not hbase package
      return !(className.startsWith("org.apache.hadoop") && !className.startsWith("org.apache.hadoop.hbase"));
    }
  }

  private static String getPrivateKey() {
    return "-----BEGIN RSA PRIVATE KEY-----\n"
      + "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\n"
      + "-----END RSA PRIVATE KEY-----";
  }


  public static void main(String[] args) {
    SSHConfig sshConfig = new SSHConfig("35.200.155.105", "yourname", getPrivateKey());

    String tempDir = "/tmp/dptwill/" + System.currentTimeMillis();
//    System.out.println(SSHUtils.runCommand(sshConfig, "mkdir -p " + tempDir));
//    System.out.println(SSHUtils.runCommand(sshConfig, "ls -ltrh " + tempDir));

    DataProcTwillRunner twillRunner = new DataProcTwillRunner(sshConfig);

    TwillPreparer twillPreparer = twillRunner.prepare(new EchoServer());
    twillPreparer.addLogHandler(new PrinterLogHandler(new PrintWriter(System.out, true)));
    twillPreparer.withBundlerClassAcceptor(new HadoopClassExcluder());
    TwillController controller = twillPreparer.start();
  }
}
