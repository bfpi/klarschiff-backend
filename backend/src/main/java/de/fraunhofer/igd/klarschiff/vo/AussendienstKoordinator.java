package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;


/**
 * VO f�r die Relation der Au�endienst-Koordinatoren. <br/>
 *
 * @author Robert Vo� (BFPI GmbH)
 */
@SuppressWarnings("serial")
@Entity
@IdClass(AussendienstKoordinatorPK.class)
public class AussendienstKoordinator implements Serializable {

  /**
   * Aussendienst (CN der Rolle der Art aussendienst)
   */
  @Id
  private String aussendienst;

  /**
   * Koordinator (CN des Nutzers der Rolle Koordinator)
   */
  @Id
  private String koordinator;

  /**
   * Getter und Setter
   */
  public String getAussendienst() {
    return aussendienst;
  }

  public void setAussendienst(String aussendienst) {
    this.aussendienst = aussendienst;
  }

  public String getKoordinator() {
    return koordinator;
  }

  public void setKoordinator(String koordinator) {
    this.koordinator = koordinator;
  }
}
