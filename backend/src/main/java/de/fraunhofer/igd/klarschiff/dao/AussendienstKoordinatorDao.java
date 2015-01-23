package de.fraunhofer.igd.klarschiff.dao;

import de.fraunhofer.igd.klarschiff.vo.AussendienstKoordinator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Die Dao-Klasse erlaubt die Verwaltung der Auﬂendienst-Koordinatoren in der
 * DB.
 *
 * @author Robert Voﬂ (BFPI GmbH)
 */
@Repository
public class AussendienstKoordinatorDao {

  @PersistenceContext
  EntityManager em;

  @Transactional
  public List<String> findAussendienstByLogin(String login) {
    if (login == null) {
      return null;
    }
    return em.createQuery("select aussendienst from AussendienstKoordinator ak where ak.koordinator=:login order by aussendienst", String.class).setParameter("login", login).getResultList();
  }

  @Transactional
  public int resetAussendienstByLogin(String login) {
    if (login == null) {
      return 0;
    }
    return em.createNativeQuery("delete from klarschiff_aussendienst_koordinator ak where ak.koordinator=:login", String.class).setParameter("login", login).executeUpdate();
  }

  public boolean setTeamsForLogin(String login, String[] teams) {
    if(teams != null) {
      for (String team : teams) {
        AussendienstKoordinator ak = new AussendienstKoordinator();
        ak.setKoordinator(login);
        ak.setAussendienst(team);
        em.merge(ak);
      }
    }
    return true;
  }

}
