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
 *
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
   *
   * @return Statistik
   */
  public Statistic getStatistic() {
    Statistic statistic = new Statistic();
    Date jetzt = new Date();

    // aktive Vorgänge mit Missbrauchsmeldungen
    statistic.setVorgaengeMissbrauchsmeldungen(statisticDao.findVorgaengeMissbrauchsmeldungen());

    // neueste aktive Vorgänge
    statistic.setLastVorgaenge(statisticDao.findLastVorgaenge(5));

    // Vorgänge mit dem Status 'offen', die seit einem bestimmten Datum zugewiesen sind, bisher aber nicht akzeptiert wurden
    Date datum = DateUtils.addDays(jetzt, -3);
    statistic.setVorgaengeOffenNichtAkzeptiert(statisticDao.findVorgaengeOffenNichtAkzeptiert(datum));

    // Vorgänge mit dem Status 'in Bearbeitung', die seit einem bestimmten Datum nicht mehr verändert wurden, bisher aber keine Info der Verwaltung aufweisen
    datum = DateUtils.addDays(jetzt, -30);
    statistic.setVorgaengeInbearbeitungOhneStatusKommentar(statisticDao.findVorgaengeInbearbeitungOhneStatusKommentar(datum));

    // Vorgänge des Typs 'idee' mit dem Status 'offen', die ihre Erstsichtung seit einem bestimmten Datum hinter sich haben, bisher aber noch nicht die Zahl der notwendigen Unterstützungen aufweisen
    datum = DateUtils.addDays(jetzt, -60);
    statistic.setVorgaengeIdeeOffenOhneUnterstuetzung(statisticDao.findVorgaengeIdeeOffenOhneUnterstuetzung(datum));

    // Vorgänge mit dem Status 'wird nicht bearbeitet', die bisher keine Info der Verwaltung aufweisen
    statistic.setVorgaengeWirdnichtbearbeitetOhneStatuskommentar(statisticDao.findVorgaengeWirdnichtbearbeitetOhneStatuskommentar());

    // Vorgänge, die zwar nicht mehr den Status 'offen' aufweisen, bisher aber dennoch nicht akzeptiert wurden
    statistic.setVorgaengeNichtMehrOffenNichtAkzeptiert(statisticDao.findVorgaengeNichtMehrOffenNichtAkzeptiert());

    // Vorgänge, die ihre Erstsichtung bereits hinter sich haben, deren Betreff, Details oder Foto bisher aber noch nicht freigegeben wurden
    statistic.setVorgaengeOhneRedaktionelleFreigaben(statisticDao.findVorgaengeOhneRedaktionelleFreigaben());

    // aktive Vorgänge und deren Statusverteilung
    statistic.setStatusVerteilung(new ArrayList<StatusVerteilungEntry>());
    long countOverall = 0;
    for (Object[] o : statisticDao.getStatusVerteilung(true)) {
      StatusVerteilungEntry entry = new StatusVerteilungEntry(o);
      countOverall = countOverall + entry.getCount();
      statistic.getStatusVerteilung().add(entry);
    }
    for (StatusVerteilungEntry entry : statistic.getStatusVerteilung()) {
      entry.setCountOverall(countOverall);
    }

    return statistic;
  }
}