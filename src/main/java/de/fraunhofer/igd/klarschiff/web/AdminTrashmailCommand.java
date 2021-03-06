package de.fraunhofer.igd.klarschiff.web;

/**
 * Command für Trashmails im Adminbereich
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
public class AdminTrashmailCommand extends Command {

  /* --------------- Attribute ----------------------------*/
  /**
   * Strings mit einer Liste von Trashmailadressen
   */
  String trashmailStr;

  /* --------------- GET + SET ----------------------------*/
  public String getTrashmailStr() {
    return trashmailStr;
  }

  public void setTrashmailStr(String trashmailStr) {
    this.trashmailStr = trashmailStr;
  }

}
