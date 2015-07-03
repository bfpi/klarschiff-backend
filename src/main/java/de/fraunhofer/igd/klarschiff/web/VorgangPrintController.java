package de.fraunhofer.igd.klarschiff.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.fraunhofer.igd.klarschiff.dao.KommentarDao;
import de.fraunhofer.igd.klarschiff.dao.LobHinweiseKritikDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;


/**
 * Controller für druckoptimierte Vorgangsanzeigen
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Controller
public class VorgangPrintController {

	@Autowired
	VorgangDao vorgangDao;

	@Autowired
	KommentarDao kommentarDao;

	@Autowired
	LobHinweiseKritikDao lobHinweiseKritikDao;
	
	@Autowired
	GeoService geoService;
	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/{id}/print</code><br/>
	 * Seitenbeschreibung: Druckoptimierte Vorgangsübersicht
	 * @param id Vorgangs-ID
	 * @param model Model in der ggf. Daten für die View abgelegt werden
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/vorgang/{id}/print", method = RequestMethod.GET)
	public String print(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
		return print(id, model, request, false);
	}
	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/delegiert/{id}/print</code><br/>
	 * Seitenbeschreibung: Druckoptimierte Übersicht für delegierte Vorgänge
	 * @param id Vorgangs-ID
	 * @param model Model in der ggf. Daten für die View abgelegt werden
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/vorgang/delegiert/{id}/print", method = RequestMethod.GET)
	public String printDelegiert(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
		return print(id, model, request, true);
	}
	
	/**
	 * Reichert Model mit geoService-Verweis, Vorgangsdaten, -kommentaren und -missbrauchsmeldungen (nicht bei Delegierung)
	 * an und liefert View für druckoptimierte Anzeige.
	 * @param id Vorgangs-ID
	 * @param model Model in der ggf. Daten für die View abgelegt werden
	 * @param request Request
	 * @param delegiert true für Anzeige für Externe (verhindert z.B. Anfügen von Missbrauchsmeldungen)
	 * @return
	 */
	public String print(Long id, ModelMap model, HttpServletRequest request, boolean delegiert) {
		Vorgang vorgang = vorgangDao.findVorgang(id);
		model.put("geoService", geoService);
		model.put("lobhinweisekritik", lobHinweiseKritikDao.findLobHinweiseKritikForVorgang(vorgang, null, null));
		model.put("kommentare", kommentarDao.findKommentareForVorgang(vorgang, null, null));
		if (delegiert) model.put("delegiert", true);
		else model.put("missbrauchsmeldungen", vorgangDao.listMissbrauchsmeldung(vorgang));
		model.put("vorgang", vorgang);
		return "noMenu/vorgang/printEmail/print";
	}
}
