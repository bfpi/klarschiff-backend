package de.fraunhofer.igd.klarschiff.service.security;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.KeyGenerator;
import com.googlecode.ehcache.annotations.Property;

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


  /* --------------- GET + SET ----------------------------*/
  public LdapTemplate getLdapTemplate() {
    return ldapTemplate;
  }

  public void setLdapTemplate(LdapTemplate ldapTemplate) {
    this.ldapTemplate = ldapTemplate;
  }

}
