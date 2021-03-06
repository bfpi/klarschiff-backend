package de.fraunhofer.igd.klarschiff.vo;

/**
 * Definiert den Typ der Eintrages im Verlauf eines Vorganges
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public enum EnumVerlaufTyp implements EnumText {

  erzeugt,
  beschreibung,
  beschreibungFreigabeStatus,
  adresse,
  ovi,
  fotoFreigabeStatus,
  foto,
  fotowunsch,
  fotoBestaetigung,
  typ,
  kategorie,
  status,
  statusKommentar,
  archiv,
  zustaendigkeitAkzeptiert,
  zustaendigkeit,
  lobHinweiseKritik,
  delegiertAn,
  kommentar,
  prioritaet,
  missbrauchsmeldungErzeugt,
  missbrauchsmeldungBearbeitet,
  missbrauchsmeldungBestaetigung,
  missbrauchsmeldungEmail,
  vorgangBestaetigungEmail,
  vorgangBestaetigung,
  unterstuetzerEmail,
  unterstuetzerBestaetigung,
  weiterleitenEmail,
  flurstueckseigentum,
  aufgabeStatus;

  @Override
  public String getText() {
    return name();
  }

  public static EnumVerlaufTyp[] relevantBeiLetztenAktivitaeten() {
    return new EnumVerlaufTyp[]{foto, lobHinweiseKritik, kommentar, unterstuetzerBestaetigung, beschreibung, typ, kategorie, adresse, ovi, flurstueckseigentum};
  }
}
