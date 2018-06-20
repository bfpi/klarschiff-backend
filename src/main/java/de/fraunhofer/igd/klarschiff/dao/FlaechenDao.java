package de.fraunhofer.igd.klarschiff.dao;

import de.fraunhofer.igd.klarschiff.vo.Flaeche;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Die Dao-Klasse erlaubt den Zugriff auf die Flächen in der DB.
 *
 * @author Robert Voß (BFPI GmbH)
 */
@Repository
public class FlaechenDao {

  @PersistenceContext
  EntityManager em;

  /**
   * Gibt eine Liste aller vorhandenen Flächen zurück.
   * 
   * @return Liste der Flächen
   */
  public List<Flaeche> getAllFlaechen() {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT f FROM Flaeche f ");
    sql.append("ORDER BY f.kurzname");
    return em.createQuery(sql.toString(), Flaeche.class).getResultList();
  }

  /**
   * Gibt die Fläche mit dem übergebenen Kurznamen zurück.
   * 
   * @param kurzname Kurzname der Fläche
   * @return Fläche
   */
  @Transactional
  public Flaeche findByKurzname(String kurzname) {
    if (kurzname == null) {
      return null;
    }
    List<Flaeche> list = em.createQuery("select f from Flaeche f where f.kurzname like :kurzname", Flaeche.class)
      .setParameter("kurzname", kurzname).getResultList();
    if (list.isEmpty()) {
      return null;
    }
    return list.get(0);
  }
}
