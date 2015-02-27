package de.fraunhofer.igd.klarschiff.web;

import static de.fraunhofer.igd.klarschiff.util.BeanUtil.getProperty;
import static de.fraunhofer.igd.klarschiff.util.BeanUtil.getPropertyString;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindingResult;


/**
 * Hilfsklasse um die Definition von Validation zu vereinfachen
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class Assert
{
	/**
	 * Legt fest ob eine Prüfung abhängig von den bereits durchgeführten Prüfungen durchgeführt werden soll.<br/>
	 * @author Stefan Audersch (Fraunhofer IGD)
	 */
	public enum EvaluateOn { 
		/**
		 * Prüfung wird immer auasgeführt 
		 */
		ever, 
		
		/**
		 * wenn ein Fehler bereits vorhanden ist, wird die Prüfung nicht ausgeführt
		 */
		firstError, 
		
		/**
		 * wenn ein Fehler für das gleiche Property bereits vorhanden ist, wird die Prüfung nicht ausgeführt
		 */
		firstPropertyError }


	/**
	 * Fehler, wenn der Wert leer ist
	 * @param bean Bean mit den Werten
	 * @param result Bindingresult mit den Fehlermeldungen
	 * @param evaluateOn siehe EvaluateOn
	 * @param property Name des zu prüfenden Properties
	 * @param errorMessage Fehlermeldung, die bei einem Fehler verwendet werden soll
	 */
	public static void assertNotEmpty(Object bean, BindingResult result, EvaluateOn evaluateOn, String property, String errorMessage)
	{
		if (!evaluate(evaluateOn, result, property)) return;
		if (isEmpty(getProperty(bean, property))) addErrorMessage(result, property, errorMessage);
	}

	
	/**
	 * Fehler, wenn der Wert leer ist
	 * @param bean Bean mit den Werten
	 * @param result Bindingresult mit den Fehlermeldungen
	 * @param evaluateOn siehe EvaluateOn
	 * @param property Name des zu prüfenden Properties
	 * @param errorProperty Property auf das das evaluateOn angewendet wird und an das ggf. die Fehlermeldung gebunden wird
	 * @param errorMessage Fehlermeldung, die bei einem Fehler verwendet werden soll
	 */
	public static void assertNotEmpty(Object bean, BindingResult result, EvaluateOn evaluateOn, String property, String errorProperty, String errorMessage)
	{
		if (!evaluate(evaluateOn, result, errorProperty)) return;
		if (isEmpty(getProperty(bean, property))) addErrorMessage(result, errorProperty, errorMessage);
	}


	/**
	 * Fehler, wenn die Werte unterschiedlich sind
	 * @param bean Bean mit den Werten
	 * @param result Bindingresult mit den Fehlermeldungen
	 * @param evaluateOn siehe EvaluateOn
	 * @param property Name des zu prüfenden Properties
	 * @param value Vergleichswert
	 * @param errorProperty Property auf das das evaluateOn angewendet wird und an das ggf. die Fehlermeldung gebunden wird
	 * @param errorMessage Fehlermeldung, die bei einem Fehler verwendet werden soll
	 */
	public static void assertEquals(Object bean, BindingResult result, EvaluateOn evaluateOn, String property, Object value, String errorProperty, String errorMessage)
	{
		if (!evaluate(evaluateOn, result, errorProperty)) return;
		if (!isEquals(getProperty(bean, property), value)) addErrorMessage(result, errorProperty, errorMessage);
	}


	/**
	 * Fehler, wenn die Werte unterschiedlich sind
	 * @param bean Bean mit den Werten
	 * @param result Bindingresult mit den Fehlermeldungen
	 * @param evaluateOn siehe EvaluateOn
	 * @param property1 Name des zu prüfenden Properties
	 * @param property2 Name des zu prüfenden Properties
	 * @param propertyError Property auf das das evaluateOn angewendet wird und an das ggf. die Fehlermeldung gebunden wird
	 * @param errorMessage Fehlermeldung, die bei einem Fehler verwendet werden soll
	 */
	public static void assertEqualsPassword(Object bean, BindingResult result, EvaluateOn evaluateOn, String property1, String property2, String propertyError, String errorMessage)
	{
		if (!evaluate(evaluateOn, result, property1)) return;
		if (!evaluate(evaluateOn, result, property2)) return;
		if (isEmpty(getProperty(bean, property1))) return;
		if (isEmpty(getProperty(bean, property2))) return;

		try {
			if (!getProperty(bean, property1).equals(getProperty(bean, property2))) throw new Exception();
		} catch (Exception e) {
			addErrorMessage(result, propertyError, errorMessage);
			addErrorMessage(result, property1, errorMessage);
			addErrorMessage(result, property2, errorMessage);
		}
	}


	/**
	 * Fehler, wenn der Wert zu lang ist
	 * @param bean Bean mit den Werten
	 * @param result Bindingresult mit den Fehlermeldungen
	 * @param evaluateOn siehe EvaluateOn
	 * @param property Name des zu prüfenden Properties
	 * @param length maximale Zeichenlänge
	 * @param errorMessage Fehlermeldung, die bei einem Fehler verwendet werden soll
	 */
	public static void assertMaxLength(Object bean, BindingResult result, EvaluateOn evaluateOn, String property, int length, String errorMessage)
	{
		if (!evaluate(evaluateOn, result, property)) return;
		if (isEmpty(getProperty(bean, property))) return;
		try {
			if (getPropertyString(bean, property).length()>length) throw new Exception();
		} catch (Exception e) {
			addErrorMessage(result, property, errorMessage);
		}
		
	}
	
	
	/**
	 * Fehler, wenn der Wert keine PLZ ist
	 * @param bean Bean mit den Werten
	 * @param result Bindingresult mit den Fehlermeldungen
	 * @param evaluateOn siehe EvaluateOn
	 * @param property Name des zu prüfenden Properties
	 * @param errorMessage Fehlermeldung, die bei einem Fehler verwendet werden soll
	 */
	public static void assertPlz(Object bean, BindingResult result, EvaluateOn evaluateOn, String property, String errorMessage)
	{
		assertPattern(bean, result, evaluateOn, property, "\\d\\d\\d\\d\\d[\\s]*", errorMessage);
	}


	/**
	 * Fehler, wenn der Wert keine E-Mail-Adresse ist
	 * @param bean Bean mit den Werten
	 * @param result Bindingresult mit den Fehlermeldungen
	 * @param evaluateOn siehe EvaluateOn
	 * @param property Name des zu prüfenden Properties
	 * @param errorMessage Fehlermeldung, die bei einem Fehler verwendet werden soll
	 */
	public static void assertEmail(Object bean, BindingResult result, EvaluateOn evaluateOn, String property, String errorMessage)
	{
		assertPattern(bean, result, evaluateOn, property, "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", errorMessage);
	}


	/**
	 * Fehler, wenn die Werte nicht dem Pattern entspricht
	 * @param bean Bean mit den Werten
	 * @param result Bindingresult mit den Fehlermeldungen
	 * @param evaluateOn siehe EvaluateOn
	 * @param property Name des zu prüfenden Properties
	 * @param pattern Regulärer Ausdruck
	 * @param errorMessage Fehlermeldung, die bei einem Fehler verwendet werden soll
	 */
	public static void assertPattern(Object bean, BindingResult result, EvaluateOn evaluateOn, String property, String pattern, String errorMessage)
	{
		if (!evaluate(evaluateOn, result, property)) return;
		if (isEmpty(getProperty(bean, property))) return;
		try {
			if (!matches(getPropertyString(bean, property), pattern)) throw new Exception();
		} catch (Exception e) {
			addErrorMessage(result, property, errorMessage);
		}
	}


	/* ########################### Hilfsfunktionen zum validieren ***************************** */

	/**
	 * Testet eine String nach einem vorgegebenen regulären Ausdruck
	 * @param str zu prüfender String
	 * @param pattern regulärer Ausdruck 
	 * @return <code>true</code> wenn der reguläre Ausdruck passt
	 */
	public static boolean matches(String str, String pattern)
	{
       Pattern p = Pattern.compile(pattern);
       Matcher m = p.matcher(str);
       return m.matches();
	}


	/**
	 * Testet ob ein Object leer ist. String nur aus Leerzeichen oder leere Collection sind dabei ebenfalls leer
	 * @param o zu prüfendes Objekt
	 * @return <code>true</code> - es ist leer
	 */
	@SuppressWarnings("unchecked")
	private static boolean isEmpty(Object o)
	{
		if (o==null) return true;
		if (o instanceof String)
			if (StringUtils.isBlank((String)o)) return true;
			else return false;
		if (o instanceof Collection)
			if (((Collection)o).isEmpty()) return true;
			else return false;
		if (o instanceof Object[])
			if (((Object[])o).length==0) return true;
			else return false;
		return false;
	}

	
	/**
	 * Testet ob zwei Objekte gleich sind
	 * @param o1 Objekt 1
	 * @param o2 Objekt 2
	 * @return <code>true</code> - Objekte sind gleich
	 */
	private static boolean isEquals(Object o1, Object o2)
	{
		if (o1==o2) return true;
		if (o1==null || o2==null) return false;
		return o1.equals(o2);
	}

	/**
	 * Testet ob eine Property einer Bean leer ist
	 * @param bean Bean
	 * @param property Name der Property
	 * @return <code>true</code> - Wert der Property ist leer
	 */
	public static boolean isEmpty(Object bean, String property)
	{
		return isEmpty(getProperty(bean, property));
	}


	/* ########################### Prüfung, ob eine Validierung stattfinden soll ***************************** */


	/**
	 * Prüft ob eine Validierung mit den den Parametern durchgeführt werden soll
	 * @param evaluateOn siehe EvaluateOn
	 * @param result Bindingresult mit den Fehlermeldungen
	 * @param property Property auf das das evaluateOn angewendet wird
	 * @return <code>true</code> - Prüfung soll angewendet werden
	 */
	private static boolean evaluate(EvaluateOn evaluateOn, BindingResult result, String property)
	{
		switch(evaluateOn) {
			case ever:
				return true;
			case firstError:
				return !result.hasErrors();
			case firstPropertyError:
				return !result.hasFieldErrors(property);
		}
		return true;
	}


	/**
	 * Sind bereit Fehlermeldungen im Bindingresult vorhanden?
	 * @param result Bindingresult mit den Fehlermeldungen
	 * @return <code>true</code> - Bindingresult beinhaltet bereits Fehlermeldungen
	 */
	public static boolean hasError(BindingResult result)
	{
		return result.hasErrors();
	}

	
	/**
	 * Sind bereit Fehlermeldungen für ein Property im Bindingresult vorhanden?
	 * @param result Bindingresult mit den Fehlermeldungen
	 * @param property Property für das nach Fehlern gesucht werden soll
	 * @return <code>true</code> - Bindingresult beinhaltet bereits Fehlermeldungen für das Property
	 */
	public static boolean hasError(BindingResult result, String property)
	{
		return result.hasFieldErrors(property);
	}


	/* ########################### Hinzufügen von Fehlernachrichten ***************************** */


	/**
	 * Fügt eine Fehlernachricht dem BindingResult hinzu
	 * @param result Bindingresult mit den Fehlermeldungen
	 * @param property Property an das die Fehlermeldung gebunden werden soll
	 * @param errorMessage Fehlermeldung
	 */
	public static void addErrorMessage(BindingResult result, String property, String errorMessage)
	{
		result.rejectValue(property, "error" ,errorMessage==null ? "Error" : errorMessage);
	}

	
	/**
	 * Fügt eine Fehlernachricht dem BindingResult hinzu
	 * @param result Bindingresult mit den Fehlermeldungen
	 * @param evaluateOn siehe EvaluateOn
	 * @param property Property auf das das evaluateOn angewendet wird und an das ggf. die Fehlermeldung gebunden wird
	 * @param errorMessage Fehlermeldung
	 */
	public static void addErrorMessage(BindingResult result, EvaluateOn evaluateOn, String property, String errorMessage)
	{
		if (!evaluate(evaluateOn, result, property)) return;
		result.rejectValue(property, "error" ,errorMessage==null ? "Error" : errorMessage);
	}
}
