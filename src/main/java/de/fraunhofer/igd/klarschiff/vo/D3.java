package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;

/**
 * VO für die D3-Akten der Kategorien der Vorgänge. <br/>
 * Hauptkategorien haben ein <code>typ</code> aber kein <code>parent</code><br/>
 * Unterkategorien haben keinen <code>typ</code> aber ein <code>parent</code>
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
@Entity
public class D3 implements Serializable {

  /**
   * Id des D3-Akte
   */
  @Id
  @TableGenerator(
    name = "D3Sequence",
    initialValue = 1,
    allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "D3Sequence")
  private Long id;

  /**
   * URL
   */
  private String url;

  /**
   * DCC
   */
  private String dcc;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getDcc() {
    return dcc;
  }

  public void setDcc(String dcc) {
    this.dcc = dcc;
  }

}
