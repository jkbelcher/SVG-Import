/**
 * Copyright 2024- Justin K. Belcher
 *
 * @author Justin K. Belcher <justin@jkb.studio>
 */

package jkbstudio.svg;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.structure.LXFixture;
import heronarts.lx.structure.LXStructure;
import heronarts.lx.utils.LXUtils;
import jkbstudio.parameter.SumParameter;
import jkbstudio.parameter.SyncParameter;
import jkbstudio.structure.JsonKeys;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Component that imports SVG files, creates fixtures from SVG paths, and provides
 * management across imported fixtures including parameter sync and group export.
 *
 * @author Justin Belcher <justin@jkb.studio>
 */
public class SvgImport extends LXComponent implements LXStructure.Listener, JsonKeys {

  public final StringParameter fileName =
    new StringParameter("File Name", "")
      .setDescription("Name of the imported SVG file, read-only");

  public final BooleanParameter clearExistingOnImport =
    new BooleanParameter("Clear on Import", true)
      .setDescription("Whether to clear previously imported SVG Path fixtures when a new SVG file is imported");

  public final MutableParameter numPaths =
    new MutableParameter("NumPaths", 0)
      .setDescription("Number of paths found in the SVG file, read-only");

  public final MutableParameter numForExport =
    new MutableParameter("NumForExport", 0)
      .setDescription("Number of path fixtures ready for export to LXF, read-only");

  public final BooleanParameter exportParentFixture =
    new BooleanParameter("Export Parent Fixture", true)
      .setDescription("Whether Export All creates a parent fixture containing all the path fixtures");

  private final List<PathFixture> fixtures = new ArrayList<>();
  public final SyncParameter syncPathUnits;
  public final SyncParameter syncModelUnits;
  public final SyncParameter syncPointMode;
  public final SyncParameter syncSpacing;
  public final SyncParameter syncNumPoints;
  public final SyncParameter syncReversePath;
  public final SyncParameter syncPadStart;
  public final SyncParameter syncPadEnd;

  public final SumParameter totalPoints =
    new SumParameter("Total Points")
      .setDescription("Total number of points");

  private final LXParameterListener deactivateListener = (p) -> {
    refreshNumForExport();
    // Add/Remove child size from points sum
    // TODO: handle this in the SumParameter
    PathFixture fixture = (PathFixture)p.getParent();
    if (((BooleanParameter)p).isOn()) {
      this.totalPoints.removeChildParameter(fixture.size);
    } else {
      if (!this.totalPoints.hasChildParameter(fixture.size)) {
        this.totalPoints.addChildParameter(fixture.size);
      }
    }
  };

  public SvgImport(LX lx) {
    super(lx);
    addParameter("clearExistingOnImport", this.clearExistingOnImport);
    addParameter("exportParentFixture", this.exportParentFixture);
    this.lx.structure.addListener(this);

    addChild("syncPathUnits",
      this.syncPathUnits = new SyncParameter(lx, PathFixture.newPathUnits()));
    addChild("syncModelUnits",
      this.syncModelUnits = new SyncParameter(lx, PathFixture.newModelUnits()));
    addChild("syncPointMode",
      this.syncPointMode = new SyncParameter(lx, PathFixture.newPointMode()));
    addChild("syncSpacing",
      this.syncSpacing = new SyncParameter(lx, PathFixture.newSpacing()));
    addChild("syncNumPoints",
      this.syncNumPoints = new SyncParameter(lx, PathFixture.newNumPoints()));
    addChild("syncReversePath",
      this.syncReversePath = new SyncParameter(lx, PathFixture.newReversePath()));
    addChild("syncPadStart",
      this.syncPadStart = new SyncParameter(lx, PathFixture.newPadStart()));
    addChild("syncPadEnd",
      this.syncPadEnd = new SyncParameter(lx, PathFixture.newPadEnd()));
  }

  private void addFixtures(Collection<PathFixture> fixtures) {
    for (PathFixture fixture : fixtures) {
      addFixture(fixture);
    }
  }

  private void addFixture(PathFixture fixture) {
    this.fixtures.add(fixture);
    fixture.deactivate.addListener(this.deactivateListener);
    this.lx.structure.addFixture(fixture);
    refreshNumForExport();

    this.totalPoints.addChildParameter(fixture.size);

    this.syncModelUnits.addChildParameter(fixture.modelUnits);
    this.syncPathUnits.addChildParameter(fixture.pathUnits);
    this.syncPointMode.addChildParameter(fixture.pointMode);
    this.syncNumPoints.addChildParameter(fixture.numPoints);
    this.syncSpacing.addChildParameter(fixture.spacing);
    this.syncReversePath.addChildParameter(fixture.reversePath);
    this.syncPadStart.addChildParameter(fixture.padStart);
    this.syncPadEnd.addChildParameter(fixture.padEnd);
  }

  private void removeFixtures(Collection<PathFixture> fixtures) {
    for (PathFixture fixture : fixtures) {
      removeFixture(fixture);
    }
  }

  private void removeFixture(PathFixture fixture) {
    this.lx.structure.removeFixture(fixture);
    // Fires lx.structure.fixtureRemoved()
  }

  @Override
  public void fixtureAdded(LXFixture fixture) {}

  @Override
  public void fixtureMoved(LXFixture fixture, int index) {}

  @Override
  public void fixtureRemoved(LXFixture fixture) {
    if (fixture instanceof PathFixture pathFixture && this.fixtures.contains(fixture)) {
      pathFixture.deactivate.removeListener(this.deactivateListener);
      if (this.totalPoints.hasChildParameter(pathFixture.size)) {
        this.totalPoints.removeChildParameter(pathFixture.size);
      }
      this.syncModelUnits.removeChildParameter(pathFixture.modelUnits);
      this.syncPathUnits.removeChildParameter(pathFixture.pathUnits);
      this.syncPointMode.removeChildParameter(pathFixture.pointMode);
      this.syncNumPoints.removeChildParameter(pathFixture.numPoints);
      this.syncSpacing.removeChildParameter(pathFixture.spacing);
      this.syncReversePath.removeChildParameter(pathFixture.reversePath);
      this.syncPadStart.removeChildParameter(pathFixture.padStart);
      this.syncPadEnd.removeChildParameter(pathFixture.padEnd);
      this.fixtures.remove(pathFixture);
      refreshNumForExport();
    }
  }

  private void refreshNumForExport() {
    this.numForExport.setValue(
      this.fixtures.stream()
        .filter(item -> !item.deactivate.isOn())
        .count()
    );
  }

  public void importSvg(File file) {
    if (this.clearExistingOnImport.getValueb()) {
      for (int i = this.fixtures.size() - 1; i >= 0; i--) {
        removeFixture(this.fixtures.get(i));
      }
    }
    this.numPaths.reset();
    List<String> paths;

    // Attempt file read
    try {
      paths = SvgUtils.loadSVGpaths(file);
      this.fileName.setValue(file.getName());
      this.numPaths.setValue(paths.size());
    } catch (Exception x) {
      LX.error(x, "Error loading SVG: ");
      return;
    }

    // Create fixtures
    int iPath = 0;
    for (String path : paths) {
      PathFixture fixture = new PathFixture(this.lx, path);
      fixture.label.setValue(fixture.label.getString() + " " + iPath++);
      addFixture(fixture);
    }
  }

  public void exportAll() {
    // Currently overwrites existing files

    // Export each child fixture
    List<String> childFixtureNames = new ArrayList<>();
    for (PathFixture fixture : this.fixtures) {
      if (!fixture.deactivate.isOn()) {
        String childName = fixture.exportToDefault();
        if (!LXUtils.isEmpty(childName)) {
          childFixtureNames.add(childName);
        }
      }
    }

    // Export the parent fixture
    if (this.exportParentFixture.isOn() && childFixtureNames.size() > 0) {
      String fileName = !LXUtils.isEmpty(this.fileName.getString())
        ? PathFixture.removeExtension(this.fileName.getString())
        : "SVGexport";
      exportParent(fileName, childFixtureNames);
    }
  }

  private void exportParent(String fixtureName, List<String> childFixtureNames) {
    String filePath = this.lx.getMediaFolder(LX.Media.FIXTURES).toString() + File.separator + fixtureName + ".lxf";
    File file = new File(filePath);

    JsonObject obj = new JsonObject();
    obj.addProperty(KEY_LABEL, fixtureName);

    // tags
    JsonArray tags = new JsonArray();
    tags.add("SVG");
    tags.add(fixtureName);
    obj.add(KEY_TAGS, tags);

    // metadata
    JsonObject metadata = new JsonObject();
    metadata.addProperty("generator", "Chromatik, SVG Import Plugin, Version " + SvgImportPlugin.VERSION);
    obj.add(KEY_METADATA, metadata);

    // components
    JsonArray components = new JsonArray();
    obj.add(KEY_COMPONENTS, components);

    // child components
    for (String childFixtureName : childFixtureNames) {
      JsonObject component = new JsonObject();
      components.add(component);
      component.addProperty("type", childFixtureName);
    }

    try (JsonWriter writer = new JsonWriter(new FileWriter(file))) {
      writer.setIndent("  ");
      new GsonBuilder().create().toJson(obj, writer);
      LX.log("Parent fixture exported successfully to " + file);
    } catch (IOException iox) {
      LX.error(iox, "Exception writing fixture file to " + file);
    }
  }

  @Override
  public void dispose() {
    this.lx.structure.removeListener(this);
    super.dispose();
  }

}
