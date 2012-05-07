package de.fraunhofer.igd.klarschiff.service.geo;

import org.apache.log4j.Logger;

import de.fraunhofer.igd.klarschiff.util.ClassPathResourceUtil;
import de.fraunhofer.igd.klarschiff.vo.StadtGrenze;
import de.fraunhofer.igd.klarschiff.vo.StadtteilGrenze;

/**
 * FactoryKlasse zum Auslesen der Stadt- und Stadteilgrenzen aus der Konfiguration 
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class GrenzeFactory {
	
	Logger logger = Logger.getLogger(GrenzeFactory.class);
	
	/**
	 * Funktion zum lesen der Stadtgrenze.
	 * @param file Wkt-File mit der Stadtgrenze
	 * @return Stadtgrenze mit Geometrie
	 * @throws Exception
	 */
	public StadtGrenze createStadtGrenze(String file) throws Exception
	{
		StadtGrenze grenze = new StadtGrenze();
		grenze.setGrenzeWkt(ClassPathResourceUtil.readFile(file));
		return grenze;
	}
	
	
	/**
	 * Funktion zum Lesen einer Stadtteilgrenze
	 * @param name Name des Stadtteils
	 * @param file Wkt-file mit der Stadteilgrenze
	 * @return Stadtteilgrenze mit Namen und Geometrie
	 * @throws Exception
	 */
	public StadtteilGrenze createStadtteilGrenze(String name, String file) throws Exception
	{
		StadtteilGrenze grenze = new StadtteilGrenze();
		grenze.setName(name);
		grenze.setGrenzeWkt(ClassPathResourceUtil.readFile(file));
		return grenze;
	}
}
