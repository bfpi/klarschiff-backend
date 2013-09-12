package de.fraunhofer.igd.klarschiff.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.fraunhofer.igd.klarschiff.vo.RedaktionKriterien;

/**
 * DAO zum Lesen und Aktualisieren der Empf�nger von redaktionellen E-Mails mit Hilfe der DB
 * @author Sebastian Schwarz (Hansestadt Rostock)
 */
@Repository
public class RedaktionKriterienDao {

	@PersistenceContext
	EntityManager em;

	/**
	 * gibt eine Liste mit den in der DB gelisteten Empf�ngern von redaktionellen E-Mails zur�ck
	 */
	@SuppressWarnings("unchecked")
	public List<RedaktionKriterien> getKriterienList() {
		return (List<RedaktionKriterien>)em.createQuery("SELECT v FROM RedaktionKriterien v ORDER BY v.stufe").getResultList();
	}
}
