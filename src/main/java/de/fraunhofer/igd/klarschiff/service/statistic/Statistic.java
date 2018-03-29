package de.fraunhofer.igd.klarschiff.service.statistic;

import java.util.List;

import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Die Klasse stellt eine Bean zum Ablegen der ermittelten Daten für die Statistik bereit.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class Statistic {

  List<Vorgang> neuesteVorgaenge;
  List<Vorgang> eigeneVorgaenge;
  List<Vorgang> ehemaligeVorgaenge;
  List<Vorgang> vorgaengeMissbrauchsmeldungen;
  List<Vorgang> vorgaengeOffenNichtAkzeptiert;
  List<Vorgang> vorgaengeInbearbeitungOhneStatusKommentar;
  List<Vorgang> vorgaengeIdeeOffenOhneUnterstuetzung;
  List<Vorgang> vorgaengeNichtLoesbarOhneStatuskommentar;
  List<Vorgang> vorgaengeNichtMehrOffenNichtAkzeptiert;
  List<Vorgang> vorgaengeOhneRedaktionelleFreigaben;
  List<StatusVerteilungEntry> statusVerteilung;

  /* --------------- GET + SET ----------------------------*/
  public List<Vorgang> getVorgaengeMissbrauchsmeldungen() {
    return vorgaengeMissbrauchsmeldungen;
  }

  public void setVorgaengeMissbrauchsmeldungen(List<Vorgang> vorgaengeMissbrauchsmeldungen) {
    this.vorgaengeMissbrauchsmeldungen = vorgaengeMissbrauchsmeldungen;
  }

  public List<Vorgang> getNeuesteVorgaenge() {
    return neuesteVorgaenge;
  }

  public void setNeuesteVorgaenge(List<Vorgang> neuesteVorgaenge) {
    this.neuesteVorgaenge = neuesteVorgaenge;
  }

  public List<Vorgang> getEigeneVorgaenge() {
    return eigeneVorgaenge;
  }

  public void setEigeneVorgaenge(List<Vorgang> eigeneVorgaenge) {
    this.eigeneVorgaenge = eigeneVorgaenge;
  }

  public List<Vorgang> getEhemaligeVorgaenge() {
    return ehemaligeVorgaenge;
  }

  public void setEhemaligeVorgaenge(List<Vorgang> ehemaligeVorgaenge) {
    this.ehemaligeVorgaenge = ehemaligeVorgaenge;
  }

  public List<Vorgang> getVorgaengeOffenNichtAkzeptiert() {
    return vorgaengeOffenNichtAkzeptiert;
  }

  public void setVorgaengeOffenNichtAkzeptiert(List<Vorgang> vorgaengeOffenNichtAkzeptiert) {
    this.vorgaengeOffenNichtAkzeptiert = vorgaengeOffenNichtAkzeptiert;
  }

  public List<Vorgang> getVorgaengeInbearbeitungOhneStatusKommentar() {
    return vorgaengeInbearbeitungOhneStatusKommentar;
  }

  public void setVorgaengeInbearbeitungOhneStatusKommentar(List<Vorgang> vorgaengeInbearbeitungOhneStatusKommentar) {
    this.vorgaengeInbearbeitungOhneStatusKommentar = vorgaengeInbearbeitungOhneStatusKommentar;
  }

  public List<Vorgang> getVorgaengeIdeeOffenOhneUnterstuetzung() {
    return vorgaengeIdeeOffenOhneUnterstuetzung;
  }

  public void setVorgaengeIdeeOffenOhneUnterstuetzung(List<Vorgang> vorgaengeIdeeOffenOhneUnterstuetzung) {
    this.vorgaengeIdeeOffenOhneUnterstuetzung = vorgaengeIdeeOffenOhneUnterstuetzung;
  }

  public List<Vorgang> getVorgaengeNichtLoesbarOhneStatuskommentar() {
    return vorgaengeNichtLoesbarOhneStatuskommentar;
  }

  public void setVorgaengeNichtLoesbarOhneStatuskommentar(List<Vorgang> vorgaengeNichtLoesbarOhneStatuskommentar) {
    this.vorgaengeNichtLoesbarOhneStatuskommentar = vorgaengeNichtLoesbarOhneStatuskommentar;
  }

  public List<Vorgang> getVorgaengeNichtMehrOffenNichtAkzeptiert() {
    return vorgaengeNichtMehrOffenNichtAkzeptiert;
  }

  public void setVorgaengeNichtMehrOffenNichtAkzeptiert(List<Vorgang> vorgaengeNichtMehrOffenNichtAkzeptiert) {
    this.vorgaengeNichtMehrOffenNichtAkzeptiert = vorgaengeNichtMehrOffenNichtAkzeptiert;
  }

  public List<Vorgang> getVorgaengeOhneRedaktionelleFreigaben() {
    return vorgaengeOhneRedaktionelleFreigaben;
  }

  public void setVorgaengeOhneRedaktionelleFreigaben(List<Vorgang> vorgaengeOhneRedaktionelleFreigaben) {
    this.vorgaengeOhneRedaktionelleFreigaben = vorgaengeOhneRedaktionelleFreigaben;
  }

  public List<StatusVerteilungEntry> getStatusVerteilung() {
    return statusVerteilung;
  }

  public void setStatusVerteilung(List<StatusVerteilungEntry> statusVerteilung) {
    this.statusVerteilung = statusVerteilung;
  }

}
