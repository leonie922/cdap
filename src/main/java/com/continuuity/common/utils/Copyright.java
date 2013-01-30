/*
 * Copyright (c) 2012, Continuuity Inc. All rights reserved.
 */

package com.continuuity.common.utils;

import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;

public class Copyright {

  private static final String[] lines = {
      StringUtils.repeat("=", 80),
      "Continuuity AppFabric (tm) - Copyright 2012-2013 Continuuity, " +
          "Inc. All Rights Reserved.",
      ""
  };

  public static void print(PrintStream out) {
    for (String line : lines) out.println(line);
  }

  public static void print() {
    print(System.out);
  }
}
