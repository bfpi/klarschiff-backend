package de.fraunhofer.igd.klarschiff.web;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.fraunhofer.igd.klarschiff.dao.GrenzenDao;
import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.poi.PoiService;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.web.VorgangSuchenCommand.EinfacheSuche;
import de.fraunhofer.igd.klarschiff.web.VorgangSuchenCommand.Suchtyp;

/**
 * Controller f�r die Vorgangsuche
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SessionAttributes({"cmdvorgangsuchen"})
@RequestMapping("/vorgang/suchen")
@Controller
public class VorgangSuchenController {

	Logger logger = Logger.getLogger(VorgangSuchenController.class);
	
	@Autowired
	VorgangDao vorgangDao;

	@Autowired
	GrenzenDao grenzenDao;
	
	@Autowired
	PoiService poiService;
	
	@Autowired
	GeoService geoService;
	
	@Autowired
	SecurityService securityService;
	
	@Autowired
	KategorieDao kategorieDao;
	
	@Autowired
	SettingsService settingsService;
	
	
	/**
	 * Liefert alle im System vorhandenen Zust�ndigkeiten
	 */
	@ModelAttribute("allZustaendigkeiten")
    public List<Role> allZustaendigkeiten() {
        return securityService.getAllZustaendigkeiten(true);
    }

	/**
	 * Liefert alle im System vorhandenen Rollen zum Delegieren
	 */
	@ModelAttribute("allDelegiertAn")
	public List<Role> allDelegiertAn() {
		return securityService.getAllDelegiertAn();
	}

	/**
	 * Liefert alle m�glichen Auspr�gungen f�r Vorgangs-Status-Typen 
	 */
	@ModelAttribute("allVorgangStatus")
	public EnumVorgangStatus[] allVorgangStatus() {
		return EnumVorgangStatus.values();
	}
	
	/**
	 * Liefert alle m�glichen Auspr�gungen f�r Vorgangstypen 
	 */
	@ModelAttribute("vorgangtypen")
    public Collection<EnumVorgangTyp> populateEnumVorgangTypen() {
        return Arrays.asList(EnumVorgangTyp.values());
    }

	/**
	 * Liefert alle m�glichen Auspr�gungen f�r Priorit�tsbezeichner 
	 */
	@ModelAttribute("allPrioritaeten")
    public Collection<EnumPrioritaet> allPrioritaeten() {
        return Arrays.asList(EnumPrioritaet.values());
    }

	/**
	 * Liefert (in Systemkonfiguration festgelegte) Anzahl an Unterst�tzungen, die ben�tigt werden damit Idee Relevanz erlangt (z.B. in der Vorgangssuche
	 * automatisch erscheint). 
	 */
	@ModelAttribute("vorgangIdeenUnterstuetzer")
    public Long vorgangIdeenUnterstuetzer() {
        return settingsService.getVorgangIdeeUnterstuetzer();
    }
	
	/**
	 * Liefert alle Stadtteile mit ihren Grenzen
	 * @return Liste mit Arrays [0] id (long), [1] name (String)
	 */
	@ModelAttribute("allStadtteile")
    public List<Object[]> allStadtteile() {
        return grenzenDao.findStadtteilGrenzen();
    }
	
    /** Initialisiert <code>VorgangSuchenCommand</code>-Objekt  mit Standardwerten zur Benutzung als ModelAttribute 
     * f�r Suchoperation
     */
	@ModelAttribute("cmdvorgangsuchen")
    public VorgangSuchenCommand initCommand() {
		VorgangSuchenCommand cmd = new VorgangSuchenCommand();
    	cmd.setSize(20);
    	cmd.setOrder(2);
    	cmd.setOrderDirection(1);
    	//Suchtyp
    	cmd.setSuchtyp(VorgangSuchenCommand.Suchtyp.einfach);
    	//Initiale einfache Suche
    	cmd.setEinfacheSuche(VorgangSuchenCommand.EinfacheSuche.offene);
    	//Initiale erweiterte Suche
    	cmd.setErweitertArchiviert(false);
    	cmd.setErweitertZustaendigkeit("#mir zugewiesen#");
    	cmd.setErweitertVorgangStatus((EnumVorgangStatus[])ArrayUtils.removeElement(ArrayUtils.removeElement(EnumVorgangStatus.values(), EnumVorgangStatus.gemeldet), EnumVorgangStatus.geloescht));
        return cmd;
    }
	
	/**
	 * Aktualisiert Unterkategorie und Liste m�glicher Hauptkategorien (abh�ngig von Vorgangstyp) in �bergebenem
	 * Model mit Daten aus �bergebenem Commandobjekt 
	 * @param model Model
	 * @param cmd Command
	 */
	private void updateKategorieInModel(ModelMap model, VorgangSuchenCommand cmd) {
		try {
			model.addAttribute("hauptkategorien", kategorieDao.findRootKategorienForTyp(cmd.getErweitertVorgangTyp()));
			model.addAttribute("unterkategorien", kategorieDao.findKategorie(cmd.getErweitertHauptkategorie().getId()).getChildren());
		}catch (Exception e) {}
	}
	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/suchen</code><br/>
	 * Seitenbeschreibung: Darstellung der Backend-Suchfunktionalit�t
	 * @param cmd Command
	 * @param neu optionaler Parameter, triggert Initialisierung des Commandobjektes bei neuer Suchanfrage
	 * @param modelMap Model in der ggf. Daten f�r die View abgelegt werden
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(method = RequestMethod.GET)
    public String suchen(@ModelAttribute(value = "cmdvorgangsuchen") VorgangSuchenCommand cmd, @RequestParam(value = "neu", required = false) boolean neu, ModelMap modelMap) {
		if (neu) {
			cmd = initCommand();
			modelMap.put("cmdvorgangsuchen", cmd);
		}
		updateKategorieInModel(modelMap, cmd);
    	//Suchen
		modelMap.addAttribute("vorgaenge", vorgangDao.listVorgang(cmd));
		if (cmd.suchtyp==Suchtyp.einfach && cmd.einfacheSuche==EinfacheSuche.offene)
			modelMap.put("missbrauchsmeldungenAbgeschlossenenVorgaenge", vorgangDao.missbrauchsmeldungenAbgeschlossenenVorgaenge());
		modelMap.put("maxPages", calculateMaxPages(cmd.getSize(), vorgangDao.countVorgang(cmd)));

		return "vorgang/suchen";
	}
	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/suchen/karte</code><br/>
	 * Seitenbeschreibung: Kartenandarstellung f�r die Ergebnisse der aktuellen Suchanfrage 
	 * @param cmd Command
	 * @param modelMap Model in der ggf. Daten f�r die View abgelegt werden
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/karte", method = RequestMethod.GET)
	public String karte(@ModelAttribute(value = "cmdvorgangsuchen") VorgangSuchenCommand cmd, ModelMap modelMap) 
	{
		try {
			VorgangSuchenCommand cmd2 = (VorgangSuchenCommand)BeanUtils.cloneBean(cmd);
			cmd2.setPage(null);
			cmd2.setSize(null);
			
			modelMap.addAttribute("geoService", geoService);
			modelMap.addAttribute("vorgaenge", vorgangDao.listVorgang(cmd2));
			return "vorgang/suchenKarte";
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/suchen/vorgaenge.xls</code><br/>
	 * Funktionsbeschreibung: Erzeugt Excel-Datei mit dem Inhalt der aktuellen Suchergebnisse und liefert
	 * diese als Download mit <code>Content-Type:"application/ms-excel"</code> aus
	 * @param cmd Command
	 */
	@RequestMapping(value="/vorgaenge.xls", method = RequestMethod.GET)
    @ResponseBody
	public void excel(
    		@ModelAttribute(value = "cmdvorgangsuchen") VorgangSuchenCommand cmd,
			HttpServletRequest request,
			HttpServletResponse response) {
		try {
			VorgangSuchenCommand cmd2 = (VorgangSuchenCommand)BeanUtils.cloneBean(cmd);
			cmd2.setPage(null);
			cmd2.setSize(null);
			
			List<Object[]> vorgaenge = vorgangDao.listVorgang(cmd2);
			
			HSSFWorkbook workbook = poiService.createSheet(PoiService.Template.vorgangListe, vorgaenge);
			
			response.setHeader("Content-Type", "application/ms-excel");
			workbook.write(response.getOutputStream());
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Ermittelt die Anzahl maximal ben�tigter Seiten aus:
	 * @param size gew�nschter Anzahl an Elementen (Suchergebnissen) pro Seite
	 * @param count gegebener Anzahl an darzustellender Elemente
	 * @return maximal ben�tigte Seitenzahl
	 */
	private int calculateMaxPages(int size, long count)
    {
		float nrOfPages = (float) count / size;
		return (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
    }
	
}
