package de.fraunhofer.igd.klarschiff.vo;

/**
 * Status eines Auftrags
 *
 * @author Robert Voß (BFPI GmbH)
 */
public enum EnumAuftragStatus implements EnumText {

  abgehakt,
  nicht_abgehakt,
  nicht_abarbeitbar;

  @Override
  public String getText() {
    return name();
  }
}
