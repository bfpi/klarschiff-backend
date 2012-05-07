package de.fraunhofer.igd.klarschiff.service.classification;

import java.util.List;

import weka.core.FastVector;

/**
 * Mit Hilfe der Klasse wird die Arbeit mit Attributen, wie sie f�r Weka ben�tigt werden, vereinfacht. Die 
 * Attribute sind somit mehr an die Arbeitsweise in Klarschiff angepasst. Es werden Grundfunktionen bereitgestellt,
 * um Attribute f�r Weka einfach zu erzeugen.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
public class Attribute extends weka.core.Attribute {

	/**
	 * legt fest welche Berechnung f�r einen Punkt bzgl. einer Fl�che (Abstand innerhalb einer Fl�che vom Rand, 
	 * Abstand au�erhalb einer Fl�che vom Rand, Gr��e der Fl�che) verwendet wird.
	 */
	public enum GeoMeasure { abstandInnerhalb, abstandAusserhalb, flaechenGroesse }
	
	boolean isClassAttribute = false;
	boolean isGeoAttribute = false;
	boolean isUpdateble = true;
	String typeName;
	String propertyName;
	String propertyValue;
	GeoMeasure geoMeasure;
	String geomPropertyName;
	
	
	/**
	 * Erstellt ein Attribut, wie in der Superklasse.
	 * @param attributeName Name des Attributes
	 */
	private Attribute(String attributeName) {
		super(attributeName);
	}
	
	
	/**
	 * Erstellt ein Attribut mit einem definierten Wertebereich, wie in der Superklasse.
	 * @param attributeName Name des Attributes
	 * @param attributeValues Wertebereich
	 */
	private Attribute(String attributeName, FastVector attributeValues) {
		super(attributeName, attributeValues);
	}

	
	/**
	 * Erzeugt ein Attribut mit einem vorgegebenen Wertebereich.
	 * @param name Name des Attributes
	 * @param values Wertebereich
	 * @param updateble Kann sich der Wert des Attributes (Feature des Vorganges) mit der Zeit �ndern?
	 * @return Attribut mit erweiterten Funktionen, wie es auch in Weka verwendet werden kann
	 */
	public static Attribute createAttribute(String name, List<String> values, boolean updateble) {
		FastVector _values = new FastVector();
		for(String value : values) _values.addElement(value);
		Attribute attr = new Attribute(name, _values);
		attr.isUpdateble = updateble;
		return attr;
	}
	
	
	/**
	 * Erzeugt ein Attribut mit einem vorgegebenen Wertebereich.
	 * Der Wert des Attributes (Feature des Vorganges) kann sich mit der Zeit �ndern.
	 * @param name Name des Attributes
	 * @param values Wertebereich
	 * @return Attribut mit erweiterten Funktionen, wie es auch in Weka verwendet werden kann
	 * @see #createAttribute(String, List, boolean)
	 */
	public static Attribute createClassAttribute(String name, List<String> values) {
		Attribute attr = createAttribute(name, values, true);
		attr.isClassAttribute=true;
		return attr;
	}
	
	
	/**
	 * Erzeugt ein Attribut mit geographischem Hintergrund. Die Attributwerte werden in der Anwendung �ber einen WFS ermittelt.
	 * @param name Name des Attributes
	 * @param typeName Typ des Attributes im WFS
	 * @param geoMeasure Berechnungstyp f�r das Attribut
	 * @param geomPropertyName Attributname der Geometrie beim WMS
	 * @param updateble Kann sich der Wert des Attributes (Feature des Vorganges) mit der Zeit �ndern?
	 * @return Attribut mit erweiterten Funktionen, wie es auch in Weka verwendet werden kann
	 */
	public static Attribute createGeoAttribute(String name, String typeName, GeoMeasure geoMeasure, String geomPropertyName, boolean updateble) {
		Attribute attr = new Attribute(name);
		attr.typeName = typeName;
		attr.geoMeasure = geoMeasure;
		attr.geomPropertyName = geomPropertyName;
		attr.isGeoAttribute = true;
		attr.isUpdateble = updateble;
		return attr;
	}
	

	/**
	 * Erzeugt mehrere Attribute mit geographischem Hintergrund. Dabei wird f�r jedes <code>GeoMeasure</code> jeweils ein 
	 * Attribut erzeugt. Der Name der Attribute ergibt sich aus dem <code>namePrefix</code> und dem jeweiligen <code>GeoMeasure</code>.
	 * @param namePrefix Pr�fix f�r die Namen der Attribute
	 * @param typeName Typ des Attributes im WFS
	 * @param geomPropertyName Attributname der Geometrie beim WMS
	 * @param updateble K�nnen sich die Werte der Attribute (Feature des Vorganges) mit der Zeit �ndern?
	 * @return List (FastVector) mit Attributen
	 */
	public static FastVector createGeoAttributes(String namePrefix, String typeName, String geomPropertyName, boolean updateble) {
		FastVector v = new FastVector();
		for(GeoMeasure geoMeasure : GeoMeasure.values()) {
			v.addElement(createGeoAttribute(namePrefix+"_"+geoMeasure.name(), typeName, geoMeasure, geomPropertyName, updateble));
		}
		return v;
	}
	
	
	/**
	 * Erzeugt ein Attribut mit geographischem Hintergrund. Die Attributwerte werden in der Anwendung �ber einen WFS ermittelt.
	 * Bei der Ermittlung der Attributwerte vom WFS werden dabei Typ (z.B. igd:bewirtschaftung), PropertyName 
	 * (z.B. bewirtschafter) und PropertyValue (z.B. Umweltamt) verwendet. 
	 * @param name Name des Attributes
	 * @param typeName Typ des Attributes im WFS
	 * @param propertyName PropertyName des Attributes beim WFS
	 * @param propertyValue PropertyValue des Attributes beim WFS
	 * @param geomPropertyName Attributname der Geometrie beim WMS
	 * @param updateble Kann sich der Wert des Attributes (Feature des Vorganges) mit der Zeit �ndern?
	 * @return Attribut mit erweiterten Funktionen, wie es auch in Weka verwendet werden kann
	 */
	public static Attribute createGeoAttribute(String name, String typeName, String propertyName, String propertyValue, GeoMeasure geoMeasure, String geomPropertyName, boolean updateble) {
		Attribute attr = createGeoAttribute(name, typeName, geoMeasure, geomPropertyName, updateble);
		attr.propertyName = propertyName;
		attr.propertyValue = propertyValue;
		return attr;
	}
	
	
	/**
	 * Erzeugt mehrere Attribute mit geographischem Hintergrund. Dabei wird f�r jedes <code>GeoMeasure</code> jeweils ein 
	 * Attribut erzeugt. Der Name der Attribute ergibt sich aus dem <code>namePrefix</code> und dem jeweiligen <code>GeoMeasure</code>.
	 * Bei der Ermittlung der Attributwerte vom WFS werden dabei Typ (z.B. igd:bewirtschaftung), PropertyName 
	 * (z.B. bewirtschafter) und PropertyValue (z.B. Umweltamt) verwendet.
	 * @param namePrefix Pr�fix f�r die Namen der Attribute
	 * @param typeName Typ des Attributes im WFS
	 * @param propertyName PropertyName des Attributes beim WFS
	 * @param propertyValue PropertyValue des Attributes beim WFS
	 * @param geomPropertyName Attributname der Geometrie beim WMS
	 * @param updateble K�nnen sich die Werte der Attribute (Feature des Vorganges) mit der Zeit �ndern?
	 * @return List (FastVector) mit Attributen
	 */
	public static FastVector createGeoAttributes(String namePrefix, String typeName, String propertyName, String propertyValue, String geomPropertyName, boolean updateble) {
		FastVector v = new FastVector();
		for(GeoMeasure geoMeasure : GeoMeasure.values()) {
			v.addElement(createGeoAttribute(namePrefix+"_"+geoMeasure.name(), typeName, propertyName, propertyValue, geoMeasure, geomPropertyName, updateble));
		}
		return v;
	}

	/* --------------- GET + SET ----------------------------*/

	public String getName() {
		return name();
	}
	public boolean isClassAttribute() {
		return isClassAttribute;
	}
	public boolean isGeoAttribute() {
		return isGeoAttribute;
	}
	public boolean isUpdateble() {
		return isUpdateble;
	}
	public String getTypeName() {
		return typeName;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public String getPropertyValue() {
		return propertyValue;
	}
	public GeoMeasure getGeoMeasure() {
		return geoMeasure;
	}
	public String getGeomPropertyName() {
		return geomPropertyName;
	}
	
}
