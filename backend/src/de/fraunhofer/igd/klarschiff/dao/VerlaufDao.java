package de.fraunhofer.igd.klarschiff.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.Verlauf;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Die Dao-Klasse erlaubt das Verwalten der Verlauf-Daten der Vorgänge in der DB.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Repository
public class VerlaufDao {
	
	@PersistenceContext
	EntityManager em;
	
	@Autowired
	SecurityService securityService;
	
	@Autowired
	VorgangDao vorgangDao;
	
	@Transactional
    public void persist(Verlauf verlauf) {
        em.persist(verlauf);
    }
	
	/**
	 * Fügt zu einem Vorgang neue Verlaufswerte hinzu, ohne diese in der DB zu speichern.
	 * @param vorgang Vorgang zu dem die Verlaufswerte hinzugefügt werden sollen
	 * @param typ Typ des Verlaufs
	 * @param wertAlt Alter Wert
	 * @param wertNeu Neuer Wert
	 * @return Verlaufswerte
	 */
	public Verlauf addVerlaufToVorgang(Vorgang vorgang, EnumVerlaufTyp typ, String wertAlt, String wertNeu) {
		Verlauf verlauf = new Verlauf();
		verlauf.setVorgang(vorgang);
		try {
			verlauf.setNutzer(securityService.getCurrentUser().getName());
		} catch (Exception e) {}
		verlauf.setTyp(typ);
		verlauf.setWertAlt(wertAlt);
		verlauf.setWertNeu(wertNeu);
		vorgang.getVerlauf().add(verlauf);
		return verlauf;
	}
	

	@Transactional
	public List<Verlauf> findVerlaufForVorgang(Vorgang vorgang, Integer page, Integer size) {
		TypedQuery<Verlauf> query = em.createQuery("SELECT o FROM Verlauf o WHERE o.vorgang=:vorgang ORDER BY o.datum DESC", Verlauf.class).setParameter("vorgang", vorgang);
		
		if (page!=null && size!=null)
			query.setFirstResult((page-1)*size);
		if (size!=null)
			query.setMaxResults(size);

		return query.getResultList();
	}

	public long countVerlauf(Vorgang vorgang) {
		return em.createQuery("select count(o) from Verlauf o WHERE o.vorgang=:vorgang", Long.class).setParameter("vorgang", vorgang).getSingleResult();
	}
}
