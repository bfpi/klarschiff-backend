package de.fraunhofer.igd.klarschiff.service.classification;

/**
 * Die Klasse dient als Hilfsklasse für die Abbildung des Ergebnisses vom Klassifikator.
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
	 * @return Name der Klasse (bzw. Zuständigkeit)
	 */
	public String getClassValue() {
		return classValue;
	}
	
	/**
	 * @return Relevanz für die entsprechende Klasse (Zuständigkeit)
	 */
	public Double getWeight() {
		return weight;
	}
}
