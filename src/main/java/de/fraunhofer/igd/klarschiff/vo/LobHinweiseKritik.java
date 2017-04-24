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
 * VO zum Abbilden von Lob, Hinweisen oder Kritik zu einem Vorgang.
 *
 * @author Sebastian Gutzeit (Hansestadt Rostock)
 */
@SuppressWarnings("serial")
@Entity
public class LobHinweiseKritik implements Serializable {

  /* --------------- Attribute ----------------------------*/
  /**
   * Id
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  /**
   * Vorgang, zu dem Lob, Hinweise oder Kritik gehören
   */
  @ManyToOne
  private Vorgang vorgang;

  /**
   * E-Mail-Adresse des Senders
   */
  @Size(max = 300)
  private String autorEmail;

  /**
   * E-Mail-Adresse des Empfängers
   */
  @Size(max = 300)
  private String empfaengerEmail;

  /**
   * Freitext
   */
  @Lob
  @Type(type = "org.hibernate.type.TextType")
  private String freitext;

  /**
   * Erstellungszeit
   */
  @NotNull
  @Temporal(TemporalType.TIMESTAMP)
  @DateTimeFormat(style = "S-")
  private Date datum;

  /* --------------- GET + SET ----------------------------*/
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

  public Long getId() {
    return id;
  }

  public String getFreitext() {
    return freitext;
  }

  public void setFreitext(String freitext) {
    this.freitext = freitext;
  }

  public String getAutorEmail() {
    return this.autorEmail;
  }

  public void setAutorEmail(String autorEmail) {
    this.autorEmail = autorEmail;
  }

  public String getEmpfaengerEmail() {
    return this.empfaengerEmail;
  }

  public void setEmpfaengerEmail(String empfaengerEmail) {
    this.empfaengerEmail = empfaengerEmail;
  }
}
