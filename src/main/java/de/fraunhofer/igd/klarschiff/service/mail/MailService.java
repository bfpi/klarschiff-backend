package de.fraunhofer.igd.klarschiff.service.mail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import de.fraunhofer.igd.klarschiff.dao.KommentarDao;
import de.fraunhofer.igd.klarschiff.dao.LobHinweiseKritikDao;
import de.fraunhofer.igd.klarschiff.dao.VerlaufDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.job.JobExecutorService;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.vo.Foto;
import de.fraunhofer.igd.klarschiff.vo.Kommentar;
import de.fraunhofer.igd.klarschiff.vo.LobHinweiseKritik;
import de.fraunhofer.igd.klarschiff.vo.Missbrauchsmeldung;
import de.fraunhofer.igd.klarschiff.vo.Unterstuetzer;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import java.nio.file.Paths;

/**
 * Diese Klasse stellt einen Service bereit, über den E-Mails erstellt und versendet werden können.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class MailService {

  @Autowired
  ServletContext servletContext;

  @Autowired
  JobExecutorService jobExecutorService;

  @Autowired
  SecurityService securityService;

  @Autowired
  GeoService geoService;

  @Autowired
  KommentarDao kommentarDao;

  @Autowired
  LobHinweiseKritikDao lobHinweiseKritikDao;

  @Autowired
  VorgangDao vorgangDao;

  @Autowired
  VerlaufDao verlaufDao;

  @Autowired
  SettingsService settingsService;

  JavaMailSender mailSender;
  String serverBaseUrlBackend;
  String serverBaseUrlFrontend;

  String mailFrom;

  String sendAllMailsTo;

  String mailtoMailclientEncoding = "UTF-8";

  SimpleMailMessage vorgangBestaetigungMailTemplate;
  SimpleMailMessage unterstuetzungBestaetigungMailTemplate;
  SimpleMailMessage missbrauchsmeldungBestaetigungMailTemplate;
  SimpleMailMessage fotoBestaetigungMailTemplate;
  SimpleMailMessage vorgangWeiterleitenMailTemplate;
  SimpleMailMessage informDispatcherMailTemplate;
  SimpleMailMessage informExternMailTemplate;
  SimpleMailMessage informErstellerMailInBearbeitungTemplate;
  SimpleMailMessage informErstellerMailLangeInBearbeitungTemplate;
  SimpleMailMessage informErstellerMailAbschlussTemplate;
  SimpleMailMessage informUnterstuetzerMailInBearbeitungTemplate;
  SimpleMailMessage informUnterstuetzerMailAbschlussTemplate;
  SimpleMailMessage kriteriumOffenNichtAkzeptiertTemplate;
  SimpleMailMessage kriteriumOffenInbearbeitungOhneStatusKommentarTemplate;
  SimpleMailMessage kriteriumIdeeOffenOhneUnterstuetzungTemplate;
  SimpleMailMessage kriteriumInBearbeitungTemplate;
  SimpleMailMessage kriteriumNichtLoesbarOhneStatuskommentarTemplate;
  SimpleMailMessage kriteriumNichtMehrOffenNichtAkzeptiertTemplate;
  SimpleMailMessage kriteriumOhneRedaktionelleFreigabenTemplate;
  SimpleMailMessage kriteriumOhneZustaendigkeitTemplate;
  SimpleMailMessage informRedaktionEmpfaengerMailTemplate;

  private final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");

  /**
   * Erstellt und versendet eine E-Mail mit Lob, Hinweise oder Kritik von Bürger/-innen zu einem
   * Vorgang an den letzten Bearbeiter des Vorgangs
   *
   * @param vorgang Vorgang, zu dem die E-Mail versendet werden soll
   * @param absender Absender der Mail
   * @param empfaenger Empfänger der Mail
   * @param freitext Inhalt der Mail
   */
  public void sendLobHinweiseKritikMail(Vorgang vorgang, String absender, String empfaenger, String freitext) {
    try {
      MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mailSender.createMimeMessage(), true);
      mimeMessageHelper.setSubject(settingsService.getContextAppTitle()
        + ": Lob, Hinweise oder Kritik von Bürger/-in zu Vorgang " + vorgang.getId());
      mimeMessageHelper.setFrom(getMailFrom());
      mimeMessageHelper.setTo(empfaenger);
      mimeMessageHelper.setBcc(absender);
      mimeMessageHelper.setReplyTo(absender);
      mimeMessageHelper.setText(freitext);
      jobExecutorService.runJob(new MailSenderJob(this, mimeMessageHelper.getMimeMessage()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Erstellt und versendet eine E-Mail zur Bestätigung eines Vorganges
   *
   * @param vorgang Vorgang, zu dem die E-Mail versendet werden soll
   */
  public void sendVorgangBestaetigungMail(Vorgang vorgang) {
    SimpleMailMessage msg = new SimpleMailMessage(vorgangBestaetigungMailTemplate);
    msg.setSubject(msg.getSubject());
    msg.setTo(vorgang.getAutorEmail());
    msg.setSubject(msg.getSubject().replaceAll("%id%", vorgang.getId().toString()).replaceAll(
      "%title%", settingsService.getContextAppTitle()));
    String mailText = msg.getText();
    mailText = mailText.replaceAll("%title%", settingsService.getContextAppTitle());
    mailText = mailText.replaceAll("%id%", vorgang.getId().toString());
    mailText = mailText.replaceAll("%baseUrlFrontend%", getServerBaseUrlFrontend());
    mailText = mailText.replaceAll("%hash%", vorgang.getHash());
    StringBuilder str = new StringBuilder();
    str.append(geoService.getMapExternExternUrl(vorgang));
    mailText = mailText.replaceAll("%meldungLink%", str.toString());
    msg.setText(mailText);
    jobExecutorService.runJob(new MailSenderJob(this, msg));
  }

  /**
   * Erstellt und versendet eine E-Mail zur Bestätigung einer Unterstützung
   *
   * @param unterstuetzer Unterstützung, zu der die E-Mail versendet werden soll
   * @param email E-Mail-Adresse, an die die E-Mail versendet werden soll
   * @param vorgang Vorgang-Id
   */
  public void sendUnterstuetzerBestaetigungMail(Unterstuetzer unterstuetzer, String email, Long vorgang) {
    SimpleMailMessage msg = new SimpleMailMessage(unterstuetzungBestaetigungMailTemplate);
    msg.setTo(email);
    msg.setSubject(msg.getSubject().replaceAll("%id%", vorgang.toString()).replaceAll(
      "%title%", settingsService.getContextAppTitle()));
    String mailText = msg.getText();
    mailText = mailText.replaceAll("%title%", settingsService.getContextAppTitle());
    mailText = mailText.replaceAll("%id%", vorgang.toString());
    mailText = mailText.replaceAll("%baseUrlFrontend%", getServerBaseUrlFrontend());
    mailText = mailText.replaceAll("%hash%", unterstuetzer.getHash());
    msg.setText(mailText);
    jobExecutorService.runJob(new MailSenderJob(this, msg));
  }

  /**
   * Erstellt und versendet eine E-Mail zur Bestätigung einer Missbrauchsmeldung
   *
   * @param missbrauchsmeldung Missbrauchsmeldung, zu der die E-Mail versendet werden soll
   * @param email E-Mail-Adresse, an die die E-Mail versendet werden soll
   * @param vorgang Vorgang-Id
   */
  public void sendMissbrauchsmeldungBestaetigungMail(Missbrauchsmeldung missbrauchsmeldung, String email, Long vorgang) {
    SimpleMailMessage msg = new SimpleMailMessage(missbrauchsmeldungBestaetigungMailTemplate);
    msg.setTo(email);
    msg.setSubject(msg.getSubject().replaceAll("%id%", vorgang.toString()).replaceAll("%title%",
      settingsService.getContextAppTitle()));
    String mailText = msg.getText();
    mailText = mailText.replaceAll("%title%", settingsService.getContextAppTitle());
    mailText = mailText.replaceAll("%id%", vorgang.toString());
    mailText = mailText.replaceAll("%baseUrlFrontend%", getServerBaseUrlFrontend());
    mailText = mailText.replaceAll("%hash%", missbrauchsmeldung.getHash());
    msg.setText(mailText);
    jobExecutorService.runJob(new MailSenderJob(this, msg));
  }

  /**
   * Erstellt und versendet eine E-Mail zur Bestätigung eines neuen Fotos
   *
   * @param foto Foto, zu der die E-Mail versendet werden soll
   * @param email E-Mail-Adresse, an die die E-Mail versendet werden soll
   * @param vorgang Vorgang-Id
   */
  public void sendFotoBestaetigungMail(Foto foto, String email, Long vorgang) {
    SimpleMailMessage msg = new SimpleMailMessage(fotoBestaetigungMailTemplate);
    msg.setTo(email);
    msg.setSubject(msg.getSubject().replaceAll("%id%", vorgang.toString()).replaceAll("%title%",
      settingsService.getContextAppTitle()));
    String mailText = msg.getText();
    mailText = mailText.replaceAll("%title%", settingsService.getContextAppTitle());
    mailText = mailText.replaceAll("%id%", vorgang.toString());
    mailText = mailText.replaceAll("%baseUrlFrontend%", getServerBaseUrlFrontend());
    mailText = mailText.replaceAll("%hash%", foto.getHash());
    msg.setText(mailText);
    jobExecutorService.runJob(new MailSenderJob(this, msg));
  }

  /**
   * Erstellt und versendet eine E-Mail mit den Daten eines Vorgangs
   *
   * @param vorgang Vorgang, zu dem die E-Mail versendet werden soll
   * @param fromEmail E-Mail-Adresse des Absenders
   * @param toEmail E-Mail-Adresse, an die die E-Mail versendet werden soll
   * @param text Freitext, der in die E-Mail aufgenommen wird
   * @param sendAutor Soll die Angabe des Autors mitgesendet werden?
   * @param sendKarte Soll ein Link für die Karte mitgesendet werden?
   * @param sendKommentare Sollen die internen Kommentare mitgesendet werden?
   * @param sendLobHinweiseKritik Sollen Lob, Hinweise oder Kritik von Bürger/-innen mitgesendet
   * werden?
   * @param sendFoto Soll das Foto als Anhang mitgesendet werden?
   * @param sendMissbrauchsmeldungen Sollen die Missbrauchsmeldungen mitgesendet werden?
   */
  public void sendVorgangWeiterleitenMail(Vorgang vorgang, String fromEmail, String toEmail, String text,
    boolean sendAutor, boolean sendKarte, boolean sendKommentare, boolean sendLobHinweiseKritik,
    boolean sendFoto, boolean sendMissbrauchsmeldungen) {
    try {
      MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mailSender.createMimeMessage(), true);
      mimeMessageHelper.setSubject(vorgangWeiterleitenMailTemplate.getSubject());
      mimeMessageHelper.setFrom(getMailFrom());
      mimeMessageHelper.setTo(toEmail);
      mimeMessageHelper.setBcc(securityService.getCurrentUser().getEmail());
      mimeMessageHelper.setReplyTo(securityService.getCurrentUser().getEmail());

      String mailText = composeVorgangWeiterleitenMail(vorgang, text, sendAutor, sendKarte, sendKommentare,
        sendLobHinweiseKritik, sendMissbrauchsmeldungen);

      mimeMessageHelper.setText(mailText);

      if (sendFoto && vorgang.getFotoNormal() != null) {
        mimeMessageHelper.addAttachment("foto.jpg",
          Paths.get(settingsService.getPropertyValue("image.path"),
            vorgang.getFotoNormal()).toFile());
      }

      jobExecutorService.runJob(new MailSenderJob(this, mimeMessageHelper.getMimeMessage()));

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Erstellt den Text einer E-Mail zum Weiterleiten eines Vorgangs
   *
   * @param vorgang Vorgang zu dem die E-Mail erstellt werden soll.
   * @param text Freitextfeld, der in die E-Mail aufgenommen wird.
   * @param sendAutor Soll der Autor in der E-Mail aufgenommen werden?
   * @param sendKarte Soll ein Link für die Karte in der E-Mail erzeugt werden?
   * @param sendKommentare Sollen die Kommentare in der E-Mail aufgenommen werden?
   * @param sendLobHinweiseKritik Sollen Lob, Hinweise oder Kritik von Bürger/-innen mitgesendet
   * werden?
   * @param sendMissbrauchsmeldungen sollen die Missbrauchsmeldungen in der E-Mail aufgenommen
   * werden?
   * @return Text der erzeugten E-Mail
   */
  public String composeVorgangWeiterleitenMail(Vorgang vorgang, String text, boolean sendAutor,
    boolean sendKarte, boolean sendKommentare, boolean sendLobHinweiseKritik,
    boolean sendMissbrauchsmeldungen) throws RuntimeException {

    try {
      String mailText = vorgangWeiterleitenMailTemplate.getText();
      mailText = mailText.replaceAll("%title%", settingsService.getContextAppTitle());
      mailText = mailText.replaceAll("%id%", vorgang.getId().toString());
      mailText = mailText.replaceAll("%absender%", securityService.getCurrentUser().getName());
      mailText = mailText.replaceAll("%text%", text);
      StringBuilder str = new StringBuilder();
      str.append("Typ: ");
      str.append(vorgang.getTyp().getText());
      str.append("\n");

      str.append("ID: ");
      str.append(vorgang.getId());
      str.append("\n");

      str.append("Hauptkategorie: ");
      str.append(vorgang.getKategorie().getParent().getName());
      str.append("\n");

      str.append("Unterkategorie: ");
      str.append(vorgang.getKategorie().getName());
      str.append("\n");

      if (vorgang.getBeschreibung() != null && !vorgang.getBeschreibung().isEmpty()) {
        str.append("Beschreibung: ");
        str.append(vorgang.getBeschreibung());
        str.append("\n");
        str.append("Beschreibung Freigabestatus: ");
        str.append(vorgang.getBeschreibungFreigabeStatus().toString());
        str.append("\n");
      }

      if (vorgang.getAdresse() != null && !vorgang.getAdresse().isEmpty()) {
        str.append("Adresse: ");
        str.append(vorgang.getAdresse());
        str.append("\n");
      }

      if (vorgang.getFlurstueckseigentum() != null && !vorgang.getFlurstueckseigentum().isEmpty()) {
        str.append("Flurstückseigentum: ");
        str.append(vorgang.getFlurstueckseigentum());
        str.append("\n");
      }

      str.append("Erstellung: ");
      str.append(formatter.format(vorgang.getDatum()));
      str.append("\n");

      if (sendAutor && vorgang.getAutorEmail() != null && !vorgang.getAutorEmail().isEmpty()) {
        str.append("Autor: ");
        str.append(vorgang.getAutorEmail());
        str.append("\n");
      }

      str.append("Status: ");
      str.append(vorgang.getStatus().getText());
      str.append("\n");

      if (vorgang.getStatusKommentar() != null && !vorgang.getStatusKommentar().isEmpty()) {
        str.append("öffentliche Statusinformation: ");
        str.append(vorgang.getStatusKommentar());
        str.append("\n");
      }

      str.append("Zuständigkeit: ");
      str.append(vorgang.getZustaendigkeit());
      str.append(" (");
      str.append(vorgang.getZustaendigkeitStatus());
      str.append(")");
      str.append("\n");

      if (vorgang.getDelegiertAn() != null && !vorgang.getDelegiertAn().isEmpty()) {
        str.append("Delegiert an: ");
        str.append(vorgang.getDelegiertAn());
        str.append("\n");
      }

      if (sendKarte) {
        str.append("\nKarte\n*****\n");
        str.append("Aufruf in ").append(settingsService.getContextAppTitle()).append(": ")
          .append(geoService.getMapExternExternUrl(vorgang)).append("\n\n");
        str.append("Aufruf in ").append(geoService.getMapExternName()).append(": ")
          .append(geoService.getMapExternUrl(vorgang)).append("\n");
      }

      if (sendKommentare) {
        str.append("\ninterne Kommentare\n******************\n");
        for (Kommentar kommentar : kommentarDao.findKommentareForVorgang(vorgang)) {
          str.append("- ").append(kommentar.getNutzer()).append(" ")
            .append(formatter.format(kommentar.getDatum())).append(" -\n");
          str.append(kommentar.getText());
          str.append("\n\n");
        }
      }

      if (sendLobHinweiseKritik) {
        str.append("\nLob, Hinweise oder Kritik von Bürger/-innen\n*******************************************\n");
        for (LobHinweiseKritik lobHinweiseKritik : lobHinweiseKritikDao.findLobHinweiseKritikForVorgang(vorgang)) {
          str.append("- ").append(lobHinweiseKritik.getAutorEmail()).append(" ")
            .append(formatter.format(lobHinweiseKritik.getDatum())).append(" -\n");
          str.append(lobHinweiseKritik.getFreitext());
          str.append("\n\n");
        }
      }

      if (sendMissbrauchsmeldungen) {
        str.append("\nMissbrauchsmeldungen\n********************\n");
        for (Missbrauchsmeldung missbrauchsmeldung : vorgangDao.listMissbrauchsmeldung(vorgang)) {
          str.append("- ").append(formatter.format(missbrauchsmeldung.getDatum())).append(" -\n");
          str.append(missbrauchsmeldung.getText());
          str.append("\n\n");
        }
      }

      mailText = mailText.replaceAll("%vorgang%", str.toString());

      return mailText;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Sendet E-Mails an die Dispatcher mit neuen Vorgängen.
   *
   * @param newVorgaenge Liste der Vorgänge, die in der E-Mail dargestellt werden sollen.
   * @param to Liste der Empfänger der E-Mail.
   */
  public void sendInformDispatcherMail(List<Vorgang> newVorgaenge, String[] to) {
    if (CollectionUtils.isEmpty(newVorgaenge) || ArrayUtils.isEmpty(to)) {
      return;
    }

    SimpleMailMessage msg = new SimpleMailMessage(informDispatcherMailTemplate);
    msg.setTo(to);
    msg.setSubject(msg.getSubject());
    StringBuilder str = new StringBuilder();
    for (Vorgang vorgang : newVorgaenge) {
      str.append("Nummer        : ").append(vorgang.getId()).append("\n");
      str.append("Hauptkategorie: ").append(vorgang.getKategorie().getParent().getName()).append("\n");
      str.append("Unterkategorie: ").append(vorgang.getKategorie().getName()).append("\n");
      str.append("URL           : ").append(getServerBaseUrlBackend()).append("vorgang/")
        .append(vorgang.getId()).append("/uebersicht\n");
      str.append("************************************\n");
    }

    msg.setText(msg.getText().replaceAll("%vorgaenge%", str.toString()));
    jobExecutorService.runJob(new MailSenderJob(this, msg));
  }

  /**
   * Sendet E-Mails an die externen Benutzer mit neuen Vorgängen.
   *
   * @param newVorgaenge Liste der Vorgänge, die in der E-Mail dargestellt werden sollen.
   * @param to Liste der Empfänger der E-Mail.
   */
  public void sendInformExternMail(List<Vorgang> newVorgaenge, String[] to) {
    if (CollectionUtils.isEmpty(newVorgaenge) || ArrayUtils.isEmpty(to)) {
      return;
    }

    SimpleMailMessage msg = new SimpleMailMessage(informExternMailTemplate);
    msg.setTo(to);
    msg.setSubject(msg.getSubject());
    StringBuilder str = new StringBuilder();
    for (Vorgang vorgang : newVorgaenge) {
      str.append("Nummer        : ").append(vorgang.getId()).append("\n");
      str.append("Hauptkategorie: ").append(vorgang.getKategorie().getParent().getName()).append("\n");
      str.append("Unterkategorie: ").append(vorgang.getKategorie().getName()).append("\n");
      str.append("URL           : ").append(getServerBaseUrlBackend()).append("vorgang/delegiert/")
        .append(vorgang.getId()).append("/uebersicht\n");
      str.append("************************************\n");
    }

    msg.setText(msg.getText().replaceAll("%vorgaenge%", str.toString()));
    jobExecutorService.runJob(new MailSenderJob(this, msg));
  }

  /**
   * Sendet eine E-Mail an den Ersteller eines Vorganges mit den Daten über den aktuellen Status des
   * Vorganges.
   *
   * @param vorgang Vorgang zu dem der Ersteller informiert werden sollen.
   */
  public void sendInformErstellerMailInBearbeitung(Vorgang vorgang) {
    SimpleMailMessage msg = new SimpleMailMessage(informErstellerMailInBearbeitungTemplate);
    msg.setTo(vorgang.getAutorEmail());
    msg.setSubject(msg.getSubject().replaceAll("%id%", vorgang.getId().toString())
      .replaceAll("%title%", settingsService.getContextAppTitle()));

    String mailtext = msg.getText();
    mailtext = mailtext.replaceAll("%id%", vorgang.getId().toString());
    mailtext = mailtext.replaceAll("%title%", settingsService.getContextAppTitle());
    StringBuilder str = new StringBuilder();
    //Vorgang
    str.append("Nummer        : ").append(vorgang.getId()).append("\n");
    str.append("Typ           : ").append(vorgang.getTyp().getText()).append("\n");
    str.append("Hauptkategorie: ").append(vorgang.getKategorie().getParent().getName()).append("\n");
    str.append("Unterkategorie: ").append(vorgang.getKategorie().getName()).append("\n\n\n");
    str.append(geoService.getMapExternExternUrl(vorgang)).append("\n");
    mailtext = mailtext.replaceAll("%vorgang%", str.toString());
    //Datum
    mailtext = mailtext.replaceAll("%datum%", formatter.format(vorgang.getDatum()));
    //Status
    str = new StringBuilder();
    str.append(vorgang.getStatus().getText());
    if (!StringUtils.isBlank(vorgang.getStatusKommentar())) {
      str.append(" (Statusinformation: ").append(vorgang.getStatusKommentar()).append(")\n");
    }
    mailtext = mailtext.replaceAll("%status%", str.toString());

    msg.setText(mailtext);

    jobExecutorService.runJob(new MailSenderJob(this, msg));
  }

  /**
   * Sendet eine E-Mail an den Ersteller eines Vorganges mit den Daten über den aktuellen Status des
   * Vorganges.
   *
   * @param vorgang Vorgang zu dem der Ersteller informiert werden sollen.
   */
  public void sendInformErstellerMailLangeInBearbeitung(Vorgang vorgang) {
    SimpleMailMessage msg = new SimpleMailMessage(informErstellerMailLangeInBearbeitungTemplate);
    msg.setTo(vorgang.getAutorEmail());
    msg.setSubject(msg.getSubject().replaceAll("%id%", vorgang.getId().toString())
      .replaceAll("%title%", settingsService.getContextAppTitle()));

    String mailtext = msg.getText();
    mailtext = mailtext.replaceAll("%id%", vorgang.getId().toString());
    mailtext = mailtext.replaceAll("%title%", settingsService.getContextAppTitle());
    StringBuilder str = new StringBuilder();
    //Vorgang
    str.append("Nummer        : ").append(vorgang.getId()).append("\n");
    str.append("Typ           : ").append(vorgang.getTyp().getText()).append("\n");
    str.append("Hauptkategorie: ").append(vorgang.getKategorie().getParent().getName()).append("\n");
    str.append("Unterkategorie: ").append(vorgang.getKategorie().getName()).append("\n\n\n");
    str.append(geoService.getMapExternExternUrl(vorgang)).append("\n");
    mailtext = mailtext.replaceAll("%vorgang%", str.toString());
    //Datum
    mailtext = mailtext.replaceAll("%datum%", formatter.format(vorgang.getDatum()));
    //Status
    str = new StringBuilder();
    str.append(vorgang.getStatus().getText());
    if (!StringUtils.isBlank(vorgang.getStatusKommentar())) {
      str.append(" (Statusinformation: ").append(vorgang.getStatusKommentar()).append(")\n");
    }
    mailtext = mailtext.replaceAll("%status%", str.toString());

    msg.setText(mailtext);

    jobExecutorService.runJob(new MailSenderJob(this, msg));
  }

  /**
   * Sendet eine E-Mail an den Ersteller eines Vorganges mit den Daten über den aktuellen Status des
   * Vorganges.
   *
   * @param vorgang Vorgang zu dem der Ersteller informiert werden sollen.
   */
  public void sendInformErstellerMailAbschluss(Vorgang vorgang) {
    SimpleMailMessage msg = new SimpleMailMessage(informErstellerMailAbschlussTemplate);
    msg.setTo(vorgang.getAutorEmail());
    msg.setSubject(msg.getSubject().replaceAll("%id%", vorgang.getId().toString())
      .replaceAll("%title%", settingsService.getContextAppTitle()));

    String mailtext = msg.getText();
    mailtext = mailtext.replaceAll("%id%", vorgang.getId().toString());
    mailtext = mailtext.replaceAll("%title%", settingsService.getContextAppTitle());
    StringBuilder str = new StringBuilder();
    //Vorgang
    str.append("Nummer        : ").append(vorgang.getId()).append("\n");
    str.append("Typ           : ").append(vorgang.getTyp().getText()).append("\n");
    str.append("Hauptkategorie: ").append(vorgang.getKategorie().getParent().getName()).append("\n");
    str.append("Unterkategorie: ").append(vorgang.getKategorie().getName()).append("\n\n\n");
    str.append(geoService.getMapExternExternUrl(vorgang)).append("\n");
    mailtext = mailtext.replaceAll("%vorgang%", str.toString());
    //Datum
    mailtext = mailtext.replaceAll("%datum%", formatter.format(vorgang.getDatum()));
    //Status
    str = new StringBuilder();
    str.append(vorgang.getStatus().getText());
    if (!StringUtils.isBlank(vorgang.getStatusKommentar())) {
      str.append(" (Statusinformation: ").append(vorgang.getStatusKommentar()).append(")\n");
    }
    mailtext = mailtext.replaceAll("%status%", str.toString());

    msg.setText(mailtext);

    jobExecutorService.runJob(new MailSenderJob(this, msg));
  }

  /**
   * Sendet eine E-Mail an den Unterstuetzer eines Vorganges mit den Daten über den aktuellen Status des
   * Vorganges.
   *
   * @param unterstuetzer Unterstützung des Vorgang zu dem informiert werden sollen.
   */
  public void sendInformUnterstuetzerMailInBearbeitung(Unterstuetzer unterstuetzer) {
    Vorgang vorgang = unterstuetzer.getVorgang();
    SimpleMailMessage msg = new SimpleMailMessage(informUnterstuetzerMailInBearbeitungTemplate);
    msg.setTo(vorgang.getAutorEmail());
    msg.setSubject(msg.getSubject().replaceAll("%id%", vorgang.getId().toString())
      .replaceAll("%title%", settingsService.getContextAppTitle()));

    String mailtext = msg.getText();
    mailtext = mailtext.replaceAll("%id%", vorgang.getId().toString());
    mailtext = mailtext.replaceAll("%title%", settingsService.getContextAppTitle());
    StringBuilder str = new StringBuilder();
    //Vorgang
    str.append("Nummer        : ").append(vorgang.getId()).append("\n");
    str.append("Typ           : ").append(vorgang.getTyp().getText()).append("\n");
    str.append("Hauptkategorie: ").append(vorgang.getKategorie().getParent().getName()).append("\n");
    str.append("Unterkategorie: ").append(vorgang.getKategorie().getName()).append("\n\n\n");
    str.append(geoService.getMapExternExternUrl(vorgang)).append("\n");
    mailtext = mailtext.replaceAll("%vorgang%", str.toString());
    //Datum
    mailtext = mailtext.replaceAll("%datum%", formatter.format(vorgang.getDatum()));
    //Status
    str = new StringBuilder();
    str.append(vorgang.getStatus().getText());
    if (!StringUtils.isBlank(vorgang.getStatusKommentar())) {
      str.append(" (Statusinformation: ").append(vorgang.getStatusKommentar()).append(")\n");
    }
    mailtext = mailtext.replaceAll("%status%", str.toString());

    msg.setText(mailtext);

    jobExecutorService.runJob(new MailSenderJob(this, msg));
  }

  /**
   * Sendet eine E-Mail an den Unterstuetzer eines Vorganges mit den Daten über den aktuellen Status des
   * Vorganges.
   *
   * @param unterstuetzer Unterstützung des Vorgang zu dem informiert werden sollen.
   */
  public void sendInformUnterstuetzerMailAbschluss(Unterstuetzer unterstuetzer) {
    Vorgang vorgang = unterstuetzer.getVorgang();
    SimpleMailMessage msg = new SimpleMailMessage(informUnterstuetzerMailAbschlussTemplate);
    msg.setTo(vorgang.getAutorEmail());
    msg.setSubject(msg.getSubject().replaceAll("%id%", vorgang.getId().toString())
      .replaceAll("%title%", settingsService.getContextAppTitle()));

    String mailtext = msg.getText();
    mailtext = mailtext.replaceAll("%id%", vorgang.getId().toString());
    mailtext = mailtext.replaceAll("%title%", settingsService.getContextAppTitle());
    StringBuilder str = new StringBuilder();
    //Vorgang
    str.append("Nummer        : ").append(vorgang.getId()).append("\n");
    str.append("Typ           : ").append(vorgang.getTyp().getText()).append("\n");
    str.append("Hauptkategorie: ").append(vorgang.getKategorie().getParent().getName()).append("\n");
    str.append("Unterkategorie: ").append(vorgang.getKategorie().getName()).append("\n\n\n");
    str.append(geoService.getMapExternExternUrl(vorgang)).append("\n");
    mailtext = mailtext.replaceAll("%vorgang%", str.toString());
    //Datum
    mailtext = mailtext.replaceAll("%datum%", formatter.format(vorgang.getDatum()));
    //Status
    str = new StringBuilder();
    str.append(vorgang.getStatus().getText());
    if (!StringUtils.isBlank(vorgang.getStatusKommentar())) {
      str.append(" (Statusinformation: ").append(vorgang.getStatusKommentar()).append(")\n");
    }
    mailtext = mailtext.replaceAll("%status%", str.toString());

    msg.setText(mailtext);

    jobExecutorService.runJob(new MailSenderJob(this, msg));
  }

  /**
   * Sendet E-Mails an die Empfänger redaktioneller E-Mails.
   *
   * @param tageOffenNichtAkzeptiert Anzahl der Tage zum Redaktionskriterium 1.
   * @param tageInbearbeitungOhneStatusKommentar Anzahl der Tage zum Redaktionskriterium 2.
   * @param tageIdeeOffenOhneUnterstuetzung Anzahl der Tage zum Redaktionskriterium 3.
   * @param tageInBearbeitung Anzahl der Tage zum Redaktionskriterium 4.
   * @param vorgaengeOffenNichtAkzeptiert Liste der Vorgänge zu Redaktionskriterium 1, die in der
   * E-Mail dargestellt werden sollen.
   * @param vorgaengeInbearbeitungOhneStatusKommentar Liste der Vorgänge zu Redaktionskriterium 2,
   * die in der E-Mail dargestellt werden sollen.
   * @param vorgaengeIdeeOffenOhneUnterstuetzung Liste der Vorgänge zu Redaktionskriterium 3, die in
   * der E-Mail dargestellt werden sollen.
   * @param vorgaengeInBearbeitung Liste der Vorgänge zu Redaktionskriterium 4, die in
   * der E-Mail dargestellt werden sollen.
   * @param vorgaengeNichtLoesbarOhneStatuskommentar Liste der Vorgänge zu Redaktionskriterium 5,
   * die in der E-Mail dargestellt werden sollen.
   * @param vorgaengeNichtMehrOffenNichtAkzeptiert Liste der Vorgänge zu Redaktionskriterium 6, die
   * in der E-Mail dargestellt werden sollen.
   * @param vorgaengeOhneRedaktionelleFreigaben Liste der Vorgänge zu Redaktionskriterium 7, die in
   * der E-Mail dargestellt werden sollen.
   * @param vorgaengeOhneZustaendigkeit Liste der Vorgänge zu Redaktionskriterium 8, die in der
   * E-Mail dargestellt werden sollen.
   * @param to Empfänger der E-Mail.
   * @param zustaendigkeit
   */
  public void sendInformRedaktionEmpfaengerMail(Short tageOffenNichtAkzeptiert,
    Short tageInbearbeitungOhneStatusKommentar, Short tageIdeeOffenOhneUnterstuetzung, Short tageInBearbeitung,
    List<Vorgang> vorgaengeOffenNichtAkzeptiert, List<Vorgang> vorgaengeInbearbeitungOhneStatusKommentar,
    List<Vorgang> vorgaengeIdeeOffenOhneUnterstuetzung, List<Vorgang> vorgaengeInBearbeitung,
    List<Vorgang> vorgaengeNichtLoesbarOhneStatuskommentar, List<Vorgang> vorgaengeNichtMehrOffenNichtAkzeptiert,
    List<Vorgang> vorgaengeOhneRedaktionelleFreigaben, List<Vorgang> vorgaengeOhneZustaendigkeit,
    String to, String zustaendigkeit) {

    //keine E-Mail versenden, falls alle Listen von Vorgängen leer sind
    if ((CollectionUtils.isEmpty(vorgaengeOffenNichtAkzeptiert))
      && (CollectionUtils.isEmpty(vorgaengeInbearbeitungOhneStatusKommentar))
      && (CollectionUtils.isEmpty(vorgaengeIdeeOffenOhneUnterstuetzung))
      && (CollectionUtils.isEmpty(vorgaengeInBearbeitung))
      && (CollectionUtils.isEmpty(vorgaengeNichtLoesbarOhneStatuskommentar))
      && (CollectionUtils.isEmpty(vorgaengeNichtMehrOffenNichtAkzeptiert))
      && (CollectionUtils.isEmpty(vorgaengeOhneRedaktionelleFreigaben))
      && (CollectionUtils.isEmpty(vorgaengeOhneZustaendigkeit))) {

      return;
    }

    //lokale Variablen initiieren
    Date jetzt = new Date();

    //Teiltexte für die einzelnen Redaktionskriterien initiieren
    SimpleMailMessage textKriteriumOffenNichtAkzeptiert
      = new SimpleMailMessage(kriteriumOffenNichtAkzeptiertTemplate);
    SimpleMailMessage textKriteriumOffenInbearbeitungOhneStatusKommentar
      = new SimpleMailMessage(kriteriumOffenInbearbeitungOhneStatusKommentarTemplate);
    SimpleMailMessage textKriteriumIdeeOffenOhneUnterstuetzung
      = new SimpleMailMessage(kriteriumIdeeOffenOhneUnterstuetzungTemplate);
    SimpleMailMessage textKriteriumInBearbeitung
      = new SimpleMailMessage(kriteriumInBearbeitungTemplate);
    SimpleMailMessage textKriteriumNichtLoesbarOhneStatuskommentar
      = new SimpleMailMessage(kriteriumNichtLoesbarOhneStatuskommentarTemplate);
    SimpleMailMessage textKriteriumNichtMehrOffenNichtAkzeptiert
      = new SimpleMailMessage(kriteriumNichtMehrOffenNichtAkzeptiertTemplate);
    SimpleMailMessage textKriteriumOhneRedaktionelleFreigaben
      = new SimpleMailMessage(kriteriumOhneRedaktionelleFreigabenTemplate);
    SimpleMailMessage textKriteriumOhneZustaendigkeit
      = new SimpleMailMessage(kriteriumOhneZustaendigkeitTemplate);

    //Gesamt-E-Mail initiieren, mit Adresse des Empfängers versehen und entsprechenden Platzhalter für Zuständigkeit ersetzen
    SimpleMailMessage msg = new SimpleMailMessage(informRedaktionEmpfaengerMailTemplate);
    msg.setTo(to);
    msg.setSubject(msg.getSubject().replaceAll("%title%", settingsService.getContextAppTitle()));
    msg.setText(msg.getText().replaceAll("%zustaendigkeit%", zustaendigkeit));
    msg.setText(msg.getText().replaceAll("%title%", settingsService.getContextAppTitle()));

    //falls Liste der Vorgänge zu Redaktionskriterium 1 nicht leer ist...
    if (!CollectionUtils.isEmpty(vorgaengeOffenNichtAkzeptiert)) {

      //Liste der Vorgänge auslesen und zu String zusammenbauen
      StringBuilder str = new StringBuilder();
      for (Vorgang vorgang : vorgaengeOffenNichtAkzeptiert) {
        str.append(String.format("%1$-9s", vorgang.getId()))
          .append(String.format("%1$-27s", vorgang.getZustaendigkeit()))
          .append(String.format("%1$-10s", vorgang.getTyp().getText()))
          .append(formatter.format(vorgang.getVersion()))
          .append(" (vor ")
          .append((jetzt.getTime() - vorgang.getVersion().getTime()) / (24 * 60 * 60 * 1000))
          .append(" Tagen)\n");
      }

      //Platzhalter für Teiltexte ersetzen und Teiltexte in Gesamt-E-Mail einfügen
      textKriteriumOffenNichtAkzeptiert.setText(textKriteriumOffenNichtAkzeptiert.getText()
        .replaceAll("%tage%", tageOffenNichtAkzeptiert.toString()));
      textKriteriumOffenNichtAkzeptiert.setText(textKriteriumOffenNichtAkzeptiert.getText()
        .replaceAll("%vorgaenge%", str.toString()));
      msg.setText(msg.getText().replaceAll("%kriteriumOffenNichtAkzeptiert%",
        textKriteriumOffenNichtAkzeptiert.getText()));
    } //ansonsten Platzhalter für Teiltexte des Redaktionskriteriums 1 (plus nachfolgende Linebreaks) aus Gesamt-E-Mail entfernen
    else {
      msg.setText(msg.getText().replaceAll("%kriteriumOffenNichtAkzeptiert%\n\n", ""));
    }

    //falls Liste der Vorgänge zu Redaktionskriterium 2 nicht leer ist...
    if (!CollectionUtils.isEmpty(vorgaengeInbearbeitungOhneStatusKommentar)) {

      //Liste der Vorgänge auslesen und zu String zusammenbauen
      StringBuilder str = new StringBuilder();
      for (Vorgang vorgang : vorgaengeInbearbeitungOhneStatusKommentar) {
        str.append(String.format("%1$-9s", vorgang.getId()))
          .append(String.format("%1$-27s", vorgang.getZustaendigkeit()))
          .append(String.format("%1$-10s", vorgang.getTyp().getText()))
          .append(formatter.format(vorgang.getVersion()))
          .append(" (vor ")
          .append((jetzt.getTime() - vorgang.getVersion().getTime()) / (24 * 60 * 60 * 1000))
          .append(" Tagen)\n");
      }

      //Platzhalter für Teiltexte ersetzen und Teiltexte in Gesamt-E-Mail einfügen
      textKriteriumOffenInbearbeitungOhneStatusKommentar.setText(
        textKriteriumOffenInbearbeitungOhneStatusKommentar.getText().replaceAll("%tage%",
          tageInbearbeitungOhneStatusKommentar.toString()));
      textKriteriumOffenInbearbeitungOhneStatusKommentar.setText(
        textKriteriumOffenInbearbeitungOhneStatusKommentar.getText().replaceAll("%vorgaenge%", str.toString()));
      msg.setText(msg.getText().replaceAll("%kriteriumOffenInbearbeitungOhneStatusKommentar%",
        textKriteriumOffenInbearbeitungOhneStatusKommentar.getText()));
    } //ansonsten Platzhalter für Teiltexte des Redaktionskriteriums 2 (plus nachfolgende Linebreaks) aus Gesamt-E-Mail entfernen
    else {
      msg.setText(msg.getText().replaceAll("%kriteriumOffenInbearbeitungOhneStatusKommentar%\n\n", ""));
    }

    //falls Liste der Vorgänge zu Redaktionskriterium 3 nicht leer ist...
    if (!CollectionUtils.isEmpty(vorgaengeIdeeOffenOhneUnterstuetzung)) {

      //Liste der Vorgänge auslesen und zu String zusammenbauen
      StringBuilder str = new StringBuilder();
      for (Vorgang vorgang : vorgaengeIdeeOffenOhneUnterstuetzung) {
        str.append(String.format("%1$-9s", vorgang.getId()))
          .append(String.format("%1$-27s", vorgang.getZustaendigkeit()))
          .append(String.format("%1$-18s", vorgangDao.countUnterstuetzerByVorgang(vorgang)))
          .append(formatter.format(verlaufDao.getAktuellstesAkzeptierenDerZustaendigkeitZuVorgang(vorgang)))
          .append(" (vor ")
          .append((jetzt.getTime() - verlaufDao.getAktuellstesAkzeptierenDerZustaendigkeitZuVorgang(vorgang).getTime()) / (24 * 60 * 60 * 1000))
          .append(" Tagen)\n");
      }

      //Platzhalter für Teiltexte ersetzen und Teiltexte in Gesamt-E-Mail einfügen
      textKriteriumIdeeOffenOhneUnterstuetzung.setText(textKriteriumIdeeOffenOhneUnterstuetzung.getText()
        .replaceAll("%tage%", tageIdeeOffenOhneUnterstuetzung.toString()));
      textKriteriumIdeeOffenOhneUnterstuetzung.setText(textKriteriumIdeeOffenOhneUnterstuetzung.getText()
        .replaceAll("%unterstuetzungen%", settingsService.getVorgangIdeeUnterstuetzer().toString()));
      textKriteriumIdeeOffenOhneUnterstuetzung.setText(textKriteriumIdeeOffenOhneUnterstuetzung.getText()
        .replaceAll("%vorgaenge%", str.toString()));
      msg.setText(msg.getText().replaceAll("%kriteriumIdeeOffenOhneUnterstuetzung%",
        textKriteriumIdeeOffenOhneUnterstuetzung.getText()));
    } //ansonsten Platzhalter für Teiltexte des Redaktionskriteriums 3 (plus nachfolgende Linebreaks) aus Gesamt-E-Mail entfernen
    else {
      msg.setText(msg.getText().replaceAll("%kriteriumIdeeOffenOhneUnterstuetzung%\n\n", ""));
    }

    //falls Liste der Vorgänge zu Redaktionskriterium 4 nicht leer ist...
    if (!CollectionUtils.isEmpty(vorgaengeInBearbeitung)) {

      //Liste der Vorgänge auslesen und zu String zusammenbauen
      StringBuilder str = new StringBuilder();
      for (Vorgang vorgang : vorgaengeInBearbeitung) {
        str.append(String.format("%1$-9s", vorgang.getId()))
          .append(String.format("%1$-27s", vorgang.getZustaendigkeit()))
          .append(String.format("%1$-10s", vorgang.getTyp().getText()))
          .append(formatter.format(vorgang.getDatum()))
          .append(" (vor ")
          .append((jetzt.getTime() - vorgang.getDatum().getTime()) / (24 * 60 * 60 * 1000))
          .append(" Tagen)\n");
      }

      //Platzhalter für Teiltexte ersetzen und Teiltexte in Gesamt-E-Mail einfügen
      textKriteriumInBearbeitung.setText(textKriteriumInBearbeitung.getText()
        .replaceAll("%tage%", tageInBearbeitung.toString()));
      textKriteriumInBearbeitung.setText(textKriteriumInBearbeitung.getText()
        .replaceAll("%unterstuetzungen%", settingsService.getVorgangIdeeUnterstuetzer().toString()));
      textKriteriumInBearbeitung.setText(textKriteriumInBearbeitung.getText()
        .replaceAll("%vorgaenge%", str.toString()));
      msg.setText(msg.getText().replaceAll("%kriteriumInBearbeitung%",
        textKriteriumInBearbeitung.getText()));
    } //ansonsten Platzhalter für Teiltexte des Redaktionskriteriums 4 (plus nachfolgende Linebreaks) aus Gesamt-E-Mail entfernen
    else {
      msg.setText(msg.getText().replaceAll("%kriteriumInBearbeitung%\n\n", ""));
    }

    //falls Liste der Vorgänge zu Redaktionskriterium 5 nicht leer ist...
    if (!CollectionUtils.isEmpty(vorgaengeNichtLoesbarOhneStatuskommentar)) {

      //Liste der Vorgänge auslesen und zu String zusammenbauen
      StringBuilder str = new StringBuilder();
      for (Vorgang vorgang : vorgaengeNichtLoesbarOhneStatuskommentar) {
        str.append(String.format("%1$-9s", vorgang.getId()))
          .append(String.format("%1$-27s", vorgang.getZustaendigkeit()))
          .append(String.format("%1$-10s", vorgang.getTyp().getText()))
          .append(formatter.format(vorgang.getVersion()))
          .append(" (vor ")
          .append((jetzt.getTime() - vorgang.getVersion().getTime()) / (24 * 60 * 60 * 1000))
          .append(" Tagen)\n");
      }

      //Platzhalter für Teiltexte ersetzen und Teiltexte in Gesamt-E-Mail einfügen
      textKriteriumNichtLoesbarOhneStatuskommentar.setText(
        textKriteriumNichtLoesbarOhneStatuskommentar.getText().replaceAll("%vorgaenge%", str.toString()));
      msg.setText(msg.getText().replaceAll("%kriteriumNichtLoesbarOhneStatuskommentar%",
        textKriteriumNichtLoesbarOhneStatuskommentar.getText()));
    } //ansonsten Platzhalter für Teiltexte des Redaktionskriteriums 5 (plus nachfolgende Linebreaks) aus Gesamt-E-Mail entfernen
    else {
      msg.setText(msg.getText().replaceAll("%kriteriumNichtLoesbarOhneStatuskommentar%\n\n", ""));
    }

    //falls Liste der Vorgänge zu Redaktionskriterium 6 nicht leer ist...
    if (!CollectionUtils.isEmpty(vorgaengeNichtMehrOffenNichtAkzeptiert)) {

      //Liste der Vorgänge auslesen und zu String zusammenbauen
      StringBuilder str = new StringBuilder();
      for (Vorgang vorgang : vorgaengeNichtMehrOffenNichtAkzeptiert) {
        str.append(String.format("%1$-9s", vorgang.getId()))
          .append(String.format("%1$-27s", vorgang.getZustaendigkeit()))
          .append(String.format("%1$-10s", vorgang.getTyp().getText()))
          .append(String.format("%1$-24s", vorgang.getStatus().getText()))
          .append(formatter.format(vorgang.getVersion()))
          .append(" (vor ")
          .append((jetzt.getTime() - vorgang.getVersion().getTime()) / (24 * 60 * 60 * 1000))
          .append(" Tagen)\n");
      }

      //Platzhalter für Teiltexte ersetzen und Teiltexte in Gesamt-E-Mail einfügen
      textKriteriumNichtMehrOffenNichtAkzeptiert.setText(textKriteriumNichtMehrOffenNichtAkzeptiert.getText()
        .replaceAll("%vorgaenge%", str.toString()));
      msg.setText(msg.getText().replaceAll("%kriteriumNichtMehrOffenNichtAkzeptiert%",
        textKriteriumNichtMehrOffenNichtAkzeptiert.getText()));
    } //ansonsten Platzhalter für Teiltexte des Redaktionskriteriums 6 (plus nachfolgende Linebreaks) aus Gesamt-E-Mail entfernen
    else {
      msg.setText(msg.getText().replaceAll("%kriteriumNichtMehrOffenNichtAkzeptiert%\n\n", ""));
    }

    //falls Liste der Vorgänge zu Redaktionskriterium 7 nicht leer ist...
    if (!CollectionUtils.isEmpty(vorgaengeOhneRedaktionelleFreigaben)) {

      //Liste der Vorgänge auslesen und zu String zusammenbauen
      StringBuilder str = new StringBuilder();
      for (Vorgang vorgang : vorgaengeOhneRedaktionelleFreigaben) {
        str.append(String.format("%1$-9s", vorgang.getId()))
          .append(String.format("%1$-27s", vorgang.getZustaendigkeit()))
          .append(String.format("%1$-10s", vorgang.getTyp().getText()))
          .append(String.format("%1$-24s", vorgang.getStatus().getText()))
          .append(String.format("%1$-25s", vorgang.getBeschreibungFreigabeStatus()))
          .append(vorgang.getFotoFreigabeStatus()).append("\n");
      }

      //Platzhalter für Teiltexte ersetzen und Teiltexte in Gesamt-E-Mail einfügen
      textKriteriumOhneRedaktionelleFreigaben.setText(textKriteriumOhneRedaktionelleFreigaben.getText()
        .replaceAll("%vorgaenge%", str.toString()));
      msg.setText(msg.getText().replaceAll("%kriteriumOhneRedaktionelleFreigaben%",
        textKriteriumOhneRedaktionelleFreigaben.getText()));
    } //ansonsten Platzhalter für Teiltexte des Redaktionskriteriums 7 (plus nachfolgende Linebreaks) aus Gesamt-E-Mail entfernen
    else {
      msg.setText(msg.getText().replaceAll("%kriteriumOhneRedaktionelleFreigaben%\n\n", ""));
    }

    //falls Liste der Vorgänge zu Redaktionskriterium 8 nicht leer ist...
    if (!CollectionUtils.isEmpty(vorgaengeOhneZustaendigkeit)) {

      //Liste der Vorgänge auslesen und zu String zusammenbauen
      StringBuilder str = new StringBuilder();
      for (Vorgang vorgang : vorgaengeOhneZustaendigkeit) {
        str.append(String.format("%1$-9s", vorgang.getId()))
          .append(String.format("%1$-10s", vorgang.getTyp().getText()))
          .append(vorgang.getStatus().getText())
          .append("\n");
      }

      //Platzhalter für Teiltexte ersetzen und Teiltexte in Gesamt-E-Mail einfügen
      textKriteriumOhneZustaendigkeit.setText(textKriteriumOhneZustaendigkeit.getText()
        .replaceAll("%vorgaenge%", str.toString()));
      msg.setText(msg.getText().replaceAll("%kriteriumOhneZustaendigkeit%",
        textKriteriumOhneZustaendigkeit.getText()));
    } //ansonsten Platzhalter für Teiltexte des Redaktionskriteriums 8 (plus nachfolgende Linebreaks) aus Gesamt-E-Mail entfernen
    else {
      msg.setText(msg.getText().replaceAll("%kriteriumOhneZustaendigkeit%\n\n", ""));
    }

    //Job ausführen
    jobExecutorService.runJob(new MailSenderJob(this, msg));
  }

  /**
   * Passt eine URL an, so dass sie beim Versenden einer EMail verwendet werden kann.
   *
   * @param url URL, die geprüft ung ggf. angepasst werden soll.
   * @return angepasste URL
   */
  private String getServerUrl(String url) {
    if (!StringUtils.isBlank(url)) {
      return url.endsWith("/") ? url : url + "/";
    } else {
      try {
        return "http:/" + servletContext.getResource("/").getPath();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public String getServerBaseUrlBackend() {
    return getServerUrl(serverBaseUrlBackend);
  }

  public String getServerBaseUrlFrontend() {
    return getServerUrl(serverBaseUrlFrontend);
  }

  public JavaMailSender getMailSender() {
    return mailSender;
  }

  public void setMailSender(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public String getMailFrom() {
    return mailFrom;
  }

  public void setMailFrom(String mailFrom) {
    this.mailFrom = mailFrom;
  }

  public String getSendAllMailsTo() {
    return sendAllMailsTo;
  }

  public void setSendAllMailsTo(String sendAllMailsTo) {
    this.sendAllMailsTo = sendAllMailsTo;
  }

  public SimpleMailMessage getVorgangBestaetigungMailTemplate() {
    return vorgangBestaetigungMailTemplate;
  }

  public void setVorgangBestaetigungMailTemplate(SimpleMailMessage vorgangBestaetigungMailTemplate) {
    this.vorgangBestaetigungMailTemplate = vorgangBestaetigungMailTemplate;
  }

  public SimpleMailMessage getUnterstuetzungBestaetigungMailTemplate() {
    return unterstuetzungBestaetigungMailTemplate;
  }

  public void setUnterstuetzungBestaetigungMailTemplate(SimpleMailMessage unterstuetzungBestaetigungMailTemplate) {
    this.unterstuetzungBestaetigungMailTemplate = unterstuetzungBestaetigungMailTemplate;
  }

  public SimpleMailMessage getMissbrauchsmeldungBestaetigungMailTemplate() {
    return missbrauchsmeldungBestaetigungMailTemplate;
  }

  public void setMissbrauchsmeldungBestaetigungMailTemplate(SimpleMailMessage missbrauchsmeldungBestaetigungMailTemplate) {
    this.missbrauchsmeldungBestaetigungMailTemplate = missbrauchsmeldungBestaetigungMailTemplate;
  }

  public SimpleMailMessage getFotoBestaetigungMailTemplate() {
    return fotoBestaetigungMailTemplate;
  }

  public void setFotoBestaetigungMailTemplate(SimpleMailMessage fotoBestaetigungMailTemplate) {
    this.fotoBestaetigungMailTemplate = fotoBestaetigungMailTemplate;
  }

  public SimpleMailMessage getVorgangWeiterleitenMailTemplate() {
    return vorgangWeiterleitenMailTemplate;
  }

  public void setVorgangWeiterleitenMailTemplate(SimpleMailMessage vorgangWeiterleitenMailTemplate) {
    this.vorgangWeiterleitenMailTemplate = vorgangWeiterleitenMailTemplate;
  }

  public SimpleMailMessage getInformDispatcherMailTemplate() {
    return informDispatcherMailTemplate;
  }

  public void setInformDispatcherMailTemplate(SimpleMailMessage informDispatcherMailTemplate) {
    this.informDispatcherMailTemplate = informDispatcherMailTemplate;
  }

  public SimpleMailMessage getInformExternMailTemplate() {
    return informExternMailTemplate;
  }

  public void setInformExternMailTemplate(SimpleMailMessage informExternMailTemplate) {
    this.informExternMailTemplate = informExternMailTemplate;
  }

  public void setServerBaseUrlBackend(String serverBaseUrlBackend) {
    this.serverBaseUrlBackend = serverBaseUrlBackend;
  }

  public void setServerBaseUrlFrontend(String serverBaseUrlFrontend) {
    this.serverBaseUrlFrontend = serverBaseUrlFrontend;
  }

  public SimpleMailMessage getInformErstellerMailInBearbeitungTemplate() {
    return informErstellerMailInBearbeitungTemplate;
  }

  public void setInformErstellerMailInBearbeitungTemplate(
    SimpleMailMessage informErstellerMailInBearbeitungTemplate) {
    this.informErstellerMailInBearbeitungTemplate = informErstellerMailInBearbeitungTemplate;
  }

  public SimpleMailMessage getInformErstellerMailAbschlussTemplate() {
    return informErstellerMailAbschlussTemplate;
  }

  public void setInformErstellerMailAbschlussTemplate(
    SimpleMailMessage informErstellerMailAbschlussTemplate) {
    this.informErstellerMailAbschlussTemplate = informErstellerMailAbschlussTemplate;
  }

  public SimpleMailMessage getInformUnterstuetzerMailInBearbeitungTemplate() {
    return informUnterstuetzerMailInBearbeitungTemplate;
  }

  public void setInformUnterstuetzerMailInBearbeitungTemplate(SimpleMailMessage informUnterstuetzerMailInBearbeitungTemplate) {
    this.informUnterstuetzerMailInBearbeitungTemplate = informUnterstuetzerMailInBearbeitungTemplate;
  }

  public SimpleMailMessage getInformUnterstuetzerMailAbschlussTemplate() {
    return informUnterstuetzerMailAbschlussTemplate;
  }

  public void setInformUnterstuetzerMailAbschlussTemplate(SimpleMailMessage informUnterstuetzerMailAbschlussTemplate) {
    this.informUnterstuetzerMailAbschlussTemplate = informUnterstuetzerMailAbschlussTemplate;
  }

  public SimpleMailMessage getKriteriumOffenNichtAkzeptiertTemplate() {
    return kriteriumOffenNichtAkzeptiertTemplate;
  }

  public void setKriteriumOffenNichtAkzeptiertTemplate(
    SimpleMailMessage kriteriumOffenNichtAkzeptiertTemplate) {
    this.kriteriumOffenNichtAkzeptiertTemplate = kriteriumOffenNichtAkzeptiertTemplate;
  }

  public SimpleMailMessage getKriteriumOffenInbearbeitungOhneStatusKommentarTemplate() {
    return kriteriumOffenInbearbeitungOhneStatusKommentarTemplate;
  }

  public void setKriteriumOffenInbearbeitungOhneStatusKommentarTemplate(
    SimpleMailMessage kriteriumOffenInbearbeitungOhneStatusKommentarTemplate) {
    this.kriteriumOffenInbearbeitungOhneStatusKommentarTemplate = kriteriumOffenInbearbeitungOhneStatusKommentarTemplate;
  }

  public SimpleMailMessage getKriteriumIdeeOffenOhneUnterstuetzungTemplate() {
    return kriteriumIdeeOffenOhneUnterstuetzungTemplate;
  }

  public void setKriteriumIdeeOffenOhneUnterstuetzungTemplate(
    SimpleMailMessage kriteriumIdeeOffenOhneUnterstuetzungTemplate) {
    this.kriteriumIdeeOffenOhneUnterstuetzungTemplate = kriteriumIdeeOffenOhneUnterstuetzungTemplate;
  }

  public SimpleMailMessage getKriteriumInBearbeitungTemplate() {
    return kriteriumInBearbeitungTemplate;
  }

  public void setKriteriumInBearbeitungTemplate(
    SimpleMailMessage kriteriumInBearbeitungTemplate) {
    this.kriteriumInBearbeitungTemplate = kriteriumInBearbeitungTemplate;
  }

  public SimpleMailMessage getKriteriumNichtLoesbarOhneStatuskommentarTemplate() {
    return kriteriumNichtLoesbarOhneStatuskommentarTemplate;
  }

  public void setKriteriumNichtLoesbarOhneStatuskommentarTemplate(
    SimpleMailMessage kriteriumNichtLoesbarOhneStatuskommentarTemplate) {
    this.kriteriumNichtLoesbarOhneStatuskommentarTemplate = kriteriumNichtLoesbarOhneStatuskommentarTemplate;
  }

  public SimpleMailMessage getKriteriumNichtMehrOffenNichtAkzeptiertTemplate() {
    return kriteriumNichtMehrOffenNichtAkzeptiertTemplate;
  }

  public void setKriteriumNichtMehrOffenNichtAkzeptiertTemplate(
    SimpleMailMessage kriteriumNichtMehrOffenNichtAkzeptiertTemplate) {
    this.kriteriumNichtMehrOffenNichtAkzeptiertTemplate = kriteriumNichtMehrOffenNichtAkzeptiertTemplate;
  }

  public SimpleMailMessage getKriteriumOhneRedaktionelleFreigabenTemplate() {
    return kriteriumOhneRedaktionelleFreigabenTemplate;
  }

  public void setKriteriumOhneRedaktionelleFreigabenTemplate(
    SimpleMailMessage kriteriumOhneRedaktionelleFreigabenTemplate) {
    this.kriteriumOhneRedaktionelleFreigabenTemplate = kriteriumOhneRedaktionelleFreigabenTemplate;
  }

  public SimpleMailMessage getKriteriumOhneZustaendigkeitTemplate() {
    return kriteriumOhneZustaendigkeitTemplate;
  }

  public void setKriteriumOhneZustaendigkeitTemplate(
    SimpleMailMessage kriteriumOhneZustaendigkeitTemplate) {
    this.kriteriumOhneZustaendigkeitTemplate = kriteriumOhneZustaendigkeitTemplate;
  }

  public SimpleMailMessage getInformRedaktionEmpfaengerMailTemplate() {
    return informRedaktionEmpfaengerMailTemplate;
  }

  public void setInformRedaktionEmpfaengerMailTemplate(
    SimpleMailMessage informRedaktionEmpfaengerMailTemplate) {
    this.informRedaktionEmpfaengerMailTemplate = informRedaktionEmpfaengerMailTemplate;
  }

  public String getMailtoMailclientEncoding() {
    return mailtoMailclientEncoding;
  }

  public void setMailtoMailclientEncoding(String mailtoMailclientEncoding) {
    this.mailtoMailclientEncoding = mailtoMailclientEncoding;
  }
}
