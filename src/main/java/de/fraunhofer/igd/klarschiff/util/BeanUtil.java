package de.fraunhofer.igd.klarschiff.util;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class BeanUtil {

	/**
	 * Ermittelt den Wert einer Property von einer Bean
	 * @param bean Bean
	 * @param property Name der Property
	 * @return Wert der Property als Object oder <code>null</code>
	 */
	public static Object getProperty(Object bean, String property)
	{
		ExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression(property);
		EvaluationContext context = new StandardEvaluationContext(bean);
		try {
			return exp.getValue(context);
		} catch (Exception e) {
			return null;
		}
	}


	/**
	 * Ermittelt den Wert einer Property von einer Bean als String
	 * @param bean Bean
	 * @param property Name der Property
	 * @return Wert der Property als String oder <code>null</code>
	 */
	public static String getPropertyString(Object bean, String property)
	{
		Object o = getProperty(bean, property);
		return (o!=null) ? o.toString() : null;
	}


	/**
	 * Ermittelt den Wert einer Property von einer Bean als Boolean
	 * @param bean Bean
	 * @param property Name der Property
	 * @return Wert der Property als Boolean oder <code>null</code>
	 */
	public static Boolean getPropertyBoolean(Object bean, String property)
	{
		Object o = getProperty(bean, property);
		if (o==null) return null;
		if (o instanceof Boolean) return (Boolean)o;
		if (o instanceof String) return Boolean.parseBoolean((String)o);
		return null;
	}
}
