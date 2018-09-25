package de.fraunhofer.igd.klarschiff.web;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.security.User;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.util.SecurityUtil;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

/**
 * Command für den RSS-Feed im Backend <br>
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@RequestMapping("/xmlfeeds")
@Controller
public class VorgangFeedController {

  @Autowired
  VorgangDao vorgangDao;

  @Autowired
  SecurityService securityService;

  @Autowired
  SettingsService settingsService;

  DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/feed/{user}</code><br>
   * Seitenbeschreibung: XML-RSS-Feed von Vorgängen
   *
   * @param loginCrypt Encrypted Login
   * @param version Version
   * @param request HttpServletRequest-Objekt
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @throws java.lang.Exception
   */
  @RequestMapping(value = "/feed/{user}", method = RequestMethod.GET)
  @ResponseBody
  public void xmlfeed(
    @PathVariable("user") String loginCrypt,
    @RequestParam(value = "version", defaultValue = "2.0") String version,
    HttpServletRequest request,
    HttpServletResponse response) throws Exception {
    String login = SecurityUtil.simpleDecrypt(loginCrypt);

    User user = securityService.getUser(login);

    //Command für die Suche zusammenstellen
    VorgangFeedCommand cmd = new VorgangFeedCommand();
    cmd.setSize(50);
    cmd.setOrder(2);
    cmd.setOrderDirection(1);
    cmd.setSuchtyp(VorgangSuchenCommand.Suchtyp.einfach);
    cmd.setEinfacheSuche(VorgangSuchenCommand.EinfacheSuche.offene);
    cmd.setZustaendigkeiten(user.getZustaendigkeiten());

    List<Object[]> list = vorgangDao.getVorgaenge(cmd);

    Element rss = new Element("rss");
    rss.setAttribute("version", "2.0");
    Namespace atomNamespace = Namespace.getNamespace("atom", "http://www.w3.org/2005/Atom");
    rss.addNamespaceDeclaration(atomNamespace);
    Namespace georssNamespace = Namespace.getNamespace("georss", "http://www.georss.org/georss");
    rss.addNamespaceDeclaration(georssNamespace);
    Element channel = new Element("channel");
    rss.addContent(channel);

    Element elem;

    //Title
    elem = new Element("title");
    elem.addContent(settingsService.getContextAppTitle() + "-Vorgänge");
    channel.addContent(elem);

    //Atom:Link
    elem = new Element("link", atomNamespace);
    elem.setAttribute("href", settingsService.getPropertyValue("mail.server.baseurl.backend") + "xmlfeeds/feed/" + loginCrypt);
    elem.setAttribute("rel", "self");
    elem.setAttribute("type", "application/rss+xml");
    channel.addContent(elem);

    //Link
    elem = new Element("link");
    elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend"));
    channel.addContent(elem);

    //Description
    elem = new Element("description");
    if (settingsService.getContextAppDemo()) {
      elem.addContent("Diese Daten umfassen Ihre 50 aktuellsten zu bearbeitenden Vorgänge in " + settingsService.getContextAppTitle() + ", dem Portal zur Bürgerbeteiligung.");
    } else {
      elem.addContent("Diese Daten umfassen Ihre 50 aktuellsten zu bearbeitenden Vorgänge in " + settingsService.getContextAppTitle() + ", dem Portal zur Bürgerbeteiligung der " + settingsService.getContextAppArea() + ".");
    }
    channel.addContent(elem);

    //Language
    elem = new Element("language");
    elem.addContent("de-de");
    channel.addContent(elem);

    //Copyright
    elem = new Element("copyright");
    elem.addContent("Hanse- und Universitätsstadt Rostock");
    channel.addContent(elem);

    //Image
    Element image = new Element("image");
    channel.addContent(image);
    elem = new Element("url");
    elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend") + "images/rss.png");
    image.addContent(elem);
    elem = new Element("title");
    elem.addContent(settingsService.getContextAppTitle() + "-Vorgänge");
    image.addContent(elem);
    elem = new Element("link");
    elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend"));
    image.addContent(elem);

    for (Object[] entry : list) {
      Vorgang vorgang = (Vorgang) entry[0];
      Date aenderungsdatum = (Date) entry[1];
      int unterstuetzer = (Integer) entry[2];
      long missbrauchsmeldungen = (Long) entry[3];
      Element item = new Element("item");
      channel.addContent(item);

      elem = new Element("title");
      elem.addContent("#" + vorgang.getId() + " " + vorgang.getTyp().getText() + " (" + vorgang.getKategorie().getParent().getName() + " – " + vorgang.getKategorie().getName() + ")");
      item.addContent(elem);

      StringBuilder str = new StringBuilder();
      str.append("<b>Status:</b> ");
      str.append(vorgang.getStatus().getText());
      str.append("<br/>");
      str.append("<b>Statusinformation:</b> ");
      if (vorgang.getStatusKommentar() != null && vorgang.getStatusKommentar() != "") {
        str.append(vorgang.getStatusKommentar());
      } else {
        str.append("nicht vorhanden");
      }
      str.append("<br/>");
      str.append("<b>Adresse: </b>");
      str.append(vorgang.getAdresse());
      str.append("<br/>");
      str.append("<b>Flurstückseigentum: </b>");
      str.append(vorgang.getFlurstueckseigentum());
      str.append("<br/>");
      str.append("<b>Unterstützungen:</b> ");
      if (unterstuetzer > 0) {
        str.append(unterstuetzer);
      } else {
        str.append("bisher keine");
      }
      str.append("<br/>");
      str.append("<b>Missbrauchsmeldungen:</b> ");
      if (missbrauchsmeldungen > 0) {
        str.append(missbrauchsmeldungen);
      } else {
        str.append("bisher keine");
      }
      str.append("<br/>");
      str.append("<b>Zuständigkeit: </b>");
      str.append(securityService.getZustaendigkeit(vorgang.getZustaendigkeit()).getDescription());
      if (vorgang.getZustaendigkeitStatus() != null) {
        str.append(" (").append(vorgang.getZustaendigkeitStatus().getText()).append(")");
      }
      if (StringUtils.isNotBlank(vorgang.getDelegiertAn())) {
        str.append(", delegiert an: ");
        str.append(securityService.getZustaendigkeit(vorgang.getDelegiertAn()).getDescription());
      }
      str.append("<br/>");
      str.append("<b>Beschreibung:</b> ");
      if (vorgang.getBeschreibung() != null && vorgang.getBeschreibung() != "") {
        str.append(vorgang.getBeschreibung());
      } else {
        str.append("nicht vorhanden");
      }
      str.append("<br/>");
      str.append("<b>Foto:</b><br/>");
      if (vorgang.getFotoExists()) {
        str.append("<a href=\"" + settingsService.getPropertyValue("image.url") + vorgang.getFotoNormal() + "\" target=\"_blank\" title=\"große Ansicht öffnen…\"><img src=\"" + settingsService.getPropertyValue("image.url") + vorgang.getFotoThumb() + "\" alt=\"" + settingsService.getPropertyValue("image.url") + vorgang.getFotoThumb() + "\" /></a>");
      } else {
        str.append("nicht vorhanden");
      }
      str.append("<br/>");
      str.append("<a href=\"" + settingsService.getPropertyValue("mail.server.baseurl.backend") + "vorgang/" + vorgang.getId() + "/uebersicht" + "\" target=\"_blank\">Vorgang in " + settingsService.getContextAppTitle() + " öffnen</a>");

      elem = new Element("description");
      elem.addContent(new CDATA(str.toString()));
      item.addContent(elem);

      elem = new Element("link");
      elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend") + "vorgang/" + vorgang.getId() + "/uebersicht");
      item.addContent(elem);

      elem = new Element("guid");
      elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend") + "vorgang/" + vorgang.getId() + "/uebersicht");
      item.addContent(elem);

      elem = new Element("pubDate");
      elem.addContent(dateFormat.format(aenderungsdatum));
      item.addContent(elem);

      CoordinateReferenceSystem utm = CRS.decode("EPSG:25833");
      CoordinateReferenceSystem geographic = CRS.decode("EPSG:4326");
      MathTransform transformation = CRS.findMathTransform(utm, geographic);
      Point point = (Point) JTS.transform(vorgang.getOvi(), transformation);
      Coordinate coor = new Coordinate(point.getY(), point.getX());
      point.getCoordinate().setCoordinate(coor);

      elem = new Element("point", georssNamespace);
      elem.addContent(point.getY() + " " + point.getX());
      item.addContent(elem);

    }

    Document doc = new Document();
    doc.setRootElement(rss);

    response.setHeader("Content-Type", "application/rss+xml");
    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    outputter.output(doc, response.getWriter());
    response.setStatus(HttpServletResponse.SC_OK);
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/feedDelegiert/{user}</code><br>
   * Seitenbeschreibung: XML-RSS-Feed von Deligierten Vorgängen
   *
   * @param loginCrypt Encrypted Login
   * @param version Version
   * @param request HttpServletRequest-Objekt
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @throws java.lang.Exception
   */
  @RequestMapping(value = "/feedDelegiert/{user}", method = RequestMethod.GET)
  public void xmlfeedDelegiert(
    @PathVariable("user") String loginCrypt,
    @RequestParam(value = "version", defaultValue = "2.0") String version,
    HttpServletRequest request,
    HttpServletResponse response) throws Exception {
    String login = SecurityUtil.simpleDecrypt(loginCrypt);
    User user = securityService.getUser(login);

    //Command für die Suche zusammenstellen
    VorgangFeedDelegiertAnCommand cmd = new VorgangFeedDelegiertAnCommand();
    cmd.setSize(50);
    cmd.setOrder(2);
    cmd.setOrderDirection(1);
    cmd.setEinfacheSuche(VorgangDelegiertSuchenCommand.EinfacheSuche.offene);
    cmd.setDelegiertAn(user.getDelegiertAn());

    List<Vorgang> list = vorgangDao.listVorgang(cmd);

    Element rss = new Element("rss");
    rss.setAttribute("version", "2.0");
    Namespace atomNamespace = Namespace.getNamespace("atom", "http://www.w3.org/2005/Atom");
    rss.addNamespaceDeclaration(atomNamespace);
    Namespace georssNamespace = Namespace.getNamespace("georss", "http://www.georss.org/georss");
    rss.addNamespaceDeclaration(georssNamespace);
    Element channel = new Element("channel");
    rss.addContent(channel);

    Element elem;

    //Title
    elem = new Element("title");
    elem.addContent(settingsService.getContextAppTitle() + "-Vorgänge");
    channel.addContent(elem);

    //Atom:Link
    elem = new Element("link", atomNamespace);
    elem.setAttribute("href", settingsService.getPropertyValue("mail.server.baseurl.backend") + "xmlfeeds/feed/" + loginCrypt);
    elem.setAttribute("rel", "self");
    elem.setAttribute("type", "application/rss+xml");
    channel.addContent(elem);

    //Link
    elem = new Element("link");
    elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend"));
    channel.addContent(elem);

    //Description
    elem = new Element("description");
    if (settingsService.getContextAppDemo()) {
      elem.addContent("Diese Daten umfassen Ihre 50 aktuellsten zu bearbeitenden Vorgänge in " + settingsService.getContextAppTitle() + ", dem Portal zur Bürgerbeteiligung.");
    } else {
      elem.addContent("Diese Daten umfassen Ihre 50 aktuellsten zu bearbeitenden Vorgänge in " + settingsService.getContextAppTitle() + ", dem Portal zur Bürgerbeteiligung der " + settingsService.getContextAppArea() + ".");
    }
    channel.addContent(elem);

    //Language
    elem = new Element("language");
    elem.addContent("de-de");
    channel.addContent(elem);

    //Copyright
    elem = new Element("copyright");
    elem.addContent("Hanse- und Universitätsstadt Rostock");
    channel.addContent(elem);

    //Image
    Element image = new Element("image");
    channel.addContent(image);
    elem = new Element("url");
    elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend") + "images/rss.png");
    image.addContent(elem);
    elem = new Element("title");
    elem.addContent(settingsService.getContextAppTitle() + "-Vorgänge");
    image.addContent(elem);
    elem = new Element("link");
    elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend"));
    image.addContent(elem);

    for (Vorgang vorgang : list) {
      Element item = new Element("item");
      channel.addContent(item);

      elem = new Element("title");
      elem.addContent("#" + vorgang.getId() + " " + vorgang.getTyp().getText() + " (" + vorgang.getKategorie().getParent().getName() + " – " + vorgang.getKategorie().getName() + ")");
      item.addContent(elem);

      StringBuilder str = new StringBuilder();
      str.append("<b>Status:</b> ");
      str.append(vorgang.getStatus().getText());
      str.append("<br/>");
      str.append("<b>Statusinformation:</b> ");
      if (vorgang.getStatusKommentar() != null && vorgang.getStatusKommentar() != "") {
        str.append(vorgang.getStatusKommentar());
      } else {
        str.append("nicht vorhanden");
      }
      str.append("<br/>");
      str.append("<b>Adresse: </b>");
      str.append(vorgang.getAdresse());
      str.append("<br/>");
      str.append("<b>Flurstückseigentum: </b>");
      str.append(vorgang.getFlurstueckseigentum());
      str.append("<br/>");
      str.append("<b>Unterstützungen:</b> ");
      if (vorgang.getUnterstuetzerCount() != null && vorgang.getUnterstuetzerCount() > 0) {
        str.append(vorgang.getUnterstuetzerCount());
      } else {
        str.append("bisher keine");
      }
      str.append("<br/>");
      str.append("<b>Zuständigkeit: </b>");
      str.append(securityService.getZustaendigkeit(vorgang.getZustaendigkeit()).getDescription());
      str.append(" (");
      str.append(vorgang.getZustaendigkeitStatus().getText());
      str.append(")");
      if (StringUtils.isNotBlank(vorgang.getDelegiertAn())) {
        str.append(", delegiert an: ");
        str.append(securityService.getZustaendigkeit(vorgang.getDelegiertAn()).getDescription());
      }
      str.append("<br/>");
      str.append("<b>Beschreibung:</b> ");
      if (vorgang.getBeschreibung() != null && vorgang.getBeschreibung() != "") {
        str.append(vorgang.getBeschreibung());
      } else {
        str.append("nicht vorhanden");
      }
      str.append("<br/>");
      str.append("<b>Foto:</b><br/>");
      if (vorgang.getFotoExists()) {
        str.append("<a href=\"" + settingsService.getPropertyValue("image.url") + vorgang.getFotoNormal() + "\" target=\"_blank\" title=\"große Ansicht öffnen…\"><img src=\"" + settingsService.getPropertyValue("image.url") + vorgang.getFotoThumb() + "\" alt=\"" + settingsService.getPropertyValue("image.url") + vorgang.getFotoThumb() + "\" /></a>");
      } else {
        str.append("nicht vorhanden");
      }
      str.append("<br/>");
      str.append("<a href=\"" + settingsService.getPropertyValue("mail.server.baseurl.backend") + "vorgang/delegiert/" + vorgang.getId() + "/uebersicht" + "\" target=\"_blank\">Vorgang in " + settingsService.getContextAppTitle() + " öffnen</a>");

      elem = new Element("description");
      elem.addContent(new CDATA(str.toString()));
      item.addContent(elem);

      elem = new Element("link");
      elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend") + "vorgang/delegiert/" + vorgang.getId() + "/uebersicht");
      item.addContent(elem);

      elem = new Element("guid");
      elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend") + "vorgang/delegiert/" + vorgang.getId() + "/uebersicht");
      item.addContent(elem);

      elem = new Element("pubDate");
      elem.addContent(dateFormat.format(vorgang.getDatum()));
      item.addContent(elem);

      CoordinateReferenceSystem utm = CRS.decode("EPSG:25833");
      CoordinateReferenceSystem geographic = CRS.decode("EPSG:4326");
      MathTransform transformation = CRS.findMathTransform(utm, geographic);
      Point point = (Point) JTS.transform(vorgang.getOvi(), transformation);
      Coordinate coor = new Coordinate(point.getY(), point.getX());
      point.getCoordinate().setCoordinate(coor);

      elem = new Element("point", georssNamespace);
      elem.addContent(point.getY() + " " + point.getX());
      item.addContent(elem);

    }

    Document doc = new Document();
    doc.setRootElement(rss);

    response.setHeader("Content-Type", "application/rss+xml");
    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    outputter.output(doc, response.getWriter());
    response.setStatus(HttpServletResponse.SC_OK);
  }
}
