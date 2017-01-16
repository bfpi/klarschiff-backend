package de.fraunhofer.igd.klarschiff.web;

import de.fraunhofer.igd.klarschiff.dao.GrenzenDao;
import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.dao.StatistikDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.statistik.StatistikKumulativ;
import de.fraunhofer.igd.klarschiff.statistik.StatistikZeitraum;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
  GrenzenDao grenzenDao;
  
  @Autowired
  KategorieDao kategorieDao;
  
  @Autowired
  StatistikDao statistikDao;
  
  @Autowired
  VorgangDao vorgangDao;
  
  @Autowired
  SecurityService securityService;

  @Autowired
  SettingsService settingsService;

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
    if (cmd.getZeitraumBis() == null) {
      cmd.setZeitraumBis(new Date());
    }
    model.addAttribute("cmd", cmd);

    return "statistik/kumulativ";
  }

  @RequestMapping(value = "/kumulativ", method = RequestMethod.POST)
  public String kumulativSubmit(@ModelAttribute("cmd") StatistikCommand cmd, Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      cmd.setType("kumulativ");
      model.addAttribute("cmd", cmd);

      if (cmd.getZeitraumVon() == null) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        cmd.setZeitraumVon(sdf.parse(settingsService.getPropertyValue("startDatum")));
      }
      if (cmd.getZeitraumBis() == null) {
        return "redirect:/statistik/kumulativ";
      }

      StatistikKumulativ sz = new StatistikKumulativ(statistikDao, securityService, vorgangDao);
      HSSFWorkbook workbook = sz.createStatistik(cmd);

      response.setHeader("Content-Type", "application/ms-excel");
      response.setHeader("Content-Disposition", "attachment;filename=StatistikZeitraum.xls");
      workbook.write(response.getOutputStream());
      response.setStatus(HttpServletResponse.SC_OK);
    } catch (Exception e) {
      logger.error(e);
      throw new RuntimeException(e);
    }
    return "";
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

    Calendar cal = Calendar.getInstance();
    Date day = new Date();
    day.setDate(1);
    cal.setTime(day);
    cal.add(Calendar.MONTH, -1);
    cmd.setZeitraumVon(cal.getTime());

    cal.add(Calendar.MONTH, 1);
    cal.add(Calendar.DATE, -1);
    cmd.setZeitraumBis(cal.getTime());

    model.addAttribute("cmd", cmd);
    return "statistik/zeitraum";
  }

  @RequestMapping(value = "/zeitraum", method = RequestMethod.POST)
  public String zeitraumSubmit(@ModelAttribute("cmd") StatistikCommand cmd, Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      cmd.setType("zeitraum");
      model.addAttribute("cmd", cmd);

      if (cmd.getZeitraumVon() == null || cmd.getZeitraumBis() == null) {
        return "redirect:/statistik/zeitraum";
      }

      StatistikZeitraum sz = new StatistikZeitraum(grenzenDao, kategorieDao, statistikDao, securityService, settingsService);
      HSSFWorkbook workbook = sz.createStatistik(cmd);

      response.setHeader("Content-Type", "application/ms-excel");
      response.setHeader("Content-Disposition", "attachment;filename=StatistikZeitraum.xls");
      workbook.write(response.getOutputStream());
      response.setStatus(HttpServletResponse.SC_OK);
    } catch (Exception e) {
      logger.error(e);
      throw new RuntimeException(e);
    }
    return "";
  }
}
