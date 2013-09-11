package de.fraunhofer.igd.klarschiff.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.util.SqlScriptUtil;

/**
 * Controller f�r die Rollen�bersicht
 * @author Sebastian Schwarz (Hansestadt Rostock)
 */
@RequestMapping("/rollen")
@Controller
public class RollenController {
	
	@Autowired
	SecurityService securityService;

	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/rollen</code><br/>
	 * Seitenbeschreibung: �bersicht �ber die Rollen
	 * @param model Model in dem ggf. Daten f�r die View abgelegt werden
	 * @param request Request
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="", method = RequestMethod.GET)
	public String rollen(Model model, HttpServletRequest request) {
		model.addAttribute("rollenIntern", securityService.getAllZustaendigkeiten(true));
		model.addAttribute("rollenExtern", securityService.getAllDelegiertAn());
		return "rollen";
	}

}