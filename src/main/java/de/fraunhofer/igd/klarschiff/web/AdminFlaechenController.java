package de.fraunhofer.igd.klarschiff.web;

import de.fraunhofer.igd.klarschiff.dao.BenutzerDao;
import de.fraunhofer.igd.klarschiff.dao.FlaechenDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.security.User;
import de.fraunhofer.igd.klarschiff.vo.Benutzer;
import de.fraunhofer.igd.klarschiff.vo.Flaeche;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller zum Bearbeiten der Flächen im Adminbereich
 *
 * @author Robert Voß (BFPI GmbH)
 */
@RequestMapping("/admin")
@Controller
@SessionAttributes("cmd")
public class AdminFlaechenController {

  @Autowired
  SecurityService securityService;
  @Autowired
  FlaechenDao flaechenDao;
  @Autowired
  BenutzerDao benutzerDao;

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/admin/flaechen</code><br>
   * Seitenbeschreibung: Übersichtsseite zur Flächenverwaltung
   *
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/flaechen", method = RequestMethod.GET)
  public String flaechen(Model model, HttpServletRequest request) {
    return renderListe(model, request);
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL
   * <code>/flaechen/{login}/bearbeiten</code><br>
   * Seitenbeschreibung: Formular zur Bearbeiten der Berechtigungen zum Zuweisen von Flaechen-Teams
   *
   * @param login User-Login
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/flaechen/{login}/bearbeiten", method = RequestMethod.GET)
  public String bearbeiten(@PathVariable("login") String login,
    Model model,
    HttpServletRequest request) {
    User user = securityService.getUser(login);
    model.addAttribute("benutzer", user);

    Iterator it = flaechenDao.getAllFlaechen().iterator();
    List<String> flaechen = new ArrayList<String>();
    while (it.hasNext()) {
      Flaeche temp = (Flaeche) it.next();
      if (!user.getFlaechen().contains(temp)) {
        flaechen.add(temp.getKurzname());
      }
    }
    model.addAttribute("flaechen", flaechen);
    return "admin/flaechen_bearbeiten";
  }

  /**
   * Die Methode verarbeitet den POST-Request auf der URL
   * <code>/flaechen/{login}/bearbeiten</code><br>
   * Seitenbeschreibung: Ändert die Berechtigungen zum Zuweisen von Flaechen-Teams
   *
   * @param login User-Login
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/flaechen/{login}/bearbeiten", method = RequestMethod.POST)
  public String bearbeitenSubmit(@PathVariable("login") String login,
    Model model,
    HttpServletRequest request) {
    //User user = securityService.getUser(login);
    Benutzer benutzer = benutzerDao.findByBenutzername(login);

    String[] zugewiesen = request.getParameterValues("zugewiesen[]");
    List<Flaeche> tmpList = new ArrayList<Flaeche>();
    if (zugewiesen != null) {
      for (String tmp : zugewiesen) {
        Flaeche f = flaechenDao.findByKurzname(tmp);
        if (f != null) {
          tmpList.add(f);
        }
      }
    }
    benutzer.setFlaechen(tmpList);
    benutzerDao.merge(benutzer);

    return renderListe(model, request);
  }

  /**
   * Die Methode Rendert die View für die Requests
   *
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  private String renderListe(Model model, HttpServletRequest request) {
    model.addAttribute("benutzer", securityService.getAllUserWithAreas());
    return "admin/flaechen";
  }
}
