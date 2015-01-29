package de.fraunhofer.igd.klarschiff.web;

import de.fraunhofer.igd.klarschiff.dao.AuftragDao;
import de.fraunhofer.igd.klarschiff.dao.VerlaufDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.Auftrag;
import de.fraunhofer.igd.klarschiff.vo.EnumAuftragStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp;
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
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller für die Auftragslisten
 *
 * @author Robert Voß (BFPI GmbH)
 */
@Controller
public class AuftragslisteController {

  @Autowired
  AuftragDao auftragDao;

	@Autowired
	VerlaufDao verlaufDao;

  @Autowired
  VorgangDao vorgangDao;

  @Autowired
  SecurityService securityService;

  /**
   * Die Methode verarbeitet den Request auf der URL
   * <code>/auftragsliste</code><br/>
   *
   * @param cmd
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/auftragsliste")
  public String liste(@ModelAttribute(value = "cmdauftragsliste") AuftragslisteCommand cmd,
          ModelMap model, HttpServletRequest request) {

    String team = securityService.getCurrentUser().getAussendienstTeam();
    if (cmd.getDatum() == null) {
      Date d = new Date();
      d.setHours(0);
      d.setMinutes(0);
      d.setSeconds(0);
      long t = d.getTime();
      t -= t % 1000; // Millisekunden auf 0 setzen
      cmd.setDatum(new Date(t));
    }

    model.put("cmdauftragsliste", cmd);

    List<Auftrag> auftraege = auftragDao.findAuftraegeByTeamAndDate(team, cmd.getDatum());
    model.put("auftraege", auftraege);

    return "auftragsliste/liste";
  }

  /**
   * Die Methode verarbeitet den Request auf der URL
   * <code>/auftragsliste/{auftrag_id}/update_status</code><br/>
   *
   * @param cmd
   * @param auftrag_id
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/auftragsliste/{auftrag_id}/update_status", method = RequestMethod.POST)
  public @ResponseBody
  String update_status(@ModelAttribute(value = "cmdauftragsliste") AuftragslisteCommand cmd,
          @PathVariable("auftrag_id") Integer auftrag_id, ModelMap model, HttpServletRequest request) {

    try {
      Auftrag auftrag = auftragDao.find(auftrag_id);
      verlaufDao.addVerlaufToVorgang(auftrag.getVorgang(), EnumVerlaufTyp.aufgabeStatus, auftrag.getStatus().getText(), cmd.getStatus().getText());
      
      auftrag.setStatus(cmd.getStatus());
      vorgangDao.merge(auftrag);
      
      return "true";
    } catch (Exception e) {
      e.printStackTrace();
      return "false";
    }
  }

  /**
   * Liefert alle möglichen Ausprägungen für Auftrag-Status-Typen
   *
   * @return
   */
  @ModelAttribute("allAuftragStatus")
  public EnumAuftragStatus[] allAuftragStatus() {
    return EnumAuftragStatus.values();
  }

}
