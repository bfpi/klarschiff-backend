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
 * VO zum Abbilden der Stadtgrenze.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Entity
public class StadtGrenze {

  /* --------------- Attribute ----------------------------*/
  /**
   * Id der Stadtgrenze
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  /**
   * Stadtgrenze
   */
  @Type(type = "org.hibernatespatial.GeometryUserType")
  private MultiPolygon grenze;

  /* --------------- transient ----------------------------*/
  @Transient
  private static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 25833);

  @Transient
  private static WKTReader wktReader = new WKTReader(geometryFactory);

  @Transient
  private static WKTWriter wktWriter = new WKTWriter();

  /**
   * Setzen der Stadtgrenze als WKT
   *
   * @param grenzeWkt Stadtgrenze als WKT
   */
  @Transient
  public void setGrenzeWkt(String grenzeWkt) throws Exception {
    grenze = (StringUtils.isBlank(grenzeWkt)) ? null : (MultiPolygon) wktReader.read(grenzeWkt);
  }

  /**
   * Lesen der Stadtgrenze als WKT
   *
   * @return stadtgrenze als WKT
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
}
