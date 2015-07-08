package de.fraunhofer.igd.klarschiff.util;

/**
 * Die Klasse stellt Funktionen für die Arbeit mit Zahlenwerte bereit.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class NumberUtil {

  /**
   * Ermittelt des Minimum von zwei Double-Werten. <code>null</code>-Werte werden dabei ignoriert.
   *
   * @param a Wert 1
   * @param b Wert 2
   * @return Minimum
   */
  public static Double min(Double a, Double b) {
    if (a == null) {
      return b;
    }
    if (b == null) {
      return a;
    }
    return Math.min(a, b);
  }

}
