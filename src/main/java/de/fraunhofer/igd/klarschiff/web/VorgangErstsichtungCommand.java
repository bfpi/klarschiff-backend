package de.fraunhofer.igd.klarschiff.web;

import java.io.Serializable;

import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Command für die Erstsichtung im Backend <br />
 * Beinhaltet lediglich ein Vorgangs-Objekt.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
public class VorgangErstsichtungCommand implements Serializable {

  Vorgang vorgang;

  public Vorgang getVorgang() {
    return vorgang;
  }

  public void setVorgang(Vorgang vorgang) {
    this.vorgang = vorgang;
  }
}
