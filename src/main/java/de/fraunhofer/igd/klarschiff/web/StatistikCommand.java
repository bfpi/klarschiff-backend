package de.fraunhofer.igd.klarschiff.web;

import java.util.Date;

/**
 * Command für die Erstellung von Statistiken im Backend. <br />
 *
 * @author Robert Voß (BFPI)
 */
@SuppressWarnings("serial")
public class StatistikCommand extends Command {

  String type;
  Date zeitraumVon;
  Date zeitraumBis;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

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
