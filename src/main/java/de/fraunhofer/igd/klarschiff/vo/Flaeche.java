package de.fraunhofer.igd.klarschiff.vo;

import com.vividsolutions.jts.geom.MultiPolygon;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import org.hibernate.annotations.Type;

/**
 * VO zum Abbilden eine Fläche
 *
 * @author Robert Voß (BFPI GmbH)
 *
 */
@SuppressWarnings("serial")
@Entity
public class Flaeche implements Serializable {

  /* --------------- Attribute ----------------------------*/
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  /**
   * Kurzname
   */
  private String kurzname;

  /**
   * Langname
   */
  private String langname;

  /**
   * Stadtteilgrenze
   */
  @Type(type = "org.hibernate.spatial.GeometryType")
  private MultiPolygon flaeche;

  @ManyToMany(mappedBy = "flaechen")
  private List<Benutzer> benutzer = new ArrayList<Benutzer>();

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getKurzname() {
    return kurzname;
  }

  public void setKurzname(String kurzname) {
    this.kurzname = kurzname;
  }

  public String getLangname() {
    return langname;
  }

  public void setLangname(String langname) {
    this.langname = langname;
  }

  public MultiPolygon getFlaeche() {
    return flaeche;
  }

  public void setFlaeche(MultiPolygon flaeche) {
    this.flaeche = flaeche;
  }

  public List<Benutzer> getBenutzer() {
    return benutzer;
  }

  public void setBenutzer(List<Benutzer> benutzer) {
    this.benutzer = benutzer;
  }

}
