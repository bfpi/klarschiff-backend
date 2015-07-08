package de.bfpi.tools;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

public class GeoTools {

  public static final String wgs84Projection = "EPSG:4326";

  /**
   * Sichere Tranformation eines Punktes von einer Projektion in eine andere. Berücksichtigung der
   * eventuell notwendigen Koordinatenvertauschung, falls eine der Projektionen WGS84 ist.
   *
   * @param point mit Koordinatenformat [LAT, LONG]
   * @param sourceProjection Name der Quellprojektion
   * @param targetProjection Name der Zielprojektion
   * @return Point im Koordinatenformat [LAT, LONG]
   * @throws FactoryException
   * @throws MismatchedDimensionException
   * @throws TransformException
   */
  public static Point transformPosition(Point point, String sourceProjection, String targetProjection)
    throws FactoryException, MismatchedDimensionException, TransformException {

    if (sourceProjection.equals(targetProjection)) {
      return point;
    }

    // Define CRS forced as EAST_NORTH, because of unpredictable axis order
    // when using system default.
    // System default axis order sometimes changes after redeployment!
    CoordinateReferenceSystem sourceCRS = CRS.decode(sourceProjection, true);
    CoordinateReferenceSystem targetCRS = CRS.decode(targetProjection, true);

    Point input = (Point) point.clone();
    if (sourceProjection.equals(wgs84Projection)) {
      input.getCoordinate().setCoordinate(new Coordinate(point.getY(), point.getX()));
    }

    Point output = (Point) JTS.transform(input, CRS.findMathTransform(sourceCRS, targetCRS));
    if (targetProjection.equals(wgs84Projection)) {
      output.getCoordinate().setCoordinate(new Coordinate(output.getY(), output.getX()));
    }

    CRS.cleanupThreadLocals();

    return output;
  }
}
