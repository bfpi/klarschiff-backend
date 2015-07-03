package de.fraunhofer.igd.klarschiff.web;

import de.fraunhofer.igd.klarschiff.dao.AussendienstKoordinatorDao;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.security.User;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller zum Bearbeiten der Außendienstteam-Koordinatoren im Adminbereich
 *
 * @author Robert Voß (BFPI GmbH)
 */
@RequestMapping("/admin")
@Controller
@SessionAttributes("cmd")
public class AdminAussendienstController {

  @Autowired
  SecurityService securityService;
  @Autowired
  AussendienstKoordinatorDao aussendienstKoordinatorDao;

  /**
   * Die Methode verarbeitet den GET-Request auf der URL
   * <code>/admin/aussendienst</code><br/>
   * Seitenbeschreibung: Übersichtsseite zum Außendienst
   *
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/aussendienst", method = RequestMethod.GET)
  public String aussendienst(Model model, HttpServletRequest request) {
    return renderListe(model, request);
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL
   * <code>/aussendienst/{login}/bearbeiten</code><br/>
   * Seitenbeschreibung: Formular zur Bearbeiten der Berechtigungen zum Zuweisen
   * von Aussendienst-Teams
   *
   * @param login User-Login
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/aussendienst/{login}/bearbeiten", method = RequestMethod.GET)
  public String bearbeiten(@PathVariable("login") String login,
          Model model,
          HttpServletRequest request) {
    User user = securityService.getUser(login);
    if (!user.getUserKoordinator()) {
      return renderListe(model, request);
    }

    model.addAttribute("benutzer", user);

    Iterator it = securityService.getAllAussendienstTeams().iterator();
    List<String> teams = new ArrayList<String>();
    while (it.hasNext()) {
      Role temp = (Role) it.next();
      if (!user.getAussendienstKoordinatorZustaendigkeiten().contains(temp.getId())) {
        teams.add(temp.getId());
      }
    }
    model.addAttribute("aussendienstTeams", teams);
    return "admin/aussendienst_bearbeiten";
  }

  /**
   * Die Methode verarbeitet den POST-Request auf der URL
   * <code>/aussendienst/{login}/bearbeiten</code><br/>
   * Seitenbeschreibung: Ändert die Berechtigungen zum Zuweisen von
   * Aussendienst-Teams
   *
   * @param login
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/aussendienst/{login}/bearbeiten", method = RequestMethod.POST)
  public String bearbeitenSubmit(@PathVariable("login") String login,
          Model model,
          HttpServletRequest request) {
    User user = securityService.getUser(login);
    if (!user.getUserKoordinator()) {
      return renderListe(model, request);
    }

    String[] zugewiesene_teams = request.getParameterValues("zugewiesen[]");
    aussendienstKoordinatorDao.resetAussendienstByLogin(login);
    aussendienstKoordinatorDao.setTeamsForLogin(login, zugewiesene_teams);

    return renderListe(model, request);
  }

  private String renderListe(Model model, HttpServletRequest request) {
    model.addAttribute("benutzer", securityService.getAllUserForGroup(securityService.getGroupKoordinator()));
    return "admin/aussendienst";
  }
}
