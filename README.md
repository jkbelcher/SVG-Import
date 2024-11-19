# SVG Import Plugin

This is a plugin for [Chromatik](https://chromatik.co/).  It can be used to convert an SVG file containing `path` elements to a Chromatik fixture file.

**BY DOWNLOADING OR USING THE LX SOFTWARE OR ANY PART THEREOF, YOU AGREE TO THE TERMS AND CONDITIONS OF THE [CHROMATIK / LX SOFTWARE LICENSE AND DISTRIBUTION AGREEMENT](http://chromatik.co/license/).**

## Installation

- Download the latest release jar.
- Copy the jar to your `~/Chromatik/Packages` folder, OR import it from Chromatik under the `Left Pane > CONTENT > PACKAGES > +(Import)`
- Confirm the plugin is enabled (box is checked) in the Plugins section
- Find the `SVG Import` section under `Left Pane > MODEL` above the `FIXTURES` section.
- If the `SVG Import` section is not visible, restart Chromatik.

## Usage

- Under the `Left Pane -> MODEL` tab, find the new section `SVG Import`
- Click the ellipsis to browse for an SVG file.
- If the import succeeds, a PathFixture will be created for each `path` element in the SVG file.
- The PathFixture in the `FIXTURES` list is a special editor fixture. Select a fixture and use the `INSPECTOR` section to modify how points are rendered using the Path.  Or, to make the same edit on all PathFixtures generated from the current SVG file, use the `GLOBAL` parameters in the `SVG Import` section.
- Geometry adjustments in the `INSPECTOR` section will be applied to the exported fixture.
- To exclude a PathFixture from group export, deactivate it or delete it from the fixtures list.
- SVG Paths can be export to fixture files individually or all in one operation.
- To export individually, click the `Export...` button at the bottom of the `INSPECTOR` section
- To export all, click the `Export All...` button at the bottom of the `SVG Import` section

## Plugin Development

- Build with `mvn package`
- Install via `mvn install`
