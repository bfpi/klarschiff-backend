package de.fraunhofer.igd.klarschiff.util;

public class SystemUtil {

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
