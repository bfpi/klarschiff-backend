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
import java.math.BigInteger;
import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;

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
			if (vorgangOld.getFotowunsch() != vorgang.getFotowunsch()) verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.fotowunsch, vorgangOld.getFotowunsch() ? "aktiv" : "inaktiv", vorgang.getFotowunsch() ? "aktiv" : "inaktiv");
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
	public List<Vorgang> findVorgaenge(Long[] ids) {
		if (ids == null) return null;
    return em.createQuery("select o from Vorgang o where o.id in (:ids)", Vorgang.class).setParameter("ids", Arrays.asList(ids)).getResultList();
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
	public List<Vorgang> listVorgang() {
		return em.createQuery("select o from Vorgang o", Vorgang.class).getResultList();
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
   * F�gt zu einem StringBuilder den WHERE-Teil einer SQL-Query zur Suche von
   * Vorg�ngen anhand der Parameter im <code>VorgangSuchenCommand</code>
   * hinzu.
   *
   * @param cmd Command mit den Parametern zur Suche
   * @param sql StringBuilder an den angeh�ngt wird
   * @return StringBuilder an den angeh�ngt wird mit WHERE
   */
  private StringBuilder addFilter(VorgangSuchenCommand cmd, StringBuilder sql) {
    List<EnumVorgangStatus> unStatus = new ArrayList<EnumVorgangStatus>(Arrays.asList(EnumVorgangStatus.closedVorgangStatus()));

    ArrayList<String> conds = new ArrayList<String>();
    switch (cmd.getSuchtyp()) {
      case einfach:
        unStatus.add(EnumVorgangStatus.inBearbeitung);
         {
          List<String> zustaendigkeiten;
          if (cmd instanceof VorgangFeedCommand) {
            zustaendigkeiten = Role.toString(((VorgangFeedCommand) cmd).getZustaendigkeiten());
          } else {
            zustaendigkeiten = Role.toString(securityService.getCurrentZustaendigkeiten(true));
          }
          conds.add("vo.zustaendigkeit IN ('" + StringUtils.join(zustaendigkeiten, "', '") + "')");
        }
        conds.add("vo.archiviert IS NULL OR NOT vo.archiviert");
        switch (cmd.getEinfacheSuche()) {
          case offene:
            conds.add("vo.status IN ('" + EnumVorgangStatus.offen + "', '"
                    + EnumVorgangStatus.inBearbeitung + "')");
            conds.add("vo.typ != '" + EnumVorgangTyp.idee + "'"
                    + " OR vo.status IN ('" + StringUtils.join(unStatus, "', '") + "')"
                    + " OR un.count >= " + settingsService.getVorgangIdeeUnterstuetzer()
                    + " OR NOT vo.erstsichtung_erfolgt "
                    + " OR mi.count > 0");
            break;
          case offeneIdeen:
            conds.add("vo.status IN ('" + EnumVorgangStatus.offen + "')");
            conds.add("vo.typ = '" + EnumVorgangTyp.idee + "'"
                    + " AND (un.count < " + settingsService.getVorgangIdeeUnterstuetzer() + " OR vo.id NOT IN (SELECT DISTINCT vorgang FROM klarschiff_unterstuetzer))"
                    + " AND vo.erstsichtung_erfolgt ");
            break;
          case abgeschlossene:
            conds.add("vo.status in ('" + StringUtils.join(EnumVorgangStatus.closedVorgangStatus(), "', '") + "')");
            break;
        }
        break;
      case erweitert:
      case aussendienst:
        if(cmd.getSuchtyp() == VorgangSuchenCommand.Suchtyp.aussendienst) {
          conds.add("vo.zustaendigkeit_status = 'akzeptiert'");
        }
        //FullText
        if (!StringUtils.isBlank(cmd.getErweitertFulltext())) {
          String text = StringEscapeUtils.escapeSql("%" + cmd.getErweitertFulltext() + "%");
          conds.add("vo.betreff ILIKE '" + text + "'"
                  + " OR vo.details ILIKE '" + text + "'"
                  + " OR vo.status_kommentar ILIKE '" + text + "'"
                  + " OR vo.id IN (SELECT vorgang FROM klarschiff_missbrauchsmeldung "
                  + "   WHERE datum_bestaetigung IS NOT NULL AND text ILIKE '" + text + "')"
                  + " OR vo.id IN (SELECT vorgang FROM klarschiff_kommentar "
                  + "   WHERE NOT geloescht AND text ILIKE '" + text + "')"
                  + " OR vo.kategorie IN (SELECT id FROM klarschiff_kategorie WHERE name ILIKE '" + text + "')"
                  + " OR vo.kategorie IN (SELECT id FROM klarschiff_kategorie WHERE parent in ("
                  + "SELECT id FROM klarschiff_kategorie WHERE name ILIKE '" + text + "'))");
        }
        //Nummer
        if (cmd.getErweitertNummerAsLong() != null) {
          conds.add("vo.id = " + cmd.getErweitertNummerAsLong());
        }
        if (cmd.getVorgangAuswaehlen() != null && cmd.getVorgangAuswaehlen().length > 0) {
          conds.add("vo.id in (" + StringUtils.join(cmd.getVorgangAuswaehlen(), ",")  + ")");
        }
        
        //Kategorie
        if (cmd.getErweitertKategorie() != null) {
          conds.add("vo.kategorie = " + cmd.getErweitertKategorie().getId());
          //Hauptkategorie
        } else if (cmd.getErweitertHauptkategorie() != null) {
          conds.add("vo.kategorie IN (SELECT id FROM klarschiff_kategorie WHERE parent = " + cmd.getErweitertHauptkategorie().getId() + ")");
          //Typ
        } else if (cmd.getErweitertVorgangTyp() != null) {
          conds.add("vo.typ = '" + cmd.getErweitertVorgangTyp().name() + "'");
        }
        //Status
         {
          List<EnumVorgangStatus> inStatus = Arrays.asList(cmd.getErweitertVorgangStatus());
          List<EnumVorgangStatus> notInStatus;
          if(cmd.getSuchtyp() == VorgangSuchenCommand.Suchtyp.aussendienst) {
            notInStatus = new ArrayList<EnumVorgangStatus>(Arrays.asList(EnumVorgangStatus.aussendienstVorgangStatus()));
          } else {
            notInStatus = new ArrayList<EnumVorgangStatus>(Arrays.asList(EnumVorgangStatus.values()));
          }
          notInStatus.removeAll(inStatus);
          if (!inStatus.isEmpty()) {
            conds.add("vo.status IN ('" + StringUtils.join(inStatus, "', '") + "')");
          }
          if (!notInStatus.isEmpty()) {
            conds.add("vo.status NOT IN ('" + StringUtils.join(notInStatus, "', '") + "')");
          }
        }
        //Zust�ndigkeit
        if (!StringUtils.isBlank(cmd.getErweitertZustaendigkeit())) {
          if (cmd.getErweitertZustaendigkeit().equals("#mir zugewiesen#")) {
            conds.add("vo.zustaendigkeit IN ('" + StringUtils.join(
                    Role.toString(securityService.getCurrentZustaendigkeiten(true)), "', '"
            ) + "')");
          } else {
            conds.add("vo.zustaendigkeit = '" + cmd.getErweitertZustaendigkeit() + "'");
          }
        }
        String cmd_negation = cmd.getNegation();
        boolean filter_auftrag_datum = true;
        if (!StringUtils.isBlank(cmd.getAuftragTeam())) {
          if(cmd_negation != null && cmd_negation.length() > 0 && cmd_negation.contains("agency_responsible")) {
            conds.add("auftrag.vorgang is null OR auftrag.team != '" + cmd.getAuftragTeam() + "'");
            filter_auftrag_datum = false;
          } else {
            conds.add("auftrag.team = '" + cmd.getAuftragTeam() + "'");
          }
        }
        if (filter_auftrag_datum && cmd.getAuftragDatum() != null) {
          java.sql.Date auftragDatum = new java.sql.Date(DateUtils.truncate(cmd.getAuftragDatum(), Calendar.DAY_OF_MONTH).getTime());
          conds.add("auftrag.datum = '" + auftragDatum + "'");
        }
        //DelegiertAn
        if (!StringUtils.isBlank(cmd.getErweitertDelegiertAn())) {
          conds.add("vo.delegiert_an = '" + cmd.getErweitertDelegiertAn() + "'");
        }
        //Datum
        if (cmd.getErweitertDatumVon() != null) {
          java.sql.Date datumVon = new java.sql.Date(DateUtils.truncate(cmd.getErweitertDatumVon(), Calendar.DAY_OF_MONTH).getTime());
          conds.add("vo.datum >= '" + datumVon.toString() + "'");
        }
        if (cmd.getErweitertDatumBis() != null) {
          java.sql.Date datumBis = new java.sql.Date(DateUtils.truncate(cmd.getErweitertDatumBis(), Calendar.DAY_OF_MONTH).getTime());
          conds.add("vo.datum <= '" + datumBis.toString() + "'");
        }
        //Datum
        if (cmd.getAktualisiertVon() != null) {
          java.sql.Date datumVon = new java.sql.Date(DateUtils.truncate(cmd.getAktualisiertVon(), Calendar.DAY_OF_MONTH).getTime());
          conds.add("vo.version >= '" + datumVon.toString() + "'");
        }
        if (cmd.getAktualisiertBis() != null) {
          java.sql.Date datumBis = new java.sql.Date(DateUtils.truncate(cmd.getAktualisiertBis(), Calendar.DAY_OF_MONTH).getTime());
          conds.add("vo.version <= '" + datumBis.toString() + "'");
        }
        //Archiviert
        if (cmd.getErweitertArchiviert() != null) {
          if (cmd.getErweitertArchiviert()) {
            conds.add("vo.archiviert");
          } else {
            conds.add("vo.archiviert IS NULL OR NOT vo.archiviert");
          }
        }
        //Unterst�tzer
        if (cmd.getErweitertUnterstuetzerAb() != null) {
          unStatus.add(EnumVorgangStatus.inBearbeitung);
          conds.add("vo.typ != '" + EnumVorgangTyp.idee + "' "
                  + " OR NOT vo.erstsichtung_erfolgt "
                  + " OR vo.status IN ('" + StringUtils.join(unStatus, "', '") + "')"
                  + " OR un.count >= " + cmd.getErweitertUnterstuetzerAb()
          );
        }
        //Missbrauchsmeldungen
        if(cmd.getUeberspringeVorgaengeMitMissbrauchsmeldungen()) {
          conds.add("COALESCE(mi.count, 0) = 0");
        }
        //Priorit�t
        if (cmd.getErweitertPrioritaet() != null) {
          conds.add("vo.prioritaet = '" + cmd.getErweitertPrioritaet().name() + "'");
        }
        //Stadtteil
        if (cmd.getErweitertStadtteilgrenze() != null) {
          conds.add("geometry_within(vo.ovi, (SELECT grenze FROM klarschiff_stadtteil_grenze WHERE id=" + cmd.getErweitertStadtteilgrenze() + "))");
        }

        if(cmd.getSuchbereich() != null) {
          conds.add("geometry_within(ST_Transform(vo.ovi, 4326), " + cmd.getSuchbereich() + ")");
        }
        break;
    }
    // Unterst�tzer
    sql.append(" LEFT JOIN (SELECT vorgang, COUNT(DISTINCT id) FROM klarschiff_unterstuetzer")
            .append(" WHERE datum_bestaetigung IS NOT NULL GROUP BY vorgang) un")
            .append(" ON vo.id = un.vorgang");
    // Missbrauchsmeldungen
    sql.append(" LEFT JOIN (SELECT vorgang, COUNT(DISTINCT id) FROM klarschiff_missbrauchsmeldung")
            .append(" WHERE datum_bestaetigung IS NOT NULL ")
            .append("   AND datum_abarbeitung IS NULL GROUP BY vorgang) mi")
            .append(" ON vo.id = mi.vorgang");
    if (!conds.isEmpty()) {
      sql.append(" WHERE (").append(StringUtils.join(conds, ") AND (")).append(")");
    }
    return sql;
    }

    
    /**
    * F�gt zu einem StringBuilder den WHERE-Teil einer SQL-Query zur Suche von
    * Vorg�ngen anhand der Parameter im <code>VorgangDelegiertSuchenCommand</code>
    * hinzu.
    *
    * @param cmd Command mit den Parametern zur Suche
    * @param sql StringBuilder an den angeh�ngt wird
    * @return StringBuilder an den angeh�ngt wird mit WHERE
    */
    private StringBuilder addFilter(VorgangDelegiertSuchenCommand cmd, StringBuilder sql) {

    ArrayList<String> conds = new ArrayList<String>();
    conds.add("vo.archiviert IS NULL OR NOT vo.archiviert");

    if (cmd instanceof VorgangFeedDelegiertAnCommand) {
        conds.add("vo.delegiert_an IN " + Role.toString(((VorgangFeedDelegiertAnCommand)cmd).getDelegiertAn()));
    } else {
        conds.add("vo.delegiert_an IN ('" + StringUtils.join(Role.toString(securityService.getCurrentDelegiertAn()), "', '") + "')");
    }
    
    switch (cmd.getSuchtyp()) {
      case einfach:
        switch (cmd.getEinfacheSuche()) {
          case offene:
            conds.add("vo.status IN ('" + EnumVorgangStatus.inBearbeitung + "')");
            break;
          case abgeschlossene:
            conds.add("vo.status IN ('" + StringUtils.join(EnumVorgangStatus.closedVorgangStatus(), "', '") + "')");
            break;
        }
        break;
      case erweitert:
        //FullText
        if (!StringUtils.isBlank(cmd.getErweitertFulltext())) {
          String text = StringEscapeUtils.escapeSql("%" + cmd.getErweitertFulltext() + "%");
          conds.add("vo.betreff ILIKE '" + text + "'"
                  + " OR vo.details ILIKE '" + text + "'"
                  + " OR vo.status_kommentar ILIKE '" + text + "'"
                  + " OR vo.id IN (SELECT vorgang FROM klarschiff_kommentar "
                  + "   WHERE NOT geloescht AND text ILIKE '" + text + "')"
                  + " OR vo.kategorie IN (SELECT id FROM klarschiff_kategorie WHERE name ILIKE '" + text + "')"
                  + " OR vo.kategorie IN (SELECT id FROM klarschiff_kategorie WHERE parent in ("
                  + "SELECT id FROM klarschiff_kategorie WHERE name ILIKE '" + text + "'))");
        }
        //Nummer
        if (cmd.getErweitertNummerAsLong() != null) {
          conds.add("vo.id = " + cmd.getErweitertNummerAsLong());
        }
        //Kategorie
        if (cmd.getErweitertKategorie() != null) {
          conds.add("vo.kategorie = " + cmd.getErweitertKategorie().getId());
          //Hauptkategorie
        } else if (cmd.getErweitertHauptkategorie() != null) {
          conds.add("vo.kategorie IN (SELECT id FROM klarschiff_kategorie WHERE parent = " + cmd.getErweitertHauptkategorie().getId() + ")");
          //Typ
        } else if (cmd.getErweitertVorgangTyp() != null) {
          conds.add("vo.typ = '" + cmd.getErweitertVorgangTyp().name() + "'");
        }
        //Status
         {
          List<EnumVorgangStatus> inStatus = Arrays.asList(cmd.getErweitertVorgangStatus());
          List<EnumVorgangStatus> notInStatus = new ArrayList<EnumVorgangStatus>(Arrays.asList(EnumVorgangStatus.values()));
          notInStatus.removeAll(inStatus);
          if (!inStatus.isEmpty()) {
            conds.add("vo.status IN ('" + StringUtils.join(inStatus, "', '") + "')");
          }
          if (!notInStatus.isEmpty()) {
            conds.add("vo.status NOT IN ('" + StringUtils.join(notInStatus, "', '") + "')");
          }
        }
        //Datum
        if (cmd.getErweitertDatumVon() != null) {
          java.sql.Date datumVon = new java.sql.Date(DateUtils.truncate(cmd.getErweitertDatumVon(), Calendar.DAY_OF_MONTH).getTime());
          conds.add("vo.datum >= '" + datumVon.toString() + "'");
        }
        if (cmd.getErweitertDatumBis() != null) {
          java.sql.Date datumBis = new java.sql.Date(DateUtils.truncate(cmd.getErweitertDatumBis(), Calendar.DAY_OF_MONTH).getTime());
          conds.add("vo.datum <= '" + datumBis.toString() + "'");
        }
        //Priorität
        if (cmd.getErweitertPrioritaet() != null) {
          conds.add("vo.prioritaet = '" + cmd.getErweitertPrioritaet().name() + "'");
        }
        //Stadtteil
        if (cmd.getErweitertStadtteilgrenze() != null) {
          conds.add("geometry_within(vo.ovi, (SELECT grenze FROM klarschiff_stadtteil_grenze WHERE id=" + cmd.getErweitertStadtteilgrenze() + "))");
        }
        break;
    }
    if (!conds.isEmpty()) {
      sql.append(" WHERE (").append(StringUtils.join(conds, ") AND (")).append(")");
    }
    return sql;
    }


	/**
	 * F�gt die GroupBy-Terme zu einer HQL-Anfrage hinzu, wenn in der Anfrage nach dem Vorgang gruppiert werden soll. Die Parameter f�r
	 * die Projektion auf die Vorgangsattributte werden dabei zur HQL-Anfrage hinzugef�gt.
	 * @param query Hilfsobjekt f�r HQL-Anfragen
	 * @return ver�ndertes Hilfsobjekt f�r HQL-Anfragen
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
			.addGroupByAttribute("vo.fotoGross")
			.addGroupByAttribute("vo.fotoNormal")
			.addGroupByAttribute("vo.fotoThumb")
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
   * Ermittelt die Liste der Vorg�nge zur Suche anhand der Parameter im
   * <code>VorgangSuchenCommand</code>
   *
   * @param cmd Command mit den Parametern zur Suche
   * @return Ergebnisliste der Vorg�nge
   */
  public List<Object[]> getVorgaenge(VorgangSuchenCommand cmd) {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT vo.*,")
            .append(" verlauf1.datum AS aenderungsdatum,")
            .append(" COALESCE(un.count, 0) AS unterstuetzer,")
            .append(" COALESCE(mi.count, 0) AS missbrauchsmeldung");
    sql.append(" FROM klarschiff_vorgang vo");
    // F�r Sortierung
    sql.append(" LEFT JOIN klarschiff_kategorie kat_unter ON vo.kategorie = kat_unter.id");
    sql.append(" LEFT JOIN klarschiff_kategorie kat_haupt ON kat_unter.parent = kat_haupt.id");
    // F�r Auftrag
    sql.append(" LEFT JOIN klarschiff_auftrag auftrag ON vo.id = auftrag.vorgang");
    // �nderungsdatum
    sql.append(" INNER JOIN (SELECT vorgang, MAX(datum) AS datum FROM klarschiff_verlauf")
            .append(" GROUP BY vorgang) verlauf1 ON vo.id = verlauf1.vorgang");

    sql = addFilter(cmd, sql);
    // ORDER
    ArrayList orderBys = new ArrayList();
    for (String field : cmd.getOrderString().split(",")) {
      orderBys.add(field.trim() + " " + cmd.getOrderDirectionString());
    }
    if (!orderBys.isEmpty()) {
      sql.append(" ORDER BY ").append(StringUtils.join(orderBys, ", "));
    }
    // LIMIT
    if (cmd.getSize() != null) {
      sql.append(" LIMIT ").append(cmd.getSize());
    }
    if (cmd.getPage() != null && cmd.getSize() != null) {
      sql.append(" OFFSET ").append((cmd.getPage() - 1) * cmd.getSize());
    }
    return ((Session) em.getDelegate())
            .createSQLQuery(sql.toString())
            .addEntity("vo", Vorgang.class)
            .addScalar("aenderungsdatum", StandardBasicTypes.DATE)
            .addScalar("unterstuetzer", StandardBasicTypes.LONG)
            .addScalar("missbrauchsmeldung", StandardBasicTypes.LONG)
            .list();
  }
	
	/**
    * Ermittelt die Liste der Vorgänge zur Suche anhand der Parameter im
    * <code>VorgangDelegiertSuchenCommand</code>
    *
    * @param cmd Command mit den Parametern zur Suche
    * @return Ergebnisliste der Vorgänge
    */
    public List<Object[]> getVorgaenge(VorgangDelegiertSuchenCommand cmd) {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT vo.*");
    sql.append(" FROM klarschiff_vorgang vo");
    // Für Sortierung
    sql.append(" LEFT JOIN klarschiff_kategorie kat_unter ON vo.kategorie = kat_unter.id");
    sql.append(" LEFT JOIN klarschiff_kategorie kat_haupt ON kat_unter.parent = kat_haupt.id");
    // Änderungsdatum
    sql.append(" INNER JOIN (SELECT vorgang, MAX(datum) AS datum FROM klarschiff_verlauf")
            .append(" GROUP BY vorgang) verlauf1 ON vo.id = verlauf1.vorgang");

    sql = addFilter(cmd, sql);
    // ORDER
    ArrayList orderBys = new ArrayList();
    for (String field : cmd.getOrderString().split(",")) {
      orderBys.add(field.trim() + " " + cmd.getOrderDirectionString());
    }
    if (!orderBys.isEmpty()) {
      sql.append(" ORDER BY ").append(StringUtils.join(orderBys, ", "));
    }
    // LIMIT
    if (cmd.getSize() != null) {
      sql.append(" LIMIT ").append(cmd.getSize());
    }
    if (cmd.getPage() != null && cmd.getSize() != null) {
      sql.append(" OFFSET ").append((cmd.getPage() - 1) * cmd.getSize());
    }
    return ((Session) em.getDelegate())
            .createSQLQuery(sql.toString())
            .addEntity("vo", Vorgang.class)
            .list();
    }

    /**
	 * Ermittelt die Ergebnisanzahl für eine definierte parametrisierte Anfrage nach Vorgängen. 
	 * @param cmd Command mit den Parametern zur Suche
	 * @return Anzahl der Vorgänge im Suchergebnis
	 */
    public long countVorgaenge(VorgangDelegiertSuchenCommand cmd) {
      StringBuilder sql = new StringBuilder();
      sql.append("SELECT count(vo.*) AS count FROM klarschiff_vorgang vo");
      sql = addFilter(cmd, sql);
      SQLQuery query = ((Session) em.getDelegate()).createSQLQuery(sql.toString());
      for (BigInteger i: (List<BigInteger>) query.list()) {
        return i.longValue();
      }
      return 0;
    }

    /**
	 * Ermittelt die Ergebnisanzahl f�r eine definierte parametrisierte Anfrage nach Vorg�ngen. 
	 * @param cmd Command mit den Parametern zur Suche
	 * @return Anzahl der Vorg�nge im Suchergebnis
	 */
    public long countVorgaenge(VorgangSuchenCommand cmd) {
      StringBuilder sql = new StringBuilder();
      sql.append("SELECT count(vo.*) AS count FROM klarschiff_vorgang vo");
      sql = addFilter(cmd, sql);
      SQLQuery query = ((Session) em.getDelegate()).createSQLQuery(sql.toString());
      for (BigInteger i: (List<BigInteger>) query.list()) {
        return i.longValue();
      }
      return 0;
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
	 * Ermittelt alle Vorgänge, die abgeschlossen sind und seit einem bestimmten Zeitraum nicht mehr bearbeitet wurden.
	 * @param versionBefor Zeitpunkt, bis zu dem die letzte Bearbeitung hätte stattfinden müssen
	 * @return Ergebnisliste mit Vorgängen
	 * @see de.fraunhofer.igd.klarschiff.service.job.JobsService#archivVorgaenge()
	 */
	public List<Vorgang> findNotArchivVorgang(Date versionBefor) {
	    return em.createQuery("SELECT o FROM Vorgang o WHERE o.status IN (:status) AND version <= :versionBefor AND (archiviert IS NULL OR archiviert = FALSE)", Vorgang.class)
		    .setParameter("status", Arrays.asList(EnumVorgangStatus.closedVorgangStatus()))
		    .setParameter("versionBefor", versionBefor)
		    .getResultList();	    
	}

	
	/**
	 * Ermittelt alle Vorgänge, die gemeldet, aber nach einem bestimmten Zeitraum noch nicht bestätigt wurden.
	 * @param datumBefor Zeitpunkt, bis zu dem die Vorgänge hätten bestätigt werden müssen
	 * @return Ergebnisliste mit Vorgängen
	 * @see de.fraunhofer.igd.klarschiff.service.job.JobsService#removeUnbestaetigtVorgang()
	 */
	public List<Vorgang> findUnbestaetigtVorgang(Date datumBefor) {
		return em.createQuery("SELECT o FROM Vorgang o WHERE o.status = :status AND datum <= :datumBefor", Vorgang.class)
			.setParameter("status", EnumVorgangStatus.gemeldet)
			.setParameter("datumBefor", datumBefor)
			.getResultList();	    
	}

	
	/**
	 * Ermittelt alle Unterstützungen, die eingegangen sind, aber nach einem bestimmten Zeitraum noch nicht bestätigt wurden.
	 * @param datumBefor Zeitpunkt, bis zu dem die Unterstützungen hätten bestätigt werden müssen
	 * @return Ergebnisliste mit Unterstützungen
	 * @see de.fraunhofer.igd.klarschiff.service.job.JobsService#removeUnbestaetigtUnterstuetzer()
	 */
	public List<Unterstuetzer> findUnbestaetigtUnterstuetzer(Date datumBefor) {
	    return em.createQuery("SELECT o FROM Unterstuetzer o WHERE o.datumBestaetigung IS NULL AND datum <= :datumBefor", Unterstuetzer.class)
		    .setParameter("datumBefor", datumBefor)
		    .getResultList();	    
	}
	
	
	/**
	 * Ermittelt alle Missbrauchsmeldungen, die eingegangen sind, aber nach einem bestimmten Zeitraum noch nicht bestätigt wurden.
	 * @param datumBefor Zeitpunkt, bis zu dem die Missbrauchsmeldungen hätten bestätigt werden müssen
	 * @return Ergebnisliste mit Missbrauchsmeldungen
	 * @see de.fraunhofer.igd.klarschiff.service.job.JobsService#removeUnbestaetigtMissbrauchsmeldung()
	 */
	public List<Missbrauchsmeldung> findUnbestaetigtMissbrauchsmeldung(Date datumBefor) {
	    return em.createQuery("SELECT o FROM Missbrauchsmeldung o WHERE o.datumBestaetigung IS NULL AND datum <= :datumBefor", Missbrauchsmeldung.class)
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
	 * Ermittlet die aktuellsten Vorgänge, die eine akzeptierte Zuständigkeit besitzen, um mit diesen den Zuständigkeitsfinder zu trainieren
	 * @param maxResults maximale Anzahl von Vorgängen in der Ergebnisliste 
	 * @return Vorgänge mit akzeptierten Zuständigkeiten
	 */
	@SuppressWarnings("unchecked")
	public List<Vorgang> findVorgangForTrainClassificator(int maxResults) {
		return em.createQuery("SELECT a FROM Vorgang a, Vorgang b WHERE a.kategorie = b.kategorie AND a.version <= b.version AND a.zustaendigkeitStatus = 'akzeptiert' AND b.zustaendigkeitStatus = 'akzeptiert' GROUP BY a.id HAVING count(*) <= 10", Vorgang.class).setMaxResults(maxResults).getResultList();
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
			.addWhereConditions("ve.typ = :verlaufTyp").addParameter("verlaufTyp", EnumVerlaufTyp.status)
			.addWhereConditions("ve.datum >= :datum").addParameter("datum", lastChange)
            .addWhereConditions("vo.status = :status").addParameter("status", EnumVorgangStatus.inBearbeitung)
            .addWhereConditions("ve.wertNeu = 'in Bearbeitung'")
			.addWhereConditions("vo.autorEmail IS NOT NULL")
			.addWhereConditions("vo.autorEmail != :autorEmail").addParameter("autorEmail", "");
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
			.addWhereConditions("ve.typ = :verlaufTyp").addParameter("verlaufTyp", EnumVerlaufTyp.status)
			.addWhereConditions("ve.datum >= :datum").addParameter("datum", lastChange)
			.addWhereConditions("vo.status IN (:status)").addParameter("status", Arrays.asList(EnumVorgangStatus.closedVorgangStatus()))
			.addWhereConditions("ve.wertNeu IN ('abgeschlossen', 'wird nicht bearbeitet')")
			.addWhereConditions("vo.autorEmail IS NOT NULL")
			.addWhereConditions("vo.autorEmail != :autorEmail").addParameter("autorEmail", "");
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
            .addWhereConditions("((SELECT COUNT(*) FROM Unterstuetzer un WHERE un.vorgang = vo.id) < :unterstuetzer OR vo.id NOT IN (SELECT DISTINCT un.vorgang FROM Unterstuetzer un))").addParameter("unterstuetzer", settingsService.getVorgangIdeeUnterstuetzer());
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
			.addWhereConditions("((vo.betreff IS NOT NULL AND vo.betreff != '' AND (betreffFreigabeStatus IS NULL OR betreffFreigabeStatus = 'intern')) OR (vo.details IS NOT NULL AND vo.details != '' AND (detailsFreigabeStatus IS NULL OR detailsFreigabeStatus = 'intern')) OR (vo.fotoThumb IS NOT NULL AND (fotoFreigabeStatus IS NULL OR fotoFreigabeStatus = 'intern')))");
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
