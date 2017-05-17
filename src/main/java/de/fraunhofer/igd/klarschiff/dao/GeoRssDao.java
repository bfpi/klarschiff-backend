/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fraunhofer.igd.klarschiff.dao;

import de.fraunhofer.igd.klarschiff.vo.GeoRss;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import org.apache.commons.lang.StringUtils;


/**
 *
 * @author roest
 */
@Repository
public class GeoRssDao {
  
  @PersistenceContext
  EntityManager entityManager;
  
  public GeoRss findGeoRss(String hash) {
    String sql = "SELECT g.id, g.ovi, g.ideen, g.ideen_hauptkategorien, g.ideen_unterkategorien, g.probleme, g.probleme_hauptkategorien, g.probleme_unterkategorien FROM klarschiff_geo_rss g";
    sql += " WHERE md5(cast(g.id AS varchar)) = '" + hash + "'";
    return (GeoRss) entityManager.createNativeQuery(sql, GeoRss.class).getSingleResult();
  }
}
