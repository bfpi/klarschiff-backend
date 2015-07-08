package de.fraunhofer.igd.klarschiff.web;

import java.io.Serializable;

import de.fraunhofer.igd.klarschiff.vo.Missbrauchsmeldung;

/**
 * Command zum Erstellen, Betrachten und Bestätigen der Bearbeitung von Missbrauchsmeldungen <br />
 * Beinhaltet Missbrauchsmeldung (VO) und ihre ID.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
public class VorgangMissbrauchCommand implements Serializable {

  Missbrauchsmeldung missbrauchsmeldung;
  Long missbrauchsmeldungId;

  public Missbrauchsmeldung getMissbrauchsmeldung() {
    return missbrauchsmeldung;
  }

  public void setMissbrauchsmeldung(Missbrauchsmeldung missbrauchsmeldung) {
    this.missbrauchsmeldung = missbrauchsmeldung;
  }

  public Long getMissbrauchsmeldungId() {
    return missbrauchsmeldungId;
  }

  public void setMissbrauchsmeldungId(Long missbrauchsmeldungId) {
    this.missbrauchsmeldungId = missbrauchsmeldungId;
  }
}
