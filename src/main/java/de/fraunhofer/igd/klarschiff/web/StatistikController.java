package de.fraunhofer.igd.klarschiff.web;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller für Statistiken
 *
 * @author Robert Voß (BFPI)
 */
@RequestMapping("/statistik")
@Controller
public class StatistikController {

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/statistik/kumulativ</code><br/>
   * Seitenbeschreibung: Eingabefelder für Kumulative Statistik
   *
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/kumulativ", method = RequestMethod.GET)
  public String kumulativ(Model model, HttpServletRequest request) {
    StatistikCommand cmd = new StatistikCommand();
    model.addAttribute("cmd", cmd);
    
    return "statistik/kumulativ";
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/statistik/zeitraum</code><br/>
   * Seitenbeschreibung: Eingabefelder für Statistik innerhalb eines anzugebenen Zeitraumes
   *
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/zeitraum", method = RequestMethod.GET)
  public String zeitraum(Model model, HttpServletRequest request) {
    StatistikCommand cmd = new StatistikCommand();
    model.addAttribute("cmd", cmd);
    
    return "statistik/zeitraum";
  }
}
