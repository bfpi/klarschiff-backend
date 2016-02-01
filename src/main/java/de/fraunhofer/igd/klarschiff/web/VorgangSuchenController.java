package de.fraunhofer.igd.klarschiff.web;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.fraunhofer.igd.klarschiff.dao.GrenzenDao;
import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.poi.PoiService;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.security.User;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.vo.Auftrag;
import de.fraunhofer.igd.klarschiff.vo.EnumAuftragStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import de.fraunhofer.igd.klarschiff.web.VorgangSuchenCommand.EinfacheSuche;
import de.fraunhofer.igd.klarschiff.web.VorgangSuchenCommand.Suchtyp;
import java.util.Map;

/**
 * Controller für die Vorgangsuche
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SessionAttributes({"cmdvorgangsuchen"})
@RequestMapping("/vorgang/suchen")
@Controller
public class VorgangSuchenController {

  Logger logger = Logger.getLogger(VorgangSuchenController.class);

  @Autowired
  VorgangDao vorgangDao;

  @Autowired
  GrenzenDao grenzenDao;

  @Autowired
  PoiService poiService;

  @Autowired
  GeoService geoService;

  @Autowired
  SecurityService securityService;

  @Autowired
  KategorieDao kategorieDao;

  @Autowired
  SettingsService settingsService;

  /**
   * Liefert alle im System vorhandenen Zuständigkeiten
   */
  @ModelAttribute("allZustaendigkeiten")
  public List<Role> allZustaendigkeiten() {
    return securityService.getAllZustaendigkeiten(true);
  }

  /**
   * Liefert alle im System vorhandenen Rollen zum Delegieren
   */
  @ModelAttribute("allDelegiertAn")
  public List<Role> allDelegiertAn() {
    return securityService.getAllDelegiertAn();
  }

  /**
   * Liefert alle möglichen Ausprägungen für Vorgangs-Status-Typen
   */
  @ModelAttribute("allVorgangStatus")
  public EnumVorgangStatus[] allVorgangStatus() {
    return EnumVorgangStatus.values();
  }

  /**
   * Liefert alle möglichen Ausprägungen für Vorgangs-Status-Typen im Außendienst
   */
  @ModelAttribute("allVorgangStatusAussendienst")
  public EnumVorgangStatus[] allVorgangStatusAussendienst() {
    return EnumVorgangStatus.aussendienstVorgangStatus();
  }

  /**
   * Liefert alle möglichen Ausprägungen für Vorgangstypen
   */
  @ModelAttribute("vorgangtypen")
  public Collection<EnumVorgangTyp> populateEnumVorgangTypen() {
    return Arrays.asList(EnumVorgangTyp.values());
  }

  /**
   * Liefert alle möglichen Ausprägungen für Prioritätsbezeichner
   */
  @ModelAttribute("allPrioritaeten")
  public Collection<EnumPrioritaet> allPrioritaeten() {
    return Arrays.asList(EnumPrioritaet.values());
  }

  /**
   * Liefert (in Systemkonfiguration festgelegte) Anzahl an Unterstützungen, die benötigt werden
   * damit Idee Relevanz erlangt (z.B. in der Vorgangssuche automatisch erscheint).
   */
  @ModelAttribute("vorgangIdeenUnterstuetzer")
  public Long vorgangIdeenUnterstuetzer() {
    return settingsService.getVorgangIdeeUnterstuetzer();
  }

  /**
   * Liefert alle Stadtteile mit ihren Grenzen
   *
   * @return Liste mit Arrays [0] id (long), [1] name (String)
   */
  @ModelAttribute("allStadtteile")
  public List<Object[]> allStadtteile() {
    return grenzenDao.findStadtteilGrenzen();
  }

  /**
   * Initialisiert <code>VorgangSuchenCommand</code>-Objekt mit Standardwerten zur Benutzung als
   * ModelAttribute für Suchoperation
   */
  @ModelAttribute("cmdvorgangsuchen")
  public VorgangSuchenCommand initCommand() {
    VorgangSuchenCommand cmd = new VorgangSuchenCommand();
    cmd.setSize(20);
    cmd.setOrder(2);
    cmd.setOrderDirection(1);
    //Suchtyp
    cmd.setSuchtyp(VorgangSuchenCommand.Suchtyp.einfach);
    //Initiale einfache Suche
    cmd.setEinfacheSuche(VorgangSuchenCommand.EinfacheSuche.offene);
    //Initiale erweiterte Suche
    cmd.setErweitertArchiviert(false);
    cmd.setErweitertZustaendigkeit("#mir zugewiesen#");
    cmd.setErweitertVorgangStatus((EnumVorgangStatus[]) ArrayUtils.removeElement(ArrayUtils.removeElement(EnumVorgangStatus.values(), EnumVorgangStatus.gemeldet), EnumVorgangStatus.geloescht));
    return cmd;
  }

  /**
   * Aktualisiert Unterkategorie und Liste möglicher Hauptkategorien (abhängig von Vorgangstyp) in
   * übergebenem Model mit Daten aus übergebenem Commandobjekt
   *
   * @param model Model
   * @param cmd Command
   */
  private void updateKategorieInModel(ModelMap model, VorgangSuchenCommand cmd) {
    try {
      model.addAttribute("hauptkategorien", kategorieDao.findRootKategorienForTyp(cmd.getErweitertVorgangTyp()));
      model.addAttribute("unterkategorien", kategorieDao.findKategorie(cmd.getErweitertHauptkategorie().getId()).getChildren());
    } catch (Exception e) {
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/suchen</code><br/>
   * Seitenbeschreibung: Darstellung der Backend-Suchfunktionalität
   *
   * @param cmd Command
   * @param neu optionaler Parameter, triggert Initialisierung des Commandobjektes bei neuer
   * Suchanfrage
   * @param modelMap Model in der ggf. Daten für die View abgelegt werden
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(method = RequestMethod.GET)
  public String suchen(@ModelAttribute(value = "cmdvorgangsuchen") VorgangSuchenCommand cmd, @RequestParam(value = "neu", required = false) boolean neu, ModelMap modelMap) {
    if (neu) {
      cmd = initCommand();
      modelMap.put("cmdvorgangsuchen", cmd);
    }
    updateKategorieInModel(modelMap, cmd);
    
    if (cmd.getVorgangAuswaehlen() != null && cmd.getVorgangAuswaehlen().length > 0) {
      List<Vorgang> vorgaenge = vorgangDao.findVorgaenge(cmd.getVorgangAuswaehlen());
      for (Vorgang vorgang : vorgaenge) {
        Auftrag auftrag;
        if (vorgang.getAuftrag() == null) {
          auftrag = new Auftrag();
          auftrag.setVorgang(vorgang);
        } else {
          auftrag = vorgang.getAuftrag();
        }
        if ((!StringUtils.equals("Team wählen", cmd.getAuftragTeam()) && cmd.getAuftragDatum() != null) || ((StringUtils.equals("Team wählen", cmd.getAuftragTeam()) || cmd.getAuftragTeam() == null || StringUtils.equals("", cmd.getAuftragTeam())) && cmd.getAuftragDatum() == null)) {
            auftrag.setTeam(cmd.getAuftragTeam());
            auftrag.setDatum(cmd.getAuftragDatum());
        }
        auftrag.setPrioritaet(null);
        auftrag.setStatus(EnumAuftragStatus.nicht_abgehakt);
        vorgang.setAuftrag(auftrag);
        vorgangDao.merge(vorgang);
      }
      cmd.setVorgangAuswaehlen(null);
      cmd.setAuftragTeam(null);
      cmd.setAuftragDatum(null);
    }
    //Suchen
    modelMap.addAttribute("vorgaenge", vorgangDao.getVorgaenge(cmd));
    if (cmd.suchtyp == Suchtyp.einfach && cmd.einfacheSuche == EinfacheSuche.offene) {
      modelMap.put("missbrauchsmeldungenAbgeschlossenenVorgaenge", vorgangDao.missbrauchsmeldungenAbgeschlossenenVorgaenge());
    }
    modelMap.put("maxPages", calculateMaxPages(cmd.getSize(), vorgangDao.countVorgaenge(cmd)));

    User user = securityService.getCurrentUser();
    if (user.getUserKoordinator()) {
      modelMap.put("aussendienst_optionen_berechtigungen", true);
    }
    if (user.getUserKoordinator() && cmd.getSuchtyp() == Suchtyp.aussendienst) {
      modelMap.put("aussendienstTeams", securityService.getCurrentUser().getAussendienstKoordinatorZustaendigkeiten());
      modelMap.put("prioritaeten", Arrays.asList(EnumPrioritaet.values()));
    }

    return "vorgang/suchen";
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/suchen/karte</code><br/>
   * Seitenbeschreibung: Kartenandarstellung für die Ergebnisse der aktuellen Suchanfrage
   *
   * @param cmd Command
   * @param modelMap Model in der ggf. Daten für die View abgelegt werden
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/karte", method = RequestMethod.GET)
  public String karte(@ModelAttribute(value = "cmdvorgangsuchen") VorgangSuchenCommand cmd, ModelMap modelMap) {
    try {
      VorgangSuchenCommand cmd2 = (VorgangSuchenCommand) BeanUtils.cloneBean(cmd);
      cmd2.setPage(null);
      cmd2.setSize(null);

      modelMap.addAttribute("geoService", geoService);
      modelMap.addAttribute("vorgaenge", vorgangDao.getVorgaenge(cmd2));
      return "vorgang/suchenKarte";
    } catch (Exception e) {
      logger.error(e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL
   * <code>/vorgang/suchen/vorgaenge.xls</code><br/>
   * Funktionsbeschreibung: Erzeugt Excel-Datei mit dem Inhalt der aktuellen Suchergebnisse und
   * liefert diese als Download mit <code>Content-Type:"application/ms-excel"</code> aus
   *
   * @param cmd Command
   */
  @RequestMapping(value = "/vorgaenge.xls", method = RequestMethod.GET)
  @ResponseBody
  public void excel(
    @ModelAttribute(value = "cmdvorgangsuchen") VorgangSuchenCommand cmd,
    HttpServletRequest request,
    HttpServletResponse response) {
    try {
      VorgangSuchenCommand cmd2 = (VorgangSuchenCommand) BeanUtils.cloneBean(cmd);
      cmd2.setPage(null);
      cmd2.setSize(null);

      List<Object[]> vorgaenge = vorgangDao.getVorgaenge(cmd2);

      HSSFWorkbook workbook = poiService.createSheet(PoiService.Template.vorgangListe, vorgaenge);

      response.setHeader("Content-Type", "application/ms-excel");
      workbook.write(response.getOutputStream());
      response.setStatus(HttpServletResponse.SC_OK);
    } catch (Exception e) {
      logger.error(e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Ermittelt die Anzahl maximal benötigter Seiten aus:
   *
   * @param size gewünschter Anzahl an Elementen (Suchergebnissen) pro Seite
   * @param count gegebener Anzahl an darzustellender Elemente
   * @return maximal benötigte Seitenzahl
   */
  private int calculateMaxPages(int size, long count) {
    float nrOfPages = (float) count / size;
    return (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
  }

}
