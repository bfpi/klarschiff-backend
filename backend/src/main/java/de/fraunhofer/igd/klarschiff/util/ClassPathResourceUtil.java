package de.fraunhofer.igd.klarschiff.util;

import org.springframework.core.io.ClassPathResource;

/**
 * Die Klasse ermöglicht den einfachen Zugriff auf Dateien im Klassenpfad.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class ClassPathResourceUtil {
	
	/**
	 * Liest eine Datei als String ein.
	 * @param file Dateipfad im Klassenpfad (z.B. <code>META-INF/sql/init.sql</code> oder <code>META-INF/oviwkt/stadtgrenze.wkt</code>)
	 * @return Inhalt der Datei als String
	 * @throws Exception
	 */
	public static String readFile(String file) throws Exception
	{
		return new String(StreamUtil.readInputStream(new ClassPathResource(file).getInputStream()));
	}

}
