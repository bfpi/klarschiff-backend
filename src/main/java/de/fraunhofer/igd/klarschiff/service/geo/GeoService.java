package de.fraunhofer.igd.klarschiff.service.geo;

import static de.fraunhofer.igd.klarschiff.util.NumberUtil.min;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.persistence.Transient;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.factory.annotation.Autowired;
import com.vividsolutions.jts.algorithm.distance.DistanceToPoint;
import com.vividsolutions.jts.algorithm.distance.PointPairDistance;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import de.fraunhofer.igd.klarschiff.service.classification.Attribute;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.util.LogUtil;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Der Service dient zum Halten von Konfigurationsparametern für die Darstellung von Karten in den
 * Webseiten mit Hilfe von OpenLayers, zur Erstellung von URLs für die Darstellung der Position
 * eines Vorganges in einem externen System als auch zur Ermittlung von Features mit geographischem
 * Hintergrund für den Zuständigkeitsfinder, die über den WFS ermittelt werden. Bei der Ermittlung
 * von Features mit geographischen Hintergrund wird ein zweistufiger Cache verwendet, um die
 * Abfragen an den WFS zu reduzieren. Hierfür wird die Hilfsklasse GeoServiceWfs verwendet, da der
 * Caching nur funktioniert, wenn der Funktionsaufruf nicht aus der gleichen Klasse erfolgt.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 * @author Marcus Kröller (Fraunhofer IGD)
 * @see GeoServiceWfs
 */
public class GeoService {

  private static final Logger logger = Logger.getLogger(GeoService.class);

  @Autowired
  GeoServiceWfs geoServiceWfs;

  @Autowired
  SettingsService settingsService;

  String mapProjection;
  String mapMaxExtent;
  String mapRestrictedExtent;
  String mapResolutions;
  String mapUnits;
  Integer mapOviMargin;

  String mapLayersOneType;
  String mapLayersOneParams;
  String mapLayersTwoType;
  String mapLayersTwoParams;
  String mapLayersPoiType;
  String mapLayersPoiParams;

  String mapExternProjection;
  String mapExternName;
  String mapExternUrl;
  String mapExternExternUrl;

  String vorgangCoordinates;

  String wfsVorgaengeUrl;
  String wfsVorgaengeFeatureNs;
  String wfsVorgaengeFeaturePrefix;
  String wfsVorgaengeFeatureType;

  String adressensucheUrl;
  String adressensucheKey;
  String adressensucheLocalisator;

  public enum WfsZufiExceptionHandling {

    warn, error
  };
  WfsZufiExceptionHandling wfsZufiExceptionHandling = WfsZufiExceptionHandling.error;
  String wfsZufiCapabilitiesUrl;
  double wfsZufiOviBuffer = 10d;

  DataStore dataStore;
  FilterFactory2 filterFactory;

  @Transient
  private static SettingsService localSettingsService = new SettingsService();

  /**
   * Initialisierung für die Nutzung des WFS und in diesem Zusammenhang ggf. das Setzen von
   * Proxyparametern.
   */
  @PostConstruct
  public void init() {
    try {
      //ConnectionParameter setzen
      Map<String, String> connectionParameters = new HashMap<String, String>();
      connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", wfsZufiCapabilitiesUrl);

      //ggf. Proxy setzen
      if (!StringUtils.isBlank(settingsService.getProxyHost()) && !StringUtils.isBlank(settingsService.getProxyPort())) {
        logger.info("Proxy wird fuer die Verbindung mit dem WFS wird gesetzt. (ProxyHost:" + settingsService.getProxyHost() + " ProxyPort:" + settingsService.getProxyPort() + ")");
        System.setProperty("http.proxyHost", settingsService.getProxyHost());
        System.setProperty("http.proxyPort", settingsService.getProxyPort());
      }
      logger.info("aktuelle Proxyeinstellungen: (ProxyHost:" + System.getProperty("http.proxyHost") + " ProxyPort:" + System.getProperty("http.proxyPort") + ")");

      try {
        //DataStoreErzeugen
        LogUtil.info("Verbindung zum WFS wird aufgebaut ...");
        dataStore = DataStoreFinder.getDataStore(connectionParameters);

        if (dataStore == null) {
          throw new NullPointerException();
        }

        try {
          if (logger.getLevel().isGreaterOrEqual(Level.DEBUG)) //nur zum Debuggen
          {
            for (String typeName : dataStore.getTypeNames()) {
              SimpleFeatureType schema = dataStore.getSchema(typeName);
              CoordinateReferenceSystem crs = schema.getGeometryDescriptor().getCoordinateReferenceSystem();
              logger.debug(typeName);
              for (Iterator<ReferenceIdentifier> iter = crs.getIdentifiers().iterator(); iter.hasNext();) {
                logger.debug(iter.next().getCode());
              }
            }
          }
        } catch (Exception e) {
        }

        //FilterFactory erzeugen
        filterFactory = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());

      } catch (Exception e) {
        switch (wfsZufiExceptionHandling) {
          case warn:
            dataStore = null;
            logger.error("Verbindung zum WFS konnte nicht richtig initialisiert werden.", e);
            LogUtil.info("Verbindung zum WFS konnte nicht richtig initialisiert werden.");

            break;
          default:
            throw e;
        }
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Koordinatentransformation von der internen Darstellung auf ein Koordinatensystem für die
   * Darstellung des Ortes eines Vorganges in einem externen System.
   *
   * @param point Punktkoordinate, die transformiert werden soll
   */
  private Point transformMapProjectionToMapExternProjection(Point point) {
    try {
      return de.bfpi.tools.GeoTools.transformPosition(point, mapProjection, mapExternProjection);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Erstellung der URL zur Darstellung des Ortes eines Vorganges in einem externen
   * Web-Mapping-System Die URL kann über die Einstellungen konfiguriert werden und kann die
   * folgenden Platzhalter beinhalten: <code>%xmin%</code>, <code>%ymin%</code>,
   * <code>%xmax%</code>, <code>%ymax%</code>, <code>%x%</code>, <code>%y%</code>, <code>%id%</code>
   *
   * @param vorgang Vorgang, für den die URL erzeugt werden soll
   * @return URL für die externe Anzeige
   * @see #mapExternUrl
   */
  public String getMapExternUrl(Vorgang vorgang) {
    Point point = transformMapProjectionToMapExternProjection(vorgang.getOvi());
    String x = String.valueOf((int) point.getX());
    String y = String.valueOf((int) point.getY());
    String xmin = String.valueOf((int) (point.getX() - 200));
    String ymin = String.valueOf((int) (point.getY() - 200));
    String xmax = String.valueOf((int) (point.getX() + 200));
    String ymax = String.valueOf((int) (point.getY() + 200));
    String id = String.valueOf(vorgang.getId());
    return mapExternUrl.replaceAll("%xmin%", xmin).replaceAll("%ymin%", ymin).replaceAll("%xmax%", xmax).replaceAll("%ymax%", ymax).replaceAll("%x%", x).replaceAll("%y%", y).replaceAll("%id%", id);
  }

  /**
   * Ermittlung der Adresse
   *
   * @param point Punktkoordinate, für die die Adresse ermittelt werden soll
   * @return Adresse
   */
  public String calculateAddress(Point point, Boolean d3) {
    try {
      String x = String.valueOf((int) point.getX());
      String y = String.valueOf((int) point.getY());
      String adresse = null;
      String url = localSettingsService.getPropertyValue("geo.adressensuche.url");
      url += "key=" + localSettingsService.getPropertyValue("geo.adressensuche.key");
      url += "&query=" + x + "," + y;
      url += "&type=reverse";
      url += "&class=address";
      if (!d3)
        url += "&radius=100";
      url += "&in_epsg=25833";

      URL httpUrl = new URL(url);
      HttpURLConnection connection = (HttpURLConnection) httpUrl.openConnection();
      connection.setRequestMethod("GET");

      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuilder stringBuilder = new StringBuilder();
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        stringBuilder.append(line + "\n");
      }
      bufferedReader.close();

      JSONObject jsonObject = new JSONObject(stringBuilder.toString());
      JSONArray features = jsonObject.getJSONArray("features");
      if (d3) {
        for (int i = 0; i < features.length(); i++) {
          JSONObject feature = features.getJSONObject(i);
          JSONObject properties = feature.getJSONObject("properties");
          String objektgruppe = properties.getString("objektgruppe");
          if (StringUtils.equals(objektgruppe, "Straße")) {
            adresse = properties.getString("strasse_name");
            adresse += " (";
            adresse += properties.getString("gemeindeteil_name");
            adresse += "-";
            adresse += properties.getString("strasse_schluessel");
            adresse += ")";
            break;
          }
        }
      } else {
        for (int i = 0; i < features.length(); i++) {
          JSONObject feature = features.getJSONObject(i);
          JSONObject properties = feature.getJSONObject("properties");
          String objektgruppe = properties.getString("objektgruppe");
          if (StringUtils.equals(objektgruppe, "Adresse")) {
            adresse = properties.getString("strasse_name");
            adresse += " ";
            adresse += properties.getString("hausnummer");
            if (!properties.get("hausnummer_zusatz").equals(null))
              adresse += properties.getString("hausnummer_zusatz");
            adresse += " (";
            adresse += properties.getString("abkuerzung");
            adresse += ")";
            Double entfernung = properties.getDouble("entfernung");
            if (entfernung > 50)
              adresse = "bei " + adresse;
            break;
          } else if (StringUtils.equals(objektgruppe, "Straße") && StringUtils.isEmpty(adresse)) {
            adresse = properties.getString("strasse_name");
            Double entfernung = properties.getDouble("entfernung");
            if (entfernung > 50)
              adresse = "bei " + adresse;
          }
        }
      }

      if (!d3 && StringUtils.isEmpty(adresse))
        adresse = "nicht zuordenbar";

      return adresse;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Erstellung der URL zur Darstellung des Ortes eines Vorganges in einem externen System (z.B. im
   * Frontend). Die URL kann über die Einstellungen konfiguriert werden und kann die folgenden
   * Platzhalter beinhalten: <code>%x%</code>, <code>%y%</code> und <code>%id%</code>,
   *
   * @param vorgang Vorgang, für den die URL erzeugt werden soll
   * @return Url für die externe Anzeige
   * @see #mapExternExternUrl
   */
  public String getMapExternExternUrl(Vorgang vorgang) {
    Point point = transformMapProjectionToMapExternProjection(vorgang.getOvi());
    return mapExternExternUrl.replaceAll("%x%", point.getX() + "").replaceAll("%y%", point.getY() + "").replaceAll("%id%", vorgang.getId() + "");
  }

  /**
   * Erstellung der URL zur Darstellung des Ortes eines Vorganges in einem externen System (z.B. im
   * Frontend). Die URL kann über die Einstellungen konfiguriert werden und kann die folgenden
   * Platzhalter beinhalten: <code>%x%</code>, <code>%y%</code> und <code>%id%</code>,
   *
   * @param vorgang Vorgang, für den die Koordinaten ausgegeben werden sollen
   * @return Koordinaten
   */
  public String getVorgangCoordinates(Vorgang vorgang) {
    Point point = transformMapProjectionToMapExternProjection(vorgang.getOvi());
    String x = String.valueOf((int) point.getX());
    String y = String.valueOf((int) point.getY());
    return x + " " + y;
  }

  /**
   * Ermitteln des FeatureWertes für ein Feature mit geographischem Hintergrund
   *
   * @param ovi Punkt, für den das Feature ermittelt werden soll
   * @param attribute Feature, für den der Wert ermittelt werden soll
   * @return Distanz bzw. Flächengröße entsprechend des Typs
   * @see de.fraunhofer.igd.klarschiff.service.classification.Attribute.GeoMeasure
   */
  public Double calculateFeature(Point ovi, Attribute attribute) {
    if (dataStore == null) {
      return null;
    }

    Double[] features = geoServiceWfs.getGeoFeatures(ovi, wfsZufiOviBuffer, attribute.getTypeName(), attribute.getGeomPropertyName(), attribute.getPropertyName(), attribute.getPropertyValue());

    /*switch(attribute.getGeoMeasure()) {
     case abstandAusserhalb: return features[0];
     case abstandInnerhalb: return features[1];
     case flaechenGroesse: return features[2];*/
    if (features[1] == null) {
      return 0.0;
    } else {
      if (features[1] > 0) {
        return 1.0;
      } else {
        return 0.0;
      }
    }
    //default: throw new RuntimeException();
    //}
  }

  /**
   * Ermittelt die FeatureWerte mit geographischem Hintergrund für ein Feature. Dabei werden die
   * Werte für alle GeoMeasure (abstandInnerhalb, abstandAusserhalb, flaechenGroesse) berechnet.
   * Durch den Aufruf der Funktion über die Funktion
   * {@link GeoServiceWfs#getGeoFeatures(Point, double, String, String, String, String)} kann ein
   * ggf. gecachtes Ergebnis der Funktion aufgerufen werden.
   *
   * @param ovi Punkt für den die Features berechnet werden sollen
   * @param wfsZufiOviBuffer Umkreis des Punktes der mit berücksichtigt werden soll
   * @param typeName Typ des Features beim WFS
   * @param geomPropertyName Name des Geometrieattributs beim WFS
   * @param propertyName PropertyName beim WFS
   * @param propertyValue PropertyValue beim WFS
   * @return Featurwerte für den Punkt [0] abstandAusserhalb, [1] abstandInnerhalb und [2]
   * flaechenGroesse
   * @see GeoServiceWfs#getGeoFeatures(Point, double, String, String, String, String)
   */
  protected Double[] getGeoFeatures(Point ovi, double wfsZufiOviBuffer, String typeName, String geomPropertyName, String propertyName, String propertyValue) {
    logger.debug("getGeoFeatures L2: ovi=" + ovi.getX() + "," + ovi.getY() + " typeName=" + typeName + " geomPropertyName=" + geomPropertyName + " propertyName=" + propertyName + " propertyValue=" + propertyValue);
    //Features für ein typeName über den WFS ermitteln
    List<GeoFeature> features = geoServiceWfs.getGeoFeatures(ovi, wfsZufiOviBuffer, typeName, geomPropertyName, propertyName);

    //ggf. Feature bei Attributen mit angegebenem propertyName und propertyValue herausfiltern
    if (propertyName != null && propertyValue != null) {
      List<GeoFeature> _features = new ArrayList<GeoFeature>();
      for (GeoFeature feature : features) {
        if (StringUtils.equals(propertyValue, feature.getPropertyValue())) {
          _features.add(feature);
        }
      }
      features = _features;
    }

    //Workaround für die nachfolgende Operation
    Geometry _ovi = ovi.buffer(0.001, 1);

    //Fläche im das ovi bestimmen
    Polygon oviWithBuffer = (Polygon) ovi.buffer(wfsZufiOviBuffer);

    //HilfsObjekt zur Abstandsberechnung
    PointPairDistance ppd = new PointPairDistance();
    //Hilfsvariablen für den Durchlauf der Features
    Double distance = null;
    boolean isInFeatures = false;
    Double area = 0d;

    //jedes gültige Feature durchlaufen
    for (GeoFeature feature : features) {
      try {
        Double _distance = distance;
        boolean _isInFeatures = isInFeatures;
        Double _area = area;
        //Abstand zum Rand berechnen
        DistanceToPoint.computeDistance(feature.getGeometry(), ovi.getCoordinate(), ppd);

        if (_ovi.coveredBy(feature.getGeometry())) {
          //ovi ist innerhalb des Features
          if (!_isInFeatures) {
            _distance = null;
          }
          _isInFeatures = true;
          _distance = min(_distance, ppd.getDistance());
        } else {
          //ovi ist außerhalb des Features
          if (!_isInFeatures) {
            _distance = min(_distance, ppd.getDistance());
          }
        }
        //Fläche zur Gesamtfläche hinzufügen
        _area += oviWithBuffer.intersection(feature.getGeometry()).getArea();

        distance = _distance;
        isInFeatures = _isInFeatures;
        area = _area;
      } catch (Exception e) {
        logger.warn("Berechnungen für ein GeoFeature sind fehlerhaft.", e);
      }
    }

    return (isInFeatures) ? new Double[]{null, distance, area} : new Double[]{distance, null, (area > 0d) ? area : null};
  }

  /**
   * Ermittelt die angefragten WFS-Features zu einem gegebenen Punkt und dessen Umkreis, die den
   * Umkreis schneiden. Durch den Aufruf der Funktion über die Funktion
   * {@link GeoServiceWfs#getGeoFeatures(Point, double, String, String, String)} kann ein ggf.
   * gecachtes Ergebnis der Funktion aufgerufen werden.
   *
   * @param ovi Punkt für den die WFS-Features ermittelt werden sollen
   * @param wfsZufiOviBuffer Umkreis um den Punkt, der berücksichtigt werden soll
   * @param typeName Typ des Features beim WFS
   * @param geomPropertyName Name des Geometrieattributs beim WFS
   * @param propertyName PropertyName beim WFS
   * @return Liste von GeoFeatures
   * @see de.fraunhofer.igd.klarschiff.service.geo.GeoServiceWfs#getGeoFeatures(Point, double,
   * String, String, String)
   * @see de.fraunhofer.igd.klarschiff.service.geo.GeoFeature
   */
  protected List<GeoFeature> getGeoFeatures(Point ovi, double wfsZufiOviBuffer, String typeName, String geomPropertyName, String propertyName) {
    logger.debug("getGeoFeatures L1: ovi=" + ovi.getX() + "," + ovi.getY() + " typeName=" + typeName + " geomPropertyName=" + geomPropertyName + " propertyName=" + propertyName);
    try {
      //Fläche um das ovi bestimmen
      Polygon oviWithBuffer = (Polygon) ovi.buffer(wfsZufiOviBuffer);

      //Filter für die WFS-anfrage bestimmen
      Filter filter = filterFactory.intersects(filterFactory.property(geomPropertyName), filterFactory.literal(oviWithBuffer));

      //WFS-Anfrage durchführen
      FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);
      FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures(new Query(typeName, filter));

      //Ergebnis der WFS-Anfrage verarbeiten
      List<GeoFeature> result = new ArrayList<GeoFeature>();
      FeatureIterator<SimpleFeature> iter = features.features();
      while (iter.hasNext()) {
        try {
          SimpleFeature simpleFeature = iter.next();
          Geometry geom = (Geometry) simpleFeature.getProperty(geomPropertyName).getValue();
          //Workaround, um Fehler in der Geometry zu beheben
          geom = geom.buffer(0);
          GeoFeature geoFeature;
          if (propertyName != null) {
            geoFeature = new GeoFeature(geom, (String) simpleFeature.getAttribute(propertyName));
          } else {
            geoFeature = new GeoFeature(geom, null);
          }
          result.add(geoFeature);
          logger.debug("getGeoFeatures L1 found simpleFeature for typeName=" + typeName + " (propertyValue=" + geoFeature.getPropertyValue() + " geometry=" + (geoFeature.getGeometry() != null) + ")");
        } catch (Exception e) {
          logger.error("GeoFeature kann nicht vom WFS ermittelt werden.", e);
        }
      }
      iter.close();

      return result;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public String getMapProjection() {
    return mapProjection;
  }

  public void setMapProjection(String mapProjection) {
    this.mapProjection = mapProjection;
  }

  public String getMapMaxExtent() {
    return mapMaxExtent;
  }

  public void setMapMaxExtent(String mapMaxExtent) {
    this.mapMaxExtent = mapMaxExtent;
  }

  public String getMapRestrictedExtent() {
    return mapRestrictedExtent;
  }

  public void setMapRestrictedExtent(String mapRestrictedExtent) {
    this.mapRestrictedExtent = mapRestrictedExtent;
  }

  public String getMapResolutions() {
    return mapResolutions;
  }

  public void setMapResolutions(String mapResolutions) {
    this.mapResolutions = mapResolutions;
  }

  public String getMapUnits() {
    return mapUnits;
  }

  public void setMapUnits(String mapUnits) {
    this.mapUnits = mapUnits;
  }

  public Integer getMapOviMargin() {
    return mapOviMargin;
  }

  public void setMapOviMargin(Integer mapOviMargin) {
    this.mapOviMargin = mapOviMargin;
  }

  public String getMapLayersOneType() {
    return mapLayersOneType;
  }

  public void setMapLayersOneType(String mapLayersOneType) {
    this.mapLayersOneType = mapLayersOneType;
  }

  public String getMapLayersOneParams() {
    return mapLayersOneParams;
  }

  public void setMapLayersOneParams(String mapLayersOneParams) {
    this.mapLayersOneParams = mapLayersOneParams;
  }

  public String getMapLayersTwoType() {
    return mapLayersTwoType;
  }

  public void setMapLayersTwoType(String mapLayersTwoType) {
    this.mapLayersTwoType = mapLayersTwoType;
  }

  public String getMapLayersTwoParams() {
    return mapLayersTwoParams;
  }

  public void setMapLayersTwoParams(String mapLayersTwoParams) {
    this.mapLayersTwoParams = mapLayersTwoParams;
  }

  public String getMapLayersPoiType() {
    return mapLayersPoiType;
  }

  public void setMapLayersPoiType(String mapLayersPoiType) {
    this.mapLayersPoiType = mapLayersPoiType;
  }

  public String getMapLayersPoiParams() {
    return mapLayersPoiParams;
  }

  public void setMapLayersPoiParams(String mapLayersPoiParams) {
    this.mapLayersPoiParams = mapLayersPoiParams;
  }

  public String getMapExternProjection() {
    return mapExternProjection;
  }

  public void setMapExternProjection(String mapExternProjection) {
    this.mapExternProjection = mapExternProjection;
  }

  public String getMapExternName() {
    return mapExternName;
  }

  public void setMapExternName(String mapExternName) {
    this.mapExternName = mapExternName;
  }

  public String getMapExternUrl() {
    return mapExternUrl;
  }

  public void setMapExternUrl(String mapExternUrl) {
    this.mapExternUrl = mapExternUrl;
  }

  public String getMapExternExternUrl() {
    return mapExternExternUrl;
  }

  public void setMapExternExternUrl(String mapExternExternUrl) {
    this.mapExternExternUrl = mapExternExternUrl;
  }

  public String getVorgangCoordinates() {
    return vorgangCoordinates;
  }

  public void setVorgangCoordinates(String vorgangCoordinates) {
    this.vorgangCoordinates = vorgangCoordinates;
  }

  public String getWfsZufiCapabilitiesUrl() {
    return wfsZufiCapabilitiesUrl;
  }

  public void setWfsZufiCapabilitiesUrl(String wfsZufiCapabilitiesUrl) {
    this.wfsZufiCapabilitiesUrl = wfsZufiCapabilitiesUrl;
  }

  public double getWfsZufiOviBuffer() {
    return wfsZufiOviBuffer;
  }

  public void setWfsZufiOviBuffer(double wfsZufiOviBuffer) {
    this.wfsZufiOviBuffer = wfsZufiOviBuffer;
  }

  public WfsZufiExceptionHandling getWfsZufiExceptionHandling() {
    return wfsZufiExceptionHandling;
  }

  public void setWfsZufiExceptionHandling(WfsZufiExceptionHandling wfsZufiExceptionHandling) {
    this.wfsZufiExceptionHandling = wfsZufiExceptionHandling;
  }

  public String getWfsVorgaengeUrl() {
    return wfsVorgaengeUrl;
  }

  public void setWfsVorgaengeUrl(String wfsVorgaengeUrl) {
    this.wfsVorgaengeUrl = wfsVorgaengeUrl;
  }

  public String getWfsVorgaengeFeatureNs() {
    return wfsVorgaengeFeatureNs;
  }

  public void setWfsVorgaengeFeatureNs(String wfsVorgaengeFeatureNs) {
    this.wfsVorgaengeFeatureNs = wfsVorgaengeFeatureNs;
  }

  public String getWfsVorgaengeFeaturePrefix() {
    return wfsVorgaengeFeaturePrefix;
  }

  public void setWfsVorgaengeFeaturePrefix(String wfsVorgaengeFeaturePrefix) {
    this.wfsVorgaengeFeaturePrefix = wfsVorgaengeFeaturePrefix;
  }

  public String getWfsVorgaengeFeatureType() {
    return wfsVorgaengeFeatureType;
  }

  public void setWfsVorgaengeFeatureType(String wfsVorgaengeFeatureType) {
    this.wfsVorgaengeFeatureType = wfsVorgaengeFeatureType;
  }

  public String getAdressensucheUrl() {
    return adressensucheUrl;
  }

  public void setAdressensucheUrl(String adressensucheUrl) {
    this.adressensucheUrl = adressensucheUrl;
  }

  public String getAdressensucheKey() {
    return adressensucheKey;
  }

  public void setAdressensucheKey(String adressensucheKey) {
    this.adressensucheKey = adressensucheKey;
  }

  public String getAdressensucheLocalisator() {
    return adressensucheLocalisator;
  }

  public void setAdressensucheLocalisator(String adressensucheLocalisator) {
    this.adressensucheLocalisator = adressensucheLocalisator;
  }

  public DataStore getDataStore() {
    return dataStore;
  }
}
