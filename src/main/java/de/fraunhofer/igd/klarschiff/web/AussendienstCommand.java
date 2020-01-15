package de.fraunhofer.igd.klarschiff.web;

import java.util.Date;

/**
 * Command für die Auftragslisten im Backend <br>
 *
 * @author Robert Voß (BFPI GmbH)
 */
@SuppressWarnings("serial")
public class AussendienstCommand extends Command {

  Date datum;
  boolean alleVorgaengeAuswaehlen;
  Long[] vorgangAuswaehlen;

  /* --------------- GET + SET ----------------------------*/
  public Date getDatum() {
    return datum;
  }

  public void setDatum(Date datum) {
    this.datum = datum;
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
}
