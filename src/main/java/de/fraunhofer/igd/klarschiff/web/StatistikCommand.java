package de.fraunhofer.igd.klarschiff.web;

import java.util.Date;

/**
 * Command für die Erstellung von Statistiken im Backend. <br />
 *
 * @author Robert Voß (BFPI)
 */
@SuppressWarnings("serial")
public class StatistikCommand extends Command {

  Date zeitraumVon;
  Date zeitraumBis;

  public Date getZeitraumVon() {
    return zeitraumVon;
  }

  public void setZeitraumVon(Date zeitraumVon) {
    this.zeitraumVon = zeitraumVon;
  }

  public Date getZeitraumBis() {
    return zeitraumBis;
  }

  public void setZeitraumBis(Date zeitraumBis) {
    this.zeitraumBis = zeitraumBis;
  }

}
