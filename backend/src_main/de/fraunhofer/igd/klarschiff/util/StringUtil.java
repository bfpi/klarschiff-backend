package de.fraunhofer.igd.klarschiff.util;

import java.io.UnsupportedEncodingException;

/**
 * Die Klasse bieten Funktionen zum Arbeiten mit Strings.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class StringUtil {
	
	/**
	 * Ändert das Encoding eines Strings
	 * @param str String
	 * @param fromEncoding Encoding des Inputs
	 * @param toEncoding Encoding des Output
	 * @return String mit dem Encoding des Output
	 */
	public static String encode(String str, String fromEncoding, String toEncoding) {
		try {
			return new String(str.getBytes(fromEncoding), toEncoding);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
