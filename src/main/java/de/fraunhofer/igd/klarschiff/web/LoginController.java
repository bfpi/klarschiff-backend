package de.fraunhofer.igd.klarschiff.web;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import de.fraunhofer.igd.klarschiff.dao.KommentarDao;
import de.fraunhofer.igd.klarschiff.dao.VerlaufDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;

/**
 * Controller für die Anmeldefunktionalität am Backend
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Controller
public class LoginController {

  @Autowired
  VorgangDao vorgangDao;

  @Autowired
  VerlaufDao verlaufDao;

  @Autowired
  KommentarDao kommentarDao;

  @Autowired
  SecurityService securityService;

  @Autowired
  GeoService geoService;

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/login</code><br>
   * Seitenbeschreibung: Loginseite mit Rückmeldung bei Fehleingabe
   *
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/login")
  public String login(
    ModelMap model,
    HttpServletRequest request) {
    if (request.getParameter("access_denied_error") != null) {
      model.addAttribute("accessDeniedError", true);
    }
    if (request.getParameter("login_error") != null) {
      model.addAttribute("loginError", true);
    }
    return "login";
  }

}
