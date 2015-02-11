package de.fraunhofer.igd.klarschiff.service.geo;

import static de.fraunhofer.igd.klarschiff.util.NumberUtil.min;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

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
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Log4JLoggerFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.springframework.beans.factory.annotation.Autowired;

import com.vividsolutions.jts.algorithm.distance.EuclideanDistanceToPoint;
import com.vividsolutions.jts.algorithm.distance.PointPairDistance;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import de.fraunhofer.igd.klarschiff.service.classification.Attribute;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.util.LogUtil;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;


/**
 * Der Service dient zum Halten von Konfigurationsparametern für die Darstellung von Karten in den Webseiten mit Hilfe von 
 * OpenLayers, zur Erstellung von URLs für die Darstellung der Position eines Vorganges in einem externen System als auch 
 * zur Ermittlung von Features mit geographischem Hintergrund für den Zuständigkeitsfinder, die über den WFS ermittelt
 * werden. Bei der Ermittlung von Features mit geographischen Hintergrund wird ein zweistufiger Cache verwendet, um die 
 * Abfragen an den WFS zu reduzieren. Hierfür wird die Hilfsklasse GeoServiceWfs verwendet, da der Caching nur funktioniert,
 * wenn der Funktionsaufruf nicht aus der gleichen Klasse erfolgt. 
 * @author Stefan Audersch (Fraunhofer IGD)
 * @author Marcus Kröller (Fraunhofer IGD)
 * @see GeoServiceWfs
 */
public class GeoService {
	private static final Logger logger = Logger.getLogger(GeoService.class);

	public enum WfsExceptionHandling { warn, error };
	
	@Autowired
	GeoServiceWfs geoServiceWfs;
	
	@Autowired
	SettingsService settingsService;
	
	String mapProjection;
	String mapMaxExtent;
	String mapRestrictedExtent;
	String mapResolutions;
	String mapServerResolutions;
	Integer mapOviMargin;
	String mapTmsServer;
	String mapTmsServerLayers;
	String mapExternProjection;
	String mapExternName;
	String mapExternUrl;
	String mapExternExternUrl;
    
    String wmsUrl;
    String wmsTitle;
    String wmsLayers;
    String wmsFormat;
    Boolean wmsTransparent;
    Integer wmsMinScale;
    Boolean wmsSingleTile;
	
	String wfsCapabilitiesUrl;
	double oviBuffer = 10d;
	WfsExceptionHandling wfsExceptionHandling = WfsExceptionHandling.error;
	
	DataStore dataStore; 
	FilterFactory2 filterFactory;
	
    private MathTransform mapProjectionToMapExternProjection;

    /**
     * Initialisierung für die Nutzung des WFS und in diesem Zusammenhang ggf. das Setzen von Proxyparametern.
     */
    @PostConstruct
    public void init() {
    	try {
    		//java.util.logging.Logger.getLogger("sun.net.www.protocol.http.HttpURLConnection").setLevel(java.util.logging.Level.SEVERE);
    		//GeoTools.. .setLoggerFactory(Log4JLoggerFactory.getInstance());
        	CoordinateReferenceSystem mapCRS = CRS.decode(mapProjection);
        	CoordinateReferenceSystem mapExternCRS = CRS.decode(mapExternProjection);
        	mapProjectionToMapExternProjection = CRS.findMathTransform(mapCRS, mapExternCRS);
        	
			//ConnectionParameter setzen
			Map<String,String> connectionParameters = new HashMap<String,String>();
			connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", wfsCapabilitiesUrl);

			//ggf. Proxy setzen
			if (!StringUtils.isBlank(settingsService.getProxyHost()) && !StringUtils.isBlank(settingsService.getProxyPort())) {
				logger.info("Proxy wird fuer die Verbindung mit dem WFS wird gesetzt. (ProxyHost:"+settingsService.getProxyHost()+" ProxyPort:"+settingsService.getProxyPort()+")");
				System.setProperty("http.proxyHost", settingsService.getProxyHost());
				System.setProperty("http.proxyPort", settingsService.getProxyPort());
			}
			logger.info("aktuelle Proxyeinstellungen: (ProxyHost:"+System.getProperty("http.proxyHost")+" ProxyPort:"+System.getProperty("http.proxyPort")+")");

			try {
				//DataStoreErzeugen
				LogUtil.info("Verbindung zum WFS wird aufgebaut ...");
				dataStore = DataStoreFinder.getDataStore(connectionParameters);

				if (dataStore==null) throw new NullPointerException();

				try {
					if (logger.getLevel().isGreaterOrEqual(Level.DEBUG))
						//nur zum Debuggen
						for(String typeName : dataStore.getTypeNames()) {
							SimpleFeatureType schema = dataStore.getSchema(typeName);
							CoordinateReferenceSystem  crs = schema.getGeometryDescriptor().getCoordinateReferenceSystem();
							logger.debug(typeName);
							for (Iterator<ReferenceIdentifier> iter = crs.getIdentifiers().iterator(); iter.hasNext(); )
								logger.debug(iter.next().getCode());
						}
				} catch (Exception e) { }
				
				//FilterFactory erzeugen
				filterFactory = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
				
			} catch (Exception e) {
				switch (wfsExceptionHandling) {
				case warn:
					dataStore = null;
					logger.error("Verbindung zum WFS konnte nicht richtig initialisiert werden.", e);
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
     * Koordinatentransformation von der internen Darstellung auf ein Koordinatensystem für die Darstellung des
     * Ortes eines Vorganges in einem externen System.
     * @param point Punktkoordinate, die transformiert werden soll
     */
    private Point transformMapProjectionToMapExternProjection(Point point) {
        try {
        	point = (Point)JTS.transform(point, mapProjectionToMapExternProjection);
        	Coordinate coor = new Coordinate(point.getY(), point.getX());
        	point.getCoordinate().setCoordinate(coor);
        	return point;
        	
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    /**
     * Erstellung der URL zur Darstellung des Ortes eines Vorganges in einem externen Web-Mapping-System
     * Die URL kann über die Einstellungen konfiguriert werden
     * und kann die folgenden Platzhalter beinhalten:
     * <code>%xmin%</code>, <code>%ymin%</code>, <code>%xmax%</code>, <code>%ymax%</code>, <code>%x%</code>, <code>%y%</code>, <code>%id%</code>
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
     * Erstellung der URL zur Darstellung des Ortes eines Vorganges in einem externen System (z.B. im Frontend). 
     * Die URL kann über die Einstellungen konfiguriert werden und
     * kann die folgenden Platzhalter beinhalten: <code>%x%</code>, <code>%y%</code> und <code>%id%</code>,    
     * @param vorgang Vorgang für den die URL erzeugt werden soll
     * @return Url für die externe Anzeige
     * @see #mapExternExternUrl
     */
    public String getMapExternExternUrl(Vorgang vorgang) {
    	Point point = transformMapProjectionToMapExternProjection(vorgang.getOvi());
    	return mapExternExternUrl.replaceAll("%x%", point.getX()+"").replaceAll("%y%", point.getY()+"").replaceAll("%id%", vorgang.getId()+"");
    }

    
    /**
     * Ermitteln des FeatureWertes für ein Feature mit geographischem Hintergrund
     * @param ovi Punkt, für den das Feature ermittelt werden soll
     * @param attribute Feature, für den der Wert ermittelt werden soll
     * @return Distanz bzw. Flächengröße entsprechend des Typs
     * @see de.fraunhofer.igd.klarschiff.service.classification.Attribute.GeoMeasure
     */
    public Double calculateFeature(Point ovi, Attribute attribute) {
		if (dataStore==null) return null;
    	
		Double[] features = geoServiceWfs.getGeoFeatures(ovi, oviBuffer, attribute.getTypeName(), attribute.getGeomPropertyName(), attribute.getPropertyName(), attribute.getPropertyValue());
		
		switch(attribute.getGeoMeasure()) {
			case abstandAusserhalb: return features[0];
			case abstandInnerhalb: return features[1];
			case flaechenGroesse: return features[2];
			default: throw new RuntimeException();
		}
	}
	
    
    /**
     * Ermittelt die FeatureWerte mit geographischem Hintergrund für ein Feature. Dabei werden die Werte für alle
     * GeoMeasure (abstandInnerhalb, abstandAusserhalb, flaechenGroesse) berechnet. Durch den Aufruf der Funktion
     * über die Funktion {@link GeoServiceWfs#getGeoFeatures(Point, double, String, String, String, String)} kann ein ggf.
     * gecachtes Ergebnis der Funktion aufgerufen werden.
     * @param ovi Punkt für den die Features berechnet werden sollen
     * @param oviBuffer Umkreis des Punktes der mit berücksichtigt werden soll
     * @param typeName Typ des Features beim WFS
     * @param geomPropertyName Name des Geometrieattributs beim WFS
     * @param propertyName PropertyName beim WFS
     * @param propertyValue PropertyValue beim WFS
     * @return Featurwerte für den Punkt [0] abstandAusserhalb, [1] abstandInnerhalb und [2] flaechenGroesse
     * @see GeoServiceWfs#getGeoFeatures(Point, double, String, String, String, String)
     */
	protected Double[] getGeoFeatures(Point ovi, double oviBuffer, String typeName, String geomPropertyName, String propertyName, String propertyValue) {
		logger.debug("getGeoFeatures L2: ovi=" +ovi.getX()+","+ovi.getY()+" typeName="+typeName+" geomPropertyName="+geomPropertyName+" propertyName="+propertyName+" propertyValue="+propertyValue);
		//Features für ein typeName über den WFS ermitteln
		List<GeoFeature> features = geoServiceWfs.getGeoFeatures(ovi, oviBuffer, typeName, geomPropertyName, propertyName);
		
		//ggf. Feature bei Attributen mit angegebenem propertyName und propertyValue herausfiltern
		if (propertyName!=null && propertyValue!=null) {
			List<GeoFeature> _features = new ArrayList<GeoFeature>();
			for(GeoFeature feature : features)
				if (StringUtils.equals(propertyValue, feature.getPropertyValue()))
						_features.add(feature);
			features = _features;
		} 
		
		//Workaround für die nachfolgende Operation
		Geometry _ovi = ovi.buffer(0.001,1);

		//Fläche im das ovi bestimmen
		Polygon oviWithBuffer = (Polygon) ovi.buffer(oviBuffer);
		
		//HilfsObjekt zur Abstandsberechnung
		PointPairDistance ppd = new PointPairDistance();
		//Hilfsvariablen für den Durchlauf der Features
		Double distance      = null;
		boolean isInFeatures = false;
		Double area          = 0d;
		
		//jedes gültige Feature durchlaufen
		for(GeoFeature feature : features)
			try {
				Double _distance      = distance;
				boolean _isInFeatures = isInFeatures;
				Double _area          = area;
				//Abstand zum Rand berechnen
				EuclideanDistanceToPoint.computeDistance(feature.getGeometry(), ovi.getCoordinate(), ppd);
				
				if (_ovi.coveredBy(feature.getGeometry())) {
					//ovi ist innerhalb des Features
					if (!_isInFeatures) _distance=null;
					_isInFeatures = true;
					_distance = min(_distance, ppd.getDistance());
				} else {
					//ovi ist außerhalb des Features
					if(!_isInFeatures) _distance = min(_distance, ppd.getDistance());
				}
				//Fläche zur Gesamtfläche hinzufügen
				_area += oviWithBuffer.intersection(feature.getGeometry()).getArea();
			
				distance = _distance;
				isInFeatures = _isInFeatures;
				area = _area;
			} catch (Exception e) {
				logger.warn("Berechnungen für ein GeoFeature sind fehlerhaft.", e);
			}
		
		return (isInFeatures) ? new Double[]{null, distance, area} : new Double[]{distance, null, (area>0d) ? area : null};
	}
	
	
	/**
	 * Ermittelt die angefragten WFS-Features zu einem gegebenen Punkt und dessen Umkreis, die den Umkreis schneiden. 
	 * Durch den Aufruf der Funktion über die Funktion 
	 * {@link GeoServiceWfs#getGeoFeatures(Point, double, String, String, String)} kann ein ggf. gecachtes Ergebnis 
	 * der Funktion aufgerufen werden.
	 * @param ovi Punkt für den die WFS-Features ermittelt werden sollen
	 * @param oviBuffer Umkreis um den Punkt, der berücksichtigt werden soll
	 * @param typeName Typ des Features beim WFS
     * @param geomPropertyName Name des Geometrieattributs beim WFS
	 * @param propertyName PropertyName beim WFS
	 * @return Liste von GeoFeatures
	 * @see de.fraunhofer.igd.klarschiff.service.geo.GeoServiceWfs#getGeoFeatures(Point, double, String, String, String)
	 * @see de.fraunhofer.igd.klarschiff.service.geo.GeoFeature
	 */
	protected List<GeoFeature> getGeoFeatures(Point ovi, double oviBuffer, String typeName, String geomPropertyName, String propertyName) {
		logger.debug("getGeoFeatures L1: ovi=" +ovi.getX()+","+ovi.getY()+" typeName="+typeName+" geomPropertyName="+geomPropertyName+" propertyName="+propertyName);
		try {
			//Fläche um das ovi bestimmen
			Polygon oviWithBuffer = (Polygon)ovi.buffer(oviBuffer);
			
			//Filter für die WFS-anfrage bestimmen
			Filter filter = filterFactory.intersects(filterFactory.property(geomPropertyName), filterFactory.literal(oviWithBuffer)); 
			
			//WFS-Anfrage durchführen
			FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);
			FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures(new Query(typeName, filter));
			
			//Ergebnis der WFS-Anfrage verarbeiten
			List<GeoFeature> result =  new ArrayList<GeoFeature>();
			FeatureIterator<SimpleFeature> iter = features.features();
			while(iter.hasNext()) 
				try {
					SimpleFeature simpleFeature = iter.next();
					Geometry geom = (Geometry)simpleFeature.getProperty(geomPropertyName).getValue(); 
					//Workaround, um Fehler in der Geometry zu beheben
					geom = geom.buffer(0); 
					GeoFeature geoFeature;
					if (propertyName!=null)
						geoFeature = new GeoFeature(geom, (String)simpleFeature.getAttribute(propertyName));
					else 
						geoFeature = new GeoFeature(geom, null);
					result.add(geoFeature);
					logger.debug("getGeoFeatures L1 found simpleFeature for typeName="+typeName+" (propertyValue="+geoFeature.getPropertyValue()+" geometry="+(geoFeature.getGeometry()!=null)+")"); 
				} catch (Exception e) {
					logger.error("GeoFeature kann nicht vom WFS ermittelt werden.", e);
				}
			iter.close();
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/* --------------- GET + SET ----------------------------*/

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
	public Integer getMapOviMargin() {
		return mapOviMargin;
	}
	public void setMapOviMargin(Integer mapOviMargin) {
		this.mapOviMargin = mapOviMargin;
	}
	public String getMapTmsServer() {
		return mapTmsServer;
	}
	public void setMapTmsServer(String mapTmsServer) {
		this.mapTmsServer = mapTmsServer;
	}
	public String getMapTmsServerLayers() {
		return mapTmsServerLayers;
	}
	public void setMapTmsServerLayers(String mapTmsServerLayers) {
		this.mapTmsServerLayers = mapTmsServerLayers;
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
	public String getWmsUrl() {
		return wmsUrl;
	}
	public void setWmsUrl(String wmsUrl) {
		this.wmsUrl = wmsUrl;
	}
	public String getWmsTitle() {
		return wmsTitle;
	}
	public void setWmsTitle(String wmsTitle) {
		this.wmsTitle = wmsTitle;
	}
	public String getWmsLayers() {
		return wmsLayers;
	}
	public void setWmsLayers(String wmsLayers) {
		this.wmsLayers = wmsLayers;
	}
	public String getWmsFormat() {
		return wmsFormat;
	}
	public void setWmsFormat(String wmsFormat) {
		this.wmsFormat = wmsFormat;
	}
	public Boolean getWmsTransparent() {
		return wmsTransparent;
	}
	public void setWmsTransparent(Boolean wmsTransparent) {
		this.wmsTransparent = wmsTransparent;
	}
	public Integer getWmsMinScale() {
		return wmsMinScale;
	}
	public void setWmsMinScale(Integer wmsMinScale) {
		this.wmsMinScale = wmsMinScale;
	}
	public Boolean getWmsSingleTile() {
		return wmsSingleTile;
	}
	public void setWmsSingleTile(Boolean wmsSingleTile) {
		this.wmsSingleTile = wmsSingleTile;
	}
    public String getWfsCapabilitiesUrl() {
		return wfsCapabilitiesUrl;
	}
    public void setWfsCapabilitiesUrl(String wfsCapabilitiesUrl) {
		this.wfsCapabilitiesUrl = wfsCapabilitiesUrl;
	}
    public double getOviBuffer() {
		return oviBuffer;
	}
    public void setOviBuffer(double oviBuffer) {
		this.oviBuffer = oviBuffer;
	}
    public WfsExceptionHandling getWfsExceptionHandling() {
		return wfsExceptionHandling;
	}
    public void setWfsExceptionHandling(WfsExceptionHandling wfsExceptionHandling) {
		this.wfsExceptionHandling = wfsExceptionHandling;
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
    public DataStore getDataStore() {
		return dataStore;
	}
    public String getMapServerResolutions() {
		return mapServerResolutions;
	}
    public void setMapServerResolutions(String mapServerResolutions) {
		this.mapServerResolutions = mapServerResolutions;
	}
}
