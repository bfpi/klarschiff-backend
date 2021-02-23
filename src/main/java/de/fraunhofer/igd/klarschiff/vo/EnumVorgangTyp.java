package de.fraunhofer.igd.klarschiff.vo;

import de.fraunhofer.igd.klarschiff.context.AppContext;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Typ eines Vorganges
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public enum EnumVorgangTyp implements EnumText {

  problem("Problem"),
  idee("Idee"),
  tipp("Tipp");

  private String text;

  private EnumVorgangTyp(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public static List getEnumVorgangTypen() {
    SettingsService settings = AppContext.getApplicationContext().getBean(SettingsService.class);
    List al = new ArrayList();
    for (EnumVorgangTyp value : EnumVorgangTyp.values()) {
      if(!value.name().equals("tipp") || settings.getPropertyValueBoolean("issue.type.tipp.enabled", true)) {
        al.add(value);
      }
    }

    return al;
  }
}
