package de.fraunhofer.igd.klarschiff.vo.extra;

import javax.persistence.Entity;

import org.springframework.beans.factory.annotation.Configurable;

/**
 * Hilfsklasse zum Persitieren der Werte des Enums EnumVorgangStatus in der DB
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 * @see de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus
 */
@Entity
@Configurable
public class EnumVorgangStatus extends EnumBean {

}
