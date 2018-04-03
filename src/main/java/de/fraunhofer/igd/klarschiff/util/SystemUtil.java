package de.fraunhofer.igd.klarschiff.util;

/**
 * Die Klasse bieten Funktionen zum auslesen von System-Variablen.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class SystemUtil {

  /**
   * Auslesen und ausgeben von System-Variablen.
   */
  public static void printlnSystemVariables() {

    System.out.println("-- system environment --");
    for (String key : System.getenv().keySet()) {
      System.out.println(" " + key + "=" + System.getenv(key));
    }
    System.out.println("--  system properties --");
    for (Object key : System.getProperties().keySet()) {
      System.out.println(" " + key + "=" + System.getProperty((String) key));
    }
  }
}
