package de.fraunhofer.igd.klarschiff.dao;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Die Dao-Klasse erlaubt das Ermitteln von Daten aus der DB für die Statistik.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Repository
public class StatisticDao {
	
	@PersistenceContext
	EntityManager entityManager;
	
	@Autowired
	VorgangDao vorgangDao;

	@Autowired
	SecurityService securityService;

	@Autowired
	SettingsService settingsService;
	
	public Long countNewVorgaenge(Date datumVon, boolean onlyCurrentZustaendigkeitDelegiertAn) {
		HqlQueryHelper query = new HqlQueryHelper()
			.addFromTables("Vorgang vo LEFT JOIN vo.missbrauchsmeldungen mi WITH mi.datumBestaetigung IS NOT NULL AND mi.datumAbarbeitung IS NULL")
			.addSelectAttribute("COUNT(DISTINCT vo.id)")
			.addWhereConditions("vo.datum>=:datumVon")
			.addParameter("datumVon", datumVon);
		if (onlyCurrentZustaendigkeitDelegiertAn) processZustaendigkeitDelegiertAn(query);
		return (Long)query.getSingleResult(entityManager);
	}
	
	public Long countFixedVorgaenge(Date datumVon, boolean onlyCurrentZustaendigkeitDelegiertAn) {
		HqlQueryHelper query = new HqlQueryHelper()
			.addFromTables("Vorgang vo JOIN vo.verlauf ve WITH ve.datum>=:datumVon AND ve.typ=:verlaufTyp")
			.addSelectAttribute("COUNT(DISTINCT vo.id)")
			.addParameter("datumVon", datumVon)
			.addParameter("verlaufTyp", EnumVerlaufTyp.status)
			.addWhereConditions("vo.status IN (:status)")
			.addParameter("status", Arrays.asList(EnumVorgangStatus.closedVorgangStatus()));
		if (onlyCurrentZustaendigkeitDelegiertAn) processZustaendigkeitDelegiertAn(query);
		return (Long)query.getSingleResult(entityManager);
	}

	public Long countMissbrauchsmeldungen(boolean onlyCurrentZustaendigkeitDelegiertAn) {
		HqlQueryHelper query = new HqlQueryHelper()
			.addFromTables("Vorgang vo JOIN vo.missbrauchsmeldungen mi WITH mi.datumBestaetigung IS NOT NULL AND mi.datumAbarbeitung IS NULL")
			.addSelectAttribute("COUNT(DISTINCT vo.id)");
		if (onlyCurrentZustaendigkeitDelegiertAn) processZustaendigkeitDelegiertAn(query);
		return (Long)query.getSingleResult(entityManager);
	}

	@SuppressWarnings("unchecked")
	public List<Vorgang> findLastVorgaenge(int maxResult) {
		HqlQueryHelper query = new HqlQueryHelper()
			.addFromTables("Vorgang vo LEFT JOIN vo.unterstuetzer un WITH un.datumBestaetigung IS NOT NULL ");
		vorgangDao.addGroupByVorgang(query, true);
		//Ideen mit zu wenig Unterstützern herausfiltern
		query.addHavingConditions("(vo.typ=:unTyp OR vo.status=:unStatus OR COUNT(DISTINCT un.id)>=:unterstuetzer) ")
			.addParameter("unTyp", EnumVorgangTyp.problem)
			.addParameter("unStatus", EnumVorgangStatus.offen)
			.addParameter("unterstuetzer", settingsService.getVorgangIdeeUnterstuetzer());
		//Zuständigkeit & DelegiertAn
		processZustaendigkeitDelegiertAn(query);
		//Sortierung
		query.orderBy("vo.datum DESC");
		//maxResult
		query.maxResults(maxResult);
		
		return query.getResultList(entityManager);
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Object[]> getStatusVerteilung(boolean onlyCurrentZustaendigkeitDelegiertAn)
	{
		HqlQueryHelper query = new HqlQueryHelper()
			.addFromTables("Vorgang vo")
			.addSelectAttribute("vo.status")
			.addSelectAttribute("COUNT(vo.id)")
			.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert=:archiviert)")
			.addParameter("archiviert", Boolean.FALSE)
			.addGroupByAttribute("vo.status")
			.addGroupByAttribute("vo.statusOrdinal")
			.orderBy("vo.statusOrdinal");
		if (onlyCurrentZustaendigkeitDelegiertAn) processZustaendigkeitDelegiertAn(query);
		return query.getResultList(entityManager);
	}
	
	public void processZustaendigkeitDelegiertAn(HqlQueryHelper query) {
		List<Role> zustaendigkeiten = securityService.getCurrentZustaendigkeiten(true);
		List<Role> delegiertAn = securityService.getCurrentDelegiertAn();
	
		//Zuständigkeit & DelegiertAn
		if (!CollectionUtils.isEmpty(zustaendigkeiten) && !CollectionUtils.isEmpty(delegiertAn)) 
			query.addWhereConditions("(vo.zustaendigkeit IN (:zustaendigkeit) OR vo.delegiertAn IN (:delegiertAn))")
				.addParameter("zustaendigkeit", Role.toString(zustaendigkeiten))
				.addParameter("delegiertAn", Role.toString(delegiertAn));
		else if (!CollectionUtils.isEmpty(zustaendigkeiten))
			query.addWhereConditions("vo.zustaendigkeit IN (:zustaendigkeit)").addParameter("zustaendigkeit", Role.toString(zustaendigkeiten));
		else if (!CollectionUtils.isEmpty(delegiertAn)) 
			query.addWhereConditions("vo.delegiertAn IN (:delegiertAn)").addParameter("delegiertAn", Role.toString(delegiertAn));
	}
}