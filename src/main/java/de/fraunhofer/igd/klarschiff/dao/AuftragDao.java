package de.fraunhofer.igd.klarschiff.dao;

import de.fraunhofer.igd.klarschiff.vo.Auftrag;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import de.fraunhofer.igd.klarschiff.web.AussendienstCommand;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Die Dao-Klasse erlaubt das Verwalten der Aufträge in der DB.
 *
 * @author Robert Voß (BFPI GmbH)
 */
@Repository
public class AuftragDao {

  @PersistenceContext
  EntityManager em;

  /**
   * Gibt eine Liste aller Aufträge zurück.
   *
   * @return Liste der Aufträge
   */
  @Transactional
  public List<Auftrag> alleAuftraege() {
    return em.createQuery("SELECT a FROM Auftrag a, Vorgang v WHERE "
      + "a.vorgang = v.id "
      + "order by a.prioritaet, v.datum", Auftrag.class)
      .getResultList();
  }

  /**
   * Gibt eine Liste aller Aufträge des übergebenen Teams zurück.
   *
   * @param team Name des Aussendiens-Teams
   * @return Liste der Aufträge
   */
  @Transactional
  public List<Auftrag> findAuftraegeByTeam(String team) {
    return em.createQuery("SELECT a FROM Auftrag a, Vorgang v WHERE "
      + "a.team = :team and a.vorgang = v.id "
      + "order by a.prioritaet, v.datum", Auftrag.class)
      .setParameter("team", team)
      .getResultList();
  }

  /**
   * Gibt eine Liste aller Aufträge des übergebenen Teams für einen Tag zurück.
   *
   * @param team Name des Aussendiens-Teams
   * @param datum Datum des benötigten Tages
   * @return Liste der Aufträge
   */
  @Transactional
  public List<Auftrag> findAuftraegeByTeamAndDate(String team, Date datum) {
    return findAuftraegeByTeamAndDateAndAuswahl(team, datum, null);
  }

  /**
   * Gibt eine Liste aller Aufträge des übergebenen Teams für einen Tag zurück.
   *
   * @param team Name des Aussendiens-Teams
   * @param datum Datum des benötigten Tages
   * @param auswahl Auswahl der Vorgänge als ID-Liste
   * @return Liste der Aufträge
   */
  @Transactional
  public List<Auftrag> findAuftraegeByTeamAndDateAndAuswahl(String team, Date datum, Long[] auswahl) {

    String sql = "SELECT a FROM Auftrag a, Vorgang v WHERE "
      + "a.team = :team AND a.datum = :datum and a.vorgang = v.id ";
    if (auswahl != null && auswahl.length > 0) {
        sql += "and v.id in (" + StringUtils.join(auswahl, ",") + ") ";
    }
    sql += "order by a.prioritaet, v.datum";

    return em.createQuery(sql, Auftrag.class)
      .setParameter("team", team)
      .setParameter("datum", datum)
      .getResultList();
  }

  /**
   * Holt den Auftrag anhand der ID
   *
   * @param id ID des Auftrags
   * @return Auftrag
   */
  @Transactional
  public Auftrag find(Integer id) {
    return em.createQuery("SELECT a FROM Auftrag a WHERE a.id = :id", Auftrag.class)
      .setParameter("id", id)
      .getSingleResult();
  }

  /**
   * Gibt eine Liste aller Aufträge der übergebenen Vorgänge zurück.
   *
   * @param vorgaenge
   * @return Liste der Aufträge
   */
  @Transactional
  public List<Auftrag> findAuftraegeByVorgaenge(List<Vorgang> vorgaenge) {
    if (vorgaenge == null) {
      return null;
    }
    return em.createQuery("select a from Auftrag a, Vorgang v where "
      + "a.vorgang in (:ids) and a.vorgang = v.id "
      + "order by a.prioritaet, v.datum", Auftrag.class).setParameter("ids", vorgaenge).getResultList();
  }
}
