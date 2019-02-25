package de.fraunhofer.igd.klarschiff.dao;

import de.fraunhofer.igd.klarschiff.vo.AussendienstKoordinator;
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
public class AussendienstKoordinatorDao {

  @PersistenceContext
  EntityManager em;

  /**
   * Gibt eine Liste aller Außendienst-Teams des übergebenen Benutzer-Logins zurück.
   * 
   * @param login Login des Benutzers
   * @return Liste der Außendienst-Teams
   */
  @Transactional
  public List<String> findAussendienstByLogin(String login) {
    if (login == null) {
      return null;
    }
    return em.createQuery("select aussendienst from AussendienstKoordinator ak where ak.koordinator=:login order by aussendienst", String.class).setParameter("login", login).getResultList();
  }

  /**
   * Setzt die Zugehörigkeit der Außendiens-Teams zurück.
   * 
   * @param login Login des Benutzers
   * @return Anzahl der gelöschten Einträge
   */
  @Transactional
  public int resetAussendienstByLogin(String login) {
    if (login == null) {
      return 0;
    }
    return em.createNativeQuery("delete from klarschiff_aussendienst_koordinator ak where ak.koordinator=:login", String.class).setParameter("login", login).executeUpdate();
  }

  /**
   * Speichert die Teams für den der Benutzer als Koordinator zustängig ist.
   * 
   * @param login Login des zu Speichernden Benutzers
   * @param teams Liste der zuständigen Außendiens-Teams
   * @return <code>true</code> - Teams gespeichern
   */
  @Transactional
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
  }

}
