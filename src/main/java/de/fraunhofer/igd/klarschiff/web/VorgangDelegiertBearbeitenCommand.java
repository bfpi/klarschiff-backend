package de.fraunhofer.igd.klarschiff.web;

/**
 * Command für das Vorgangbearbeiten im Backend durch Externe (Delegierte)<br>
 * Beinhaltet ein Vorgangs-Objekt, Kommentar sowie die akutelle Seitenzahl und die Seitengröße.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
public class VorgangDelegiertBearbeitenCommand extends Command {

  String kommentar;

  Integer page;
  Integer size;

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
