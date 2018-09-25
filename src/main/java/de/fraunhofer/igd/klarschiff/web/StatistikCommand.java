package de.fraunhofer.igd.klarschiff.web;

import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import java.util.Date;

/**
 * Command für die Erstellung von Statistiken im Backend. <br />
 *
 * @author Robert Voß (BFPI)
 */
@SuppressWarnings("serial")
public class StatistikCommand extends Command {

  String type;
  EnumVorgangTyp typ;
  Date zeitraumVon;
  Date zeitraumBis;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public EnumVorgangTyp getTyp() {
    return typ;
  }

  public void setTyp(EnumVorgangTyp typ) {
    this.typ = typ;
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
