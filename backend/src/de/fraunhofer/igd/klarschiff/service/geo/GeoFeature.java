package de.fraunhofer.igd.klarschiff.service.geo;

import com.vividsolutions.jts.geom.Geometry;


/**
 * Die Klasse ist eine Hilfsklasse zum Übergeben der ermittelten Features vom WFS. 
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class GeoFeature {
	Geometry geometry;
	String propertyValue;
	
	
	/**
	 * Initialisieren
	 * @param geometry Geometrie
	 * @param propertyValue ggf. PropertyValue vom WFS oder null
	 */
	public GeoFeature(Geometry geometry, String propertyValue) {
		super();
		this.geometry = geometry;
		this.propertyValue = propertyValue;
	}
	
	public Geometry getGeometry() {
		return geometry;
	}
	
	public String getPropertyValue() {
		return propertyValue;
	}
	
	
}
