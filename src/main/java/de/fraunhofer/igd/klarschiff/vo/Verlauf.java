package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * VO zum Abbilden des Verlaufes für einen Vorgang
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
@Entity
public class Verlauf implements Serializable {

  /* --------------- Attribute ----------------------------*/
  /**
   * Id der Verlaufeintrages
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  /**
   * Vorgang zu dem der Verlaufeintrag gehört
   */
  @ManyToOne(cascade = CascadeType.PERSIST)
  @JoinColumn
  private Vorgang vorgang;

  /**
   * Zeitpunkt des Verlaufeintrages
   */
  @Version
  @Temporal(TemporalType.TIMESTAMP)
  @DateTimeFormat(style = "S-")
  private Date datum;

  /**
   * Id des Benutzers, der den Verlaufeintrag verantwortet
   */
  private String nutzer;

  /**
   * Typ des Verlaufeintrages
   */
  @NotNull
  @Enumerated(EnumType.STRING)
  private EnumVerlaufTyp typ;

  /**
   * alter Wert (ist abhängig von Typ des Verlaufeintrages)
   */
  private String wertAlt;

  /**
   * alter Wert (ist abhängig von Typ des Verlaufeintrages)
   */
  private String wertNeu;

  /* --------------- GET + SET ----------------------------*/
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Vorgang getVorgang() {
    return vorgang;
  }

  public void setVorgang(Vorgang vorgang) {
    this.vorgang = vorgang;
  }

  public Date getDatum() {
    return datum;
  }

  public void setDatum(Date datum) {
    this.datum = datum;
  }

  public EnumVerlaufTyp getTyp() {
    return typ;
  }

  public void setTyp(EnumVerlaufTyp typ) {
    this.typ = typ;
  }

  public String getWertAlt() {
    return wertAlt;
  }

  public void setWertAlt(String wertAlt) {
    this.wertAlt = wertAlt;
  }

  public String getWertNeu() {
    return wertNeu;
  }

  public void setWertNeu(String wertNeu) {
    this.wertNeu = wertNeu;
  }

  public String getNutzer() {
    return nutzer;
  }

  public void setNutzer(String nutzer) {
    this.nutzer = nutzer;
  }
}
