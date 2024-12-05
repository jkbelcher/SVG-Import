/**
 * Copyright 2024- Justin K. Belcher
 *
 * @author Justin K. Belcher <justin@jkb.studio>
 */

package jkbstudio.svg;

import heronarts.lx.LX;
import heronarts.lx.LXPlugin;
import heronarts.lx.studio.LXStudio;
import jkbstudio.ui.svg.UISvgImport;

/**
 * Chromatik plugin that adds an SVG Import component and associated UI element.
 *
 * @author Justin Belcher <justin@jkb.studio>
 */
@LXPlugin.Name("SVG Import")
public class SvgImportPlugin implements LXStudio.Plugin {

  // This string must be manually updated to match the pom.xml version
  public static final String VERSION = "0.1.1-SNAPSHOT";
  private SvgImport svgImport;

  public SvgImportPlugin(LX lx) {
    LX.log("SVG Import Plugin, Version: " + VERSION);
  }

  @Override
  public void initialize(LX lx) {
    lx.engine.registerComponent("svgImport", this.svgImport = new SvgImport((lx)));
  }

  @Override
  public void initializeUI(LXStudio lxStudio, LXStudio.UI ui) { }

  @Override
  public void onUIReady(LXStudio lxStudio, LXStudio.UI ui) {
    new UISvgImport(lxStudio, ui, this.svgImport, ui.leftPane.model.getContentWidth())
      .addToContainer(ui.leftPane.model, 1);
      // .addBeforeSibling(ui.leftPane.fixtures);  // Can't see fixtures manager
  }

  @Override
  public void dispose() {
    this.svgImport.dispose();
  }
}
