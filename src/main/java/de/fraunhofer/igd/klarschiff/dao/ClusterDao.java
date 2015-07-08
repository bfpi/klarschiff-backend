package de.fraunhofer.igd.klarschiff.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.fraunhofer.igd.klarschiff.service.cluster.ClusterUtil;
import de.fraunhofer.igd.klarschiff.vo.Server;

/**
 * Die Dao-Klasse dient zum Aktualisieren und Lesen Serverdaten in einem Cluster mit Hilfe der DB
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Repository
public class ClusterDao {

  @PersistenceContext
  EntityManager em;

  /**
   * Löscht allte Einträge in der DB und registriert bzw. aktualisiert den Eintrag des aktuellen
   * Servers in der DB
   */
  @Transactional
  public void notifyAliveServer() {
    String serverName = ClusterUtil.getServerName();
    String serverConnectorPort = ClusterUtil.getServerConnectorPort();

    Date date = new Date();
    //alte löschen
    em.createQuery("DELETE FROM Server WHERE datum<:datum").setParameter("datum", DateUtils.addSeconds(date, -30)).executeUpdate();

    //aktualisieren oder neu?
    Server server = null;
    try {
      server = (Server) em.createQuery("SELECT v FROM Server v WHERE v.name=:name AND v.connectorPort=:connectorPort").setParameter("name", serverName).setParameter("connectorPort", serverConnectorPort).getSingleResult();
    } catch (Exception e) {
    }

    if (server != null) {
      //aktualisieren
      server.setDatum(date);
      em.merge(server);
    } else {
      //neu
      server = new Server();
      server.setName(serverName);
      server.setConnectorPort(serverConnectorPort);
      server.setIp(ClusterUtil.getServerIp());
      server.setJvmRoute(ClusterUtil.getServerJvmRoute());
      server.setDatum(date);
      em.persist(server);
    }
  }

  /**
   * Gibt eine Liste mit den in der DB gelisteten Servern zurück.
   */
  @SuppressWarnings("unchecked")
  public List<Server> getAliveServerList() {
    return (List<Server>) em.createQuery("SELECT v FROM Server v").getResultList();
  }
}
