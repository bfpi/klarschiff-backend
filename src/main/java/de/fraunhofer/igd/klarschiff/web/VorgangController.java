package de.fraunhofer.igd.klarschiff.web;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import de.fraunhofer.igd.klarschiff.dao.KommentarDao;
import de.fraunhofer.igd.klarschiff.dao.VerlaufDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.classification.FeatureService;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.image.ImageService;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.EnumFreigabeStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.Verlauf;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * Controller für die Vorgangsübersicht und Detailansichten
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Controller
public class VorgangController {

  @Autowired
  VorgangDao vorgangDao;

  @Autowired
  VerlaufDao verlaufDao;

  @Autowired
  KommentarDao kommentarDao;

  @Autowired
  SecurityService securityService;

  @Autowired
  GeoService geoService;

  @Autowired
  ImageService imageService;

  @Autowired
  FeatureService featureService;

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/{id}/uebersicht</code><br>
   * Seitenbeschreibung: Übersichtsseite für den aktuellen Vorgang
   *
   * @param id Vorgangs-ID
   * @param page Seitenzahl
   * @param size Seitengröße
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/vorgang/{id}/uebersicht", method = RequestMethod.GET)
  public String uebersicht(
    @PathVariable("id") Long id,
    @RequestParam(value = "page", defaultValue = "1") Integer page,
    @RequestParam(value = "size", defaultValue = "5") Integer size,
    ModelMap model,
    HttpServletRequest request) {
    Vorgang vorgang = vorgangDao.findVorgang(id);

    if (!vorgang.getErstsichtungErfolgt()) {
      return "redirect:/vorgang/" + id + "/erstsichtung";
    }

    model.put("vorgang", vorgang);
    model.put("vorgangZustaendigkeit", securityService.isCurrentZustaendigkeiten(vorgang.getId()));
    model.put("geoService", geoService);
    model.put("unterstuetzer", vorgangDao.countUnterstuetzerByVorgang(vorgang));
    model.put("missbrauch", vorgangDao.countOpenMissbrauchsmeldungByVorgang(vorgang));
    model.put("page", page);
    model.put("size", size);
    model.put("maxPages", calculateMaxPages(size, kommentarDao.countKommentare(vorgang)));
    model.put("kommentare", kommentarDao.findKommentareForVorgang(vorgang, page, size));
    return "vorgang/uebersicht";
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/{id}/karte</code><br>
   * Seitenbeschreibung: Kartenansicht für den aktuellen Vorgang
   *
   * @param id Vorgangs-ID
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/vorgang/{id}/karte", method = RequestMethod.GET)
  public String karte(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
    Vorgang vorgang = vorgangDao.findVorgang(id);
    model.put("vorgang", vorgang);
    model.put("geoService", geoService);
    model.put("mapExternName", geoService.getMapExternName());
    model.put("mapExternUrl", geoService.getMapExternUrl(vorgang));
    model.put("vorgangCoordinates", geoService.getVorgangCoordinates(vorgang));
    return "vorgang/karte";
  }

  /**
   *
   * @param id Vorgangs-ID
   * @param oviWkt Position als WKT
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   * @throws java.lang.Exception
   */
  @RequestMapping(value = "/vorgang/{id}/karte", method = RequestMethod.POST)
  public String karteSubmit(
    @PathVariable("id") Long id,
    @RequestParam(value = "oviWkt", defaultValue = "") String oviWkt,
    ModelMap model,
    HttpServletRequest request) throws Exception {
    Vorgang vorgang = vorgangDao.findVorgang(id);

    if (StringUtils.isNotEmpty(oviWkt)) {
      String altesFlurstueckseigentum = vorgang.getFlurstueckseigentum();
      String alterOviWkt = vorgang.getOviWkt();
      vorgang.setOviWkt(oviWkt);
      String alteAdresse = vorgang.getAdresse();
      String neueAdresse = geoService.calculateAddress(vorgang.getOvi());
      vorgang.setAdresse(neueAdresse);
      vorgangDao.merge(vorgang);
      String neuerOviWkt = vorgang.getOviWkt();
      String neuesFlurstueckseigentum = vorgang.getFlurstueckseigentum();
      // Verlauf: Ovi
      if (!StringUtils.equals(alterOviWkt, neuerOviWkt)) {
        verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.ovi, null, null);
      }
      // Verlauf: Adresse
      if (!StringUtils.equals(alteAdresse, neueAdresse)) {
        verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.adresse, alteAdresse, neueAdresse);
      }
      // Verlauf: Flurstückseigentum
      if (!StringUtils.equals(altesFlurstueckseigentum, neuesFlurstueckseigentum)) {
        verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.adresse, altesFlurstueckseigentum, neuesFlurstueckseigentum);
      }
      featureService.removeNonUpdatableFeatures(vorgang);
    }

    model.put("vorgang", vorgang);
    model.put("geoService", geoService);
    model.put("mapExternName", geoService.getMapExternName());
    model.put("mapExternUrl", geoService.getMapExternUrl(vorgang));
    model.put("vorgangCoordinates", geoService.getVorgangCoordinates(vorgang));
    return "vorgang/karte";
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/{id}/foto</code><br>
   * Seitenbeschreibung: Fotoansicht für den aktuellen Vorgang
   *
   * @param id Vorgangs-ID
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/vorgang/{id}/foto", method = RequestMethod.GET)
  public String foto(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
    Vorgang vorgang = vorgangDao.findVorgang(id);
    model.put("vorgang", vorgang);
    return "vorgang/foto";
  }

  /**
   * Die Methode verarbeitet den POST-Request auf der URL <code>/vorgang/{id}/foto</code><br>
   * Funktionsbeschreibung: Dient in Abhängigkeit vom <code>action</code> Parameter der Annahme der
   * Fotodrehung (<code>action=fotoRotate</code>), der Fotobearbeitung
   * (<code>action=fotoSave</code>), der Freigabestatusänderung
   * (<code>action.startsWith("freigabeStatus_Foto")</code> oder des Fotowunsches
   * (<code>action=fotowunsch</code>).
   *
   * @param id Vorgangs-ID
   * @param action Aktion der Foto-Bearbeitung
   * @param censorRectangleString Zensur-Rechteck
   * @param censoringWidth Zensur-Breite
   * @param censoringHeight Zensur-Höhe
   * @param foto Foto
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @param command Command
   * @return View, die zum Rendern des Request verwendet wird
   * @throws java.lang.Exception
   */
  @RequestMapping(value = "/vorgang/{id}/foto", method = RequestMethod.POST)
  public String foto(
    @PathVariable("id") Long id,
    @RequestParam(value = "action", required = true) String action,
    @RequestParam(value = "censorRectangles", required = false) String censorRectangleString,
    @RequestParam(value = "censoringWidth", required = false) Integer censoringWidth,
    @RequestParam(value = "censoringHeight", required = false) Integer censoringHeight,
    @RequestParam(value = "foto", required = false) CommonsMultipartFile foto,
    ModelMap model, HttpServletRequest request, Object command)
    throws Exception {
    Vorgang vorgang = vorgangDao.findVorgang(id);
    for (@SuppressWarnings("unused") Verlauf verlauf : vorgang.getVerlauf());

    if (action.equals("fotoSave")) {
      imageService.censorImageForVorgang(vorgang, censorRectangleString, censoringWidth, censoringHeight);
      vorgangDao.merge(vorgang);
    } else if (action.equals("fotoRotate")) {
      imageService.rotateImageForVorgang(vorgang);
      vorgangDao.merge(vorgang);
    } else if (action != null && action.startsWith("freigabeStatus_Foto")) {
      String str[] = action.split("_");
      EnumFreigabeStatus freigabeStatus = EnumFreigabeStatus.valueOf(str[2]);
      verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.fotoFreigabeStatus, vorgang.getFotoFreigabeStatus().getText(), freigabeStatus.getText());
      vorgang.setFotoFreigabeStatus(freigabeStatus);
      vorgangDao.merge(vorgang, false);
    } else if (action.equals("upload") && !foto.isEmpty()) {
      try {
        imageService.setImageForVorgang(foto.getBytes(), vorgang);
        verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.foto, null, null);
      } catch (Exception e) {
      }
    } else if (action.equals("fotowunsch")) {
      verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.fotowunsch, vorgang.getFotowunsch() ? "aktiv" : "inaktiv", vorgang.getFotowunsch() ? "inaktiv" : "aktiv");
      vorgang.setFotowunsch(!vorgang.getFotowunsch());
      vorgangDao.merge(vorgang);
    }
    model.put("vorgang", vorgang);
    return "vorgang/foto";
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/{id}/verlauf</code><br>
   * Seitenbeschreibung: Verlaufsansicht für den aktuellen Vorgang
   *
   * @param id Vorgangs-ID
   * @param page Seitenzahl
   * @param size Seitengröße
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/vorgang/{id}/verlauf", method = RequestMethod.GET)
  public String verlauf(@PathVariable("id") Long id, @RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "size", defaultValue = "10") Integer size, ModelMap model, HttpServletRequest request) {
    Vorgang vorgang = vorgangDao.findVorgang(id);
    model.put("vorgang", vorgang);
    model.put("page", page);
    model.put("size", size);
    model.addAttribute("verlauf", verlaufDao.findVerlaufForVorgang(vorgang, page, size));
    model.put("maxPages", calculateMaxPages(size, verlaufDao.countVerlauf(vorgang)));
    return "vorgang/verlauf";
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
