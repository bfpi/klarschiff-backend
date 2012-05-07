package de.fraunhofer.igd.klarschiff.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.fraunhofer.igd.klarschiff.vo.Trashmail;

/**
 * Die Dao-Klasse erlaubt das Verwalten der Trashmail-Daten in der DB.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Repository
public class TrashmailDao {
	
	@PersistenceContext
	EntityManager em;
	
	@Transactional
    public void persist(Trashmail trashmail) {
        em.persist(trashmail);
    }
	
	@Transactional
	public void removeAll() {
		for(Trashmail trashmail : findAllTrashmail()) em.remove(trashmail);
	}

	@Transactional
	public List<Trashmail> findAllTrashmail() {
		return em.createQuery("SELECT o FROM Trashmail o ORDER BY o.pattern ASC", Trashmail.class).getResultList();
	}
}
