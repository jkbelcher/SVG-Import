/**
 * Copyright 2024- Justin K. Belcher
 *
 * @author Justin K. Belcher <justin@jkb.studio>
 */

package studio.jkb.svg;

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

        default:
          LX.error("Unknown SVG command: " + Character.toUpperCase(command));
          break;
      }
    }
    return path;
  }

}
