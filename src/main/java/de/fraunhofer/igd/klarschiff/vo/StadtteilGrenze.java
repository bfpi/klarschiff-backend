package de.fraunhofer.igd.klarschiff.vo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * VO zum Abbilden der Stadtteilgrenzen/Ortsteilgrenzen
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Entity
public class StadtteilGrenze {

  /* --------------- Attribute ----------------------------*/
  /**
   * Id des Stadtteils
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  /**
   * Name des Stadtteils
   */
  private String name;

  /**
   * Stadtteilgrenze
   */
  @Type(type = "org.hibernate.spatial.GeometryType")
  private MultiPolygon grenze;

  /* --------------- transient ----------------------------*/
  @Transient
  private static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 25833);

  @Transient
  private static WKTReader wktReader = new WKTReader(geometryFactory);

  @Transient
  private static WKTWriter wktWriter = new WKTWriter();

  /**
   * Setzen der Stadtteilgrenze als WKT
   *
   * @param grenzeWkt Stadtteilgrenze als WKT
   * @throws Exception
   */
  @Transient
  public void setGrenzeWkt(String grenzeWkt) throws Exception {
    grenze = (StringUtils.isBlank(grenzeWkt)) ? null : (MultiPolygon) wktReader.read(grenzeWkt);
  }

  /**
   * Lesen der Stadtteilgrenze als WKT
   *
   * @return Stadtteilgrenze als WKT
   */
  @Transient
  public String getGrenzeWkt() {
    return (grenze == null) ? null : wktWriter.write(grenze);
  }

  /* --------------- GET + SET ----------------------------*/
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public MultiPolygon getGrenze() {
    return grenze;
  }

  public void setGrenze(MultiPolygon grenze) {
    this.grenze = grenze;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
