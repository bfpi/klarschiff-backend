package de.fraunhofer.igd.klarschiff.vo;

/**
 * Status eines Auftrags
 *
 * @author Robert Vo� (BFPI GmbH)
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
