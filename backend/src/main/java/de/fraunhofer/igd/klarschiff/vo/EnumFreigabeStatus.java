package de.fraunhofer.igd.klarschiff.vo;


/**
 * Freigabestatus f�r Betreff, Details und Foto
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public enum EnumFreigabeStatus implements EnumText {

    intern, 
    extern,
    geloescht;
        
	@Override
	public String getText() {
		return name();
	}
}
