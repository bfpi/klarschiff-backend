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
  List<String> user = new ArrayList<String>();

  /**
   * IDs der übergebenen Rollen als Liste wieder zurückgeben.
   *
   * @param roles Liste von Rollen
   * @return Liste der IDs
   */
  public static List<String> toString(Collection<Role> roles) {
    List<String> _roles = new ArrayList<String>();
    for (Role role : roles) {
      _roles.add(role.getId());
    }
    return _roles;
  }

  /**
   * Vergleich ob es sich um die übergebene Rolle handelt
   *
   * @param o Rolle mit der Verglcihen werden soll
   * @return <code>true</code> - bei der übergebenen Rolle handelt es sich um die aktuelle Rolle;
   * <code>false</code> - die Übergebene Rolle ist nicht die aktuelle Rolle
   */
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

  public List<String> getUser() {
    return user;
  }

  public void setUser(List<String> user) {
    this.user = user;
  }

}
