package de.fraunhofer.igd.klarschiff.dao;

import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.security.User;
import de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.Verlauf;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Die Dao-Klasse erlaubt das Verwalten der Verlauf-Daten der Vorgänge in der DB.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Repository
public class VerlaufDao {

  @PersistenceContext
  EntityManager em;

  @Autowired
  SecurityService securityService;

  @Autowired
  VorgangDao vorgangDao;

  /**
   * Das Objekt wird in der DB gespeichert.
   *
   * @param verlauf Das zu speichernde Objekt
   */
  @Transactional
  public void persist(Verlauf verlauf) {
    em.persist(verlauf);
  }

  /**
   * Das Objekt wird in der DB gespeichert.
   *
   * @param verlauf Das zu speichernde Objekt
   */
  @Transactional
  public void merge(Verlauf verlauf) {
    em.merge(verlauf);
  }

  /**
   * Fügt zu einem Vorgang neue Verlaufswerte hinzu, ohne diese in der DB zu speichern.
   *
   * @param vorgang Vorgang zu dem die Verlaufswerte hinzugefügt werden sollen
   * @param typ Typ des Verlaufs
   * @param wertAlt Alter Wert
   * @param wertNeu Neuer Wert
   * @return Verlaufswerte
   */
  public Verlauf addVerlaufToVorgang(Vorgang vorgang, EnumVerlaufTyp typ, String wertAlt, String wertNeu) {
    return addVerlaufToVorgang(vorgang, typ, wertAlt, wertNeu, null);
  }

  /**
   * Fügt zu einem Vorgang neue Verlaufswerte hinzu, ohne diese in der DB zu speichern.
   *
   * @param vorgang Vorgang zu dem die Verlaufswerte hinzugefügt werden sollen
   * @param typ Typ des Verlaufs
   * @param wertAlt Alter Wert
   * @param wertNeu Neuer Wert
   * @param nutzer_email
   * @return Verlaufswerte
   */
  public Verlauf addVerlaufToVorgang(Vorgang vorgang, EnumVerlaufTyp typ, String wertAlt,
    String wertNeu, String nutzer_email) {

    Verlauf verlauf = new Verlauf();
    verlauf.setVorgang(vorgang);
    if (nutzer_email != null) {
      User user = securityService.getUserByEmail(nutzer_email);
      if (user != null) {
        verlauf.setNutzer(user.getName());
      } else {
        verlauf.setNutzer(nutzer_email);
      }
    } else {
      try {
        verlauf.setNutzer(securityService.getCurrentUser().getName());
      } catch (Exception e) {
      }
    }
    if (verlauf.getNutzer() != null && verlauf.getNutzer().length() > 0) {
      vorgang.setLetzterBearbeiter(verlauf.getNutzer());
    }
    verlauf.setTyp(typ);
    verlauf.setWertAlt(wertAlt);
    verlauf.setWertNeu(wertNeu);
    vorgang.getVerlauf().add(verlauf);
    return verlauf;
  }

  /**
   * Liefert die Verlaufswerte des übergebenen Vorgangs.
   *
   * @param vorgang Vorgang zu dem die Verlaufswerte angezeigt werden sollen
   * @param page Seite
   * @param size Anzahl pro Seite
   * @return Verlaufswerte
   */
  @Transactional
  public List<Verlauf> findVerlaufForVorgang(Vorgang vorgang, Integer page, Integer size) {
    TypedQuery<Verlauf> query = em.createQuery("SELECT o FROM Verlauf o "
      + "WHERE o.vorgang = :vorgang ORDER BY o.datum DESC",
      Verlauf.class).setParameter("vorgang", vorgang);

    if (page != null && size != null) {
      query.setFirstResult((page - 1) * size);
    }
    if (size != null) {
      query.setMaxResults(size);
    }

    return query.getResultList();
  }

  /**
   * Findet denjenigen Benutzernamen aus einer gegebenen Liste von Benutzernamen, der gemäß dem
   * Verlauf die letzte Bearbeitung am gegebenen Vorgang durchgeführt hat.
   *
   * @param vorgang Vorgang
   * @param userNames Liste von Benutzernamen
   * @return Nutzername als String
   */
  public String findLastUserForVorgangAndZustaendigkeit(Vorgang vorgang, List<String> userNames) {
    try {
      for (int i = 0; i < userNames.size(); i++) {
        userNames.set(i, "'" + userNames.get(i) + "'");
      }
      return em.createQuery("SELECT o.nutzer FROM Verlauf o "
        + "WHERE o.nutzer IS NOT NULL AND o.vorgang = :vorgang AND o.nutzer IN (" + StringUtils.join(userNames, ", ") + ") "
        + "ORDER BY o.datum DESC", String.class).setParameter("vorgang", vorgang)
        .setMaxResults(1).getSingleResult();
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Holt die Anzahl der vorhandenen Verlaufswerte an einem Vorgang
   *
   * @param vorgang Vorgang deren Verlaufswerte gezählt werden sollen
   * @return Anzahl
   */
  public long countVerlauf(Vorgang vorgang) {
    return em.createQuery("SELECT count(o) FROM Verlauf o WHERE o.vorgang = :vorgang", Long.class)
      .setParameter("vorgang", vorgang).getSingleResult();
  }

  /**
   * Holt das Datum, wann die Zuständigkeit des Vorgangs das letzte mal akzeptiert wurde.
   *
   * @param vorgang Vorgang deren Verlaufswerte gezählt werden sollen
   * @return Datum des letzten Akzeptierens des Vorgangs
   */
  public Date getAktuellstesAkzeptierenDerZustaendigkeitZuVorgang(Vorgang vorgang) {
    return em.createQuery("SELECT MAX(o.datum) FROM Verlauf o "
      + "WHERE typ = 'zustaendigkeitAkzeptiert' AND o.vorgang = :vorgang", Date.class)
      .setParameter("vorgang", vorgang).getSingleResult();
  }
}
