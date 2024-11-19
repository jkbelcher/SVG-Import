package jkbstudio;

public enum DistanceUnits {
  INCHES("Inches", "in", 1),
  FEET("Feet", "ft",1/12.),
  YARDS("Yards", "yd", 1/36.),
  MILLIMETERS("Millimeters", "mm", 25.4),
  CENTIMETERS("Centimeters", "cm", 2.54),
  METERS("Meters", "m", .0254);

  public final String name;
  public final String abbrev;
  public final double scaleFactor;

  DistanceUnits(String name, String abbreviation, double scaleFactor) {
    this.name = name;
    this.abbrev = abbreviation;
    this.scaleFactor = scaleFactor;
  }

  @Override
  public String toString() {
    return this.name;
  }

  public String getAbbrev() {
    return this.abbrev;
  }

  public double getScaleFactor() {
    return this.scaleFactor;
  }

  public double to(DistanceUnits to, double value) {
    return convert(this, to, value);
  }

  public double from(DistanceUnits from, double value) {
    return convert(from, this, value);
  }

  public static double convert(DistanceUnits from, DistanceUnits to, double value) {
    return value / from.scaleFactor * to.scaleFactor;
  }
}
