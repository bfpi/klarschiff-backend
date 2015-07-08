package de.fraunhofer.igd.klarschiff.vo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * VO zum Abbilden der Trash-E-Mails.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Entity
public class Trashmail {

  /* --------------- Attribute ----------------------------*/
  /**
   * Id der Trash-E-Mail
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  /**
   * Pattern der Trash-E-Mail
   */
  private String pattern;

  /* --------------- GET + SET ----------------------------*/
  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPattern() {
    return this.pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }
}
