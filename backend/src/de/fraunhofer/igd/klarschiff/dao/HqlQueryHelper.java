package de.fraunhofer.igd.klarschiff.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mit Hilfe der Klasse wird die Erstellung von korrekten HQL-Anfragen vereinfacht. Dabei werden die Angaben wie Projektion (SelectAttributes),
 * Selektion (WhereConditions), Gruppierung (GroupByAtttributes) etc. separat nacheinander angegeben.
 * Die HQL-Anfrage kann dann mit der Methode <code>getHqlQuery()</code> ermittelt werden. Analog dazu kann die Anfrage an die DB direkt mit Hilfe
 * der Funktion <code>getResultList(EntityManger)</code> oder <code>getSingleResult(EntityManger)</code> gestellt werden.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class HqlQueryHelper {
	private final static Log logger = LogFactory.getLog(HqlQueryHelper.class);

	List<String> selectAttributes = new ArrayList<String>();
	Set<String> fromTables = new HashSet<String>();
	Set<String> whereConditions = new HashSet<String>();
	Set<String> groupByAttributes = new HashSet<String>();
	Set<String> havingConditions = new HashSet<String>();
	List<String> orderByAttributes = new ArrayList<String>();
	Map<String, Object> params = new Hashtable<String, Object>();
	boolean distinctEnable = false;
	Integer firstResult = null;
	Integer maxResults = null;
	String whereConditionsOperation = "AND";

	public HqlQueryHelper addSelectAttribute(String attribute) {
		selectAttributes.add(attribute);
		return this;
	}

	public HqlQueryHelper addFromTables(String table) {
		fromTables.add(table);
		return this;
	}

	public HqlQueryHelper addGroupByAttribute(String attribut) {
		groupByAttributes.add(attribut);
		return this;
	}

	public HqlQueryHelper addHavingConditions(String condition) {
		havingConditions.add(condition);
		return this;
	}
	
	public HqlQueryHelper addWhereConditions(String condition) {
		whereConditions.add(condition);
		return this;
	}

	public HqlQueryHelper setWhereConditionsOperation(String whereConditionsOperation) {
		this.whereConditionsOperation = whereConditionsOperation.trim().toUpperCase();
		return this;
	}

	public HqlQueryHelper addParameter(String key, Object value) {
		params.put(key, value);
		return this;
	}

	public HqlQueryHelper addLikeParameter(String key, String value) {
    	value = value.replace('*', '%');
        if (!value.startsWith("%")) value = "%" + value;
        if (!value.endsWith("%")) value = value + "%";

        params.put(key, value);
		return this;
	}

	public HqlQueryHelper firstResult(Integer startPosition) {
		this.firstResult = startPosition;
		return this;
	}

	public HqlQueryHelper maxResults(Integer maxResult) {
		this.maxResults = maxResult;
		return this;
	}

	public HqlQueryHelper orderBy(String attribute) {
		orderByAttributes.add(attribute);
		return this;
	}

	public Map<String, Object> getParameterMap() {
		return params;
	}

	/**
	 * Erzeugt eine gültige HQL-Anfrage
	 * @return HQL-Anfrage
	 */
	public String getHqlQuery() {
		if (selectAttributes.size()<1) throw new RuntimeException("Es sind keine Attributte für die Projektion angegeben");
		if (fromTables.size()<1) throw new RuntimeException("Es sind keine Tabellen angegeben");

		StringBuilder str = new StringBuilder();

		//SELECT
		str.append("SELECT ");
		if (distinctEnable) str.append("DISTINCT ");
		for(Iterator<String> iter=selectAttributes.iterator(); iter.hasNext(); )
		{
			str.append(iter.next());
			if (iter.hasNext()) str.append(", ");
		}

		//FROM
		str.append(" FROM ");
		for(Iterator<String> iter=fromTables.iterator(); iter.hasNext(); )
		{
			str.append(iter.next());
			if (iter.hasNext()) str.append(", ");
		}

		//WHERE
		if (!whereConditions.isEmpty())
		{
			str.append(" WHERE ");
			for(Iterator<String> iter=whereConditions.iterator(); iter.hasNext(); )
			{
				str.append(iter.next());
				if (iter.hasNext()) str.append(" "+whereConditionsOperation+" ");
			}
		}

		//GROUPBY
		if (!groupByAttributes.isEmpty())
		{
			str.append(" GROUP BY ");
			for(Iterator<String> iter=groupByAttributes.iterator(); iter.hasNext(); )
			{
				str.append(iter.next());
				if (iter.hasNext()) str.append(", ");
			}
		}

		//HAVING
		if (!havingConditions.isEmpty())
		{
			str.append(" HAVING ");
			for(Iterator<String> iter=havingConditions.iterator(); iter.hasNext(); )
			{
				str.append(iter.next());
				if (iter.hasNext()) str.append(" AND ");
			}
		}

		//ORDER BY
		if (!orderByAttributes.isEmpty())
		{
			str.append(" ORDER BY ");
			for(Iterator<String> iter=orderByAttributes.iterator(); iter.hasNext(); )
			{
				str.append(iter.next());
				if (iter.hasNext()) str.append(", ");
			}
		}

		logger.debug(str.toString());

		return str.toString();
	}

	/**
	 * Stellt die HQL-Anfrage an die DB. Dabei werden die gesetzten Parameter verwendet.
	 * @param entityManager
	 * @return Ergebnisobjekt der Anfrage
	 */
	@Transactional
	public Object getSingleResult(EntityManager entityManager) {
		Query query = entityManager.createQuery(getHqlQuery());
		for(Entry<String, Object> entry : getParameterMap().entrySet())
			query.setParameter(entry.getKey(), entry.getValue());
		if (firstResult!=null) query.setFirstResult(firstResult);
		return query.getSingleResult();
	}

	/**
	 * Stellt die HQL-Anfrage an die DB. Dabei werden die gesetzten Parameter verwendet.
	 * @param entityManager
	 * @return Liste der Ergebnisobjekte der Anfrage
	 */
	@Transactional
	public List getResultList(EntityManager entityManager) {
		Query query = entityManager.createQuery(getHqlQuery());
		for(Entry<String, Object> entry : getParameterMap().entrySet())
			query.setParameter(entry.getKey(), entry.getValue());
		if (firstResult!=null) query.setFirstResult(firstResult);
		if (maxResults!=null) query.setMaxResults(maxResults);
		return query.getResultList();
	}
}
