package de.fraunhofer.igd.klarschiff.service.job;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fraunhofer.igd.klarschiff.dao.ClusterDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.classification.ClassificationService;
import de.fraunhofer.igd.klarschiff.service.cluster.ScheduledSyncInCluster;
import de.fraunhofer.igd.klarschiff.service.mail.MailService;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.Missbrauchsmeldung;
import de.fraunhofer.igd.klarschiff.vo.Unterstuetzer;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Die Klasse stellt einen Service mit verscheidenen Hintergrundjobs bereit. Die Methoden mit den Jobs sind durch
 * die Annotation <code>@Scheduled</code> oder <code>@ScheduledSyncInCluster</code> gekennzeichnet, parametriesiert
 * und dardurch beim Start des Servers auch initialisiert.   
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Service
public class JobsService {
	private static final Logger logger = Logger.getLogger(JobsService.class);
	
	int monthsToArchivVorgaenge;
	int hoursToRemoveUnbestaetigtVorgang;
	int hoursToRemoveUnbestaetigtUnterstuetzer;
	int hoursToRemoveUnbestaetigtMissbrauchsmeldung;
	
	@Autowired
	VorgangDao vorgangDao;
	
	@Autowired
	ClusterDao clusterDao;
	
	@Autowired
	SecurityService securityService;
	
	@Autowired
	MailService mailService;
	
	@Autowired
	ClassificationService classificationService;
	

	//TEST
//	@Scheduled(cron="30 * */3 * * *")
//	public void testLdap() {
//		try {
//			securityService.getAllZustaendigkeiten(false);
//		} catch (Exception e) {
//			logger.error("ClassificationContext konnte nicht erneuert werden.", e);
//		}
//	}
	//TEST
	
	
	/**
	 * Der Job archiviert abgeschlossene Vorgänge.
	 */
	@Transactional
	@ScheduledSyncInCluster(cron="0 40 0 * * *", name="Vorgaenge archivieren")
	public void archivVorgaenge() {
		Date date = DateUtils.addMonths(new Date(), -monthsToArchivVorgaenge);
		for(Vorgang vorgang : vorgangDao.findNotArchivVorgang(date))
		{
			vorgang.setArchiviert(true);
			vorgangDao.merge(vorgang);
		}
	}

	
	/**
	 * Der Job entfernt gemeldet aber noch nicht bestätigte Vorgänge.
	 */
	@Transactional
	@ScheduledSyncInCluster(cron="0 43 * * * *", name="unbestaetigte Vorgaenge entfernen")
	public void removeUnbestaetigtVorgang() {
		Date date = DateUtils.addHours(new Date(), -hoursToRemoveUnbestaetigtVorgang);
		for(Vorgang vorgang : vorgangDao.findUnbestaetigtVorgang(date))
		{
			vorgangDao.remove(vorgang);
		}
	}

	
	/**
	 * Der Job entfernt gemeldete aber noch nicht bestätigte Unterstützungen.
	 */
	@Transactional
	@ScheduledSyncInCluster(cron="0 46 * * * *", name="unbestaetigte Unterstuetzer entfernen")
	public void removeUnbestaetigtUnterstuetzer() {
		Date date = DateUtils.addHours(new Date(), -hoursToRemoveUnbestaetigtUnterstuetzer);
		for(Unterstuetzer unterstuetzer : vorgangDao.findUnbestaetigtUnterstuetzer(date))
		{
			vorgangDao.remove(unterstuetzer);
		}
	}

	
	/**
	 * Der Job entfernt gemeldete aber noch nicht bestätigte Missbrauchsmeldungen.
	 */
	@Transactional
	@ScheduledSyncInCluster(cron="0 49 * * * *", name="unbestaetigte Missbrauchsmeldungen entfernen")
	public void removeUnbestaetigtMissbrauchsmeldung() {
		Date date = DateUtils.addHours(new Date(), -hoursToRemoveUnbestaetigtMissbrauchsmeldung);
		for(Missbrauchsmeldung missbrauchsmeldung : vorgangDao.findUnbestaetigtMissbrauchsmeldung(date))
		{
			vorgangDao.remove(missbrauchsmeldung);
		}
	}

	
	/**
	 * Der Job aktuallisiert den Klassifikationsalgorithmus für den Zuständigkeitsfinder.
	 */
	@Scheduled(cron="0 52 * * * *")
	public void reBuildClassifier() {
		try {
			Thread.sleep(new Random().nextInt(1000));
			classificationService.reBuildClassifier();
		} catch (Exception e) {
			logger.error("ClassificationContext konnte nicht erneuert werden.", e);
		}
	}

	
	/**
	 * Der Job informiert Externe Benutzer über neue an sie delegierte Vorgänge.
	 */
	@ScheduledSyncInCluster(cron="0 0 5 * * *", name="Externe ueber neue Vorgaenge informieren")
	public void informExtern() {
		Date date = DateUtils.addDays(new Date(), -1);

		//Für alle delegiertAn
		for(Role delegiertAn : securityService.getAllDelegiertAn()) {

			//Finde alle Vorgänge, dessen DelegiertAn in den letzten 24h geändert wurde und deren DelegiertAn=delegiertAn ist
			List<Vorgang> vorgaenge = vorgangDao.findVorgaengeForDelegiertAn(date, delegiertAn.getId());

			//sende eMail
			mailService.sendInformExternMail(vorgaenge, securityService.getAllUserEmailsForRole(delegiertAn.getId()));
		}
	}
	
	
	/**
	 * Der Job informiert die Dispatcher über neue nicht zuordbare Vorgänge.
	 */
	@ScheduledSyncInCluster(cron="0 5 5 * * *", name="Dispatcher ueber neue Vorgaenge informieren")
	public void informDispatcher() {
		Date date = DateUtils.addDays(new Date(), -1);
		
		//Finde alle Vorgänge, dessen Zuständigkeit in den letzten 24h geändert wurde und deren Zuständigkeit=dispatcher ist
		List<Vorgang> vorgaenge = vorgangDao.findVorgaengeForZustaendigkeit(date, securityService.getDispatcherZustaendigkeitId());
		
		//sende eMail
		mailService.sendInformDispatcherMail(vorgaenge, securityService.getAllUserEmailsForRole(securityService.getDispatcherZustaendigkeitId()));
	}
    
    /**
	 * Der Job informiert den Ersteller von Vorgängen über deren Übergang in den Status "in Bearbeitung".
	 */
	@ScheduledSyncInCluster(cron="0 5 10 * * *", name="Ersteller ueber Statusaenderung nach in Bearbeitung informieren")
	public void informErstellerInBearbeitung() {
		Date date = DateUtils.addDays(new Date(), -1);
		
		//Finde alle Vorgänge, deren Status sich in den letzten 24h auf "in Bearbeitung" geändert hat und eine autorEmail haben
		List<Vorgang> vorgaenge = vorgangDao.findInProgressVorgaenge(date);
		
		//sende eMail
		for (Vorgang vorgang : vorgaenge)
			mailService.sendInformErstellerMailInBearbeitung(vorgang);
	}

	
	/**
	 * Der Job informiert den Ersteller von Vorgängen über deren Abschluß.
	 */
	@ScheduledSyncInCluster(cron="0 5 10 * * *", name="Ersteller ueber Vorgangsabschluss informieren")
	public void informErstellerAbschluss() {
		Date date = DateUtils.addDays(new Date(), -1);
		
		//Finde alle Vorgänge, die in den letzten 24h abgeschlossen wurden und eine autorEmail haben
		List<Vorgang> vorgaenge = vorgangDao.findClosedVorgaenge(date);
		
		//sende eMail
		for (Vorgang vorgang : vorgaenge)
			mailService.sendInformErstellerMailAbschluss(vorgang);
	}


	/**
	 * Der Job registriert die aktulle ServerInstanze in der DB
	 */
	@Scheduled(fixedRate=20000)
	public void notifyAliveServer(){
		clusterDao.notifyAliveServer();
	}
	
//	@ScheduledSyncInCluster(cron="*/20 * * * * *", name="test")
//	public void test() {
//		System.out.println("TestJob wird ausgeführt.");
//	}
	
    /* --------------- GET + SET ----------------------------*/
	
	public int getMonthsToArchivVorgaenge() {
		return monthsToArchivVorgaenge;
	}

	public void setMonthsToArchivVorgaenge(int monthsToArchivVorgaenge) {
		this.monthsToArchivVorgaenge = monthsToArchivVorgaenge;
	}

	public int getHoursToRemoveUnbestaetigtVorgang() {
		return hoursToRemoveUnbestaetigtVorgang;
	}

	public void setHoursToRemoveUnbestaetigtVorgang(
			int hoursToRemoveUnbestaetigtVorgang) {
		this.hoursToRemoveUnbestaetigtVorgang = hoursToRemoveUnbestaetigtVorgang;
	}

	public int getHoursToRemoveUnbestaetigtUnterstuetzer() {
		return hoursToRemoveUnbestaetigtUnterstuetzer;
	}

	public void setHoursToRemoveUnbestaetigtUnterstuetzer(
			int hoursToRemoveUnbestaetigtUnterstuetzer) {
		this.hoursToRemoveUnbestaetigtUnterstuetzer = hoursToRemoveUnbestaetigtUnterstuetzer;
	}

	public int getHoursToRemoveUnbestaetigtMissbrauchsmeldung() {
		return hoursToRemoveUnbestaetigtMissbrauchsmeldung;
	}

	public void setHoursToRemoveUnbestaetigtMissbrauchsmeldung(
			int hoursToRemoveUnbestaetigtMissbrauchsmeldung) {
		this.hoursToRemoveUnbestaetigtMissbrauchsmeldung = hoursToRemoveUnbestaetigtMissbrauchsmeldung;
	}
}
