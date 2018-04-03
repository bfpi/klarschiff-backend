package de.fraunhofer.igd.klarschiff.web;

import de.fraunhofer.igd.klarschiff.vo.Kategorie;

/**
 * Command für das Vorgangbearbeiten im Backend<br>
 * Beinhaltet ein Vorgangs-Objekt, Kategorie, Kommentar sowie die akutelle Seitenzahl und die
 * Seitengröße.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
public class VorgangBearbeitenCommand extends Command {

  Kategorie kategorie;
  String kommentar;

  Integer page;
  Integer size;

  public Kategorie getKategorie() {
    return kategorie;
  }

  public void setKategorie(Kategorie kategorie) {
    this.kategorie = kategorie;
  }

  public String getKommentar() {
    return kommentar;
  }

  public void setKommentar(String kommentar) {
    this.kommentar = kommentar;
  }

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
}
