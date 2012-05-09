package de.fraunhofer.igd.klarschiff.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import de.fraunhofer.igd.klarschiff.service.statistic.StatisticService;

/**
 * Controller für die Backend-Homepage
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Controller
public class HomeController {

	@Autowired
	StatisticService statisticService;
	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/</code><br/>
	 * Seitenbeschreibung: Die Klarschiff Backend Homepage mit Statistiken zu aktuellen Vorgängen
	 * @param modelMap Model in dem ggf. Daten für die View abgelegt werden
	 * @param request Request
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/")
	public String index(ModelMap modelMap, HttpServletRequest request) {
		modelMap.addAttribute("statistic", statisticService.getStatistic());
		return "index";
	}
}
