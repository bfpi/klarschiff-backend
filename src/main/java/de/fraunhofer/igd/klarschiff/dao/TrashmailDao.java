package de.fraunhofer.igd.klarschiff.dao;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import de.fraunhofer.igd.klarschiff.vo.Trashmail;

/**
 * Die Dao-Klasse erlaubt das Verwalten der Trashmail-Daten in der DB.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Repository
public class TrashmailDao {

  @PersistenceContext
  EntityManager em;

  /**
   * Das Objekt wird in der DB gespeichert.
   *
   * @param trashmail Das zu speichernde Objekt
   */
  @Transactional
  public void persist(Trashmail trashmail) {
    em.persist(trashmail);
  }

  /**
   * Entfernt alle Trashmail-Objekte.
   *
   */
  @Transactional
  public void removeAll() {
    for (Trashmail trashmail : findAllTrashmail()) {
      em.remove(trashmail);
    }
  }

  /**
   * Gibt eine Liste aller vorhandenen Trashmail-Objekte zurück.
   *
   * @return Liste der Flächen
   */
  @Transactional
  public List<Trashmail> findAllTrashmail() {
    return em.createQuery("SELECT o FROM Trashmail o ORDER BY o.pattern ASC", Trashmail.class).getResultList();
  }

  /**
   * Holt eine Liste vorhandenen Trashmail-Objekte anhand des Patterns.
   *
   * @param pattern Der Pattern nach dem gefiltert werden soll.
   * @return Trashmail-Objekt
   */
  @Transactional
  public Trashmail findTrashmail(String pattern) {
    List<Trashmail> list = em.createQuery("SELECT o FROM Trashmail o WHERE o.pattern = '" + pattern + "'", Trashmail.class).getResultList();
    return list.size() > 0 ? list.get(0) : null;
  }
}
