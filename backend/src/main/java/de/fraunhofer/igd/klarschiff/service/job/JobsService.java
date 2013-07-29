package de.fraunhofer.igd.klarschiff.service.job;

import java.text.SimpleDateFormat;
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
import de.fraunhofer.igd.klarschiff.dao.RedaktionEmpfaengerDao;
import de.fraunhofer.igd.klarschiff.dao.RedaktionKriterienDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.classification.ClassificationService;
import de.fraunhofer.igd.klarschiff.service.cluster.ScheduledSyncInCluster;
import de.fraunhofer.igd.klarschiff.service.mail.MailService;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.Missbrauchsmeldung;
import de.fraunhofer.igd.klarschiff.vo.RedaktionEmpfaenger;
import de.fraunhofer.igd.klarschiff.vo.RedaktionKriterien;
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
	RedaktionEmpfaengerDao redaktionEmpfaengerDao;
    
    @Autowired
	RedaktionKriterienDao redaktionKriterienDao;
	
	@Autowired
	SecurityService securityService;
	
	@Autowired
	MailService mailService;
	
	@Autowired
	ClassificationService classificationService;
	

	/**
	 * Der Job archiviert abgeschlossene Vorg�nge.
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
	 * Der Job entfernt gemeldet aber noch nicht best�tigte Vorg�nge.
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
	 * Der Job entfernt gemeldete aber noch nicht best�tigte Unterst�tzungen.
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
	 * Der Job entfernt gemeldete aber noch nicht best�tigte Missbrauchsmeldungen.
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
	 * Der Job aktuallisiert den Klassifikationsalgorithmus f�r den Zust�ndigkeitsfinder.
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
	 * Der Job informiert Externe Benutzer �ber neue an sie delegierte Vorg�nge.
	 */
	@ScheduledSyncInCluster(cron="0 0 5 * * *", name="Externe ueber neue Vorgaenge informieren")
	public void informExtern() {
		Date date = DateUtils.addDays(new Date(), -1);

		//F�r alle delegiertAn
		for(Role delegiertAn : securityService.getAllDelegiertAn()) {

			//Finde alle Vorg�nge, dessen DelegiertAn in den letzten 24h ge�ndert wurde und deren DelegiertAn=delegiertAn ist
			List<Vorgang> vorgaenge = vorgangDao.findVorgaengeForDelegiertAn(date, delegiertAn.getId());

			//sende eMail
			mailService.sendInformExternMail(vorgaenge, securityService.getAllUserEmailsForRole(delegiertAn.getId()));
		}
	}
	
	
	/**
	 * Der Job informiert die Dispatcher �ber neue nicht zuordbare Vorg�nge.
	 */
	@ScheduledSyncInCluster(cron="0 5 5 * * *", name="Dispatcher ueber neue Vorgaenge informieren")
	public void informDispatcher() {
		Date date = DateUtils.addDays(new Date(), -1);
		
		//Finde alle Vorg�nge, dessen Zust�ndigkeit in den letzten 24h ge�ndert wurde und deren Zust�ndigkeit=dispatcher ist
		List<Vorgang> vorgaenge = vorgangDao.findVorgaengeForZustaendigkeit(date, securityService.getDispatcherZustaendigkeitId());
		
		//sende eMail
		mailService.sendInformDispatcherMail(vorgaenge, securityService.getAllUserEmailsForRole(securityService.getDispatcherZustaendigkeitId()));
	}
    
    /**
	 * Der Job informiert den Ersteller von Vorg�ngen �ber deren �bergang in den Status "in Bearbeitung".
	 */
	@ScheduledSyncInCluster(cron="0 5 10 * * *", name="Ersteller ueber Statusaenderung nach in Bearbeitung informieren")
	public void informErstellerInBearbeitung() {
		Date date = DateUtils.addDays(new Date(), -1);
		
		//Finde alle Vorg�nge, deren Status sich in den letzten 24h auf "in Bearbeitung" ge�ndert hat und eine autorEmail haben
		List<Vorgang> vorgaenge = vorgangDao.findInProgressVorgaenge(date);
		
		//sende eMail
		for (Vorgang vorgang : vorgaenge)
			mailService.sendInformErstellerMailInBearbeitung(vorgang);
	}

	
	/**
	 * Der Job informiert den Ersteller von Vorg�ngen �ber deren Abschlu�.
	 */
	@ScheduledSyncInCluster(cron="0 10 10 * * *", name="Ersteller ueber Vorgangsabschluss informieren")
	public void informErstellerAbschluss() {
		Date date = DateUtils.addDays(new Date(), -1);
		
		//Finde alle Vorg�nge, die in den letzten 24h abgeschlossen wurden und eine autorEmail haben
		List<Vorgang> vorgaenge = vorgangDao.findClosedVorgaenge(date);
		
		//sende eMail
		for (Vorgang vorgang : vorgaenge)
			mailService.sendInformErstellerMailAbschluss(vorgang);
	}
    
    
    /**
	 * Der Job informiert die Empf�nger redaktioneller E-Mails.
	 */
	@ScheduledSyncInCluster(cron="0 15 10 * * *", name="Empfaenger redaktioneller E-Mails informieren")
	public void informRedaktionEmpfaenger() {
    
        //Liste aller Redaktionskriterien erstellen
		List<RedaktionKriterien> kriterienAlle = redaktionKriterienDao.getKriterienList();
        
        //Liste aller Empf�nger redaktioneller E-Mails erstellen
		List<RedaktionEmpfaenger> empfaengerAlle = redaktionEmpfaengerDao.getEmpfaengerList();
        
        //lokale Variablen f�r die nachfolgende for-Schleife initiieren
        Date jetzt = new Date();
        Short tageOffenNichtAkzeptiert = 0;
        Short tageInbearbeitungOhneStatusKommentar = 0;
		Date datum;
        List<Vorgang> vorgaengeOffenNichtAkzeptiert;
        List<Vorgang> vorgaengeInbearbeitungOhneStatusKommentar;
		
        //Liste aller Empf�nger durchgehen
        for (RedaktionEmpfaenger empfaenger : empfaengerAlle) {
            
            //pr�fe Zeitstempel des letzten E-Mail-Versands an aktuellen Empf�nger: soll �berhaupt eine E-Mail geschickt werden?
            if ( (empfaenger.getLetzteMail() == null) || (DateUtils.addDays(empfaenger.getLetzteMail(), empfaenger.getTageZwischenMails()).compareTo(jetzt) <= 0) ) {
        
                //Liste aller Redaktionskriterien durchgehen
                for (RedaktionKriterien kriterium : kriterienAlle) {
                
                    //alle Redaktionskriterien der Stufe des Empf�ngers entsprechend zuweisen
                    if (kriterium.getStufe() == empfaenger.getStufe()) {
                        tageOffenNichtAkzeptiert = kriterium.getTageOffenNichtAkzeptiert();
                        tageInbearbeitungOhneStatusKommentar = kriterium.getTageInbearbeitungOhneStatusKommentar();
                        break;
                    }
                }
                
                //'datum' berechnen durch Subtrahieren von 'tageOffenNichtAkzeptiert' vom aktuellen Datum
                datum = DateUtils.addDays(jetzt, -(tageOffenNichtAkzeptiert));
            
                //finde alle Vorg�nge mit dem Status 'offen' f�r die Zust�ndigkeit des aktuellen Empf�ngers, die seit mindestens 'datum' zugewiesen sind, bisher aber nicht akzeptiert wurden
                vorgaengeOffenNichtAkzeptiert = vorgangDao.findVorgaengeOffenNichtAkzeptiert(empfaenger.getZustaendigkeit(), datum);
                
                //'datum' berechnen durch Subtrahieren von 'tageInbearbeitungOhneStatusKommentar' vom aktuellen Datum
                datum = DateUtils.addDays(jetzt, -(tageInbearbeitungOhneStatusKommentar));
            
                //finde alle Vorg�nge mit dem Status 'offen' f�r die Zust�ndigkeit des aktuellen Empf�ngers, die seit mindestens 'datum' zugewiesen sind, bisher aber nicht akzeptiert wurden
                vorgaengeInbearbeitungOhneStatusKommentar = vorgangDao.findVorgaengeInbearbeitungOhneStatusKommentar(empfaenger.getZustaendigkeit(), datum);
                
                //falls Vorg�nge existieren...
                if ( (!vorgaengeOffenNichtAkzeptiert.isEmpty()) || (!vorgaengeInbearbeitungOhneStatusKommentar.isEmpty())) {
                
                    //setzte Zeitstempel des letzten E-Mail-Versands an aktuellen Empf�nger auf aktuellen Zeitstempel
                    empfaenger.setLetzteMail(jetzt);
                    
                    //sende E-Mail an aktuellen Empf�nger
                    mailService.sendInformRedaktionEmpfaengerMail(tageOffenNichtAkzeptiert, tageInbearbeitungOhneStatusKommentar, vorgaengeOffenNichtAkzeptiert, vorgaengeInbearbeitungOhneStatusKommentar, empfaenger.getEmail());
                }
            }
        }
	}


	/**
	 * Der Job registriert die aktulle ServerInstanze in der DB
	 */
	@Scheduled(fixedRate=20000)
	public void notifyAliveServer(){
		clusterDao.notifyAliveServer();
	}
    
	
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
