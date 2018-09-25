package de.fraunhofer.igd.klarschiff.service.security;

import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;
import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.KeyGenerator;
import com.googlecode.ehcache.annotations.Property;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.apache.commons.lang.StringUtils;

/**
 * Mit Hilfe des Service werden Daten vom LDAP gelesen. Dabei werden ergebnisse der anfrage gecacht.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 * @author Hani Samara (Fraunhofer IGD)
 */
@Service
public class SecurityServiceLdap {

  static final Logger logger = Logger.getLogger(SecurityServiceLdap.class);

  LdapTemplate ldapTemplate;

  /**
   * Ermittelt anhand eines gegebenen Path und eines Filters Daten vom LDAP. die Daten werden mit
   * hilfe eines Mappers auf eine Bean gemappt.
   *
   * @param <T>
   * @param path Path
   * @param filter Suchfilter
   * @param contextMapper Mapper zum Abbilden der Daten auf eine Bean
   * @return Liste von Beans mit den Daten
   */
  @SuppressWarnings("unchecked")
  @Cacheable(cacheName = "ldapCache",
    keyGenerator = @KeyGenerator(name = "ListCacheKeyGenerator",
      properties = {
        @Property(name = "useReflection", value = "true"),
        @Property(name = "checkforCycles", value = "true"),
        @Property(name = "includeMethod", value = "true")
      }
    )
  )
  public <T> List<T> getObjectListFromLdap(String path, String filter, IContextMapper<T> contextMapper) {
    if (path == null) {
      return ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter, contextMapper);
    }
    return ldapTemplate.search(path, filter, contextMapper);
  }

  /**
   * Ermittelt anhand eines gegebenen Path und eines Filters Daten vom LDAP. die Daten werden mit
   * hilfe eines Mappers auf eine Bean gemappt.
   *
   * @param path Path
   * @param filter Suchfilter
   * @param userSearchFilter
   * @param userSearchBase
   * @param userContextMapper Mapper zum Abbilden der Daten auf eine Bean
   * @param roleContextMapper Mapper zum Abbilden der Daten auf eine Bean
   * @return Liste von Beans mit den Daten
   */
  @SuppressWarnings("unchecked")
  @Cacheable(cacheName = "ldapCache",
    keyGenerator = @KeyGenerator(name = "ListCacheKeyGenerator",
      properties = {
        @Property(name = "useReflection", value = "true"),
        @Property(name = "checkforCycles", value = "true"),
        @Property(name = "includeMethod", value = "true")
      }
    )
  )
  public List<Role> getRoleListFromLdapAndReloadNames(String path, String filter, String userSearchFilter, String userSearchBase, ContextMapper<User> userContextMapper, ContextMapper<Role> roleContextMapper) {
    List<Role> roles = new ArrayList<Role>();
    if (path == null) {
      roles = ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter, roleContextMapper);
    }
    roles = ldapTemplate.search(path, filter, roleContextMapper);

    ArrayList conditionList = new ArrayList<String>();
    for (Role role : roles) {
      for (String user : role.getUser()) {
        conditionList.add("(" + StringUtils.replace(userSearchFilter, "{0}", user) + ")");
      }
    }
    if(conditionList.size() > 0) {
      String condition = conditionList.size() > 1 ? "(|" + String.join("", conditionList) + ")" : conditionList.get(0).toString();
      List<User> ldapUsers = getObjectListFromLdap(userSearchBase, condition, userContextMapper);

      for (Role role : roles) {
        List<String> newUser = new ArrayList<String>();
        for (String user : role.getUser()) {
          for (User ldapUser : ldapUsers) {
            if (user.equals(ldapUser.getId())) {
              newUser.add(ldapUser.getName() + " (" + ldapUser.getId() + ")");
              break;
            }
          }
        }
        role.setUser(newUser);
      }

      Collections.sort(roles, new Comparator<Role>() {
        public int compare(Role r1, Role r2) {
          return r1.getId().compareTo(r2.getId());
        }
      });
    }
    return roles;
  }

  /* --------------- GET + SET ----------------------------*/
  public LdapTemplate getLdapTemplate() {
    return ldapTemplate;
  }

  public void setLdapTemplate(LdapTemplate ldapTemplate) {
    this.ldapTemplate = ldapTemplate;
  }

}
