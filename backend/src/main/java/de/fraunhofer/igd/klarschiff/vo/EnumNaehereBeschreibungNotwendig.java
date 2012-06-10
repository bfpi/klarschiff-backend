package de.fraunhofer.igd.klarschiff.vo;

/**
 * Legt anhand der Kategorie fest, ob eine N�here Beschreibung durch den Betreff und/oder die Details notwendig sind.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public enum EnumNaehereBeschreibungNotwendig implements EnumText {

    keine, 
    betreff,
    details,
    betreffUndDetails;
        
	@Override
	public String getText() {
		return name();
	}
}
