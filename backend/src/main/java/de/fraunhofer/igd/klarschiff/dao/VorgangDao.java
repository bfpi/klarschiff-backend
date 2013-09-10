package de.fraunhofer.igd.klarschiff.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.fraunhofer.igd.klarschiff.context.AppContext;
import de.fraunhofer.igd.klarschiff.service.classification.ClassificationService;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.EnumZustaendigkeitStatus;
import de.fraunhofer.igd.klarschiff.vo.Missbrauchsmeldung;
import de.fraunhofer.igd.klarschiff.vo.StatusKommentarVorlage;
import de.fraunhofer.igd.klarschiff.vo.Unterstuetzer;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import de.fraunhofer.igd.klarschiff.vo.VorgangFeatures;
import de.fraunhofer.igd.klarschiff.vo.VorgangHistoryClasses;
import de.fraunhofer.igd.klarschiff.web.VorgangDelegiertSuchenCommand;
import de.fraunhofer.igd.klarschiff.web.VorgangFeedCommand;
import de.fraunhofer.igd.klarschiff.web.VorgangFeedDelegiertAnCommand;
import de.fraunhofer.igd.klarschiff.web.VorgangSuchenCommand;

/**
 * Die Dao-Klasse erlaubt das Verwalten der Vorgänge in der DB.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Repository
public class VorgangDao {
	
	@PersistenceContext
	EntityManager em;
	
	@Autowired
	SecurityService securityService;
	
	@Autowired
	SettingsService settingsService;
	
	@Autowired
	VerlaufDao verlaufDao;
	
	/**
	 * Das Objekt wird in der DB gespeichert. Bei Vorgängen wird geprüft, ob diese sich geändert haben. Entsprechend werden die
	 * Verlaufsdaten zum Vorgang ergänzt.
	 * @param o Das zu speichernde Objekt
	 */
	@Transactional
    public void persist(Object o) {
		if (o instanceof Vorgang) checkForUpdate((Vorgang)o);
        em.persist(o);
    }

	public void merge(Object o) {
		merge(o, true);
	}

	/**
	 * Das Objekt wird in der DB gespeichert. Bei Vorgängen wird ggf. geprüft, ob diese sich geändert haben. Entsprechend werden die
	 * Verlaufsdaten zum Vorgang ergänzt.
	 * @param o Das zu speichernde Objekt
	 * @param checkForUpdateEnable Sollen Vorgänge auf Änderung geprüft werden und somit ggf. der Verlauf ergänzt werden?
	 */
	@Transactional
	public void merge(Object o, boolean checkForUpdateEnable) {
		if (checkForUpdateEnable && o instanceof Vorgang) checkForUpdate((Vorgang)o);
		em.merge(o);
		em.flush();
	}
	
	@Transactional
	public void remove(Object o) {
		em.remove(o);
		em.flush();
	}
	

	/**
	 * Prüft einen Vorgang auf Änderungen und ergänzt den Verlauf
	 * @param vorgang Vorgang der geprüft werden soll
	 */
	private void checkForUpdate(Vorgang vorgang) 
	{
		if (vorgang.getId()==null) verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.erzeugt, null, null);
		else {
			Vorgang vorgangOld = findVorgang(vorgang.getId());
			//Status
			if (vorgangOld.getStatus() != vorgang.getStatus()) verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.status, vorgangOld.getStatus().getText(), vorgang.getStatus().getText());
			//Statuskommentar
			if (!StringUtils.equals(vorgangOld.getStatusKommentar(), vorgang.getStatusKommentar()) && (!StringUtils.isBlank(vorgangOld.getStatusKommentar()) || !StringUtils.isBlank(vorgang.getStatusKommentar()))) 
				verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.statusKommentar, StringUtils.abbreviate(vorgangOld.getStatusKommentar(), 100), StringUtils.abbreviate(vorgang.getStatusKommentar(), 100));
			//Zuständigkeit
			if (!StringUtils.equals(vorgangOld.getZustaendigkeit(),vorgang.getZustaendigkeit())) {
				verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.zustaendigkeit, vorgangOld.getZustaendigkeit(), vorgang.getZustaendigkeit());
				if (vorgang.getZustaendigkeitStatus()==EnumZustaendigkeitStatus.akzeptiert) 
					verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.zustaendigkeitAkzeptiert, vorgangOld.getZustaendigkeitStatus().getText(), vorgang.getZustaendigkeitStatus().getText());
			}
			if (vorgangOld.getZustaendigkeitStatus() != vorgang.getZustaendigkeitStatus()) verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.zustaendigkeitAkzeptiert, vorgangOld.getZustaendigkeitStatus().getText(), vorgang.getZustaendigkeitStatus().getText());
			//Zuständigkeit beim ClassificationService registrieren
			if (vorgang.getZustaendigkeitStatus()==EnumZustaendigkeitStatus.akzeptiert &&
					(vorgangOld.getZustaendigkeitStatus()!=EnumZustaendigkeitStatus.akzeptiert || !StringUtils.equals(vorgangOld.getZustaendigkeit(),vorgang.getZustaendigkeit()))) 
				AppContext.getApplicationContext().getBean(ClassificationService.class).registerZustaendigkeitAkzeptiert(vorgang);
			//Freigabestatus
			if (vorgangOld.getBetreffFreigabeStatus() != vorgang.getBetreffFreigabeStatus()) verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.betreffFreigabeStatus, vorgangOld.getBetreffFreigabeStatus().getText(), vorgang.getBetreffFreigabeStatus().getText());
			if (vorgangOld.getDetailsFreigabeStatus() != vorgang.getDetailsFreigabeStatus()) verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.detailsFreigabeStatus, vorgangOld.getDetailsFreigabeStatus().getText(), vorgang.getDetailsFreigabeStatus().getText());
			if (vorgangOld.getFotoFreigabeStatus() != vorgang.getFotoFreigabeStatus()) verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.fotoFreigabeStatus, vorgangOld.getFotoFreigabeStatus().getText(), vorgang.getFotoFreigabeStatus().getText());
			//Typ
			if (vorgangOld.getTyp() != vorgang.getTyp()) verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.typ, vorgangOld.getTyp().getText(), vorgang.getTyp().getText());
			//Kategorie
			if (vorgangOld.getKategorie().getId() != vorgang.getKategorie().getId()) verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.kategorie, vorgangOld.getKategorie().getParent().getName()+" / "+vorgangOld.getKategorie().getName(), vorgang.getKategorie().getParent().getName()+" / "+vorgang.getKategorie().getName());
			//Betreff
			if (!StringUtils.equals(vorgangOld.getBetreff(), vorgang.getBetreff())) verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.betreff, StringUtils.abbreviate(vorgangOld.getBetreff(), 100), StringUtils.abbreviate(vorgang.getBetreff(), 100));
			//Details
			if (!StringUtils.equals(vorgangOld.getDetails(), vorgang.getDetails())) verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.detail, StringUtils.abbreviate(vorgangOld.getDetails(), 100), StringUtils.abbreviate(vorgang.getDetails(), 100));
			//Adresse
			if (!StringUtils.equals(vorgangOld.getAdresse(), vorgang.getAdresse())) verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.adresse, StringUtils.abbreviate(vorgangOld.getAdresse(), 100), StringUtils.abbreviate(vorgang.getAdresse(), 100));
            //Flurstückseigentum
			if (!StringUtils.equals(vorgangOld.getFlurstueckseigentum(), vorgang.getFlurstueckseigentum())) verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.flurstueckseigentum, StringUtils.abbreviate(vorgangOld.getFlurstueckseigentum(), 100), StringUtils.abbreviate(vorgang.getFlurstueckseigentum(), 100));
			//Delegieren
			if (!StringUtils.equals(vorgangOld.getDelegiertAn(), vorgang.getDelegiertAn())) verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.delegiertAn, vorgangOld.getDelegiertAn(), vorgang.getDelegiertAn());
			//Priorität
			if (vorgangOld.getPrioritaet()!=vorgang.getPrioritaet()) verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.prioritaet, vorgangOld.getPrioritaet().getText(), vorgang.getPrioritaet().getText());
			//Archiv
			if (vorgangOld.getArchiviert() != vorgang.getArchiviert()) verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.archiv, vorgangOld.getArchiviert()+"", vorgang.getArchiviert()+"");
		}
	}
	
	@Transactional
	public Vorgang findVorgang(Long id) {
		if (id == null) return null;
		return em.find(Vorgang.class, id);
	}	
	
	@Transactional
	public Vorgang findVorgangByHash(String hash) {
		if (hash == null) return null;
		return em.createQuery("select o from Vorgang o where o.hash=:hash", Vorgang.class).setParameter("hash", hash).getSingleResult();
	}

	@Transactional
	public Unterstuetzer findUnterstuetzer(String hash) {
		if (hash == null) return null;
		List<Unterstuetzer> list = em.createQuery("select o from Unterstuetzer o where o.hash=:hash", Unterstuetzer.class).setParameter("hash", hash).setMaxResults(1).getResultList();
		if (list.isEmpty()) return null;
		else return list.get(0);
	}

	@Transactional
	public Long countUnterstuetzerByVorgang(Vorgang vorgang) {
		return em.createQuery("select count(o) from Unterstuetzer o where o.vorgang=:vorgang AND o.datumBestaetigung IS NOT NULL", Long.class).setParameter("vorgang", vorgang).getSingleResult();
	}
	
	@Transactional
	public Missbrauchsmeldung findMissbrauchsmeldung(Long id) {
		if (id == null) return null;
		return em.find(Missbrauchsmeldung.class, id);
	}	

	@Transactional
	public Missbrauchsmeldung findMissbrauchsmeldung(String hash) {
		if (hash == null) return null;
		List<Missbrauchsmeldung> list = em.createQuery("select o from Missbrauchsmeldung o where o.hash=:hash", Missbrauchsmeldung.class).setParameter("hash", hash).setMaxResults(1).getResultList();
		if (list.isEmpty()) return null;
		else return list.get(0);
	}

	@Transactional
	public Long countOpenMissbrauchsmeldungByVorgang(Vorgang vorgang) {
		return em.createQuery("select count(o) from Missbrauchsmeldung o where o.vorgang=:vorgang AND o.datumBestaetigung IS NOT NULL AND o.datumAbarbeitung IS NULL", Long.class).setParameter("vorgang", vorgang).getSingleResult();
	}

	@Transactional
	public List<Vorgang> listVorgang(int firstResult, int maxResults) {
		return em.createQuery("select o from Vorgang o", Vorgang.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
	}
	
	@Transactional
	public List<Missbrauchsmeldung> listMissbrauchsmeldung(Vorgang vorgang) {
		List<Missbrauchsmeldung> missbrauchsmeldungen = em.createQuery("select o from Missbrauchsmeldung o WHERE o.vorgang=:vorgang AND o.datumBestaetigung IS NOT NULL ORDER BY o.datum DESC", Missbrauchsmeldung.class).setParameter("vorgang", vorgang).getResultList();
		for (Missbrauchsmeldung missbrauchsmeldung : missbrauchsmeldungen)
			missbrauchsmeldung.getText();
		return missbrauchsmeldungen;
	}

	
	public long countVorgang() {
		return em.createQuery("select count(o) from Vorgang o", Long.class).getSingleResult();
	}

	
	/**
	 * Erzeugt das Grundgerüst der HQL-Anfrage zur Suche von Vorgängen anhand der Parameter im <code>VorgangSuchenCommand</code>
	 * @param cmd Command mit den Parametern zur Suche
	 * @return vorbereitetes Hilfsobjekt für HQL-Anfragen
	 */
	private HqlQueryHelper prepare(VorgangSuchenCommand cmd) {
		HqlQueryHelper query = new HqlQueryHelper();
		
		List<EnumVorgangStatus> unStatus = new ArrayList<EnumVorgangStatus>(Arrays.asList(EnumVorgangStatus.closedVorgangStatus()));

		switch (cmd.getSuchtyp()) {
		case einfach:
			unStatus.add(EnumVorgangStatus.inBearbeitung);
			query.addWhereConditions("vo.zustaendigkeit IN(:zustaendigkeit)")
				.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert=:archiviert)")
				.addParameter("archiviert", Boolean.FALSE);
			if (cmd instanceof VorgangFeedCommand) {
				query.addParameter("zustaendigkeit", Role.toString(((VorgangFeedCommand)cmd).getZustaendigkeiten()));
			} else {
				query.addParameter("zustaendigkeit", Role.toString(securityService.getCurrentZustaendigkeiten(true)));
			}
			switch (cmd.getEinfacheSuche()) {
			case offene:
				query.addWhereConditions("(vo.status=:status1 OR vo.status=:status2)")
					.addParameter("status1", EnumVorgangStatus.offen)
					.addParameter("status2", EnumVorgangStatus.inBearbeitung)
                    .addHavingConditions("(vo.typ!=:unTyp OR vo.status IN (:unStatus) OR COUNT(DISTINCT un.id)>=:unterstuetzer OR vo.erstsichtungErfolgt=:erstsichtungErfolgt OR COUNT(DISTINCT mi.id)>0) ")
                    .addParameter("unTyp", EnumVorgangTyp.idee)
                    .addParameter("unStatus", unStatus)
                    .addParameter("erstsichtungErfolgt", false)
                    .addParameter("unterstuetzer", settingsService.getVorgangIdeeUnterstuetzer());
				break;
            case offeneIdeen:
				query.addWhereConditions("(vo.status=:status)")
					.addParameter("status", EnumVorgangStatus.offen)
                    .addHavingConditions("(vo.typ=:unTyp AND vo.erstsichtungErfolgt=:erstsichtungErfolgt AND COUNT(DISTINCT un.id)<:unterstuetzer) ")
                    .addParameter("unTyp", EnumVorgangTyp.idee)
                    .addParameter("erstsichtungErfolgt", true)
                    .addParameter("unterstuetzer", settingsService.getVorgangIdeeUnterstuetzer());
				break;
			case abgeschlossene:
				query.addWhereConditions("(vo.status IN (:status))")
                    .addParameter("status", Arrays.asList(EnumVorgangStatus.closedVorgangStatus()));
				break;
			}
			break;
		case erweitert:
			//FullText
			if (!StringUtils.isBlank(cmd.getErweitertFulltext())) {
				query.addWhereConditions("(vo.betreff LIKE :fulltext OR " +
						"vo.details LIKE :fulltext OR " +
						"vo.statusKommentar LIKE :fulltext OR " +
						"mi.text LIKE :fulltext OR " +
						"ko.text LIKE :fulltext OR " +
						"vo.kategorie.name LIKE :fulltext OR " +
						"vo.kategorie.parent.name LIKE :fulltext)").addParameter("fulltext", "%"+cmd.getErweitertFulltext().toLowerCase()+"%");
			}
			//Nummer
			if (cmd.getErweitertNummerAsLong()!=null) {
				query.addWhereConditions("(vo.id = :nummer)").addParameter("nummer", cmd.getErweitertNummerAsLong());
			}
			//Kategorie
			if (cmd.getErweitertKategorie()!=null) {
				query.addWhereConditions("(vo.kategorie = :kategorie)").addParameter("kategorie", cmd.getErweitertKategorie());
		 	//Hauptkategorie
			} else if (cmd.getErweitertHauptkategorie()!=null) {
				query.addWhereConditions("(vo.kategorie IN (:kategorie))").addParameter("kategorie", cmd.getErweitertHauptkategorie().getChildren());
			//Typ
			} else if (cmd.getErweitertVorgangTyp()!=null) {
				query.addWhereConditions("vo.typ=:typ").addParameter("typ", cmd.getErweitertVorgangTyp());
			}
			//Status
			{
				List<EnumVorgangStatus> inStatus = Arrays.asList(cmd.getErweitertVorgangStatus());
				List<EnumVorgangStatus> notInStatus = new ArrayList<EnumVorgangStatus>(Arrays.asList(EnumVorgangStatus.values())); 
				notInStatus.removeAll(inStatus);
				if (inStatus.size()!=0) query.addWhereConditions("vo.status IN (:inStatus)").addParameter("inStatus", inStatus);
				if (notInStatus.size()!=0)	query.addWhereConditions("vo.status NOT IN (:notInStatus)").addParameter("notInStatus", notInStatus);
			}
			//Zuständigkeit
			if (!StringUtils.isBlank(cmd.getErweitertZustaendigkeit())) {
				if (cmd.getErweitertZustaendigkeit().equals("#mir zugewiesen#")) {
					query.addWhereConditions("vo.zustaendigkeit IN (:zustaendigkeit)")
						.addParameter("zustaendigkeit", Role.toString(securityService.getCurrentZustaendigkeiten(true)));
				} else {
					query.addWhereConditions("vo.zustaendigkeit=:zustaendigkeit")
						.addParameter("zustaendigkeit", cmd.getErweitertZustaendigkeit());
				}
			}
			//DelegiertAn
			if (!StringUtils.isBlank(cmd.getErweitertDelegiertAn())) {
				query.addWhereConditions("vo.delegiertAn=:delegiertAn")
					.addParameter("delegiertAn", cmd.getErweitertDelegiertAn());
			}
			//Datum
			if (cmd.getErweitertDatumVon()!=null) {
				query.addWhereConditions("vo.datum>=:datumVon")
					.addParameter("datumVon", DateUtils.truncate(cmd.getErweitertDatumVon(), Calendar.DAY_OF_MONTH));
			}
			if (cmd.getErweitertDatumBis()!=null) {
				query.addWhereConditions("vo.datum<=:datumBis")
					.addParameter("datumBis", DateUtils.ceiling(cmd.getErweitertDatumBis(), Calendar.DAY_OF_MONTH));
			}
			//Archiviert
			if (cmd.getErweitertArchiviert()!=null) {
				if (cmd.getErweitertArchiviert()) {
					query.addWhereConditions("vo.archiviert=:archiviert")
						.addParameter("archiviert", Boolean.TRUE);
				} else {
					query.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert=:archiviert)")
						.addParameter("archiviert", Boolean.FALSE);
				}
			}
			//Unterstützer
			if (cmd.getErweitertUnterstuetzerAb()!=null) {
				unStatus.add(EnumVorgangStatus.inBearbeitung);
				query.addHavingConditions("(vo.typ!=:unTyp OR vo.erstsichtungErfolgt=:erstsichtungErfolgt OR vo.status IN (:unStatus) OR COUNT(DISTINCT un.id)>=:unterstuetzer)")
					.addParameter("unTyp", EnumVorgangTyp.idee)
					.addParameter("erstsichtungErfolgt", false)
					.addParameter("unStatus", unStatus)
					.addParameter("unterstuetzer", cmd.getErweitertUnterstuetzerAb());
			}
			//Priorität
			if (cmd.getErweitertPrioritaet()!=null) {
				query.addWhereConditions("vo.prioritaet=:prioritaet")
					.addParameter("prioritaet", cmd.getErweitertPrioritaet());
			}
			//Stadtteil
			if (cmd.getErweitertStadtteilgrenze()!=null) {
				query.addWhereConditions("within(vo.ovi, (SELECT g.grenze FROM StadtteilGrenze g WHERE g.id=:stadtteil))=true")
					.addParameter("stadtteil", cmd.getErweitertStadtteilgrenze());
			}
			break;
		}
		return query;
	}
	
	/**
	 * Ermittelt die Ergebnisanzahl für eine definierte parametrisierte Anfrage nach Vorgängen. 
	 * @param cmd Command mit den Parametern zur Suche
	 * @return Anzahl der Vorgänge im Suchergebnis
	 */
	@SuppressWarnings("unchecked")
	public long countVorgang(VorgangSuchenCommand cmd) {
		HqlQueryHelper query = addGroupByVorgang(prepare(cmd), false)
			.addFromTables("Vorgang vo " +
					"JOIN vo.verlauf ve " +
					"LEFT JOIN vo.unterstuetzer un WITH un.datumBestaetigung IS NOT NULL " +
					"LEFT JOIN vo.missbrauchsmeldungen mi WITH mi.datumBestaetigung IS NOT NULL AND mi.datumAbarbeitung IS NULL " +
					"LEFT JOIN vo.kommentare ko")
			.addSelectAttribute("vo.id");
		List<Long> ids = query.getResultList(em);
		return ids.size();
	}
	
	
	/**
	 * Fügt die GroupBy-Terme zu einer HQL-Anfrage hinzu, wenn in der Anfrage nach dem Vorgang gruppiert werden soll. Die Parameter für
	 * die Projektion auf die Vorgangsattributte werden dabei zur HQL-Anfrage hinzugefügt.
	 * @param query Hilfsobjekt für HQL-Anfragen
	 * @return verändertes Hilfsobjekt für HQL-Anfragen
	 */
	private HqlQueryHelper addGroupByVorgang(HqlQueryHelper query) {
		return addGroupByVorgang(query, true);
	}
	
	
	/**
	 * Fügt die GroupBy-Terme zu einer HQL-Anfrage hinzu, wenn in der Anfrage nach dem Vorgang gruppiert werden soll. Die Parameter für
	 * die Projektion auf die Vorgangsattribute werden dabei ggf. zur HQL-Anfrage hinzugefügt.
	 * @param query Hilfsobjekt für HQL-Anfragen
	 * @param addSelectAttribute Sollen die Projektionen auf die Vorgangsattribute mit in die HQL-Anfrage aufgenommen werden?
	 * @return verändertes Hilfsobjekt für HQL-Anfragen
	 */
	protected HqlQueryHelper addGroupByVorgang(HqlQueryHelper query, boolean addSelectAttribute) {
		if (addSelectAttribute) query.addSelectAttribute("vo");
		query
			.addGroupByAttribute("vo.id")
			.addGroupByAttribute("vo.version")
			.addGroupByAttribute("vo.datum")
			.addGroupByAttribute("vo.typ")
			.addGroupByAttribute("vo.betreff")
			.addGroupByAttribute("vo.betreffFreigabeStatus")
			.addGroupByAttribute("vo.details")
			.addGroupByAttribute("vo.detailsFreigabeStatus")
			.addGroupByAttribute("vo.ovi")
			.addGroupByAttribute("vo.autorEmail")
			.addGroupByAttribute("vo.adresse")
			.addGroupByAttribute("vo.flurstueckseigentum")
			.addGroupByAttribute("vo.hash")
			.addGroupByAttribute("vo.status")
			.addGroupByAttribute("vo.statusOrdinal")
			.addGroupByAttribute("vo.statusKommentar")
			.addGroupByAttribute("vo.erstsichtungErfolgt")
			.addGroupByAttribute("vo.fotoNormalJpg")
			.addGroupByAttribute("vo.fotoThumbJpg")
			.addGroupByAttribute("vo.fotoFreigabeStatus")
			.addGroupByAttribute("vo.zustaendigkeit")
			.addGroupByAttribute("vo.zustaendigkeitStatus")
			.addGroupByAttribute("vo.delegiertAn")
			.addGroupByAttribute("vo.kategorie")
			.addGroupByAttribute("vo.prioritaet")
			.addGroupByAttribute("vo.prioritaetOrdinal")
			.addGroupByAttribute("vo.archiviert")
			.addGroupByAttribute("vo.kategorie.name")
			.addGroupByAttribute("vo.kategorie.parent.name");
		return query;
	}
	
	
	/**
	 * Ermittelt die Liste der Vorgänge zur Suche anhand der Parameter im <code>VorgangSuchenCommand</code>
	 * @param cmd Command mit den Parametern zur Suche
	 * @return Ergebnisliste der Vorgänge
	 * @see #prepare(VorgangSuchenCommand)
	 * @see #addGroupByVorgang(HqlQueryHelper)
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> listVorgang(VorgangSuchenCommand cmd) {
		HqlQueryHelper query = addGroupByVorgang(prepare(cmd))
			.addSelectAttribute("MAX(ve.datum)")
			.addSelectAttribute("COUNT(DISTINCT un.id)")
			.addSelectAttribute("COUNT(DISTINCT mi.id) AS missbrauch")
			.addFromTables("Vorgang vo " +
					"JOIN vo.verlauf ve " +
					"LEFT JOIN vo.unterstuetzer un WITH un.datumBestaetigung IS NOT NULL " +
					"LEFT JOIN vo.missbrauchsmeldungen mi WITH mi.datumBestaetigung IS NOT NULL AND mi.datumAbarbeitung IS NULL " +
					"LEFT JOIN vo.kommentare ko");
		if (cmd.getPage()!=null && cmd.getSize()!=null)
			query.firstResult((cmd.getPage()-1)*cmd.getSize());
		if (cmd.getSize()!=null)
			query.maxResults(cmd.getSize());
		
		query.orderBy("missbrauch DESC");
		for(String field : cmd.getOrderString().split(","))
			query.orderBy(field.trim()+" "+cmd.getOrderDirectionString());
		
		return query.getResultList(em);
	}

	
	/**
	 * Ermittelt die Anzahl der offenen Missbrauchsmeldungen für abgeschlosse Vorgänge.
	 * Bei der Suche werden die Rollen des aktuellen Benutzers berücksichtigt.
	 * @return Anzahl der offenen Missbrauchsmeldungen
	 */
	public long missbrauchsmeldungenAbgeschlossenenVorgaenge() {
		HqlQueryHelper query = new HqlQueryHelper()
			.addFromTables("Vorgang vo JOIN vo.missbrauchsmeldungen mi WITH mi.datumBestaetigung IS NOT NULL AND mi.datumAbarbeitung IS NULL ")
			.addSelectAttribute("COUNT(DISTINCT vo.id)")
			.addWhereConditions("(vo.status IN (:status))")
			.addParameter("status", Arrays.asList(EnumVorgangStatus.closedVorgangStatus()))
			.addWhereConditions("vo.zustaendigkeit IN(:zustaendigkeit)")
			.addParameter("zustaendigkeit", Role.toString(securityService.getCurrentZustaendigkeiten(true)))
			.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert=:archiviert)")
			.addParameter("archiviert", Boolean.FALSE);
		return (Long)query.getSingleResult(em);
	}

	
	
	/**
	 * Erzeugt das Grundgerüst der HQL-Anfrage zur Suche von Vorgängen anhand der Parameter im <code>VorgangDelegiertSuchenCommand</code>
	 * für die Suche im Bereich für Externe (Delegierte)
	 * Die Rollen des aktuell angemeldeten Benutzers werden dabei berücksichtigt.
	 * @param cmd Command mit den Parametern zur Suche
	 * @return vorbereitetes Hilfsobjekt für HQL-Anfragen
	 */
	private HqlQueryHelper prepareForDelegiertSuche(VorgangDelegiertSuchenCommand cmd) {
		HqlQueryHelper query = new HqlQueryHelper();
		
		query.addWhereConditions("vo.delegiertAn IN (:delegiertAn)")
			.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert=:archiviert)")
			.addParameter("archiviert", Boolean.FALSE);

		if (cmd instanceof VorgangFeedDelegiertAnCommand) {
			query.addParameter("delegiertAn", Role.toString(((VorgangFeedDelegiertAnCommand)cmd).getDelegiertAn()));
		} else {
			query.addParameter("delegiertAn", Role.toString(securityService.getCurrentDelegiertAn()));
		}
		
		switch (cmd.getEinfacheSuche()) {
			case offene:
				query.addWhereConditions("(vo.status=:status1 OR vo.status=:status2)")
				.addParameter("status1", EnumVorgangStatus.offen)
				.addParameter("status2", EnumVorgangStatus.inBearbeitung);
				break;
			case abgeschlossene:
				query.addWhereConditions("(vo.status IN (:status))")
				.addParameter("status", Arrays.asList(EnumVorgangStatus.closedVorgangStatus()));
				break;
			}
		return query;
	}

	
	/**
	 * Ermittelt die Liste der Vorgänge zur Suche anhand der Parameter im <code>VorgangDelegiertSuchenCommand</code>.
	 * Die Rollen des aktuellen Benutzers werden dabei berücksichtigt.
	 * @param cmd Command mit den Parametern zur Suche
	 * @return Ergebnisliste der Vorgänge
	 * @see #prepareForDelegiertSuche(VorgangDelegiertSuchenCommand)
	 */
	@SuppressWarnings("unchecked")
	public List<Vorgang> listVorgang(VorgangDelegiertSuchenCommand cmd) {
		HqlQueryHelper query = prepareForDelegiertSuche(cmd)
			.addSelectAttribute("vo")
			.addFromTables("Vorgang vo");
		if (cmd.getPage()!=null && cmd.getSize()!=null)
			query.firstResult((cmd.getPage()-1)*cmd.getSize());
		if (cmd.getSize()!=null)
			query.maxResults(cmd.getSize());
		
		for(String field : cmd.getOrderString().split(","))
			query.orderBy(field.trim()+" "+cmd.getOrderDirectionString());
		
		return query.getResultList(em);
	}

	
	/**
	 * Ermittelt die Ergebnisanzahl für eine definierte parametrisierte Anfrage nach Vorgängen. 
	 * Die Rollen des aktuellen Benutzers werden dabei berücksichtigt.
	 * @param cmd Command mit den Parametern zur Suche
	 * @return Anzahl der Vorgänge im Suchergebnis
	 * @see #prepareForDelegiertSuche(VorgangDelegiertSuchenCommand)
	 */
	public long countVorgang(VorgangDelegiertSuchenCommand cmd) {
		HqlQueryHelper query = prepareForDelegiertSuche(cmd)
			.addFromTables("Vorgang vo")
			.addSelectAttribute("COUNT(vo)");
		return (Long)query.getSingleResult(em);
	}

	
	/**
	 * Ermittelt alle Vorgänge, die noch nicht archiviert wurden und bis zu einem bestimmten Zeitpunkt abgeschlossen wurden. 
	 * @param versionBefor Zeitpunkt, bis zu dem die Vorgänge abgeschlossen sein sollten
	 * @return Ergebnisliste der Vorgänge
	 * @see de.fraunhofer.igd.klarschiff.service.job.JobsService#archivVorgaenge()
	 */
	public List<Vorgang> findNotArchivVorgang(Date versionBefor) {
	    return em.createQuery("select o from Vorgang o WHERE o.status IN (:status) AND version<:versionBefor AND archiviert IS NULL", Vorgang.class)
		    .setParameter("status", Arrays.asList(EnumVorgangStatus.closedVorgangStatus()))
		    .setParameter("versionBefor", versionBefor)
		    .getResultList();	    
	}

	
	/**
	 * Ermittelt alle Vorgänge, die bis zu einem bestimmten Zeitpunkt gemeldet, aber noch nicht bestätigt wurden.
	 * @param datumBefor Zeitpunkt bis zu dem die Vorgänge gemeldet wurden
	 * @return Ergebnisliste der Vorgänge
	 * @see de.fraunhofer.igd.klarschiff.service.job.JobsService#removeUnbestaetigtVorgang()
	 */
	public List<Vorgang> findUnbestaetigtVorgang(Date datumBefor) {
		return em.createQuery("select o from Vorgang o WHERE o.status=:status AND datum<:datumBefor", Vorgang.class)
			.setParameter("status", EnumVorgangStatus.gemeldet)
			.setParameter("datumBefor", datumBefor)
			.getResultList();	    
	}

	
	/**
	 * Ermittelt alle Unterstützungen, die bis zu einem bestimmten Zeitpunkt abgegeben aber noch nicht bestätigt wurden
	 * @param datumBefor Zeitpunkt bis zu dem die Unterstützungen abgegeben wurden
	 * @return Ergebnisliste mit Unterstützungen
	 * @see de.fraunhofer.igd.klarschiff.service.job.JobsService#removeUnbestaetigtUnterstuetzer()
	 */
	public List<Unterstuetzer> findUnbestaetigtUnterstuetzer(Date datumBefor) {
	    return em.createQuery("select o from Unterstuetzer o WHERE o.datumBestaetigung IS NULL AND datum<:datumBefor", Unterstuetzer.class)
		    .setParameter("datumBefor", datumBefor)
		    .getResultList();	    
	}
	
	
	/**
	 * Ermittelt alle Missbrauchsmeldungen, die bis zu einem bestimmten Zeitpunkt abgegeben aber noch nicht bestätigt wurden
	 * @param datumBefor Zeitpunkt bis zu dem die Missbrauchsmeldungen abgegeben wurden
	 * @return Ergebnisliste mit Missbrauchsmeldungen
	 * @see de.fraunhofer.igd.klarschiff.service.job.JobsService#removeUnbestaetigtMissbrauchsmeldung()
	 */
	public List<Missbrauchsmeldung> findUnbestaetigtMissbrauchsmeldung(Date datumBefor) {
	    return em.createQuery("select o from Missbrauchsmeldung o WHERE o.datumBestaetigung IS NULL AND datum<:datumBefor", Missbrauchsmeldung.class)
		    .setParameter("datumBefor", datumBefor)
		    .getResultList();	    
	}
	
	/**
	 * Ermittelt alle Statuskommentarvorlagen
	 * @return Ergebisliste mit Statuskommentarvorlagen
	 */
	public List<StatusKommentarVorlage> findStatusKommentarVorlage() {
		return em.createQuery("select o from StatusKommentarVorlage o", StatusKommentarVorlage.class).getResultList();
	}	
	
	
	/**
	 * Ermittelt alle berechneten und gespeicherten Features für einen Vorgang.
	 * @param vorgang Vorgang zu dem die Features ermittelt werden sollen
	 * @return Ermittelte Features für den gegebenen Vorgang
	 */
	@Transactional
	public VorgangFeatures findVorgangFeatures(Vorgang vorgang) {
		if (vorgang == null) return null;
		try {
			return em.createQuery("select o from VorgangFeatures o WHERE o.vorgang=:vorgang", VorgangFeatures.class).setParameter("vorgang", vorgang).getSingleResult();
		}catch (Exception e) {
			return null;
		}
	}

	/**
	 * Ermittelt alle bisher gewählten Zuständigkeiten für einen Vorgang
	 * @param vorgang Vorgang für den die bisher verwendeten Zuständigkeiten ermittelt wurden
	 * @return bisher verwendete Zuständigkeiten für ein Vorgang
	 */
	@Transactional
	public VorgangHistoryClasses findVorgangHistoryClasses(Vorgang vorgang) {
		if (vorgang == null) return null;
		try {
			return em.createQuery("select o from VorgangHistoryClasses o WHERE o.vorgang=:vorgang", VorgangHistoryClasses.class).setParameter("vorgang", vorgang).getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}
	
	
	/**
	 * Ermittlet die aktuellsten Vorgänge, die eine akzeptierte Zuständigkeit besitzen zum trainieren des Zuständigkeitsfinders 
	 * @param maxResult maximale Anzahl von Vorgängen in der Ergebnisliste 
	 * @return Vorgänge mit akzeptierten Zuständigkeiten
	 */
	@SuppressWarnings("unchecked")
	public List<Vorgang> findVorgangForTrainClassificator(int maxResult) {
		HqlQueryHelper query = addGroupByVorgang(new HqlQueryHelper())
			.addFromTables("Vorgang vo JOIN vo.verlauf ve")
			.addWhereConditions("vo.zustaendigkeitStatus=:zustaendigkeitStatus")
			.addParameter("zustaendigkeitStatus", EnumZustaendigkeitStatus.akzeptiert)
			.orderBy("MAX(ve.datum) DESC")
			.maxResults(maxResult);
		return query.getResultList(em);
	}

	
	/**
	 * Ermittelt alle Vorgänge, bei denen ab einer bestimmten Zeit die Zuständigkeit geändert wurde. 
	 * @param lastChange Zeitpunkt ab dem die Zuständigkeit geändert wurde
	 * @param zustaendigkeit Zuständigkeit
	 * @return Liste mit Vorgängen
	 * @see de.fraunhofer.igd.klarschiff.service.job.JobsService#informDispatcher()
	 */
	@SuppressWarnings("unchecked")
	public List<Vorgang> findVorgaengeForZustaendigkeit(Date lastChange, String zustaendigkeit) {
		HqlQueryHelper query = addGroupByVorgang(new HqlQueryHelper())
			.addFromTables("Vorgang vo JOIN vo.verlauf ve")
			.addWhereConditions("ve.typ=:verlaufTyp").addParameter("verlaufTyp", EnumVerlaufTyp.zustaendigkeit)
			.addWhereConditions("ve.datum>=:datum").addParameter("datum", lastChange)
			.addWhereConditions("vo.zustaendigkeit=:zustaendigkeit").addParameter("zustaendigkeit", zustaendigkeit);
		return query.getResultList(em);
	}
	
	
	/**
	 * Ermittelt alle Vorgänge, bei denen ab einer bestimmten Zeit delegiert wurde.
	 * @param lastChange Zeitpunkt ab dem die Vorgänge delegiert wurden
	 * @param delegiertAn Delegiert an
	 * @return Liste mit Vorgängen
 	 * @see de.fraunhofer.igd.klarschiff.service.job.JobsService#informExtern()
	 */
	@SuppressWarnings("unchecked")
	public List<Vorgang> findVorgaengeForDelegiertAn(Date lastChange, String delegiertAn) {
		HqlQueryHelper query = addGroupByVorgang(new HqlQueryHelper())
			.addFromTables("Vorgang vo JOIN vo.verlauf ve")
			.addWhereConditions("ve.typ=:verlaufTyp").addParameter("verlaufTyp", EnumVerlaufTyp.delegiertAn)
			.addWhereConditions("ve.datum>=:datum").addParameter("datum", lastChange)
			.addWhereConditions("vo.delegiertAn=:delegiertAn").addParameter("delegiertAn", delegiertAn);
		return query.getResultList(em);
	}
    
    
    /**
	 * Ermittelt alle Vorgänge, die ab einer bestimmten Zeit den Status "in Bearbeitung" erhalten haben.
	 * @param lastChange Zeitpunkt, ab dem die Vorgänge den Status "in Bearbeitung" erhalten haben.
	 * @return Liste mit Vorgängen
	 */
	@SuppressWarnings("unchecked")
	public List<Vorgang> findInProgressVorgaenge(Date lastChange) {
		HqlQueryHelper query = addGroupByVorgang(new HqlQueryHelper())
			.addFromTables("Vorgang vo JOIN vo.verlauf ve")
			.addWhereConditions("ve.typ=:verlaufTyp").addParameter("verlaufTyp", EnumVerlaufTyp.status)
			.addWhereConditions("ve.datum>=:datum").addParameter("datum", lastChange)
			.addWhereConditions("vo.status IN (:status)").addParameter("status", Arrays.asList(EnumVorgangStatus.inProgressVorgangStatus()))
			.addWhereConditions("vo.autorEmail IS NOT NULL")
			.addWhereConditions("vo.autorEmail!=:autorEmail").addParameter("autorEmail", "");
		return query.getResultList(em);
	}

	
	/**
	 * Ermittelt alle Vorgänge, die ab einer bestimmten Zeit abgeschlossen wurden.
	 * @param lastChange Zeitpunkt, ab dem die Vorgänge abgeschlossen wurden.
	 * @return Liste mit Vorgängen
	 */
	@SuppressWarnings("unchecked")
	public List<Vorgang> findClosedVorgaenge(Date lastChange) {
		HqlQueryHelper query = addGroupByVorgang(new HqlQueryHelper())
			.addFromTables("Vorgang vo JOIN vo.verlauf ve")
			.addWhereConditions("ve.typ=:verlaufTyp").addParameter("verlaufTyp", EnumVerlaufTyp.status)
			.addWhereConditions("ve.datum>=:datum").addParameter("datum", lastChange)
			.addWhereConditions("vo.status IN (:status)").addParameter("status", Arrays.asList(EnumVorgangStatus.closedVorgangStatus()))
			.addWhereConditions("vo.autorEmail IS NOT NULL")
			.addWhereConditions("vo.autorEmail!=:autorEmail").addParameter("autorEmail", "");
		return query.getResultList(em);
	}

	
	/**
	 * Ermittelt die Zuständigkeit für einen Vorgang
	 * @param vorgangId Id des Vorgangs
	 * @return Zuständigkeit des Vorgangs
	 */
	public String getZustaendigkeitForVorgang(Long vorgangId) {
		return em.createQuery("SELECT vo.zustaendigkeit FROM Vorgang vo WHERE vo.id=:id", String.class).setParameter("id", vorgangId).getSingleResult();
	}

	
	/**
	 * Ermittelt für einen Vorgang an wen dieser delegiert wurde
	 * @param vorgangId Id des Vorgangs
	 * @return Delegiert an
	 */
	public String getDelegiertAnForVorgang(Long vorgangId) {
		return em.createQuery("SELECT vo.delegiertAn FROM Vorgang vo WHERE vo.id=:id", String.class).setParameter("id", vorgangId).getSingleResult();
	}
    
    
    /**
	 * Ermittelt alle Vorgänge mit dem Status 'offen', die seit einem bestimmten Datum einer bestimmten Zuständigkeit zugewiesen sind, bisher aber nicht akzeptiert wurden.
	 * @param administrator Zuständigkeit ignorieren?
	 * @param zustaendigkeit Zuständigkeit, der die Vorgänge zugewiesen sind
	 * @param datum Datum, seit dem die Vorgänge zugewiesen sind
	 * @return Liste mit Vorgängen
	 */
	@SuppressWarnings("unchecked")
    public List<Vorgang> findVorgaengeOffenNichtAkzeptiert(Boolean administrator, String zustaendigkeit, Date datum) {
		HqlQueryHelper query = (new HqlQueryHelper()).addSelectAttribute("vo")
			.addFromTables("Vorgang vo")
			.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
			.addWhereConditions("vo.status = 'offen'")
			.addWhereConditions("vo.zustaendigkeitStatus != 'akzeptiert'")
            .addWhereConditions("vo.version <= :datum").addParameter("datum", datum);
        if (administrator == false)
            query.addWhereConditions("vo.zustaendigkeit = :zustaendigkeit").addParameter("zustaendigkeit", zustaendigkeit);
        query.orderBy("vo.zustaendigkeit, vo.id");
		return query.getResultList(em);
	}
    
    
    /**
	 * Ermittelt alle Vorgänge mit dem Status 'in Bearbeitung', die einer bestimmten Zuständigkeit zugewiesen sind und seit einem bestimmten Datum nicht mehr verändert wurden, bisher aber keine Info der Verwaltung aufweisen.
	 * @param administrator Zuständigkeit ignorieren?
	 * @param zustaendigkeit Zuständigkeit, der die Vorgänge zugewiesen sind
	 * @param datum Datum, seit dem die Vorgänge zugewiesen sind
	 * @return Liste mit Vorgängen
	 */
	@SuppressWarnings("unchecked")
    public List<Vorgang> findVorgaengeInbearbeitungOhneStatusKommentar(Boolean administrator, String zustaendigkeit, Date datum) {
		HqlQueryHelper query = (new HqlQueryHelper()).addSelectAttribute("vo")
			.addFromTables("Vorgang vo")
			.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
			.addWhereConditions("vo.status = 'inBearbeitung'")
			.addWhereConditions("(vo.statusKommentar IS NULL OR vo.statusKommentar = '')")
            .addWhereConditions("vo.version <= :datum").addParameter("datum", datum);
        if (administrator == false)
            query.addWhereConditions("vo.zustaendigkeit = :zustaendigkeit").addParameter("zustaendigkeit", zustaendigkeit);
        query.orderBy("vo.zustaendigkeit, vo.id");
		return query.getResultList(em);
	}
    
    
    /**
	 * Ermittelt alle Vorgänge des Typs 'idee' mit dem Status 'offen', die ihre Erstsichtung seit einem bestimmten Datum hinter sich haben, bisher aber noch nicht die Zahl der notwendigen Unterstützungen aufweisen.
	 * @param administrator Zuständigkeit ignorieren?
	 * @param zustaendigkeit Zuständigkeit, der die Vorgänge zugewiesen sind
	 * @param datum Datum, seit dem die Erstsichtung abgeschlossen ist
	 * @return Liste mit Vorgängen
	 */
	@SuppressWarnings("unchecked")
    public List<Vorgang> findVorgaengeIdeeOffenOhneUnterstuetzung(Boolean administrator, String zustaendigkeit, Date datum) {
		HqlQueryHelper query = addGroupByVorgang(new HqlQueryHelper())
			.addFromTables("Vorgang vo JOIN vo.verlauf ve")
			.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
			.addWhereConditions("vo.typ = 'idee'")
			.addWhereConditions("vo.status = 'offen'")
            .addWhereConditions("vo.erstsichtungErfolgt = TRUE")
            .addWhereConditions("ve.typ = 'zustaendigkeitAkzeptiert'")
            .addWhereConditions("ve.datum <= :datum").addParameter("datum", datum)
            .addWhereConditions("(SELECT COUNT(*) FROM Unterstuetzer un WHERE un.vorgang = vo.id) < :unterstuetzer").addParameter("unterstuetzer", settingsService.getVorgangIdeeUnterstuetzer());
        if (administrator == false)
            query.addWhereConditions("vo.zustaendigkeit = :zustaendigkeit").addParameter("zustaendigkeit", zustaendigkeit);
        query.orderBy("vo.zustaendigkeit, vo.id");
		return query.getResultList(em);
	}
    
    
    /**
	 * Ermittelt alle Vorgänge mit dem Status 'wird nicht bearbeitet', die bisher keine Info der Verwaltung aufweisen.
	 * @param administrator Zuständigkeit ignorieren?
	 * @param zustaendigkeit Zuständigkeit, der die Vorgänge zugewiesen sind
	 * @return Liste mit Vorgängen
	 */
	@SuppressWarnings("unchecked")
    public List<Vorgang> findVorgaengeWirdnichtbearbeitetOhneStatuskommentar(Boolean administrator, String zustaendigkeit) {
		HqlQueryHelper query = (new HqlQueryHelper()).addSelectAttribute("vo")
			.addFromTables("Vorgang vo")
			.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
			.addWhereConditions("vo.status = 'wirdNichtBearbeitet'")
			.addWhereConditions("(vo.statusKommentar IS NULL OR vo.statusKommentar = '')");
        if (administrator == false)
            query.addWhereConditions("vo.zustaendigkeit = :zustaendigkeit").addParameter("zustaendigkeit", zustaendigkeit);
        query.orderBy("vo.zustaendigkeit, vo.id");
		return query.getResultList(em);
	}
    
    
    /**
	 * Ermittelt alle Vorgänge, die zwar nicht mehr den Status 'offen' aufweisen, bisher aber dennoch nicht akzeptiert wurden.
	 * @param administrator Zuständigkeit ignorieren?
	 * @param zustaendigkeit Zuständigkeit, der die Vorgänge zugewiesen sind
	 * @return Liste mit Vorgängen
	 */
	@SuppressWarnings("unchecked")
    public List<Vorgang> findVorgaengeNichtMehrOffenNichtAkzeptiert(Boolean administrator, String zustaendigkeit) {
		HqlQueryHelper query = (new HqlQueryHelper()).addSelectAttribute("vo")
			.addFromTables("Vorgang vo")
			.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
			.addWhereConditions("vo.status NOT IN ('gemeldet','offen')")
			.addWhereConditions("vo.zustaendigkeitStatus != 'akzeptiert'");
        if (administrator == false)
            query.addWhereConditions("vo.zustaendigkeit = :zustaendigkeit").addParameter("zustaendigkeit", zustaendigkeit);
        query.orderBy("vo.zustaendigkeit, vo.id");
		return query.getResultList(em);
	}
    
    
    /**
	 * Ermittelt alle Vorgänge, die ihre Erstsichtung bereits hinter sich haben, deren Betreff, Details oder Foto bisher aber noch nicht freigegeben wurden.
	 * @param administrator Zuständigkeit ignorieren?
	 * @param zustaendigkeit Zuständigkeit, der die Vorgänge zugewiesen sind
	 * @return Liste mit Vorgängen
	 */
	@SuppressWarnings("unchecked")
    public List<Vorgang> findVorgaengeOhneRedaktionelleFreigaben(Boolean administrator, String zustaendigkeit) {
		HqlQueryHelper query = (new HqlQueryHelper()).addSelectAttribute("vo")
			.addFromTables("Vorgang vo")
			.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
			.addWhereConditions("vo.status IN ('offen', 'inBearbeitung', 'wirdNichtBearbeitet', 'abgeschlossen')")
			.addWhereConditions("vo.erstsichtungErfolgt = TRUE")
			.addWhereConditions("((vo.betreff IS NOT NULL AND vo.betreff != '' AND (betreffFreigabeStatus IS NULL OR betreffFreigabeStatus = 'intern')) OR (vo.details IS NOT NULL AND vo.details != '' AND (detailsFreigabeStatus IS NULL OR detailsFreigabeStatus = 'intern')) OR (length(vo.fotoThumbJpg) IS NOT NULL AND (fotoFreigabeStatus IS NULL OR fotoFreigabeStatus = 'intern')))");
        if (administrator == false)
            query.addWhereConditions("vo.zustaendigkeit = :zustaendigkeit").addParameter("zustaendigkeit", zustaendigkeit);
        query.orderBy("vo.zustaendigkeit, vo.id");
		return query.getResultList(em);
	}
    
    
    /**
	 * Ermittelt alle Vorgänge, die auf Grund von Kommunikationsfehlern im System keine Einträge in den Datenfeldern 'zustaendigkeit' und/oder 'zustaendigkeit_status' aufweisen.
	 * @param administrator Zuständigkeit ignorieren?
	 * @return Liste mit Vorgängen
	 */
	@SuppressWarnings("unchecked")
    public List<Vorgang> findVorgaengeOhneZustaendigkeit(Boolean administrator) {
		HqlQueryHelper query = (new HqlQueryHelper()).addSelectAttribute("vo")
			.addFromTables("Vorgang vo")
			.addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
			.addWhereConditions("vo.status != 'gemeldet'")
			.addWhereConditions("(vo.zustaendigkeit IS NULL OR vo.zustaendigkeitStatus IS NULL)")
            .orderBy("vo.id");
		return query.getResultList(em);
	}
}
