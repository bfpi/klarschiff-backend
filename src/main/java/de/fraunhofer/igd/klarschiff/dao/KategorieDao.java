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
   * Gibt eine Liste aller vorhandenen Hauptkategorien zurück.
   *
   * @return Liste der Hauptkategorien
   */
  public List<Kategorie> findRootKategorien() {
    return entityManager.createQuery("SELECT o FROM Kategorie o WHERE o.parent IS NULL ORDER BY o.name",
      Kategorie.class).getResultList();
  }

  /**
   * Gibt eine Liste aller vorhandenen Unterkategorien zurück.
   *
   * @return Liste der Unterkategorien
   */
  public List<Kategorie> findUnterKategorien() {
    return entityManager.createQuery("SELECT o FROM Kategorie o WHERE o.parent IS NOT NULL ORDER BY o.name",
      Kategorie.class).getResultList();
  }

  /**
   * Holt die Kategorien anhand des Typs
   *
   * @param typ Typ der Kategorie
   * @return Liste der Kategorien
   */
  public List<Kategorie> findRootKategorienForTyp(EnumVorgangTyp typ) {
    return entityManager.createQuery("SELECT o FROM Kategorie o WHERE o.parent IS NULL AND o.typ = :typ ORDER BY o.name",
      Kategorie.class).setParameter("typ", typ).getResultList();
  }

  /**
   * Holt die Kategorie anhand der ID
   *
   * @param id ID der Kategorie
   * @return Kategorie
   */
  public Kategorie findKategorie(Long id) {
    if (id == null) {
      return null;
    }
    return entityManager.find(Kategorie.class, id);
  }

  /**
   * Gibt eine Liste aller vorhandenen Kategorien zurück.
   *
   * @return Liste der Kategorien
   */
  public List<Kategorie> getKategorien() {
    return getKategorien(true);
  }

  /**
   * Gibt eine Liste der Kategorien zurück.
   *
   * @param showTipp Tipps ebenfalls anzeigen
   * @return Liste der Kategorien
   */
  public List<Kategorie> getKategorien(boolean showTipp) {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT o FROM Kategorie o JOIN o.parent op ");
    sql.append("WHERE o.parent IS NOT NULL ");

    if (!showTipp) {
      sql.append("AND op.typ <> 'tipp' ");
    }

    sql.append("ORDER BY op.name, o.name");

    return entityManager.createQuery(sql.toString(), Kategorie.class).getResultList();
  }

  /**
   * Gibt eine Liste aller vorhandenen Kategorien zurück, die nicht den Typ 'Tipp' haben.
   *
   * @return Liste der Kategorien
   */
  public List<Kategorie> getAllKategorien() {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT o FROM Kategorie o LEFT JOIN o.parent op ");
    sql.append("WHERE op.typ <> 'tipp' OR o.typ <> 'tipp' ");
    sql.append("ORDER BY op.name, o.name");
    return entityManager.createQuery(sql.toString(), Kategorie.class).getResultList();
  }

  /**
   * Ermittelt die Kategorien, die keine Vorgänge haben, um den Zuständigkeitsfinder zu trainieren
   *
   * @return Kategorien
   */
  @SuppressWarnings("unchecked")
  public List<Kategorie> findKategorienWithUntrainedVorgaengeForTrainClassificator() {
    return entityManager.createQuery("SELECT k FROM Kategorie k WHERE id NOT IN ("
      + "SELECT a.kategorie " + VorgangDao.CLASSIFIER_TRAIN_QUERY + ")", Kategorie.class).getResultList();
  }
}
