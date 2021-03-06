package de.fraunhofer.igd.klarschiff.vo;

import de.fraunhofer.igd.klarschiff.context.AppContext;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Status eines Vorganges
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 *
 */
public enum EnumVorgangStatus implements EnumText {

  gemeldet("gemeldet"),
  offen("offen"),
  inBearbeitung("in Bearbeitung"),
  nichtLoesbar("nicht l&#246;sbar"),
  duplikat("Duplikat"),
  geloest("gel&#246;st"),
  geloescht("gel&#246;scht");

  /**
   * Gibt alle Status zurück, bei denen der Vorgang noch offen ist.
   *
   * @return offen Status
   */
  public static EnumVorgangStatus[] openVorgangStatus() {
    return new EnumVorgangStatus[]{gemeldet, offen, inBearbeitung};
  }

  /**
   * Gibt alle Status zurück, bei denen der Vorgang noch in Bearbeitung ist.
   *
   * @return inBearbeitung Status
   */
  public static EnumVorgangStatus[] inProgressVorgangStatus() {
    return new EnumVorgangStatus[]{inBearbeitung};
  }

  /**
   * Gibt alle Status zurück, bei denen der Vorgang geschlossen ist
   *
   * @return geschlossen Status
   */
  public static EnumVorgangStatus[] closedVorgangStatus() {
    return new EnumVorgangStatus[]{nichtLoesbar, duplikat, geloest, geloescht};
  }

  /**
   * Gibt alle Status zurück, die für den Außendienst berücksichtigt werden
   *
   * @return geschlossen Status
   */
  public static EnumVorgangStatus[] aussendienstVorgangStatus() {
    return new EnumVorgangStatus[]{offen, inBearbeitung, nichtLoesbar, duplikat, geloest};
  }

  /**
   * Gibt alle Status zurück, die auch für Externe (Delegiert) vorgesehen sind
   *
   * @return delegiert Status
   */
  public static EnumVorgangStatus[] delegiertVorgangStatus() {
    return new EnumVorgangStatus[]{inBearbeitung, nichtLoesbar, duplikat, geloest};
  }

  private String text;

  private EnumVorgangStatus(String text) {
    this.text = text;
  }

  @Override
  public String getText() {
    String tmp = AppContext.getApplicationContext().getBean(SettingsService.class).getPropertyValue("enum.status." + name());
    if (tmp != null) {
      return tmp;
    }
    return StringEscapeUtils.unescapeHtml(text);
  }

  public String getTextEncoded() {
    return text;
  }
}
