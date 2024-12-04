/**
 * Copyright 2024- Justin K. Belcher
 *
 * @author Justin K. Belcher <justin@jkb.studio>
 */

package jkbstudio;

import heronarts.lx.parameter.LXParameter;

import java.text.DecimalFormat;

public enum Format implements LXParameter.Formatter {
  DECIMAL_CLEAN {
    private static final DecimalFormat decimalFormat = new DecimalFormat("#0.##");

    @Override
    public String format(double value) {
      return decimalFormat.format(value);
    }
  };
}
