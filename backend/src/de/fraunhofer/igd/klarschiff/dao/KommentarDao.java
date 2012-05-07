package de.fraunhofer.igd.klarschiff.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.fraunhofer.igd.klarschiff.vo.Kommentar;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Die Dao-Klasse ermöglicht den Zugriff auf die Kommentare der Vorgänge 
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Repository
public class KommentarDao {
	
	@PersistenceContext
	EntityManager em;
	
	@Transactional
    public void persist(Kommentar kommentar) {
        em.persist(kommentar);
    }

	@Transactional
	public List<Kommentar> findKommentareForVorgang(Vorgang vorgang) {
		return em.createQuery("SELECT o FROM Kommentar o WHERE o.vorgang=:vorgang ORDER BY o.datum DESC", Kommentar.class).setParameter("vorgang", vorgang).getResultList();
	}
	
	@Transactional
	public List<Kommentar> findKommentareForVorgang(Vorgang vorgang, Integer page, Integer size) {
		TypedQuery<Kommentar> query = em.createQuery("SELECT o FROM Kommentar o WHERE o.vorgang=:vorgang ORDER BY o.datum DESC", Kommentar.class).setParameter("vorgang", vorgang);
		
		if (page!=null && size!=null)
			query.setFirstResult((page-1)*size);
		if (size!=null)
			query.setMaxResults(size);

		return query.getResultList();
	}
	
	public long countKommentare(Vorgang vorgang) {
		return em.createQuery("SELECT COUNT(o) FROM Kommentar o WHERE o.vorgang=:vorgang", Long.class).setParameter("vorgang", vorgang).getSingleResult();
	}
}