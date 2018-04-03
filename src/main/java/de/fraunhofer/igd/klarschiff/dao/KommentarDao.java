package de.fraunhofer.igd.klarschiff.dao;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import de.fraunhofer.igd.klarschiff.vo.Kommentar;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Die Dao-Klasse ermöglicht den Zugriff auf die Kommentare der Vorgänge
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Repository
public class KommentarDao {

  @PersistenceContext
  EntityManager em;

  /**
   * Das Objekt wird in der DB gespeichert.
   *
   * @param kommentar Das zu speichernde Objekt
   */
  @Transactional
  public void persist(Kommentar kommentar) {
    em.persist(kommentar);
  }

  /**
   * Das Objekt wird in der DB gespeichert.
   *
   * @param kommentar Das zu speichernde Objekt
   */
  @Transactional
  public void merge(Kommentar kommentar) {
    em.merge(kommentar);
    em.flush();
  }

  /**
   * Holt alle vorhandenen Kommentare eines Vorgang
   *
   * @param vorgang Vorgang deren Kommentare geholt werden sollen
   * @return Liste der Kommentare
   */
  @Transactional
  public List<Kommentar> findKommentareForVorgang(Vorgang vorgang) {
    return em.createQuery("SELECT o FROM Kommentar o WHERE o.vorgang=:vorgang AND o.geloescht = 'false' ORDER BY o.datum DESC", Kommentar.class).setParameter("vorgang", vorgang).getResultList();
  }

  /**
   * Holt die vorhandenen Kommentare an einem Vorgang
   *
   * @param vorgang Vorgang deren Kommentare geholt werden sollen
   * @param page Seite
   * @param size Anzahl pro Seite
   * @return Liste der Kommentare
   */
  @Transactional
  public List<Kommentar> findKommentareForVorgang(Vorgang vorgang, Integer page, Integer size) {
    TypedQuery<Kommentar> query = em.createQuery("SELECT o FROM Kommentar o WHERE o.vorgang=:vorgang AND o.geloescht = 'false' ORDER BY o.datum DESC", Kommentar.class).setParameter("vorgang", vorgang);

    if (page != null && size != null) {
      query.setFirstResult((page - 1) * size);
    }
    if (size != null) {
      query.setMaxResults(size);
    }

    return query.getResultList();
  }

  /**
   * Holt die Anzahl der vorhandenen Kommentare an einem Vorgang
   *
   * @param vorgang Vorgang deren Kommentare gezählt werden sollen
   * @return Anzahl
   */
  public long countKommentare(Vorgang vorgang) {
    return em.createQuery("SELECT COUNT(o) FROM Kommentar o WHERE o.vorgang=:vorgang AND o.geloescht = 'false'", Long.class).setParameter("vorgang", vorgang).getSingleResult();
  }

  /**
   * Holt den Kommentar anhand der ID
   *
   * @param id ID des Kommentars
   * @return Kommentar
   */
  public Kommentar findById(long id) {
    return em.find(Kommentar.class, id);
  }
}
