package de.fraunhofer.igd.klarschiff.web;

import static de.fraunhofer.igd.klarschiff.web.Assert.assertEmail;
import static de.fraunhofer.igd.klarschiff.web.Assert.assertMaxLength;
import static de.fraunhofer.igd.klarschiff.web.Assert.assertNotEmpty;

import java.io.OutputStream;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.mail.MailService;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Controller zum Versenden von Vorgangsinformationen per E-Mail.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SessionAttributes("cmd")
@Controller
public class VorgangEmailController {

	Logger logger = Logger.getLogger(VorgangEmailController.class);
	
	@Autowired
	VorgangDao vorgangDao;
	
	@Autowired
	SecurityService securityService;

	@Autowired
	MailService mailService;
	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/{id}/email</code><br/>
	 * Seitenbeschreibung: Formular zum Versenden einer �bersicht des aktuellen Vorganges per E-Mail.
	 * Die Eingabe des Empf�ngers, eines Freitext sowie die Auswahl, welche Vorgangselemente (Karte, Bild, 
	 * Kommentare, Missbrauchsmeldungen) angef�gt werden sollen, sind m�glich.
	 * @param id Vorgangs-ID
	 * @param model Model in der ggf. Daten f�r die View abgelegt werden
	 * @param request HttpServletRequest-Objekt
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/vorgang/{id}/email", method = RequestMethod.GET)
	public String email(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
		return email(id, model, request, false);
	}
	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/delegiert/{id}/email</code><br/>
	 * Seitenbeschreibung: Formular zum Versenden einer �bersicht des aktuellen Vorganges per E-Mail f�r
	 * Externe (Delegierte).
	 * Die Eingabe des Empf�ngers, eines Freitext sowie die Auswahl, welche Vorgangselemente (Karte, Bild, 
	 * Kommentare) angef�gt werden sollen, sind m�glich. Missbrauchsmeldungen stehen Externen nicht zur Verf�gung.
	 * @param id Vorgangs-ID
	 * @param model Model in der ggf. Daten f�r die View abgelegt werden
	 * @param request HttpServletRequest-Objekt
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/vorgang/delegiert/{id}/email", method = RequestMethod.GET)
	public String emailDelegiert(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
		return email(id, model, request, true);
	}
	
	/**
	 * Die Methode liefert den View f�r den E-Mailversand und reichert zuvor das zugeh�rige Command-
	 * Objekt (<code>VorgangEmailCommand</code>) mit Vorgangs- und Nutzerinformationen an. 
	 * In Abh�ngigkeit vom <code>delegiert</code> Parameter werden Missbrauchsmeldungen angeh�ngt 
	 * (<code>delegiert=false</code>).
	 * @param id Vorgangs-ID
	 * @param model Model in der ggf. Daten f�r die View abgelegt werden
	 * @param request HttpServletRequest-Objekt
	 * @param delegiert E-Mailverand f�r delgierte? (keine Missbrauchsmedlungen)
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	public String email(Long id, ModelMap model, HttpServletRequest request, boolean delegiert) {
		VorgangEmailCommand cmd = new VorgangEmailCommand();
		cmd.setVorgang(vorgangDao.findVorgang(id));
		cmd.setFromEmail(securityService.getCurrentUser().getEmail());
		cmd.setFromName(securityService.getCurrentUser().getName());
		if (delegiert) {
			cmd.setSendMissbrauchsmeldungen(false);
			model.put("delegiert", true);
		}
		model.put("cmd", cmd);
		
		return "noMenu/vorgang/printEmail/email";
	}
	
	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/vorgang/delegiert/{id}/email</code><br/>
	 * Funktionsbeschreibung: F�hrt E-Mail-Versand nach Pr�fung durch. Liefert Best�tigungsseite oder weist auf Fehler hin.
	 * @param cmd VorgangEmailCommand
	 * @param result BindingResult
	 * @param id Vorgangs-ID
	 * @param model Model in der ggf. Daten f�r die View abgelegt werden
	 * @param request HttpServletRequest-Objekt
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/vorgang/{id}/email", method = RequestMethod.POST)
    public String emailSubmit(
    		@ModelAttribute(value = "cmd") VorgangEmailCommand cmd, 
    		BindingResult result, 
    		@PathVariable("id") Long id, 
    		ModelMap model, 
    		HttpServletRequest request) {
		return emailSubmit(cmd, result, id, model, request, false);
	}
	
	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/vorgang/delegiert/{id}/email</code><br/>
	 * Funktionsbeschreibung: F�hrt E-Mail-Versand nach Pr�fung f�r Externe (Delegierte) durch. 
	 * Liefert Best�tigungsseite oder weist auf Fehler hin.
	 * @param cmd VorgangEmailCommand
	 * @param result BindingResult
	 * @param id Vorgangs-ID
	 * @param model Model in der ggf. Daten f�r die View abgelegt werden
	 * @param request HttpServletRequest-Objekt
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/vorgang/delegiert/{id}/email", method = RequestMethod.POST)
    public String emailDelegiertSubmit(
    		@ModelAttribute(value = "cmd") VorgangEmailCommand cmd, 
    		BindingResult result, 
    		@PathVariable("id") Long id, 
    		ModelMap model, 
    		HttpServletRequest request) {
		return emailSubmit(cmd, result, id, model, request, true);		
	}


	/**
	 * Die Methode f�hrt den EMailversand durch. Sie pr�ft auf Vorhandensein und G�ltigkeit der E-Mail-Adresse
	 * sowie auf Vorhandensein und maximale L�nge von 300 Zeichen des Mail-Textes.
	 * @param cmd VorgangEmailCommand
	 * @param result BindingResult
	 * @param id Vorgangs-ID
	 * @param model Model in der ggf. Daten f�r die View abgelegt werden
	 * @param request HttpServletRequest-Objekt
 	 * @param delegiert E-Mailverand f�r delgierte? (keine Missbrauchsmedlungen)
	 * @return View, die zum Rendern des Request verwendet wird
	 */
    public String emailSubmit(
    		VorgangEmailCommand cmd, 
    		BindingResult result, 
    		Long id, 
    		ModelMap model, 
    		HttpServletRequest request,
    		boolean delegiert) {
		if (delegiert)
			model.put("delegiert", true);
		
		assertNotEmpty(cmd, result, Assert.EvaluateOn.ever, "toEmail", null);
		assertNotEmpty(cmd, result, Assert.EvaluateOn.ever, "text", null);
		assertMaxLength(cmd, result,  Assert.EvaluateOn.ever, "text", 300, null);
		assertEmail(cmd, result, Assert.EvaluateOn.firstPropertyError, "toEmail", null);
		if (result.hasErrors()) {
			return "noMenu/vorgang/printEmail/email";
		}			
		
		mailService.sendVorgangWeiterleitenMail(vorgangDao.findVorgang(id), cmd.getFromEmail(), cmd.getToEmail(), cmd.getText(), cmd.getSendKarte(), cmd.getSendKommentare(), cmd.getSendFoto(), cmd.getSendMissbrauchsmeldungen());
		return "noMenu/vorgang/printEmail/emailSubmit";
	}

	/**
	 * Die Methode verarbeitet den Request auf der URL <code>/vorgang/{id}/emailDirect</code><br/>
	 * Funktionsbeschreibung: erlaubt das Versenden der Vorgangs�bersichts-E-Mail mit Hilfe eines lokalen Mailclients
	 * via <code>mailto:</code>.
	 * @param id Vorgangs-ID
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value="/vorgang/{id}/emailDirect")
	@ResponseBody
	public void emailDirect(
			@PathVariable("id") Long id, 
			HttpServletResponse response) throws Exception {
		emailDirect(id, response, false);
	}

	/**
	 * Die Methode verarbeitet den Request auf der URL <code>/vorgang/{id}/emailDirect</code><br/>
	 * Funktionsbeschreibung: erlaubt das Versenden der Vorgangs�bersichts-E-Mail mit Hilfe eines lokalen Mailclients
	 * via <code>mailto:</code> f�r Externe (Delegierte).
	 * @param id Vorgangs-ID
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value="/vorgang/delegiert/{id}/emailDirect")
	@ResponseBody
	public void emailDelegiertDirect(
			@PathVariable("id") Long id, 
			HttpServletResponse response) throws Exception {
		emailDirect(id, response, true);
	}
	
	/**
	 * Methode erzeugt den Link zum Versand einer Email mit dem 
	 * lokalen Mailclient.
	 * @param id Vorgangs-ID
	 * @param response HttpServletResponse
	 * @param onlyurl erstellt nur die mailto-URL oder eine Seite mit entsprechendem JavaScript
	 * @param delegiert E-Mailverand f�r delgierte? (keine Missbrauchsmedlungen)
	 */
	public void emailDirect(Long id, HttpServletResponse response, boolean delegiert) throws Exception {
		// Vorgang ermitteln
		Vorgang vorgang = vorgangDao.findVorgang(id);
		
		// Subject und Body f�r die E-Mail ermittlen
		String subject = mailService.getVorgangWeiterleitenMailTemplate().getSubject();
		String body = mailService.composeVorgangWeiterleitenMail(vorgang, "", true, true, !delegiert);
		
		// Text-Encoding ermitteln
        String encoding = mailService.getMailtoMailclientEncoding();
        
        // Subject und Body jeweils URL-encoden sowie vollst�ndigen Link zusammensetzen
		String link = "mailto:?subject=" + URLEncoder.encode(subject) + "&body=" + URLEncoder.encode(body);

		response.setCharacterEncoding(encoding);
		response.setHeader("Content-Type", "text/html;charset=" + encoding);
		response.getOutputStream().write(link.replaceAll("\\+", "%20").getBytes(encoding));
		response.setStatus(HttpServletResponse.SC_OK);
		response.flushBuffer();
	}
}
