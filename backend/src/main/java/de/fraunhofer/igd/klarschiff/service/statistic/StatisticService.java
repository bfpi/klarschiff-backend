package de.fraunhofer.igd.klarschiff.service.statistic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.fraunhofer.igd.klarschiff.dao.StatisticDao;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;

/**
 * Die Klasse stellt einen Service zur Ermittlung und Berechnung der Statistik bereit.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Service
public class StatisticService {
	
	@Autowired
	StatisticDao statisticDao;
	
	@Autowired
	SecurityService securityService;
	

	/**
	 * Berechnet die Statistik für den aktuell angemeldeten Benutzer.
	 * @return Statistik
	 */
	public Statistic getStatistic() {
		Statistic statistic = new Statistic();
		Date date = DateUtils.addDays(DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH), -7);
		// Anzahl der Missbrauchsmeldungen
		statistic.setCountMissbrauchsmeldungen(statisticDao.countMissbrauchsmeldungen(true));
        // Vorgänge mit Missbrauchsmeldungen
		statistic.setVorgaengeMissbrauchsmeldungen(statisticDao.findVorgaengeMissbrauchsmeldungen());
		// neue Vorgänge
		statistic.setLastVorgaenge(statisticDao.findLastVorgaenge(5));
		// StatusVerteilung
		statistic.setStatusVerteilung(new ArrayList<StatusVerteilungEntry>());
		long countOverall = 0;
		for(Object[] o : statisticDao.getStatusVerteilung(true)) {
			StatusVerteilungEntry entry = new StatusVerteilungEntry(o);
			countOverall = countOverall + entry.getCount();
			statistic.getStatusVerteilung().add(entry);
		}
		for(StatusVerteilungEntry entry : statistic.getStatusVerteilung()) entry.setCountOverall(countOverall);
		
		return statistic;
	}
}
