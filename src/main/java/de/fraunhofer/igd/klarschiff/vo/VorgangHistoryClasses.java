package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * VO zum Abbilden der bereits einem Zugang zugeordneten Zuständigkeiten bzw. Klassen bei der
 * Klassifikation. Die bereits verwendeten Zuständigkeiten für eine Vorgang werden verwendet, damit
 * beim zuständigkeitsinder ein Vorgang nicht wiederholt die gleiche Zuständigkeit zugeordnet wird.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
@Entity
public class VorgangHistoryClasses implements Serializable {

  /* --------------- Attribute ----------------------------*/
  /**
   * Vorgang für die die bereits verwendeten Zuständigkeiten abgelegt werden.
   */
  @Id
  @OneToOne
  @JoinColumn
  Vorgang vorgang;

  /**
   * Liste von bereits verwendeten Zuständigkeiten
   */
  @ElementCollection(fetch = FetchType.EAGER)
  Set<String> historyClasses = new HashSet<String>();

  /* --------------- GET + SET ----------------------------*/
  public Vorgang getVorgang() {
    return vorgang;
  }

  public void setVorgang(Vorgang vorgang) {
    this.vorgang = vorgang;
  }

  public Set<String> getHistoryClasses() {
    return historyClasses;
  }

  public void setHistoryClasses(Set<String> historyClasses) {
    this.historyClasses = historyClasses;
  }
}
