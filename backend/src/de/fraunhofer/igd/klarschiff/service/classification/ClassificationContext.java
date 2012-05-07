package de.fraunhofer.igd.klarschiff.service.classification;

import java.util.Map;

import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.FastVector;
import weka.core.Instances;

/**
 * Die Klasse hält den Kontext für den Klassifikator. Somit kann mit einer Instanz des Klassifikators noch gearbeitet werden,
 * währenddessen im Hintergrund der Klassifikator bereits neu initialisiert und trainiert wird.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class ClassificationContext {

	private NaiveBayesUpdateable classifier;
	private Instances dataset;
	private FastVector attributes;
	private Attribute classAttribute;
	private Map<String, Attribute> attributMap;
	
	/* --------------- GET + SET ----------------------------*/

	public NaiveBayesUpdateable getClassifier() {
		return classifier;
	}
	public void setClassifier(NaiveBayesUpdateable classifier) {
		this.classifier = classifier;
	}
	public Instances getDataset() {
		return dataset;
	}
	public void setDataset(Instances dataset) {
		this.dataset = dataset;
	}
	public FastVector getAttributes() {
		return attributes;
	}
	public void setAttributes(FastVector attributes) {
		this.attributes = attributes;
	}
	public Attribute getClassAttribute() {
		return classAttribute;
	}
	public void setClassAttribute(Attribute classAttribute) {
		this.classAttribute = classAttribute;
	}
	public Map<String, Attribute> getAttributMap() {
		return attributMap;
	}
	public void setAttributMap(Map<String, Attribute> attributMap) {
		this.attributMap = attributMap;
	}

	
}
