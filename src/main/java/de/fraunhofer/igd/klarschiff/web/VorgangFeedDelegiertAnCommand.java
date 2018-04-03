package de.fraunhofer.igd.klarschiff.web;

import java.util.List;
import de.fraunhofer.igd.klarschiff.service.security.Role;

/**
 * Command für Deligierte Vorgänge im Backend <br>
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class VorgangFeedDelegiertAnCommand extends VorgangDelegiertSuchenCommand {

  private List<Role> delegiertAn;

  public List<Role> getDelegiertAn() {
    return delegiertAn;
  }

  public void setDelegiertAn(List<Role> delegiertAn) {
    this.delegiertAn = delegiertAn;
  }
}
