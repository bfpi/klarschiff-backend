package de.fraunhofer.igd.klarschiff.vo;

import org.apache.commons.lang.StringEscapeUtils;


/**
 * Status eines Vorganges
 * @author Stefan Audersch (Fraunhofer IGD)
 *
 */
public enum EnumVorgangStatus implements EnumText {
    gemeldet("gemeldet"),				//Ersteller hat seine E-Mail noch nicht bestätigt
    offen("offen"),					//ErstellerEmail wurde bestätigt
    inBearbeitung("in Bearbeitung"),			//wenn das erste mal die Zuständigkeit durch einen Sachbearbeiter akzeptiert wurde
    wirdNichtBearbeitet("wird nicht bearbeitet"),
    duplikat("Duplikat"),
    abgeschlossen("abgeschlossen"),
    geloescht("gel&#246;scht");

    /**
     * Gibt alle Status zurück bei denen der Vorgang noch offen ist.
     * @return offen Status
     */
    public static EnumVorgangStatus[] openVorgangStatus() {
    	return new EnumVorgangStatus[] {gemeldet, offen, inBearbeitung};
    }
    
    /**
     * Gibt alle Status zurück bei denen der Vorgang noch in Bearbeitung ist.
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
