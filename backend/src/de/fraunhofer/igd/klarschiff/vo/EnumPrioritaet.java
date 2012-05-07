package de.fraunhofer.igd.klarschiff.vo;

/**
 * Prioritšt eines Vorganges
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public enum EnumPrioritaet implements EnumText {
	niedrig,
	mittel,
	hoch;
	
	@Override
	public String getText() {
		return name();
	}
}
