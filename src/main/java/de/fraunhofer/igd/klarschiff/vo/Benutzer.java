package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.TableGenerator;

/**
 * VO zum Abbilden eines Benutzers
 *
 * @author Robert Voß (BFPI GmbH)
 *
 */
@SuppressWarnings("serial")
@Entity
public class Benutzer implements Serializable {

  /* --------------- Attribute ----------------------------*/
  @Id
  @TableGenerator(
    name = "BenutzerSequence",
    table = "klarschiff_BenutzerSequence",
    initialValue = 1,
    allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "BenutzerSequence")
  private Integer id;

  /**
   * Benutzername
   */
  private String benutzername;

  @ManyToMany()
  @JoinTable(
    name = "benutzer_flaeche",
    joinColumns = {
      @JoinColumn(name = "benutzer_id")},
    inverseJoinColumns = {
      @JoinColumn(name = "flaeche_id")}
  )
  private List<Flaeche> flaechen = new ArrayList<Flaeche>();

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getBenutzername() {
    return benutzername;
  }

  public void setBenutzername(String benutzername) {
    this.benutzername = benutzername;
  }

  public List<Flaeche> getFlaechen() {
    return flaechen;
  }

  public void setFlaechen(List<Flaeche> flaechen) {
    this.flaechen = flaechen;
  }

}
