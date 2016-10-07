package de.fraunhofer.igd.klarschiff.web;

import java.util.Date;

import de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;

/**
 * Command für die Vorgangsuche. <br />
 * Beinhaltet Suchfelder für einfache und erweiterte Suche sowie Attribute für die
 * Ergebnisdarstellung: <br/>
 * <code>page</code>: die aktuelle Seitenzahl<br/>
 * <code>size</code>: die konfigurierte Anzahl von Einträgen pro Seite<br/>
 * <code>order</code>: die Spalte nach der sortiert wird<br/>
 * <code>orderDirection</code>: die Sortierreihenfolge (1:absteigend,default:aufsteigend)
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
public class VorgangSuchenCommand extends Command {

  /* --------------- Attribute ----------------------------*/
  public enum Suchtyp {

    einfach, erweitert, aussendienst
  };

  public enum EinfacheSuche {

    offene, offeneIdeen, abgeschlossene
  };

  Integer page;
  Integer size;
  Integer order;
  Integer orderDirection;

  // einfache vs. erweiterte suche
  Suchtyp suchtyp;

  // einfache Suche
  EinfacheSuche einfacheSuche;

  // erweiterte Suche
  String erweitertFulltext;
  EnumVorgangTyp erweitertVorgangTyp;
  Kategorie erweitertHauptkategorie;
  Kategorie erweitertKategorie;
  Date erweitertDatumVon;
  Date erweitertDatumBis;
  Date aktualisiertVon;
  Date aktualisiertBis;
  EnumVorgangStatus[] erweitertVorgangStatus;
  Boolean erweitertArchiviert;
  String erweitertZustaendigkeit;
  Long erweitertUnterstuetzerAb;
  EnumPrioritaet erweitertPrioritaet;
  String erweitertDelegiertAn;
  Integer erweitertStadtteilgrenze;
  String erweitertNummer;
  boolean alleVorgaengeAuswaehlen;
  Long[] vorgangAuswaehlen;
  String auftragTeam;
  Date auftragDatum;
  String negation;
  String suchbereich;
  Boolean ueberspringeVorgaengeMitMissbrauchsmeldungen;

  //NUR ADMIN dürfen andere Zuständigkeiten sehen
  public String getOrderString() {
    switch (order) {
      case 0:
        return "vo.id";
      case 1:
        return "vo.typ";
      case 2:
        return "vo.datum";
      case 3:
        return "kat_haupt.name,kat_unter.name";
      case 4:
        return "vo.status_ordinal";
      case 5:
        return "vo.adresse";
      case 6:
        return "un.count";
      case 7:
        return "vo.zustaendigkeit";
      case 8:
        return "auftrag.prioritaet, vo.datum";
      default:
        return "";
    }
  }

  public String getOrderDirectionString() {
    switch (orderDirection) {
      case 1:
        return "desc";
      default:
        return "asc";
    }
  }

  /* --------------- GET + SET ----------------------------*/
  public Integer getPage() {
    return page;
  }

  public void setPage(Integer page) {
    this.page = page;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public Suchtyp getSuchtyp() {
    return suchtyp;
  }

  public void setSuchtyp(Suchtyp suchtyp) {
    setPage(1);
    this.suchtyp = suchtyp;
  }

  public EinfacheSuche getEinfacheSuche() {
    return einfacheSuche;
  }

  public void setEinfacheSuche(EinfacheSuche einfacheSuche) {
    this.einfacheSuche = einfacheSuche;
  }

  public Integer getOrder() {
    return order;
  }

  public void setOrder(Integer order) {
    this.order = order;
  }

  public Integer getOrderDirection() {
    return orderDirection;
  }

  public void setOrderDirection(Integer orderDirection) {
    this.orderDirection = orderDirection;
  }

  public String getErweitertFulltext() {
    return erweitertFulltext;
  }

  public void setErweitertFulltext(String erweitertFulltext) {
    try {
      if (erweitertFulltext != null) {
        this.erweitertFulltext = new String(erweitertFulltext.getBytes(), "UTF-8");
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Long getErweitertNummerAsLong() {
    if (this.erweitertNummer == null) {
      return null;
    }
    try {
      return Long.parseLong(erweitertNummer);
    } catch (Exception e) {
      return null;
    }
  }

  public String getErweitertNummer() {
    Long erwNummer = this.getErweitertNummerAsLong();
    if (erwNummer == null) {
      return "";
    } else {
      return erwNummer.toString();
    }
  }

  public void setErweitertNummer(String erweitertNummer) {
    this.erweitertNummer = erweitertNummer;
  }

  public EnumVorgangTyp getErweitertVorgangTyp() {
    return erweitertVorgangTyp;
  }

  public void setErweitertVorgangTyp(EnumVorgangTyp erweitertVorgangTyp) {
    this.erweitertVorgangTyp = erweitertVorgangTyp;
  }

  public Kategorie getErweitertHauptkategorie() {
    return erweitertHauptkategorie;
  }

  public void setErweitertHauptkategorie(Kategorie erweitertHauptkategorie) {
    this.erweitertHauptkategorie = erweitertHauptkategorie;
  }

  public Kategorie getErweitertKategorie() {
    return erweitertKategorie;
  }

  public void setErweitertKategorie(Kategorie erweitertKategorie) {
    this.erweitertKategorie = erweitertKategorie;
  }

  public Date getErweitertDatumVon() {
    return erweitertDatumVon;
  }

  public void setErweitertDatumVon(Date erweitertDatumVon) {
    this.erweitertDatumVon = erweitertDatumVon;
  }

  public Date getErweitertDatumBis() {
    return erweitertDatumBis;
  }

  public void setErweitertDatumBis(Date erweitertDatumBis) {
    this.erweitertDatumBis = erweitertDatumBis;
  }

  public Date getAktualisiertVon() {
    return aktualisiertVon;
  }

  public void setAktualisiertVon(Date aktualisiertVon) {
    this.aktualisiertVon = aktualisiertVon;
  }

  public Date getAktualisiertBis() {
    return aktualisiertBis;
  }

  public void setAktualisiertBis(Date aktualisiertBis) {
    this.aktualisiertBis = aktualisiertBis;
  }

  public EnumVorgangStatus[] getErweitertVorgangStatus() {
    return erweitertVorgangStatus;
  }

  public void setErweitertVorgangStatus(EnumVorgangStatus[] erweitertVorgangStatus) {
    this.erweitertVorgangStatus = erweitertVorgangStatus;
  }

  public Boolean getErweitertArchiviert() {
    return erweitertArchiviert;
  }

  public void setErweitertArchiviert(Boolean erweitertArchiviert) {
    this.erweitertArchiviert = erweitertArchiviert;
  }

  public String getErweitertZustaendigkeit() {
    return erweitertZustaendigkeit;
  }

  public void setErweitertZustaendigkeit(String erweitertZustaendigkeit) {
    this.erweitertZustaendigkeit = erweitertZustaendigkeit;
  }

  public Long getErweitertUnterstuetzerAb() {
    return erweitertUnterstuetzerAb;
  }

  public void setErweitertUnterstuetzerAb(Long erweitertUnterstuetzerAb) {
    this.erweitertUnterstuetzerAb = erweitertUnterstuetzerAb;
  }

  public EnumPrioritaet getErweitertPrioritaet() {
    return erweitertPrioritaet;
  }

  public void setErweitertPrioritaet(EnumPrioritaet erweitertPrioritaet) {
    this.erweitertPrioritaet = erweitertPrioritaet;
  }

  public String getErweitertDelegiertAn() {
    return erweitertDelegiertAn;
  }

  public void setErweitertDelegiertAn(String erweitertDelegiertAn) {
    this.erweitertDelegiertAn = erweitertDelegiertAn;
  }

  public Integer getErweitertStadtteilgrenze() {
    return erweitertStadtteilgrenze;
  }

  public void setErweitertStadtteilgrenze(Integer erweitertStadtteilgrenze) {
    this.erweitertStadtteilgrenze = erweitertStadtteilgrenze;
  }

  public boolean isAlleVorgaengeAuswaehlen() {
    return alleVorgaengeAuswaehlen;
  }

  public void setAlleVorgaengeAuswaehlen(boolean alleVorgaengeAuswaehlen) {
    this.alleVorgaengeAuswaehlen = alleVorgaengeAuswaehlen;
  }

  public Long[] getVorgangAuswaehlen() {
    return vorgangAuswaehlen;
  }

  public void setVorgangAuswaehlen(Long[] vorgangAuswaehlen) {
    this.vorgangAuswaehlen = vorgangAuswaehlen;
  }

  public String getAuftragTeam() {
    return auftragTeam;
  }

  public void setAuftragTeam(String auftragTeam) {
    this.auftragTeam = auftragTeam;
  }

  public Date getAuftragDatum() {
    return auftragDatum;
  }

  public void setAuftragDatum(Date auftragDatum) {
    this.auftragDatum = auftragDatum;
  }

  public String getNegation() {
    return negation;
  }

  public void setNegation(String negation) {
    this.negation = negation;
  }

  public String getSuchbereich() {
    return suchbereich;
  }

  public void setSuchbereich(String suchbereich) {
    this.suchbereich = suchbereich;
  }

  public Boolean getUeberspringeVorgaengeMitMissbrauchsmeldungen() {
    return ueberspringeVorgaengeMitMissbrauchsmeldungen == null ? false : ueberspringeVorgaengeMitMissbrauchsmeldungen;
  }

  public void setUeberspringeVorgaengeMitMissbrauchsmeldungen(Boolean ueberspringeVorgaengeMitMissbrauchsmeldungen) {
    this.ueberspringeVorgaengeMitMissbrauchsmeldungen = ueberspringeVorgaengeMitMissbrauchsmeldungen;
  }
}
