package de.fraunhofer.igd.klarschiff.vo;

import org.apache.commons.lang.StringEscapeUtils;


/**
 * Status eines Vorganges
 * @author Stefan Audersch (Fraunhofer IGD)
 *
 */
public enum EnumVorgangStatus implements EnumText {
    gemeldet("gemeldet"),
    offen("offen"),
    inBearbeitung("in Bearbeitung"),
    wirdNichtBearbeitet("wird nicht bearbeitet"),
    duplikat("Duplikat"),
    abgeschlossen("abgeschlossen"),
    geloescht("gel&#246;scht");

    /**
     * Gibt alle Status zurück, bei denen der Vorgang noch offen ist.
     * @return offen Status
     */
    public static EnumVorgangStatus[] openVorgangStatus() {
    	return new EnumVorgangStatus[] {gemeldet, offen, inBearbeitung};
    }
    
    /**
     * Gibt alle Status zurück, bei denen der Vorgang noch in Bearbeitung ist.
     * @return inBearbeitung Status
     */
    public static EnumVorgangStatus[] inProgressVorgangStatus() {
    	return new EnumVorgangStatus[] {inBearbeitung};
    }
    
    /**
     * Gibt alle Status zurück, bei denen der Vorgang geschlossen ist
     * @return geschlossen Status
     */
    public static EnumVorgangStatus[] closedVorgangStatus() {
    	return new EnumVorgangStatus[] {wirdNichtBearbeitet, duplikat, abgeschlossen, geloescht};
    }
    
    /**
     * Gibt alle Status zurück, die für den Außendienst berücksichtigt werden
     * @return geschlossen Status
     */
    public static EnumVorgangStatus[] aussendienstVorgangStatus() {
    	return new EnumVorgangStatus[] {offen, inBearbeitung, wirdNichtBearbeitet, duplikat, abgeschlossen};
    }
    
    /**
     * Gibt alle Status zurück, die auch für Externe (Delegiert) vorgesehen sind
     * @return delegiert Status
     */
    public static EnumVorgangStatus[] delegiertVorgangStatus() {
    	return new EnumVorgangStatus[] {inBearbeitung, wirdNichtBearbeitet, duplikat, abgeschlossen};
    }
    
    private String text;
    
    private EnumVorgangStatus(String text) {
    	this.text = text;
    }
    
    public String getText() {
    	return StringEscapeUtils.unescapeHtml(text);
    }
    
    public String getTextEncoded() {
    	return text;
    }
}
