package de.fraunhofer.igd.klarschiff.web;

import java.util.Date;

import de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;

/**
 * Command für die Vorgangsuche für Externe (Delegierte). <br />
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
public class VorgangDelegiertSuchenCommand extends Command {

  /* --------------- Attribute ----------------------------*/
  public enum Suchtyp {

    einfach, erweitert
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
  EnumVorgangStatus[] erweitertVorgangStatus;
  EnumPrioritaet erweitertPrioritaet;
  Integer erweitertStadtteilgrenze;
  String erweitertNummer;

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
      case 7:
        return "vo.zustaendigkeit";
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

  public EnumVorgangStatus[] getErweitertVorgangStatus() {
    return erweitertVorgangStatus;
  }

  public void setErweitertVorgangStatus(EnumVorgangStatus[] erweitertVorgangStatus) {
    this.erweitertVorgangStatus = erweitertVorgangStatus;
  }

  public EnumPrioritaet getErweitertPrioritaet() {
    return erweitertPrioritaet;
  }

  public void setErweitertPrioritaet(EnumPrioritaet erweitertPrioritaet) {
    this.erweitertPrioritaet = erweitertPrioritaet;
  }

  public Integer getErweitertStadtteilgrenze() {
    return erweitertStadtteilgrenze;
  }

  public void setErweitertStadtteilgrenze(Integer erweitertStadtteilgrenze) {
    this.erweitertStadtteilgrenze = erweitertStadtteilgrenze;
  }
}
