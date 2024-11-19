package jkbstudio.svg;

import heronarts.lx.LX;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.geom.Path2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SvgUtils {

  public static class ControlPoint {
    double x, y;

    ControlPoint(double x, double y) {
      this.x = x;
      this.y = y;
    }
  }

  /**
   * Read an SVG file and return a list of all path data
   */
  public static List<String> loadSVGpaths(File file) throws Exception {
    List<String> paths = new ArrayList<>();

    // Parse SVG file
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(file);

    // Get all path elements
    NodeList pathElements = document.getElementsByTagName("path");

    // Extract each path data
    for (int i = 0; i < pathElements.getLength(); i++) {
      Element pathElement = (Element) pathElements.item(i);
      String pathData = pathElement.getAttribute("d");
      paths.add(pathData);
    }

    return paths;
  }

  private static ControlPoint lastControlPoint;
  private static ControlPoint lastQuadControlPoint;

  /**
   * Parse SVG path data into a Path2D object
   */
  public static Path2D parseSVGPath(String pathData) {
    Path2D path = new Path2D.Double();
    String[] commands = pathData.split("(?=[MmLlHhVvCcSsQqTtAaZz])");
    double currentX = 0;
    double currentY = 0;

    for (String cmd : commands) {
      if (cmd.isEmpty()) continue;
      char command = cmd.charAt(0);
      String[] sParams = cmd.substring(1).trim().split("[ ,]+");
      boolean isRelative = Character.isLowerCase(command);
      command = Character.toUpperCase(command);

      // Convert parameters to doubles
      List<Double> params = new ArrayList<>();
      for (String sParam : sParams) {
        if (!sParam.isEmpty()) {
          try {
            params.add(Double.parseDouble(sParam));
          } catch (NumberFormatException e) {
            LX.warning("Failed to parse SVG parameter. Expected double, found: " + sParam);
          }
        }
      }

      switch (Character.toUpperCase(command)) {
        case 'M': // Move
          currentX = (isRelative ? currentX : 0) + params.get(0);
          currentY = (isRelative ? currentY : 0) + params.get(1);
          path.moveTo(currentX, currentY);
          break;

        case 'L': // Line
          for (int p = 0; p < params.size() - 1; p += 2) {
            currentX = (isRelative ? currentX : 0) + params.get(p);
            currentY = (isRelative ? currentY : 0) + params.get(p + 1);
            path.lineTo(currentX, currentY);
          }
          break;

        case 'H': // Horizontal line
          currentX = (isRelative ? currentX : 0) + params.get(0);
          path.lineTo(currentX, currentY);
          break;

        case 'V': // Vertical line
          currentY = (isRelative ? currentY : 0) + params.get(1);
          path.lineTo(currentX, currentY);
          break;

        case 'Z': // Close path
          path.closePath();
          break;

        ///////////////////////////////////////////////////////////////////////////////////////
        // ** Flushed out the following as placeholder but everything below here is untested **

        case 'C': // Cubic Bézier (x1 y1 x2 y2 x y)
          for (int p = 0; p < params.size() - 5; p += 6) {
            double x1 = (isRelative ? currentX : 0) + params.get(p);
            double y1 = (isRelative ? currentY : 0) + params.get(p + 1);
            double x2 = (isRelative ? currentX : 0) + params.get(p + 2);
            double y2 = (isRelative ? currentY : 0) + params.get(p + 3);
            double x = (isRelative ? currentX : 0) + params.get(p + 4);
            double y = (isRelative ? currentY : 0) + params.get(p + 5);
            path.curveTo(x1, y1, x2, y2, x, y);
            currentX = x;
            currentY = y;
          }
          break;

        case 'S': // Smooth cubic Bézier (x2 y2 x y)
          for (int p = 0; p < params.size() - 3; p += 4) {
            // Calculate reflection of last control point
            double x1 = currentX + (currentX - lastControlPoint.x);
            double y1 = currentY + (currentY - lastControlPoint.y);

            double x2 = (isRelative ? currentX : 0) + params.get(p);
            double y2 = (isRelative ? currentY : 0) + params.get(p + 1);
            double x = (isRelative ? currentX : 0) + params.get(p + 2);
            double y = (isRelative ? currentY : 0) + params.get(p + 3);

            path.curveTo(x1, y1, x2, y2, x, y);

            lastControlPoint = new ControlPoint(x2, y2);
            currentX = x;
            currentY = y;
          }
          break;

        case 'Q': // Quadratic Bézier (x1 y1 x y)
          for (int p = 0; p < params.size() - 3; p += 4) {
            double x1 = (isRelative ? currentX : 0) + params.get(p);
            double y1 = (isRelative ? currentY : 0) + params.get(p + 1);
            double x = (isRelative ? currentX : 0) + params.get(p + 2);
            double y = (isRelative ? currentY : 0) + params.get(p + 3);
            path.quadTo(x1, y1, x, y);
            currentX = x;
            currentY = y;
          }
          break;

        case 'T': // Smooth quadratic Bézier (x y)
          for (int p = 0; p < params.size() - 1; p += 2) {
            // Calculate reflection of last quadratic control point
            double x1 = currentX + (currentX - lastQuadControlPoint.x);
            double y1 = currentY + (currentY - lastQuadControlPoint.y);

            double x = isRelative ? currentX + params.get(p) : params.get(p);
            double y = isRelative ? currentY + params.get(p + 1) : params.get(p + 1);

            path.quadTo(x1, y1, x, y);

            lastQuadControlPoint = new ControlPoint(x1, y1);
            currentX = x;
            currentY = y;
          }
          break;

        case 'A': // Elliptical Arc (rx ry x-axis-rotation large-arc-flag sweep-flag x y)
          for (int p = 0; p < params.size() - 6; p += 7) {
            double rx = Math.abs(params.get(p));
            double ry = Math.abs(params.get(p + 1));
            double xAxisRotation = params.get(p + 2);
            boolean largeArcFlag = params.get(p + 3) != 0;
            boolean sweepFlag = params.get(p + 4) != 0;
            double x = isRelative ? currentX + params.get(p + 5) : params.get(p + 5);
            double y = isRelative ? currentY + params.get(p + 6) : params.get(p + 6);

            // Convert the SVG arc to a sequence of cubic Bézier curves
            addArcToPath(path, currentX, currentY, rx, ry, xAxisRotation,
              largeArcFlag, sweepFlag, x, y);

            currentX = x;
            currentY = y;
          }
          break;

        default:
          LX.error("Unknown SVG command: " + Character.toUpperCase(command));
          break;
      }
    }
    return path;
  }

  /**
   * Converts an SVG arc to a series of cubic Bézier curves
   */
  public static void addArcToPath(Path2D path, double x0, double y0, double rx, double ry,
                                   double angle, boolean largeArcFlag, boolean sweepFlag,
                                   double x, double y) {
    // Implementation of the endpoint to center parameterization
    // Based on the SVG implementation notes
    // https://www.w3.org/TR/SVG/implnote.html#ArcImplementationNotes

    // Handle degenerate cases
    if (x0 == x && y0 == y) return;
    if (rx == 0 || ry == 0) {
      path.lineTo(x, y);
      return;
    }

    // Convert angle from degrees to radians
    double angleRad = Math.toRadians(angle % 360.0);

    // Transform to origin
    double dx2 = (x0 - x) / 2.0;
    double dy2 = (y0 - y) / 2.0;

    // Compute transformed point
    double x1 = Math.cos(angleRad) * dx2 + Math.sin(angleRad) * dy2;
    double y1 = -Math.sin(angleRad) * dx2 + Math.cos(angleRad) * dy2;

    // Ensure radii are large enough
    rx = Math.abs(rx);
    ry = Math.abs(ry);
    double Prx = rx * rx;
    double Pry = ry * ry;
    double Px1 = x1 * x1;
    double Py1 = y1 * y1;

    // Check if radii are large enough
    double radiiCheck = Px1/Prx + Py1/Pry;
    if (radiiCheck > 1) {
      rx = Math.sqrt(radiiCheck) * rx;
      ry = Math.sqrt(radiiCheck) * ry;
      Prx = rx * rx;
      Pry = ry * ry;
    }

    // Compute center point
    double sign = (largeArcFlag == sweepFlag) ? -1 : 1;
    double sq = ((Prx*Pry)-(Prx*Py1)-(Pry*Px1)) / ((Prx*Py1)+(Pry*Px1));
    sq = (sq < 0) ? 0 : sq;
    double coef = sign * Math.sqrt(sq);
    double cx1 = coef * ((rx*y1)/ry);
    double cy1 = coef * -((ry*x1)/rx);

    // Compute final center point
    double cx = Math.cos(angleRad) * cx1 - Math.sin(angleRad) * cy1 + (x0 + x)/2;
    double cy = Math.sin(angleRad) * cx1 + Math.cos(angleRad) * cy1 + (y0 + y)/2;

    // Compute start and sweep angles
    double ux = (x1 - cx1) / rx;
    double uy = (y1 - cy1) / ry;
    double vx = (-x1 - cx1) / rx;
    double vy = (-y1 - cy1) / ry;
    double startAngle = Math.toDegrees(Math.atan2(uy, ux));
    double sweepAngle = Math.toDegrees(Math.atan2(vy, vx)) - startAngle;

    if (!sweepFlag && sweepAngle > 0) {
      sweepAngle -= 360;
    } else if (sweepFlag && sweepAngle < 0) {
      sweepAngle += 360;
    }

    // Convert to cubic Bézier curves
    // Break up the arc into small segments and approximate with cubic Bézier curves
    double numSegments = Math.ceil(Math.abs(sweepAngle) / 90.0);
    double deltaAngle = sweepAngle / numSegments;
    double angle1 = Math.toRadians(startAngle);

    for (int i = 0; i < numSegments; i++) {
      double angle2 = angle1 + Math.toRadians(deltaAngle);

      double t = Math.tan(Math.toRadians(deltaAngle) / 4);
      double alpha = Math.sin(Math.toRadians(deltaAngle)) *
        (Math.sqrt(4 + 3 * t * t) - 1) / 3;

      double sinA1 = Math.sin(angle1);
      double cosA1 = Math.cos(angle1);
      double sinA2 = Math.sin(angle2);
      double cosA2 = Math.cos(angle2);

      double x1c = rx * cosA1;
      double y1c = ry * sinA1;
      double x2c = rx * cosA2;
      double y2c = ry * sinA2;

      path.curveTo(
        cx + cosA1 * rx - alpha * sinA1 * rx,
        cy + sinA1 * ry + alpha * cosA1 * ry,
        cx + cosA2 * rx + alpha * sinA2 * rx,
        cy + sinA2 * ry - alpha * cosA2 * ry,
        cx + x2c,
        cy + y2c
      );

      angle1 = angle2;
    }
  }

}
