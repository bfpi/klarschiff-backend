package de.fraunhofer.igd.klarschiff.vo.extra;

import javax.persistence.Entity;

import org.springframework.beans.factory.annotation.Configurable;

/**
 * Hilfsklasse zum Persitieren der Werte des Enums EnumPrioritaet in der DB
 * @author Stefan Audersch (Fraunhofer IGD)
 * @see de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet
 */
@Entity
@Configurable
public class EnumPrioritaet extends EnumBean {

}
