package de.fraunhofer.igd.klarschiff.web;

import java.io.Serializable;

/**
 * Command für Trashmails im Adminbereich
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
public class AdminTrashmailCommand implements Serializable {

	/* --------------- Attribute ----------------------------*/

	/**
	 * Strings mit einer Liste von Trashmailadressen
	 */
	String trashmailStr;
	
	/* --------------- GET + SET ----------------------------*/

	public String getTrashmailStr() {
		return trashmailStr;
	}
	public void setTrashmailStr(String trashmailStr) {
		this.trashmailStr = trashmailStr;
	}
	
	
}
