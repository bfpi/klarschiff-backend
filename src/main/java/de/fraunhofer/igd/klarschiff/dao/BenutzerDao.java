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

  /**
   * Holt den Nenutzer anhand des Benutzernamens
   *
   * @param benutzername Der Benutzername der zu suchenden Person.
   * @return Benutzer
   */
  @Transactional
  public Benutzer findByBenutzername(String benutzername) {
    if (benutzername == null) {
      return null;
    }
    List<Benutzer> list = em.createQuery("select b from Benutzer b where b.benutzername like :benutzername", Benutzer.class)
      .setParameter("benutzername", benutzername).getResultList();
    if (list.isEmpty()) {
      return null;
    }
    return list.get(0);
  }
}
