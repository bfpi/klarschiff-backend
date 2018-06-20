package de.fraunhofer.igd.klarschiff.dao;

import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import de.fraunhofer.igd.klarschiff.vo.LobHinweiseKritik;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import de.fraunhofer.igd.klarschiff.web.AdminLobHinweiseKritikCommand;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Die Dao-Klasse ermöglicht den Zugriff auf Lob, Hinweise oder Kritik zu einem Vorgang
 *
 * @author Sebastian Gutzeit (Hanse- und Universitätsstadt Rostock)
 */
@Repository
public class LobHinweiseKritikDao {

  @PersistenceContext
  EntityManager em;

  @Autowired
  SecurityService securityService;

  /**
   * Das Objekt wird in der DB gespeichert.
   *
   * @param lobHinweiseKritik Das zu speichernde Objekt
   */
  @Transactional
  public void persist(LobHinweiseKritik lobHinweiseKritik) {
    em.persist(lobHinweiseKritik);
  }

  /**
   * Holt alle vorhandenen Lob/Hinweis/Kritik-Einträge eines Vorgang
   *
   * @param vorgang Vorgang deren Lob/Hinweis/Kritik-Einträge geholt werden sollen
   * @return Liste der Lob/Hinweis/Kritik-Einträge
   */
  @Transactional
  public List<LobHinweiseKritik> findLobHinweiseKritikForVorgang(Vorgang vorgang) {
    return em.createQuery("SELECT o FROM LobHinweiseKritik o WHERE o.vorgang=:vorgang ORDER BY o.datum DESC", LobHinweiseKritik.class).setParameter("vorgang", vorgang).getResultList();
  }

  /**
   * Holt die vorhandenen Lob/Hinweis/Kritik-Einträge an einem Vorgang
   *
   * @param vorgang Vorgang deren Lob/Hinweis/Kritik-Einträge geholt werden sollen
   * @param page Seite
   * @param size Anzahl pro Seite
   * @return Liste der Lob/Hinweis/Kritik-Einträge
   */
  @Transactional
  public List<LobHinweiseKritik> findLobHinweiseKritikForVorgang(Vorgang vorgang, Integer page, Integer size) {
    TypedQuery<LobHinweiseKritik> query = em.createQuery("SELECT o FROM LobHinweiseKritik o WHERE o.vorgang=:vorgang ORDER BY o.datum DESC", LobHinweiseKritik.class).setParameter("vorgang", vorgang);

    if (page != null && size != null) {
      query.setFirstResult((page - 1) * size);
    }
    if (size != null) {
      query.setMaxResults(size);
    }

    return query.getResultList();
  }

  /**
   * Holt alle vorhandenen Lob/Hinweis/Kritik-Einträge eines Vorgang
   *
   * @param cmd Command mit den Parametern zur Suche
   * @return Liste der Lob/Hinweis/Kritik-Einträge
   */
  @SuppressWarnings("unchecked")
  public List<LobHinweiseKritik> findLobHinweiseKritik(AdminLobHinweiseKritikCommand cmd) {
    HqlQueryHelper query = (new HqlQueryHelper(securityService)).addSelectAttribute("o")
      .addFromTables("LobHinweiseKritik o");

    if (cmd.getPage() != null && cmd.getSize() != null) {
      query.firstResult((cmd.getPage() - 1) * cmd.getSize());
    }
    if (cmd.getSize() != null) {
      query.maxResults(cmd.getSize());
    }

    for (String field : cmd.getOrderString().split(",")) {
      query.orderBy(field.trim() + " " + cmd.getOrderDirectionString());
    }

    return query.getResultList(em);
  }

  /**
   * Holt die Anzahl der vorhandenen Lob/Hinweis/Kritik-Einträge an einem Vorgang
   *
   * @param vorgang Vorgang deren Lob/Hinweis/Kritik-Einträge gezählt werden sollen
   * @return Anzahl
   */
  public long countLobHinweiseKritik(Vorgang vorgang) {
    return em.createQuery("SELECT COUNT(o) FROM LobHinweiseKritik o WHERE o.vorgang=:vorgang", Long.class).setParameter("vorgang", vorgang).getSingleResult();
  }

  /**
   * Holt die Anzahl aller vorhandenen Lob/Hinweis/Kritik-Einträge
   *
   * @return Anzahl
   */
  public long countLobHinweiseKritik() {
    return em.createQuery("SELECT COUNT(o) FROM LobHinweiseKritik o", Long.class).getSingleResult();
  }
}
