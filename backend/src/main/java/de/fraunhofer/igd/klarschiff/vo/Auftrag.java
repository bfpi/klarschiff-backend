package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * VO zum Abbilden des Au�endienst-Auftrages f�r einen Vorgang
 *
 * @author Robert Vo� (BFPI GmbH)
 *
 */
@SuppressWarnings("serial")
@Entity
public class Auftrag implements Serializable {

  /* --------------- Attribute ----------------------------*/
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  /**
   * Vorgang f�r den Auftrag
   */
  @OneToOne
  @JoinColumn(name = "vorgang")
  private Vorgang vorgang;

  /**
   * Team f�r den Auftrag
   */
  @NotNull
  @Enumerated(EnumType.STRING)
  private String team;

  /**
   * Zeitpunkt der Bearbeitung
   */
  @NotNull
  @Temporal(TemporalType.TIMESTAMP)
  @DateTimeFormat(style = "S-")
  private Date datum;

  /**
   * Priorit�t
   */
  @NotNull
  @Enumerated(EnumType.STRING)
  private EnumPrioritaet prioritaet;

  /* --------------- GET + SET ----------------------------*/
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Vorgang getVorgang() {
    return vorgang;
  }

  public void setVorgang(Vorgang vorgang) {
    this.vorgang = vorgang;
  }

  public String getTeam() {
    return team;
  }

  public void setTeam(String team) {
    this.team = team;
  }

  public Date getDatum() {
    return datum;
  }

  public void setDatum(Date datum) {
    this.datum = datum;
  }

  public EnumPrioritaet getPrioritaet() {
    return prioritaet;
  }

  public void setPrioritaet(EnumPrioritaet prioritaet) {
    this.prioritaet = prioritaet;
  }
}
