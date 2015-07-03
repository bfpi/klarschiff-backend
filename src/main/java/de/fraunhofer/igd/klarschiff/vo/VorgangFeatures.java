package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * VO zum Abbilden der berechneten Features für einen Vorgang. Bei den Features handelt es sich um Features, die für
 * die Klassifikation bzw. den Zuständigkeitsfinder berechnet werden und sich nach der erstmaligen Berechnung nicht mehr
 * ändern können. Um keine Neuberechnung für diese Features bei einer erneuten Klassifikation zu verhindern werden die
 * Werte für die Features in der DB persitiert.
 * @author Stefan Audersch (Fraunhofer IGD)
 *
 */
@SuppressWarnings("serial")
@Entity
public class VorgangFeatures implements Serializable {

	/* --------------- Attribute ----------------------------*/

	/**
	 * Vorgang für den die Feature berechnet wurden
	 */
	@Id
	@OneToOne
	@JoinColumn
	Vorgang vorgang;

	/**
	 * Map mit dem Namen des Features und dem Wert für das Feature
	 */
	@ElementCollection(fetch=FetchType.EAGER)
	Map<String, String> features = new HashMap<String, String>();

	/* --------------- GET + SET ----------------------------*/

	public Vorgang getVorgang() {
		return vorgang;
	}

	public void setVorgang(Vorgang vorgang) {
		this.vorgang = vorgang;
	}

	public Map<String, String> getFeatures() {
		return features;
	}

	public void setFeatures(Map<String, String> features) {
		this.features = features;
	}
}
