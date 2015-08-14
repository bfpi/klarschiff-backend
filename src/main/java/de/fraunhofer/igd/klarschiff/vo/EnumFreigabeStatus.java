package de.fraunhofer.igd.klarschiff.vo;

/**
 * Freigabestatus für Beschreibung und Foto
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public enum EnumFreigabeStatus implements EnumText {

  intern,
  extern,
  geloescht;

  @Override
  public String getText() {
    return name();
  }
}
