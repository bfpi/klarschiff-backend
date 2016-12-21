package de.fraunhofer.igd.klarschiff.web;

import de.fraunhofer.igd.klarschiff.dao.StatistikDao;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.statistik.StatistikZeitraum;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller für Statistiken
 *
 * @author Robert Voß (BFPI)
 */
@RequestMapping("/statistik")
@Controller
public class StatistikController {

  Logger logger = Logger.getLogger(StatistikController.class);
  
  @Autowired
  StatistikDao statistikDao;

  @Autowired
  SecurityService securityService;

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

  @RequestMapping(value = "/kumulativ", method = RequestMethod.POST)
  public String kumulativSubmit(Model model, HttpServletRequest request) {
    StatistikCommand cmd = new StatistikCommand();
    cmd.setType("kumulativ");
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

  @RequestMapping(value = "/zeitraum", method = RequestMethod.POST)
  @ResponseBody
  public void zeitraumSubmit(@ModelAttribute("cmd") StatistikCommand cmd, Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      cmd.setType("zeitraum");
      model.addAttribute("cmd", cmd);

      StatistikZeitraum sz = new StatistikZeitraum(statistikDao, securityService);
      HSSFWorkbook workbook = sz.createStatistik(cmd);

      response.setHeader("Content-Type", "application/ms-excel");
      response.setHeader("Content-Disposition", "attachment;filename=StatistikZeitraum.xls");
      workbook.write(response.getOutputStream());
      response.setStatus(HttpServletResponse.SC_OK);
    } catch (Exception e) {
      logger.error(e);
      throw new RuntimeException(e);
    }
  }
}
