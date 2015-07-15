package de.fraunhofer.igd.klarschiff.web;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ArrayUtils;
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
import de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import de.fraunhofer.igd.klarschiff.web.VorgangDelegiertSuchenCommand.EinfacheSuche;
import de.fraunhofer.igd.klarschiff.web.VorgangDelegiertSuchenCommand.Suchtyp;

/**
 * Controller für die Vorgangsuche für Externe (Delegierte)
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SessionAttributes({"cmdvorgangdelegiertsuchen"})
@RequestMapping("/vorgang/delegiert/suchen")
@Controller
public class VorgangDelegiertSuchenController {

  Logger logger = Logger.getLogger(VorgangDelegiertSuchenController.class);

  @Autowired
  VorgangDao vorgangDao;

  @Autowired
  GrenzenDao grenzenDao;

  @Autowired
  PoiService poiService;

  @Autowired
  GeoService geoService;

  @Autowired
  KategorieDao kategorieDao;

  /**
   * Liefert alle möglichen Ausprägungen für Vorgangs-Status-Typen
   */
  @ModelAttribute("allVorgangStatus")
  public EnumVorgangStatus[] allVorgangStatus() {
    return EnumVorgangStatus.values();
  }

  /**
   * Liefert alle Ausprägungen für Vorgangs-Status-Typen, die auch für Externe (Delegiert)
   * vorgesehen sind
   */
  @ModelAttribute("allDelegiertVorgangStatus")
  public EnumVorgangStatus[] allDelegiertVorgangStatus() {
    return EnumVorgangStatus.delegiertVorgangStatus();
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
   * Liefert alle Stadtteile mit ihren Grenzen
   *
   * @return Liste mit Arrays [0] id (long), [1] name (String)
   */
  @ModelAttribute("allStadtteile")
  public List<Object[]> allStadtteile() {
    return grenzenDao.findStadtteilGrenzen();
  }

  @ModelAttribute("delegiert")
  public boolean delegiert() {
    return true;
  }

  /**
   * Initialisiert <code>VorgangSuchenCommand</code>-Objekt mit Standardwerten zur Benutzung als
   * ModelAttribute für Suchoperation
   */
  @ModelAttribute("cmdvorgangdelegiertsuchen")
  public VorgangDelegiertSuchenCommand initCommand() {
    VorgangDelegiertSuchenCommand cmd = new VorgangDelegiertSuchenCommand();
    cmd.setSize(20);
    cmd.setOrder(2);
    cmd.setOrderDirection(1);
    //Suchtyp
    cmd.setSuchtyp(VorgangDelegiertSuchenCommand.Suchtyp.einfach);
    //Initiale einfache Suche
    cmd.setEinfacheSuche(VorgangDelegiertSuchenCommand.EinfacheSuche.offene);
    //Initiale erweiterte Suche
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
  private void updateKategorieInModel(ModelMap model, VorgangDelegiertSuchenCommand cmd) {
    try {
      model.addAttribute("hauptkategorien", kategorieDao.findRootKategorienForTyp(cmd.getErweitertVorgangTyp()));
      model.addAttribute("unterkategorien", kategorieDao.findKategorie(cmd.getErweitertHauptkategorie().getId()).getChildren());
    } catch (Exception e) {
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/delegiert/suchen</code><br/>
   * Seitenbeschreibung: Darstellung der Backend-Suchfunktionalität
   *
   * @param cmd Command
   * @param neu optionaler Parameter, triggert Initialisierung des Commandobjektes bei neuer
   * Suchanfrage
   * @param modelMap Model in der ggf. Daten für die View abgelegt werden
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(method = RequestMethod.GET)
  public String suchen(@ModelAttribute(value = "cmdvorgangdelegiertsuchen") VorgangDelegiertSuchenCommand cmd, @RequestParam(value = "neu", required = false) boolean neu, ModelMap modelMap) {
    if (neu) {
      cmd = initCommand();
      modelMap.put("cmdvorgangdelegiertsuchen", cmd);
    }
    updateKategorieInModel(modelMap, cmd);
    //Suchen
    modelMap.addAttribute("vorgaenge", vorgangDao.getVorgaenge(cmd));
    modelMap.put("maxPages", calculateMaxPages(cmd.getSize(), vorgangDao.countVorgaenge(cmd)));

    return "vorgang/delegiert/suchen";
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL
   * <code>/vorgang/delegiert/suchen/karte</code><br/>
   * Seitenbeschreibung: Kartenandarstellung für die Ergebnisse der aktuellen Suchanfrage
   *
   * @param cmd Command
   * @param modelMap Model in der ggf. Daten für die View abgelegt werden
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/karte", method = RequestMethod.GET)
  public String karte(@ModelAttribute(value = "cmdvorgangdelegiertsuchen") VorgangDelegiertSuchenCommand cmd, ModelMap modelMap) {
    try {
      VorgangDelegiertSuchenCommand cmd2 = (VorgangDelegiertSuchenCommand) BeanUtils.cloneBean(cmd);
      cmd2.setPage(null);
      cmd2.setSize(null);

      modelMap.addAttribute("geoService", geoService);
      modelMap.addAttribute("vorgaenge", vorgangDao.getVorgaenge(cmd2));
      return "vorgang/delegiert/suchenKarte";
    } catch (Exception e) {
      logger.error(e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL
   * <code>/vorgang/delegiert/suchen/vorgaenge.xls</code><br/>
   * Funktionsbeschreibung: Erzeugt Excel-Datei mit dem Inhalt der aktuellen Suchergebnisse und
   * liefert diese als Download mit <code>Content-Type:"application/ms-excel"</code> aus
   *
   * @param cmd Command
   */
  @RequestMapping(value = "/vorgaenge.xls", method = RequestMethod.GET)
  @ResponseBody
  public void excel(@ModelAttribute(value = "cmdvorgangdelegiertsuchen") VorgangDelegiertSuchenCommand cmd, HttpServletRequest request, HttpServletResponse response) {
    try {
      VorgangDelegiertSuchenCommand cmd2 = (VorgangDelegiertSuchenCommand) BeanUtils.cloneBean(cmd);
      cmd2.setPage(null);
      cmd2.setSize(null);

      HSSFWorkbook workbook = poiService.createSheet(PoiService.Template.vorgangDelegiertListe, vorgangDao.getVorgaenge(cmd2));

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
