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
import heronarts.glx.ui.component.UICollapsibleSection;
import heronarts.glx.ui.component.UIDoubleBox;
import heronarts.glx.ui.component.UIIntegerBox;
import heronarts.glx.ui.component.UILabel;
import heronarts.glx.ui.vg.VGraphics;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.ui.device.UIControls;
import studio.jkb.parameter.SyncParameter;

import java.io.File;

public class UISvgImport extends UICollapsibleSection implements UIControls {

  private static final int SECTION_SPACING = 6;
  private static final int ROW_HEIGHT = 16;

  private final float syncControlWidth;

  private final UILabel labelFilename;
  private final UILabel labelFoundPaths;
  private final UILabel labelNumForExport;
  private final UILabel labelTotalPoints;

  private final UI2dComponent numPointsRow;
  private final UI2dComponent spacingRow;
  private final UI2dComponent densityRow;
  private final UI2dComponent padEndRow;

  /**
   * Constructs a new collapsible section
   *
   * @param ui UI
   * @param w  Width
   */
  public UISvgImport(LXStudio lx, UI ui, SvgImport component, float w) {
    super(ui, 0,0, w, 0);
    setTitle("SVG IMPORT");
    setLayout(Layout.VERTICAL, 4);
    this.syncControlWidth = (getContentWidth() - 20) / 2;

    addChildren(
      newSectionLabel("SETTINGS"),
      newParamButton(component.clearExistingOnImport),

      newSectionLabel("IMPORT"),
      newHorizontalContainer(ROW_HEIGHT, 4,
        // Filename
        this.labelFilename = (UILabel) new UILabel(getContentWidth() - 24, ROW_HEIGHT, "fileName")
          .setLabel(component.fileName.getString())
          .setBackgroundColor(ui.theme.controlBackgroundColor)
          .setBorderColor(ui.theme.controlBorderColor)
          .setBorderRounding(4)
          .setTextAlignment(VGraphics.Align.LEFT, VGraphics.Align.MIDDLE)
          .setDescription(component.fileName.getDescription()),
        // Ellipsis button
        new UIButton(0, 0, 20, ROW_HEIGHT) {
          @Override
          protected void onToggle(boolean on) {
            if (on) {
              lx.showOpenFileDialog(
                "Import SVG",
                "SVG files",
                new String[] { "svg" },
                "",
                (path) -> { component.importSvg(new File(path)); }
              );
            }
          }
        }
          .setMomentary(true)
          .setLabel("...")
          .setBorderRounding(4)
          .setDescription("Import SVG file")
      ),
      this.labelFoundPaths = (UILabel) newInfoLabel().setVisible(false),

      newSectionLabel("GLOBAL"),
      newSyncParameterRow(ui, component.syncPathUnits),
      newSyncParameterRow(ui, component.syncModelUnits),
      newSyncParameterRow(ui, component.syncPointMode),
      this.spacingRow = UI2dContainer.newVerticalContainer(getContentWidth(), 4,
        newSyncParameterRow(ui, component.syncSpacing),
        newSyncParameterRow(ui, component.syncSpacingUnits)
        ),
      this.densityRow = UI2dContainer.newVerticalContainer(getContentWidth(), 4,
        newSyncParameterRow(ui, component.syncDensity),
        newSyncParameterRow(ui, component.syncDensityUnits)
      ),
      this.numPointsRow = newSyncParameterRow(ui, component.syncNumPoints),
      newSyncParameterRow(ui, component.syncReversePath),
      newSyncParameterRow(ui, component.syncPadStart),
      this.padEndRow = newSyncParameterRow(ui, component.syncPadEnd),

      newSectionLabel("EXPORT TO LXF"),
      this.labelNumForExport = newInfoLabel(),
      this.labelTotalPoints = newInfoLabel(),
      newParamButton(component.exportParentFixture),
      new UIButton(getContentWidth(), ROW_HEIGHT) {
        @Override
        protected void onToggle(boolean on) {
          if (on) {
            component.exportAll();
          }
        }
      }
        .setMomentary(true)
        .setLabel("Export ALL to LXF")
        .setBorderRounding(2)
      );

    addListener(component.fileName, (p) -> {
      this.labelFilename.setLabel(component.fileName.getString());
    });

    addListener(component.numPaths, (p) -> {
      this.labelFoundPaths
        .setLabel("Imported " + component.numPaths.getValuei() + " paths")
        .setVisible(true);
    });

    addListener(component.numForExport, (p) -> {
      this.labelNumForExport.setLabel(component.numForExport.getValuei() + " fixtures ready for export");
    }, true);

    addListener(component.totalPoints, p -> {
      this.labelTotalPoints.setLabel(component.totalPoints.getValuei() + " points");
    });

    addListener(component.syncPointMode.parameter, p -> {
      PathFixture.PointMode pointMode =
        ((EnumParameter<PathFixture.PointMode>)component.syncPointMode.parameter).getEnum();
      this.numPointsRow.setVisible(pointMode == PathFixture.PointMode.NUMPOINTS);
      this.spacingRow.setVisible(pointMode == PathFixture.PointMode.SPACING);
      this.densityRow.setVisible(pointMode == PathFixture.PointMode.DENSITY);
      this.padEndRow.setVisible(pointMode != PathFixture.PointMode.SPACING);
    }, true);

  }

  private UI2dComponent newSectionLabel(String label) {
    return new UILabel(getContentWidth(),  label)
      .setFont(UI.get().theme.getLabelFont())
      .setTextAlignment(VGraphics.Align.LEFT, VGraphics.Align.MIDDLE)
      .setTopMargin(SECTION_SPACING);
  }

  private UILabel newInfoLabel() {
    return (UILabel) new UILabel(getContentWidth(),  "")
      .setFont(UI.get().theme.getControlFont())
      .setTextAlignment(VGraphics.Align.LEFT, VGraphics.Align.MIDDLE);
  }

  private UI2dComponent newParamButton(BooleanParameter p) {
    return newHorizontalContainer(ROW_HEIGHT, 2,
      new UILabel(getContentWidth() - COL_WIDTH,  p.getLabel())
        .setFont(UI.get().theme.getControlFont())
        .setTextAlignment(VGraphics.Align.LEFT, VGraphics.Align.MIDDLE)
        .setDescription(p.getDescription()),
      newButton(p)
        .setActiveLabel("Enabled")
        .setInactiveLabel("Disabled")
        .setHeight(ROW_HEIGHT)
    );
  }

  private UI2dComponent newSyncParameterRow(UI ui, SyncParameter syncParameter) {
    UI2dComponent controlComponent;
    if (syncParameter.parameter instanceof EnumParameter) {
      controlComponent = newDropMenu((EnumParameter)syncParameter.parameter, this.syncControlWidth);
    } else if (syncParameter.parameter instanceof DiscreteParameter) {
      controlComponent = new UIIntegerBox(this.syncControlWidth, ROW_HEIGHT, (DiscreteParameter) syncParameter.parameter);
    } else if (syncParameter.parameter instanceof BooleanParameter) {
      controlComponent = new UIButton(0, 0, this.syncControlWidth, ROW_HEIGHT)
        .setParameter((BooleanParameter) syncParameter.parameter)
        .setActiveLabel("On")
        .setInactiveLabel("Off");
    } else if (syncParameter.parameter instanceof BoundedParameter) {
      controlComponent = new UIDoubleBox(0, 0, this.syncControlWidth, ROW_HEIGHT)
        .setParameter((BoundedParameter) syncParameter.parameter)
        .setNormalizedMouseEditing(false)
        .setShiftMultiplier(.05f);
    } else {
      LX.error("TODO: make control row for syncParameter type");
      controlComponent = new UILabel(this.syncControlWidth, "TODO");
    }

    return UI2dContainer.newHorizontalContainer(ROW_HEIGHT, 4,
      new UIButton(0,2,12,12, syncParameter.enabled)
        .setLabel("")
        .setBorderRounding(2),
      new UILabel((getContentWidth() - 20) / 2, ROW_HEIGHT, syncParameter.parameter.getLabel())
        .setFont(ui.theme.getControlFont())
        .setTextAlignment(VGraphics.Align.LEFT, VGraphics.Align.MIDDLE),
      controlComponent
      );
  }
}
