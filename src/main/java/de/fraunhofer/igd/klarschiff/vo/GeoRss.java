package de.fraunhofer.igd.klarschiff.vo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

/**
 * VO zum Ablegen von individuell definierten GeoRss
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Entity
public class GeoRss {

  /* --------------- Attribute ----------------------------*/
  /**
   * Id des Feeds
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  /**
   * überwachte Fläche
   */
  @Type(type = "org.hibernate.spatial.GeometryType")
  private MultiPolygon ovi;

  /**
   * Probleme überwachen?
   */
  private boolean probleme;

  /**
   * Liste der überwachten Hauptkategorien bei den Problemen
   */
  private String problemeHauptkategorien;

  /**
   * Liste der überwachten Unterkategorien bei den Problemen
   */
  private String problemeUnterkategorien;

  /**
   * Ideen überwachen?
   */
  private boolean ideen;

  /**
   * Liste der überwachten Hauptkategorien bei den Ideen
   */
  private String ideenHauptkategorien;

  /**
   * Liste der überwachten Unterkategorien bei den Ideen
   */
  private String ideenUnterkategorien;

  /* --------------- transient ----------------------------*/
  @Transient
  private static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 25833);

  @Transient
  private static WKTReader wktReader = new WKTReader(geometryFactory);

  @Transient
  private static WKTWriter wktWriter = new WKTWriter();

  /**
   * Setzen der überwachten Fläche als WKT
   *
   * @param oviWkt Fläche als WKT
   * @throws Exception
   */
  @Transient
  public void setOviWkt(String oviWkt) throws Exception {
    ovi = (StringUtils.isBlank(oviWkt)) ? null : (MultiPolygon) wktReader.read(oviWkt);
  }

  /**
   * Lesen der überwachten Fläche als WKT
   *
   * @return überwachte Fläche als WKT
   */
  @Transient
  public String getOviWkt() {
    return (ovi == null) ? null : wktWriter.write(ovi);
  }

  /* --------------- GET + SET ----------------------------*/

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public MultiPolygon getOvi() {
    return ovi;
  }

  public void setOvi(MultiPolygon ovi) {
    this.ovi = ovi;
  }

  public boolean getProbleme() {
    return probleme;
  }

  public void setProbleme(boolean probleme) {
    this.probleme = probleme;
  }

  public String getProblemeHauptkategorien() {
    return problemeHauptkategorien;
  }

  public void setProblemeHauptkategorien(String problemeHauptkategorien) {
    this.problemeHauptkategorien = problemeHauptkategorien;
  }

  public String getProblemeUnterkategorien() {
    return problemeUnterkategorien;
  }

  public void setProblemeUnterkategorien(String problemeUnterkategorien) {
    this.problemeUnterkategorien = problemeUnterkategorien;
  }

  public boolean getIdeen() {
    return ideen;
  }

  public void setIdeen(boolean ideen) {
    this.ideen = ideen;
  }

  public String getIdeenHauptkategorien() {
    return ideenHauptkategorien;
  }

  public void setIdeenHauptkategorien(String ideenHauptkategorien) {
    this.ideenHauptkategorien = ideenHauptkategorien;
  }

  public String getIdeenUnterkategorien() {
    return ideenUnterkategorien;
  }

  public void setIdeenUnterkategorien(String ideenUnterkategorien) {
    this.ideenUnterkategorien = ideenUnterkategorien;
  }
}
