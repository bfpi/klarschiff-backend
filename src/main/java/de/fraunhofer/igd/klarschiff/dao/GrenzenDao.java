package de.fraunhofer.igd.klarschiff.dao;

import de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.StadtGrenze;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import de.fraunhofer.igd.klarschiff.vo.StadtteilGrenze;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import javax.persistence.Query;

/**
 * Die Dao-Klasse erlaubt den Zugriff auf die Stadtteilgrenzen in der DB.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Repository
public class GrenzenDao {

  @PersistenceContext
  EntityManager entityManager;

  /**
   * Ermittelt alle Stadtteile mit ihren Grenzen
   *
   * @return Liste mit Arrays [0] id (long), [1] name (String)
   */
  @SuppressWarnings("unchecked")
  public List<Object[]> findStadtteilGrenzen() {
    return entityManager.createQuery("SELECT o.id, o.name FROM StadtteilGrenze o ORDER BY o.name").getResultList();
  }

  /**
   * Holt die Stadtteilgrenze anhand der id
   *
   * @param id Id der Stadtteilgrenze
   * @return Stadtteilgrenze
   */
  public StadtteilGrenze findStadtteilGrenze(Integer id) {
    if (id == null) {
      return null;
    }
    return entityManager.find(StadtteilGrenze.class, id);
  }

  /**
   * Holt die Stadtteilgrenze anhand eines vorgangs
   *
   * @param vorgang Vorgang
   * @return Stadtteilgrenze
   */
  public StadtteilGrenze findStadtteilGrenzeByVorgang(Vorgang vorgang) {
    if (vorgang == null) {
      return null;
    }
    List<Integer> l = entityManager.createNativeQuery("select ssg.id from klarschiff_stadtteil_grenze ssg "
      + " inner join klarschiff_vorgang kv on ST_Within(kv.ovi, ssg.grenze) where kv.id = " + vorgang.getId())
      .getResultList();

    return findStadtteilGrenze(l.get(0));
  }

  /**
   * Ermittelt die Stadtgrenze
   *
   * @return Stadtteilgrenze
   */
  @SuppressWarnings("unchecked")
  public StadtGrenze getStadtgrenze() {
    return entityManager.createQuery("select sg from StadtGrenze sg", StadtGrenze.class).getResultList().get(0);
  }
}
