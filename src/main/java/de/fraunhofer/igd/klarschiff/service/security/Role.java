package de.fraunhofer.igd.klarschiff.service.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.fraunhofer.igd.klarschiff.context.AppContext;

import org.apache.commons.lang.StringUtils;

/**
 * Bean zum Abbilden der Daten einer Rolle aus dem LDAP.
 *
 * @see ContextMapper
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class Role {

  String id;
  String description;
  String dn;
  String l;

  public static List<String> toString(Collection<Role> roles) {
    List<String> _roles = new ArrayList<String>();
    for (Role role : roles) {
      _roles.add(role.getId());
    }
    return _roles;
  }

  @Override
  public boolean equals(Object o) {
    if (o != null && o instanceof Role) {
      return StringUtils.equals(id, ((Role) o).getId());
    } else {
      return super.equals(o);
    }
  }

  /**
   * Ermittelt die Benutzer der Rolle.
   *
   * @return Benutzer der Rolle
   */
  public List<User> getUsersRole() {
    return AppContext.getApplicationContext().getBean(SecurityService.class).getAllUserForRole(id);
  }

  /* --------------- GET + SET ----------------------------*/
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDn() {
    return dn;
  }

  public void setDn(String dn) {
    this.dn = dn;
  }

  public String getL() {
    return l;
  }

  public void setL(String l) {
    this.l = l;
  }
}
