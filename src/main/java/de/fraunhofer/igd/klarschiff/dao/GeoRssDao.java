package de.fraunhofer.igd.klarschiff.dao;

import de.fraunhofer.igd.klarschiff.vo.GeoRss;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

/**
 * Die Dao-Klasse erlaubt den Zugriff auf die GeoRSS-Einträge in der DB.
 *
 * @author Ricardo Oest (BFPI GmbH)
 */
@Repository
public class GeoRssDao {

  @PersistenceContext
  EntityManager entityManager;

  /**
   * Gibt den GeoRSS-Eintrag zurück.
   *
   * @param hash
   * @return GeoRss-Eintrag
   */
  public GeoRss findGeoRss(String hash) {
    String sql = "SELECT g.id, g.ovi, g.ideen, g.ideen_hauptkategorien, g.ideen_unterkategorien, g.probleme, g.probleme_hauptkategorien, g.probleme_unterkategorien FROM klarschiff_geo_rss g";
    sql += " WHERE md5(cast(g.id AS varchar)) = '" + hash + "'";
    return (GeoRss) entityManager.createNativeQuery(sql, GeoRss.class).getSingleResult();
  }
}
