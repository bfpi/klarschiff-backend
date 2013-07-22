package de.fraunhofer.igd.klarschiff.service.mail;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import de.fraunhofer.igd.klarschiff.dao.KommentarDao;
import de.fraunhofer.igd.klarschiff.dao.VerlaufDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.job.JobExecutorService;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.Kommentar;
import de.fraunhofer.igd.klarschiff.vo.Missbrauchsmeldung;
import de.fraunhofer.igd.klarschiff.vo.Unterstuetzer;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Die Klasse stellt einen Service bereit über den E-Mails erstellt und versendet werden können.
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
	VorgangDao vorgangDao;
	
	@Autowired
	VerlaufDao verlaufDao;

	JavaMailSender mailSender;
	String serverBaseUrlBackend;
	String serverBaseUrlFrontend;
	
	String sendAllMailsTo;

	String mailtoMailclientEncoding="UTF-8";
	
	SimpleMailMessage vorgangBestaetigungMailTemplate;
	SimpleMailMessage unterstuetzungBestaetigungMailTemplate;
	SimpleMailMessage missbrauchsmeldungBestaetigungMailTemplate;
	SimpleMailMessage vorgangWeiterleitenMailTemplate;
	SimpleMailMessage informDispatcherMailTemplate;
	SimpleMailMessage informExternMailTemplate;
	SimpleMailMessage informErstellerMailInBearbeitungTemplate;
	SimpleMailMessage informErstellerMailAbschlussTemplate;

	private SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	
	
	/**
	 * Versendet eine E-Mail zur Bestätigung eines Vorganges.
	 * @param vorgang Vorgang, zu dem eine bestätigungsmail versendet werden soll.
	 */
	public void sendVorgangBestaetigungMail(Vorgang vorgang) {
		SimpleMailMessage msg = new SimpleMailMessage(vorgangBestaetigungMailTemplate);
		msg.setTo(vorgang.getAutorEmail());
		String mailText = msg.getText();
		mailText = mailText.replaceAll("%baseUrlFrontend%", getServerBaseUrlFrontend());
		mailText = mailText.replaceAll("%hash%", vorgang.getHash());
		StringBuilder str = new StringBuilder();
		str.append(geoService.getMapExternExternUrl(vorgang));
		mailText = mailText.replaceAll("%meldungLink%", str.toString());
		msg.setText(mailText);
		jobExecutorService.runJob(new MailSenderJob(this, msg));
	}
	
	
	/**
	 * Versendet eine E-Mail zur Bestätigung einer Unterstützung. 
	 * @param unterstuetzer Unterstützung, zu der die Bestätigungsmail versendet werden soll.
	 * @param email E-Mailadresse, an die die E-Mail versendet werden soll.
	 */
	public void sendUnterstuetzerBestaetigungMail(Unterstuetzer unterstuetzer, String email) {
		SimpleMailMessage msg = new SimpleMailMessage(unterstuetzungBestaetigungMailTemplate);
		msg.setTo(email);
		String mailText = msg.getText();
		mailText = mailText.replaceAll("%baseUrlFrontend%", getServerBaseUrlFrontend());
		mailText = mailText.replaceAll("%hash%", unterstuetzer.getHash());
		msg.setText(mailText);
		jobExecutorService.runJob(new MailSenderJob(this, msg));
	}

	
	/**
	 * Versendet eine E-Mail zur Bestätigung einer Missbrauchsmeldung.
	 * @param missbrauchsmeldung Missbrauchsmeldung, zu der die Bestätigungsemail versendet werden soll.
	 * @param email E-Mailadresse, an die die E-Mail versendet werden soll.
	 */
	public void sendMissbrauchsmeldungBestaetigungMail(Missbrauchsmeldung missbrauchsmeldung, String email) {
		SimpleMailMessage msg = new SimpleMailMessage(missbrauchsmeldungBestaetigungMailTemplate);
		msg.setTo(email);
		String mailText = msg.getText();
		mailText = mailText.replaceAll("%baseUrlFrontend%", getServerBaseUrlFrontend());
		mailText = mailText.replaceAll("%hash%", missbrauchsmeldung.getHash());
		msg.setText(mailText);
		jobExecutorService.runJob(new MailSenderJob(this, msg));
	}
		

	/**
	 * Erstellt und versendet eine E-Mail mit den Daten eines Vorganges
	 * @param vorgang Vorgang zu dem die E-Mail erstellt werden soll.
	 * @param fromEmail E-Mailadresse des Absenders
	 * @param toEmail E-Mailadresse des Empfängers
	 * @param text Freitextfeld, der in die E-Mail aufgenommen wird.
	 * @param sendKarte Soll ein Link für die Karte mitgesendet werden?
	 * @param sendKommentare Sollen die Kommentare mitgesendet werden?
	 * @param sendFoto Soll das Foto als Anhang mitgesendet werden?
	 * @param sendMissbrauchsmeldungen sollen die Missbrauchsmeldungen mitgesendet werden?
	 */
	public void sendVorgangWeiterleitenMail(Vorgang vorgang, String fromEmail, String toEmail, String text, boolean sendKarte, boolean sendKommentare, boolean sendFoto, boolean sendMissbrauchsmeldungen)
	{
		try {
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mailSender.createMimeMessage(), true);
			mimeMessageHelper.setSubject(vorgangWeiterleitenMailTemplate.getSubject());
			mimeMessageHelper.setFrom(StringUtils.isBlank(sendAllMailsTo) ? fromEmail : sendAllMailsTo);
			mimeMessageHelper.setTo(toEmail);

			String mailText = composeVorgangWeiterleitenMail(vorgang, text, sendKarte, sendKommentare, sendMissbrauchsmeldungen);

			mimeMessageHelper.setText(mailText);
			
			if (sendFoto && vorgang.getFotoNormalJpg()!=null) 
				mimeMessageHelper.addAttachment("foto.jpg", new ByteArrayResource(vorgang.getFotoNormalJpg()), "image/jpg");
				
			jobExecutorService.runJob(new MailSenderJob(this, mimeMessageHelper.getMimeMessage()));
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	
	}

	
	/**
	 * Erstellt den Text einer E-Mail zum Weiterleiten eines Vorganges
	 * @param vorgang Vorgang zu dem die E-Mail erstellt werden soll.
	 * @param text Freitextfeld, der in die E-Mail aufgenommen wird.
	 * @param sendKarte Soll ein Link für die Karte in der E-Mail erzeugt werden?
	 * @param sendKommentare Sollen die Kommentare in der E-Mail aufgenommen werden?
	 * @param sendMissbrauchsmeldungen sollen die Missbrauchsmeldungen in der E-Mail aufgenommen werden?
	 * @return Text der erzeugten E-Mail
	 */
	public String composeVorgangWeiterleitenMail(Vorgang vorgang, String text, boolean sendKarte, boolean sendKommentare, boolean sendMissbrauchsmeldungen) throws RuntimeException
	{
		try {
			String mailText = vorgangWeiterleitenMailTemplate.getText();
			mailText = mailText.replaceAll("%absender%", securityService.getCurrentUser().getName());
			mailText = mailText.replaceAll("%text%", text);
			StringBuilder str = new StringBuilder();
			str.append("Typ: " );
			str.append(vorgang.getTyp().getText());
			str.append("\n");

			str.append("ID: " );
			str.append(vorgang.getId());
			str.append("\n");			
			
			str.append("Hauptkategorie: " );
			str.append(vorgang.getKategorie().getParent().getName());
			str.append("\n");
			
			str.append("Unterkategorie: " );
			str.append(vorgang.getKategorie().getName());
			str.append("\n");
			
			if (vorgang.getBetreff().isEmpty() == false) {
				str.append("Betreff: " );
				str.append(vorgang.getBetreff());
				str.append("\n");
				str.append("Betreff Freigabestatus: " );
				str.append(vorgang.getBetreffFreigabeStatus().toString());
				str.append("\n");
			}
			
			if (vorgang.getDetails().isEmpty() == false) {
				str.append("Details: " );
				str.append(vorgang.getDetails());
				str.append("\n");
				str.append("Details Freigabestatus: " );
				str.append(vorgang.getDetailsFreigabeStatus().toString());
				str.append("\n");
			}
			
			if(vorgang.getAdresse().isEmpty() == false){
				str.append("Adresse: ");
				str.append(vorgang.getAdresse());
				str.append("\n");
			}
			
			str.append("Erstellung: " );
			str.append(formatter.format(vorgang.getDatum()));
			str.append("\n");
			
			if (vorgang.getAutorEmail().isEmpty() == false) {
				str.append("Autor: " );
				str.append(vorgang.getAutorEmail());
				str.append("\n");
			}
			
			str.append("Status: " );
			str.append(vorgang.getStatus().getText());
			str.append("\n");
			
			if (vorgang.getStatusKommentar() != null) {
				str.append("Kommentar: " );
				str.append(vorgang.getStatusKommentar());
				str.append("\n");
			}
			
			str.append("Zust\u00e4ndigkeit: " ); 
			str.append(vorgang.getZustaendigkeit());
			str.append(" (");
			str.append(vorgang.getZustaendigkeitStatus());
			str.append(")");
			str.append("\n");

			if (vorgang.getDelegiertAn() != null) {
				str.append("Delegiert an: " );
				str.append(vorgang.getDelegiertAn());
				str.append("\n");
			}

			if ( sendKarte==true ) {
				str.append("\nKarte\n*****\n");
				str.append("Aufruf in Klarschiff: "+geoService.getMapExternExternUrl(vorgang)+"\n\n");
				str.append("Aufruf in Geoport.HRO: "+geoService.getMapExternUrl(vorgang)+"\n");
			}
			
			if ( sendKommentare == true) {
				str.append("\ninterne Kommentare\n******************\n");
				for (Kommentar kommentar : kommentarDao.findKommentareForVorgang(vorgang)) {
					str.append("- " + kommentar.getNutzer() + " " + formatter.format(kommentar.getDatum()) +" -\n" );
					str.append(kommentar.getText());
					str.append("\n\n");
				}
			}
				
			if ( sendMissbrauchsmeldungen == true) {	
				str.append("\nMissbrauchsmeldungen\n********************\n");
				for (Missbrauchsmeldung missbrauchsmeldung : vorgangDao.listMissbrauchsmeldung(vorgang)) {
					str.append("- " + formatter.format(missbrauchsmeldung.getDatum()) +" -\n" );
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
	 * @param newVorgaenge Liste der Vorgänge, die in der E-Mail dargestellt werden sollen.
	 * @param to Liste der Empfänger der E-Mail.
	 */
	public void sendInformDispatcherMail(List<Vorgang> newVorgaenge, String[] to) {
		if (CollectionUtils.isEmpty(newVorgaenge) || ArrayUtils.isEmpty(to)) return;
		
		SimpleMailMessage msg = new SimpleMailMessage(informDispatcherMailTemplate);
		msg.setTo(to);
		StringBuilder str = new StringBuilder();
		for(Vorgang vorgang : newVorgaenge) {
			str.append("Nummer        : "+vorgang.getId()+"\n");
			str.append("Hauptkategorie: "+vorgang.getKategorie().getParent().getName()+"\n");
			str.append("Unterkategorie: "+vorgang.getKategorie().getName()+"\n");
			str.append("URL           : "+getServerBaseUrlBackend()+"vorgang/"+vorgang.getId()+"/uebersicht\n");
			str.append("************************************\n");
		}
		
		msg.setText(msg.getText().replaceAll("%vorgaenge%", str.toString()));
		jobExecutorService.runJob(new MailSenderJob(this, msg));	
	}
	
	
	/**
	 * Sendet E-Mails an die externen Benutzer mit neune Vorgängen.
	 * @param newVorgaenge Liste der Vorgänge, die in der E-Mail dargestellt werden sollen.
	 * @param to Liste der Empfänger der E-Mail.
	 */
	public void sendInformExternMail(List<Vorgang> newVorgaenge, String[] to) {
		if (CollectionUtils.isEmpty(newVorgaenge) || ArrayUtils.isEmpty(to)) return;
		
		SimpleMailMessage msg = new SimpleMailMessage(informExternMailTemplate);
		msg.setTo(to);
		StringBuilder str = new StringBuilder();
		for(Vorgang vorgang : newVorgaenge) {
			str.append("Nummer        : "+vorgang.getId()+"\n");
			str.append("Hauptkategorie: "+vorgang.getKategorie().getParent().getName()+"\n");
			str.append("Unterkategorie: "+vorgang.getKategorie().getName()+"\n");
			str.append("URL           : "+getServerBaseUrlBackend()+"vorgang/delegiert/"+vorgang.getId()+"/uebersicht\n");
			str.append("************************************\n");
		}
		
		msg.setText(msg.getText().replaceAll("%vorgaenge%", str.toString()));
		jobExecutorService.runJob(new MailSenderJob(this, msg));
	}
    
    
    /**
	 * Sendet eine E-Mail an den Ersteller eines Vorganges mit den Daten über den aktuellen Status des Vorganges.
	 * @param vorgang Vorgang zu dem der Ersteller informiert werden sollen.
	 */
	public void sendInformErstellerMailInBearbeitung(Vorgang vorgang) {
		SimpleMailMessage msg = new SimpleMailMessage(informErstellerMailInBearbeitungTemplate);
		msg.setTo(vorgang.getAutorEmail());

		String mailtext = msg.getText();
		StringBuilder str = new StringBuilder();
		//Vorgang
		str.append("Nummer        : "+vorgang.getId()+"\n");
		str.append("Typ           : "+vorgang.getTyp().getText()+"\n");
		str.append("Hauptkategorie: "+vorgang.getKategorie().getParent().getName()+"\n");
		str.append("Unterkategorie: "+vorgang.getKategorie().getName()+"\n\n\n");
		str.append(geoService.getMapExternExternUrl(vorgang)+"\n");
		mailtext = mailtext.replaceAll("%vorgang%", str.toString());
		//Datum
		mailtext = mailtext.replaceAll("%datum%", formatter.format(vorgang.getDatum()));
		//Status
		str = new StringBuilder();
		str.append(vorgang.getStatus().getText());
		if (!StringUtils.isBlank(vorgang.getStatusKommentar()))
			str.append(" (Info der Verwaltung: "+vorgang.getStatusKommentar()+")\n");
		mailtext = mailtext.replaceAll("%status%", str.toString());
		
		msg.setText(mailtext);
		
		jobExecutorService.runJob(new MailSenderJob(this, msg));
	}

	
	/**
	 * Sendet eine E-Mail an den Ersteller eines Vorganges mit den Daten über den aktuellen Status des Vorganges.
	 * @param vorgang Vorgang zu dem der Ersteller informiert werden sollen.
	 */
	public void sendInformErstellerMailAbschluss(Vorgang vorgang) {
		SimpleMailMessage msg = new SimpleMailMessage(informErstellerMailAbschlussTemplate);
		msg.setTo(vorgang.getAutorEmail());

		String mailtext = msg.getText();
		StringBuilder str = new StringBuilder();
		//Vorgang
		str.append("Nummer        : "+vorgang.getId()+"\n");
		str.append("Typ           : "+vorgang.getTyp().getText()+"\n");
		str.append("Hauptkategorie: "+vorgang.getKategorie().getParent().getName()+"\n");
		str.append("Unterkategorie: "+vorgang.getKategorie().getName()+"\n\n\n");
		str.append(geoService.getMapExternExternUrl(vorgang)+"\n");
		mailtext = mailtext.replaceAll("%vorgang%", str.toString());
		//Datum
		mailtext = mailtext.replaceAll("%datum%", formatter.format(vorgang.getDatum()));
		//Status
		str = new StringBuilder();
		str.append(vorgang.getStatus().getText());
		if (!StringUtils.isBlank(vorgang.getStatusKommentar()))
			str.append(" (Info der Verwaltung: "+vorgang.getStatusKommentar()+")\n");
		mailtext = mailtext.replaceAll("%status%", str.toString());
		
		msg.setText(mailtext);
		
		jobExecutorService.runJob(new MailSenderJob(this, msg));
	}
	
	
	/**
	 * Passt eine URL an, so dass sie beim Versenden einer EMail verwendet werden kann.
	 * @param url URL, die geprüft ung ggf. angepasst werden soll.
	 * @return angepasste URL
	 */
	private String getServerUrl(String url) {
		if (!StringUtils.isBlank(url)) {
			return url.endsWith("/") ? url : url+"/";
		} else {
			try {
				return "http:/"+servletContext.getResource("/").getPath();
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

    /* --------------- GET + SET ----------------------------*/

	public JavaMailSender getMailSender() {
		return mailSender;
	}
	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
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
	public void setVorgangBestaetigungMailTemplate( SimpleMailMessage vorgangBestaetigungMailTemplate) {
		this.vorgangBestaetigungMailTemplate = vorgangBestaetigungMailTemplate;
	}
	public SimpleMailMessage getUnterstuetzungBestaetigungMailTemplate() {
		return unterstuetzungBestaetigungMailTemplate;
	}
	public void setUnterstuetzungBestaetigungMailTemplate( SimpleMailMessage unterstuetzungBestaetigungMailTemplate) {
		this.unterstuetzungBestaetigungMailTemplate = unterstuetzungBestaetigungMailTemplate;
	}
	public SimpleMailMessage getMissbrauchsmeldungBestaetigungMailTemplate() {
		return missbrauchsmeldungBestaetigungMailTemplate;
	}
	public void setMissbrauchsmeldungBestaetigungMailTemplate(SimpleMailMessage missbrauchsmeldungBestaetigungMailTemplate) {
		this.missbrauchsmeldungBestaetigungMailTemplate = missbrauchsmeldungBestaetigungMailTemplate;
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


	public String getMailtoMailclientEncoding() {
		return mailtoMailclientEncoding;
	}


	public void setMailtoMailclientEncoding(String mailtoMailclientEncoding) {
		this.mailtoMailclientEncoding = mailtoMailclientEncoding;
	}	
}
