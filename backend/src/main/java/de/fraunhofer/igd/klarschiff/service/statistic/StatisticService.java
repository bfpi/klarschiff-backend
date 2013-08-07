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
	 * Berechnet die Statistik f�r den aktuell angemeldeten Benutzer.
	 * @return Statistik
	 */
	public Statistic getStatistic() {
		Statistic statistic = new Statistic();
        Date jetzt = new Date();
        
		// aktive Vorg�nge mit Missbrauchsmeldungen
		statistic.setVorgaengeMissbrauchsmeldungen(statisticDao.findVorgaengeMissbrauchsmeldungen());
        
		// neueste aktive Vorg�nge
		statistic.setLastVorgaenge(statisticDao.findLastVorgaenge(5));
        
        // Vorg�nge mit dem Status 'offen', die seit einem bestimmten Datum zugewiesen sind, bisher aber nicht akzeptiert wurden
        Date datum = DateUtils.addDays(jetzt, -3);
		statistic.setVorgaengeOffenNichtAkzeptiert(statisticDao.findVorgaengeOffenNichtAkzeptiert(datum));
        
        // Vorg�nge mit dem Status 'in Bearbeitung', die seit einem bestimmten Datum nicht mehr ver�ndert wurden, bisher aber keine Info der Verwaltung aufweisen
        datum = DateUtils.addDays(jetzt, -30);
		statistic.setVorgaengeInbearbeitungOhneStatusKommentar(statisticDao.findVorgaengeInbearbeitungOhneStatusKommentar(datum));
        
        // Vorg�nge des Typs 'idee' mit dem Status 'offen', die ihre Erstsichtung seit einem bestimmten Datum hinter sich haben, bisher aber noch nicht die Zahl der notwendigen Unterst�tzungen aufweisen
        datum = DateUtils.addDays(jetzt, -60);
		statistic.setVorgaengeIdeeOffenOhneUnterstuetzung(statisticDao.findVorgaengeIdeeOffenOhneUnterstuetzung(datum));
        
        // Vorg�nge mit dem Status 'wird nicht bearbeitet', die bisher keine Info der Verwaltung aufweisen
		statistic.setVorgaengeWirdnichtbearbeitetOhneStatuskommentar(statisticDao.findVorgaengeWirdnichtbearbeitetOhneStatuskommentar());
        
        // Vorg�nge, die zwar nicht mehr den Status 'offen' aufweisen, bisher aber dennoch nicht akzeptiert wurden
		statistic.setVorgaengeNichtMehrOffenNichtAkzeptiert(statisticDao.findVorgaengeNichtMehrOffenNichtAkzeptiert());
        
        // Vorg�nge, die ihre Erstsichtung bereits hinter sich haben, deren Betreff, Details oder Foto bisher aber noch nicht freigegeben wurden
		statistic.setVorgaengeOhneRedaktionelleFreigaben(statisticDao.findVorgaengeOhneRedaktionelleFreigaben());
        
		// aktive Vorg�nge und deren Statusverteilung
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
