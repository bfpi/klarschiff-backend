package de.fraunhofer.igd.klarschiff.service.security;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.ldap.core.DirContextAdapter;

/**
 * Mapper zum Mappen von Daten aus dem LDAP auf eine Bean
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 * @param <T> Klasse der Bean, auf die die Daten aus dem LDAP gemappt werden sollen.
 */
public class ContextMapper<T> implements IContextMapper<T> {

  MultiValueMap mapping = new MultiValueMap();
  Class<T> clazz;
  String basePath;
  SecurityService securityService;

  /**
   *
   * @param clazz Klasse der Bean, in dem die Daten gemappt werden sollen.
   * @param mapping Mapping der Attribute in der Bean auf die Attribute im LDAP, z.B.:
   * <code>name=cn,vorname=sn,...</code>
   * @param basePath String
   * @param securityService SecurityService
   */
  public ContextMapper(Class<T> clazz, String mapping, String basePath, SecurityService securityService) {
    this.clazz = clazz;
    this.basePath = basePath;
    this.securityService = securityService;
    for (String str : mapping.split(",")) {
      String[] pair = str.split("=");
      this.mapping.put(pair[1].trim(), pair[0].trim());
    }
  }

  /**
   * Funktion zum Mappen der Daten aus dem LDAP auf die Bean.
   *
   * @param ctx Context
   * @return T
   */
  @Override
  public T mapFromContext(Object ctx) {
    T t;
    try {
      t = clazz.newInstance();
    } catch (Exception e) {
      throw new RuntimeException();
    }

    DirContextAdapter dca = (DirContextAdapter) ctx;

    try {
      String dn = dca.getDn().toString();
      if (!StringUtils.isBlank(basePath) && !dn.contains(basePath)) {
        dn += "," + basePath;
      }
      PropertyUtils.setSimpleProperty(t, "dn", dn);
    } catch (Exception e) {
    }

    for (Object ldapKey : mapping.keySet()) {
      String _ldapKey = (String) ldapKey;

      if (dca.getStringAttribute(_ldapKey) != null) {
        try {
          for (Object beanKey : mapping.getCollection(ldapKey)) {
            String _beanKey = (String) beanKey;

            if (_beanKey.equals("group") && PropertyUtils.getPropertyType(t, _beanKey) == List.class) {
              List tmpList = (List) PropertyUtils.getProperty(t, _beanKey);
              for (String stringAttribute : dca.getStringAttributes(_ldapKey)) {
                if (stringAttribute.toLowerCase().endsWith(securityService.groupSearchBase.toLowerCase())) {

                  Pattern pattern = Pattern.compile(securityService.groupObjectId + "=([\\w\\d]*),");
                  Matcher matcher = pattern.matcher(stringAttribute);
                  if (matcher.find()) {
                    tmpList.add(matcher.group(1));
                  }

                }
              }
              PropertyUtils.setProperty(t, _beanKey, tmpList);
            } else if (_beanKey.equals("user") && PropertyUtils.getPropertyType(t, _beanKey) == List.class) {
              List tmpList = (List) PropertyUtils.getProperty(t, _beanKey);
              for (String stringAttribute : dca.getStringAttributes(_ldapKey)) {
                if (stringAttribute.toLowerCase().endsWith(securityService.userSearchBase.toLowerCase())) {
                  Pattern pattern = Pattern.compile(securityService.groupObjectId + "=([\\w\\d\\.\\@]*),");
                  Matcher matcher = pattern.matcher(stringAttribute);
                  if (matcher.find()) {
                    tmpList.add(matcher.group(1));
                  }
                }
              }
              PropertyUtils.setProperty(t, _beanKey, tmpList);
            } else {
              PropertyUtils.setSimpleProperty(t, _beanKey, dca.getStringAttribute(_ldapKey));
            }
          }
        } catch (Exception e) {
        }
      }
    }
    return t;
  }
}
