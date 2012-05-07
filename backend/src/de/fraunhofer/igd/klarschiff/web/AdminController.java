package de.fraunhofer.igd.klarschiff.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.fraunhofer.igd.klarschiff.dao.ClusterDao;
import de.fraunhofer.igd.klarschiff.dao.JobDao;
import de.fraunhofer.igd.klarschiff.service.cluster.ClusterUtil;
import de.fraunhofer.igd.klarschiff.service.dbsync.DbSyncService;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.job.JobsService;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.util.SqlScriptUtil;

/**
 * Controller für den Adminbereich
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@RequestMapping("/admin")
@Controller
public class AdminController {
	
	@Autowired
	SecurityService securityService;
	
	@Autowired
	JobsService jobsService;
	
	@Autowired
	DbSyncService dbSyncService;
	
	@Autowired
	JobDao jobDao;
	
	@Autowired
	ClusterDao clusterDao;
	
	@Autowired
	GeoService geoService;

	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/admin/uebersicht</code><br/>
	 * Seitenbeschreibung: Übersichtsseite zum Adminbereich
	 * @param model Model in dem ggf. Daten für die View abgelegt werden
	 * @param request Request
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/uebersicht", method = RequestMethod.GET)
    public String uebersicht(Model model, HttpServletRequest request) {
		return "admin/uebersicht";
	}

	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/admin/benutzer</code><br/>
	 * Seitenbeschreibung: Übersicht über die Benutzer im Adminbereich
	 * @param model Model in dem ggf. Daten für die View abgelegt werden
	 * @param request Request
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/benutzer", method = RequestMethod.GET)
	public String benutzer(Model model, HttpServletRequest request) {
		model.addAttribute("benutzer", securityService.getAllUser());
		return "admin/benutzer";
	}
	
	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/admin/rollen</code><br/>
	 * Seitenbeschreibung: Übersicht über die Rollen im Adminbereich
	 * @param model Model in dem ggf. Daten für die View abgelegt werden
	 * @param request Request
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/rollen", method = RequestMethod.GET)
	public String rollen(Model model, HttpServletRequest request) {
		model.addAttribute("rollenIntern", securityService.getAllZustaendigkeiten(true));
		model.addAttribute("rollenExtern", securityService.getAllDelegiertAn());
		return "admin/rollen";
	}

	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/admin/status</code><br/>
	 * Seitenbeschreibung: Übersicht zum Status des Servers im Adminbereich
	 * @param model Model in dem ggf. Daten für die View abgelegt werden
	 * @param request Request
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/status", method = RequestMethod.GET)
	public String status(Model model, HttpServletRequest request) {
		model.addAttribute("wfs", geoService.getDataStore()!=null);
		model.addAttribute("anzahlAbgeschlosseneJobs", jobDao.getAnzahlAbgeschlosseneJobs());
		model.addAttribute("fehlerhafteJobs", jobDao.getFehlerhafteJobs());
		model.addAttribute("serverConnectorPort", ClusterUtil.getServerConnectorPort());
		model.addAttribute("serverIp", ClusterUtil.getServerIp());
		model.addAttribute("serverName", ClusterUtil.getServerName());
		model.addAttribute("serverJvmRoute", ClusterUtil.getServerJvmRoute());
		model.addAttribute("aliveServerList", clusterDao.getAliveServerList());
		return "admin/status";
	}
	
	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/admin/test</code><br/>
	 * Seitenbeschreibung: Testfunktionen für die Jobs und die Schnittstelle für das Frontend im Adminbereich
	 * @param model Model in dem ggf. Daten für die View abgelegt werden
	 * @param request Request
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/test", method = RequestMethod.GET)
	public String test(Model model, HttpServletRequest request) {
		return "admin/test";
	}

	
	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/admin/test</code><br/>
	 * Seitenbeschreibung: Ausführen von Jobs im Adminbereich
	 * @param model Model in dem ggf. Daten für die View abgelegt werden
	 * @param action Id um den auszuführenden Job zu identifizieren
	 * @param request Request
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/test", method = RequestMethod.POST)
	public String testPost(Model model, @RequestParam(value = "action", required = true) String action, HttpServletRequest request) {
		if(action.equalsIgnoreCase("archivVorgaenge")) {
			jobsService.archivVorgaenge();
		} else if(action.equalsIgnoreCase("removeUnbestaetigtVorgang")) {
			jobsService.removeUnbestaetigtVorgang();
		} else if(action.equalsIgnoreCase("removeUnbestaetigtUnterstuetzer")) {
			jobsService.removeUnbestaetigtUnterstuetzer();
		} else if(action.equalsIgnoreCase("removeUnbestaetigtMissbrauchsmeldung")) {
			jobsService.removeUnbestaetigtMissbrauchsmeldung();
		} else if(action.equalsIgnoreCase("reBuildClassifier")) {
			jobsService.reBuildClassifier();
		} else if(action.equalsIgnoreCase("informExtern")) {
			jobsService.informExtern();
		} else if(action.equalsIgnoreCase("informDispatcher")) {
			jobsService.informDispatcher();
		} else if(action.equalsIgnoreCase("informErsteller")) {
			jobsService.informErsteller();
		}
		return "admin/test";
	}

	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/admin/datenbank</code><br/>
	 * Seitenbeschreibung: Darstellung der Datenbankfunktionen im Adminbereich
	 * @param model Model in dem ggf. Daten für die View abgelegt werden
	 * @param request Request
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/datenbank", method = RequestMethod.GET)
	public String datenbank(Model model, HttpServletRequest request) {
		return "admin/datenbank";
	}

	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/admin/datenbank</code><br/>
	 * Seitenbeschreibung: Ausführung einer Datenbankfunktion im Adminbereich
	 * @param model Model in dem ggf. Daten für die View abgelegt werden
	 * @param action Id zum identifizieren der auszuführenden Datenbankfunktion
	 * @param request Request
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/datenbank", method = RequestMethod.POST)
	public String datenbankPost(Model model, @RequestParam(value = "action", required = true) String action, HttpServletRequest request) {
		if(action.equalsIgnoreCase("executeSqlScriptFrontendDb")) {
			dbSyncService.executeSqlScriptFrontendDb(SqlScriptUtil.State.error);
		} else if(action.equalsIgnoreCase("viewSqlScriptFrontendDb")) {
			model.addAttribute("sqlScriptFrontendDb", dbSyncService.getSqlScriptFrontendDb());
		} else if(action.equalsIgnoreCase("executeSqlScriptDbLink")) {
			dbSyncService.executeSqlScriptDbLink(SqlScriptUtil.State.error);
		} else if(action.equalsIgnoreCase("viewSqlScriptDbLink")) {
			model.addAttribute("sqlScriptDbLink", dbSyncService.getSqlScriptDbLink());
		}
		return "admin/datenbank";
	}

	@RequestMapping(value="/zertifikate", method = RequestMethod.GET)
	public String zertifikate(Model model, HttpServletRequest request) {
		
		return "admin/zertifikate";
	}

	@RequestMapping(value="/zertifikate", method = RequestMethod.POST)
	public String zertifikatePost(Model model, @RequestParam(value = "storepass", required = false) String storepass, HttpServletRequest request) {
		model.addAttribute("result", securityService.installCertificates(storepass));
		return "admin/zertifikate";
	}

}
