package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * VO zum Abbilden der laufenden Serverinstanzen in einem Cluster.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
@Entity
public class Server implements Serializable {

  /* --------------- Attribute ----------------------------*/
  /**
   * Rechnername der Serverinstanz
   */
  @Id
  String name;

  /**
   * ConnectorPort der Serverinstanz
   */
  @Id
  String connectorPort;

  /**
   * Liste der IPs der Serverinstanz
   */
  String ip;

  /**
   * Wert des Attributes <code>jvmRoute</code> in der <code>server.xml</code> der Serverinstanz
   */
  String jvmRoute;

  /**
   * Zeitpunkt der letzen Registrierung/Benachrichtung der Serverinstanz. Anhand des Datums werden
   * veraltete Einträge in der DB gelöscht.
   */
  Date datum;

  /* --------------- GET + SET ----------------------------*/
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getConnectorPort() {
    return connectorPort;
  }

  public void setConnectorPort(String connectorPort) {
    this.connectorPort = connectorPort;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public Date getDatum() {
    return datum;
  }

  public void setDatum(Date datum) {
    this.datum = datum;
  }

  public String getJvmRoute() {
    return jvmRoute;
  }

  public void setJvmRoute(String jvmRoute) {
    this.jvmRoute = jvmRoute;
  }
}
