package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import javax.persistence.Embeddable;

/**
 * Primary-Key-Klasse, da die Tabelle 2 PKs hat. <br/>
 *
 * @author Robert Voﬂ (BFPI GmbH)
 */
@Embeddable
public class AussendienstKoordinatorPK implements Serializable {

  /**
   * Aussendienst (CN der Rolle der Art aussendienst)
   */
  String aussendienst;

  /**
   * Koordinator (CN des Nutzers der Rolle Koordinator)
   */
  String koordinator;

  @Override
  public boolean equals(Object other) {
    return true;
  }

  public int hashCode() {
    return super.hashCode();
  }
}
