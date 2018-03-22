package de.fraunhofer.igd.klarschiff.dao;

import de.fraunhofer.igd.klarschiff.vo.Flaeche;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Die Dao-Klasse erlaubt die Verwaltung der Außendienst-Koordinatoren in der DB.
 *
 * @author Robert Voß (BFPI GmbH)
 */
@Repository
public class FlaechenDao {

  @PersistenceContext
  EntityManager em;
  
  public List<Flaeche> getAllFlaechen() {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT f FROM Flaeche f ");
    sql.append("ORDER BY f.kurzname");
    return em.createQuery(sql.toString(), Flaeche.class).getResultList();
  }

  @Transactional
  public Flaeche findByKurzname(String kurzname) {
    if (kurzname == null) {
      return null;
    }
    List<Flaeche> list = em.createQuery("select f from Flaeche f where f.kurzname like :kurzname", Flaeche.class)
      .setParameter("kurzname", kurzname).getResultList();
    if(list.isEmpty()) {
      return null;
    }
    return list.get(0);
  }

  /*
  @Transactional
  public List<String> findFlaechenByLogin(String login) {
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
    if (teams != null) {
      for (String team : teams) {
        AussendienstKoordinator ak = new AussendienstKoordinator();
        ak.setKoordinator(login);
        ak.setAussendienst(team);
        em.merge(ak);
      }
    }
    return true;
  }*/

}
