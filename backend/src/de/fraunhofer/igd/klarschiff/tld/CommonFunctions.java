package de.fraunhofer.igd.klarschiff.tld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

/**
 * Die Klasse stellt die Implementierung für allgemeine EL-Funktionen bereit.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class CommonFunctions {

	/**
	 * Die Methode teilt einen String bei einem Komma.
	 * @param array String mit Komma-separierten Werten
	 * @return einzelne Werte aus dem String als Array
	 */
	public static String[] array(String array) {
		return array.split(",");
	}
	
	
	/**
	 * Die Methode teilt einen String bei einem gegeben Trennzeichen
	 * @param array String mit separierten Werten
	 * @param delim Trennzeichen
	 * @return einzelne Werte aus dem String als Array
	 */
	public static String[] array(String array, String delim) {
		return array.split(delim);
	}


	/**
	 * Kodiert einen String in HTML wobei Zeilenumbrüche in <code>&lt;br/&gt;</code> umgewandelt werden.
	 */
	public static String toHtml(String str) {
		return str.replaceAll("\n", "<br/>");
	}
	
	
	/**
	 * Kodiert ein String in HTML wobei zwei aufeinanderfolgende Leerzeichen in <code>&amp;nbsp;&amp;nbsp;</code> umgewandelt werden.
	 */
	public static String whitespaceToHtml(String str) {
		return str.replaceAll("  ", "&nbsp;&nbsp;").replaceAll("\t", "&nbsp;&nbsp;");
	}


	/**
	 * Kürzt einen String auf eine festgegebenen Länge. Am Ende des neuen Strings werden "..." gesetzt.
	 * @param str String der gekürzt werden soll
	 * @param maxWidth maximale Länge des neuen Strings
	 * @return gekürzter String
	 */
	public static String abbreviate(String str, Integer maxWidth) {
		return StringUtils.abbreviate(str, maxWidth);
	}
	
	
	/**
	 * Extrahiert aus einer Collection von Arrays die Werte an einer gegebenen Position im Array.
	 * @param listOfArrays Collection mit Arrays
	 * @param position Position der zu extrahierenden Werte in den Arrays
	 * @return Liste mit Werten aus dem Array
	 */
	@SuppressWarnings({ "rawtypes" })
	public static Collection extractArrayItemFromList(Collection listOfArrays, Integer position) {
		List<Object> result = new ArrayList<Object>();
		for(Object array : listOfArrays) result.add(((Object[])array)[position]);
		return result;
	}
}
