package de.fraunhofer.igd.klarschiff.web;

import static de.fraunhofer.igd.klarschiff.web.Assert.addErrorMessage;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.classification.ClassificationService;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.image.ImageService;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.EnumZustaendigkeitStatus;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Controller zum Erstellen neuer Vorgänge im Backend-Interface
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SessionAttributes("cmd")
@RequestMapping("/vorgangneu")
@Controller
public class VorgangNeuController {

  public static Logger logger = Logger.getLogger(VorgangNeuController.class);

  @Autowired
  KategorieDao kategorieDao;

  @Autowired
  VorgangDao vorgangDao;

  @Autowired
  SecurityService securityService;

  @Autowired
  ClassificationService classificationService;

  @Autowired
  ImageService imageService;

  @Autowired
  GeoService geoService;

  /**
   * Liefert alle möglichen Ausprägungen für Vorgangstypen
   */
  @ModelAttribute("vorgangtypen")
  public Collection<EnumVorgangTyp> populateEnumVorgangTypen() {
    return Arrays.asList(EnumVorgangTyp.values());
  }

  @ModelAttribute("geoService")
  public GeoService getGeoService() {
    return geoService;
  }

  @ModelAttribute("allZustaendigkeiten")
  public Collection<Role> allZustaendigkeiten() {
    return securityService.getAllZustaendigkeiten(false);
  }

  /**
   * Aktualisiert Unterkategorie und Liste möglicher Hauptkategorien (abhängig von Vorgangstyp) in
   * übergebenem Model mit Daten aus übergebenem Commandobjekt
   *
   * @param model Model
   * @param cmd Command
   */
  private void updateKategorieInModel(ModelMap model, VorgangNeuCommand cmd) {
    try {
      model.addAttribute("hauptkategorien", kategorieDao.findRootKategorienForTyp(cmd.getVorgang().getTyp()));
      model.addAttribute("unterkategorien", kategorieDao.findKategorie(cmd.getKategorie().getId()).getChildren());
    } catch (Exception e) {
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgangneu</code><br/>
   * Seitenbeschreibung: Darstellung des Formulars zur Vorgangerstellung im Backend
   *
   * @param model Model in der ggf. Daten für die View abgelegt werden
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(method = RequestMethod.GET)
  public String form(ModelMap model) {
    VorgangNeuCommand cmd = new VorgangNeuCommand();
    cmd.getVorgang().setTyp(EnumVorgangTyp.problem);
    model.addAttribute("cmd", cmd);
    updateKategorieInModel(model, cmd);
    return "vorgangneu/form";
  }

  /**
   * Die Methode verarbeitet den POST-Request auf der URL <code>/vorgangneu</code><br/>
   * Funktionsbeschreibung: Absenden des im Backend ausgefüllten Vorgangerstellungsformulars
   *
   * @param cmd Command
   * @param result BindingResult
   * @param model Model in der ggf. Daten für die View abgelegt werden
   * @param request HttpServletRequest-Objekt
   * @return submit View im Erfolgsfall / form view bei nötigen Korrekturen
   */
  @RequestMapping(method = RequestMethod.POST)
  public String submit(@ModelAttribute("cmd") VorgangNeuCommand cmd, BindingResult result, ModelMap model, HttpServletRequest request) {
    Vorgang vorgang = cmd.getVorgang();

    cmd.validate(result, kategorieDao);

    if (result.hasErrors()) {
      if (cmd.getKategorie() != null) {
        model.addAttribute("kategorien", cmd.getKategorie().getChildren());
      }
      updateKategorieInModel(model, cmd);
      return "vorgangneu/form";
    }
    vorgang.setDatum(new Date());
    vorgang.setStatus(EnumVorgangStatus.offen);
    vorgang.setStatusDatum(new Date());
    vorgang.setPrioritaet(EnumPrioritaet.mittel);

    if (StringUtils.isNotBlank(cmd.zustaendigkeit)) {
      vorgang.setZustaendigkeit(cmd.getZustaendigkeit());
      vorgang.setZustaendigkeitFrontend(securityService.getZustaendigkeit(cmd.getZustaendigkeit()).getL());
      vorgang.setZustaendigkeitStatus(EnumZustaendigkeitStatus.zugewiesen);
    }

    vorgangDao.persist(vorgang);

    if (cmd.getFoto() != null && !cmd.getFoto().isEmpty()) {
      try {
        imageService.setImageForVorgang(cmd.getFoto(), cmd.getVorgang());
        cmd.setFotoName(cmd.getFoto().getOriginalFilename());
        cmd.setFoto(null);
      } catch (Exception e) {
        if (!cmd.getVorgang().getFotoExists()) {
          cmd.setFotoName(null);
        }
        addErrorMessage(result, "foto", e.getMessage());
      }
      vorgangDao.merge(vorgang);
    }

    if (StringUtils.isBlank(cmd.zustaendigkeit)) {
      vorgang.setZustaendigkeit(classificationService.calculateZustaendigkeitforVorgang(vorgang).getId());
      vorgang.setZustaendigkeitFrontend(securityService.getZustaendigkeit(vorgang.getZustaendigkeit()).getL());
      vorgang.setZustaendigkeitStatus(EnumZustaendigkeitStatus.zugewiesen);
      vorgangDao.merge(vorgang);
    }

    return "vorgangneu/submit";
  }

}
