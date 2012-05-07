package de.fraunhofer.igd.klarschiff.web;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.security.User;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.util.SecurityUtil;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

@RequestMapping("/xmlfeeds")
@Controller
public class VorgangFeedController {

	@Autowired
	VorgangDao vorgangDao;

	@Autowired
	SecurityService securityService;
	
	@Autowired
	SettingsService settingsService;
	
	DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");

	@RequestMapping(value="/feed/{user}", method = RequestMethod.GET)
    @ResponseBody
	public void xmlfeed(
			@PathVariable("user") String loginCrypt, 
			@RequestParam(value="version", defaultValue="2.0") String version, 
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String login = SecurityUtil.simpleDecrypt(loginCrypt);
		
		User user = securityService.getUser(login);
		
		//Command für die Suche zusammenstellen
		VorgangFeedCommand cmd = new VorgangFeedCommand();
    	cmd.setSize(20);
    	cmd.setOrder(2);
    	cmd.setOrderDirection(1);
    	cmd.setSuchtyp(VorgangSuchenCommand.Suchtyp.einfach);
    	cmd.setEinfacheSuche(VorgangSuchenCommand.EinfacheSuche.offene);
    	cmd.setZustaendigkeiten(user.getZustaendigkeiten());
		
    	List<Object[]> list = vorgangDao.listVorgang(cmd);
    	
    	Element root = new Element("rss");
    	root.setAttribute("version", "2.0");
    	Element channel = new Element("channel");
    	root.addContent(channel);
    	
    	Element elem;
    	
    	//Titel
    	elem = new Element("title");
    	elem.addContent("Klarschiff-RSS-Feed");
    	channel.addContent(elem);
    	
    	//Link
    	elem = new Element("link");
    	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend")+"xmlfeeds/feed/"+loginCrypt);
    	channel.addContent(elem);
    	
    	//Description
    	elem = new Element("description");
    	elem.addContent("Übersicht über Ihre aktuell offenen Vorgänge.");
    	channel.addContent(elem);
    	
    	//Language
    	elem = new Element("language");
    	elem.addContent("de-de");
    	channel.addContent(elem);
    	
    	//Icon
    	Element image = new Element("image");
    	channel.addContent(image);
    	elem = new Element("url");
    	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend")+"images/rssfeedImage.png");
    	image.addContent(elem);
    	elem = new Element("title");
    	elem.addContent("Klarschiff");
    	image.addContent(elem);
    	elem = new Element("link");
    	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend"));
    	image.addContent(elem);

    	for(Object[] entry : list) {
    		Vorgang vorgang = (Vorgang)entry[0];
    		Date aenderungsdatum = (Date)entry[1];
    		long unterstuetzer = (Long)entry[2];
    		long missbrauchsmeldung = (Long)entry[3];
    		Element item = new Element("item");
    		channel.addContent( item);

        	elem = new Element("title");
        	elem.addContent(dateFormat.format(vorgang.getDatum()) + " - " + vorgang.getKategorie().getName());
        	item.addContent(elem);

        	StringBuilder str = new StringBuilder();
        	str.append("ID: ");
        	str.append(vorgang.getId());
        	str.append("<br/>");
        	str.append("Typ: ");
        	str.append(vorgang.getTyp().getText());
        	str.append("<br/>");
        	str.append("Erstellung: ");
        	str.append(dateFormat.format(vorgang.getDatum()));
        	str.append("<br/>");
        	str.append("letzte Änderung: ");
        	str.append(dateFormat.format(aenderungsdatum));
        	str.append("<br/>");
        	str.append("Hauptkategorie: ");
        	str.append(vorgang.getKategorie().getParent().getName());
        	str.append("<br/>");
        	str.append("Unterkategorie: ");
        	str.append(vorgang.getKategorie().getName());
        	str.append("<br/>");
        	str.append("Status: ");
        	str.append(vorgang.getStatus().getText());
        	str.append("<br/>");
        	str.append("Unterstützungen: ");
        	str.append(unterstuetzer);
        	str.append("<br/>");
        	str.append("Missbrauchsmeldungen: ");
        	str.append(missbrauchsmeldung);
        	str.append("<br/>");
        	str.append("Zuständigkeit: ");
        	str.append(securityService.getZustaendigkeit(vorgang.getZustaendigkeit()).getDescription());
        	str.append(" (");
        	str.append(vorgang.getZustaendigkeitStatus().getText());
        	str.append(")");
        	if (StringUtils.isNotBlank(vorgang.getDelegiertAn())) {
        		str.append(" (delegiert an: ");
        		str.append(securityService.getZustaendigkeit(vorgang.getDelegiertAn()).getDescription());
        		str.append(")");
        	}
        	str.append("<br/>");
        	str.append("Priorität: ");
        	str.append(vorgang.getPrioritaet().getText());
        	//TODO:
        	elem = new Element("description");
        	elem.addContent(new CDATA(str.toString()));
        	item.addContent(elem);

        	elem = new Element("link");
        	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend")+"vorgang/"+vorgang.getId()+"/uebersicht");
        	item.addContent(elem);

        	elem = new Element("guid");
        	elem.addContent(vorgang.getId()+"");
        	item.addContent(elem);
        	
        	elem = new Element("pubDate");
        	elem.addContent(dateFormat.format(aenderungsdatum));
        	item.addContent(elem);
    	
    	}
    	
    	Document doc = new Document();
    	doc.setRootElement(root);
    	
		response.setHeader("Content-Type", "application/rss+xml");
    	XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(doc, response.getWriter());       
        response.setStatus(HttpServletResponse.SC_OK);
	}

	@RequestMapping(value="/feedDelegiert/{user}", method = RequestMethod.GET)
	public void xmlfeedDelegiert(
			@PathVariable("user") String loginCrypt, 
			@RequestParam(value="version", defaultValue="2.0") String version, 
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String login = SecurityUtil.simpleDecrypt(loginCrypt);
		User user = securityService.getUser(login);
		
		//Command für die Suche zusammenstellen
		VorgangFeedDelegiertAnCommand cmd = new VorgangFeedDelegiertAnCommand();
    	cmd.setSize(20);
    	cmd.setOrder(2);
    	cmd.setOrderDirection(1);
    	cmd.setEinfacheSuche(VorgangDelegiertSuchenCommand.EinfacheSuche.offene);
    	cmd.setDelegiertAn(user.getDelegiertAn());
		
    	List<Vorgang> list = vorgangDao.listVorgang(cmd);
    	
    	Element root = new Element("rss");
    	root.setAttribute("version", "2.0");
    	Element channel = new Element("channel");
    	root.addContent(channel);
    	
    	Element elem;
    	
    	//Titel
    	elem = new Element("title");
    	elem.addContent("Klarschiff RSSFeed (extern)");
    	channel.addContent(elem);
    	
    	//Link
    	elem = new Element("link");
    	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend")+"xmlfeeds/feedDelegiert/"+loginCrypt);
    	channel.addContent(elem);
    	
    	//Description
    	elem = new Element("description");
    	elem.addContent("Übersicht über die aktuell aktiven neusten Vorgänge.");
    	channel.addContent(elem);
    	
    	//Language
    	elem = new Element("language");
    	elem.addContent("de-de");
    	channel.addContent(elem);
    	
    	//Icon
    	Element image = new Element("image");
    	channel.addContent(image);
    	elem = new Element("url");
    	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend")+"images/rssfeedImage.png");
    	image.addContent(elem);
    	elem = new Element("title");
    	elem.addContent("Klarschiff");
    	image.addContent(elem);
    	elem = new Element("link");
    	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend"));
    	image.addContent(elem);

    	for(Vorgang vorgang : list) {
    		Element item = new Element("item");
    		channel.addContent( item);

        	elem = new Element("title");
        	elem.addContent(dateFormat.format(vorgang.getDatum()) + " - " + vorgang.getKategorie().getName());
        	item.addContent(elem);

        	StringBuilder str = new StringBuilder();
        	str.append("ID: ");
        	str.append(vorgang.getId());
        	str.append("<br/>");
        	str.append("Typ: ");
        	str.append(vorgang.getTyp().getText());
        	str.append("<br/>");
        	str.append("Erstellung: ");
        	str.append(dateFormat.format(vorgang.getDatum()));
        	str.append("<br/>");
        	str.append("Hauptkategorie: ");
        	str.append(vorgang.getKategorie().getParent().getName());
        	str.append("<br/>");
        	str.append("Unterkategorie: ");
        	str.append(vorgang.getKategorie().getName());
        	str.append("<br/>");
        	str.append("Status: ");
        	str.append(vorgang.getStatus().getText());
        	str.append("<br/>");
        	str.append("Zuständigkeit: ");
        	str.append(securityService.getZustaendigkeit(vorgang.getZustaendigkeit()).getDescription());
        	str.append(" (");
        	str.append(vorgang.getZustaendigkeitStatus().getText());
        	str.append(")");
        	if (StringUtils.isNotBlank(vorgang.getDelegiertAn())) {
        		str.append(" (delegiert an: ");
        		str.append(securityService.getZustaendigkeit(vorgang.getDelegiertAn()).getDescription());
        		str.append(")");
        	}
        	str.append("<br/>");
        	str.append("Priorität: ");
        	str.append(vorgang.getPrioritaet().getText());
        	//TODO:
        	elem = new Element("description");
        	elem.addContent(new CDATA(str.toString()));
        	item.addContent(elem);

        	elem = new Element("link");
        	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend")+"vorgang/delegiert/"+vorgang.getId()+"/uebersicht");
        	item.addContent(elem);

        	elem = new Element("guid");
        	elem.addContent(vorgang.getId()+"");
        	item.addContent(elem);
        	
        	elem = new Element("pubDate");
        	elem.addContent(dateFormat.format(vorgang.getDatum()));
        	item.addContent(elem);
    	
    	}
    	
    	Document doc = new Document();
    	doc.setRootElement(root);
    	
		response.setHeader("Content-Type", "application/rss+xml");
    	XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(doc, response.getWriter());       
        response.setStatus(HttpServletResponse.SC_OK);
	}
}
