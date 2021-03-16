package de.fraunhofer.igd.klarschiff.service.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.joda.time.*;
import de.fraunhofer.igd.klarschiff.dao.ClusterDao;
import de.fraunhofer.igd.klarschiff.dao.RedaktionEmpfaengerDao;
import de.fraunhofer.igd.klarschiff.dao.RedaktionKriterienDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.classification.ClassificationService;
import de.fraunhofer.igd.klarschiff.service.cluster.ScheduledSyncInCluster;
import de.fraunhofer.igd.klarschiff.service.mail.MailService;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.Foto;
import de.fraunhofer.igd.klarschiff.vo.Missbrauchsmeldung;
import de.fraunhofer.igd.klarschiff.vo.RedaktionEmpfaenger;
import de.fraunhofer.igd.klarschiff.vo.RedaktionKriterien;
import de.fraunhofer.igd.klarschiff.vo.Unterstuetzer;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Die Klasse stellt einen Service mit verscheidenen Hintergrundjobs bereit. Die Methoden mit den
 * Jobs sind durch die Annotation <code>@Scheduled</code> oder <code>@ScheduledSyncInCluster</code>
 * gekennzeichnet, parametriesiert und dardurch beim Start des Servers auch initialisiert.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Service
public class JobsService {

  private static final Logger logger = Logger.getLogger(JobsService.class);

  int monthsToArchivProbleme;
  int monthsToArchivIdeen;
  int hoursToRemoveUnbestaetigtVorgang;
  int hoursToRemoveUnbestaetigtUnterstuetzer;
  int hoursToRemoveUnbestaetigtMissbrauchsmeldung;
  int hoursToRemoveUnbestaetigtFoto;
  boolean removeAuthorEmailFromArchiv;
  String removeAuthorEmailFromArchivReplacement;

  @Autowired
  VorgangDao vorgangDao;

  @Autowired
  ClusterDao clusterDao;

  @Autowired
  RedaktionEmpfaengerDao redaktionEmpfaengerDao;

  @Autowired
  RedaktionKriterienDao redaktionKriterienDao;

  @Autowired
  SettingsService settingsService;

  @Autowired
  SecurityService securityService;

  @Autowired
  MailService mailService;

  @Autowired
  ClassificationService classificationService;

  /**
   * Dieser Job löscht alle Vorgänge, die gemeldet, aber nach einem bestimmten Zeitraum noch nicht
   * bestätigt wurden.
   */
  @Transactional
  @ScheduledSyncInCluster(cron = "0 32 * * * *", name = "unbestaetigte Vorgaenge loeschen")
  public void removeUnbestaetigtVorgang() {
    Date date = DateUtils.addHours(new Date(), -hoursToRemoveUnbestaetigtVorgang);
    for (Vorgang vorgang : vorgangDao.findUnbestaetigtVorgang(date)) {
      for (Foto foto : vorgangDao.findFotosZuUnbestaetigtVorgang(date)) {
        vorgangDao.remove(foto);
      }
      vorgangDao.remove(vorgang);
    }
  }

  /**
   * Dieser Job löscht alle Unterstützungen, die eingegangen sind, aber nach einem bestimmten
   * Zeitraum noch nicht bestätigt wurden.
   */
  @Transactional
  @ScheduledSyncInCluster(cron = "0 37 * * * *", name = "unbestaetigte Unterstuetzungen loeschen")
  public void removeUnbestaetigtUnterstuetzer() {
    Date date = DateUtils.addHours(new Date(), -hoursToRemoveUnbestaetigtUnterstuetzer);
    for (Unterstuetzer unterstuetzer : vorgangDao.findUnbestaetigtUnterstuetzer(date)) {
      vorgangDao.remove(unterstuetzer);
    }
  }

  /**
   * Dieser Job löscht alle Missbrauchsmeldungen, die eingegangen sind, aber nach einem bestimmten
   * Zeitraum noch nicht bestätigt wurden.
   */
  @Transactional
  @ScheduledSyncInCluster(cron = "0 42 * * * *", name = "unbestaetigte Missbrauchsmeldungen loeschen")
  public void removeUnbestaetigtMissbrauchsmeldung() {
    Date date = DateUtils.addHours(new Date(), -hoursToRemoveUnbestaetigtMissbrauchsmeldung);
    for (Missbrauchsmeldung missbrauchsmeldung : vorgangDao.findUnbestaetigtMissbrauchsmeldung(date)) {
      vorgangDao.remove(missbrauchsmeldung);
    }
  }

  /**
   * Dieser Job löscht alle Fotos, die eingegangen sind, aber nach einem bestimmten Zeitraum noch
   * nicht bestätigt wurden.
   */
  @Transactional
  @ScheduledSyncInCluster(cron = "0 47 * * * *", name = "unbestaetigte Fotos loeschen")
  public void removeUnbestaetigtFoto() {
    Date date = DateUtils.addHours(new Date(), -hoursToRemoveUnbestaetigtFoto);
    for (Foto foto : vorgangDao.findUnbestaetigtFoto(date)) {
      vorgangDao.remove(foto);
    }
  }

  /**
   * Dieser Job aktualisiert den Klassifikator für den Zuständigkeitsfinder.
   */
  @Scheduled(cron = "0 52 * * * *")
  public void reBuildClassifier() {
    try {
      Thread.sleep(new Random().nextInt(1000));
      classificationService.reBuildClassifier();
    } catch (Exception e) {
      logger.error("ClassificationContext konnte nicht erneuert werden.", e);
    }
  }

  /**
   * Archiviert alle Vorgänge, die abgeschlossen sind und seit einem bestimmten Zeitraum
   * nicht mehr bearbeitet wurden.
   *
   */
  @Transactional
  @ScheduledSyncInCluster(cron = "0 40 00 * * *", name = "abgeschlossene Vorgaenge archivieren")
  public void archivVorgaenge() {
    archivVorgaengeByTyp(monthsToArchivProbleme, EnumVorgangTyp.problem);
    archivVorgaengeByTyp(monthsToArchivIdeen, EnumVorgangTyp.idee);
    if (removeAuthorEmailFromArchiv) {
      for (Vorgang vorgang : vorgangDao.findArchivVorgangWithEmail(removeAuthorEmailFromArchivReplacement)) {
        vorgang.setAutorEmail(removeAuthorEmailFromArchivReplacement);
        vorgangDao.merge(vorgang);
      }
    }
  }

  private void archivVorgaengeByTyp(int months, EnumVorgangTyp typ) {
    Date dateP = DateUtils.addMonths(new Date(), -months);
    for (Vorgang vorgang : vorgangDao.findNotArchivVorgang(typ, dateP)) {
      vorgang.setArchiviert(true);
      vorgangDao.merge(vorgang);
      for(Unterstuetzer unterstuetzer : vorgang.getUnterstuetzer()) {
        if(null != unterstuetzer.getEmail() && unterstuetzer.getEmail().length() > 0) {
          unterstuetzer.setEmail(removeAuthorEmailFromArchivReplacement);
          vorgangDao.merge(vorgang);
        }
      }
    }
  }

  /**
   * Dieser Job informiert die Empfänger redaktioneller E-Mails.
   */
  @ScheduledSyncInCluster(cron = "0 40 01 * * *", name = "Empfaenger redaktioneller E-Mails informieren")
  public void informRedaktionEmpfaenger() {

    try {

      //lokale Variablen initiieren
      Boolean administrator = false;
      Date jetzt = new Date();
      Short tageOffenNichtAkzeptiert = 0;
      Short tageInbearbeitungOhneStatusKommentar = 0;
      Short tageIdeeOffenOhneUnterstuetzung = 0;
      Short tageInBearbeitung = 0;
      Boolean sollVorgaengeNichtLoesbarOhneStatuskommentar = false;
      Boolean sollVorgaengeNichtMehrOffenNichtAkzeptiert = false;
      Boolean sollVorgaengeOhneRedaktionelleFreigaben = false;
      Boolean sollVorgaengeOhneZustaendigkeit = false;
      Date datum = null;
      List<Vorgang> vorgaengeOffenNichtAkzeptiert = new ArrayList<Vorgang>();
      List<Vorgang> vorgaengeInbearbeitungOhneStatusKommentar = new ArrayList<Vorgang>();
      List<Vorgang> vorgaengeIdeeOffenOhneUnterstuetzung = new ArrayList<Vorgang>();
      List<Vorgang> vorgaengeInBearbeitung = new ArrayList<Vorgang>();
      List<Vorgang> vorgaengeNichtLoesbarOhneStatuskommentar = new ArrayList<Vorgang>();
      List<Vorgang> vorgaengeNichtMehrOffenNichtAkzeptiert = new ArrayList<Vorgang>();
      List<Vorgang> vorgaengeOhneRedaktionelleFreigaben = new ArrayList<Vorgang>();
      List<Vorgang> vorgaengeOhneZustaendigkeit = new ArrayList<Vorgang>();

      //Liste aller Redaktionskriterien erstellen
      List<RedaktionKriterien> kriterienAlle = redaktionKriterienDao.getKriterienList();

      //Liste aller Empfänger redaktioneller E-Mails erstellen
      List<RedaktionEmpfaenger> empfaengerAlle = redaktionEmpfaengerDao.getEmpfaengerList();

      //Liste aller Empfänger durchgehen
      for (RedaktionEmpfaenger empfaenger : empfaengerAlle) {

        //Ist der aktuelle Empfänger als Admininstrator definiert?
        if (empfaenger.getZustaendigkeit().toLowerCase().contains("admin".toLowerCase())) {
          administrator = true;
        } else {
          administrator = false;
        }

        //prüfe Zeitstempel des letzten E-Mail-Versands an aktuellen Empfänger: soll überhaupt eine E-Mail geschickt werden?
        if ((empfaenger.getLetzteMail() == null) || (Days.daysBetween(new DateTime(empfaenger.getLetzteMail()),
          new DateTime(jetzt)).getDays() >= empfaenger.getTageZwischenMails())) {

          //Liste aller Redaktionskriterien durchgehen
          for (RedaktionKriterien kriterium : kriterienAlle) {

            //alle Redaktionskriterien der Stufe des Empfängers entsprechend zuweisen
            if (Objects.equals(kriterium.getStufe(), empfaenger.getStufe())) {
              tageOffenNichtAkzeptiert = kriterium.getTageOffenNichtAkzeptiert();
              tageInbearbeitungOhneStatusKommentar = kriterium.getTageInbearbeitungOhneStatusKommentar();
              tageIdeeOffenOhneUnterstuetzung = kriterium.getTageIdeeOffenOhneUnterstuetzung();
              tageInBearbeitung = kriterium.getTageInBearbeitung();
              sollVorgaengeNichtLoesbarOhneStatuskommentar = kriterium.getNichtLoesbarOhneStatuskommentar();
              sollVorgaengeNichtMehrOffenNichtAkzeptiert = kriterium.getNichtMehrOffenNichtAkzeptiert();
              sollVorgaengeOhneRedaktionelleFreigaben = kriterium.getOhneRedaktionelleFreigaben();
              sollVorgaengeOhneZustaendigkeit = kriterium.getOhneZustaendigkeit();
              break;
            }
          }

          //'datum' berechnen durch Subtrahieren von 'tageOffenNichtAkzeptiert' vom aktuellen Datum
          datum = DateUtils.addDays(jetzt, -(tageOffenNichtAkzeptiert));

          //finde alle Vorgänge mit dem Status 'offen' für die Zuständigkeit des aktuellen Empfängers, die seit mindestens 'datum' zugewiesen sind, bisher aber nicht akzeptiert wurden
          vorgaengeOffenNichtAkzeptiert = vorgangDao.findVorgaengeOffenNichtAkzeptiert(administrator, empfaenger.getZustaendigkeit(), datum);

          //'datum' berechnen durch Subtrahieren von 'tageInbearbeitungOhneStatusKommentar' vom aktuellen Datum
          datum = DateUtils.addDays(jetzt, -(tageInbearbeitungOhneStatusKommentar));

          //finde alle Vorgänge mit dem Status 'offen' für die Zuständigkeit des aktuellen Empfängers, die seit mindestens 'datum' zugewiesen sind, bisher aber nicht akzeptiert wurden
          vorgaengeInbearbeitungOhneStatusKommentar = vorgangDao.findVorgaengeInbearbeitungOhneStatusKommentar(administrator, empfaenger.getZustaendigkeit(), datum);

          //'datum' berechnen durch Subtrahieren von 'tageIdeeOffenOhneUnterstuetzung' vom aktuellen Datum
          datum = DateUtils.addDays(jetzt, -(tageIdeeOffenOhneUnterstuetzung));

          //finde alle Vorgänge des Typs 'idee' mit dem Status 'offen', die ihre Erstsichtung seit mindestens 'datum' hinter sich haben, bisher aber noch nicht die Zahl der notwendigen Unterstützungen aufweisen
          vorgaengeIdeeOffenOhneUnterstuetzung = vorgangDao.findVorgaengeIdeeOffenOhneUnterstuetzung(administrator, empfaenger.getZustaendigkeit(), datum);

          //'datum' berechnen durch Subtrahieren von 'tageInBearbeitung' vom aktuellen Datum
          datum = DateUtils.addDays(jetzt, -(tageInBearbeitung));
          //finde alle Vorgänge die seit mindestens 'datum' im System sind und noch im Status 'inBearbeitung'
          vorgaengeInBearbeitung = vorgangDao.findVorgaengeInBearbeitung(administrator, empfaenger.getZustaendigkeit(), datum);

          //falls dies gemacht werden soll...
          if (sollVorgaengeNichtLoesbarOhneStatuskommentar == true) {
            //finde alle Vorgänge mit dem Status 'nicht lösbar'/'wird nicht bearbeitet', die bisher keine öffentliche Statusinformation aufweisen
            vorgaengeNichtLoesbarOhneStatuskommentar = vorgangDao.findVorgaengeNichtLoesbarOhneStatuskommentar(administrator, empfaenger.getZustaendigkeit());
          }

          //falls dies gemacht werden soll...
          if (sollVorgaengeNichtMehrOffenNichtAkzeptiert == true) {
            //finde alle Vorgänge, die zwar nicht mehr den Status 'offen' aufweisen, bisher aber dennoch nicht akzeptiert wurden
            vorgaengeNichtMehrOffenNichtAkzeptiert = vorgangDao.findVorgaengeNichtMehrOffenNichtAkzeptiert(administrator, empfaenger.getZustaendigkeit());
          }

          //falls dies gemacht werden soll...
          if (sollVorgaengeOhneRedaktionelleFreigaben == true) {
            //finde alle Vorgänge, die ihre Erstsichtung bereits hinter sich haben, deren Beschreibung oder Foto bisher aber noch nicht freigegeben wurden
            vorgaengeOhneRedaktionelleFreigaben = vorgangDao.findVorgaengeOhneRedaktionelleFreigaben(administrator, empfaenger.getZustaendigkeit());
          }

          //falls dies gemacht werden soll...
          if (sollVorgaengeOhneZustaendigkeit == true) {
            //finde alle Vorgänge, die auf Grund von Kommunikationsfehlern im System keine Einträge in den Datenfeldern 'zustaendigkeit' und/oder 'zustaendigkeit_status' aufweisen
            vorgaengeOhneZustaendigkeit = vorgangDao.findVorgaengeOhneZustaendigkeit(administrator);
          }

          //falls Vorgänge existieren...
          if ((!vorgaengeOffenNichtAkzeptiert.isEmpty()) || (!vorgaengeInbearbeitungOhneStatusKommentar.isEmpty()) || (!vorgaengeIdeeOffenOhneUnterstuetzung.isEmpty()) || (!vorgaengeInBearbeitung.isEmpty()) || (!vorgaengeNichtLoesbarOhneStatuskommentar.isEmpty()) || (!vorgaengeNichtMehrOffenNichtAkzeptiert.isEmpty()) || (!vorgaengeOhneRedaktionelleFreigaben.isEmpty()) || (!vorgaengeOhneZustaendigkeit.isEmpty())) {

            //setzte Zeitstempel des letzten E-Mail-Versands an aktuellen Empfänger auf aktuellen Zeitstempel
            empfaenger.setLetzteMail(jetzt);

            //sende E-Mail an aktuellen Empfänger
            mailService.sendInformRedaktionEmpfaengerMail(tageOffenNichtAkzeptiert, tageInbearbeitungOhneStatusKommentar, tageIdeeOffenOhneUnterstuetzung, tageInBearbeitung, vorgaengeOffenNichtAkzeptiert, vorgaengeInbearbeitungOhneStatusKommentar, vorgaengeIdeeOffenOhneUnterstuetzung, vorgaengeInBearbeitung, vorgaengeNichtLoesbarOhneStatuskommentar, vorgaengeNichtMehrOffenNichtAkzeptiert, vorgaengeOhneRedaktionelleFreigaben, vorgaengeOhneZustaendigkeit, empfaenger.getEmail(), empfaenger.getZustaendigkeit());

            redaktionEmpfaengerDao.merge(empfaenger);
          }
        }
      }
    } catch (Exception e) {
      logger.error("Job zum Informieren der Empfaenger redaktioneller E-Mails wurde nicht ausgefuehrt.", e);
    }
  }

  /**
   * Dieser Job erstellt statische Dateien als Übersicht von aktuell aktiven Vorgängen
   */
  @ScheduledSyncInCluster(cron = "0 05 02 * * *", name = "Erstellt Übersicht von aktuell aktiven Vorgängen")
  public void createRequestOverview() {
    RequestOverview ro = new RequestOverview();
    ro.create(settingsService, vorgangDao);
  }

  /**
   * Dieser Job informiert externe Nutzer mittels E-Mail über diejenigen Vorgänge, die innerhalb der
   * letzten 24 Stunden an sie delegiert wurden.
   */
  @ScheduledSyncInCluster(cron = "0 00 07 * * *", name = "externe Nutzer ueber neue Vorgaenge informieren")
  public void informExtern() {
    Date date = DateUtils.addDays(new Date(), -1);

    // für alle delegiertAn
    for (Role delegiertAn : securityService.getAllDelegiertAn(false)) {

      // finde alle Vorgänge, deren DelegiertAn in den letzten 24 Stunden geändert wurde und deren DelegiertAn gleich delegiertAn ist
      List<Vorgang> vorgaenge = vorgangDao.findVorgaengeForDelegiertAn(date, delegiertAn.getId());

      // falls Vorgänge gefunden wurden
      if (!vorgaenge.isEmpty() && vorgaenge != null) {

        // sende E-Mail
        mailService.sendInformExternMail(vorgaenge, securityService.getAllExternUserEmailsForRole(delegiertAn.getId()));
      }
    }
  }

  /**
   * Dieser Job informiert die Dispatcher mittels E-Mail über diejenigen Vorgänge, die innerhalb der
   * letzten 24 Stunden durch wiederholtes automatisches Zuweisung keiner Zuständigkeit zugeordnet
   * werden konnten und somit letztendlich der Dispatcher-Gruppe zugewiesen wurden.
   */
  @ScheduledSyncInCluster(cron = "0 05 07 * * *", name = "Dispatcher ueber neue Vorgaenge informieren")
  public void informDispatcher() {
    Date date = DateUtils.addDays(new Date(), -1);

    // finde alle Vorgänge, deren Zuständigkeit in den letzten 24 Stunden geändert wurde und deren Zuständigkeit gleich dispatcher ist
    List<Vorgang> vorgaenge = vorgangDao.findVorgaengeForZustaendigkeit(date, securityService.getDispatcherZustaendigkeitId());

    // sende E-Mail
    mailService.sendInformDispatcherMail(vorgaenge, securityService.getAllUserEmailsForRole(securityService.getDispatcherZustaendigkeitId()));
  }

  /**
   * Dieser Job informiert die Ersteller von Vorgängen darüber, dass ihre Vorgänge innerhalb der
   * letzten 24 Stunden in Bearbeitung genommen wurden.
   */
  @ScheduledSyncInCluster(cron = "0 05 10 * * *", name = "Ersteller ueber Statusaenderungen nach in Bearbeitung informieren")
  public void informErstellerInBearbeitung() {
    Date date = DateUtils.addDays(new Date(), -1);

    // finde alle Vorgänge, deren Status sich innerhalb der letzten 24 Stunden auf inBearbeitung geändert hat und die eine autorEmail aufweisen
    List<Vorgang> vorgaenge = vorgangDao.findInProgressVorgaenge(date);

    // sende E-Mail
    for (Vorgang vorgang : vorgaenge) {
      mailService.sendInformErstellerMailInBearbeitung(vorgang);
    }
  }

  /**
   * Dieser Job informiert die Ersteller von Vorgängen darüber, dass ihre Vorgänge innerhalb der
   * letzten 24 Stunden abgeschlossen wurden.
   */
  @ScheduledSyncInCluster(cron = "0 10 10 * * *", name = "Ersteller ueber Vorgangsabschluesse informieren")
  public void informErstellerAbschluss() {
    Date date = DateUtils.addDays(new Date(), -1);

    // finde alle Vorgänge, die innerhalb der letzten 24 Stunden abgeschlossen wurden und die eine autorEmail aufweisen
    List<Vorgang> vorgaenge = vorgangDao.findClosedVorgaenge(date);

    // sende E-Mail
    for (Vorgang vorgang : vorgaenge) {
      mailService.sendInformErstellerMailAbschluss(vorgang);
    }
  }

  /**
   * Die Ersteller von Vorgängen werden darüber informiert, dass ihre Vorgänge innerhalb der letzten
   * 14 Tage noch nicht abschließend bearbeitet wurden.
   */
  @ScheduledSyncInCluster(cron = "0 25 10 * * *", name = "Ersteller ueber anhaltende Bearbeitungszeit informieren")
  public void informErstellerLangeInBearbeitung() {
    Date date_to = DateUtils.addDays(new Date(), -14);
    Date date_from = DateUtils.addDays(date_to, -1);

    // finde alle Vorgänge, deren Status sich innerhalb der letzten 24 Stunden vor 14 Tagen auf inBearbeitung geändert hat und die eine autorEmail aufweisen
    List<Vorgang> vorgaenge = vorgangDao.findLongTimeInProgressVorgaenge(date_from, date_to);

    // sende E-Mail
    for (Vorgang vorgang : vorgaenge) {
      mailService.sendInformErstellerMailLangeInBearbeitung(vorgang);
    }
  }

  /**
   * Dieser Job informiert die Unterstuetzer von Vorgängen darüber, dass ihre Vorgänge innerhalb der
   * letzten 24 Stunden in Bearbeitung genommen wurden.
   */
  @ScheduledSyncInCluster(cron = "0 15 10 * * *", name = "Unterstuetzer ueber Statusaenderungen nach in Bearbeitung informieren")
  public void informUnterstuetzerInBearbeitung() {
    Date date = DateUtils.addDays(new Date(), -1);

    // finde alle Vorgänge, deren Status sich innerhalb der letzten 24 Stunden auf inBearbeitung geändert hat und die eine autorEmail aufweisen
    List<Unterstuetzer> unterstuetzer = vorgangDao.findInProgressVorgaengeUnterstuetzerToInform(date);

    // sende E-Mail
    for (Unterstuetzer u : unterstuetzer) {
      mailService.sendInformUnterstuetzerMailInBearbeitung(u);
    }
  }

  /**
   * Dieser Job informiert die Unterstuetzer von Vorgängen darüber, dass ihre Vorgänge innerhalb der
   * letzten 24 Stunden abgeschlossen wurden.
   */
  @ScheduledSyncInCluster(cron = "0 20 10 * * *", name = "Unterstuetzer ueber Vorgangsabschluesse informieren")
  public void informUnterstuetzerAbschluss() {
    Date date = DateUtils.addDays(new Date(), -1);

    // finde alle Vorgänge, die innerhalb der letzten 24 Stunden abgeschlossen wurden und die eine autorEmail aufweisen
    List<Unterstuetzer> unterstuetzer = vorgangDao.findClosedVorgaengeToInform(date);

    // sende E-Mail
    for (Unterstuetzer u : unterstuetzer) {
      mailService.sendInformUnterstuetzerMailAbschluss(u);
    }
  }

  /**
   * Dieser Job registriert die aktulle ServerInstanze in der DB
   */
  @Scheduled(fixedRate = 20000)
  public void notifyAliveServer() {
    clusterDao.notifyAliveServer();
  }

  public int getMonthsToArchivProbleme() {
    return monthsToArchivProbleme;
  }

  public void setMonthsToArchivProbleme(int monthsToArchivProbleme) {
    this.monthsToArchivProbleme = monthsToArchivProbleme;
  }

  public int getMonthsToArchivIdeen() {
    return monthsToArchivIdeen;
  }

  public void setMonthsToArchivIdeen(int monthsToArchivIdeen) {
    this.monthsToArchivIdeen = monthsToArchivIdeen;
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

  public int getHoursToRemoveUnbestaetigtFoto() {
    return hoursToRemoveUnbestaetigtFoto;
  }

  public void setHoursToRemoveUnbestaetigtFoto(int hoursToRemoveUnbestaetigtFoto) {
    this.hoursToRemoveUnbestaetigtFoto = hoursToRemoveUnbestaetigtFoto;
  }

  public boolean isRemoveAuthorEmailFromArchiv() {
    return removeAuthorEmailFromArchiv;
  }

  public void setRemoveAuthorEmailFromArchiv(boolean removeAuthorEmailFromArchiv) {
    this.removeAuthorEmailFromArchiv = removeAuthorEmailFromArchiv;
  }

  public String getRemoveAuthorEmailFromArchivReplacement() {
    return removeAuthorEmailFromArchivReplacement;
  }

  public void setRemoveAuthorEmailFromArchivReplacement(String removeAuthorEmailFromArchivReplacement) {
    this.removeAuthorEmailFromArchivReplacement = removeAuthorEmailFromArchivReplacement;
  }
}
