package de.fraunhofer.igd.klarschiff.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
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

import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.poi.PoiService;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Controller f�r die Vorgangsuche f�r Externe (Delegierte)
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SessionAttributes({"cmdvorgangdelegiertsuchen"})
@RequestMapping("/vorgang/delegiert/suchen")
@Controller
public class VorgangDelegiertSuchenController {

	Logger logger = Logger.getLogger(VorgangDelegiertSuchenController.class);
	
	@Autowired
	VorgangDao vorgangDao;

	@Autowired
	PoiService poiService;
	
	@Autowired
	GeoService geoService;
	
	@ModelAttribute("delegiert")
    public boolean delegiert() {
        return true;
    }
	
	@ModelAttribute("cmdvorgangdelegiertsuchen")
    public VorgangDelegiertSuchenCommand initCommand() {
		VorgangDelegiertSuchenCommand cmd = new VorgangDelegiertSuchenCommand();
    	cmd.setSize(20);
    	cmd.setOrder(2);
    	cmd.setOrderDirection(1);
    	cmd.setEinfacheSuche(VorgangDelegiertSuchenCommand.EinfacheSuche.offene);
        return cmd;
    }
	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/delegiert/suchen</code><br/>
	 * Seitenbeschreibung: Darstellung der Backend-Suchfunktionalit�t
	 * @param cmd Command
	 * @param neu optionaler Parameter, triggert Initialisierung des Commandobjektes bei neuer Suchanfrage
	 * @param modelMap Model in der ggf. Daten f�r die View abgelegt werden
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(method = RequestMethod.GET)
    public String suchen(@ModelAttribute(value = "cmdvorgangdelegiertsuchen") VorgangDelegiertSuchenCommand cmd, @RequestParam(value = "neu", required = false) boolean neu, ModelMap modelMap) {
		if (neu) {
			cmd = initCommand();
			modelMap.put("cmdvorgangdelegiertsuchen", cmd);
		}
    	//Suchen
		modelMap.addAttribute("vorgaenge", vorgangDao.listVorgang(cmd));
    	modelMap.put("maxPages", calculateMaxPages(cmd.getSize(), vorgangDao.countVorgang(cmd)));

		return "vorgang/delegiert/suchen";
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
	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/delegiert/suchen/karte</code><br/>
	 * Seitenbeschreibung: Kartenandarstellung f�r die Ergebnisse der aktuellen Suchanfrage 
	 * @param cmd Command
	 * @param modelMap Model in der ggf. Daten f�r die View abgelegt werden
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/karte", method = RequestMethod.GET)
	public String karte(@ModelAttribute(value = "cmdvorgangdelegiertsuchen") VorgangDelegiertSuchenCommand cmd, ModelMap modelMap) 
	{
		try {
			VorgangDelegiertSuchenCommand cmd2 = (VorgangDelegiertSuchenCommand)BeanUtils.cloneBean(cmd);
			cmd2.setPage(null);
			cmd2.setSize(null);
			
			modelMap.addAttribute("geoService", geoService);
			modelMap.addAttribute("vorgaenge", vorgangDao.listVorgang(cmd2));
			return "vorgang/delegiert/suchenKarte";
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/delegiert/suchen/vorgaenge.xls</code><br/>
	 * Funktionsbeschreibung: Erzeugt Excel-Datei mit dem Inhalt der aktuellen Suchergebnisse und liefert
	 * diese als Download mit <code>Content-Type:"application/ms-excel"</code> aus
	 * @param cmd Command
	 */
	@RequestMapping(value="/vorgaenge.xls", method = RequestMethod.GET)
    @ResponseBody
	public void excel(
    		@ModelAttribute(value = "cmdvorgangdelegiertsuchen") VorgangDelegiertSuchenCommand cmd,
			HttpServletRequest request,
			HttpServletResponse response) {
		try {
			VorgangDelegiertSuchenCommand cmd2 = (VorgangDelegiertSuchenCommand)BeanUtils.cloneBean(cmd);
			cmd2.setPage(null);
			cmd2.setSize(null);
			
			List<Vorgang> vorgaenge = vorgangDao.listVorgang(cmd2);
			
			HSSFWorkbook workbook = poiService.createSheet(PoiService.Template.vorgangDelegiertListe, vorgaenge);
			
			response.setHeader("Content-Type", "application/ms-excel");
			workbook.write(response.getOutputStream());
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
}
