package de.fraunhofer.igd.klarschiff.dao;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import de.fraunhofer.igd.klarschiff.vo.RedaktionKriterien;

/**
 * DAO zum Lesen und Aktualisieren der Empfänger von redaktionellen E-Mails mit Hilfe der DB
 *
 * @author Sebastian Gutzeit (Hanse- und Universitätsstadt Rostock)
 */
@Repository
public class RedaktionKriterienDao {

  @PersistenceContext
  EntityManager em;

  /**
   * Gibt eine Liste mit den in der DB gelisteten Empfängern von redaktionellen E-Mails zurück
   *
   * @return Liste der Empfänger
   */
  @SuppressWarnings("unchecked")
  public List<RedaktionKriterien> getKriterienList() {
    return (List<RedaktionKriterien>) em.createQuery("SELECT v FROM RedaktionKriterien v ORDER BY v.stufe").getResultList();
  }
}
