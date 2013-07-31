package de.fraunhofer.igd.klarschiff.web;

import static de.fraunhofer.igd.klarschiff.web.Assert.assertMaxLength;

import java.util.List;

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.fraunhofer.igd.klarschiff.dao.KommentarDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.Kommentar;
import de.fraunhofer.igd.klarschiff.vo.StatusKommentarVorlage;
import de.fraunhofer.igd.klarschiff.vo.Verlauf;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Controller für die Vorgangsbearbeitung durch Externe (Delegierte)
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SessionAttributes("cmd")
@Controller
public class VorgangDelegiertBearbeitenController {

	Logger logger = Logger.getLogger(VorgangDelegiertBearbeitenController.class);
	
	@Autowired
	VorgangDao vorgangDao;

	@Autowired
	KommentarDao kommentarDao;
	

	@Autowired
	SecurityService securityService;
		
	
	@ModelAttribute("delegiert")
    public boolean delegiert() {
        return true;
    }

	/**
	 * Liefert alle möglichen Ausprägungen für Vorgangs-Status-Typen 
	 */
	@ModelAttribute("allVorgangStatus")
    public EnumVorgangStatus[] allVorgangStatus() {
		EnumVorgangStatus[] allVorgangStatus = EnumVorgangStatus.values();
		allVorgangStatus = (EnumVorgangStatus[]) ArrayUtils.removeElement(ArrayUtils.removeElement(ArrayUtils.removeElement(ArrayUtils.removeElement(ArrayUtils.removeElement(allVorgangStatus, EnumVorgangStatus.gemeldet), EnumVorgangStatus.offen), EnumVorgangStatus.geloescht), EnumVorgangStatus.wirdNichtBearbeitet), EnumVorgangStatus.duplikat);
        return allVorgangStatus;
    }

	/**
	 * Liefert alle Statuskommentarvorlagen
	 */
	@ModelAttribute("allStatusKommentarVorlage")
    public List<StatusKommentarVorlage> allStatusKommentarVorlage() {
        return vorgangDao.findStatusKommentarVorlage();
    }
	
	/**
	 * Aktualisiert Kategorien in übergebenem Model mit Daten aus übergebenem Commandobjekt 
	 * @param model Model
	 * @param cmd Command
	 */
	private void updateKommentarInModel(ModelMap model, VorgangDelegiertBearbeitenCommand cmd) {
		try {
			model.addAttribute("kommentare", kommentarDao.findKommentareForVorgang(cmd.getVorgang(), cmd.getPage(), cmd.getSize()));
	    	model.put("maxPages", calculateMaxPages(cmd.getSize(), kommentarDao.countKommentare(cmd.getVorgang())));

		}catch (Exception e) {}
	}

	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/{id}/bearbeiten</code><br/>
	 * Seitenbeschreibung: Formular zur Vorgangsbearbeitung oder Hinweis auf noch nicht aktivierte
	 * Bearbeitbarkeit falls Vorgang noch im Status <code>gemeldet</code>
	 * @param id Vorgangs-ID
	 * @param model Model in dem ggf. Daten für die View abgelegt werden
	 * @param request Request
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/vorgang/delegiert/{id}/bearbeiten", method = RequestMethod.GET)
	public String bearbeiten(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
		VorgangDelegiertBearbeitenCommand cmd = new VorgangDelegiertBearbeitenCommand();
		cmd.setSize(5);
		cmd.setVorgang(getVorgang(id));
		model.put("cmd", cmd);
		updateKommentarInModel(model, cmd);
		
		return "vorgang/delegiert/bearbeiten";
	}

	/**
	 * Ermittelt Vorgang mit übergebener ID aus Backend-Datenbank
	 * @param id Vorgangs-ID
	 * @return
	 */
	@Transient
	private Vorgang getVorgang(Long id) {
		Vorgang vorgang = vorgangDao.findVorgang(id);
		for (@SuppressWarnings("unused") Verlauf verlauf : vorgang.getVerlauf());
		return vorgang;
	}

	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/vorgang/delegiert/{id}/bearbeiten</code><br/>
	 * Funktionsbeschreibung: 
	 * <br/>Die Wahl des <code>action</code> Parameters erlaubt folgende Funktionalitäten:
	 * <ul>
	 * <li><code>Status setzen</code></li>
	 * <li><code>zur&uuml;ckweisen</code></li>
	 * <li><code>Kommentar speichern</code></li>
	 * </ul>
	 * 
	 * @param cmd Command
	 * @param result BindingResult
	 * @param id Vorgangs-ID
	 * @param action Stringparameter zur funktionalen Steuerung
	 * @param model Model in der ggf. Daten für die View abgelegt werden
	 * @param request HttpServletRequest-Objekt
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@Transactional
	@RequestMapping(value="/vorgang/delegiert/{id}/bearbeiten", method = RequestMethod.POST)
	public String bearbeitenSubbmit(
    		@ModelAttribute(value = "cmd") VorgangDelegiertBearbeitenCommand cmd, 
    		BindingResult result, 
    		@PathVariable("id") Long id, 
    		@RequestParam(value = "action", required = true) String action, 
    		ModelMap model, 
    		HttpServletRequest request) {
		
		action = StringEscapeUtils.escapeHtml(action);

		assertMaxLength(cmd, result,  Assert.EvaluateOn.ever, "vorgang.statusKommentar", 500, "Der Statuskommentar ist zu lang");
		if (result.hasErrors()) {
			cmd.setVorgang(getVorgang(id));
			updateKommentarInModel(model, cmd);
			return "vorgang/delegiert/bearbeiten";
		}			
		
		if (action.equals("Status setzen")) {
			vorgangDao.merge(cmd.getVorgang());
		}  else if (action.equals("zur&uuml;ckweisen")) {
			cmd.getVorgang().setDelegiertAn(null);
			vorgangDao.merge(cmd.getVorgang());
		}  else if (action.equals("Kommentar speichern")) {
			if (!StringUtils.isBlank(cmd.getKommentar())) {		
				Kommentar kommentar = new Kommentar();
				kommentar.setVorgang(cmd.getVorgang());
				kommentar.setText(cmd.getKommentar());
				kommentar.setNutzer(securityService.getCurrentUser().getName());
				kommentarDao.persist(kommentar);
				cmd.setKommentar(null);
			}
		} 

		cmd.setVorgang(getVorgang(id));
		updateKommentarInModel(model, cmd);
		return "vorgang/delegiert/bearbeiten";
	}
	
	/**
	 * Ermittelt die Anzahl maximal benötigter Seiten aus:
	 * @param size gewünschter Anzahl an Elementen (Suchergebnissen) pro Seite
	 * @param count gegebener Anzahl an darzustellender Elemente
	 * @return maximal benötigte Seitenzahl
	 */
	private int calculateMaxPages(int size, long count)
    {
		float nrOfPages = (float) count / size;
		return (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
    }
	
}
