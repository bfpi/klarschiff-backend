package de.fraunhofer.igd.klarschiff.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;

/**
 * Die Dao-Klasse erlaubt den Zugriff auf die Kategorien in der DB.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Repository
public class KategorieDao {

  @PersistenceContext
  EntityManager entityManager;

  /**
   * Gibt eine Liste aller vorhandenen Hauptkategorien zurück, die nicht als gelöscht markiert sind.
   *
   * @return Liste der Hauptkategorien
   */
  public List<Kategorie> findRootKategorien() {
    return entityManager.createQuery("SELECT o FROM Kategorie o "
      + "WHERE o.geloescht = false AND o.parent IS NULL ORDER BY o.name",
      Kategorie.class).getResultList();
  }

  /**
   * Gibt eine Liste aller vorhandenen Unterkategorien zurück, die nicht als gelöscht markiert sind.
   *
   * @return Liste der Unterkategorien
   */
  public List<Kategorie> getKategorien() {
    return entityManager.createQuery("SELECT o FROM Kategorie o JOIN o.parent op " +
      " WHERE op.geloescht = false AND o.parent IS NOT NULL ORDER BY o.name",
      Kategorie.class).getResultList();
  }

  public List<Kategorie> findRootKategorienForTyp(EnumVorgangTyp typ) {
    return entityManager.createQuery("SELECT o FROM Kategorie o "
      + "WHERE o.parent IS NULL AND o.typ = :typ ORDER BY o.name",
      Kategorie.class).setParameter("typ", typ).getResultList();
  }

  public Kategorie findKategorie(Long id) {
    if (id == null) {
      return null;
    }
    return entityManager.find(Kategorie.class, id);
  }
}
