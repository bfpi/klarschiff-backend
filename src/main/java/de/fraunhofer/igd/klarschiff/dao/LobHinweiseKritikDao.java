package de.fraunhofer.igd.klarschiff.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.fraunhofer.igd.klarschiff.vo.LobHinweiseKritik;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import de.fraunhofer.igd.klarschiff.web.AdminLobHinweiseKritikCommand;

/**
 * Die Dao-Klasse ermöglicht den Zugriff auf Lob, Hinweise oder Kritik zu einem Vorgang
 * @author Sebastian Schwarz (Hansestadt Rostock)
 */
@Repository
public class LobHinweiseKritikDao {
	
	@PersistenceContext
	EntityManager em;
	
	@Transactional
    public void persist(LobHinweiseKritik lobHinweiseKritik) {
        em.persist(lobHinweiseKritik);
    }

	@Transactional
	public List<LobHinweiseKritik> findLobHinweiseKritikForVorgang(Vorgang vorgang) {
		return em.createQuery("SELECT o FROM LobHinweiseKritik o WHERE o.vorgang=:vorgang ORDER BY o.datum DESC", LobHinweiseKritik.class).setParameter("vorgang", vorgang).getResultList();
	}
	
	@Transactional
	public List<LobHinweiseKritik> findLobHinweiseKritikForVorgang(Vorgang vorgang, Integer page, Integer size) {
		TypedQuery<LobHinweiseKritik> query = em.createQuery("SELECT o FROM LobHinweiseKritik o WHERE o.vorgang=:vorgang ORDER BY o.datum DESC", LobHinweiseKritik.class).setParameter("vorgang", vorgang);
		
		if (page!=null && size!=null)
			query.setFirstResult((page-1)*size);
		if (size!=null)
			query.setMaxResults(size);

		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<LobHinweiseKritik> findLobHinweiseKritik(AdminLobHinweiseKritikCommand cmd) {
		HqlQueryHelper query = (new HqlQueryHelper()).addSelectAttribute("o")
			.addFromTables("LobHinweiseKritik o");

		if (cmd.getPage()!=null && cmd.getSize()!=null)
			query.firstResult((cmd.getPage()-1)*cmd.getSize());
		if (cmd.getSize()!=null)
			query.maxResults(cmd.getSize());
		
		for(String field : cmd.getOrderString().split(","))
			query.orderBy(field.trim()+" "+cmd.getOrderDirectionString());
		
		return query.getResultList(em);
	}
    
    public long countLobHinweiseKritik(Vorgang vorgang) {
		return em.createQuery("SELECT COUNT(o) FROM LobHinweiseKritik o WHERE o.vorgang=:vorgang", Long.class).setParameter("vorgang", vorgang).getSingleResult();
	}
    
    public long countLobHinweiseKritik() {
		return em.createQuery("SELECT COUNT(o) FROM LobHinweiseKritik o", Long.class).getSingleResult();
	}
}