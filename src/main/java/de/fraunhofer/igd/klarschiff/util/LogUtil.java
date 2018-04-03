package de.fraunhofer.igd.klarschiff.util;

import org.apache.log4j.Logger;

/**
 * Die Klasse stellt Funktionen zum Loggen bereit.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class LogUtil {

  private static final Logger logger = Logger.getLogger("klarschiff.backend");

  /**
   * Erstelle einen Log-Eintrag
   *
   * @param message Text der geloggt werden soll.
   */
  public static void info(String message) {
    logger.info(message);
  }
}
