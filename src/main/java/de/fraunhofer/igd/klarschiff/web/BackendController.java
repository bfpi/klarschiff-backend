package de.fraunhofer.igd.klarschiff.web;

import de.fraunhofer.igd.klarschiff.dao.AuftragDao;
import de.fraunhofer.igd.klarschiff.dao.GrenzenDao;
import java.math.BigInteger;
import java.security.*;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.dao.KommentarDao;
import de.fraunhofer.igd.klarschiff.dao.RedaktionEmpfaengerDao;
import de.fraunhofer.igd.klarschiff.dao.VerlaufDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.dao.GeoRssDao;
import de.fraunhofer.igd.klarschiff.dao.TrashmailDao;
import de.fraunhofer.igd.klarschiff.service.classification.ClassificationService;
import de.fraunhofer.igd.klarschiff.service.image.ImageService;
import de.fraunhofer.igd.klarschiff.service.mail.MailService;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.security.User;
import de.fraunhofer.igd.klarschiff.service.settings.PropertyPlaceholderConfigurer;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.vo.Auftrag;
import de.fraunhofer.igd.klarschiff.vo.EnumAuftragStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumFreigabeStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet;
import de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.EnumZustaendigkeitStatus;
import de.fraunhofer.igd.klarschiff.vo.Foto;
import de.fraunhofer.igd.klarschiff.vo.GeoRss;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;
import de.fraunhofer.igd.klarschiff.vo.Kommentar;
import de.fraunhofer.igd.klarschiff.vo.LobHinweiseKritik;
import de.fraunhofer.igd.klarschiff.vo.Missbrauchsmeldung;
import de.fraunhofer.igd.klarschiff.vo.RedaktionEmpfaenger;
import de.fraunhofer.igd.klarschiff.vo.StadtGrenze;
import de.fraunhofer.igd.klarschiff.vo.StadtteilGrenze;
import de.fraunhofer.igd.klarschiff.vo.Unterstuetzer;
import de.fraunhofer.igd.klarschiff.vo.Verlauf;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import org.codehaus.jackson.map.ObjectMapper;
import com.vividsolutions.jts.geom.Point;
import static de.bfpi.tools.GeoTools.pointWktToPoint;
import static de.bfpi.tools.GeoTools.transformPosition;
import static de.bfpi.tools.GeoTools.wgs84Projection;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.ui.ModelMap;

/**
 * Der Controller dient als Schnittstelle für das Frontend
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@RequestMapping("/service")
@Controller
public class BackendController {

  Logger logger = Logger.getLogger(BackendController.class);

  @Autowired
  KategorieDao kategorieDao;

  @Autowired
  RedaktionEmpfaengerDao redaktionEmpfaengerDao;

  @Autowired
  AuftragDao auftragDao;

  @Autowired
  GrenzenDao grenzenDao;

  @Autowired
  KommentarDao kommentarDao;

  @Autowired
  VorgangDao vorgangDao;

  @Autowired
  VerlaufDao verlaufDao;

  @Autowired
  GeoRssDao geoRssDao;

  @Autowired
  TrashmailDao trashmailDao;

  @Autowired
  ClassificationService classificationService;

  @Autowired
  SecurityService securityService;

  @Autowired
  ImageService imageService;

  @Autowired
  MailService mailService;

  @Autowired
  SettingsService settingsService;

  @Autowired
  GeoService geoService;

  ObjectMapper mapper = new ObjectMapper();

  private static final String internalProjection = PropertyPlaceholderConfigurer.getPropertyValue("geo.map.projection");

  /**
   * Die Methode verarbeitet den POST-Request auf der URL <code>/service/vorgang</code><br>
   * Beschreibung: erstellt einen neuen Vorgang
   *
   * @param authCode Code zur Identifizierung des Clients
   * @param autorEmail E-Mail-Adresse des Erstellers
   * @param bild Foto base64 kodiert
   * @param beschreibung Beschreibung
   * @param fotowunsch Fotowunsch
   * @param kategorie Kategorie
   * @param oviWkt Position als WKT
   * @param positionWGS84 Position im WGS84 Format
   * @param adresse Adresse
   * @param typ Vorgangstyp
   * @param datenschutz Datenschutzerklärung wurde akzeptiert
   * @param resultObjectOnSubmit <code>true</code> - gibt den neuen Vorgangs als Ergebnis zurück
   * @param resultHashOnSubmit <code>true</code> - gibt den Hash zum Bestätigen als Ergebnis zurück
   * @param response Response in das das Ergebnis direkt geschrieben wird
   */
  @RequestMapping(value = "/vorgang", method = RequestMethod.POST)
  @ResponseBody
  public void vorgang(
    @RequestParam(value = "authCode", required = false) String authCode,
    @RequestParam(value = "autorEmail", required = false) String autorEmail,
    @RequestParam(value = "bild", required = false) String bild,
    @RequestParam(value = "beschreibung", required = false) String beschreibung,
    @RequestParam(value = "fotowunsch", required = false) Boolean fotowunsch,
    @RequestParam(value = "kategorie", required = false) Long kategorie,
    @RequestParam(value = "oviWkt", required = false) String oviWkt,
    @RequestParam(value = "positionWGS84", required = false) String positionWGS84,
    @RequestParam(value = "adresse", required = false) String adresse,
    @RequestParam(value = "typ", required = false) String typ,
    @RequestParam(value = "datenschutz", required = false) Boolean datenschutz,
    @RequestParam(value = "resultObjectOnSubmit", required = false) Boolean resultObjectOnSubmit,
    @RequestParam(value = "resultHashOnSubmit", required = false) Boolean resultHashOnSubmit,
    HttpServletResponse response) {

    if (resultHashOnSubmit == null) {
      resultHashOnSubmit = false;
    }
    if (resultObjectOnSubmit == null) {
      resultObjectOnSubmit = false;
    }
    try {

      if (settingsService.getPropertyValueBoolean("validation.vorgang.datenschutz_required")
        && (datenschutz == null || !datenschutz)) {
        throw new BackendControllerException(101, "[datenschutz] fehlt", "Die Datenschutzerklärung wurde nicht akzeptiert.");
      }
      Vorgang vorgang = new Vorgang();
      vorgang.setSecurityService(securityService);

      if (StringUtils.isBlank(typ)) {
        throw new BackendControllerException(1, "[typ] fehlt", "Der Typ ist nicht angegeben.");
      }

      if (kategorie == null) {
        throw new BackendControllerException(3, "[kategorie] fehlt", "Die Angaben zur Kategorie fehlen.");
      }

      if (StringUtils.isBlank(autorEmail)) {
        throw new BackendControllerException(7, "[autorEmail] fehlt", "Die E-Mail-Adresse fehlt.");
      }
      if (!isShortEnough(autorEmail, 300)) {
        throw new BackendControllerException(8, "[autorEmail] zu lang", "Die angegebene E-Mail-Adresse ist zu lang.");
      }
      if (!isEmail(autorEmail)) {
        throw new BackendControllerException(9, "[autorEmail] nicht korrekt", "Die angegebene E-Mail-Adresse ist nicht gültig.");
      }
      if (isTrashMail(autorEmail)) {
        throw new BackendControllerException(10, "[autorEmail] nicht erlaubt", "Die Domain der angegebenen E-Mail-Adresse ist nicht zulässig.");
      }
      vorgang.setAutorEmail(autorEmail);
      vorgang.setHash(securityService.createHash(autorEmail + System.currentTimeMillis()));

      vorgang.setDatum(new Date());
      vorgang.setPrioritaet(EnumPrioritaet.mittel);
      if (fotowunsch == null) {
        fotowunsch = false;
      }

      vorgang.setStatus(EnumVorgangStatus.gemeldet);
      vorgang.setStatusDatum(new Date());
      vorgangParameterUebernehmen(autorEmail, vorgang, typ, kategorie, positionWGS84, oviWkt,
        adresse, beschreibung, fotowunsch, bild, false);

      if (authCode != null && authCode.equals(settingsService.getPropertyValue("auth.kod_code")) && vorgang.autorAussendienst()) {
        vorgang.setBeschreibungFreigabeStatus(EnumFreigabeStatus.extern);
      }

      if (authCode != null && authCode.equals(settingsService.getPropertyValue("auth.kod_code")) && vorgang.autorIntern()) {
        vorgang.setStatus(EnumVorgangStatus.offen);
        vorgang.setStatusDatum(new Date());
        vorgangDao.persist(vorgang);

        vorgang.setZustaendigkeit(classificationService.calculateZustaendigkeitforVorgang(vorgang).getId());
        vorgang.setZustaendigkeitFrontend(securityService.getZustaendigkeit(vorgang.getZustaendigkeit()).getL());
        vorgang.setZustaendigkeitStatus(EnumZustaendigkeitStatus.zugewiesen);

        vorgangDao.merge(vorgang);

        String neueAdresse = "nicht zuordenbar";
        if (oviWkt != null) {
          Point point = pointWktToPoint(oviWkt);
          neueAdresse = geoService.calculateAddress(point);
        } else if (positionWGS84 != null) {
          try {
            Point point = transformPosition(pointWktToPoint(positionWGS84), wgs84Projection, internalProjection);
            neueAdresse = geoService.calculateAddress(point);
          } catch (FactoryException | MismatchedDimensionException | TransformException e) {
            logger.error(e);
          }
        }
        vorgang.setAdresse(neueAdresse);

        vorgangDao.merge(vorgang, false);
      } else {
        vorgangDao.persist(vorgang);

        String neueAdresse = "nicht zuordenbar";
        if (oviWkt != null) {
          Point point = pointWktToPoint(oviWkt);
          neueAdresse = geoService.calculateAddress(point);
        } else if (positionWGS84 != null) {
          try {
            Point point = transformPosition(pointWktToPoint(positionWGS84), wgs84Projection, internalProjection);
            neueAdresse = geoService.calculateAddress(point);
          } catch (FactoryException | MismatchedDimensionException | TransformException e) {
            logger.error(e);
          }
        }
        vorgang.setAdresse(neueAdresse);

        vorgangDao.merge(vorgang, false);
        mailService.sendVorgangBestaetigungMail(vorgang);
      }

      if (resultHashOnSubmit) {
        sendOk(response, vorgang.getHash());
      } else if (resultObjectOnSubmit) {
        sendOk(response, mapper.writeValueAsString(vorgang));
      } else {
        sendOk(response);
      }

    } catch (Exception e) {
      logger.warn("Fehler bei BackendController.vorgang:", e);
      sendError(response, e);
    }
  }

  /**
   * Die Methode verarbeitet den POST-Request auf der URL
   * <code>/service/vorgangAktualisieren</code><br>
   * Beschreibung: aktualisiert einen bestehenden Vorgang
   *
   * @param id Vorgang-ID
   * @param authCode Code zur Identifizierung des Clients
   * @param autorEmail E-Mail-Adresse des Erstellers
   * @param bild Foto base64 kodiert
   * @param beschreibung Beschreibung
   * @param fotowunsch Fotowunsch
   * @param kategorie Kategorie
   * @param oviWkt Position als WKT
   * @param positionWGS84
   * @param adresse
   * @param typ Vorgangstyp
   * @param status Status
   * @param statusKommentar Statuskommentar
   * @param prioritaet Priorität
   * @param delegiertAn Delegiert An
   * @param auftragStatus Status des Auftrags
   * @param auftragPrioritaet Priorität des Auftrags
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @throws BackendControllerException
   */
  @RequestMapping(value = "/vorgangAktualisieren", method = RequestMethod.POST)
  @ResponseBody
  public void vorgangAktualisieren(
    @RequestParam(value = "id", required = false) Long id,
    @RequestParam(value = "authCode", required = false) String authCode,
    @RequestParam(value = "autorEmail", required = false) String autorEmail,
    @RequestParam(value = "bild", required = false) String bild,
    @RequestParam(value = "beschreibung", required = false) String beschreibung,
    @RequestParam(value = "fotowunsch", required = false) Boolean fotowunsch,
    @RequestParam(value = "kategorie", required = false) Long kategorie,
    @RequestParam(value = "oviWkt", required = false) String oviWkt,
    @RequestParam(value = "positionWGS84", required = false) String positionWGS84,
    @RequestParam(value = "adresse", required = false) String adresse,
    @RequestParam(value = "typ", required = false) String typ,
    @RequestParam(value = "status", required = false) String status,
    @RequestParam(value = "statusKommentar", required = false) String statusKommentar,
    @RequestParam(value = "prioritaet", required = false) Integer prioritaet,
    @RequestParam(value = "delegiertAn", required = false) String delegiertAn,
    @RequestParam(value = "auftragStatus", required = false) String auftragStatus,
    @RequestParam(value = "auftragPrioritaet", required = false) Integer auftragPrioritaet,
    HttpServletResponse response) throws BackendControllerException {

    try {
      if (id == null) {
        if (StringUtils.isBlank(typ)) {
          throw new BackendControllerException(1, "[id] fehlt", "Ohne ID kann kein Vorgang aktualisiert werden.");
        }
      }
      if (StringUtils.isBlank(autorEmail)) {
        throw new BackendControllerException(7, "[autorEmail] fehlt", "Die E-Mail-Adresse fehlt.");
      }
      if (!isShortEnough(autorEmail, 300)) {
        throw new BackendControllerException(8, "[autorEmail] zu lang", "Die angegebene E-Mail-Adresse ist zu lang.");
      }
      if (!isEmail(autorEmail)) {
        throw new BackendControllerException(9, "[autorEmail] nicht korrekt", "Die angegebene E-Mail-Adresse ist nicht gültig.");
      }
      if (isTrashMail(autorEmail)) {
        throw new BackendControllerException(10, "[autorEmail] nicht erlaubt", "Die Domain der angegebenen E-Mail-Adresse ist nicht zulässig.");
      }

      Vorgang vorgang = vorgangDao.findVorgang(id);
      if (vorgang == null) {
        throw new BackendControllerException(200, "[id] unbekannt", "Es konnte kein Vorgang mit der übergebenen ID gefunden werden.");
      }
      vorgangParameterUebernehmen(autorEmail, vorgang, typ, kategorie, positionWGS84, oviWkt,
        adresse, beschreibung, fotowunsch, bild, true);

      if (prioritaet != null) {
        if ((prioritaet - 1) > EnumPrioritaet.values().length) {
          throw new BackendControllerException(12, "[prioritaet] ungültig", "Die Priorität ist fehlerhaft und kann daher nicht verarbeitet werden.");
        }

        EnumPrioritaet ep = EnumPrioritaet.values()[prioritaet];
        if (!vorgang.getPrioritaet().equals(ep)) {
          verlaufDao.persist(verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.prioritaet, vorgang.getPrioritaet().getText(), ep.getText(), autorEmail));
        }
        vorgang.setPrioritaet(ep);
      }

      if (status != null) {
        EnumVorgangStatus evs = EnumVorgangStatus.valueOf(status);
        if (!vorgang.getStatus().equals(evs)) {
          verlaufDao.persist(verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.status, vorgang.getStatus().getText(), evs.getText(), autorEmail));
        }
        vorgang.setStatus(evs);
        vorgang.setStatusDatum(new Date());
      }

      if (statusKommentar != null) {
        if (vorgang.getStatusKommentar() == null || !vorgang.getStatusKommentar().equals(statusKommentar)) {
          verlaufDao.persist(verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.statusKommentar, vorgang.getStatusKommentar(), statusKommentar, autorEmail));
        }
        vorgang.setStatusKommentar(statusKommentar);
      }

      if (delegiertAn != null || auftragStatus != null || auftragPrioritaet != null) {
        if (authCode == null) {
          throw new BackendControllerException(13, "[authCode] fehlt", "Der authCode fehlt.");
        }
        if (!authCode.equals(settingsService.getPropertyValue("auth.kod_code"))) {
          throw new BackendControllerException(14, "[authCode] ungültig", "Der Übergebene authCode ist ungültig.");
        }

        if (delegiertAn != null) {
          if (!vorgang.getDelegiertAn().equals(delegiertAn)) {
            verlaufDao.persist(verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.delegiertAn, null, delegiertAn, autorEmail));
          }
          vorgang.setDelegiertAn(delegiertAn);
        }

        if (auftragStatus != null || auftragPrioritaet != null) {
          Auftrag auftrag = vorgang.getAuftrag();
          if (auftrag == null) {
            throw new BackendControllerException(15, "[auftragStatus] ungültig", "Ohne Auftrag kann der auftragStatus nicht aktualisiert werden.");
          }

          if (auftragStatus != null) {
            auftrag.setStatus(EnumAuftragStatus.valueOf(auftragStatus));
          }

          if (auftragPrioritaet != null) {
            auftrag.setPrioritaet(auftragPrioritaet);
          }
          vorgang.setAuftrag(auftrag);
        }
      }

      if (status != null) {
        EnumVorgangStatus evs = EnumVorgangStatus.valueOf(status);
        for (EnumVorgangStatus closedVorgangStatus : EnumVorgangStatus.closedVorgangStatus()) {
          if (evs == closedVorgangStatus) {
            Auftrag auftrag = vorgang.getAuftrag();
            if (auftrag != null && auftrag.getStatus() == EnumAuftragStatus.nicht_abgehakt) {
              auftrag.setStatus(EnumAuftragStatus.abgehakt);
              vorgang.setAuftrag(auftrag);
            }
          }
        }
      }

      vorgangDao.persist(vorgang);
      sendOk(response, mapper.writeValueAsString(vorgang));
    } catch (Exception e) {
      logger.warn("Fehler bei BackendController.vorgang:", e);
      sendError(response, e);
    }
  }

  /**
   * Die Methode verarbeitet den POST-Request auf der URL
   * <code>/service/vorgangAktualisieren</code><br>
   * Beschreibung: aktualisiert einen bestehenden Vorgang
   *
   * @param vorgang Vorgang
   * @param autorEmail E-Mail-Adresse des Erstellers
   * @param bild Foto base64 kodiert
   * @param beschreibung Beschreibung
   * @param fotowunsch Fotowunsch
   * @param kategorie Kategorie
   * @param oviWkt Position als WKT
   * @param verlaufErgaenzen VerlaufErgaenzen
   * @param positionWGS84
   * @param adresse
   * @param typ Vorgangstyp
   * @throws BackendControllerException
   */
  private void vorgangParameterUebernehmen(
    String autorEmail,
    Vorgang vorgang,
    String typ,
    Long kategorie,
    String positionWGS84,
    String oviWkt,
    String adresse,
    String beschreibung,
    Boolean fotowunsch,
    String bild,
    Boolean verlaufErgaenzen
  ) throws BackendControllerException {

    if (verlaufErgaenzen == null) {
      verlaufErgaenzen = false;
    }

    if (typ != null) {
      EnumVorgangTyp evt = EnumVorgangTyp.valueOf(typ);
      if (verlaufErgaenzen && !vorgang.getTyp().equals(evt)) {
        verlaufDao.persist(verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.typ,
          vorgang.getTyp().getText(), evt.getText(), autorEmail));
      }
      vorgang.setTyp(evt);
      if (vorgang.getTyp() == null) {
        throw new BackendControllerException(2, "[typ] nicht korrekt", "Der Typ ist nicht korrekt.");
      }
    }

    if (kategorie != null) {
      Kategorie newKat = kategorieDao.findKategorie(kategorie);

      if (verlaufErgaenzen && !vorgang.getKategorie().getId().equals(newKat.getId())) {
        verlaufDao.persist(verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.kategorie,
          vorgang.getKategorie().getParent().getName() + " / " + vorgang.getKategorie().getName(),
          newKat.getParent().getName() + " / " + newKat.getName(), autorEmail));
      }
      vorgang.setKategorie(newKat);
      if (vorgang.getKategorie() == null
        || vorgang.getKategorie().getParent() == null
        || vorgang.getKategorie().getParent().getTyp() != vorgang.getTyp()) {
        throw new BackendControllerException(4, "[kategorie] nicht korrekt", "Die Kategorie ist nicht gültig.");
      }
    }

    if (positionWGS84 != null) {
      try {
        vorgang.setPositionWGS84(positionWGS84);
      } catch (Exception e) {
        throw new BackendControllerException(12, "[positionWGS84] nicht korrekt", "Die Ortsangabe ist nicht korrekt.", e);
      }
    }

    if (oviWkt != null) {
      try {
        vorgang.setOviWkt(oviWkt);
      } catch (Exception e) {
        throw new BackendControllerException(6, "[oviWkt] nicht korrekt", "Die Ortsangabe ist nicht korrekt.");
      }
    }

    if (vorgang.getOviWkt() == null) {
      throw new BackendControllerException(5, "[position] nicht korrekt", "Keine gültige Ortsangabe.");
    }

    if (!vorgang.getOvi().within(grenzenDao.getStadtgrenze().getGrenze())) {
      throw new BackendControllerException(13, "[position] außerhalb", "Die neue Meldung befindet sich außerhalb des gültigen Bereichs.");
    }

    if (adresse != null) {
      vorgang.setAdresse(adresse);
    } else {
      String neueAdresse = "nicht zuordenbar";
      if (oviWkt != null) {
        Point point = pointWktToPoint(oviWkt);
        neueAdresse = geoService.calculateAddress(point);
      } else if (positionWGS84 != null) {
        try {
          Point point = transformPosition(pointWktToPoint(positionWGS84), wgs84Projection, internalProjection);
          neueAdresse = geoService.calculateAddress(point);
        } catch (FactoryException | MismatchedDimensionException | TransformException e) {
          logger.error(e);
        }
      }
      vorgang.setAdresse(neueAdresse);
    }

    if (beschreibung != null) {
      if (verlaufErgaenzen && (vorgang.getBeschreibung() == null || !vorgang.getBeschreibung().equals(beschreibung))) {
        verlaufDao.persist(verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.beschreibung, vorgang.getBeschreibung(), beschreibung, autorEmail));
      }
      vorgang.setBeschreibung(beschreibung);
    }

    if (fotowunsch != null) {
      if (verlaufErgaenzen && !Objects.equals(vorgang.getFotowunsch(), fotowunsch)) {
        verlaufDao.persist(verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.fotowunsch,
          vorgang.getFotowunsch() ? "aktiv" : "inaktiv", vorgang.getFotowunsch() ? "inaktiv" : "aktiv", autorEmail));
      }
      vorgang.setFotowunsch(fotowunsch);
    }

    if (bild != null && bild.getBytes().length > 0) {
      vorgangDao.persist(vorgang);
      try {
        imageService.setImageForVorgang(Base64.decode(bild.getBytes()), vorgang);
        vorgang.setFotoFreigabeStatus(EnumFreigabeStatus.intern);
        vorgang.setFotowunsch(false);
      } catch (Exception e) {
        throw new BackendControllerException(11, "[bild] nicht korrekt", "Das Bild ist fehlerhaft und kann nicht verarbeitet werden.", e);
      }
    }
  }

  /**
   * Prüft, ob der mitgegebene String eine gültige E-Mail-Adresse ist
   *
   * @param email String mit der E-Mail-Adresse
   * @return <code>true</code>, falls E-Mail-Adresse gültig, <code>false</code>, falls nicht
   */
  private static boolean isEmail(String email) {
    return Assert.matches(email, "^\\S+@\\S+\\.[A-Za-z]{2,6}$");
  }

  private boolean isTrashMail(String email) {
    String pattern = email.substring(email.lastIndexOf("@") + 1).toLowerCase();
    return trashmailDao.findTrashmail(pattern) != null;
  }

  /**
   * Prüft, ob der mitgegebene String kurz genug ist
   *
   * @param str String, dessen Länge geprüft werden soll
   * @param maxLength maximale Länge
   * @return <code>true</code>, falls String kurz genug oder leer, <code>false</code>, falls nicht
   */
  private static boolean isShortEnough(String str, int maxLength) {
    return str == null || str.length() <= maxLength;
  }

  /**
   * Die Methode verarbeitet den POST-Request auf der URL
   * <code>/service/vorgangBestaetigung</code><br>
   * Beschreibung: Vorgang bestätigen
   *
   * @param hash Hash zum Bestätigen
   * @param model
   * @return View die angezeigt werden soll
   */
  @RequestMapping(value = "/vorgangBestaetigung")
  public String vorgangBestaetigung(@RequestParam(value = "hash", required = false) String hash, ModelMap model) {

    try {
      if (StringUtils.isBlank(hash)) {
        throw new BackendControllerException(101, "[hash] fehlt");
      }
      Vorgang vorgang = vorgangDao.findVorgangByHash(hash);
      if (vorgang == null) {
        throw new BackendControllerException(102, "[hash] nicht korrekt");
      }

      for (Verlauf verlauf : vorgang.getVerlauf()) {
        verlauf.getTyp();
      }

      if (vorgang.getStatus() != EnumVorgangStatus.gemeldet) {
        model.put("alreadyAccepted", true);
      } else {
        vorgang.setStatus(EnumVorgangStatus.offen);
        vorgang.setStatusDatum(new Date());

        verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.vorgangBestaetigung, null, null);
        vorgangDao.merge(vorgang);

        vorgang.setZustaendigkeit(classificationService.calculateZustaendigkeitforVorgang(vorgang).getId());
        vorgang.setZustaendigkeitFrontend(securityService.getZustaendigkeit(vorgang.getZustaendigkeit()).getL());
        vorgang.setZustaendigkeitStatus(EnumZustaendigkeitStatus.zugewiesen);

        String neueAdresse = geoService.calculateAddress(vorgang.getOvi());
        vorgang.setAdresse(neueAdresse);

        vorgangDao.merge(vorgang, false);
      }

      model.put("message", "Die Meldung wurde erfolgreich aufgenommen.");
      model.put("vorgangId", String.valueOf(vorgang.getId()));

      String link = settingsService.getPropertyValue("geo.map.extern.extern.url");
      link = link.replaceAll("%id%", String.valueOf(vorgang.getId()));
      model.put("link", link);

      vorgangDao.merge(vorgang, false);

      return "backend/bestaetigungOk";

    } catch (Exception e) {
      logger.warn(e);
      return "backend/bestaetigungFehler";
    }
  }

  /**
   * Die Methode verarbeitet den POST-Request auf der URL <code>/service/unterstuetzer</code><br>
   * Beschreibung: erstellt eine Unterstützung für ein Vorgang
   *
   * @param vorgang Vorgang
   * @param email E-Mail-Adresse des Erstellers
   * @param datenschutz Datenschutzerklärung wurde akzeptiert
   * @param resultObjectOnSubmit <code>true</code> - gibt den neuen Vorgangs als Ergebnis zurück
   * @param resultHashOnSubmit <code>true</code> - gibt den Hash zum Bestätigen als Ergebnis zurück
   * @param response Response in das das Ergebnis direkt geschrieben wird
   */
  @RequestMapping(value = "/unterstuetzer", method = RequestMethod.POST)
  @ResponseBody
  public void unterstuetzer(
    @RequestParam(value = "vorgang", required = false) Long vorgang,
    @RequestParam(value = "email", required = false) String email,
    @RequestParam(value = "datenschutz", required = false) Boolean datenschutz,
    @RequestParam(value = "resultHashOnSubmit", required = false) Boolean resultHashOnSubmit,
    @RequestParam(value = "resultObjectOnSubmit", required = false) Boolean resultObjectOnSubmit,
    HttpServletResponse response) {
    if (resultHashOnSubmit == null) {
      resultHashOnSubmit = false;
    }
    if (resultObjectOnSubmit == null) {
      resultObjectOnSubmit = false;
    }
    try {
      if (settingsService.getPropertyValueBoolean("validation.vorgang.datenschutz_required")
        && (datenschutz == null || !datenschutz)) {
        throw new BackendControllerException(101, "[datenschutz] fehlt", "Die Datenschutzerklärung wurde nicht akzeptiert.");
      }
      Unterstuetzer unterstuetzer = new Unterstuetzer();
      if (vorgang == null) {
        throw new BackendControllerException(201, "[vorgang] fehlt", "Die Unterstützung ist keiner Meldung zugeordnet.");
      }
      Vorgang vorg = vorgangDao.findVorgang(vorgang);
      if (vorg == null) {
        throw new BackendControllerException(200, "[vorgang] ungültig", "Es konnte kein Vorgang mit der übergebenen ID gefunden werden.");
      }
      unterstuetzer.setVorgang(vorg);
      if (unterstuetzer.getVorgang() == null) {
        throw new BackendControllerException(202, "[vorgang] nicht korrekt", "Die Unterstützung ist keiner Meldung zugeordnet.");
      }

      if (StringUtils.isBlank(email)) {
        throw new BackendControllerException(203, "[email] fehlt", "Die E-Mail-Adresse fehlt.");
      }
      if (!isShortEnough(email, 300)) {
        throw new BackendControllerException(204, "[email] zu lang", "Die angegebene E-Mail-Adresse ist zu lang.");
      }
      if (!isEmail(email)) {
        throw new BackendControllerException(205, "[email] nicht korrekt", "Die angegebene E-Mail-Adresse ist nicht gültig.");
      }
      if (isTrashMail(email)) {
        throw new BackendControllerException(10, "[autorEmail] nicht erlaubt", "Die Domain der angegebenen E-Mail-Adresse ist nicht zulässig.");
      }
      unterstuetzer.setHash(securityService.createHash(unterstuetzer.getVorgang().getId() + email));
      if (vorgangDao.findUnterstuetzer(unterstuetzer.getHash()) != null) {
        throw new BackendControllerException(206, "[email] wurde bereits für den [vorgang] verwendet", "Sie können dieselbe Meldung nicht mehrmals unterstützen.");
      }
      if (StringUtils.equalsIgnoreCase(unterstuetzer.getVorgang().getAutorEmail(), email)) {
        throw new BackendControllerException(207, "[email] der autor des [vorgang] kann keine unterstützung für den [vorgang] abgeben", "Die Unterstützungsmeldung konnte nicht abgesetzt werden, da Sie Ihre eigene Meldung nicht unterstützen dürfen.");
      }

      unterstuetzer.setDatum(new Date());

      vorgangDao.persist(unterstuetzer);

      mailService.sendUnterstuetzerBestaetigungMail(unterstuetzer, email, vorgang);

      if (resultHashOnSubmit) {
        sendOk(response, unterstuetzer.getHash());
      } else if (resultObjectOnSubmit) {
        sendOk(response, mapper.writeValueAsString(unterstuetzer));
      } else {
        sendOk(response);
      }
    } catch (Exception e) {
      logger.warn(e);
      sendError(response, e);
    }
  }

  /**
   * Die Methode verarbeitet den POST-Request auf der URL
   * <code>/service/unterstuetzerBestaetigung</code><br>
   * Beschreibung: Unterstützung bestätigen
   *
   * @param hash Hash zum Bestätigen
   * @param model
   * @return View die angezeigt werden soll
   */
  @RequestMapping(value = "/unterstuetzerBestaetigung")
  public String unterstuetzerBestaetigung(@RequestParam(value = "hash", required = false) String hash, ModelMap model) {

    try {
      if (StringUtils.isBlank(hash)) {
        throw new BackendControllerException(301, "[hash] fehlt");
      }
      Unterstuetzer unterstuetzer = vorgangDao.findUnterstuetzer(hash);
      if (unterstuetzer == null) {
        throw new BackendControllerException(302, "[hash] nicht korrekt");
      }

      Vorgang vorgang = unterstuetzer.getVorgang();
      if (unterstuetzer.getDatumBestaetigung() != null) {
        model.put("alreadyAccepted", true);
      } else {
        unterstuetzer.setDatumBestaetigung(new Date());

        verlaufDao.addVerlaufToVorgang(unterstuetzer.getVorgang(), EnumVerlaufTyp.unterstuetzerBestaetigung, null, null);
        vorgangDao.merge(unterstuetzer);

        unterstuetzer.getVorgang().setAdresse(unterstuetzer.getVorgang().getAdresse());

        vorgangDao.merge(unterstuetzer, false);

        vorgang.setVersion(new Date());
        vorgangDao.merge(vorgang);
      }

      model.put("message", "Die Unterstützung wurde erfolgreich aufgenommen.");
      model.put("vorgangId", String.valueOf(vorgang.getId()));

      String link = settingsService.getPropertyValue("geo.map.extern.extern.url");
      link = link.replaceAll("%id%", String.valueOf(vorgang.getId()));
      model.put("link", link);

      return "backend/bestaetigungOk";

    } catch (Exception e) {
      logger.warn(e);
      return "backend/bestaetigungFehler";
    }
  }

  /**
   * Die Methode verarbeitet den POST-Request auf der URL
   * <code>/service/missbrauchsmeldung</code><br>
   * Beschreibung: erstellt eine Missbrauchsmeldung für einen Vorgang
   *
   * @param vorgang Vorgang
   * @param text Text der Missbrauchsmeldung
   * @param email E-Mail-Adresse des Erstellers
   * @param datenschutz Datenschutzerklärung wurde akzeptiert
   * @param resultHashOnSubmit <code>true</code> - gibt den Hash zum Bestätigen als Ergebnis zurück
   * @param resultObjectOnSubmit <code>true</code> - gibt den neuen Vorgangs als Ergebnis zurück
   * @param response Response in das das Ergebnis direkt geschrieben wird
   */
  @RequestMapping(value = "/missbrauchsmeldung", method = RequestMethod.POST)
  @ResponseBody
  public void missbrauchsmeldung(
    @RequestParam(value = "vorgang", required = false) Long vorgang,
    @RequestParam(value = "text", required = false) String text,
    @RequestParam(value = "email", required = false) String email,
    @RequestParam(value = "datenschutz", required = false) Boolean datenschutz,
    @RequestParam(value = "resultHashOnSubmit", required = false) Boolean resultHashOnSubmit,
    @RequestParam(value = "resultObjectOnSubmit", required = false) Boolean resultObjectOnSubmit,
    HttpServletResponse response) {
    if (resultHashOnSubmit == null) {
      resultHashOnSubmit = false;
    }
    if (resultObjectOnSubmit == null) {
      resultObjectOnSubmit = false;
    }
    try {
      if (settingsService.getPropertyValueBoolean("validation.vorgang.datenschutz_required") && (datenschutz == null || !datenschutz)) {
        throw new BackendControllerException(101, "[datenschutz] fehlt", "Die Datenschutzerklärung wurde nicht akzeptiert.");
      }
      Missbrauchsmeldung missbrauchsmeldung = new Missbrauchsmeldung();
      if (vorgang == null) {
        throw new BackendControllerException(401, "[vorgang] fehlt", "Die Missbrauchsmeldung ist keiner Meldung zugeordnet.");
      }
      Vorgang vorg = vorgangDao.findVorgang(vorgang);
      if (vorg == null) {
        throw new BackendControllerException(200, "[vorgang] ungültig", "Es konnte kein Vorgang mit der übergebenen ID gefunden werden.");
      }
      missbrauchsmeldung.setVorgang(vorg);
      if (missbrauchsmeldung.getVorgang() == null) {
        throw new BackendControllerException(402, "[vorgang] nicht korrekt", "Die Missbrauchsmeldung ist keiner Meldung zugeordnet.");
      }

      if (StringUtils.isBlank(text)) {
        throw new BackendControllerException(403, "[text] fehlt", "Die Beschreibung der Missbrauchsmeldung fehlt.");
      }
      missbrauchsmeldung.setText(text);

      if (StringUtils.isBlank(email)) {
        throw new BackendControllerException(404, "[email] fehlt", "Die E-Mail-Adresse fehlt.");
      }
      if (!isShortEnough(email, 300)) {
        throw new BackendControllerException(405, "[email] zu lang", "Die angegebene E-Mail-Adresse ist zu lang.");
      }
      if (!isEmail(email)) {
        throw new BackendControllerException(406, "[email] nicht korrekt", "Die angegebene E-Mail-Adresse ist nicht gültig.");
      }
      if (isTrashMail(email)) {
        throw new BackendControllerException(10, "[autorEmail] nicht erlaubt", "Die Domain der angegebenen E-Mail-Adresse ist nicht zulässig.");
      }
      missbrauchsmeldung.setAutorEmail(email);
      missbrauchsmeldung.setHash(securityService.createHash(missbrauchsmeldung.getVorgang().getId() + email + System.currentTimeMillis()));

      missbrauchsmeldung.setDatum(new Date());

      vorgangDao.persist(missbrauchsmeldung);

      mailService.sendMissbrauchsmeldungBestaetigungMail(missbrauchsmeldung, email, vorgang);

      if (resultHashOnSubmit) {
        sendOk(response, missbrauchsmeldung.getHash());
      } else if (resultObjectOnSubmit) {
        sendOk(response, mapper.writeValueAsString(missbrauchsmeldung));
      } else {
        sendOk(response);
      }
    } catch (Exception e) {
      logger.warn(e);
      sendError(response, e);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/service/kommentar</code><br>
   * Beschreibung: holt interne Kommentare zu einem Vorgang
   *
   * @param vorgang_id Vorgang-ID
   * @param response Response in das das Ergebnis direkt geschrieben wird
   */
  @RequestMapping(value = "/kommentar", method = RequestMethod.GET)
  @ResponseBody
  public void kommentar(
    @RequestParam(value = "vorgang_id", required = false) Long vorgang_id,
    HttpServletResponse response) {

    try {
      Vorgang vorgang = vorgangDao.findVorgang(vorgang_id);
      if (vorgang == null) {
        throw new BackendControllerException(200, "[vorgang_id] ungültig", "Es konnte kein Vorgang mit der übergebenen ID gefunden werden.");
      }
      sendOk(response, mapper.writeValueAsString(vorgang.getKommentare()));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/service/kommentarAnlegen</code><br>
   * Beschreibung: legt neuen internen Kommentare zu einem Vorgang an
   *
   * @param vorgang_id Vorgang-ID
   * @param authCode Code zur Identifizierung des Clients
   * @param autorEmail E-Mail-Adresse des Erstellers
   * @param text Kommentar-Text
   * @param response Response in das das Ergebnis direkt geschrieben wird
   */
  @RequestMapping(value = "/kommentar", method = RequestMethod.POST)
  @ResponseBody
  public void kommentar(
    @RequestParam(value = "vorgang_id", required = false) Long vorgang_id,
    @RequestParam(value = "authCode", required = false) String authCode,
    @RequestParam(value = "autorEmail", required = false) String autorEmail,
    @RequestParam(value = "text", required = false) String text,
    HttpServletResponse response) {

    try {
      if (vorgang_id == null) {
        throw new BackendControllerException(1, "[id] fehlt", "Ohne id kann kein Vorgang aktualisiert werden.");
      }

      Vorgang vorgang = vorgangDao.findVorgang(vorgang_id);
      if (vorgang == null) {
        throw new BackendControllerException(200, "[vorgang_id] ungültig", "Es konnte kein Vorgang mit der übergebenen ID gefunden werden.");
      }

      if (authCode == null || !authCode.equals(settingsService.getPropertyValue("auth.kod_code"))) {
        throw new BackendControllerException(2, "[authCode] ungültig", "Der Übergebene authCode ist ungültig.");
      }

      if (StringUtils.isBlank(autorEmail)) {
        throw new BackendControllerException(3, "[autorEmail] fehlt", "Die E-Mail-Adresse fehlt.");
      }
      if (!isShortEnough(autorEmail, 300)) {
        throw new BackendControllerException(4, "[autorEmail] zu lang", "Die angegebene E-Mail-Adresse ist zu lang.");
      }
      if (!isEmail(autorEmail)) {
        throw new BackendControllerException(5, "[autorEmail] nicht korrekt", "Die angegebene E-Mail-Adresse ist nicht gültig.");
      }
      if (isTrashMail(autorEmail)) {
        throw new BackendControllerException(10, "[autorEmail] nicht erlaubt", "Die Domain der angegebenen E-Mail-Adresse ist nicht zulässig.");
      }

      if (StringUtils.isBlank(text)) {
        throw new BackendControllerException(6, "[text] fehlt", "Es fehlt ein Text für den Kommentar.");
      }

      User user = securityService.getUserByEmail(autorEmail);

      Kommentar kommentar = new Kommentar();
      kommentar.setAnzBearbeitet(0);
      kommentar.setDatum(new Date());
      kommentar.setGeloescht(false);
      kommentar.setNutzer(user.getName());
      kommentar.setZuletztBearbeitet(new Date());
      kommentar.setText(text);
      kommentar.setVorgang(vorgang);

      kommentarDao.merge(kommentar);

      sendOk(response, mapper.writeValueAsString(kommentar));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/service/lobHinweiseKritik</code><br>
   * Beschreibung: holt Lob, Hinweise oder Kritik zu einem Vorgang
   *
   * @param vorgang_id Vorgang-ID
   * @param response Response in das das Ergebnis direkt geschrieben wird
   */
  @RequestMapping(value = "/lobHinweiseKritik", method = RequestMethod.GET)
  @ResponseBody
  public void lobHinweiseKritik(
    @RequestParam(value = "vorgang_id", required = false) Long vorgang_id,
    HttpServletResponse response) {

    try {
      Vorgang vorgang = vorgangDao.findVorgang(vorgang_id);
      if (vorgang == null) {
        throw new BackendControllerException(200, "[vorgang_id] ungültig", "Es konnte kein Vorgang mit der übergebenen ID gefunden werden.");
      }
      sendOk(response, mapper.writeValueAsString(vorgang.getLobHinweiseKritik()));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den POST-Request auf der URL
   * <code>/service/lobHinweiseKritik</code><br>
   * Beschreibung: erstellt Lob, Hinweise oder Kritik zu einem Vorgang
   *
   * @param vorgang Vorgang
   * @param email E-Mail-Adresse des Erstellers
   * @param freitext Freitext
   * @param datenschutz Datenschutzerklärung wurde akzeptiert
   * @param response Response in das das Ergebnis direkt geschrieben wird
   */
  @RequestMapping(value = "/lobHinweiseKritik", method = RequestMethod.POST)
  @ResponseBody
  public void lobHinweiseKritik(
    @RequestParam(value = "vorgang", required = false) Long vorgang,
    @RequestParam(value = "email", required = false) String email,
    @RequestParam(value = "freitext", required = false) String freitext,
    @RequestParam(value = "datenschutz", required = false) Boolean datenschutz,
    HttpServletResponse response) {
    try {
      if (settingsService.getPropertyValueBoolean("validation.vorgang.datenschutz_required")
        && (datenschutz == null || !datenschutz)) {
        throw new BackendControllerException(101, "[datenschutz] fehlt", "Die Datenschutzerklärung wurde nicht akzeptiert.");
      }
      LobHinweiseKritik lobHinweiseKritik = new LobHinweiseKritik();
      if (vorgang == null) {
        throw new BackendControllerException(401, "[vorgang] fehlt", "Lob, Hinweise oder Kritik kann/können keiner Meldung zugeordnet werden.");
      }
      Vorgang vorg = vorgangDao.findVorgang(vorgang);
      if (vorg == null) {
        throw new BackendControllerException(200, "[vorgang] ungültig", "Es konnte kein Vorgang mit der übergebenen ID gefunden werden.");
      }
      lobHinweiseKritik.setVorgang(vorg);
      if (lobHinweiseKritik.getVorgang() == null) {
        throw new BackendControllerException(402, "[vorgang] nicht korrekt", "Lob, Hinweise oder Kritik kann/können keiner Meldung zugeordnet werden.");
      }

      if (StringUtils.isBlank(email)) {
        throw new BackendControllerException(404, "[email] fehlt", "Die E-Mail-Adresse fehlt.");
      }
      if (!isShortEnough(email, 300)) {
        throw new BackendControllerException(405, "[email] zu lang", "Die angegebene E-Mail-Adresse ist zu lang.");
      }
      if (!isEmail(email)) {
        throw new BackendControllerException(406, "[email] nicht korrekt", "Die angegebene E-Mail-Adresse ist nicht gültig.");
      }
      if (isTrashMail(email)) {
        throw new BackendControllerException(10, "[autorEmail] nicht erlaubt", "Die Domain der angegebenen E-Mail-Adresse ist nicht zulässig.");
      }
      lobHinweiseKritik.setAutorEmail(email);

      // aktuelle Zuständigkeit des Vorgangs bestimmen und entsprechende Variable belegen
      String zustaendigkeit = vorg.getZustaendigkeit();

      // Variable für E-Mail-Adresse des Empfängers definieren
      String empfaengerEmail = new String();

      // falls aktuelle Zuständigkeit existiert
      if (zustaendigkeit != null && !zustaendigkeit.trim().isEmpty()) {

        // alle Nutzernamen der aktuellen Zuständigkeit bestimmen
        List<String> allUserNamesForRole = securityService.getAllUserNamesForRole(zustaendigkeit);

        // denjenigen Nutzernamen als Empfänger aus der aktuellen Zuständigkeit bestimmen, der gemäß dem Verlauf die letzte Bearbeitung am Vorgang durchgeführt hat
        String empfaenger = verlaufDao.findLastUserForVorgangAndZustaendigkeit(vorg, allUserNamesForRole);

        // falls ein Empfänger gefunden wurde
        if (empfaenger != null && !empfaenger.trim().isEmpty()) {

          // E-Mail-Adresse des Empfängers ermitteln und entsprechende Variable belegen
          empfaengerEmail = securityService.getUserEmailForRoleByName(empfaenger, zustaendigkeit);

          // Empfänger-E-Mail-Adresse für Lob, Hinweise oder Kritik auf E-Mail-Adresse des Empfängers setzen
          lobHinweiseKritik.setEmpfaengerEmail(empfaengerEmail);

          // Lob, Hinweise oder Kritik als E-Mail versenden
          mailService.sendLobHinweiseKritikMail(vorg, email, empfaengerEmail, freitext.trim());

        } // ansonsten
        else {

          // alle Empfänger redaktioneller E-Mails der aktuellen Zuständigkeit bestimmen, die zugleich auch Lob, Hinweise oder Kritik als E-Mail empfangen sollen
          List<RedaktionEmpfaenger> allEmpfaengerLobHinweiseKritikForZustaendigkeit = redaktionEmpfaengerDao.getEmpfaengerListLobHinweiseKritikForZustaendigkeit(vorg.getZustaendigkeit());

          // falls Empfänger gefunden wurden
          if (allEmpfaengerLobHinweiseKritikForZustaendigkeit.size() > 0 && !allEmpfaengerLobHinweiseKritikForZustaendigkeit.isEmpty()) {

            // Zählvariable definieren
            Short zaehler = 0;

            // Empfänger durchlaufen
            for (RedaktionEmpfaenger empfaengerLobHinweiseKritikForZustaendigkeit : allEmpfaengerLobHinweiseKritikForZustaendigkeit) {

              // falls es nur ein Empfänger ist
              if (allEmpfaengerLobHinweiseKritikForZustaendigkeit.size() == 1) {

                // E-Mail-Adresse des Empfängers ermitteln und entsprechende Variable belegen
                empfaengerEmail = empfaengerLobHinweiseKritikForZustaendigkeit.getEmail();

                // Lob, Hinweise oder Kritik als E-Mail versenden
                mailService.sendLobHinweiseKritikMail(vorg, email, empfaengerEmail, freitext.trim());

                // aus Empfänger-Durchlauf aussteigen
                break;

              } // ansonsten
              else {

                // E-Mail-Adresse des aktuellen Empfängers ermitteln und entsprechende Variable belegen
                String tempEmpfaengerEmail = empfaengerLobHinweiseKritikForZustaendigkeit.getEmail();

                // beim ersten Empfänger
                if (zaehler == 0) {

                  // entsprechende Variable mit der aktuellen E-Mail-Adresse belegen
                  empfaengerEmail = tempEmpfaengerEmail;

                } // ansonsten
                else {

                  // entsprechende Variable als kommaseparierten String mit der aktuellen E-Mail-Adresse fortführen
                  empfaengerEmail = empfaengerEmail + ", " + tempEmpfaengerEmail;

                }

                // Lob, Hinweise oder Kritik als E-Mail an aktuelle E-Mail-Adresse versenden
                mailService.sendLobHinweiseKritikMail(vorg, email, tempEmpfaengerEmail, freitext.trim());

              }

              // Zählvariable erhöhen
              zaehler++;
            }

            // Empfänger-E-Mail-Adresse für Lob, Hinweise oder Kritik auf E-Mail-Adresse(n) des/der Empfänger(s) setzen
            lobHinweiseKritik.setEmpfaengerEmail(empfaengerEmail);

          }

        }
      }

      if (StringUtils.isBlank(freitext)) {
        throw new BackendControllerException(403, "[freitext] fehlt", "Der Freitext fehlt.");
      }
      lobHinweiseKritik.setFreitext(freitext);

      lobHinweiseKritik.setDatum(new Date());

      verlaufDao.addVerlaufToVorgang(lobHinweiseKritik.getVorgang(), EnumVerlaufTyp.lobHinweiseKritik, null, null);

      vorgangDao.persist(lobHinweiseKritik);

      sendOk(response, mapper.writeValueAsString(lobHinweiseKritik));
    } catch (Exception e) {
      logger.warn(e);
      sendError(response, e);
    }
  }

  /**
   * Die Methode verarbeitet den POST-Request auf der URL
   * <code>/service/missbrauchsmeldungBestaetigung</code><br>
   * Beschreibung: Vorgang bestätigen
   *
   * @param hash Hash zum Bestätigen
   * @return View die angezeigt werden soll
   */
  @RequestMapping(value = "/missbrauchsmeldungBestaetigung")
  public String missbrauchsmeldungBestaetigung(@RequestParam(value = "hash", required = false) String hash, ModelMap model) {

    try {
      if (StringUtils.isBlank(hash)) {
        throw new BackendControllerException(501, "[hash] fehlt");
      }
      Missbrauchsmeldung missbrauchsmeldung = vorgangDao.findMissbrauchsmeldung(hash);
      if (missbrauchsmeldung == null) {
        throw new BackendControllerException(502, "[hash] nicht korrekt");
      }

      if (missbrauchsmeldung.getDatumBestaetigung() != null) {
        model.put("alreadyAccepted", true);
      } else {

        missbrauchsmeldung.setDatumBestaetigung(new Date());

        verlaufDao.addVerlaufToVorgang(missbrauchsmeldung.getVorgang(), EnumVerlaufTyp.missbrauchsmeldungBestaetigung, null, null);
        vorgangDao.merge(missbrauchsmeldung);

        missbrauchsmeldung.getVorgang().setAdresse(missbrauchsmeldung.getVorgang().getAdresse());

        vorgangDao.merge(missbrauchsmeldung, false);
        Vorgang vorgang = missbrauchsmeldung.getVorgang();
        vorgang.setVersion(new Date());
        vorgangDao.merge(vorgang);
      }

      model.put("message", "Die Missbrauchsmeldung wurde erfolgreich aufgenommen und die entsprechende Meldung damit deaktiviert.");
      return "backend/bestaetigungOk";

    } catch (Exception e) {
      logger.warn(e);
      return "backend/bestaetigungFehler";
    }
  }

  /**
   * Die Methode verarbeitet den POST-Request auf der URL <code>/service/vorgangLoeschen</code><br>
   * Beschreibung: Vorgang löschen
   *
   * @param hash Hash zum Löschen
   * @return View die angezeigt werden soll
   */
  @RequestMapping(value = "/vorgangLoeschen")
  public String vorgangloeschen(@RequestParam(value = "hash", required = false) String hash, ModelMap model) {

    try {
      if (StringUtils.isBlank(hash)) {
        throw new BackendControllerException(601, "[hash] fehlt");
      }
      Vorgang vorgang = vorgangDao.findVorgangByHash(hash);
      if (vorgang == null) {
        throw new BackendControllerException(602, "[hash] nicht korrekt");
      }

      if ((vorgang.getStatus() == EnumVorgangStatus.gemeldet || vorgang.getStatus() == EnumVorgangStatus.offen)
        && vorgang.getUnterstuetzer().size() == 0 && vorgang.getMissbrauchsmeldungen().size() == 0) {
        vorgang.setStatus(EnumVorgangStatus.geloescht);
        vorgang.setStatusDatum(new Date());
        vorgangDao.merge(vorgang);

      } else {
        model.put("alreadyDeleted", true);
      }

      return "backend/vorgangLoeschenOk";

    } catch (Exception e) {
      return "backend/vorgangLoeschenFehler";
    }
  }

  /**
   * @param stadtteilIds IDs der ausgählten Stadtteile
   * @param oviWkt überwachte Fläche als WKT
   * @param probleme Probleme überwachen?
   * @param problemeHauptkategorien Liste der überwachten Hauptkategorien bei den Problemen
   * @param problemeUnterkategorien Liste der überwachten Unterkategorien bei den Problemen
   * @param ideen Ideen überwachen?
   * @param ideenHauptkategorien Liste der überwachten Hauptkategorien bei den Ideen
   * @param ideenUnterkategorien Liste der überwachten Unterkategorien bei den Ideen
   * @param response Response in das das Ergebnis direkt geschrieben wird
   */
  @RequestMapping(value = "/geoRss", method = RequestMethod.POST)
  @ResponseBody
  public void geoRss(
    @RequestParam(value = "stadtteilIds", required = false) String stadtteilIds,
    @RequestParam(value = "oviWkt", required = false) String oviWkt,
    @RequestParam(value = "probleme", required = false) Boolean probleme,
    @RequestParam(value = "problemeHauptkategorien", required = false) String problemeHauptkategorien,
    @RequestParam(value = "problemeUnterkategorien", required = false) String problemeUnterkategorien,
    @RequestParam(value = "ideen", required = false) Boolean ideen,
    @RequestParam(value = "ideenHauptkategorien", required = false) String ideenHauptkategorien,
    @RequestParam(value = "ideenUnterkategorien", required = false) String ideenUnterkategorien,
    HttpServletResponse response) {

    try {
      logger.debug("geoRss oviWkt: " + oviWkt);
      GeoRss geoRss = new GeoRss();
      if (StringUtils.isBlank(oviWkt)) {
        if (StringUtils.isBlank(stadtteilIds)) {
          throw new BackendControllerException(701, "[oviWkt], [stadtteilIds] fehlt", "Die Ortsangaben fehlen");
        } else {
          if (stadtteilIds.equals("-1")) {
            oviWkt = grenzenDao.getStadtgrenze().getGrenzeWkt();
          } else {
            oviWkt = (String) grenzenDao.getGeometrieFromStadtteilGrenzenAsWkt(stadtteilIds);
          }
        }
      }
      try {
        geoRss.setOviWkt(oviWkt);
      } catch (Exception e) {
        throw new BackendControllerException(702, "[oviWkt] nicht korrekt", "Die Ortsangaben sind nicht korrekt.", e);
      }
      if (probleme == null) {
        throw new BackendControllerException(703, "[probleme] fehlt");
      }
      geoRss.setProbleme(probleme);
      geoRss.setProblemeHauptkategorien(problemeHauptkategorien);
      geoRss.setProblemeUnterkategorien(problemeUnterkategorien);
      if (ideen == null) {
        throw new BackendControllerException(704, "[ideen] fehlt");
      }
      geoRss.setIdeen(ideen);
      geoRss.setIdeenHauptkategorien(ideenHauptkategorien);
      geoRss.setIdeenUnterkategorien(ideenUnterkategorien);

      vorgangDao.persist(geoRss);

      HashMap result = new HashMap<String, String>();
      byte[] bytesOfId = geoRss.getId().toString().getBytes("UTF-8");
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] idDigest = md.digest(bytesOfId);
      result.put("rss_id", new BigInteger(1, idDigest).toString(16));

      sendOk(response, mapper.writeValueAsString(result));
    } catch (Exception e) {
      logger.warn(e);
      sendError(response, e);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/adressensuche</code><br>
   *
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @throws java.io.IOException
   */
  @RequestMapping(value = "/adressensuche", method = RequestMethod.GET)
  @ResponseBody
  public void adressensuche(
    @RequestParam(value = "query", required = false) String query,
    HttpServletResponse response) throws IOException {

    try {
      sendOk(response, geoService.searchAddress(query));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/auftraege</code><br>
   *
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @throws java.io.IOException
   */
  @RequestMapping(value = "/auftraege", method = RequestMethod.POST)
  @ResponseBody
  public void auftraege(
    HttpServletResponse response) throws IOException {

    try {
      List<Auftrag> auftraege = auftragDao.alleAuftraege();
      sendOk(response, mapper.writeValueAsString(auftraege));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/auftraegeEinerGruppe</code><br>
   *
   * @param team Außendienst-Team
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @throws java.io.IOException
   */
  @RequestMapping(value = "/auftraegeEinerGruppe", method = RequestMethod.POST)
  @ResponseBody
  public void auftraegeEinerGruppe(
    @RequestParam(value = "team") String team,
    HttpServletResponse response) throws IOException {

    try {
      List<Auftrag> auftraege = auftragDao.findAuftraegeByTeam(team);
      sendOk(response, mapper.writeValueAsString(auftraege));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/auftraegeEinerGruppeAm</code><br>
   *
   * @param team Außendienst-Team
   * @param datum Datum
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @throws java.io.IOException
   */
  @RequestMapping(value = "/auftraegeEinerGruppeAm", method = RequestMethod.POST)
  @ResponseBody
  public void auftraegeEinerGruppeAm(
    @RequestParam(value = "team") String team,
    @RequestParam(value = "datum") String datum,
    HttpServletResponse response) throws IOException {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    try {
      List<Auftrag> auftraege = auftragDao.findAuftraegeByTeamAndDate(team, sdf.parse(datum));

      sendOk(response, mapper.writeValueAsString(auftraege));
    } catch (ParseException ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/auftrag</code><br>
   *
   * @param id ID des Auftrags
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @throws java.io.IOException
   */
  @RequestMapping(value = "/auftrag", method = RequestMethod.POST)
  @ResponseBody
  public void auftrag(
    @RequestParam(value = "id") Integer id,
    HttpServletResponse response) throws IOException {

    try {
      Auftrag auftrag = auftragDao.find(id);
      sendOk(response, mapper.writeValueAsString(auftrag));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/auftrag</code><br>
   *
   * @param authCode
   * @param status
   * @param date
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @throws java.io.IOException
   */
  @RequestMapping(value = "/auftraege", method = RequestMethod.GET)
  @ResponseBody
  public void auftraege(
    @RequestParam(value = "authCode", required = false) String authCode,
    @RequestParam(value = "date", required = false) String date,
    @RequestParam(value = "status", required = false) String status,
    HttpServletResponse response) throws IOException {

    try {
      if (authCode != null && authCode.equals(settingsService.getPropertyValue("auth.kod_code"))) {
        if (StringUtils.isBlank(date)) {
          throw new BackendControllerException(604, "[date] fehlt", "Es wurde kein Datum übergeben.");
        }

        List<Auftrag> liste;
        if (StringUtils.isBlank(status)) {
          liste = auftragDao.findAuftraegeByDate(getDateFromParam(date));
        } else {
          liste = auftragDao.findAuftraegeByDateAndStatus(getDateFromParam(date), EnumAuftragStatus.valueOf(status));
        }

        sendOk(response, mapper.writeValueAsString(liste));
      } else {
        sendOk(response);
      }
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/auftrag</code><br>
   *
   * @param authCode
   * @param vorgang_id
   * @param agency_responsible
   * @param date
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @throws java.io.IOException
   */
  @RequestMapping(value = "/auftragAnlegen", method = RequestMethod.POST)
  @ResponseBody
  public void auftragAnlegen(
    @RequestParam(value = "authCode", required = false) String authCode,
    @RequestParam(value = "vorgang_id") Integer vorgang_id,
    @RequestParam(value = "agency_responsible") String agency_responsible,
    @RequestParam(value = "date") String date,
    HttpServletResponse response) throws IOException {

    try {
      if (authCode != null && authCode.equals(settingsService.getPropertyValue("auth.kod_code"))) {
        Vorgang vorgang = vorgangDao.findVorgang(Long.valueOf(vorgang_id));

        if (vorgang.getAuftrag() != null) {
          throw new BackendControllerException(601, "[auftrag] existiert", "Für den übergebenen Vorgang existiert bereits ein Auftrag");
        }

        if (StringUtils.isBlank(agency_responsible)) {
          throw new BackendControllerException(603, "[agency_responsible] fehlt", "Es wurde kein Außendienst-Team übergeben.");
        }

        if (StringUtils.isBlank(date)) {
          throw new BackendControllerException(604, "[date] fehlt", "Es wurde kein Datum übergeben.");
        }

        Auftrag auftrag = new Auftrag();
        auftrag.setTeam(agency_responsible);
        auftrag.setDatum(getDateFromParam(date));
        auftrag.setVorgang(vorgang);
        vorgang.setAuftrag(auftrag);
        vorgangDao.persist(vorgang);

        sendOk(response, mapper.writeValueAsString(auftrag));
      } else {
        sendOk(response);
      }
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/auftrag</code><br>
   *
   * @param authCode
   * @param vorgang_id
   * @param status
   * @param date
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @throws java.io.IOException
   */
  @RequestMapping(value = "/auftragAktualisieren", method = RequestMethod.POST)
  @ResponseBody
  public void auftragAktualisieren(
    @RequestParam(value = "authCode", required = false) String authCode,
    @RequestParam(value = "vorgang_id") Integer vorgang_id,
    @RequestParam(value = "status") String status,
    @RequestParam(value = "date") String date,
    HttpServletResponse response) throws IOException {

    try {
      if (authCode != null && authCode.equals(settingsService.getPropertyValue("auth.kod_code"))) {
        Vorgang vorgang = vorgangDao.findVorgang(Long.valueOf(vorgang_id));
        Auftrag auftrag = vorgang.getAuftrag();

        if (auftrag == null) {
          throw new BackendControllerException(602, "[auftrag] existiert nicht", "Für den übergebenen Vorgang existiert kein Auftrag");
        }

        if (StringUtils.isBlank(status)) {
          throw new BackendControllerException(605, "[status] fehlt", "Es wurde kein status übergeben.");
        }

        if (StringUtils.isBlank(date)) {
          throw new BackendControllerException(606, "[date] fehlt", "Es wurde kein Datum übergeben.");
        }

        auftrag.setDatum(getDateFromParam(date));
        auftrag.setStatus(EnumAuftragStatus.valueOf(status));
        vorgang.setAuftrag(auftrag);
        vorgangDao.persist(vorgang);

        sendOk(response, mapper.writeValueAsString(auftrag));
      } else {
        sendOk(response);
      }
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/setzeStatus</code><br>
   *
   * @param id ID des Auftrags
   * @param status Status
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @throws java.io.IOException
   */
  @RequestMapping(value = "/setzeStatus", method = RequestMethod.POST)
  @ResponseBody
  public void setzeStatus(
    @RequestParam(value = "id") Integer id,
    @RequestParam(value = "status") String status,
    HttpServletResponse response) throws IOException {

    try {
      Auftrag auftrag = auftragDao.find(id);
      for (EnumAuftragStatus val : EnumAuftragStatus.values()) {
        if (val.toString().equals(status)) {
          auftrag.setStatus(val);
          vorgangDao.merge(auftrag);
          sendOk(response, mapper.writeValueAsString(auftrag));
          return;
        }
      }
      sendOk(response);
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/kategorien</code><br>
   *
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @throws java.io.IOException
   */
  @RequestMapping(value = "/kategorien", method = RequestMethod.POST)
  @ResponseBody
  public void kategorien(
    HttpServletResponse response) throws IOException {

    try {
      List<Kategorie> kategorien = kategorieDao.findRootKategorien();
      kategorien.addAll(kategorieDao.getKategorien());
      sendOk(response, mapper.writeValueAsString(kategorien));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/kategorie</code><br>
   *
   * @param id ID der Kategorie
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @throws java.io.IOException
   */
  @RequestMapping(value = "/kategorie", method = RequestMethod.POST)
  @ResponseBody
  public void kategorie(
    @RequestParam(value = "id") Integer id,
    HttpServletResponse response) throws IOException {

    try {
      Kategorie kategorie = kategorieDao.findKategorie(Long.parseLong(id.toString()));
      sendOk(response, mapper.writeValueAsString(kategorie));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/unterkategorien</code><br>
   *
   * @param withKategorien
   * @param authCode Code zur Identifizierung des Clients
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @throws java.io.IOException
   */
  @RequestMapping(value = "/unterkategorien", method = RequestMethod.GET)
  @ResponseBody
  public void unterkategorien(
    @RequestParam(value = "extensions", required = false) boolean withKategorien,
    @RequestParam(value = "authCode", required = false) String authCode,
    HttpServletResponse response) throws IOException {

    try {
      List<Kategorie> kategorien;
      if (authCode != null && authCode.equals(settingsService.getPropertyValue("auth.kod_code"))) {
        kategorien = kategorieDao.getKategorien();
      } else {
        if (withKategorien) {
          kategorien = kategorieDao.getAllKategorien();
        } else {
          kategorien = kategorieDao.getKategorien(false);
        }
      }

      sendOk(response, mapper.writeValueAsString(kategorien));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/position</code><br>
   *
   * @param positionWGS84
   * @return Wenn die Postion innerhalb des gültigen Bereichs liegt <code>HttpStatus.OK</code> sonst
   * <code>HttpStatus.FORBIDDEN</code>
   */
  @RequestMapping(value = "/position", method = RequestMethod.GET)
  public ResponseEntity position(
    @RequestParam(value = "positionWGS84", required = false) String positionWGS84
  ) {

    HttpStatus result = HttpStatus.OK;
    Vorgang v = new Vorgang();
    try {
      v.setPositionWGS84(positionWGS84);
      if (!v.getOvi().within(grenzenDao.getStadtgrenze().getGrenze())) {
        result = HttpStatus.FORBIDDEN;
      }
    } catch (Exception ex) {
      result = HttpStatus.FORBIDDEN;
    }

    return new ResponseEntity(result);
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgaenge</code><br>
   *
   * @param id ID des Vorgangs
   * @param ids Liste von IDs der Vorgänge
   * @param category_id Kategorie-ID
   * @param status Status
   * @param date_from Erstellt nach
   * @param date_to Erstellt vor
   * @param updated_from Aktualisiert nach
   * @param updated_to Aktualisiert vor
   * @param agency_responsible Auftrags-Team
   * @param negation Negiert
   * @param restriction_area Suchbereich
   * @param just_times Nur die Zeiten der letzten Änderung ausgeben (für Caching im CitySDK)
   * @param authCode Code zur Identifizierung des Clients
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @param typ Vorgangstyp
   * @param max_requests Maximale Anzahl von Vorgängen
   * @param with_foto Nur vorgänge mit freigegebenem Foto
   * @param also_archived Auch Archivierte Vorgände ausgeben
   * @param just_count Nur die Anzahl der Vorgänge zurückgeben
   * @param area_code Stadtteilgrenze
   * @param geoRssHash GeoRSS-Hash
   * @throws java.io.IOException
   */
  @RequestMapping(value = "/vorgaenge", method = RequestMethod.GET)
  @ResponseBody
  public void vorgaenge(
    @RequestParam(value = "id", required = false) Long id,
    @RequestParam(value = "ids", required = false) String ids,
    @RequestParam(value = "category_id", required = false) Long category_id,
    @RequestParam(value = "status", required = false) String status,
    @RequestParam(value = "date_from", required = false) String date_from,
    @RequestParam(value = "date_to", required = false) String date_to,
    @RequestParam(value = "updated_from", required = false) String updated_from,
    @RequestParam(value = "updated_to", required = false) String updated_to,
    @RequestParam(value = "agency_responsible", required = false) String agency_responsible,
    @RequestParam(value = "negation", required = false) String negation,
    @RequestParam(value = "restriction_area", required = false) String restriction_area,
    @RequestParam(value = "just_times", required = false) boolean just_times,
    @RequestParam(value = "authCode", required = false) String authCode,
    @RequestParam(value = "typ", required = false) String typ,
    @RequestParam(value = "max_requests", required = false) Integer max_requests,
    @RequestParam(value = "geoRssHash", required = false) String geoRssHash,
    @RequestParam(value = "with_foto", required = false) boolean with_foto,
    @RequestParam(value = "also_archived", required = false) boolean also_archived,
    @RequestParam(value = "just_count", required = false) boolean just_count,
    @RequestParam(value = "area_code", required = false) Integer area_code,
    HttpServletResponse response) throws IOException {

    try {
      List<Vorgang> vorgaenge = new ArrayList<Vorgang>();
      List<HashMap> times = new ArrayList<HashMap>();

      VorgangSuchenCommand cmd = new VorgangSuchenCommand();
      // Suchtyp aussendienst würde nur Vorgänge mit zustaendigkeit_status = 'akzeptiert' ausgeben
      cmd.setSuchtyp(VorgangSuchenCommand.Suchtyp.erweitert);
      if (authCode != null && authCode.equals(settingsService.getPropertyValue("auth.kod_code"))) {
        cmd.setShowTips(true);
      } else {
        cmd.setShowTips(false);
      }

      if (!also_archived) {
        cmd.setErweitertArchiviert(false);
      }
      // Sortieren nach ID
      cmd.setOrder(0);
      cmd.setOrderDirection(0);
      cmd.setUeberspringeVorgaengeMitMissbrauchsmeldungen(true);
      cmd.setJustTimes(just_times);

      if (id != null) {
        cmd.setErweitertNummer(id.toString());
      }

      if (negation != null) {
        cmd.setNegation(negation);
      }

      if (restriction_area != null) {
        cmd.setSuchbereich(restriction_area);
      }

      if (ids != null && ids.length() > 0) {
        String[] idStrList = ids.split(",");
        Long[] data = new Long[idStrList.length];
        for (int i = 0; i < idStrList.length; i++) {
          data[i] = Long.valueOf(idStrList[i]);
        }
        cmd.setVorgangAuswaehlen(data);
      }

      if (category_id != null) {
        Kategorie kat = kategorieDao.findKategorie(category_id);
        if (kat != null) {
          if (kat.getParent() == null) {
            cmd.setErweitertHauptkategorie(kat);
          } else {
            cmd.setErweitertKategorie(kat);
          }
        }
      }

      if (status != null) {
        String[] status_list = status.split(",");
        EnumVorgangStatus[] evs = new EnumVorgangStatus[status_list.length];

        for (int i = 0; i < status_list.length; i++) {
          evs[i] = status_list[i].isEmpty() ? null : EnumVorgangStatus.valueOf(status_list[i]);
        }
        cmd.setErweitertVorgangStatus(evs);
      }

      if (typ != null) {
        String[] typen_list = typ.split(",");
        EnumVorgangTyp[] evt = new EnumVorgangTyp[typen_list.length];
        for (int i = 0; i < typen_list.length; i++) {
          evt[i] = typen_list[i].isEmpty() ? null : EnumVorgangTyp.valueOf(typen_list[i]);
        }
        cmd.setErweitertVorgangTypen(evt);
      }

      if (date_from != null) {
        cmd.setErweitertDatumVon(getDateFromParam(date_from));
      }
      if (date_to != null) {
        cmd.setErweitertDatumBis(getDateFromParam(date_to));
      }
      if (updated_from != null) {
        cmd.setAktualisiertVon(getDateFromParam(updated_from));
      }
      if (updated_to != null) {
        cmd.setAktualisiertBis(getDateFromParam(updated_to));
      }

      if (geoRssHash != null) {
        GeoRss geoRss = geoRssDao.findGeoRss(geoRssHash);
        String hauptKategorien = "";
        if (geoRss.getIdeenHauptkategorien() != null) {
          hauptKategorien += geoRss.getIdeenHauptkategorien();
        }
        if (geoRss.getProblemeHauptkategorien() != null) {
          hauptKategorien += geoRss.getProblemeHauptkategorien();
        }
        cmd.setErweitertHauptKategorieIds(hauptKategorien);
        String unterKategorien = "";
        if (geoRss.getIdeenUnterkategorien() != null) {
          unterKategorien += geoRss.getIdeenUnterkategorien();
        }
        if (geoRss.getProblemeUnterkategorien() != null) {
          unterKategorien += geoRss.getProblemeUnterkategorien();
        }
        cmd.setErweitertUnterKategorieIds(unterKategorien);
        cmd.setObservation(geoRss.getOviWkt());
      }

      if (area_code != null) {
        cmd.setErweitertStadtteilgrenze(area_code);
      }

      if (max_requests != null) {
        cmd.setOrder(9);
        cmd.setSize(max_requests);
      } else {
        cmd.setOrder(0);
      }

      cmd.setOrderDirection(1);

      if (with_foto) {
        cmd.setFotoFreigabeStatus(EnumFreigabeStatus.extern);
      }

      if (agency_responsible != null) {
        cmd.setAuftragTeam(agency_responsible);
        cmd.setAuftragDatum(new Date());
        cmd.setOrder(8);
        cmd.setOrderDirection(0);
      }

      if (just_count) {
        HashMap hm = new HashMap<String, String>();
        hm.put("count", vorgangDao.getVorgaengeIdAndVersion(cmd).size());
        times.add(hm);
        sendOk(response, mapper.writeValueAsString(times));
        return;
      }

      if (just_times) {
        List<Object[]> vg = vorgangDao.getVorgaengeIdAndVersion(cmd);
        for (Object[] entry : vg) {
          HashMap hm = new HashMap<String, String>();
          hm.put("id", entry[0]);
          hm.put("version", entry[1]);
          times.add(hm);
        }
      } else {
        List<Object[]> vg = vorgangDao.getVorgaenge(cmd);
        for (Object[] entry : vg) {
          Vorgang vorgang = (Vorgang) entry[0];
          vorgang.setUnterstuetzerCount((Integer) entry[2]);
          vorgang.setSecurityService(securityService);
          vorgaenge.add(vorgang);
        }
      }

      if (just_times) {
        sendOk(response, mapper.writeValueAsString(times));
      } else {
        sendOk(response, mapper.writeValueAsString(vorgaenge));
      }
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/grenzen</code><br>
   *
   * @param ids Liste von IDs der Stadtteile
   * @param with_districts Sollen die Stadtteil-Grenzen mit ausgegeben werden
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @throws java.io.IOException
   */
  @RequestMapping(value = "/grenzen", method = RequestMethod.GET)
  @ResponseBody
  public void grenzen(
    @RequestParam(value = "ids", required = false) String ids,
    @RequestParam(value = "with_districts", required = false) boolean with_districts,
    HttpServletResponse response
  ) throws IOException {
    try {
      List<HashMap> grenzenHash = new ArrayList<HashMap>();
      if (ids != null) {
        String[] idStrList = ids.split(", ");
        Integer[] data = new Integer[idStrList.length];
        for (int i = 0; i < idStrList.length; i++) {
          data[i] = Integer.valueOf(idStrList[i]);
        }
        for (int i = 0; i < data.length; i++) {
          StadtteilGrenze grenze = grenzenDao.findStadtteilGrenze(data[i]);
          HashMap hm = new HashMap<String, Object>();
          hm.put("grenze", grenze.getGrenzeWkt());
          grenzenHash.add(hm);
        }
      } else {
        if (with_districts) {
          List<StadtteilGrenze> grenzen = grenzenDao.findStadtteilGrenzenWithGrenze();
          for (StadtteilGrenze entry : grenzen) {
            HashMap hm = new HashMap<String, Object>();
            hm.put("id", entry.getId());
            hm.put("name", entry.getName());
            hm.put("grenze", entry.getGrenzeWkt());
            grenzenHash.add(hm);
          }
        } else {
          StadtGrenze grenze = grenzenDao.getStadtgrenze();
          HashMap hm = new HashMap<String, String>();
          hm.put("grenze", grenze.getGrenzeWkt());
          grenzenHash.add(hm);
        }
      }
      sendOk(response, mapper.writeValueAsString(grenzenHash));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den POST-Request auf der URL <code>/service/foto</code><br>
   * Beschreibung: erstellt ein neues Foto für einen Vorgang
   *
   * @param vorgang Vorgang
   * @param bild Foto des Vorgangs
   * @param email E-Mail-Adresse des Erstellers
   * @param resultHashOnSubmit <code>true</code> - gibt den Hash zum Bestätigen als Ergebnis zurück
   * @param resultObjectOnSubmit <code>true</code> - gibt den neuen Vorgangs als Ergebnis zurück
   * @param response Response in das das Ergebnis direkt geschrieben wird
   */
  @RequestMapping(value = "/foto", method = RequestMethod.POST)
  @ResponseBody
  public void foto(
    @RequestParam(value = "vorgang", required = false) Long vorgang,
    @RequestParam(value = "email", required = false) String email,
    @RequestParam(value = "bild", required = false) String bild,
    @RequestParam(value = "resultHashOnSubmit", required = false) Boolean resultHashOnSubmit,
    @RequestParam(value = "resultObjectOnSubmit", required = false) Boolean resultObjectOnSubmit,
    HttpServletResponse response) {

    if (resultHashOnSubmit == null) {
      resultHashOnSubmit = false;
    }
    if (resultObjectOnSubmit == null) {
      resultObjectOnSubmit = false;
    }
    try {
      Foto foto = new Foto();
      if (vorgang == null) {
        throw new BackendControllerException(401, "[vorgang] fehlt", "Das Foto ist keiner Meldung zugeordnet.");
      }
      Vorgang vorg = vorgangDao.findVorgang(vorgang);
      if (vorg == null) {
        throw new BackendControllerException(200, "[vorgang] ungültig", "Es konnte kein Vorgang mit der übergebenen ID gefunden werden.");
      }
      foto.setVorgang(vorg);
      if (foto.getVorgang() == null) {
        throw new BackendControllerException(402, "[vorgang] nicht korrekt", "Das Foto ist keiner Meldung zugeordnet.");
      }
      if (StringUtils.isBlank(bild)) {
        throw new BackendControllerException(403, "[bild] fehlt", "Es wurde kein Foto hochgeladen.");
      }
      if (StringUtils.isBlank(email)) {
        throw new BackendControllerException(404, "[email] fehlt", "Die E-Mail-Adresse fehlt.");
      }
      if (!isShortEnough(email, 300)) {
        throw new BackendControllerException(405, "[email] zu lang", "Die angegebene E-Mail-Adresse ist zu lang.");
      }
      if (!isEmail(email)) {
        throw new BackendControllerException(406, "[email] nicht korrekt", "Die angegebene E-Mail-Adresse ist nicht gültig.");
      }
      if (isTrashMail(email)) {
        throw new BackendControllerException(10, "[autorEmail] nicht erlaubt", "Die Domain der angegebenen E-Mail-Adresse ist nicht zulässig.");
      }

      foto.setAutorEmail(email);
      foto.setHash(securityService.createHash(foto.getVorgang().getId() + email + System.currentTimeMillis()));

      foto.setDatum(new Date());

      vorgangDao.persist(foto);
      try {
        imageService.setImageForFoto(Base64.decode(bild.getBytes()), foto);
      } catch (Exception e) {
        e.printStackTrace();
        throw new BackendControllerException(11, "[bild] nicht korrekt", "Das Bild ist fehlerhaft und kann nicht verarbeitet werden.", e);
      }
      vorgangDao.merge(foto);

      foto.getVorgang().setAdresse(foto.getVorgang().getAdresse());

      vorgangDao.merge(foto, false);

      mailService.sendFotoBestaetigungMail(foto, email, vorgang);

      if (resultHashOnSubmit) {
        sendOk(response, foto.getHash());
      } else if (resultObjectOnSubmit) {
        sendOk(response, mapper.writeValueAsString(foto));
      } else {
        sendOk(response);
      }
    } catch (Exception e) {
      logger.warn(e);
      sendError(response, e);
    }
  }

  /**
   * Die Methode verarbeitet den POST-Request auf der URL <code>/service/fotoBestaetigung</code><br>
   * Beschreibung: Vorgang bestätigen
   *
   * @param hash Hash zum Bestätigen
   * @return View die angezeigt werden soll
   */
  @RequestMapping(value = "/fotoBestaetigung")
  public String fotoBestaetigung(@RequestParam(value = "hash", required = false) String hash, ModelMap model) {

    try {
      if (StringUtils.isBlank(hash)) {
        throw new BackendControllerException(501, "[hash] fehlt");
      }
      Foto foto = vorgangDao.findFoto(hash);
      if (foto == null) {
        throw new BackendControllerException(502, "[hash] nicht korrekt");
      }

      Vorgang vorgang = foto.getVorgang();
      if (foto.getDatumBestaetigung() != null) {
        model.put("alreadyAccepted", true);
      } else {
        foto.setDatumBestaetigung(new Date());

        verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.fotoBestaetigung, null, null);
        vorgangDao.merge(foto);

        vorgang.setFotoGross(foto.getFotoGross());
        vorgang.setFotoNormal(foto.getFotoNormal());
        vorgang.setFotoThumb(foto.getFotoThumb());
        vorgang.setFotoFreigabeStatus(EnumFreigabeStatus.intern);
        vorgang.setFotowunsch(false);
        vorgangDao.merge(vorgang);

        foto.getVorgang().setAdresse(foto.getVorgang().getAdresse());

        vorgangDao.merge(foto, false);
      }

      model.put("message", "Das Foto wurde erfolgreich aufgenommen.");
      model.put("vorgangId", String.valueOf(vorgang.getId()));

      String link = settingsService.getPropertyValue("geo.map.extern.extern.url");
      link = link.replaceAll("%id%", String.valueOf(vorgang.getId()));
      model.put("link", link);

      return "backend/bestaetigungOk";

    } catch (Exception e) {
      logger.warn(e);
      return "backend/bestaetigungFehler";
    }
  }

  /**
   * Sendet eine Fehlermeldung
   */
  private void sendError(HttpServletResponse response, Exception exception) {

    try {
      response.setCharacterEncoding("utf-8");
      response.setHeader("Content-Type", "text/plain;charset=UTF-8");
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getOutputStream().write(exception.getMessage().getBytes());
      //response.getWriter().print(exception.getMessage());
      response.flushBuffer();
    } catch (Exception e) {
      logger.error(e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Sendet ein Ok
   */
  private void sendOk(HttpServletResponse response) {
    try {
      response.setCharacterEncoding("utf-8");
      response.setHeader("Content-Type", "text/plain;charset=UTF-8");
      response.setStatus(HttpServletResponse.SC_OK);
      response.flushBuffer();
    } catch (Exception e) {
      logger.error(e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Sendet ein Ok incl. Content
   */
  private void sendOk(HttpServletResponse response, String content) {

    try {
      response.setCharacterEncoding("utf-8");
      response.setHeader("Content-Type", "text/plain;charset=UTF-8");
      response.setStatus(HttpServletResponse.SC_OK);
      response.getWriter().append(content);
      response.flushBuffer();
    } catch (Exception e) {
      logger.error(e);
      throw new RuntimeException(e);
    }
  }

  private Date getDateFromParam(String param) {
    Date date = new Date();
    date.setTime(Long.parseLong(param));
    return date;
  }
}
