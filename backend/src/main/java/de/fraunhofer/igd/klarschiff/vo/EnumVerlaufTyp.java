package de.fraunhofer.igd.klarschiff.vo;

/**
 * Definiert den Typ der Eintrages im Verlauf eines Vorganges
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public enum EnumVerlaufTyp implements EnumText{

    erzeugt, 
    betreff, 
    betreffFreigabeStatus, 
    detailsFreigabeStatus, 
    detail,
    adresse,
    fotoFreigabeStatus, 
    foto, 
    typ,
    kategorie,
    status,
    statusKommentar,
    archiv,
    zustaendigkeitAkzeptiert,
    zustaendigkeit,
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
    flurstueckseigentum;

    @Override
	public String getText() {
		return name();
	}
}
