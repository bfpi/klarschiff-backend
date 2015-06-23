package de.fraunhofer.igd.klarschiff.vo;

/**
 * Interface, welches benutzt wird, um die verschiedenen Enums und deren möglichen Werte ebenfalls in der DB zu persistieren.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public interface EnumText {
	public String name();
	public int ordinal();
	public String getText();
}
