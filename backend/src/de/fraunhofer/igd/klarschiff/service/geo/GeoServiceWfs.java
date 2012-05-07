package de.fraunhofer.igd.klarschiff.service.geo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.KeyGenerator;
import com.googlecode.ehcache.annotations.Property;
import com.vividsolutions.jts.geom.Point;


/**
 * Hilfsklasse zum Cachen von Anfragen an den WFS. Das Ergebnis beim Aufruf von Funktionen aus der gleichen Klasse 
 * heraus nicht gechacht werden können, werden die Funktionsaufrufe über diese Hilfsklasse umgeleitet.
 * @author Stefan Audersch (Fraunhofer IGD)
 * @see GeoService
 */
@Service
public class GeoServiceWfs {

	@Autowired
	GeoService geoService;

	/**
	 * Die Funktion wird zur Nutzung des Chaches auf die Funktion 
	 * {@link GeoService#getGeoFeatures(Point, double, String, String, String, String)} umgeleitet.
	 * see GeoService#getGeoFeatures(Point, double, String, String, String, String)
	 */
	@Cacheable(cacheName="geoServiceWfsLevel2Cache", 
			keyGenerator = @KeyGenerator(name = "ListCacheKeyGenerator",
				properties = {
					@Property( name="useReflection", value="true"), 
					@Property( name="checkforCycles", value="true" ),
					@Property( name="includeMethod", value="true" )
				}
			)
		)
	public Double[] getGeoFeatures(Point ovi, double oviBuffer, String typeName, String geomPropertyName, String propertyName, String propertyValue) {
		return geoService.getGeoFeatures(ovi, oviBuffer, typeName, geomPropertyName, propertyName, propertyValue);
	}

	
	/**
	 * Die Funktion wird zur Nutzung des Chaches auf die Funktion 
	 * {@link GeoService#getGeoFeatures(Point, double, String, String, String)} umgeleitet.
	 * see GeoService#getGeoFeatures(Point, double, String, String, String)
	 */
	@Cacheable(cacheName="geoServiceWfsLevel1Cache", 
			keyGenerator = @KeyGenerator(name = "ListCacheKeyGenerator",
				properties = {
					@Property( name="useReflection", value="true"), 
					@Property( name="checkforCycles", value="true" ),
					@Property( name="includeMethod", value="true" )
				}
			)
		)
	public List<GeoFeature> getGeoFeatures(Point ovi, double oviBuffer, String typeName, String geomPropertyName, String propertyName) {
		return geoService.getGeoFeatures(ovi, oviBuffer, typeName, geomPropertyName, propertyName);
	}
}
