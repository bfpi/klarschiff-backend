package de.fraunhofer.igd.klarschiff.service.cluster;

import java.net.InetAddress;
import java.util.Set;

import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.commons.lang.StringUtils;

/**
 * Mit Hilfe der Util-Klasse können verschieden Daten des aktuellen Servers ermittelt werden können,
 * die eine Identifikation im Cluster ermöglichen.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class ClusterUtil {

  static String serverConnectorPort;
  static String serverIp;
  static String serverName;
  static String serverJvmRoute;

  /**
   * Gibt den Connector und den Port des aktuellen Servers zurück.
   *
   * @return Connector und Port des aktuellen Servers, z.B. http-bio-80.
   */
  public static String getServerConnectorPort() {
    if (serverConnectorPort == null) {
      try {
        Set<ObjectName> objectNames = MBeanServerFactory.findMBeanServer(null).get(0).queryNames(new ObjectName("Catalina:type=ThreadPool,*"), null);
        for (ObjectName objectName : objectNames) {
          String p = objectName.getKeyProperty("name");
          p = p.substring(1, p.length() - 1);
          if (p.startsWith("http")) {
            serverConnectorPort = p;
          }
        }
        if (serverConnectorPort == null) {
          String p = objectNames.iterator().next().getKeyProperty("name");
          p = p.substring(1, p.length() - 1);
          serverConnectorPort = p;
        }
      } catch (Exception e) {
      }
    }
    return serverConnectorPort;
  }

  /**
   * Gibt den in der <code>server.xml</code> des aktuellen Servers angegeben Wert der
   * <code>jvmRoute</code> zurück.
   *
   * @return Wert der <code>jvmRoute</code> in der <code>server.xml</code> des aktuellen Servers.
   */
  public static String getServerJvmRoute() {
    if (serverJvmRoute == null) {
      try {
        String p = (String) MBeanServerFactory.findMBeanServer(null).get(0).getAttribute(new ObjectName("Catalina:type=Engine"), "jvmRoute");
        //p = p.substring(1, p.length()-1);
        serverJvmRoute = p;
      } catch (Exception e) {
      }
    }
    return serverJvmRoute;
  }

  /**
   * Gibt die Liste der IPs des aktuellen Servers zurück.
   *
   * @return Liste der IPs des aktuellen Servers.
   */
  public static String getServerIp() {
    if (serverIp == null) {
      try {
        InetAddress addrs[] = InetAddress.getAllByName(getServerName());

        String ip = "";
        for (InetAddress addr : addrs) {
          if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
            ip = ip + addr.getHostAddress() + ", ";
          }
        }
        serverIp = StringUtils.isBlank(ip) ? "UNKNOWN" : ip.substring(0, ip.length() - 2);
      } catch (Exception e) {
      }
    }
    return serverIp;
  }

  /**
   * Gibt den Rechnernamen des aktuellen Servers zurück.
   *
   * @return Rechnername des aktuellen Servers.
   */
  public static String getServerName() {
    if (serverName == null) {
      try {
        serverName = InetAddress.getLocalHost().getHostName();
      } catch (Exception e) {
      }
    }
    return serverName;
  }
}
