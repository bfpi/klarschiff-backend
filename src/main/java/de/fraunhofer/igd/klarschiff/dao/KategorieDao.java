package de.fraunhofer.igd.klarschiff.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;
import org.hibernate.Session;

/**
 * Die Dao-Klasse erlaubt den Zugriff auf die Kategorien in der DB.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Repository
public class KategorieDao {

  @PersistenceContext
  EntityManager entityManager;

  public List<Kategorie> findRootKategorien() {
    return entityManager.createQuery("SELECT o FROM Kategorie o WHERE o.parent IS NULL ORDER BY o.name",
      Kategorie.class).getResultList();
  }

  public List<Kategorie> findUnterKategorien() {
    return entityManager.createQuery("SELECT o FROM Kategorie o WHERE o.parent IS NOT NULL ORDER BY o.name",
      Kategorie.class).getResultList();
  }

  public List<Kategorie> findRootKategorienForTyp(EnumVorgangTyp typ) {
    return entityManager.createQuery("SELECT o FROM Kategorie o WHERE o.parent IS NULL AND o.typ = :typ ORDER BY o.name",
      Kategorie.class).setParameter("typ", typ).getResultList();
  }

  public Kategorie findKategorie(Long id) {
    if (id == null) {
      return null;
    }
    return entityManager.find(Kategorie.class, id);
  }

  public List<Kategorie> getKategorien() {
    return getKategorien(true);
  }

  public List<Kategorie> getKategorien(boolean showTipp) {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT o FROM Kategorie o JOIN o.parent op ");
    sql.append("WHERE o.parent IS NOT NULL ");

    if (!showTipp) {
      sql.append("AND op.typ <> 'tipp'");
    }

    return entityManager.createQuery(sql.toString(), Kategorie.class).getResultList();
  }
  
  public List<Kategorie> getAllKategorien() {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT o FROM Kategorie o LEFT JOIN o.parent op ");
    sql.append("WHERE op.typ <> 'tipp' OR o.typ <> 'tipp'");
    return entityManager.createQuery(sql.toString(), Kategorie.class).getResultList();
  }
}
