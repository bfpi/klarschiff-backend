package de.fraunhofer.igd.klarschiff.web;

import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import java.io.Serializable;

/**
 * Command zur Vereinheitlichung
 *
 * @author Robert Voß (BFPI GmbH)
 */
public class Command implements Serializable {

  Vorgang vorgang;

  public Vorgang getVorgang() {
    return vorgang;
  }

  public void setVorgang(Vorgang vorgang) {
    this.vorgang = vorgang;
  }
}
