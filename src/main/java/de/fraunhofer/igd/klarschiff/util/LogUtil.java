package de.fraunhofer.igd.klarschiff.util;

import org.apache.log4j.Logger;

public class LogUtil {

  private static final Logger logger = Logger.getLogger("klarschiff.backend");

  public static void info(String message) {
    logger.info(message);
  }
}
