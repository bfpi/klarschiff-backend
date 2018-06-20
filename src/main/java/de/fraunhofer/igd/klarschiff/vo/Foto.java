package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * VO zum Abbilden von Fotos.
 *
 * @author Robert Voß (BFPI GmbH)
 */
@SuppressWarnings("serial")
@Entity
public class Foto implements Serializable {

  /* --------------- Attribute ----------------------------*/
  /**
   * Id des Fotos
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  /**
   * Vorgang zu dem das Foto gehört
   */
  @ManyToOne
  private Vorgang vorgang;

  /**
   * Hash zum Bestätigen des Fotos
   */
  @Size(max = 32)
  private String hash;

  /**
   * Foto
   */
  private String fotoGross;

  /**
   * Foto
   */
  private String fotoNormal;

  /**
   * Foto des Vorganges als Vorschaubild
   */
  private String fotoThumb;

  /**
   * Erstellungszeit des Fotos
   */
  @NotNull
  @Temporal(TemporalType.TIMESTAMP)
  @DateTimeFormat(style = "S-")
  private Date datum;

  /**
   * Zeitpunkt der Bestätigung des Fotos
   */
  @Temporal(TemporalType.TIMESTAMP)
  @DateTimeFormat(style = "S-")
  private Date datumBestaetigung;

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

  public String getFotoGross() {
    return fotoGross;
  }

  public void setFotoGross(String fotoGross) {
    this.fotoGross = fotoGross;
  }

  public String getFotoNormal() {
    return fotoNormal;
  }

  public void setFotoNormal(String fotoNormal) {
    this.fotoNormal = fotoNormal;
  }

  public String getFotoThumb() {
    return fotoThumb;
  }

  public void setFotoThumb(String fotoThumb) {
    this.fotoThumb = fotoThumb;
  }

  public String getAutorEmail() {
    return this.autorEmail;
  }

  public void setAutorEmail(String autorEmail) {
    this.autorEmail = autorEmail;
  }
}
