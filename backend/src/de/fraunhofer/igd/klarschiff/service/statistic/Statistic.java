package de.fraunhofer.igd.klarschiff.service.statistic;

import java.util.List;

import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Die Klasse stellt eine Bean zum Ablegen der ermittelten Daten für die Statistik bereit.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class Statistic {
	Long countNewVorgaenge;
	Long countFixedVorgaenge;
	Long countMissbrauchsmeldungen;
	List<Vorgang> lastVorgaenge;
	List<StatusVerteilungEntry> statusVerteilung;
	List<StatusVerteilungEntry> allStatusVerteilung;
	
	/* --------------- GET + SET ----------------------------*/

	public Long getCountNewVorgaenge() {
		return countNewVorgaenge;
	}

	public void setCountNewVorgaenge(Long countNewVorgaenge) {
		this.countNewVorgaenge = countNewVorgaenge;
	}

	public Long getCountFixedVorgaenge() {
		return countFixedVorgaenge;
	}

	public void setCountFixedVorgaenge(Long countFixedVorgaenge) {
		this.countFixedVorgaenge = countFixedVorgaenge;
	}

	public Long getCountMissbrauchsmeldungen() {
		return countMissbrauchsmeldungen;
	}

	public void setCountMissbrauchsmeldungen(Long countMissbrauchsmeldungen) {
		this.countMissbrauchsmeldungen = countMissbrauchsmeldungen;
	}

	public List<Vorgang> getLastVorgaenge() {
		return lastVorgaenge;
	}

	public void setLastVorgaenge(List<Vorgang> lastVorgaenge) {
		this.lastVorgaenge = lastVorgaenge;
	}

	public List<StatusVerteilungEntry> getStatusVerteilung() {
		return statusVerteilung;
	}

	public void setStatusVerteilung(List<StatusVerteilungEntry> statusVerteilung) {
		this.statusVerteilung = statusVerteilung;
	}

	public List<StatusVerteilungEntry> getAllStatusVerteilung() {
		return allStatusVerteilung;
	}

	public void setAllStatusVerteilung(List<StatusVerteilungEntry> allStatusVerteilung) {
		this.allStatusVerteilung = allStatusVerteilung;
	}
	
	
}
