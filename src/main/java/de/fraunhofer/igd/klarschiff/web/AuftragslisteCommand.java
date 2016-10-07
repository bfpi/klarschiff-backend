package de.fraunhofer.igd.klarschiff.web;

import de.fraunhofer.igd.klarschiff.vo.EnumAuftragStatus;
import java.util.Date;

/**
 * Command für die Auftragslisten im Backend <br />
 *
 * @author Robert Voß (BFPI GmbH)
 */
@SuppressWarnings("serial")
public class AuftragslisteCommand extends Command {

  Date datum;
  EnumAuftragStatus status;

  /* --------------- GET + SET ----------------------------*/
  public Date getDatum() {
    return datum;
  }

  public void setDatum(Date datum) {
    this.datum = datum;
  }

  public EnumAuftragStatus getStatus() {
    return status;
  }

  public void setStatus(EnumAuftragStatus status) {
    this.status = status;
  }
}
