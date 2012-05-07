package de.fraunhofer.igd.klarschiff.service.classification;

/**
 * Die Klasse dient als Hilfsklasse f�r die Abbildung des Ergebnisses vom Klassifikator.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class ClassificationResultEntry {

	String classValue;
	Double weight;

	public ClassificationResultEntry(String classValue, Double weight) {
		this.classValue = classValue;
		this.weight = weight;
	}
	
	/* --------------- GET + SET ----------------------------*/

	/**
	 * @return Name der Klasse (bzw. Zust�ndigkeit)
	 */
	public String getClassValue() {
		return classValue;
	}
	
	/**
	 * @return Relevanz f�r die entsprechende Klasse (Zust�ndigkeit)
	 */
	public Double getWeight() {
		return weight;
	}
}
