package de.fraunhofer.igd.klarschiff.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

/**
 * @author Robert Voﬂ (BFPI GmbH)
 */
@Repository
public class AuftragDao {

  @PersistenceContext
  EntityManager em;

  public void merge(Object o) {
    em.merge(o);
    em.flush();
  }
}
