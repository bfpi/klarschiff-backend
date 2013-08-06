package de.fraunhofer.igd.klarschiff.service.statistic;

import java.util.List;

import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Die Klasse stellt eine Bean zum Ablegen der ermittelten Daten für die Statistik bereit.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class Statistic {
	Long countMissbrauchsmeldungen;
	List<Vorgang> vorgaengeMissbrauchsmeldungen;
	List<Vorgang> lastVorgaenge;
	List<StatusVerteilungEntry> statusVerteilung;
	
	/* --------------- GET + SET ----------------------------*/

	public Long getCountMissbrauchsmeldungen() {
		return countMissbrauchsmeldungen;
	}

	public void setCountMissbrauchsmeldungen(Long countMissbrauchsmeldungen) {
		this.countMissbrauchsmeldungen = countMissbrauchsmeldungen;
	}
    
    public List<Vorgang> getVorgaengeMissbrauchsmeldungen() {
		return vorgaengeMissbrauchsmeldungen;
	}

	public void setVorgaengeMissbrauchsmeldungen(List<Vorgang> vorgaengeMissbrauchsmeldungen) {
		this.vorgaengeMissbrauchsmeldungen = vorgaengeMissbrauchsmeldungen;
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
	
}
