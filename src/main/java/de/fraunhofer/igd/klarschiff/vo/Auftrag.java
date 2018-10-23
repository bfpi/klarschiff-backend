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
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * VO zum Abbilden des Außendienst-Auftrages für einen Vorgang
 *
 * @author Robert Voß (BFPI GmbH)
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
   * Vorgang für den Auftrag
   */
  @OneToOne
  @JoinColumn(name = "vorgang")
  private Vorgang vorgang;

  /**
   * Team für den Auftrag
   */
  private String team;

  /**
   * Zeitpunkt der Bearbeitung
   */
  @Temporal(TemporalType.TIMESTAMP)
  @DateTimeFormat(style = "M-")
  private Date datum;

  /**
   * Priorität
   */
  private Integer prioritaet;

  /**
   * Status
   */
  @Enumerated(EnumType.STRING)
  private EnumAuftragStatus status;

  @PrePersist
  public void prePersist() {
    if (status == null) {
      status = EnumAuftragStatus.nicht_abgehakt;
    }
  }

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

  public Integer getPrioritaet() {
    return prioritaet;
  }

  public void setPrioritaet(Integer prioritaet) {
    this.prioritaet = prioritaet;
  }

  public EnumAuftragStatus getStatus() {
    return status;
  }

  public void setStatus(EnumAuftragStatus status) {
    this.status = status;
  }
}
