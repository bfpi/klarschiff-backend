package de.fraunhofer.igd.klarschiff.web;

import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import de.fraunhofer.igd.klarschiff.util.StreamUtil;

/**
 * Controller für Dokumentationen
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Controller
public class DokumentationController {

  @Autowired
  ServletContext servletContext;

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/dokumentation/index</code><br>
   * Beschreibung: Übersichts-Seite der Dokumentationen
   *
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/dokumentation/index", method = RequestMethod.GET)
  public String dokumentationIndex() {
    return "dokumentation/index";
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/dokumentation/api</code><br>
   * Beschreibung: API-Dokumentation
   *
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/dokumentation/api", method = RequestMethod.GET)
  public String dokumentationApi() {
    return "dokumentation/api";
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/dokumentation/{id}.htm</code><br>
   * Beschreibung: Anzeige der verschriedenen Handbücher
   *
   * @param id Name der Dokumentation
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @return View, die zum Rendern des Request verwendet wird
   * @throws java.lang.Exception
   */
  @RequestMapping(value = "/dokumentation/{id}.htm", method = RequestMethod.GET)
  public String dokumentation(@PathVariable("id") String id, Model model) throws Exception {
    String s = StreamUtil.readInputStreamToString(servletContext.getResourceAsStream("/dokumentation/" + id + ".html"), "UTF-8");
    s = StringUtils.substringAfter(s, "<body>");
    s = StringUtils.substringBefore(s, "<body>");
    s = s.replaceAll("href=\"Benutzerhandbuch", "href=\"Benutzerhandbuch.htm");
    s = s.replaceAll("href=\"Administrationshandbuch", "href=\"Administrationshandbuch.htm");
    s = s.replaceAll("href=\"Entwicklerdokumentation", "href=\"Entwicklerdokumentation.htm");

    model.addAttribute("content", s);
    model.addAttribute("externUrlId", id);
    return "dokumentation/" + id;
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/dokumentation/{id}.html</code><br>
   * Beschreibung: Anzeige der verschriedenen Handbücher
   *
   * @param id Name der Dokumentation
   * @param model Model in dem ggf. Daten für die View abgelegt werden
   * @param request Request
   * @param response Response in das das Ergebnis direkt geschrieben wird
   * @throws java.lang.Exception
   */
  @RequestMapping(value = "/dokumentation/{id}.html", method = RequestMethod.GET)
  @ResponseBody
  public void dokumentationHtml(
    @PathVariable("id") String id,
    Model model,
    HttpServletRequest request,
    HttpServletResponse response) throws Exception {
    String s = StreamUtil.readInputStreamToString(servletContext.getResourceAsStream("/dokumentation/" + id + ".html"), "UTF-8");
    s = s.replaceFirst("</head>", "<link rel=\"stylesheet\" type=\"text/css\" href=\"../styles/styles.css\"><title>Klarschiff Backend - " + id + "</title></head>");
    s = s.replaceFirst("<body>", "<body><center><div id=\"documentationContent\">");
    s = s.replaceFirst("</body>", "</div></center></body>");

    s = s.replaceAll("href=\"Benutzerhandbuch", "href=\"Benutzerhandbuch.html");
    s = s.replaceAll("href=\"Administrationshandbuch", "href=\"Administrationshandbuch.html");
    s = s.replaceAll("href=\"Entwicklerdokumentation", "href=\"Entwicklerdokumentation.html");

    response.setHeader("Content-Type", "text/html;charset=UTF-8");
    response.setStatus(HttpServletResponse.SC_OK);
    PrintWriter writer = response.getWriter();
    writer.write(s);
    response.flushBuffer();
  }

}
