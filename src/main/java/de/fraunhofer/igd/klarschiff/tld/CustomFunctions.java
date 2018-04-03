package de.fraunhofer.igd.klarschiff.tld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.commonsregex.RegexUtils;
import org.commonsregex.replacer.RegexReplacer;
import de.fraunhofer.igd.klarschiff.context.AppContext;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.cluster.ClusterUtil;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.security.User;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.util.SecurityUtil;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.Kommentar;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import de.fraunhofer.igd.klarschiff.web.FehlerController;

/**
 * Die Klasse stellt Funktionen für spezielle EL-funktionen für Klarschiff bereit.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class CustomFunctions {

  public static final Logger logger = Logger.getLogger(CustomFunctions.class);

  /**
   * Ermittelt ob die Rolle in der Collection enthalten ist.
   *
   * @param collection Collection mit Rollen
   * @param o Rolle, die gesucht werden soll
   * @return <code>true</code> - Rolle ist in der Collection enthalten
   */
  public static boolean roleContains(Collection<Role> collection, Role o) {
    return collection.contains(o);
  }

  /**
   * Ermittelt die Anzahl der Rollen in der Collection.
   *
   * @param collection Collection mit Rollen
   * @return Anzahl der Rollen in der Collection
   */
  public static Integer roleSize(Collection<Role> collection) {
    return collection.size();
  }

  /**
   * Entfernt eine Rolle aus einer Collection.
   *
   * @param collection Collection aus der eine Rolle entfernt werden soll
   * @param o Rolle, die entfernt werden soll
   * @return List mit rollen ohne die zu entfernenden Rolle
   */
  public static List<Role> roleMinus(Collection<Role> collection, Role o) {
    ArrayList<Role> c = new ArrayList<Role>();
    c.addAll(collection);
    c.remove(o);
    return c;
  }

  /**
   * Entfernt Rollen aus einer Collection
   *
   * @param collection Collection aus der die Rollen entfernt werden sollen
   * @param o Rollen, die aus der Collection entfernt werden sollen
   * @return Collection mit Rollen ohne die zu entfernenden Rollen
   */
  public static List<Role> roleMinus(Collection<Role> collection, Collection<Role> o) {
    ArrayList<Role> c = new ArrayList<Role>();
    c.addAll(collection);
    c.removeAll(o);
    return c;
  }

  /**
   * Ermittelt die Rolle aus dem LDAP anhand der Id
   *
   * @param id Id der Rolle
   * @return Rolle
   */
  public static Role role(String id) {
    return AppContext.getApplicationContext().getBean(SecurityService.class).getZustaendigkeit(id);
  }

  /**
   * Ermittelt die Bean mit dem gegebenen Namen aus dem ApplicationContext
   *
   * @param name Name der Bean im ApplicationContext
   * @return Bean aus dem ApplicationContext
   */
  public static Object bean(String name) {
    return AppContext.getApplicationContext().getBean(name);
  }

  /**
   * Ermittelt den Titel von Klarschiff
   *
   * @return Titel
   */
  public static String title() {
    return AppContext.getApplicationContext().getBean(SettingsService.class).getContextAppTitle();
  }

  /**
   * Ermittelt den Titel von Klarschiff inkl. der Aktuellen Vorgangsnummer
   *
   * @param vorgang
   * @return Titel
   */
  public static String titleWithVorgang(de.fraunhofer.igd.klarschiff.vo.Vorgang vorgang) {
    String title = title();
    if (vorgang != null && vorgang.getId() != null) {
      title += " - #" + vorgang.getId();
    }
    return title;
  }

  /**
   * Ermittelt das Gebiet, auf das sich Klarschiff bezieht
   *
   * @return Gebiet
   */
  public static String area() {
    return AppContext.getApplicationContext().getBean(SettingsService.class).getContextAppArea();
  }

  /**
   * Ermittelt, ob Klarschiff im Demo-Betrieb laufen soll
   *
   * @return <code>true</code> Klarschiff soll im Demo-Betrieb laufen
   */
  public static boolean demo() {
    return AppContext.getApplicationContext().getBean(SettingsService.class).getContextAppDemo();
  }

  /**
   * Ermittelt die Version von Klarschiff
   *
   * @return Version
   */
  public static String version() {
    return AppContext.getApplicationContext().getBean(SettingsService.class).getVersion();
  }

  /**
   * Ermittelt, ob die Login-Daten auf der Login-Seite angezeigt werden sollen
   *
   * @return <code>true</code> Login-Daten sollen auf der Login-Seite angezeigt werden
   */
  public static boolean showLogins() {
    return AppContext.getApplicationContext().getBean(SettingsService.class).getShowLogins();
  }

  /**
   * Parst den EnumVorgangStatus aus dem String
   *
   * @param status EnumVorgangStatus als String
   * @return EnumVorgangStatus
   */
  public static String vorgangStatus(String status) {
    return EnumVorgangStatus.valueOf(status).getText();
  }

  /**
   * Ermittelt ob der Vorgang offen Missbrauchsmeldungen hat.
   *
   * @param vorgang Vorgang, der nach offenen Missbrachsmeldungen untersucht werden soll
   * @return <code>true</code> - der gegebene Vorgang hat offene Missbrauchsmeldungen
   */
  public static boolean isOpenMissbrauchsmeldung(Vorgang vorgang) {
    VorgangDao vorgangDao = AppContext.getApplicationContext().getBean(VorgangDao.class);
    if (vorgangDao.countOpenMissbrauchsmeldungByVorgang(vorgang) == 0) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Zählt die offenen Missbrauchsmeldungen für einen Vorgang.
   *
   * @param vorgang Vorgang für den die offenen Missbrauchsmeldungen gezählt werden sollen
   * @return Anzahl der offenen Missbrauchsmeldungen
   */
  public static long countMissbrauchsmeldungen(Vorgang vorgang) {
    if (isOpenMissbrauchsmeldung(vorgang)) {
      VorgangDao vorgangDao = AppContext.getApplicationContext().getBean(VorgangDao.class);
      return vorgangDao.countOpenMissbrauchsmeldungByVorgang(vorgang);
    } else {
      return 0;
    }
  }

  /**
   * Ermittelt den aktuellen Benutzer und der Benutzerdaten.
   *
   * @return aktuelle Benutzer
   */
  public static User getCurrentUser() {
    return ((SecurityService) AppContext.getApplicationContext().getBean("securityService")).getCurrentUser();
  }

  /**
   * Ermittelt den verschlüsselten Login des aktuellen Benutzer.
   *
   * @return verschlüsselter Login des aktuellen Benutzers
   */
  public static String getCurrentUserLoginEncrypt() {
    return SecurityUtil.simpleEncrypt(((SecurityService) AppContext.getApplicationContext().getBean("securityService")).getCurrentUser().getId());
  }

  /**
   * Ermittelt ob der aktuelle Benutzer für den Vorgang zuständig ist.
   *
   * @param vorgang Vorgang der untersucht werden soll
   * @return <code>true</code> - der aktuelle Nutzer ist für den Vorgang zuständig
   */
  public static boolean isCurrentZustaendigForVorgang(Vorgang vorgang) {
    return ((SecurityService) AppContext.getApplicationContext().getBean("securityService")).isCurrentZustaendigForVorgang(vorgang);
  }

  /**
   * Ermittelt, ob der aktuelle Benutzer den Kommentar erstellt hat. Dies geschieht anhand des
   * Anzeigenamens.
   *
   * @param kommentar Kommentar, der geprüft werden soll
   * @return <code>true</code> - der aktuelle Benutzer hat den Kommentar erstellt
   */
  public static boolean mayCurrentUserEditKommentar(Kommentar kommentar) {
    return ((SecurityService) AppContext.getApplicationContext().getBean("securityService")).mayCurrentUserEditKommentar(kommentar);
  }

  /**
   * Fasst verschiedene Daten einer Exception und zusätzliche Daten für die Darstellung von
   * Exceptions in der GUI in einer Map zusammen (z.B. exceptionId, exceptionText,
   * showFehlerDetails, bugTrackingUrl).
   *
   * @param exception Exception aus der die Daten ermittelt werden sollen
   * @return Map mit Daten über die Exception und zur anzeige der Exception in der GUI
   */
  public static Map<String, Object> processException(Throwable exception) {
    return FehlerController.processException(exception);
  }

  /**
   * Ermittelt den ConnectorPort der aktuellen Serverinstanz.
   *
   * @return ConnectorPort der aktuellen Serverinstanz
   */
  public static String connector() {
    return ClusterUtil.getServerName() + ":" + ClusterUtil.getServerConnectorPort();
  }

  /**
   * Ermittelt den ConnectorPort der aktuellen Serverinstanz.
   *
   * @return ConnectorPort der aktuellen Serverinstanz
   */
  public static boolean showConnector() {
    try {
      return Boolean.parseBoolean(AppContext.getApplicationContext().getBean(SettingsService.class).getPropertyValue("show.connector"));
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Formatiert den Kommentar bei SQL-Scripten (PLSQL) <code>--...\n"</code> ->
   * <code><span class=\"codecomment\">--...</span></code>"
   *
   * @param sqlScript
   * @return umformatierter String
   */
  public static String plsqlCommentHtmlFormater(String sqlScript) {

    Pattern pattern = Pattern.compile("--[\\w\\W]*?\\n");
    sqlScript = RegexUtils.replaceAll(sqlScript, pattern, new RegexReplacer() {
      @Override
      public String replace(String input) {
        return "<span class=\"scriptCodeComment\">" + StringUtils.substringBeforeLast(input, "\n") + "</span>\n";
      }
    });
    return sqlScript;
  }
}
