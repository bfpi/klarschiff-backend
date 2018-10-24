package org.springframework.security.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.security.config.authentication.AuthenticationManagerBeanDefinitionParser;
import org.springframework.security.config.authentication.AuthenticationProviderBeanDefinitionParser;
import org.springframework.security.config.authentication.JdbcUserServiceBeanDefinitionParser;
import org.springframework.security.config.authentication.UserServiceBeanDefinitionParser;
import org.springframework.security.config.http.FilterChainMapBeanDefinitionDecorator;
import org.springframework.security.config.http.FilterInvocationSecurityMetadataSourceParser;
import org.springframework.security.config.http.HttpFirewallBeanDefinitionParser;
import org.springframework.security.config.http.HttpSecurityBeanDefinitionParser;
import org.springframework.security.config.ldap.LdapProviderBeanDefinitionParser;
import de.fraunhofer.igd.klarschiff.service.security.LdapServerBeanDefinitionParser;
import org.springframework.security.config.ldap.LdapUserServiceBeanDefinitionParser;
import org.springframework.security.config.method.GlobalMethodSecurityBeanDefinitionParser;
import org.springframework.security.config.method.InterceptMethodsBeanDefinitionDecorator;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.util.ClassUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Die Klasse überschreibt die eigentliche Klasse
 * <code>org.springframework.security.config.SecurityNamespaceHandler</code>, so dass anstatt des
 * der Klasse
 * <code>org.springframework.security.config.ldap.LdapUserServiceBeanDefinitionParser</code> zum
 * Parsen des Elements "security" mit dem Namespace
 * <code>http://www.springframework.org/schema/security</code> die eigene Implementierung
 * <code>de.fraunhofer.igd.klarschiff.service.security.LdapServerBeanDefinitionParser</code>
 * verwendet wird.
 *
 * @author Luke Taylor
 * @author Ben Alex
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public final class SecurityNamespaceHandler implements NamespaceHandler {

  private final Log logger = LogFactory.getLog(getClass());

  private final Map<String, BeanDefinitionParser> parsers = new HashMap<String, BeanDefinitionParser>();
  private final BeanDefinitionDecorator interceptMethodsBDD = new InterceptMethodsBeanDefinitionDecorator();
  private BeanDefinitionDecorator filterChainMapBDD;

  public SecurityNamespaceHandler() {
    String coreVersion = SpringSecurityCoreVersion.getVersion();

    Package pkg = SpringSecurityCoreVersion.class.getPackage();

    if (pkg == null || coreVersion == null) {
      logger.info("Couldn't determine package version information.");
      return;
    }

    String version = pkg.getImplementationVersion();
    logger.info("Spring Security 'config' module version is " + version);

    if (version.compareTo(coreVersion) != 0) {
      logger.error("You are running with different versions of the Spring Security 'core' and 'config' modules");
    }
  }

  public BeanDefinition parse(Element element, ParserContext pc) {
    if (!namespaceMatchesVersion(element)) {
      pc.getReaderContext().fatal("You must use a 3.0 schema with Spring Security 3.0."
        + "(2.0 or 3.1 versions are not valid)"
        + " Please update your schema declarations to the 3.0.3 schema (spring-security-3.0.3.xsd).", element);
    }
    String name = pc.getDelegate().getLocalName(element);
    BeanDefinitionParser parser = parsers.get(name);

    if (parser == null) {
      // SEC-1455. Load parsers when required, not just on init().
      loadParsers();
    }

    if (parser == null) {
      if (Elements.HTTP.equals(name) || Elements.FILTER_SECURITY_METADATA_SOURCE.equals(name)) {
        reportMissingWebClasses(name, pc, element);
      } else {
        reportUnsupportedNodeType(name, pc, element);
      }
    }

    return parser.parse(element, pc);
  }

  public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext pc) {
    BeanDefinitionDecorator decorator = null;
    String name = pc.getDelegate().getLocalName(node);

    // We only handle elements
    if (node instanceof Element) {
      if (Elements.INTERCEPT_METHODS.equals(name)) {
        return interceptMethodsBDD.decorate(node, definition, pc);
      }

      if (Elements.FILTER_CHAIN_MAP.equals(name)) {
        if (filterChainMapBDD == null) {
          loadParsers();
        }
        if (filterChainMapBDD == null) {
          reportMissingWebClasses(name, pc, node);
        }
        return filterChainMapBDD.decorate(node, definition, pc);
      }
    }

    if (decorator == null) {
      reportUnsupportedNodeType(name, pc, node);
    }

    return null;
  }

  private void reportUnsupportedNodeType(String name, ParserContext pc, Node node) {
    pc.getReaderContext().fatal("Security namespace does not support decoration of "
      + (node instanceof Element ? "element" : "attribute") + " [" + name + "]", node);
  }

  private void reportMissingWebClasses(String nodeName, ParserContext pc, Node node) {
    pc.getReaderContext().fatal("spring-security-web classes are not available. "
      + "You need these to use <" + Elements.FILTER_CHAIN_MAP + ">", node);
  }

  public void init() {
    loadParsers();
  }

  /**
   * In der Methode wird als Parser für das Element <code>ldap-server</code> die eigene
   * Implementierung
   * <code>de.fraunhofer.igd.klarschiff.service.security.LdapServerBeanDefinitionParser</code> statt
   * die Implementierung
   * <code>org.springframework.security.config.ldap.LdapUserServiceBeanDefinitionParser</code>
   * registiert.
   */
  @SuppressWarnings("deprecation")
  private void loadParsers() {
    // Parsers
    parsers.put(Elements.LDAP_PROVIDER, new LdapProviderBeanDefinitionParser());
    //Hier wurde der LdapServerBeanDefinitionParser auf die eignen Implementierung gesetzt
    parsers.put(Elements.LDAP_SERVER, new LdapServerBeanDefinitionParser());
    parsers.put(Elements.LDAP_USER_SERVICE, new LdapUserServiceBeanDefinitionParser());
    parsers.put(Elements.USER_SERVICE, new UserServiceBeanDefinitionParser());
    parsers.put(Elements.JDBC_USER_SERVICE, new JdbcUserServiceBeanDefinitionParser());
    parsers.put(Elements.AUTHENTICATION_PROVIDER, new AuthenticationProviderBeanDefinitionParser());
    parsers.put(Elements.GLOBAL_METHOD_SECURITY, new GlobalMethodSecurityBeanDefinitionParser());
    parsers.put(Elements.AUTHENTICATION_MANAGER, new AuthenticationManagerBeanDefinitionParser());

    // Only load the web-namespace parsers if the web classes are available
    if (ClassUtils.isPresent("org.springframework.security.web.FilterChainProxy", getClass().getClassLoader())) {
      parsers.put(Elements.HTTP, new HttpSecurityBeanDefinitionParser());
      parsers.put(Elements.HTTP_FIREWALL, new HttpFirewallBeanDefinitionParser());
      parsers.put(Elements.FILTER_INVOCATION_DEFINITION_SOURCE, new FilterInvocationSecurityMetadataSourceParser());
      parsers.put(Elements.FILTER_SECURITY_METADATA_SOURCE, new FilterInvocationSecurityMetadataSourceParser());
      filterChainMapBDD = new FilterChainMapBeanDefinitionDecorator();
    }
  }

  /**
   * Check that the schema location declared in the source file being parsed matches the Spring
   * Security version. The old 2.0 schema is not compatible with the new 3.0 parser, so it is an
   * error to explicitly use 3.0. It might be an error to declare spring-security.xsd as an alias,
   * but you are only going to find that out when one of the sub parsers breaks.
   *
   * @param element the element that is to be parsed next
   * @return true if we find a schema declaration that matches
   */
  private boolean namespaceMatchesVersion(Element element) {
    return matchesVersionInternal(element) && matchesVersionInternal(element.getOwnerDocument().getDocumentElement());
  }

  private boolean matchesVersionInternal(Element element) {
    String schemaLocation = element.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
    return schemaLocation.matches("(?m).*spring-security-3\\.0.*xsd.*")
      || schemaLocation.matches("(?m).*spring-security\\.xsd.*")
      || !schemaLocation.matches("(?m).*spring-security.*");
  }

}
