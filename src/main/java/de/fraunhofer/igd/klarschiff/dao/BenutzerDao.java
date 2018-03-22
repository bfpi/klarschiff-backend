package de.fraunhofer.igd.klarschiff.dao;

import de.fraunhofer.igd.klarschiff.vo.Benutzer;
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
public class BenutzerDao {

  @PersistenceContext
  EntityManager em;

  /**
   * Das Objekt wird in der DB gespeichert.
   *
   * @param o Das zu speichernde Objekt
   */
  @Transactional
  public void persist(Object o) {
    em.persist(o);
    em.flush();
  }

  /**
   * Das Objekt wird in der DB gespeichert.
   *
   * @param o Das zu speichernde Objekt
   */
  @Transactional
  public void merge(Object o) {
    em.merge(o);
    em.flush();
  }

  @Transactional
  public Benutzer findByBenutzername(String benutzername) {
    if (benutzername == null) {
      return null;
    }
    List<Benutzer> list = em.createQuery("select b from Benutzer b where b.benutzername like :benutzername", Benutzer.class)
      .setParameter("benutzername", benutzername).getResultList();
    if(list.isEmpty()) {
      return null;
    }
    return list.get(0);
  }
  
  
  
  /*
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
