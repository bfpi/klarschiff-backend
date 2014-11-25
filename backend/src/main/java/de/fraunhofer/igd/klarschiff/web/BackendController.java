package de.fraunhofer.igd.klarschiff.web;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.codec.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.dao.RedaktionEmpfaengerDao;
import de.fraunhofer.igd.klarschiff.dao.VerlaufDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.classification.ClassificationService;
import de.fraunhofer.igd.klarschiff.service.image.ImageService;
import de.fraunhofer.igd.klarschiff.service.mail.MailService;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.service.security.User;
import de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet;
import de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.EnumZustaendigkeitStatus;
import de.fraunhofer.igd.klarschiff.vo.GeoRss;
import de.fraunhofer.igd.klarschiff.vo.LobHinweiseKritik;
import de.fraunhofer.igd.klarschiff.vo.Missbrauchsmeldung;
import de.fraunhofer.igd.klarschiff.vo.RedaktionEmpfaenger;
import de.fraunhofer.igd.klarschiff.vo.Unterstuetzer;
import de.fraunhofer.igd.klarschiff.vo.Verlauf;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Der Controller dient als Schnittstelle f�r das Frontend
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@RequestMapping("/service")
@Controller
public class BackendController {
	Logger logger = Logger.getLogger(BackendController.class);
	
	@Autowired
	KategorieDao kategorieDao;
	
	@Autowired
	RedaktionEmpfaengerDao redaktionEmpfaengerDao;
	
	@Autowired
	VorgangDao vorgangDao;
	
	@Autowired
	VerlaufDao verlaufDao;
	
	@Autowired
	ClassificationService classificationService;
	
	@Autowired
	SecurityService securityService;

	@Autowired
	ImageService imageService;
	
	@Autowired
	MailService mailService;

	@Autowired
	SettingsService settingsService;
	

	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/service/vorgang</code><br/>
	 * Beschreibung: erstellt einen neuen Vorgang
	 * @param typ Vorgangstyp
	 * @param kategorie Kategorie
	 * @param oviWkt Position als WKT
	 * @param autorEmail E-Mail-Adresse des Erstellers
	 * @param betreff Betreff
	 * @param details Details
	 * @param bild Foto base64 kodiert
	 * @param resultHashOnSubmit <code>true</code> - gibt den Hash zum Best�tigen als Ergebnis zur�ck
	 * @param response Response in das das Ergebnis direkt geschrieben wird
	 */
	@RequestMapping(value="/vorgang", method = RequestMethod.POST)
	@ResponseBody
	public void vorgang(
			@RequestParam(value = "typ", required = false) String typ, 
			@RequestParam(value = "kategorie", required = false) Long kategorie,
			@RequestParam(value = "oviWkt", required = false) String oviWkt,
			@RequestParam(value = "autorEmail", required = false) String autorEmail,
			@RequestParam(value = "betreff", required = false) String betreff,
			@RequestParam(value = "details", required = false) String details,
			@RequestParam(value = "bild", required = false) String bild,
			@RequestParam(value = "resultHashOnSubmit", required = false) Boolean resultHashOnSubmit, 
			HttpServletResponse response) {
		if (resultHashOnSubmit==null) resultHashOnSubmit=false;
		try {
			Vorgang vorgang = new Vorgang();
			
			if (StringUtils.isBlank(typ)) throw new BackendControllerException(1, "[typ] fehlt", "Der Typ ist nicht angegeben.");
			vorgang.setTyp(EnumVorgangTyp.valueOf(typ));
			if (vorgang.getTyp()==null) throw new BackendControllerException(2, "[typ] nicht korrekt", "Der Typ ist nicht korrekt.");
			
			if (kategorie==null) throw new BackendControllerException(3, "[kategorie] fehlt", "Die Angaben zur Kategorie fehlen.");
			vorgang.setKategorie(kategorieDao.findKategorie(kategorie));
			if (vorgang.getKategorie()==null 
					|| vorgang.getKategorie().getParent()==null
					|| vorgang.getKategorie().getParent().getTyp()!=vorgang.getTyp()) throw new BackendControllerException(4, "[kategorie] nicht korrekt", "Die Kategorie ist nicht g�ltig.");
			
			if (oviWkt==null) throw new BackendControllerException(5, "[oviWkt] fehlt", "Die Orstangabe fehlt.");
			try {
				vorgang.setOviWkt(oviWkt);
			}catch (Exception e) {
				throw new BackendControllerException(6, "[oviWkt] nicht korrekt", "Die Ortsangabe ist nicht korrekt.", e);
			}

			if (StringUtils.isBlank(autorEmail)) throw new BackendControllerException(7, "[autorEmail] fehlt", "Die E-Mail-Adresse fehlt.");
			if (!isMaxLength(autorEmail, 300)) throw new BackendControllerException(8, "[autorEmail] zu lang", "Die E-Mail-Adresse ist zu lang.");
			if (!isEmail(autorEmail)) throw new BackendControllerException(9, "[autorEmail] nicht korrekt", "Die E-Mail-Adresse ist nicht g�ltig.");
			vorgang.setAutorEmail(autorEmail);
			vorgang.setHash(securityService.createHash(autorEmail+System.currentTimeMillis()));
			
			if (!isMaxLength(betreff, 300)) throw new BackendControllerException(10, "[betreff] zu lang", "Der Betreff ist zu lang. Es sind maximal 300 Zeichen erlaubt.");
			vorgang.setBetreff(betreff);
			
			vorgang.setDetails(details);
			
			vorgang.setDatum(new Date());
			vorgang.setStatus(EnumVorgangStatus.gemeldet);
			vorgang.setPrioritaet(EnumPrioritaet.mittel);
						
			vorgangDao.persist(vorgang);

			if (bild!=null) {
				try {
					imageService.setImageForVorgang(Base64.decode(bild.getBytes()), vorgang);
				} catch (Exception e) {
					throw new BackendControllerException(11, "[bild] nicht korrekt", "Das Bild ist fehlerhaft und kann nicht verarbeitewt werden.", e);
				}
				vorgangDao.merge(vorgang);
			}

			if (resultHashOnSubmit==true) sendOk(response, vorgang.getHash());
			else sendOk(response);

			mailService.sendVorgangBestaetigungMail(vorgang);
		} catch (Exception e) {
			logger.warn("Fehler bei BackendController.vorgang:", e);
			sendError(response, e);
		}
	}

	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/service/vorgangKOD</code><br/>
	 * Beschreibung: erstellt einen neuen Vorgang des kommunalen Ordnungsdienstes,
	 * im Gegensatz zum "normalen" Erstellen eines Vorgangs wird der Status
	 * direkt auf offen gesetzt, damit ist keine Best�tigungs E-Mail notwendig.
	 * @param typ Vorgangstyp
	 * @param kategorie Kategorie
	 * @param oviWkt Position als WKT
	 * @param autorEmail E-Mail-Adresse des Erstellers
	 * @param betreff Betreff
	 * @param details Details
	 * @param bild Foto base64 kodiert
	 * @param response Response in das das Ergebnis direkt geschrieben wird
	 */
	@RequestMapping(value="/vorgangKOD", method = RequestMethod.POST)
	@ResponseBody
	public void vorgangKOD(
			@RequestParam(value = "typ", required = false) String typ, 
			@RequestParam(value = "kategorie", required = false) Long kategorie,
			@RequestParam(value = "oviWkt", required = false) String oviWkt,
			@RequestParam(value = "autorEmail", required = false) String autorEmail,
			@RequestParam(value = "betreff", required = false) String betreff,
			@RequestParam(value = "details", required = false) String details,
			@RequestParam(value = "bild", required = false) String bild,
			@RequestParam(value = "authCode", required = false) String authCode,
			HttpServletResponse response) {
		try {
      if (settingsService.getPropertyValue("kod.auth_code") == null) {
        throw new BackendControllerException(13, "[authCode] nicht konfiguriert", "Es wurde kein kod.auth_code konfiguriert.");
      }
      logger.info("kod.auth_code" + settingsService.getPropertyValue("kod.auth_code"));
      logger.info("param authCode" + authCode);
      if (!settingsService.getPropertyValue("kod.auth_code").equals(authCode)) {
        throw new BackendControllerException(12, "[authCode] nicht korrekt", "Falscher AuthCode");
      }
			Vorgang vorgang = new Vorgang();
			
			if (StringUtils.isBlank(typ)) throw new BackendControllerException(1, "[typ] fehlt", "Der Typ ist nicht angegeben.");
			vorgang.setTyp(EnumVorgangTyp.valueOf(typ));
			if (vorgang.getTyp()==null) throw new BackendControllerException(2, "[typ] nicht korrekt", "Der Typ ist nicht korrekt.");
			
			if (kategorie==null) throw new BackendControllerException(3, "[kategorie] fehlt", "Die Angaben zur Kategorie fehlen.");
			vorgang.setKategorie(kategorieDao.findKategorie(kategorie));
			if (vorgang.getKategorie()==null 
					|| vorgang.getKategorie().getParent()==null
					|| vorgang.getKategorie().getParent().getTyp()!=vorgang.getTyp()) throw new BackendControllerException(4, "[kategorie] nicht korrekt", "Die Kategorie ist nicht g�ltig.");
			
			if (oviWkt==null) throw new BackendControllerException(5, "[oviWkt] fehlt", "Die Orstangabe fehlt.");
			try {
				vorgang.setOviWkt(oviWkt);
			}catch (Exception e) {
				throw new BackendControllerException(6, "[oviWkt] nicht korrekt", "Die Ortsangabe ist nicht korrekt.", e);
			}

			if (StringUtils.isBlank(autorEmail)) throw new BackendControllerException(7, "[autorEmail] fehlt", "Die E-Mail-Adresse fehlt.");
			if (!isMaxLength(autorEmail, 300)) throw new BackendControllerException(8, "[autorEmail] zu lang", "Die E-Mail-Adresse ist zu lang.");
			if (!isEmail(autorEmail)) throw new BackendControllerException(9, "[autorEmail] nicht korrekt", "Die E-Mail-Adresse ist nicht g�ltig.");
			vorgang.setAutorEmail(autorEmail);
			
			if (!isMaxLength(betreff, 300)) throw new BackendControllerException(10, "[betreff] zu lang", "Der Betreff ist zu lang. Es sind maximal 300 Zeichen erlaubt.");
			vorgang.setBetreff(betreff);
			
			vorgang.setDetails(details);
            
			vorgang.setDatum(new Date());
			vorgang.setStatus(EnumVorgangStatus.offen);
			vorgang.setPrioritaet(EnumPrioritaet.mittel);
						
			vorgangDao.persist(vorgang);
            
            if (bild!=null) {
				try {
					imageService.setImageForVorgang(Base64.decode(bild.getBytes()), vorgang);
				} catch (Exception e) {
					throw new BackendControllerException(11, "[bild] nicht korrekt", "Das Bild ist fehlerhaft und kann nicht verarbeitewt werden.", e);
				}
				vorgangDao.merge(vorgang);
			}

			sendOk(response);

			vorgang.setZustaendigkeit(classificationService.calculateZustaendigkeitforVorgang(vorgang).getId());
			vorgang.setZustaendigkeitStatus(EnumZustaendigkeitStatus.zugewiesen);
			vorgangDao.merge(vorgang);

			sendOk(response, vorgang.getId().toString());
		} catch (Exception e) {
			logger.warn("Fehler bei BackendController.vorgangKOD:", e);
			sendError(response, e);
		}
	}

	/**
	 * Pr�ft ob der String eine g�ltige E-Mail-Adresse ist
	 * @param email String mit der E-Mail-Adresse
	 * @return <code>true</code> - g�ltige E-Mail-Adresse
	 */
	private static boolean isEmail(String email)
	{
		if (!Assert.matches(email, "^\\S+@\\S+\\.[A-Za-z]{2,6}$")) return false;
		else return true;
	}

	
	/**
	 * Pr�ft ob der String zu lang ist
	 * @param str String dessen L�nge gepr�ft werden soll
	 * @param maxLength maximale L�nge
	 * @return <code>true</code> - String ist nicht zu lang
	 */
	private static boolean isMaxLength(String str, int maxLength)
	{
		if (str==null || str.length()<=maxLength) return true;
		else return true;
	}


	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/service/vorgangBestaetigung</code><br/>
	 * Beschreibung: Vorgang best�tigen
	 * @param hash Hash zum Best�tigen
	 * @return View die angezeigt werden soll
	 */
	@RequestMapping(value="/vorgangBestaetigung")
	public String vorgangBestaetigung(@RequestParam(value = "hash", required = false) String hash) {
		
		try {
			if (StringUtils.isBlank(hash)) throw new BackendControllerException(101, "[hash] fehlt");
			Vorgang vorgang = vorgangDao.findVorgangByHash(hash);
			if (vorgang==null) throw new BackendControllerException(102, "[hash] nicht korrekt");

			for (Verlauf verlauf : vorgang.getVerlauf()) verlauf.getTyp();
			
			if (vorgang.getStatus()!=EnumVorgangStatus.gemeldet)  throw new BackendControllerException(103, "Vorgang wurde bereits best�tigt");

			vorgang.setStatus(EnumVorgangStatus.offen);
						
			verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.vorgangBestaetigung, null, null);
			vorgangDao.merge(vorgang);

			vorgang.setZustaendigkeit(classificationService.calculateZustaendigkeitforVorgang(vorgang).getId());
			vorgang.setZustaendigkeitFrontend(securityService.getZustaendigkeit(vorgang.getZustaendigkeit()).getL());
			vorgang.setZustaendigkeitStatus(EnumZustaendigkeitStatus.zugewiesen);
			
			vorgangDao.merge(vorgang);
			
			return "backend/bestaetigungOk";
			
		} catch (Exception e) {
			logger.warn(e);
			return "backend/bestaetigungFehler";
		}
	}


	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/service/unterstuetzer</code><br/>
	 * Beschreibung: erstellt eine Unterst�tzung f�r ein Vorgang
	 * @param vorgang Vorgang
	 * @param email E-Mail-Adresse des Erstellers
	 * @param resultHashOnSubmit <code>true</code> - gibt den Hash zum Best�tigen als Ergebnis zur�ck
	 * @param response Response in das das Ergebnis direkt geschrieben wird
	 */
	@RequestMapping(value="/unterstuetzer", method = RequestMethod.POST)
	@ResponseBody
	public void unterstuetzer(
			@RequestParam(value = "vorgang", required = false) Long vorgang, 
			@RequestParam(value = "email", required = false) String email, 
			@RequestParam(value = "resultHashOnSubmit", required = false) Boolean resultHashOnSubmit, 
			HttpServletResponse response) {
		if (resultHashOnSubmit==null) resultHashOnSubmit=false;
		try {
			Unterstuetzer unterstuetzer = new Unterstuetzer();
			if (vorgang==null) throw new BackendControllerException(201, "[vorgang] fehlt", "Die Unterst�tzung ist keiner Meldung zugeordnet.");
			unterstuetzer.setVorgang(vorgangDao.findVorgang(vorgang));
			if (unterstuetzer.getVorgang()==null) throw new BackendControllerException(202, "[vorgang] nicht korrekt", "Die Unterst�tzung ist keiner Meldung zugeordnet.");
			
			if (StringUtils.isBlank(email)) throw new BackendControllerException(203, "[email] fehlt", "Die E-Mail-Adresse fehlt.");
			if (!isMaxLength(email, 300)) throw new BackendControllerException(204, "[email] zu lang", "Die E-Mail-Adresse ist zu lang.");
			if (!isEmail(email)) throw new BackendControllerException(205, "[email] nicht korrekt", "Die E-Mail-Adresse ist nicht g�ltig.");
			unterstuetzer.setHash(securityService.createHash(unterstuetzer.getVorgang().getId()+email));
			if (vorgangDao.findUnterstuetzer(unterstuetzer.getHash())!=null) throw new BackendControllerException(206, "[email] wurde bereits f�r den [vorgang] verwendet", "Sie k�nnen die Meldung nicht mehrmals unterst�tzen.");
			if (StringUtils.equalsIgnoreCase(unterstuetzer.getVorgang().getAutorEmail(), email)) throw new BackendControllerException(207, "[email] der autor des [vorgang] kann keine unterst�tzung f�r den [vorgang] abgeben", "Die Unterst�tzungsmeldung konnte nicht abgesetzt werden, da Sie Ihre eigene Meldung nicht unterst�tzen d�rfen.");
			
			unterstuetzer.setDatum(new Date());

			vorgangDao.persist(unterstuetzer);
			
			if (resultHashOnSubmit==true) sendOk(response, unterstuetzer.getHash());
			else sendOk(response);

			mailService.sendUnterstuetzerBestaetigungMail(unterstuetzer, email);
			
		} catch (Exception e) {
			logger.warn(e);
			sendError(response, e);
		}
	}

	
	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/service/unterstuetzerBestaetigung</code><br/>
	 * Beschreibung: Unterst�tzung best�tigen
	 * @param hash Hash zum Best�tigen
	 * @return View die angezeigt werden soll
	 */
	@RequestMapping(value="/unterstuetzerBestaetigung")
	public String unterstuetzerBestaetigung(@RequestParam(value = "hash", required = false) String hash) {
		
		try {
			if (StringUtils.isBlank(hash)) throw new BackendControllerException(301, "[hash] fehlt");
			Unterstuetzer unterstuetzer = vorgangDao.findUnterstuetzer(hash);
			if (unterstuetzer==null) throw new BackendControllerException(302, "[hash] nicht korrekt");
			
			if (unterstuetzer.getDatumBestaetigung()!=null)  throw new BackendControllerException(303, "Unterst�tzer wurde bereits best�tigt");

			unterstuetzer.setDatumBestaetigung(new Date());
			
			verlaufDao.addVerlaufToVorgang(unterstuetzer.getVorgang(), EnumVerlaufTyp.unterstuetzerBestaetigung, null, null);
			vorgangDao.merge(unterstuetzer);

			return "backend/bestaetigungOk";
			
		} catch (Exception e) {
			logger.warn(e);
			return "backend/bestaetigungFehler";
		}
	}

	
	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/service/missbrauchsmeldung</code><br/>
	 * Beschreibung: erstellt eine Missbrauchsmeldung f�r einen Vorgang
	 * @param vorgang Vorgang
	 * @param text Text der Missbrauchsmeldung
	 * @param email E-Mail-Adresse des Erstellers
	 * @param resultHashOnSubmit <code>true</code> - gibt den Hash zum Best�tigen als Ergebnis zur�ck
	 * @param response Response in das das Ergebnis direkt geschrieben wird
	 */
	@RequestMapping(value="/missbrauchsmeldung", method = RequestMethod.POST)
	@ResponseBody
	public void missbrauchsmeldung(
			@RequestParam(value = "vorgang", required = false) Long vorgang, 
			@RequestParam(value = "text", required = false) String text, 
			@RequestParam(value = "email", required = false) String email, 
			@RequestParam(value = "resultHashOnSubmit", required = false) Boolean resultHashOnSubmit, 
			HttpServletResponse response) {
		if (resultHashOnSubmit==null) resultHashOnSubmit=false;
		try {
			Missbrauchsmeldung missbrauchsmeldung = new Missbrauchsmeldung();
			if (vorgang==null) throw new BackendControllerException(401, "[vorgang] fehlt", "Die Missbrauchsmeldung ist keiner Meldung zugeordnet.");
			missbrauchsmeldung.setVorgang(vorgangDao.findVorgang(vorgang));
			if (missbrauchsmeldung.getVorgang()==null) throw new BackendControllerException(402, "[vorgang] nicht korrekt", "Die Missbrauchsmeldung ist keiner Meldung zugeordnet.");

			if (StringUtils.isBlank(text)) throw new BackendControllerException(403, "[text] fehlt", "Die Begr�ndung fehlt.");
			missbrauchsmeldung.setText(text);
			
			if (StringUtils.isBlank(email)) throw new BackendControllerException(404, "[email] fehlt", "Die E-Mail-Adresse fehlt.");
			if (!isMaxLength(email, 300)) throw new BackendControllerException(405, "[email] zu lang", "Die E-Mail-Adresse ist zu lang.");
			if (!isEmail(email)) throw new BackendControllerException(406, "[email] nicht korrekt", "Die E-Mail-Adresse ist nicht g�ltig.");
            missbrauchsmeldung.setAutorEmail(email);
			missbrauchsmeldung.setHash(securityService.createHash(missbrauchsmeldung.getVorgang().getId()+email+System.currentTimeMillis()));
			
			missbrauchsmeldung.setDatum(new Date());

			vorgangDao.persist(missbrauchsmeldung);
			
			mailService.sendMissbrauchsmeldungBestaetigungMail(missbrauchsmeldung, email);

			if (resultHashOnSubmit==true) sendOk(response, missbrauchsmeldung.getHash());
			else sendOk(response);
		} catch (Exception e) {
			logger.warn(e);
			sendError(response, e);
		}
	}

	
	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/service/lobHinweiseKritik</code><br/>
	 * Beschreibung: erstellt Lob, Hinweise oder Kritik zu einem Vorgang
	 * @param vorgang Vorgang
	 * @param email E-Mail-Adresse des Erstellers
     * @param freitext Freitext
	 * @param response Response in das das Ergebnis direkt geschrieben wird
	 */
	@RequestMapping(value="/lobHinweiseKritik", method = RequestMethod.POST)
	@ResponseBody
	public void lobHinweiseKritik(
			@RequestParam(value = "vorgang", required = false) Long vorgang,
			@RequestParam(value = "email", required = false) String email, 
			@RequestParam(value = "freitext", required = false) String freitext, 
			HttpServletResponse response) {
		try {
			LobHinweiseKritik lobHinweiseKritik = new LobHinweiseKritik();
			if (vorgang==null) throw new BackendControllerException(401, "[vorgang] fehlt", "Lob, Hinweise oder Kritik kann/k�nnen keiner Meldung zugeordnet werden.");
			lobHinweiseKritik.setVorgang(vorgangDao.findVorgang(vorgang));
			if (lobHinweiseKritik.getVorgang()==null) throw new BackendControllerException(402, "[vorgang] nicht korrekt", "Lob, Hinweise oder Kritik kann/k�nnen keiner Meldung zugeordnet werden.");

			if (StringUtils.isBlank(email)) throw new BackendControllerException(404, "[email] fehlt", "Die E-Mail-Adresse fehlt.");
			if (!isMaxLength(email, 300)) throw new BackendControllerException(405, "[email] zu lang", "Die E-Mail-Adresse ist zu lang.");
			if (!isEmail(email)) throw new BackendControllerException(406, "[email] nicht korrekt", "Die E-Mail-Adresse ist nicht g�ltig.");
            lobHinweiseKritik.setAutorEmail(email);
            
            // aktuelle Zust�ndigkeit des Vorgangs bestimmen
            String zustaendigkeit = lobHinweiseKritik.getVorgang().getZustaendigkeit();
            
            // Empf�nger gefunden?
            Boolean empfaengerGefunden = false;
            
            // falls aktuelle Zust�ndigkeit des Vorgangs nicht NULL oder leer ist und gleichzeitig akzeptiert ist
            if (zustaendigkeit != null && zustaendigkeit != "" && lobHinweiseKritik.getVorgang().getZustaendigkeitStatus() == EnumZustaendigkeitStatus.akzeptiert) {

                String empfaengerEmail = new String();
                
                // alle Nutzernamen dieser Zust�ndigkeit bestimmen
                List<String> allUserNamesForRole = securityService.getAllUserNamesForRole(zustaendigkeit);
                
                // denjenigen Nutzernamen aus dieser Zust�ndigkeit bestimmen, der gem�� dem Verlauf die letzte Bearbeitung am Vorgang durchgef�hrt hat
                String empfaenger = verlaufDao.findLastUserForVorgangAndZustaendigkeit(lobHinweiseKritik.getVorgang(), allUserNamesForRole);
                
                // falls dieser gefunden wurde
                if (empfaenger != null && empfaenger != "") {
                    empfaengerGefunden = true;
                    
                    // String mit dessen E-Mail-Adresse belegen
                    empfaengerEmail = securityService.getUserEmailForRoleByName(empfaenger, zustaendigkeit);
                    
                    // falls der String mit dessen E-Mail-Adresse nicht NULL ist
                    if (empfaengerEmail != null && empfaengerEmail != "") {
                    
                        // Empf�nger-E-Mail-Adresse f�r Lob, Hinweise oder Kritik auf zuvor gef�llten String setzen
                        lobHinweiseKritik.setEmpfaengerEmail(empfaengerEmail);
                    
                        // Lob, Hinweise oder Kritik als E-Mail versenden
                        mailService.sendLobHinweiseKritikMail(lobHinweiseKritik.getVorgang(), email, empfaengerEmail, freitext);
                    }
                }
            }
            
            // ansonsten: falls Empf�nger zuvor nicht gefunden wurde und aktuelle Zust�ndigkeit des Vorgangs nicht NULL oder leer ist, aber eben auch nicht akzeptiert ist
            else if (empfaengerGefunden == false && zustaendigkeit != null && zustaendigkeit != "") {

                String empfaengerEmail = new String();
                Short zaehler = 0;
                
                // alle Empf�nger redaktioneller E-Mails dieser Zust�ndigkeit bestimmen, die zugleich auch Lob, Hinweise oder Kritik als E-Mail empfangen sollen
                List<RedaktionEmpfaenger> allEmpfaengerLobHinweiseKritikForZustaendigkeit = redaktionEmpfaengerDao.getEmpfaengerListLobHinweiseKritikForZustaendigkeit(lobHinweiseKritik.getVorgang().getZustaendigkeit());
                
                // falls diese gefunden wurden
                if (allEmpfaengerLobHinweiseKritikForZustaendigkeit.size() > 0 && !allEmpfaengerLobHinweiseKritikForZustaendigkeit.isEmpty()) {
                    empfaengerGefunden = true;
                
                    // diese durchlaufen
                    for (RedaktionEmpfaenger empfaengerLobHinweiseKritikForZustaendigkeit : allEmpfaengerLobHinweiseKritikForZustaendigkeit) {
                        
                        // falls es nur einer ist
                        if (allEmpfaengerLobHinweiseKritikForZustaendigkeit.size() == 1 || zaehler == 0) {
                        
                            // String mit dessen E-Mail-Adresse belegen
                            empfaengerEmail = empfaengerLobHinweiseKritikForZustaendigkeit.getEmail();
                        }
                        
                        // ansonsten
                        else {
                            
                            // kommaseparierten String mit der aktuellen E-Mail-Adresse fortf�hren
                            empfaengerEmail = empfaengerEmail + ", " + empfaengerLobHinweiseKritikForZustaendigkeit.getEmail();
                        }
                        zaehler++;
                        
                        // Lob, Hinweise oder Kritik als E-Mail(s) versenden
                        mailService.sendLobHinweiseKritikMail(lobHinweiseKritik.getVorgang(), email, empfaengerLobHinweiseKritikForZustaendigkeit.getEmail(), freitext);
                    }
                    
                    // Empf�nger-E-Mail-Adresse f�r Lob, Hinweise oder Kritik auf in vorhergehender Schleife gef�llten String setzen
                    lobHinweiseKritik.setEmpfaengerEmail(empfaengerEmail);
                }
            }
            
            if (StringUtils.isBlank(freitext)) throw new BackendControllerException(403, "[freitext] fehlt", "Der Freitext fehlt.");
			lobHinweiseKritik.setFreitext(freitext);
            
			lobHinweiseKritik.setDatum(new Date());
            
            verlaufDao.addVerlaufToVorgang(lobHinweiseKritik.getVorgang(), EnumVerlaufTyp.lobHinweiseKritik, null, null);

			vorgangDao.persist(lobHinweiseKritik);

			sendOk(response);
		} catch (Exception e) {
			logger.warn(e);
			sendError(response, e);
		}
	}

	
	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/service/missbrauchsmeldungBestaetigung</code><br/>
	 * Beschreibung: Vorgang best�tigen
	 * @param hash Hash zum Best�tigen
	 * @return View die angezeigt werden soll
	 */
	@RequestMapping(value="/missbrauchsmeldungBestaetigung")
	public String missbrauchsmeldungBestaetigung(@RequestParam(value = "hash", required = false) String hash) {
		
		try {
			if (StringUtils.isBlank(hash)) throw new BackendControllerException(501, "[hash] fehlt");
			Missbrauchsmeldung missbrauchsmeldung = vorgangDao.findMissbrauchsmeldung(hash);
			if (missbrauchsmeldung==null) throw new BackendControllerException(502, "[hash] nicht korrekt");
			
			if (missbrauchsmeldung.getDatumBestaetigung()!=null)  throw new BackendControllerException(503, "Missbrauchsmeldung wurde bereits best�tigt");

			missbrauchsmeldung.setDatumBestaetigung(new Date());
			
			verlaufDao.addVerlaufToVorgang(missbrauchsmeldung.getVorgang(), EnumVerlaufTyp.missbrauchsmeldungBestaetigung, null, null);
			vorgangDao.merge(missbrauchsmeldung);

			return "backend/bestaetigungOk";
			
		} catch (Exception e) {
			logger.warn(e);
			return "backend/bestaetigungFehler";
		}
	}
	

	
	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/service/vorgangLoeschen</code><br/>
	 * Beschreibung: Vorgang l�schen
	 * @param hash Hash zum L�schen
	 * @return View die angezeigt werden soll
	 */
	@RequestMapping(value="/vorgangLoeschen")
	public String vorgangloeschen(@RequestParam(value = "hash", required = false) String hash) {
		
		try {
			if (StringUtils.isBlank(hash)) throw new BackendControllerException(601, "[hash] fehlt");
			Vorgang vorgang = vorgangDao.findVorgangByHash(hash);
			if (vorgang==null) throw new BackendControllerException(602, "[hash] nicht korrekt");
			
			if ((vorgang.getStatus()==EnumVorgangStatus.gemeldet || vorgang.getStatus()==EnumVorgangStatus.offen)
					&& vorgang.getUnterstuetzer().size()==0 && vorgang.getMissbrauchsmeldungen().size()==0)
			{
				vorgang.setStatus(EnumVorgangStatus.geloescht);
				vorgangDao.merge(vorgang);
				
			} else throw new BackendControllerException(103, "Vorgang kann nicht mehr gel�scht werden"); 
			
			return "backend/vorgangLoeschenOk";
			
		} catch (Exception e) {
			return "backend/vorgangLoeschenFehler";
		}
	}


	/**
	 * 
	 * @param oviWkt �berwachte Fl�che als WKT
	 * @param probleme Probleme �berwachen?
	 * @param problemeKategorien Liste der �berwachten Kategorien bei den Problemen
	 * @param ideen Ideen �berwachen?
	 * @param ideenKategorien Liste der �berwachten Kategorien bei den Ideen
	 * @param response
	 */
	@RequestMapping(value="/geoRss", method = RequestMethod.POST)
	@ResponseBody
	public void geoRss(
			@RequestParam(value = "oviWkt", required = false) String oviWkt, 
			@RequestParam(value = "probleme", required = false) Boolean probleme, 
			@RequestParam(value = "problemeKategorien", required = false) String problemeKategorien, 
			@RequestParam(value = "ideen", required = false) Boolean ideen, 
			@RequestParam(value = "ideenKategorien", required = false) String ideenKategorien, 
			HttpServletResponse response) {
		
		try {
			logger.debug("geoRss oviWkt: "+oviWkt);
			GeoRss geoRss = new GeoRss();
			if (StringUtils.isBlank(oviWkt)) throw new BackendControllerException(701, "[oviWkt] fehlt", "Die Ortsangaben fehlen");
			try {
				geoRss.setOviWkt(oviWkt);
			}catch (Exception e) {
				throw new BackendControllerException(702, "[oviWkt] nicht korrekt", "Die Ortsangaben sind nicht korrekt.", e);
			}
			if (probleme==null) throw new BackendControllerException(703, "[probleme] fehlt");
			geoRss.setProbleme(probleme);
			geoRss.setProblemeKategorien(problemeKategorien);
			if (ideen==null) throw new BackendControllerException(704, "[ideen] fehlt");
			geoRss.setIdeen(ideen);
			geoRss.setIdeenKategorien(ideenKategorien);
			
			vorgangDao.persist(geoRss);
			
			sendOk(response, geoRss.getId()+"");
		} catch (Exception e) {
			logger.warn(e);
			sendError(response, e);
		}
	}
	
	
	/**
	 * Sendet eine Fehlermeldung
	 */
	private void sendError(HttpServletResponse response, Exception exception) {

		try {
			response.setCharacterEncoding("utf-8");
			response.setHeader("Content-Type", "text/plain;charset=UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getOutputStream().write(exception.getMessage().getBytes());
			//response.getWriter().print(exception.getMessage());
			response.flushBuffer();
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}


	/**
	 * Sendet ein Ok
	 */
	private void sendOk(HttpServletResponse response) {

		try {
			response.setCharacterEncoding("utf-8");
			response.setHeader("Content-Type", "text/plain;charset=UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			response.flushBuffer();
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}


	/**
	 * Sendet ein Ok incl. Content
	 */
	private void sendOk(HttpServletResponse response, String content) {

		try {
			response.setCharacterEncoding("utf-8");
			response.setHeader("Content-Type", "text/plain;charset=UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().append(content);
			response.flushBuffer();
		} catch (Exception e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
}
