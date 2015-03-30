package de.fraunhofer.igd.klarschiff.web;

import de.fraunhofer.igd.klarschiff.dao.AuftragDao;
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
import de.fraunhofer.igd.klarschiff.vo.Auftrag;
import de.fraunhofer.igd.klarschiff.vo.EnumAuftragStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet;
import de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.EnumZustaendigkeitStatus;
import de.fraunhofer.igd.klarschiff.vo.GeoRss;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;
import de.fraunhofer.igd.klarschiff.vo.LobHinweiseKritik;
import de.fraunhofer.igd.klarschiff.vo.Missbrauchsmeldung;
import de.fraunhofer.igd.klarschiff.vo.RedaktionEmpfaenger;
import de.fraunhofer.igd.klarschiff.vo.Unterstuetzer;
import de.fraunhofer.igd.klarschiff.vo.Verlauf;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Der Controller dient als Schnittstelle für das Frontend
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
	AuftragDao auftragDao;
	
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
  
  ObjectMapper mapper = new ObjectMapper();

	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/service/vorgang</code><br/>
	 * Beschreibung: erstellt einen neuen Vorgang
	 * @param authCode
	 * @param autorEmail E-Mail-Adresse des Erstellers
	 * @param betreff Betreff
	 * @param bild Foto base64 kodiert
	 * @param details Details
	 * @param fotowunsch
	 * @param kategorie Kategorie
	 * @param oviWkt Position als WKT
	 * @param positionWGS84
	 * @param resultObjectOnSubmit <code>true</code> - gibt den neuen Vorgangs als Ergebnis zurück
	 * @param resultHashOnSubmit <code>true</code> - gibt den Hash zum Bestätigen als Ergebnis zurück
	 * @param typ Vorgangstyp
	 * @param response Response in das das Ergebnis direkt geschrieben wird
	 */
	@RequestMapping(value="/vorgang", method = RequestMethod.POST)
	@ResponseBody
	public void vorgang(
			@RequestParam(value = "authCode", required = false) String authCode,
			@RequestParam(value = "autorEmail", required = false) String autorEmail,
			@RequestParam(value = "betreff", required = false) String betreff,
			@RequestParam(value = "bild", required = false) String bild,
			@RequestParam(value = "details", required = false) String details,
			@RequestParam(value = "fotowunsch", required = false) Boolean fotowunsch, 
			@RequestParam(value = "kategorie", required = false) Long kategorie,
			@RequestParam(value = "oviWkt", required = false) String oviWkt,
			@RequestParam(value = "positionWGS84", required = false) String positionWGS84,
			@RequestParam(value = "resultObjectOnSubmit", required = false) Boolean resultObjectOnSubmit, 
			@RequestParam(value = "resultHashOnSubmit", required = false) Boolean resultHashOnSubmit, 
			@RequestParam(value = "typ", required = false) String typ, 
			HttpServletResponse response) {
    
		if (resultHashOnSubmit == null) {
      resultHashOnSubmit = false;
    }
		if (resultObjectOnSubmit == null) {
      resultObjectOnSubmit = false;
    }
		try {
			Vorgang vorgang = new Vorgang();
			
			if (StringUtils.isBlank(typ)) throw new BackendControllerException(1, "[typ] fehlt", "Der Typ ist nicht angegeben.");
			
			if (kategorie==null) throw new BackendControllerException(3, "[kategorie] fehlt", "Die Angaben zur Kategorie fehlen.");
      
			if (StringUtils.isBlank(autorEmail)) throw new BackendControllerException(7, "[autorEmail] fehlt", "Die E-Mail-Adresse fehlt.");
			if (!isMaxLength(autorEmail, 300)) throw new BackendControllerException(8, "[autorEmail] zu lang", "Die E-Mail-Adresse ist zu lang.");
			if (!isEmail(autorEmail)) throw new BackendControllerException(9, "[autorEmail] nicht korrekt", "Die E-Mail-Adresse ist nicht gültig.");
			vorgang.setAutorEmail(autorEmail);
			vorgang.setHash(securityService.createHash(autorEmail+System.currentTimeMillis()));
			
			vorgang.setDatum(new Date());
			vorgang.setPrioritaet(EnumPrioritaet.mittel);
      if (fotowunsch == null) {
        fotowunsch = false;
      }
      
      vorgangParameterUebernehmen(autorEmail, vorgang, typ, kategorie, positionWGS84, oviWkt,
          betreff, details, fotowunsch, bild, false);
      
      Boolean intern = false;
      if (authCode != null && authCode.equals(settingsService.getPropertyValue("auth.kod_code")) && vorgang.autorIntern()) {
        intern = true;
        vorgang.setStatus(EnumVorgangStatus.offen);
        
        vorgang.setZustaendigkeit(classificationService.calculateZustaendigkeitforVorgang(vorgang).getId());
        vorgang.setZustaendigkeitStatus(EnumZustaendigkeitStatus.zugewiesen);
      } else {
        vorgang.setStatus(EnumVorgangStatus.gemeldet);
      }
      
			vorgangDao.persist(vorgang);

      if (resultHashOnSubmit) {
        sendOk(response, vorgang.getHash());
      } else if (resultObjectOnSubmit) {
        sendOk(response, mapper.writeValueAsString(vorgang));
      } else {
        sendOk(response);
      }

      if (!intern) {
        mailService.sendVorgangBestaetigungMail(vorgang);
      }
		} catch (Exception e) {
			logger.warn("Fehler bei BackendController.vorgang:", e);
			sendError(response, e);
		}
	}
  
  @RequestMapping(value="/vorgangAktualisieren", method = RequestMethod.POST)
	@ResponseBody
	public void vorgangAktualisieren(
      @RequestParam(value = "id", required = false) Long id,
			@RequestParam(value = "authCode", required = false) String authCode,
			@RequestParam(value = "autorEmail", required = false) String autorEmail,
			@RequestParam(value = "betreff", required = false) String betreff,
			@RequestParam(value = "bild", required = false) String bild,
			@RequestParam(value = "details", required = false) String details,
			@RequestParam(value = "fotowunsch", required = false) Boolean fotowunsch, 
			@RequestParam(value = "kategorie", required = false) Long kategorie,
			@RequestParam(value = "oviWkt", required = false) String oviWkt,
			@RequestParam(value = "positionWGS84", required = false) String positionWGS84,
			@RequestParam(value = "typ", required = false) String typ, 
      @RequestParam(value = "status", required = false) String status, 
      @RequestParam(value = "statusKommentar", required = false) String statusKommentar,
      @RequestParam(value = "prioritaet", required = false) Integer prioritaet,
      @RequestParam(value = "delegiertAn", required = false) String delegiertAn,
      @RequestParam(value = "auftragStatus", required = false) String auftragStatus,
      @RequestParam(value = "auftragPrioritaet", required = false) Integer auftragPrioritaet,
      
			HttpServletResponse response) throws BackendControllerException {
    
    try {
      if (id == null) {
        if (StringUtils.isBlank(typ)) {
          throw new BackendControllerException(1, "[id] fehlt", "Ohne id kann kein Vorgang aktualisiert werden.");
        }
      }
			if (StringUtils.isBlank(autorEmail)) throw new BackendControllerException(7, "[autorEmail] fehlt", "Die E-Mail-Adresse fehlt.");
			if (!isMaxLength(autorEmail, 300)) throw new BackendControllerException(8, "[autorEmail] zu lang", "Die E-Mail-Adresse ist zu lang.");
			if (!isEmail(autorEmail)) throw new BackendControllerException(9, "[autorEmail] nicht korrekt", "Die E-Mail-Adresse ist nicht gültig.");

      Vorgang vorgang = vorgangDao.findVorgang(id);
      vorgangParameterUebernehmen(autorEmail, vorgang, typ, kategorie, positionWGS84, oviWkt,
          betreff, details, fotowunsch, bild, true);

      if (prioritaet != null) {
        if ((prioritaet - 1) > EnumPrioritaet.values().length) {
          throw new BackendControllerException(12, "[prioritaet] ungültig", "Die Priorität ist fehlerhaft und kann nicht verarbeitewt werden.");
        }
        
        EnumPrioritaet ep = EnumPrioritaet.values()[prioritaet];
        if(!vorgang.getPrioritaet().equals(ep)) {
          verlaufDao.persist(verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.prioritaet, vorgang.getPrioritaet().getText(), ep.getText(), autorEmail));
        }
        vorgang.setPrioritaet(ep);
      }

      if (status != null) {
        EnumVorgangStatus evs = EnumVorgangStatus.valueOf(status);
        if(!vorgang.getStatus().equals(evs)) {
          verlaufDao.persist(verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.status, vorgang.getStatus().getText(), evs.getText(), autorEmail));
        }
        vorgang.setStatus(evs);
      }

      if (statusKommentar != null) {
        if(!vorgang.getStatusKommentar().equals(statusKommentar)) {
          verlaufDao.persist(verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.statusKommentar, StringUtils.abbreviate(vorgang.getStatusKommentar(), 100), StringUtils.abbreviate(statusKommentar, 100), autorEmail));
        }
        vorgang.setStatusKommentar(statusKommentar);
      }

      if (delegiertAn != null || auftragStatus != null || auftragPrioritaet != null) {
        if (authCode == null) {
          throw new BackendControllerException(13, "[authCode] fehlt", "Der authCode fehlt.");
        }
        if (!authCode.equals(settingsService.getPropertyValue("auth.kod_code"))) {
          throw new BackendControllerException(14, "[authCode] ungültig", "Der Übergebene authCode ist ungültig.");
        }
        
        if (delegiertAn != null) {
          if(!vorgang.getDelegiertAn().equals(delegiertAn)) {
            verlaufDao.persist(verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.delegiertAn, vorgang.getDelegiertAn(), delegiertAn, autorEmail));
          }
          vorgang.setDelegiertAn(delegiertAn);
        }

        if (auftragStatus != null || auftragPrioritaet != null) {
          Auftrag auftrag = vorgang.getAuftrag();
          if (auftrag == null) {
            throw new BackendControllerException(15, "[auftragStatus] ungültig", "Ohne Auftrag kann der auftragStatus nicht aktualisiert werden.");
          }
          
          if (auftragStatus != null) {
            auftrag.setStatus(EnumAuftragStatus.valueOf(auftragStatus));
          }

          if (auftragPrioritaet != null) {
            auftrag.setPrioritaet(auftragPrioritaet);
          }
          vorgang.setAuftrag(auftrag);
        }
      }

      vorgangDao.persist(vorgang);
      sendOk(response, mapper.writeValueAsString(vorgang));
    } catch (Exception e) {
			logger.warn("Fehler bei BackendController.vorgang:", e);
			sendError(response, e);
		}
  }
  
  private void vorgangParameterUebernehmen(
      String autorEmail,
      Vorgang vorgang,
      String typ,
      Long kategorie,
      String positionWGS84,
      String oviWkt,
      String betreff,
      String details,
      Boolean fotowunsch,
      String bild,
      Boolean verlaufErgaenzen
  ) throws BackendControllerException {
    
    if(verlaufErgaenzen == null) {
      verlaufErgaenzen = false;
    }
    
    if (typ != null) {
      EnumVorgangTyp evt = EnumVorgangTyp.valueOf(typ);
      if(verlaufErgaenzen && !vorgang.getTyp().equals(evt)) {
        verlaufDao.persist(verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.typ, vorgang.getTyp().getText(), evt.getText(), autorEmail));
      }
      vorgang.setTyp(evt);
      if (vorgang.getTyp() == null) {
        throw new BackendControllerException(2, "[typ] nicht korrekt", "Der Typ ist nicht korrekt.");
      }
    }
    
    if (kategorie != null) {
      Kategorie newKat = kategorieDao.findKategorie(kategorie);
      
      if(verlaufErgaenzen && !vorgang.getKategorie().getId().equals(newKat.getId())) {
        verlaufDao.persist(verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.kategorie, vorgang.getKategorie().getParent().getName() + " / " + vorgang.getKategorie().getName(), newKat.getParent().getName() + " / " + newKat.getName(), autorEmail));
      }
      vorgang.setKategorie(newKat);
      if (vorgang.getKategorie() == null 
          || vorgang.getKategorie().getParent() == null
          || vorgang.getKategorie().getParent().getTyp() != vorgang.getTyp()) {
        throw new BackendControllerException(4, "[kategorie] nicht korrekt", "Die Kategorie ist nicht gültig.");
      }
    }
    
    if (positionWGS84 != null) {
      try {
        vorgang.setPositionWGS84(positionWGS84);
      } catch (Exception e) {
        throw new BackendControllerException(12, "[positionWGS84] nicht korrekt", "Die Ortsangabe ist nicht korrekt.", e);
      }
    }
      
    if (oviWkt != null) {
      try {
        vorgang.setOviWkt(oviWkt);
      } catch (Exception e) {
        throw new BackendControllerException(6, "[oviWkt] nicht korrekt", "Die Ortsangabe ist nicht korrekt.");
      }
    }
    
    if (vorgang.getOviWkt() == null) {
      throw new BackendControllerException(5, "[position] nicht korrekt", "Keine gültige Ortsangabe.");
    }
    
    if (betreff != null) {
      if (!isMaxLength(betreff, 300)) {
        throw new BackendControllerException(10, "[betreff] zu lang", "Der Betreff ist zu lang. Es sind maximal 300 Zeichen erlaubt.");
      }
      if(verlaufErgaenzen && !vorgang.getBetreff().equals(betreff)) {
        verlaufDao.persist(verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.betreff, StringUtils.abbreviate(vorgang.getBetreff(), 100), StringUtils.abbreviate(betreff, 100), autorEmail));
      }
      vorgang.setBetreff(betreff);
    }

    if (details != null) {
      if(verlaufErgaenzen && !vorgang.getDetails().equals(details)) {
        verlaufDao.persist(verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.detail, StringUtils.abbreviate(vorgang.getDetails(), 100), StringUtils.abbreviate(details, 100), autorEmail));
      }
      vorgang.setDetails(details);
    }
    
    if (fotowunsch != null) {
      if(verlaufErgaenzen && vorgang.getFotowunsch() != fotowunsch) {
        verlaufDao.persist(verlaufDao.addVerlaufToVorgang(vorgang, EnumVerlaufTyp.fotowunsch, vorgang.getFotowunsch() ? "aktiv" : "inaktiv", vorgang.getFotowunsch() ? "inaktiv" : "aktiv", autorEmail));
      }
      vorgang.setFotowunsch(fotowunsch);
    }
    
    if (bild != null && bild.getBytes().length > 0) {
      try {
        imageService.setImageForVorgang(Base64.decode(bild.getBytes()), vorgang);
      } catch (Exception e) {
        throw new BackendControllerException(11, "[bild] nicht korrekt", "Das Bild ist fehlerhaft und kann nicht verarbeitewt werden.", e);
      }
      vorgangDao.merge(vorgang);
    }
  }

	/**
	 * Prüft ob der String eine gültige E-Mail-Adresse ist
	 * @param email String mit der E-Mail-Adresse
	 * @return <code>true</code> - gültige E-Mail-Adresse
	 */
	private static boolean isEmail(String email)
	{
		if (!Assert.matches(email, "^\\S+@\\S+\\.[A-Za-z]{2,6}$")) return false;
		else return true;
	}

	
	/**
	 * Prüft ob der String zu lang ist
	 * @param str String dessen Länge geprüft werden soll
	 * @param maxLength maximale Länge
	 * @return <code>true</code> - String ist nicht zu lang
	 */
	private static boolean isMaxLength(String str, int maxLength)
	{
		if (str==null || str.length()<=maxLength) return true;
		else return true;
	}


	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/service/vorgangBestaetigung</code><br/>
	 * Beschreibung: Vorgang bestätigen
	 * @param hash Hash zum Bestätigen
	 * @return View die angezeigt werden soll
	 */
	@RequestMapping(value="/vorgangBestaetigung")
	public String vorgangBestaetigung(@RequestParam(value = "hash", required = false) String hash) {
		
		try {
			if (StringUtils.isBlank(hash)) throw new BackendControllerException(101, "[hash] fehlt");
			Vorgang vorgang = vorgangDao.findVorgangByHash(hash);
			if (vorgang==null) throw new BackendControllerException(102, "[hash] nicht korrekt");

			for (Verlauf verlauf : vorgang.getVerlauf()) verlauf.getTyp();
			
			if (vorgang.getStatus()!=EnumVorgangStatus.gemeldet)  throw new BackendControllerException(103, "Vorgang wurde bereits bestätigt");

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
	 * Beschreibung: erstellt eine Unterstützung für ein Vorgang
	 * @param vorgang Vorgang
	 * @param email E-Mail-Adresse des Erstellers
	 * @param resultObjectOnSubmit <code>true</code> - gibt den neuen Vorgangs als Ergebnis zurück
	 * @param resultHashOnSubmit <code>true</code> - gibt den Hash zum Bestätigen als Ergebnis zurück
	 * @param response Response in das das Ergebnis direkt geschrieben wird
	 */
	@RequestMapping(value="/unterstuetzer", method = RequestMethod.POST)
	@ResponseBody
	public void unterstuetzer(
			@RequestParam(value = "vorgang", required = false) Long vorgang, 
			@RequestParam(value = "email", required = false) String email, 
			@RequestParam(value = "resultObjectOnSubmit", required = false) Boolean resultObjectOnSubmit, 
			@RequestParam(value = "resultHashOnSubmit", required = false) Boolean resultHashOnSubmit, 
			HttpServletResponse response) {
		if (resultHashOnSubmit==null) resultHashOnSubmit=false;
		try {
			Unterstuetzer unterstuetzer = new Unterstuetzer();
			if (vorgang==null) throw new BackendControllerException(201, "[vorgang] fehlt", "Die Unterstützung ist keiner Meldung zugeordnet.");
			unterstuetzer.setVorgang(vorgangDao.findVorgang(vorgang));
			if (unterstuetzer.getVorgang()==null) throw new BackendControllerException(202, "[vorgang] nicht korrekt", "Die Unterstützung ist keiner Meldung zugeordnet.");
			
			if (StringUtils.isBlank(email)) throw new BackendControllerException(203, "[email] fehlt", "Die E-Mail-Adresse fehlt.");
			if (!isMaxLength(email, 300)) throw new BackendControllerException(204, "[email] zu lang", "Die E-Mail-Adresse ist zu lang.");
			if (!isEmail(email)) throw new BackendControllerException(205, "[email] nicht korrekt", "Die E-Mail-Adresse ist nicht gültig.");
			unterstuetzer.setHash(securityService.createHash(unterstuetzer.getVorgang().getId()+email));
			if (vorgangDao.findUnterstuetzer(unterstuetzer.getHash())!=null) throw new BackendControllerException(206, "[email] wurde bereits für den [vorgang] verwendet", "Sie können die Meldung nicht mehrmals unterstützen.");
			if (StringUtils.equalsIgnoreCase(unterstuetzer.getVorgang().getAutorEmail(), email)) throw new BackendControllerException(207, "[email] der autor des [vorgang] kann keine unterstützung für den [vorgang] abgeben", "Die Unterstützungsmeldung konnte nicht abgesetzt werden, da Sie Ihre eigene Meldung nicht unterstützen dürfen.");
			
			unterstuetzer.setDatum(new Date());

			vorgangDao.persist(unterstuetzer);
			
      if (resultHashOnSubmit) {
        sendOk(response, unterstuetzer.getHash());
      } else if (resultObjectOnSubmit) {
        sendOk(response, mapper.writeValueAsString(unterstuetzer));
      } else {
        sendOk(response);
      }

			mailService.sendUnterstuetzerBestaetigungMail(unterstuetzer, email, vorgang);
			
		} catch (Exception e) {
			logger.warn(e);
			sendError(response, e);
		}
	}

	
	/**
	 * Die Methode verarbeitet den POST-Request auf der URL <code>/service/unterstuetzerBestaetigung</code><br/>
	 * Beschreibung: Unterstützung bestätigen
	 * @param hash Hash zum Bestätigen
	 * @return View die angezeigt werden soll
	 */
	@RequestMapping(value="/unterstuetzerBestaetigung")
	public String unterstuetzerBestaetigung(@RequestParam(value = "hash", required = false) String hash) {
		
		try {
			if (StringUtils.isBlank(hash)) throw new BackendControllerException(301, "[hash] fehlt");
			Unterstuetzer unterstuetzer = vorgangDao.findUnterstuetzer(hash);
			if (unterstuetzer==null) throw new BackendControllerException(302, "[hash] nicht korrekt");
			
			if (unterstuetzer.getDatumBestaetigung()!=null)  throw new BackendControllerException(303, "Unterstützer wurde bereits bestätigt");

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
	 * Beschreibung: erstellt eine Missbrauchsmeldung für einen Vorgang
	 * @param vorgang Vorgang
	 * @param text Text der Missbrauchsmeldung
	 * @param email E-Mail-Adresse des Erstellers
	 * @param resultHashOnSubmit <code>true</code> - gibt den Hash zum Bestätigen als Ergebnis zurück
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

			if (StringUtils.isBlank(text)) throw new BackendControllerException(403, "[text] fehlt", "Die Begründung fehlt.");
			missbrauchsmeldung.setText(text);
			
			if (StringUtils.isBlank(email)) throw new BackendControllerException(404, "[email] fehlt", "Die E-Mail-Adresse fehlt.");
			if (!isMaxLength(email, 300)) throw new BackendControllerException(405, "[email] zu lang", "Die E-Mail-Adresse ist zu lang.");
			if (!isEmail(email)) throw new BackendControllerException(406, "[email] nicht korrekt", "Die E-Mail-Adresse ist nicht gültig.");
            missbrauchsmeldung.setAutorEmail(email);
			missbrauchsmeldung.setHash(securityService.createHash(missbrauchsmeldung.getVorgang().getId()+email+System.currentTimeMillis()));
			
			missbrauchsmeldung.setDatum(new Date());

			vorgangDao.persist(missbrauchsmeldung);
			
			mailService.sendMissbrauchsmeldungBestaetigungMail(missbrauchsmeldung, email, vorgang);

			if (resultHashOnSubmit==true) sendOk(response, missbrauchsmeldung.getHash());
			else sendOk(response);
		} catch (Exception e) {
			logger.warn(e);
			sendError(response, e);
		}
	}

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/service/kommentar</code><br/>
   * Beschreibung: holt interne Kommentare zu einem Vorgang
   * @param vorgang_id Vorgang-ID
   * @param response Response in das das Ergebnis direkt geschrieben wird
   */
  @RequestMapping(value="/kommentar", method = RequestMethod.GET)
  @ResponseBody
  public void kommentar(
      @RequestParam(value = "vorgang_id", required = false) Long vorgang_id,
      HttpServletResponse response) {
    
    try {
      Vorgang vorgang = vorgangDao.findVorgang(vorgang_id);
      sendOk(response, mapper.writeValueAsString(vorgang.getKommentare()));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }

  /**
   * Die Methode verarbeitet den GET-Request auf der URL <code>/service/lobHinweiseKritik</code><br/>
   * Beschreibung: holt Lob, Hinweise oder Kritik zu einem Vorgang
   * @param vorgang_id Vorgang-ID
   * @param response Response in das das Ergebnis direkt geschrieben wird
   */
  @RequestMapping(value="/lobHinweiseKritik", method = RequestMethod.GET)
  @ResponseBody
  public void lobHinweiseKritik(
      @RequestParam(value = "vorgang_id", required = false) Long vorgang_id,
      HttpServletResponse response) {

    try {
      Vorgang vorgang = vorgangDao.findVorgang(vorgang_id);
      sendOk(response, mapper.writeValueAsString(vorgang.getLobHinweiseKritik()));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
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
			if (vorgang==null) throw new BackendControllerException(401, "[vorgang] fehlt", "Lob, Hinweise oder Kritik kann/können keiner Meldung zugeordnet werden.");
			lobHinweiseKritik.setVorgang(vorgangDao.findVorgang(vorgang));
			if (lobHinweiseKritik.getVorgang()==null) throw new BackendControllerException(402, "[vorgang] nicht korrekt", "Lob, Hinweise oder Kritik kann/können keiner Meldung zugeordnet werden.");

			if (StringUtils.isBlank(email)) throw new BackendControllerException(404, "[email] fehlt", "Die E-Mail-Adresse fehlt.");
			if (!isMaxLength(email, 300)) throw new BackendControllerException(405, "[email] zu lang", "Die E-Mail-Adresse ist zu lang.");
			if (!isEmail(email)) throw new BackendControllerException(406, "[email] nicht korrekt", "Die E-Mail-Adresse ist nicht gültig.");
            lobHinweiseKritik.setAutorEmail(email);
            
            // aktuelle Zuständigkeit des Vorgangs bestimmen
            String zustaendigkeit = lobHinweiseKritik.getVorgang().getZustaendigkeit();
            
            // Empfänger gefunden?
            Boolean empfaengerGefunden = false;
            
            // falls aktuelle Zuständigkeit des Vorgangs nicht NULL oder leer ist und gleichzeitig akzeptiert ist
            if (zustaendigkeit != null && zustaendigkeit != "" && lobHinweiseKritik.getVorgang().getZustaendigkeitStatus() == EnumZustaendigkeitStatus.akzeptiert) {

                String empfaengerEmail = new String();
                
                // alle Nutzernamen dieser Zuständigkeit bestimmen
                List<String> allUserNamesForRole = securityService.getAllUserNamesForRole(zustaendigkeit);
                
                // denjenigen Nutzernamen aus dieser Zuständigkeit bestimmen, der gemäß dem Verlauf die letzte Bearbeitung am Vorgang durchgeführt hat
                String empfaenger = verlaufDao.findLastUserForVorgangAndZustaendigkeit(lobHinweiseKritik.getVorgang(), allUserNamesForRole);
                
                // falls dieser gefunden wurde
                if (empfaenger != null && empfaenger != "") {
                    empfaengerGefunden = true;
                    
                    // String mit dessen E-Mail-Adresse belegen
                    empfaengerEmail = securityService.getUserEmailForRoleByName(empfaenger, zustaendigkeit);
                    
                    // falls der String mit dessen E-Mail-Adresse nicht NULL ist
                    if (empfaengerEmail != null && empfaengerEmail != "") {
                    
                        // Empfänger-E-Mail-Adresse für Lob, Hinweise oder Kritik auf zuvor gefüllten String setzen
                        lobHinweiseKritik.setEmpfaengerEmail(empfaengerEmail);
                    
                        // Lob, Hinweise oder Kritik als E-Mail versenden
                        mailService.sendLobHinweiseKritikMail(lobHinweiseKritik.getVorgang(), email, empfaengerEmail, freitext);
                    }
                }
            }
            
            // ansonsten: falls Empfänger zuvor nicht gefunden wurde und aktuelle Zuständigkeit des Vorgangs nicht NULL oder leer ist, aber eben auch nicht akzeptiert ist
            else if (empfaengerGefunden == false && zustaendigkeit != null && zustaendigkeit != "") {

                String empfaengerEmail = new String();
                Short zaehler = 0;
                
                // alle Empfänger redaktioneller E-Mails dieser Zuständigkeit bestimmen, die zugleich auch Lob, Hinweise oder Kritik als E-Mail empfangen sollen
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
                            
                            // kommaseparierten String mit der aktuellen E-Mail-Adresse fortführen
                            empfaengerEmail = empfaengerEmail + ", " + empfaengerLobHinweiseKritikForZustaendigkeit.getEmail();
                        }
                        zaehler++;
                        
                        // Lob, Hinweise oder Kritik als E-Mail(s) versenden
                        mailService.sendLobHinweiseKritikMail(lobHinweiseKritik.getVorgang(), email, empfaengerLobHinweiseKritikForZustaendigkeit.getEmail(), freitext);
                    }
                    
                    // Empfänger-E-Mail-Adresse für Lob, Hinweise oder Kritik auf in vorhergehender Schleife gefüllten String setzen
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
	 * Beschreibung: Vorgang bestätigen
	 * @param hash Hash zum Bestätigen
	 * @return View die angezeigt werden soll
	 */
	@RequestMapping(value="/missbrauchsmeldungBestaetigung")
	public String missbrauchsmeldungBestaetigung(@RequestParam(value = "hash", required = false) String hash) {
		
		try {
			if (StringUtils.isBlank(hash)) throw new BackendControllerException(501, "[hash] fehlt");
			Missbrauchsmeldung missbrauchsmeldung = vorgangDao.findMissbrauchsmeldung(hash);
			if (missbrauchsmeldung==null) throw new BackendControllerException(502, "[hash] nicht korrekt");
			
			if (missbrauchsmeldung.getDatumBestaetigung()!=null)  throw new BackendControllerException(503, "Missbrauchsmeldung wurde bereits bestätigt");

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
	 * Beschreibung: Vorgang löschen
	 * @param hash Hash zum Löschen
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
				
			} else throw new BackendControllerException(103, "Vorgang kann nicht mehr gelöscht werden"); 
			
			return "backend/vorgangLoeschenOk";
			
		} catch (Exception e) {
			return "backend/vorgangLoeschenFehler";
		}
	}


	/**
	 * 
	 * @param oviWkt überwachte Fläche als WKT
	 * @param probleme Probleme überwachen?
	 * @param problemeKategorien Liste der überwachten Kategorien bei den Problemen
	 * @param ideen Ideen überwachen?
	 * @param ideenKategorien Liste der überwachten Kategorien bei den Ideen
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
   * Die Methode verarbeitet den GET-Request auf der URL
   * <code>/auftraege</code><br/>
   *
   * @param response 
   * @throws java.io.IOException 
   */
  @RequestMapping(value="/auftraege", method = RequestMethod.POST)
	@ResponseBody
	public void auftraege(
      HttpServletResponse response) throws IOException {
    
    try {
      List<Auftrag> auftraege = auftragDao.alleAuftraege();
      sendOk(response, mapper.writeValueAsString(auftraege));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }
	
  /**
   * Die Methode verarbeitet den GET-Request auf der URL
   * <code>/auftraegeEinerGruppe</code><br/>
   *
   * @param team
   * @param response 
   * @throws java.io.IOException 
   */
  @RequestMapping(value="/auftraegeEinerGruppe", method = RequestMethod.POST)
	@ResponseBody
	public void auftraegeEinerGruppe(
			@RequestParam(value = "team") String team,
      HttpServletResponse response) throws IOException {
    
    try {
      List<Auftrag> auftraege = auftragDao.findAuftraegeByTeam(team);
      sendOk(response, mapper.writeValueAsString(auftraege));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }
	
  /**
   * Die Methode verarbeitet den GET-Request auf der URL
   * <code>/auftraegeEinerGruppeAm</code><br/>
   *
   * @param team
   * @param datum
   * @param response 
   * @throws java.io.IOException 
   */
  @RequestMapping(value="/auftraegeEinerGruppeAm", method = RequestMethod.POST)
	@ResponseBody
	public void auftraegeEinerGruppeAm(
			@RequestParam(value = "team") String team,
			@RequestParam(value = "datum") String datum, 
      HttpServletResponse response) throws IOException {
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    try {
      List<Auftrag> auftraege = auftragDao.findAuftraegeByTeamAndDate(team, sdf.parse(datum));
      
      sendOk(response, mapper.writeValueAsString(auftraege));
    } catch (ParseException ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }
	
  /**
   * Die Methode verarbeitet den GET-Request auf der URL
   * <code>/auftrag</code><br/>
   *
   * @param id
   * @param response 
   * @throws java.io.IOException 
   */
  @RequestMapping(value="/auftrag", method = RequestMethod.POST)
	@ResponseBody
	public void auftrag(
			@RequestParam(value = "id") Integer id,
      HttpServletResponse response) throws IOException {
    
    try {
      Auftrag auftrag = auftragDao.find(id);
      sendOk(response, mapper.writeValueAsString(auftrag));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }
	
  /**
   * Die Methode verarbeitet den GET-Request auf der URL
   * <code>/setzeStatus</code><br/>
   *
   * @param id
   * @param status
   * @param response 
   * @throws java.io.IOException 
   */
  @RequestMapping(value="/setzeStatus", method = RequestMethod.POST)
	@ResponseBody
	public void setzeStatus(
			@RequestParam(value = "id") Integer id,
			@RequestParam(value = "status") String status,
      HttpServletResponse response) throws IOException {
    
    try {
      Auftrag auftrag = auftragDao.find(id);
      for(EnumAuftragStatus val : EnumAuftragStatus.values()) {
        if(val.toString().equals(status)) {
          auftrag.setStatus(val);
          vorgangDao.merge(auftrag);
          sendOk(response, mapper.writeValueAsString(auftrag));
          return;
        }
      }
      sendOk(response);
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }
	
  /**
   * Die Methode verarbeitet den GET-Request auf der URL
   * <code>/kategorien</code><br/>
   *
   * @param response 
   * @throws java.io.IOException 
   */
  @RequestMapping(value="/kategorien", method = RequestMethod.POST)
	@ResponseBody
	public void kategorien(
      HttpServletResponse response) throws IOException {
    
    try {
      List<Kategorie> kategorien = kategorieDao.findRootKategorien();
      kategorien.addAll(kategorieDao.getKategorien());
      sendOk(response, mapper.writeValueAsString(kategorien));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }
	
  /**
   * Die Methode verarbeitet den GET-Request auf der URL
   * <code>/kategorie</code><br/>
   *
   * @param id
   * @param response 
   * @throws java.io.IOException 
   */
  @RequestMapping(value="/kategorie", method = RequestMethod.POST)
	@ResponseBody
	public void kategorie(
			@RequestParam(value = "id") Integer id,
      HttpServletResponse response) throws IOException {
    
    try {
      Kategorie kategorie = kategorieDao.findKategorie(Long.parseLong(id.toString()));
      sendOk(response, mapper.writeValueAsString(kategorie));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }
  
  /**
   * Die Methode verarbeitet den GET-Request auf der URL
   * <code>/unterkategorien</code><br/>
   *
   * @param response 
   * @throws java.io.IOException 
   */
  @RequestMapping(value="/unterkategorien", method = RequestMethod.GET)
	@ResponseBody
	public void unterkategorien(
      HttpServletResponse response) throws IOException {
    
    try {
      List<Kategorie> kategorien = kategorieDao.findUnterKategorien();
      sendOk(response, mapper.writeValueAsString(kategorien));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
    }
  }
  
  /**
   * Die Methode verarbeitet den GET-Request auf der URL
   * <code>/vorgaenge</code><br/>
   *
   * @param id
   * @param ids
   * @param category_id
   * @param status
   * @param date_from
   * @param date_to
   * @param updated_from 
   * @param updated_to 
   * @param agency_responsible 
   * @param response 
   * @throws java.io.IOException 
   */
  @RequestMapping(value="/vorgaenge", method = RequestMethod.GET)
	@ResponseBody
	public void vorgaenge(
      @RequestParam(value = "id", required = false) Long id, 
      @RequestParam(value = "ids", required = false) String ids, 
      @RequestParam(value = "category_id", required = false) Long category_id, 
      @RequestParam(value = "status", required = false) String status, 
      @RequestParam(value = "date_from", required = false) String date_from, 
      @RequestParam(value = "date_to", required = false) String date_to, 
      @RequestParam(value = "updated_from", required = false) String updated_from, 
      @RequestParam(value = "updated_to", required = false) String updated_to, 
      @RequestParam(value = "agency_responsible", required = false) String agency_responsible, 
      
      HttpServletResponse response) throws IOException {
    
    try {
      List<Vorgang> vorgaenge = new ArrayList<Vorgang>();
      if(id != null) {
        Vorgang vg = vorgangDao.findVorgang(id);
        vg.setSecurityService(securityService);
        vorgaenge.add(vg);
      } else {
        VorgangSuchenCommand cmd = new VorgangSuchenCommand();
        // Suchtyp aussendienst würde nur Vorgänge mit zustaendigkeit_status = 'akzeptiert' ausgeben
        cmd.setSuchtyp(VorgangSuchenCommand.Suchtyp.erweitert);
        // Sortieren nach ID
        cmd.setOrder(0);
        cmd.setOrderDirection(0);
        
        if(ids != null && ids.length() > 0) {
          String[] idStrList = ids.split(",");
          Long[] data = new Long[idStrList.length];
          for (int i = 0; i < idStrList.length; i++) {
            data[i] = Long.valueOf(idStrList[i]);
          }
          cmd.setVorgangAuswaehlen(data);
        }

        if(category_id != null) {
          Kategorie kat = kategorieDao.findKategorie(category_id);
          if(kat != null) {
            if(kat.getParent() == null) {
              cmd.setErweitertHauptkategorie(kat);
            } else {
              cmd.setErweitertKategorie(kat);
            }
          }
        }

        String[] status_list = status.split(",");
        EnumVorgangStatus[] evs = new EnumVorgangStatus[status_list.length];

        for (int i = 0; i < status_list.length; i++) {
          evs[i] = EnumVorgangStatus.valueOf(status_list[i]);
        }
        cmd.setErweitertVorgangStatus(evs);

        if(date_from != null) {
          cmd.setErweitertDatumVon(getDateFromParam(date_from));
        }
        if(date_to != null) {
          cmd.setErweitertDatumBis(getDateFromParam(date_to));
        }
        if(updated_from != null) {
          cmd.setAktualisiertVon(getDateFromParam(updated_from));
        }
        if(updated_to != null) {
          cmd.setAktualisiertBis(getDateFromParam(updated_to));
        }
        
        if(agency_responsible != null) {
          cmd.setAuftragTeam(agency_responsible);
          cmd.setAuftragDatum(new Date());
          cmd.setOrder(8);
        }
        
        List<Object[]> vg = vorgangDao.getVorgaenge(cmd);
        for(Object[] entry : vg) {
    		  Vorgang vorgang = (Vorgang)entry[0];
          vorgang.setSecurityService(securityService);
          vorgaenge.add(vorgang);
        }
      }
      sendOk(response, mapper.writeValueAsString(vorgaenge));
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(BackendController.class.getName()).log(Level.SEVERE, null, ex);
      sendError(response, ex);
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
  
  private Date getDateFromParam(String param) {
    Date date = new Date();
    date.setTime(Long.parseLong(param));
    return date;
  }
}
