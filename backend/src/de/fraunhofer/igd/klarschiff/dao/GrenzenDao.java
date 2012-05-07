package de.fraunhofer.igd.klarschiff.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import de.fraunhofer.igd.klarschiff.vo.StadtteilGrenze;

/**
 * Die Dao-Klasse erlaubt den Zugriff auf die Stadtteilgrenzen in der DB.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Repository
public class GrenzenDao {
	@PersistenceContext
	EntityManager entityManager;

	
	/**
	 * Ermittelt alle Stadtteile mit ihren Grenzen
	 * @return Liste mit Arrays [0] id (long), [1] name (String)
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> findStadtteilGrenzen() {
		return entityManager.createQuery("SELECT o.id, o.name FROM StadtteilGrenze o ORDER BY o.name").getResultList();
	}

	
	/**
	 * Holt die Stadtteilgrenze anhand der id
	 * @param id Id der Stadtteilgrenze
	 * @return Stadtteilgrenze
	 */
	public StadtteilGrenze findStadtteilGrenze(Integer id) {
        if (id == null) return null;
        return entityManager.find(StadtteilGrenze.class, id);
    }
	
}
