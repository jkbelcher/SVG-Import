/**
 * Copyright 2024- Justin K. Belcher
 *
 * @author Justin K. Belcher <justin@jkb.studio>
 */

package jkbstudio;

import heronarts.lx.parameter.LXParameter;

import java.text.DecimalFormat;

public class Format {
  private static final DecimalFormat decimalFormat = new DecimalFormat("#0.##");

  public static final LXParameter.Formatter DECIMAL_CLEAN = decimalFormat::format;
}
