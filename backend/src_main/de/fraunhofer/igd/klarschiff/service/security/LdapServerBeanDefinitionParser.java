package de.fraunhofer.igd.klarschiff.service.security;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import de.fraunhofer.igd.klarschiff.service.settings.PropertyPlaceholderConfigurer;


/**
 * Die Klasse erweitert die Klasse <code>org.springframework.security.config.ldap.LdapServerBeanDefinitionParser</code>,
 * so dass durch einfache Konfiguration zwischen einem embedded gestartetem LDAP-Server und einem entfernten LDAP-Server
 * gewechselt werden kann. <br/>
 * Wenn in der <code>settings.properties</code> der Wert der Variable <code>ldap.server.ldif</code> leer ist,
 * wird ein entfernter LDAP-server verwendet. Ansonsten wird mit der im Paramter angegebenen LDIF-Datei ein
 * embedded LDAP-Server gestartet.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class LdapServerBeanDefinitionParser extends org.springframework.security.config.ldap.LdapServerBeanDefinitionParser {

	/**
	 * Entfernt die Attribute <code>ldif</code> bzw. <code>port</code> und <code>url</code> aus der Beandefinition
	 * anhand des Status der Variable <code>ldap.server.ldif</code> in den Properties, so dass nur die eine oder 
	 * die andere Variante verwendet wird.
	 */
	@Override
	public BeanDefinition parse(Element elt, ParserContext parserContext) {
		try {
			if (StringUtils.isBlank(PropertyPlaceholderConfigurer.getPropertyValue("ldap.server.ldif"))) {
				//Connect to LDAP host
				elt.removeAttribute("ldif");
			} else {
				//Embedded LDAP
				elt.removeAttribute("port");
				elt.removeAttribute("url");
			}
			
			return super.parse(elt, parserContext);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
