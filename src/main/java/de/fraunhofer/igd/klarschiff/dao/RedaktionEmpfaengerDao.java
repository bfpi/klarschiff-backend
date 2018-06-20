package de.fraunhofer.igd.klarschiff.dao;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import de.fraunhofer.igd.klarschiff.vo.RedaktionEmpfaenger;

/**
 * DAO zum Lesen und Aktualisieren der Empfänger von redaktionellen E-Mails mit Hilfe der DB
 *
 * @author Sebastian Gutzeit (Hanse- und Universitätsstadt Rostock)
 */
@Repository
public class RedaktionEmpfaengerDao {

  @PersistenceContext
  EntityManager em;

  /**
   * Gibt eine Liste mit den in der Datenbank gelisteten Empfängern von redaktionellen E-Mails
   * zurück
   *
   * @return Liste der Empfänger
   */
  @SuppressWarnings("unchecked")
  public List<RedaktionEmpfaenger> getEmpfaengerList() {
    return (List<RedaktionEmpfaenger>) em.createQuery("SELECT v FROM RedaktionEmpfaenger v ORDER BY v.zustaendigkeit, v.email").getResultList();
  }

  /**
   * Gibt eine Liste mit den in der Datenbank gelisteten Empfängern von E-Mails mit Lob, Kritik und
   * Hinweisen für eine Zuständigkeit zurück
   *
   * @param zustaendigkeit Zuständigkeit deren Empfänger gesucht werden sollen
   * @return Liste der Empfänger
   */
  @SuppressWarnings("unchecked")
  public List<RedaktionEmpfaenger> getEmpfaengerListLobHinweiseKritikForZustaendigkeit(String zustaendigkeit) {
    return (List<RedaktionEmpfaenger>) em.createQuery("SELECT v FROM RedaktionEmpfaenger v WHERE v.empfaengerLobHinweiseKritik = true AND v.zustaendigkeit=:zustaendigkeit").setParameter("zustaendigkeit", zustaendigkeit).getResultList();
  }

}
