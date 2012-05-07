package de.fraunhofer.igd.klarschiff.dao;

import java.util.ArrayList;
import java.util.Collection;
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

/**
 * Mit Hilfe der Klasse wird die Erstellung von korrekten SQL-Anfragen vereinfacht. Dabei werden die Angaben wie Projektion (SelectAttributes),
 * Selektion (WhereConditions) etc. separat nacheinander angegeben.
 * Die SQL-Anfrage kann dann mit der Methode <code>getSqlQuery()</code> ermittelt werden. Analog dazu kann die Anfrage an die DB direkt mit Hilfe
 * der Funktion <code>getResultList(EntityManger)</code> oder <code>getSingleResult(EntityManger)</code> gestellt werden.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class SqlQueryHelper {
	private final static Log logger = LogFactory.getLog(SqlQueryHelper.class);

	Set<String> selectAttributes = new HashSet<String>();
	Set<String> fromTables = new HashSet<String>();
	Set<String> whereConditions = new HashSet<String>();
	List<String> orderByAttributes = new ArrayList<String>();
	Map<String, Object> params = new Hashtable<String, Object>();
	boolean distinctEnable = false;
	Integer firstResult = null;
	Integer maxResults = null;

	public SqlQueryHelper addSelectAttribute(String attribute) {
		selectAttributes.add(attribute);
		return this;
	}

	public SqlQueryHelper addFromTables(String table) {
		fromTables.add(table);
		return this;
	}

	public SqlQueryHelper addWhereConditions(String condition) {
		whereConditions.add(condition);
		return this;
	}

	public SqlQueryHelper addWhereInConditions(String attribute, Collection<String> values) {
		if (values.size()>0)
		{
			StringBuilder str = new StringBuilder();
			str.append(attribute+" IN (");
			for(Iterator<String> iter=values.iterator(); iter.hasNext(); )
			{
				str.append("'"+iter.next()+"'");
				if (iter.hasNext()) str.append(", ");
			}
			str.append(")");
			whereConditions.add(str.toString());
		}
		return this;
	}

	public SqlQueryHelper addParameter(String key, Object value) {
		params.put(key, value);
		return this;
	}

	public SqlQueryHelper addLikeParameter(String key, String value) {
    	value = value.replace('*', '%');
        if (!value.startsWith("%")) value = "%" + value;
        if (!value.endsWith("%")) value = value + "%";

        params.put(key, value);
		return this;
	}

	public SqlQueryHelper distinct(boolean distinctEnable) {
		this.distinctEnable = distinctEnable;
		return this;
	}

	public SqlQueryHelper firstResult(Integer startPosition) {
		this.firstResult = startPosition;
		return this;
	}

	public SqlQueryHelper maxResults(Integer maxResult) {
		this.maxResults = maxResult;
		return this;
	}

	public SqlQueryHelper orderBy(String attribute) {
		orderByAttributes.add(attribute);
		return this;
	}
	public String getSqlQuery() {
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

	public Map<String, Object> getParameterMap() {
		return params;
	}

	public Object getSingleResult(EntityManager entityManager) {
		Query query = entityManager.createNativeQuery(getSqlQuery());
		for(Entry<String, Object> entry : getParameterMap().entrySet())
			query.setParameter(entry.getKey(), entry.getValue());
		if (firstResult!=null) query.setFirstResult(firstResult);
		return query.getSingleResult();
	}


	@SuppressWarnings("unchecked")
	public List getResultList(EntityManager entityManager) {
		Query query = entityManager.createNativeQuery(getSqlQuery());
		for(Entry<String, Object> entry : getParameterMap().entrySet())
			query.setParameter(entry.getKey(), entry.getValue());
		if (firstResult!=null) query.setFirstResult(firstResult);
		if (maxResults!=null) query.setMaxResults(maxResults);
		return query.getResultList();
	}
}