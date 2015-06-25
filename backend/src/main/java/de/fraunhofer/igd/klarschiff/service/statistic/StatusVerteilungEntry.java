package de.fraunhofer.igd.klarschiff.service.statistic;

import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;

/**
 * Die Klasse stellt eine Bean zum Ablegen der ermittelten Daten für die Statistik bereit.
 * @author Stefan Audersch (Fraunhofer IGD)
 *
 */
public class StatusVerteilungEntry {
	EnumVorgangStatus status;
	long count;
	long ratio;
	
	/**
	 * Erzeugt die Bean wobei die Werte aus dem Object[] auf die Variablen <code>status</code> und <code>count</code> abgebildet werden. 
	 * @param values Object[0] - EnumVorgangStatus als String; Object[1] - count als Long
	 */
	protected StatusVerteilungEntry(Object[] values) {
		status = (EnumVorgangStatus)values[0];
		count = (Long)values[1];
	}
	
	
	/**
	 * Durch das Setzen der Gesamtanzahl wird das Verhältnis (<code>ratio</code>) berechnet.
	 * @param countOverall Gesamtanzahl der Vorgänge für ein Status
	 */
	protected void setCountOverall(long countOverall) {
		ratio = Math.round(100d/countOverall*count);
	}
	
	/* --------------- GET + SET ----------------------------*/

	public EnumVorgangStatus getStatus() {
		return status;
	}
	public long getCount() {
		return count;
	}
	public long getRatio() {
		return ratio;
	}
}
