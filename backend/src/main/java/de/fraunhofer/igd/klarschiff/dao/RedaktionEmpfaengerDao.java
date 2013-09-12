package de.fraunhofer.igd.klarschiff.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.fraunhofer.igd.klarschiff.vo.RedaktionEmpfaenger;

/**
 * DAO zum Lesen und Aktualisieren der Empf�nger von redaktionellen E-Mails mit Hilfe der DB
 * @author Sebastian Schwarz (Hansestadt Rostock)
 */
@Repository
public class RedaktionEmpfaengerDao {

	@PersistenceContext
	EntityManager em;

	/**
	 * gibt eine Liste mit den in der Datenbank gelisteten Empf�ngern von redaktionellen E-Mails zur�ck
	 */
	@SuppressWarnings("unchecked")
	public List<RedaktionEmpfaenger> getEmpfaengerList() {
		return (List<RedaktionEmpfaenger>)em.createQuery("SELECT v FROM RedaktionEmpfaenger v ORDER BY v.zustaendigkeit, v.email").getResultList();
	}
    
    /**
	 * gibt eine Liste mit den in der Datenbank gelisteten Empf�ngern von E-Mails mit Lob, Kritik und Hinweisen f�r eine Zust�ndigkeit zur�ck
	 */
	@SuppressWarnings("unchecked")
	public List<RedaktionEmpfaenger> getEmpfaengerListLobHinweiseKritikForZustaendigkeit(String zustaendigkeit) {
		return (List<RedaktionEmpfaenger>)em.createQuery("SELECT v FROM RedaktionEmpfaenger v WHERE v.empfaengerLobHinweiseKritik = true AND v.zustaendigkeit=:zustaendigkeit").setParameter("zustaendigkeit", zustaendigkeit).getResultList();
	}

}
