package de.fraunhofer.igd.klarschiff.web;

import static de.fraunhofer.igd.klarschiff.web.Assert.assertMaxLength;
import static de.fraunhofer.igd.klarschiff.web.Assert.assertNotEmpty;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.dao.KommentarDao;
import de.fraunhofer.igd.klarschiff.dao.LobHinweiseKritikDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.classification.ClassificationService;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.tld.CustomFunctions;
import de.fraunhofer.igd.klarschiff.vo.Auftrag;
import de.fraunhofer.igd.klarschiff.vo.EnumAuftragStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumFreigabeStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.EnumZustaendigkeitStatus;
import de.fraunhofer.igd.klarschiff.vo.Kommentar;
import de.fraunhofer.igd.klarschiff.vo.StatusKommentarVorlage;
import de.fraunhofer.igd.klarschiff.vo.Verlauf;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import de.fraunhofer.igd.klarschiff.vo.VorgangHistoryClasses;
import java.util.Date;

/**
 * Controller für die Vorgangsbearbeitung
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SessionAttributes("cmd")
@Controller
public class VorgangBearbeitenController {

  Logger logger = Logger.getLogger(VorgangBearbeitenController.class);

  @Autowired
  VorgangDao vorgangDao;

  @Autowired
  KommentarDao kommentarDao;

  @Autowired
  KategorieDao kategorieDao;

  @Autowired
  LobHinweiseKritikDao lobHinweiseKritikDao;

  @Autowired
  ClassificationService classificationService;

  @Autowired
  SecurityService securityService;

  @Autowired
  SettingsService settingsService;

  /**
   * Liefert (in Systemkonfiguration festgelegte) Anzahl an Unterstützungen, die benötigt werden
   * damit Idee Relevanz erlangt (z.B. in der Vorgangssuche automatisch erscheint).
   */
  @ModelAttribute("vorgangIdeenUnterstuetzer")
  public Long vorgangIdeenUnterstuetzer() {
    return settingsService.getVorgangIdeeUnterstuetzer();
  }

  /**
   * Liefert (in Systemkonfiguration festgelegte) maximale Zeichenanzahl für Statuskommentare zu
   * Vorgängen
   */
  @ModelAttribute("vorgangStatusKommentarTextlaengeMaximal")
  public Integer vorgangStatusKommentarTextlaengeMaximal() {
    return settingsService.getVorgangStatusKommentarTextlaengeMaximal();
  }

  /**
   * Liefert alle vorhandenen Zuständigkeiten des aktuellen Benutzers
   */
  @ModelAttribute("currentZustaendigkeiten")
  public List<Role> currentZustaendigkeiten() {
    return securityService.getCurrentZustaendigkeiten(false);
  }

  /**
   * Liefert alle im System vorhandenen Zuständigkeiten
   */
  @ModelAttribute("allZustaendigkeiten")
  public List<Role> allZustaendigkeiten() {
    return securityService.getAllZustaendigkeiten(false);
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
    EnumVorgangStatus[] allVorgangStatus = EnumVorgangStatus.values();
    allVorgangStatus = (EnumVorgangStatus[]) ArrayUtils.removeElement(ArrayUtils.removeElement(
      allVorgangStatus, EnumVorgangStatus.gemeldet), EnumVorgangStatus.offen);
    return allVorgangStatus;
  }

  /**
   * Liefert alle möglichen Ausprägungen für Vorgangs-Status-Typen (mit offenen!)
   */
  @ModelAttribute("allVorgangStatusMitOffenen")
  public EnumVorgangStatus[] allVorgangStatusMitOffenen() {
    EnumVorgangStatus[] allVorgangStatusMitOffenen = EnumVorgangStatus.values();
    allVorgangStatusMitOffenen = (EnumVorgangStatus[]) ArrayUtils.removeElement(
      allVorgangStatusMitOffenen, EnumVorgangStatus.gemeldet);
    return allVorgangStatusMitOffenen;
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
  @ModelAttribute("allPrioritaet")
  public Collection<EnumPrioritaet> allPrioritaet() {
    return Arrays.asList(EnumPrioritaet.values());
  }

  /**
   * Liefert alle Statuskommentarvorlagen
   */
  @ModelAttribute("allStatusKommentarVorlage")
  public List<StatusKommentarVorlage> allStatusKommentarVorlage() {
    return vorgangDao.findStatusKommentarVorlage();
  }

  /**
   * Liefert alle Aussendienst-Teams für den aktuellen Koordinator
   */
  @ModelAttribute("koordinatorAussendienstTeams")
  public List<String> koordinatorAussendienstTeams() {
    return securityService.getCurrentUser().getAussendienstKoordinatorZustaendigkeiten();
  }

  /**
   * Aktualisiert Unterkategorie und Liste möglicher Hauptkategorien (abhängig von Vorgangstyp) in
   * übergebenem Model mit Daten aus übergebenem Commandobjekt
   *
   * @param model Model
   * @param cmd Command
   */
  private void updateKategorieInModel(ModelMap model, VorgangBearbeitenCommand cmd) {
    try {
      cmd.setKategorie(cmd.getVorgang().getKategorie().getParent());
      model.addAttribute("hauptkategorien", kategorieDao.findRootKategorienForTyp(cmd.getVorgang().getTyp()));
      model.addAttribute("unterkategorien", kategorieDao.findKategorie(cmd.getKategorie().getId()).getChildren());
    } catch (Exception e) {
    }
  }

  /**
   * Aktualisiert interne Kommentare in übergebenem Model mit Daten aus übergebenem Commandobjekt
   *
   * @param model Model
   * @param cmd Command
   */
  private void updateKommentarInModel(ModelMap model, VorgangBearbeitenCommand cmd) {
    try {
      model.addAttribute("kommentare", kommentarDao.findKommentareForVorgang(cmd.getVorgang(), cmd.getPage(), cmd.getSize()));
      model.put("maxPagesKommentare", calculateMaxPages(cmd.getSize(), kommentarDao.countKommentare(cmd.getVorgang())));

    } catch (Exception e) {
    }
  }

  /**
   * Aktualisiert Lob, Hinweise oder Kritik in übergebenem Model mit Daten aus übergebenem
   * Commandobjekt
   *
   * @param model Model
   * @param cmd Command
   */
  private void updateLobHinweiseKritikInModel(ModelMap model, VorgangBearbeitenCommand cmd) {
    try {
      model.addAttribute("allelobhinweisekritik", lobHinweiseKritikDao.findLobHinweiseKritikForVorgang(
        cmd.getVorgang(), cmd.getPage(), cmd.getSize()));
      model.put("maxPagesLobHinweiseKritik", calculateMaxPages(cmd.getSize(),
        lobHinweiseKritikDao.countLobHinweiseKritik(cmd.getVorgang())));

    } catch (Exception e) {
    }
  }

  /**
   * Ermittelt, ob bereits der Dispatcher für den Vorgang zuständig war. Fügt diese Informattion als
   * <code>"isDispatcherInVorgangHistoryClasses"</code> zu Modell hinzu.
   *
   * @param model Model
   * @param cmd Command
   */
  private void updateZustaendigkeitStatusInModel(ModelMap model, VorgangBearbeitenCommand cmd) {
    try {
      model.addAttribute("isDispatcherInVorgangHistoryClasses",
        classificationService.isDispatcherInVorgangHistoryClasses(cmd.getVorgang()));
    } catch (Exception e) {
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/{id}/bearbeiten</code><br/>
   * Seitenbeschreibung: Formular zur Vorgangsbearbeitung oder Hinweis auf noch nicht aktivierte
   * Bearbeitbarkeit falls Vorgang noch im Status <code>gemeldet</code>
   *
   * @param id Vorgangs-ID
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/vorgang/{id}/bearbeiten", method = RequestMethod.GET)
  public String bearbeiten(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
    VorgangBearbeitenCommand cmd = new VorgangBearbeitenCommand();
    cmd.setSize(5);
    cmd.setVorgang(getVorgang(id));
    model.put("cmd", cmd);
    updateKategorieInModel(model, cmd);
    updateKommentarInModel(model, cmd);
    updateLobHinweiseKritikInModel(model, cmd);
    updateZustaendigkeitStatusInModel(model, cmd);

    return (cmd.getVorgang().getStatus() == EnumVorgangStatus.gemeldet) ? "vorgang/bearbeitenDisabled" : "vorgang/bearbeiten";
  }

  /**
   * Ermittelt Vorgang mit übergebener ID aus Backend-Datenbank
   *
   * @param id Vorgangs-ID
   * @return
   */
  @Transient
  private Vorgang getVorgang(Long id) {
    Vorgang vorgang = vorgangDao.findVorgang(id);
    for (@SuppressWarnings("unused") Verlauf verlauf : vorgang.getVerlauf());
    return vorgang;
  }

  /**
   * Die Methode verarbeitet den POST-Request auf der URL <code>/vorgang/{id}/bearbeiten</code><br/>
   * Funktionsbeschreibung: Die Wahl des <code>action</code> Parameters erlaubt folgende
   * Funktionalitäten:
   * <ul>
   * <li><code>akzeptieren</code></li>
   * <li><code>&uuml;bernehmen und akzeptieren</code></li>
   * <li><code>automatisch neu zuweisen</code></li>
   * <li><code>zuweisen</code></li>
   * <li><code>&Auml;nderungen &uuml;bernehmen</code></li>
   * <li><code>freigabeStatus_Beschreibung_extern</code></li>
   * <li><code>freigabeStatus_Beschreibung_intern</code></li>
   * <li><code>&Auml;nderungen &uuml;bernehmen </code></li>
   * <li><code>Kommentar speichern</code></li>
   * <li><code>delegieren</code></li>
   * <li><code>zur&uuml;ckholen</code></li>
   * <li><code>archivieren</code></li>
   * <li><code>wiederherstellen</code></li>
   * <li><code>setzen</code></li>
   * <li><code>zur&uuml;cksetzen</code></li>
   * </ul>
   *
   * @param cmd Command
   * @param result BindingResult
   * @param id Vorgangs-ID
   * @param action Stringparameter zur funktionalen Steuerung
   * @param model Model in der ggf. Daten für die View abgelegt werden
   * @param request HttpServletRequest-Objekt
   * @return View, die zum Rendern des Request verwendet wird
   */
  @Transactional
  @RequestMapping(value = "/vorgang/{id}/bearbeiten", method = RequestMethod.POST)
  public String bearbeitenSubbmit(
    @ModelAttribute(value = "cmd") VorgangBearbeitenCommand cmd,
    BindingResult result,
    @PathVariable("id") Long id,
    @RequestParam(value = "action", required = true) String action,
    ModelMap model,
    HttpServletRequest request) {

    action = StringEscapeUtils.escapeHtml(action);

    assertNotEmpty(cmd, result, Assert.EvaluateOn.ever, "vorgang.zustaendigkeit", null);
    assertNotEmpty(cmd, result, Assert.EvaluateOn.ever, "vorgang.status", null);

    if (StringUtils.equals("wird nicht bearbeitet", cmd.getVorgang().getStatus().getText())) {
      assertNotEmpty(cmd, result, Assert.EvaluateOn.ever, "vorgang.statusKommentar",
        "Für den Status wird nicht bearbeitet müssen Sie eine öffentliche Statusinformation angeben!");
    }

    assertMaxLength(cmd, result, Assert.EvaluateOn.ever, "vorgang.statusKommentar",
      vorgangStatusKommentarTextlaengeMaximal(),
      "Die öffentliche Statusinformation ist zu lang! Erlaubt sind hier maximal "
      + vorgangStatusKommentarTextlaengeMaximal().toString() + " Zeichen.");

    if (result.hasErrors()) {
      cmd.setVorgang(getVorgang(id));
      updateKategorieInModel(model, cmd);
      updateKommentarInModel(model, cmd);
      updateLobHinweiseKritikInModel(model, cmd);
      updateZustaendigkeitStatusInModel(model, cmd);
      return "vorgang/bearbeiten";
    }

    if (action.equals("akzeptieren")) {
      cmd.getVorgang().setZustaendigkeitStatus(EnumZustaendigkeitStatus.akzeptiert);
      vorgangDao.merge(cmd.getVorgang());
    } else if (action.equals("&uuml;bernehmen und akzeptieren")) {
      cmd.getVorgang().setZustaendigkeitStatus(EnumZustaendigkeitStatus.akzeptiert);
      cmd.getVorgang().setZustaendigkeitFrontend(securityService.getZustaendigkeit(cmd.getVorgang().getZustaendigkeit()).getL());
      vorgangDao.merge(cmd.getVorgang());
    } else if (action.equals("automatisch neu zuweisen")) {
      cmd.getVorgang().setZustaendigkeit(classificationService.calculateZustaendigkeitforVorgang(cmd.getVorgang()).getId());
      cmd.getVorgang().setZustaendigkeitFrontend(securityService.getZustaendigkeit(cmd.getVorgang().getZustaendigkeit()).getL());
      cmd.getVorgang().setZustaendigkeitStatus(EnumZustaendigkeitStatus.zugewiesen);
      vorgangDao.merge(cmd.getVorgang());
    } else if (action.equals("zuweisen")) {
      cmd.getVorgang().setZustaendigkeitStatus(EnumZustaendigkeitStatus.zugewiesen);
      cmd.getVorgang().setZustaendigkeitFrontend(securityService.getZustaendigkeit(cmd.getVorgang().getZustaendigkeit()).getL());
      vorgangDao.merge(cmd.getVorgang());
    } else if (action.equals("&Auml;nderungen &uuml;bernehmen")) {
      assertNotEmpty(cmd, result, Assert.EvaluateOn.ever, "vorgang.status", null);
      if (result.hasErrors()) {
        cmd.setVorgang(getVorgang(id));
        updateKategorieInModel(model, cmd);
        updateKommentarInModel(model, cmd);
        updateLobHinweiseKritikInModel(model, cmd);
        updateZustaendigkeitStatusInModel(model, cmd);
        return "vorgang/bearbeiten";
      }
      vorgangDao.merge(cmd.getVorgang());
    } else if (action.equals("freigabeStatus_Beschreibung_extern")) {
      cmd.getVorgang().setBeschreibungFreigabeStatus(EnumFreigabeStatus.extern);
      vorgangDao.merge(cmd.getVorgang());
    } else if (action.equals("freigabeStatus_Beschreibung_intern")) {
      cmd.getVorgang().setBeschreibungFreigabeStatus(EnumFreigabeStatus.intern);
      vorgangDao.merge(cmd.getVorgang());
    } else if (action.equals("&Auml;nderungen &uuml;bernehmen ")) {
      assertNotEmpty(cmd, result, Assert.EvaluateOn.ever, "vorgang.typ", null);
      assertNotEmpty(cmd, result, Assert.EvaluateOn.ever, "kategorie", null);
      assertNotEmpty(cmd, result, Assert.EvaluateOn.ever, "vorgang.kategorie", null);
      if (result.hasErrors()) {
        cmd.setVorgang(getVorgang(id));
        updateKategorieInModel(model, cmd);
        updateKommentarInModel(model, cmd);
        updateLobHinweiseKritikInModel(model, cmd);
        updateZustaendigkeitStatusInModel(model, cmd);
        return "vorgang/bearbeiten";
      }
      vorgangDao.merge(cmd.getVorgang());
    } else if (action.equals("Kommentar anlegen")) {
      if (!StringUtils.isBlank(cmd.getKommentar())) {
        Kommentar kommentar = new Kommentar();
        kommentar.setVorgang(cmd.getVorgang());
        kommentar.setText(cmd.getKommentar());
        kommentar.setNutzer(securityService.getCurrentUser().getName());
        kommentar.setAnzBearbeitet(0);
        kommentar.setDatum(new Date());
        kommentarDao.persist(kommentar);
        cmd.setKommentar(null);
      }
    } else if (action.equals("kommentarSave")) {
      long kId = Long.parseLong(request.getParameter("id"));
      Kommentar kommentar = kommentarDao.findById(kId);
      if (CustomFunctions.mayCurrentUserEditKommentar(kommentar)) {
        kommentar.setText(request.getParameter("kommentarEdit"));
        kommentar.setAnzBearbeitet(kommentar.getAnzBearbeitet() + 1);
        kommentarDao.persist(kommentar);
      }
    } else if (action.equals("kommentarDelete")) {
      long kId = Long.parseLong(request.getParameter("id"));
      Kommentar kommentar = kommentarDao.findById(kId);
      if (CustomFunctions.mayCurrentUserEditKommentar(kommentar)) {
        kommentar.setGeloescht(true);
        kommentarDao.persist(kommentar);
      }
    } else if (action.equals("delegieren")) {
      /*if (cmd.getVorgang().getDelegiertAn()!=null && !cmd.getVorgang().getDelegiertAn().isEmpty())
       cmd.getVorgang().setZustaendigkeitFrontend(securityService.getZustaendigkeit(cmd.getVorgang().getDelegiertAn()).getL());
       else
       cmd.getVorgang().setZustaendigkeitFrontend(securityService.getZustaendigkeit(cmd.getVorgang().getZustaendigkeit()).getL());*/
      vorgangDao.merge(cmd.getVorgang());
    } else if (action.equals("zur&uuml;ckholen")) {
      cmd.getVorgang().setDelegiertAn(null);
      //cmd.getVorgang().setZustaendigkeitFrontend(securityService.getZustaendigkeit(cmd.getVorgang().getZustaendigkeit()).getL());
      vorgangDao.merge(cmd.getVorgang());
    } else if (action.equals("archivieren")) {
      cmd.getVorgang().setArchiviert(true);
      vorgangDao.merge(cmd.getVorgang());
    } else if (action.equals("wiederherstellen")) {
      cmd.getVorgang().setArchiviert(false);
      vorgangDao.merge(cmd.getVorgang());
    } else if (action.equals("Auftrag zuweisen")) {
      Auftrag auftrag = cmd.getVorgang().getAuftrag();
      auftrag.setStatus(EnumAuftragStatus.nicht_abgehakt);
      auftrag.setVorgang(cmd.getVorgang());
      vorgangDao.merge(auftrag.getVorgang());
    } else if (action.equals("setzen")) {
      vorgangDao.merge(cmd.getVorgang());
    } else if (action.equals("zur&uuml;cksetzen")) {
      VorgangHistoryClasses history = vorgangDao.findVorgangHistoryClasses(cmd.getVorgang());
      history.getHistoryClasses().clear();
      vorgangDao.merge(history);
    }

    cmd.setVorgang(getVorgang(id));
    updateKategorieInModel(model, cmd);
    updateKommentarInModel(model, cmd);
    updateLobHinweiseKritikInModel(model, cmd);
    updateZustaendigkeitStatusInModel(model, cmd);
    return "vorgang/bearbeiten";
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
