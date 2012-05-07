package de.fraunhofer.igd.klarschiff.web;

import static de.fraunhofer.igd.klarschiff.web.Assert.assertNotEmpty;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.fraunhofer.igd.klarschiff.dao.VerlaufDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.Missbrauchsmeldung;
import de.fraunhofer.igd.klarschiff.vo.Verlauf;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Controller zum Erstellen, Betrachten und Bestätigen der Bearbeitung von Missbrauchsmeldungen
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SessionAttributes("cmd")
@Controller
public class VorgangMissbrauchController {

	@Autowired
	VorgangDao vorgangDao;

	@Autowired
	VerlaufDao verlaufDao;
	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/{id}/missbrauch</code><br/>
	 * Seitenbeschreibung: Anzeige existierender und Erstellung neuer Missbrauchsmeldungen
	 * @param id Vorgangs-ID
	 * @param model Model in der ggf. Daten für die View abgelegt werden
	 * @param request HttpServletRequest-Objekt
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/vorgang/{id}/missbrauch", method = RequestMethod.GET)
	public String missbrauch(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
		Vorgang vorgang = vorgangDao.findVorgang(id);
		model.put("vorgang", vorgang);
		model.put("missbrauchsmeldungen", vorgangDao.listMissbrauchsmeldung(vorgang));
		VorgangMissbrauchCommand cmd = new VorgangMissbrauchCommand();
		cmd.setMissbrauchsmeldung(new Missbrauchsmeldung());
		model.put("cmd", cmd);
		return "vorgang/missbrauch";
	}
	
	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/vorgang/{id}/missbrauch</code><br/>
	 * Funktionsbeschreibung: Nimmt Missbrauchsmeldung oder Bestätigung der Bearbeitung vorhergehender
	 * Missbrauchsmeldungen entgegen
	 * @param id Vorgangs-ID
	 * @param model Model in der ggf. Daten für die View abgelegt werden
	 * @param request HttpServletRequest-Objekt
	 * @return Missbrauchs-View mit neuer Meldung oder als nun bearbeitet markiertem Missbrauch
	 */
	@RequestMapping(value="/vorgang/{id}/missbrauch", method = RequestMethod.POST)
    public String missbrauchSubbmit(
    		@ModelAttribute(value = "cmd") VorgangMissbrauchCommand cmd, 
    		BindingResult result, 
    		@PathVariable("id") Long id, 
    		@RequestParam(value = "action", required = true) String action, 
    		ModelMap model, 
    		HttpServletRequest request) {
		
		action = StringEscapeUtils.escapeHtml(action);

		
		Vorgang vorgang = vorgangDao.findVorgang(id);

		if (action.equals("abgearbeitet")) {
			Missbrauchsmeldung missbrauchsmeldung = vorgangDao.findMissbrauchsmeldung(cmd.getMissbrauchsmeldungId());
			missbrauchsmeldung.setDatumAbarbeitung(new Date());
			vorgangDao.merge(missbrauchsmeldung);
			for (@SuppressWarnings("unused") Verlauf verlauf : vorgang.getVerlauf());
			verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.missbrauchsmeldungBearbeitet, null, null);
			vorgangDao.merge(vorgang, false);
			
		} else if (action.equals("Missbrauch melden")) {
			assertNotEmpty(cmd, result, Assert.EvaluateOn.ever, "missbrauchsmeldung.text", null);
			
			if (result.hasErrors()) {
				model.put("vorgang", vorgang);
				model.put("missbrauchsmeldungen", vorgangDao.listMissbrauchsmeldung(vorgang));
				return "vorgang/missbrauch";
			}			
			cmd.getMissbrauchsmeldung().setVorgang(vorgang);
			cmd.getMissbrauchsmeldung().setDatum(new Date());
			cmd.getMissbrauchsmeldung().setDatumBestaetigung(new Date());
			vorgangDao.persist(cmd.getMissbrauchsmeldung());
			for (@SuppressWarnings("unused") Verlauf verlauf : vorgang.getVerlauf());
			verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.missbrauchsmeldungErzeugt, null, null);
			vorgangDao.merge(vorgang, false);
			cmd.setMissbrauchsmeldung(new Missbrauchsmeldung());
		}
//			String str[] = action.split("_");
//			EnumFreigabeStatus freigabeStatus = EnumFreigabeStatus.valueOf(str[2]);
//			vorgang.setFotoFreigabeStatus(freigabeStatus);
//			vorgangDao.merge(vorgang);
//		}
		model.put("vorgang", vorgang);
		model.put("missbrauchsmeldungen", vorgangDao.listMissbrauchsmeldung(vorgang));
		return "vorgang/missbrauch";
	}		

}
