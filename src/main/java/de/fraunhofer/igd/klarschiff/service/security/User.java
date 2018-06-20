package de.fraunhofer.igd.klarschiff.service.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import de.fraunhofer.igd.klarschiff.context.AppContext;
import de.fraunhofer.igd.klarschiff.vo.Flaeche;

/**
 * Bean zum Abbilden der Daten eines Benutzers aus dem LDAP. die Bean enthält zusätzlich weitere
 * Funktionen, um auf die Rollen des Benutzers leicht zugreifen zu können.
 *
 * @see ContextMapper
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class User {

  String id;
  String name;
  String email;
  String dn;
  List<String> group = new ArrayList<String>();

  private Integer dbId;
  private List<Flaeche> flaechen = new ArrayList<Flaeche>();

  /**
   * Ermittelt die Zuständigkeiten des Benutzers.
   *
   * @return Zuständigkeiten des Benutzers
   */
  public List<Role> getZustaendigkeiten() {
    SecurityService service = AppContext.getApplicationContext().getBean(SecurityService.class);
    if (group.size() > 0) {
      return getRoles(service.groupIntern);
    }
    return service.getZustaendigkeiten(id, true);
  }

  /**
   * Ermittelt die DelgiertAn-Rollen des Benutzers.
   *
   * @return DelgiertAn-Rollen des Benutzers
   */
  public List<Role> getDelegiertAn() {
    SecurityService service = AppContext.getApplicationContext().getBean(SecurityService.class);
    if (group.size() > 0) {
      return getRoles(service.groupExtern);
    }
    return service.getDelegiertAn(id);
  }

  /**
   * Ermittelt, ob der Benutzer ein externer Nutzer ist.
   *
   * @return <code>true</code> - externe Nutzer
   */
  public boolean getUserExtern() {
    SecurityService service = AppContext.getApplicationContext().getBean(SecurityService.class);
    if (group.size() > 0) {
      return isInRole(service.groupExtern);
    }
    return service.isUserExtern(id);
  }

  /**
   * Ermittelt, ob der Benutzer ein interne Nutzer ist.
   *
   * @return <code>true</code> - interne Nutzer
   */
  public boolean getUserIntern() {
    SecurityService service = AppContext.getApplicationContext().getBean(SecurityService.class);
    if (group.size() > 0) {
      return isInRole(service.groupIntern);
    }
    return service.isUserIntern(id);
  }

  /**
   * Ermittelt, ob der Benutzer ein Admin ist.
   *
   * @return <code>true</code> - Admins
   */
  public boolean getUserAdmin() {
    SecurityService service = AppContext.getApplicationContext().getBean(SecurityService.class);
    if (group.size() > 0) {
      return isInRole(service.groupAdmin);
    }
    return service.isUserAdmin(id);
  }

  /**
   * Ermittelt, ob der Benutzer ein Koordinator ist.
   *
   * @return <code>true</code> - Admins
   */
  public boolean getUserKoordinator() {
    SecurityService service = AppContext.getApplicationContext().getBean(SecurityService.class);
    if (group.size() > 0) {
      return isInRole(service.groupKoordinator);
    }
    return service.isUserKoordinator(id);
  }

  /**
   * Namen der übergebenen User als Liste wieder zurückgeben.
   *
   * @param users Liste von User
   * @return Liste der Namen
   */
  public static List<String> toString(Collection<User> users) {
    List<String> _users = new ArrayList<String>();
    for (User user : users) {
      _users.add(user.getName());
    }
    return _users;
  }

  /**
   * Ermittelt die Zuständigkeiten des Benutzers.
   *
   * @return Zuständigkeiten des Benutzers
   */
  public List<String> getAussendienstKoordinatorZustaendigkeiten() {
    return AppContext.getApplicationContext().getBean(SecurityService.class).getAussendienstKoordinatorZustaendigkeiten(id);
  }

  /**
   * Ermittelt die Zuständigkeiten des Benutzers.
   *
   * @return Zuständigkeiten des Benutzers
   */
  public List<String> getAussendienstTeams() {
    SecurityService service = AppContext.getApplicationContext().getBean(SecurityService.class);
    if (group.size() > 0) {
      return getRoleStrings(service.groupAussendienst);
    }
    return service.getAussendienstTeam(id);
  }

  private boolean isInRole(String role) {
    SecurityService service = AppContext.getApplicationContext().getBean(SecurityService.class);
    List<Role> roles = service.getGroupsForRole(role);
    for (String g : group) {
      for (Role r : roles) {
        if (g.equals(r.getId())) {
          return true;
        }
      }
    }
    return false;
  }

  private List<Role> getRoles(String role) {
    SecurityService service = AppContext.getApplicationContext().getBean(SecurityService.class);
    List<Role> roles = service.getGroupsForRole(role);
    List<Role> ret = new ArrayList<Role>();
    for (String g : group) {
      for (Role r : roles) {
        if (g.equals(r.getId())) {
          ret.add(r);
        }
      }
    }
    return ret;
  }

  private List<String> getRoleStrings(String role) {
    SecurityService service = AppContext.getApplicationContext().getBean(SecurityService.class);
    List<Role> roles = service.getGroupsForRole(role);
    List<String> ret = new ArrayList<String>();
    for (String g : group) {
      for (Role r : roles) {
        if (g.equals(r.getId())) {
          ret.add(g);
        }
      }
    }
    return ret;
  }

  /* --------------- GET + SET ----------------------------*/
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDn() {
    return dn;
  }

  public void setDn(String dn) {
    this.dn = dn;
  }

  public Integer getDbId() {
    return dbId;
  }

  public void setDbId(Integer dbId) {
    this.dbId = dbId;
  }

  public List<Flaeche> getFlaechen() {
    return flaechen;
  }

  public void setFlaechen(List<Flaeche> flaechen) {
    this.flaechen = flaechen;
  }

  public List<String> getGroup() {
    return group;
  }

  public void setGroup(List<String> group) {
    this.group = group;
  }
}
