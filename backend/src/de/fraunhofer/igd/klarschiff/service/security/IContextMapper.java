package de.fraunhofer.igd.klarschiff.service.security;

/**
 * Interface eines Mappers zum Mappen von Daten aus dem LDAP auf eine Bean
 * @author Stefan Audersch (Fraunhofer IGD)
 * @param <T> Klasse der Bean, auf die die Daten aus dem LDAP gemappt werden sollen.
 */
public interface IContextMapper<T> extends org.springframework.ldap.core.ContextMapper {

}
