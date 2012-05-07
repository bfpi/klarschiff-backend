package de.fraunhofer.igd.klarschiff.repository;

import org.hibernate.cfg.ImprovedNamingStrategy;

/**
 * Die Klasse ist für die Benennung der Tabellen und Attribute in der DB verantwortlich.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
public class NamingStrategy extends ImprovedNamingStrategy {

	public static final NamingStrategy INSTANCE = new NamingStrategy();

	/**
	 * Die Tabellennamen ergeben sich aus dem Klassennamen und einem davorgestellten
	 * <code>klarschiff_</code>
	 */
	public String classToTableName(String className) {
		return "klarschiff_"+super.classToTableName(className);
	}


	/**
	 * Die Tabellennamen ergeben sich aus dem Klassennamen und einem davorgestellten
	 * <code>klarschiff_</code>
	 */
	public String tableName(String tableName) {
		return "klarschiff_"+super.tableName(tableName);
	}


	/**
	 * Die Tabellennamen für collections ergeben sich aus dem Klassennamen und einem davorgestellten
	 * <code>klarschiff_</code>
	 */
	public String logicalCollectionTableName(String tableName, String ownerEntityTable, String associatedEntityTable, String propertyName) {
		return "klarschiff_"+super.logicalCollectionTableName(tableName, ownerEntityTable, associatedEntityTable, propertyName);
	}
}
