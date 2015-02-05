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
	 * Seitenbeschreibung: Formular zum Versenden einer Übersicht des aktuellen Vorganges per E-Mail.
	 * Die Eingabe des Empfängers, eines Freitext sowie die Auswahl, welche Vorgangselemente (Autor, Karte, Bild, 
	 * Kommentare, Missbrauchsmeldungen) angefügt werden sollen, sind möglich.
	 * @param id Vorgangs-ID
	 * @param model Model in der ggf. Daten für die View abgelegt werden
	 * @param request HttpServletRequest-Objekt
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/vorgang/{id}/email", method = RequestMethod.GET)
	public String email(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
		return email(id, model, request, false);
	}
	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/vorgang/delegiert/{id}/email</code><br/>
	 * Seitenbeschreibung: Formular zum Versenden einer Übersicht des aktuellen Vorganges per E-Mail für
	 * Externe (Delegierte).
	 * Die Eingabe des Empfängers, eines Freitext sowie die Auswahl, welche Vorgangselemente (Karte, Bild, 
	 * Kommentare) angefügt werden sollen, sind möglich. Autor und Missbrauchsmeldungen stehen Externen nicht zur Verfügung.
	 * @param id Vorgangs-ID
	 * @param model Model in der ggf. Daten für die View abgelegt werden
	 * @param request HttpServletRequest-Objekt
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(value="/vorgang/delegiert/{id}/email", method = RequestMethod.GET)
	public String emailDelegiert(@PathVariable("id") Long id, ModelMap model, HttpServletRequest request) {
		return email(id, model, request, true);
	}
	
	/**
	 * Die Methode liefert den View für den E-Mailversand und reichert zuvor das zugehörige Command-
	 * Objekt (<code>VorgangEmailCommand</code>) mit Vorgangs- und Nutzerinformationen an. 
	 * In Abhängigkeit vom <code>delegiert</code> Parameter werden Missbrauchsmeldungen angehängt 
	 * (<code>delegiert=false</code>).
	 * @param id Vorgangs-ID
	 * @param model Model in der ggf. Daten für die View abgelegt werden
	 * @param request HttpServletRequest-Objekt
	 * @param delegiert E-Mailverand für delgierte? (kein Autor und keine Missbrauchsmedlungen)
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	public String email(Long id, ModelMap model, HttpServletRequest request, boolean delegiert) {
		VorgangEmailCommand cmd = new VorgangEmailCommand();
		cmd.setVorgang(vorgangDao.findVorgang(id));
		cmd.setFromEmail(securityService.getCurrentUser().getEmail());
		cmd.setFromName(securityService.getCurrentUser().getName());
		if (delegiert) {
			cmd.setSendAutor(false);
			cmd.setSendMissbrauchsmeldungen(false);
			model.put("delegiert", true);
		}
		model.put("cmd", cmd);
		
		return "noMenu/vorgang/printEmail/email";
	}
	
	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/vorgang/delegiert/{id}/email</code><br/>
	 * Funktionsbeschreibung: Führt E-Mail-Versand nach Prüfung durch. Liefert Bestätigungsseite oder weist auf Fehler hin.
	 * @param cmd VorgangEmailCommand
	 * @param result BindingResult
	 * @param id Vorgangs-ID
	 * @param model Model in der ggf. Daten für die View abgelegt werden
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
	 * Funktionsbeschreibung: Führt E-Mail-Versand nach Prüfung für Externe (Delegierte) durch. 
	 * Liefert Bestätigungsseite oder weist auf Fehler hin.
	 * @param cmd VorgangEmailCommand
	 * @param result BindingResult
	 * @param id Vorgangs-ID
	 * @param model Model in der ggf. Daten für die View abgelegt werden
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
	 * Die Methode führt den EMailversand durch. Sie prüft auf Vorhandensein und Gültigkeit der E-Mail-Adresse
	 * sowie auf Vorhandensein und maximale Länge von 300 Zeichen des Mail-Textes.
	 * @param cmd VorgangEmailCommand
	 * @param result BindingResult
	 * @param id Vorgangs-ID
	 * @param model Model in der ggf. Daten für die View abgelegt werden
	 * @param request HttpServletRequest-Objekt
 	 * @param delegiert E-Mailverand für delgierte? (kein Autor und keine Missbrauchsmedlungen)
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
		
		mailService.sendVorgangWeiterleitenMail(vorgangDao.findVorgang(id), cmd.getFromEmail(), cmd.getToEmail(), cmd.getText(), cmd.getSendAutor(), cmd.getSendKarte(), cmd.getSendKommentare(), cmd.getSendLobHinweiseKritik(), cmd.getSendFoto(), cmd.getSendMissbrauchsmeldungen());
		return "noMenu/vorgang/printEmail/emailSubmit";
	}

	/**
	 * Die Methode verarbeitet den Request auf der URL <code>/vorgang/{id}/emailDirect</code><br/>
	 * Funktionsbeschreibung: erlaubt das Versenden der Vorgangsübersichts-E-Mail mit Hilfe eines lokalen Mailclients
	 * via <code>mailto:</code>.
	 * @param id Vorgangs-ID
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value="/vorgang/{id}/emailDirect", method = RequestMethod.GET, params = {"browser"})
	@ResponseBody
	public void emailDirect(
			@PathVariable("id") Long id,
            @RequestParam(value = "browser", required = true) String browser,
			HttpServletResponse response) throws Exception {
		emailDirect(id, browser, response, false);
	}

	/**
	 * Die Methode verarbeitet den Request auf der URL <code>/vorgang/{id}/emailDirect</code><br/>
	 * Funktionsbeschreibung: erlaubt das Versenden der Vorgangsübersichts-E-Mail mit Hilfe eines lokalen Mailclients
	 * via <code>mailto:</code> für Externe (Delegierte).
	 * @param id Vorgangs-ID
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value="/vorgang/delegiert/{id}/emailDirect", method = RequestMethod.GET, params = {"browser"})
	@ResponseBody
	public void emailDelegiertDirect(
			@PathVariable("id") Long id,
            @RequestParam(value = "browser", required = true) String browser,
			HttpServletResponse response) throws Exception {
		emailDirect(id, browser, response, true);
	}
	
	/**
	 * Methode erzeugt den Link zum Versand einer Email mit dem 
	 * lokalen Mailclient.
	 * @param id Vorgangs-ID
	 * @param response HttpServletResponse
	 * @param onlyurl erstellt nur die mailto-URL oder eine Seite mit entsprechendem JavaScript
	 * @param delegiert E-Mailverand für delgierte? (kein Autor und keine Missbrauchsmedlungen)
	 */
	public void emailDirect(Long id, String browser, HttpServletResponse response, boolean delegiert) throws Exception {
		// Vorgang ermitteln
		Vorgang vorgang = vorgangDao.findVorgang(id);
		
		// Subject und Body für die E-Mail ermittlen
		String subject = mailService.getVorgangWeiterleitenMailTemplate().getSubject();
        String body = mailService.composeVorgangWeiterleitenMail(vorgang, "", !delegiert, true, true, true, !delegiert);
		
		// Text-Encoding ermitteln
		String encoding = mailService.getMailtoMailclientEncoding();
        
        // Subject und Body jeweils URL-encoden sowie vollständigen Link zusammensetzen...
        String link = new String();
        // ...falls Firefox als Browser genutzt wird, ist das URL-Encoding ohne weitere Parameter durchzuführen
        if (browser.equals("firefox")) {
            link = "mailto:?subject=" + URLEncoder.encode(subject) + "&body=" + URLEncoder.encode(body);
        }
        // ...ansonsten muss dem URL-Encoding das gewünschte Text-Encoding als Parameter übergeben werden
        else {
            link = "mailto:?subject=" + URLEncoder.encode(subject, encoding) + "&body=" + URLEncoder.encode(body, encoding);
        }

		response.setCharacterEncoding(encoding);
		response.setHeader("Content-Type", "text/html;charset=" + encoding);
		response.getOutputStream().write(link.replaceAll("\\+", "%20").getBytes(encoding));
		response.setStatus(HttpServletResponse.SC_OK);
		response.flushBuffer();
	}
}
