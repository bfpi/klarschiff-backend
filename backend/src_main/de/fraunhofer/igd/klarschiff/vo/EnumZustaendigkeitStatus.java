package de.fraunhofer.igd.klarschiff.vo;


/**
 * Status der Zust�ndigkeit f�r einen Status
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public enum EnumZustaendigkeitStatus implements EnumText {
	zugewiesen,
	akzeptiert;

	@Override
	public String getText() {
		return name();
	}

}
