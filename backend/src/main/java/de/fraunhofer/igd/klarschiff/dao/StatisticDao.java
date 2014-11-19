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
	
	@SuppressWarnings("unchecked")
	public List<Vorgang> findVorgaengeMissbrauchsmeldungen() {
		HqlQueryHelper query = new HqlQueryHelper()
			.addFromTables("Vorgang vo JOIN vo.missbrauchsmeldungen mi WITH mi.datumBestaetigung IS NOT NULL AND mi.datumAbarbeitung IS NULL")
            .addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
            .orderBy("vo.id");
        vorgangDao.addGroupByVorgang(query, true);
		processZustaendigkeitDelegiertAn(query);
		return query.getResultList(entityManager);
	}

	@SuppressWarnings("unchecked")
	public List<Vorgang> findLastVorgaenge(int maxResult) {
		HqlQueryHelper query = new HqlQueryHelper()
			.addFromTables("Vorgang vo")
            .addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
            .addWhereConditions("NOT (vo.typ = 'idee' AND vo.erstsichtungErfolgt = TRUE AND vo.status = 'offen' AND (SELECT count(*) FROM Unterstuetzer un WHERE un.vorgang = vo.id) < :unterstuetzer)").addParameter("unterstuetzer", settingsService.getVorgangIdeeUnterstuetzer())
            .orderBy("vo.datum DESC");
		query.maxResults(maxResult);
        vorgangDao.addGroupByVorgang(query, true);
		processZustaendigkeitDelegiertAn(query);
		return query.getResultList(entityManager);
	}
    
	@SuppressWarnings("unchecked")
    public List<Vorgang> findVorgaengeOffenNichtAkzeptiert(Date datum) {
		HqlQueryHelper query = (new HqlQueryHelper()).addSelectAttribute("vo")
			.addFromTables("Vorgang vo")
			.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
			.addWhereConditions("vo.status = 'offen'")
			.addWhereConditions("vo.zustaendigkeitStatus != 'akzeptiert'")
            .addWhereConditions("vo.version <= :datum").addParameter("datum", datum)
            .orderBy("vo.id");
        processZustaendigkeitDelegiertAn(query);
		return query.getResultList(entityManager);
	}
    
    @SuppressWarnings("unchecked")
    public List<Vorgang> findVorgaengeInbearbeitungOhneStatusKommentar(Date datum) {
		HqlQueryHelper query = (new HqlQueryHelper()).addSelectAttribute("vo")
			.addFromTables("Vorgang vo")
			.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
			.addWhereConditions("vo.status = 'inBearbeitung'")
			.addWhereConditions("(vo.statusKommentar IS NULL OR vo.statusKommentar = '')")
            .addWhereConditions("vo.version <= :datum").addParameter("datum", datum)
            .orderBy("vo.id");
        processZustaendigkeitDelegiertAn(query);
		return query.getResultList(entityManager);
	}
    
    @SuppressWarnings("unchecked")
    public List<Vorgang> findVorgaengeIdeeOffenOhneUnterstuetzung(Date datum) {
		HqlQueryHelper query = (new HqlQueryHelper()).addSelectAttribute("vo")
			.addFromTables("Vorgang vo JOIN vo.verlauf ve")
			.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
			.addWhereConditions("vo.typ = 'idee'")
			.addWhereConditions("vo.status = 'offen'")
            .addWhereConditions("vo.erstsichtungErfolgt = TRUE")
            .addWhereConditions("ve.typ = 'zustaendigkeitAkzeptiert'")
            .addWhereConditions("ve.datum <= :datum").addParameter("datum", datum)
            .addWhereConditions("(SELECT COUNT(*) FROM Unterstuetzer un WHERE un.vorgang = vo.id) < :unterstuetzer").addParameter("unterstuetzer", settingsService.getVorgangIdeeUnterstuetzer())
            .orderBy("vo.id");
        processZustaendigkeitDelegiertAn(query);
		return query.getResultList(entityManager);
	}
    
	@SuppressWarnings("unchecked")
    public List<Vorgang> findVorgaengeWirdnichtbearbeitetOhneStatuskommentar() {
		HqlQueryHelper query = (new HqlQueryHelper()).addSelectAttribute("vo")
			.addFromTables("Vorgang vo")
			.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
			.addWhereConditions("vo.status = 'wirdNichtBearbeitet'")
			.addWhereConditions("(vo.statusKommentar IS NULL OR vo.statusKommentar = '')")
            .orderBy("vo.id");
        processZustaendigkeitDelegiertAn(query);
		return query.getResultList(entityManager);
	}
    
	@SuppressWarnings("unchecked")
    public List<Vorgang> findVorgaengeNichtMehrOffenNichtAkzeptiert() {
		HqlQueryHelper query = (new HqlQueryHelper()).addSelectAttribute("vo")
			.addFromTables("Vorgang vo")
			.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
			.addWhereConditions("vo.status NOT IN ('gemeldet','offen')")
			.addWhereConditions("vo.zustaendigkeitStatus != 'akzeptiert'")
            .orderBy("vo.id");
        processZustaendigkeitDelegiertAn(query);
		return query.getResultList(entityManager);
	}
    
	@SuppressWarnings("unchecked")
    public List<Vorgang> findVorgaengeOhneRedaktionelleFreigaben() {
		HqlQueryHelper query = (new HqlQueryHelper()).addSelectAttribute("vo")
			.addFromTables("Vorgang vo")
			.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
			.addWhereConditions("vo.status IN ('offen', 'inBearbeitung', 'wirdNichtBearbeitet', 'abgeschlossen')")
			.addWhereConditions("vo.erstsichtungErfolgt = TRUE")
			.addWhereConditions("((vo.betreff IS NOT NULL AND vo.betreff != '' AND (betreffFreigabeStatus IS NULL OR betreffFreigabeStatus = 'intern')) OR (vo.details IS NOT NULL AND vo.details != '' AND (detailsFreigabeStatus IS NULL OR detailsFreigabeStatus = 'intern')) OR (vo.fotoThumb IS NOT NULL AND (fotoFreigabeStatus IS NULL OR fotoFreigabeStatus = 'intern')))")
            .orderBy("vo.id");
        processZustaendigkeitDelegiertAn(query);
		return query.getResultList(entityManager);
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> getStatusVerteilung(boolean onlyCurrentZustaendigkeitDelegiertAn)
	{
		HqlQueryHelper query = new HqlQueryHelper()
			.addFromTables("Vorgang vo")
			.addSelectAttribute("vo.status")
			.addSelectAttribute("COUNT(vo.id)")
			.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
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