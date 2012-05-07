package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * VO zum Abbilden der bereits einem Zugang zugeordneten Zust�ndigkeiten bzw. Klassen bei der Klassifikation. Die bereits
 * verwendeten Zust�ndigkeiten f�r eine Vorgang werden verwendet, damit beim zust�ndigkeitsinder ein Vorgang nicht
 * wiederholt die gleiche Zust�ndigkeit zugeordnet wird. 
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
@Entity
public class VorgangHistoryClasses implements Serializable {

	/* --------------- Attribute ----------------------------*/

	/**
	 * Vorgang f�r die die bereits verwendeten Zust�ndigkeiten abgelegt werden.
	 */
	@Id
	@OneToOne
	@JoinColumn
	Vorgang vorgang;

	/**
	 * Liste von bereits verwendeten Zust�ndigkeiten
	 */
	@ElementCollection(fetch=FetchType.EAGER)
	Set<String> historyClasses = new HashSet<String>();

	/* --------------- GET + SET ----------------------------*/

	public Vorgang getVorgang() {
		return vorgang;
	}

	public void setVorgang(Vorgang vorgang) {
		this.vorgang = vorgang;
	}

	public Set<String> getHistoryClasses() {
		return historyClasses;
	}

	public void setHistoryClasses(Set<String> historyClasses) {
		this.historyClasses = historyClasses;
	}
}
