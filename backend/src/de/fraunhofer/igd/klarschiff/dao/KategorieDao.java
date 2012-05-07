package de.fraunhofer.igd.klarschiff.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import de.fraunhofer.igd.klarschiff.vo.EnumNaehereBeschreibungNotwendig;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;

/**
 * Die Dao-Klasse erlaubt den Zugriff auf die Kategorien in der DB.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Repository
public class KategorieDao {
	
	@PersistenceContext
	EntityManager entityManager;
	
	public List<Kategorie> findRootKategorien() {
		return entityManager.createQuery("SELECT o FROM Kategorie o WHERE o.parent IS NULL ORDER BY o.name", Kategorie.class).getResultList();
	}

	public List<Kategorie> findRootKategorienForTyp(EnumVorgangTyp typ) {
		return entityManager.createQuery("SELECT o FROM Kategorie o WHERE o.parent IS NULL AND o.typ=:typ ORDER BY o.name", Kategorie.class).setParameter("typ", typ).getResultList();
	}
	
	public Kategorie findKategorie(Long id) {
        if (id == null) return null;
        return entityManager.find(Kategorie.class, id);
    }

	public EnumNaehereBeschreibungNotwendig viewNaehereBeschreibung(Long hauptkategorie, Long unterkategorie) {
		EnumNaehereBeschreibungNotwendig naehereBeschreibungNotwendig = EnumNaehereBeschreibungNotwendig.keine;
		try {
			naehereBeschreibungNotwendig = (EnumNaehereBeschreibungNotwendig)entityManager.createQuery("SELECT o.naehereBeschreibungNotwendig FROM Kategorie o WHERE o.id=:kategorie").setParameter("kategorie", unterkategorie).getSingleResult();
		} catch (Exception e) {}
		if (naehereBeschreibungNotwendig!=EnumNaehereBeschreibungNotwendig.keine) return naehereBeschreibungNotwendig;

		try {
			naehereBeschreibungNotwendig = (EnumNaehereBeschreibungNotwendig)entityManager.createQuery("SELECT o.naehereBeschreibungNotwendig FROM Kategorie o WHERE o.id=:kategorie").setParameter("kategorie", hauptkategorie).getSingleResult();
		} catch (Exception e) {}
		return naehereBeschreibungNotwendig;
	}

	public EnumNaehereBeschreibungNotwendig viewNaehereBeschreibung(Kategorie hauptkategorie, Kategorie unterkategorie) {
		return viewNaehereBeschreibung(hauptkategorie==null ? null : hauptkategorie.getId(), unterkategorie==null ? null : unterkategorie.getId());
	}
	
	public List<Kategorie> getKategorien() {
		return entityManager.createQuery("SELECT o FROM Kategorie o WHERE o.parent IS NOT NULL", Kategorie.class).getResultList();
	}

}
