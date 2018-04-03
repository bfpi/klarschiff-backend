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

  /* --------------- GET + SET ----------------------------*/
  public Date getDatum() {
    return datum;
  }

  public void setDatum(Date datum) {
    this.datum = datum;
  }
}
