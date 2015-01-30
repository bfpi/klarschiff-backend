package de.fraunhofer.igd.klarschiff.dao;

import de.fraunhofer.igd.klarschiff.vo.Auftrag;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Die Dao-Klasse erlaubt das Verwalten der Auftr‰ge in der DB.
 *
 * @author Robert Voﬂ (BFPI GmbH)
 */
@Repository
public class AuftragDao {

  @PersistenceContext
  EntityManager em;

  @Transactional
  public List<Auftrag> alleAuftraege() {
    return em.createQuery("SELECT a FROM Auftrag a, Vorgang v WHERE "
            + "a.vorgang = v.id "
            + "order by a.prioritaet, v.datum", Auftrag.class)
            .getResultList();
  }

  @Transactional
  public List<Auftrag> findAuftraegeByTeam(String team) {
    return em.createQuery("SELECT a FROM Auftrag a, Vorgang v WHERE "
            + "a.team = :team and a.vorgang = v.id "
            + "order by a.prioritaet, v.datum", Auftrag.class)
            .setParameter("team", team)
            .getResultList();
  }

  @Transactional
  public List<Auftrag> findAuftraegeByTeamAndDate(String team, Date datum) {
    return em.createQuery("SELECT a FROM Auftrag a, Vorgang v WHERE "
            + "a.team = :team AND a.datum = :datum and a.vorgang = v.id "
            + "order by a.prioritaet, v.datum", Auftrag.class)
            .setParameter("team", team)
            .setParameter("datum", datum)
            .getResultList();
  }

  @Transactional
  public Auftrag find(Integer id) {
    return em.createQuery("SELECT a FROM Auftrag a WHERE a.id = :id", Auftrag.class)
            .setParameter("id", id)
            .getSingleResult();
  }

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
