package de.fraunhofer.igd.klarschiff.vo.extra;

import javax.persistence.Entity;

import org.springframework.beans.factory.annotation.Configurable;

/**
 * Hilfsklasse zum Persitieren der Werte des Enums EnumNaehereBeschreibungNotwendig in der DB
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 * @see de.fraunhofer.igd.klarschiff.vo.EnumNaehereBeschreibungNotwendig
 */
@Entity
@Configurable
public class EnumNaehereBeschreibungNotwendig extends EnumBean {

}
