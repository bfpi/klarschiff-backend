package de.fraunhofer.igd.klarschiff.web;

import de.fraunhofer.igd.klarschiff.dao.AuftragDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.Auftrag;
import de.fraunhofer.igd.klarschiff.vo.EnumAuftragStatus;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller für die Aussendienst-Verwaltung
 *
 * @author Robert Voß (BFPI GmbH)
 */
@Controller
public class AussendienstController {

  @Autowired
  AuftragDao auftragDao;

  @Autowired
  GeoService geoService;

  @Autowired
  VorgangDao vorgangDao;

  @Autowired
  SecurityService securityService;

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/aussendienst</code><br>
   *
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/aussendienst", method = RequestMethod.GET)
  public String aussendienst(ModelMap model, HttpServletRequest request) {
    List<String> teams = securityService.getCurrentUser().getAussendienstKoordinatorZustaendigkeiten();
    return "redirect:/aussendienst/" + teams.get(0);
  }

  /**
   * Die Methode verarbeitet den Request auf der URL <code>/aussendienst/{team}</code><br>
   *
   * @param cmd Command
   * @param team Außendienst-Team
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/aussendienst/{team}")
  public String team(@ModelAttribute(value = "cmdaussendienst") AussendienstCommand cmd,
    @PathVariable("team") String team, ModelMap model, HttpServletRequest request) {

    if (cmd.getDatum() == null) {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      long t = cal.getTime().getTime();
      t -= t % 1000; // Millisekunden auf 0 setzen
      cmd.setDatum(new Date(t + (1000 * 60 * 60 * 24)));
    }

    model.put("cmdaussendienst", cmd);
    model.put("team", team);
    model.put("koordinatorAussendienstTeams", securityService.getCurrentUser().getAussendienstKoordinatorZustaendigkeiten());
    List<Auftrag> auftraege = auftragDao.findAuftraegeByTeamAndDate(team, cmd.getDatum());
    model.put("auftraege", auftraege);
    List<Vorgang> vorgaenge = new ArrayList<Vorgang>();
    for (Auftrag a : auftraege) {
      vorgaenge.add(a.getVorgang());
    }
    model.put("vorgaenge", vorgaenge);
    model.put("geoService", geoService);
    return "aussendienst/team";
  }

  /**
   * Die Methode verarbeitet den Request auf der URL
   * <code>/aussendienst/{team}/update_sorting</code><br>
   *
   * @param cmd Command
   * @param team Außendienst-Team
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/aussendienst/{team}/update_sorting", method = RequestMethod.POST)
  public @ResponseBody
  String sortieren(@ModelAttribute(value = "cmdaussendienst") AussendienstCommand cmd,
    @PathVariable("team") String team, ModelMap model, HttpServletRequest request) {

    try {
      String[] ids = request.getParameterValues("ids[]");
      Long[] long_ids = new Long[ids.length];
      for (int i = 0; i < ids.length; i++) {
        long_ids[i] = Long.valueOf(ids[i]);
      }
      List<Auftrag> auftraege = auftragDao.findAuftraegeByVorgaenge(vorgangDao.findVorgaenge(long_ids));
      for (Auftrag auftrag : auftraege) {
        Integer position = Arrays.asList(ids).indexOf(auftrag.getVorgang().getId().toString());
        if (position != -1) {
          auftrag.setPrioritaet(position);
          vorgangDao.merge(auftrag);
        }
      }
      return "true";
    } catch (Exception e) {
      e.printStackTrace();
      return "false";
    }
  }

  /**
   * Die Methode verarbeitet den Request auf der URL
   * <code>/aussendienst/{team}/update</code><br>
   *
   * @param cmd Command
   * @param team Außendienst-Team
   * @param action
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/aussendienst/{team}/update", method = RequestMethod.POST)
  public String reset_sorting(@ModelAttribute(value = "cmdaussendienst") AussendienstCommand cmd,
    @PathVariable("team") String team,
    @RequestParam(value = "action", required = true) String action,
    ModelMap model, HttpServletRequest request) {

    if (action.equals("reset_sorting")) {
      List<Auftrag> auftraege = auftragDao.findAuftraegeByTeamAndDate(team, cmd.getDatum());
      for (Auftrag auftrag : auftraege) {
        auftrag.setPrioritaet(null);
        vorgangDao.merge(auftrag);
      }
    } else {
      if (cmd.isAlleVorgaengeAuswaehlen() || cmd.getVorgangAuswaehlen().length > 0) {
        List<Auftrag> auftraege = auftragDao.findAuftraegeByTeamAndDateAndAuswahl(team, cmd.getDatum(), cmd.getVorgangAuswaehlen());
        for (Auftrag auftrag : auftraege) {
          auftrag.setStatus(EnumAuftragStatus.valueOf(action));
          auftrag.getVorgang().setVersion(new Date());
          vorgangDao.merge(auftrag);
        }
      }
    }
    return team(cmd, team, model, request);
  }

  /**
   * Die Methode verarbeitet den Request auf der URL
   * <code>/aussendienst/{team}/entfernen/{auftrag_id}</code><br>
   *
   * @param cmd Command
   * @param team Außendienst-Team
   * @param auftrag_id Auftrag-ID
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/aussendienst/{team}/entfernen/{auftrag_id}")
  public String entfernen(@ModelAttribute(value = "cmdaussendienst") AussendienstCommand cmd,
    @PathVariable("team") String team, @PathVariable("auftrag_id") Integer auftrag_id,
    ModelMap model, HttpServletRequest request) {

    Auftrag auftrag = auftragDao.find(auftrag_id);
    String datum = new SimpleDateFormat("dd.MM.yyyy").format(auftrag.getDatum());
    if (auftrag.getTeam().equals(team)) {
      auftrag.getVorgang().setAuftrag(null);
      vorgangDao.remove(auftrag);
    }
    return "redirect:/aussendienst/" + team + "?datum=" + datum;
  }

}
