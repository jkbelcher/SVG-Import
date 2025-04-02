/**
 * Copyright 2024- Justin K. Belcher
 *
 * @author Justin K. Belcher <justin@jkb.studio>
 */

package studio.jkb.svg;

import heronarts.glx.ui.UI;
import heronarts.glx.ui.UI2dComponent;
import heronarts.glx.ui.UI2dContainer;
import heronarts.glx.ui.component.UIButton;
import heronarts.glx.ui.component.UILabel;
import heronarts.glx.ui.vg.VGraphics;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.ui.device.UIControls;
import heronarts.lx.studio.ui.fixture.UIFixture;
import heronarts.lx.studio.ui.fixture.UIFixtureControls;
import studio.jkb.svg.PathFixture.PointMode;

import java.io.File;

public class UIPathFixture implements UIFixtureControls<PathFixture>, UIControls {

  private static final int SECTION_SPACING = 6;
  private static final int ROW_HEIGHT = 16;

  private float controlWidth;

  private UILabel pointCount;
  private UI2dComponent numPointsRow;
  private UI2dComponent spacingRow;
  private UI2dComponent densityRow;
  private UI2dComponent padEndRow;

  @Override
  public void buildFixtureControls(LXStudio.UI ui, UIFixture uiFixture, PathFixture fixture) {
    uiFixture.setChildSpacing(4);
    controlWidth = (uiFixture.getContentWidth() / 2) - 4;

    uiFixture.addTagSection();
    uiFixture.addGeometrySection();

    UIFixture.Section sectUnits = uiFixture.addSection("Units");
    sectUnits.setTopMargin(SECTION_SPACING);
    sectUnits.addChildren(
      newRow(fixture.pathUnits, newDropMenu(fixture.pathUnits, controlWidth)),
      newRow(fixture.modelUnits, newDropMenu(fixture.modelUnits, controlWidth))
    );

    UIFixture.Section sectPoints = uiFixture.addSection("Points");
    sectPoints.setTopMargin(SECTION_SPACING);
    sectPoints.addChildren(
      newRow(fixture.pointMode, uiFixture.newControlDropMenu(fixture.pointMode, controlWidth)),
      this.numPointsRow = newRow(fixture.numPoints, uiFixture.newControlIntBox(fixture.numPoints, controlWidth)),
      this.spacingRow =
        UI2dContainer.newHorizontalContainer(ROW_HEIGHT, 2,
          new UILabel(70, ROW_HEIGHT, fixture.spacing.getLabel())
            .setFont(UI.get().theme.getControlFont())
            .setTextAlignment(VGraphics.Align.LEFT, VGraphics.Align.MIDDLE),
          uiFixture.newControlBox(fixture.spacing, 40),
          newDropMenu(fixture.spacingUnits, 53)
            .setMenuWidth(80)
        ),
      this.densityRow =
        UI2dContainer.newHorizontalContainer(ROW_HEIGHT, 2,
          new UILabel(50, ROW_HEIGHT, "Density")
            .setFont(UI.get().theme.getControlFont())
            .setTextAlignment(VGraphics.Align.LEFT, VGraphics.Align.MIDDLE),
          uiFixture.newControlBox(fixture.density, 40),
          new UILabel(20, ROW_HEIGHT, "per")
            .setFont(UI.get().theme.getControlFont())
            .setTextAlignment(VGraphics.Align.CENTER, VGraphics.Align.MIDDLE),
          newDropMenu(fixture.densityUnits, 50)
            .setMenuWidth(80)
        ),
      newRow(fixture.reversePath, uiFixture.newControlButton(fixture.reversePath, controlWidth)),
      this.pointCount = (UILabel) new UILabel(sectPoints.getContentWidth(), ROW_HEIGHT, "")
        .setFont(ui.theme.getControlFont())
        .setTextAlignment(VGraphics.Align.LEFT, VGraphics.Align.MIDDLE)
      );

    UIFixture.Section sectOffsets = uiFixture.addSection("Offsets");
    sectOffsets.setTopMargin(SECTION_SPACING);
    sectOffsets.addChildren(
      newRow(fixture.padStart, uiFixture.newControlBox(fixture.padStart, controlWidth)),
      this.padEndRow = newRow(fixture.padEnd, uiFixture.newControlBox(fixture.padEnd, controlWidth))
      );

    uiFixture.addProtocolSection(fixture, true)
      .setTopMargin(SECTION_SPACING);

    uiFixture.addSection("Export")
      .addControlRow(new UI2dComponent[]{
        new UIButton(uiFixture.getContentWidth(), ROW_HEIGHT) {
          @Override
          protected void onToggle(boolean on) {
            if (on) {
              ui.lx.showSaveFileDialog(
                "Export LXF",
                "Fixture Files",
                new String[]{"lxf"},
                ui.lx.getMediaFolder(LX.Media.FIXTURES).toString() + File.separator + fixture.getLabel() + ".lxf",
                (path) -> {
                  fixture.exportTo(new File(path));
                }
              );
            }
          }
        }
        .setMomentary(true)
        .setLabel("Export to LXF")
      })
      .setTopMargin(SECTION_SPACING);

    uiFixture.addListener(fixture.pointMode, p -> {
      PointMode pointMode = fixture.pointMode.getEnum();
      this.numPointsRow.setVisible(pointMode == PointMode.NUMPOINTS);
      this.spacingRow.setVisible(pointMode == PointMode.SPACING);
      this.densityRow.setVisible(pointMode == PointMode.DENSITY);
    }, true);

    uiFixture.addListener(fixture.size, p -> {
      this.pointCount.setLabel("Fixture contains " + fixture.size.getValuei() + " points");
    }, true);
  }

  private UI2dComponent newRow(LXParameter parameter, UI2dComponent control) {
    return UI2dContainer.newHorizontalContainer(ROW_HEIGHT, 2,
      new UILabel(controlWidth, ROW_HEIGHT, parameter.getLabel())
        .setFont(UI.get().theme.getControlFont())
        .setTextAlignment(VGraphics.Align.LEFT, VGraphics.Align.MIDDLE),
      control
      );
  }

}
