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
 * Controller für die Benutzerübersicht
 *
 * @author Sebastian Gutzeit (Hansestadt Rostock)
 */
@RequestMapping("/benutzer")
@Controller
public class BenutzerController {

  @Autowired
  SecurityService securityService;

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/benutzer</code><br/>
   * Seitenbeschreibung: Übersicht über die Benutzer
   *
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "", method = RequestMethod.GET)
  public String benutzer(Model model, HttpServletRequest request) {
    model.addAttribute("benutzer", securityService.getAllUser());
    return "benutzer";
  }

}
