package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * VO zum Abbilden von Missbrauchsmeldungen.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
@Entity
public class Missbrauchsmeldung implements Serializable {

  /* --------------- Attribute ----------------------------*/
  /**
   * Id der Missbrauchsmeldung
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  /**
   * Vorgang zu dem die Missbrauchsmeldung gehört
   */
  @ManyToOne
  private Vorgang vorgang;

  /**
   * Hash zum Bestätigen der Missbrauchsmeldung
   */
  @Size(max = 32)
  private String hash;

  /**
   * Text der Missbrauchsmeldung
   */
  @Lob
  @Type(type = "org.hibernate.type.TextType")
  private String text;

  /**
   * Erstellungszeit der Missbrauchsmeldung
   */
  @NotNull
  @Temporal(TemporalType.TIMESTAMP)
  @DateTimeFormat(style = "S-")
  private Date datum;

  /**
   * Zeitpunkt der Bestätigung der Missbrauchsmeldung
   */
  @Temporal(TemporalType.TIMESTAMP)
  @DateTimeFormat(style = "S-")
  private Date datumBestaetigung;

  /**
   * Zeitpunkt der Abbarbeitung der Missbrauchsmeldung
   */
  @Temporal(TemporalType.TIMESTAMP)
  @DateTimeFormat(style = "S-")
  private Date datumAbarbeitung;

  /**
   * E-Mail-Adresse des Senders
   */
  @Size(max = 300)
  private String autorEmail;

  /* --------------- GET + SET ----------------------------*/
  public Vorgang getVorgang() {
    return vorgang;
  }

  public void setVorgang(Vorgang vorgang) {
    this.vorgang = vorgang;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public Date getDatum() {
    return datum;
  }

  public void setDatum(Date datum) {
    this.datum = datum;
  }

  public Date getDatumBestaetigung() {
    return datumBestaetigung;
  }

  public void setDatumBestaetigung(Date datumBestaetigung) {
    this.datumBestaetigung = datumBestaetigung;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Date getDatumAbarbeitung() {
    return datumAbarbeitung;
  }

  public void setDatumAbarbeitung(Date datumAbarbeitung) {
    this.datumAbarbeitung = datumAbarbeitung;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text.trim();
  }

  public String getAutorEmail() {
    return this.autorEmail;
  }

  public void setAutorEmail(String autorEmail) {
    this.autorEmail = autorEmail;
  }
}
