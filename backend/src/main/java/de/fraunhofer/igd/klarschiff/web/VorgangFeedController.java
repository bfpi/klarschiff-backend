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
import org.jdom.Namespace;
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

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
    	cmd.setSize(50);
    	cmd.setOrder(2);
    	cmd.setOrderDirection(1);
    	cmd.setSuchtyp(VorgangSuchenCommand.Suchtyp.einfach);
    	cmd.setEinfacheSuche(VorgangSuchenCommand.EinfacheSuche.offene);
    	cmd.setZustaendigkeiten(user.getZustaendigkeiten());
		
    	List<Object[]> list = vorgangDao.getVorgaenge(cmd);
    	
    	Element rss = new Element("rss");
    	rss.setAttribute("version", "2.0");
        Namespace atomNamespace = Namespace.getNamespace("atom", "http://www.w3.org/2005/Atom");
        rss.addNamespaceDeclaration(atomNamespace);
        Namespace georssNamespace = Namespace.getNamespace("georss", "http://www.georss.org/georss");
        rss.addNamespaceDeclaration(georssNamespace);
    	Element channel = new Element("channel");
    	rss.addContent(channel);
    	
    	Element elem;
    	
    	//Title
    	elem = new Element("title");
    	elem.addContent("Klarschiff: zu bearbeitende Vorgänge");
    	channel.addContent(elem);
        
        //Atom:Link
    	elem = new Element("link", atomNamespace);
    	elem.setAttribute("href", settingsService.getPropertyValue("mail.server.baseurl.backend")+"xmlfeeds/feed/"+loginCrypt);
    	elem.setAttribute("rel", "self");
    	elem.setAttribute("type", "application/rss+xml");
    	channel.addContent(elem);
    	
    	//Link
    	elem = new Element("link");
    	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend")+"xmlfeeds/feed/"+loginCrypt);
    	channel.addContent(elem);
    	
    	//Description
    	elem = new Element("description");
    	elem.addContent("Übersicht Ihrer 50 aktuellsten zu bearbeitenden Vorgänge im Bürgerbeteiligungsportal Klarschiff");
    	channel.addContent(elem);
    	
    	//Language
    	elem = new Element("language");
    	elem.addContent("de-de");
    	channel.addContent(elem);
    	
    	//Copyright
    	elem = new Element("copyright");
    	elem.addContent("Hansestadt Rostock");
    	channel.addContent(elem);
    	
    	//Image
    	Element image = new Element("image");
    	channel.addContent(image);
    	elem = new Element("url");
    	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend")+"images/rssfeedImage.png");
    	image.addContent(elem);
    	elem = new Element("title");
    	elem.addContent("Klarschiff: zu bearbeitende Vorgänge");
    	image.addContent(elem);
    	elem = new Element("link");
    	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend")+"xmlfeeds/feed/"+loginCrypt);
    	image.addContent(elem);

    	for(Object[] entry : list) {
    		Vorgang vorgang = (Vorgang)entry[0];
    		Date aenderungsdatum = (Date)entry[1];
    		long unterstuetzer = (Long)entry[2];
    		long missbrauchsmeldungen = (Long)entry[3];
    		Element item = new Element("item");
    		channel.addContent( item);

        	elem = new Element("title");
        	elem.addContent("#" + vorgang.getId() + " " + vorgang.getTyp().getText() + " (" + vorgang.getKategorie().getParent().getName() + " – " + vorgang.getKategorie().getName() + ")");
        	item.addContent(elem);

        	StringBuilder str = new StringBuilder();
        	str.append("<b>Status:</b> ");
        	str.append(vorgang.getStatus().getText());
        	str.append("<br/>");
            str.append("<b>Adresse: </b>");
        	str.append(vorgang.getAdresse());
        	str.append("<br/>");
            str.append("<b>Flurstückseigentum: </b>");
        	str.append(vorgang.getFlurstueckseigentum());
        	str.append("<br/>");
        	str.append("<b>Unterstützungen:</b> ");
        	str.append(unterstuetzer);
        	str.append("<br/>");
        	str.append("<b>Missbrauchsmeldungen:</b> ");
        	str.append(missbrauchsmeldungen);
        	str.append("<br/>");
        	str.append("<b>Zuständigkeit: </b>");
        	str.append(securityService.getZustaendigkeit(vorgang.getZustaendigkeit()).getDescription());
        	str.append(" (");
        	str.append(vorgang.getZustaendigkeitStatus().getText());
        	str.append(")");
        	if (StringUtils.isNotBlank(vorgang.getDelegiertAn())) {
        		str.append(", delegiert an: ");
        		str.append(securityService.getZustaendigkeit(vorgang.getDelegiertAn()).getDescription());
        	}
            str.append("<br/>");
            str.append("<a href=\"" + settingsService.getPropertyValue("mail.server.baseurl.backend")+"vorgang/"+vorgang.getId()+"/uebersicht" + "\" target=\"_blank\">Vorgang in Klarschiff öffnen</a>");

        	elem = new Element("description");
        	elem.addContent(new CDATA(str.toString()));
        	item.addContent(elem);

        	elem = new Element("link");
        	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend")+"vorgang/"+vorgang.getId()+"/uebersicht");
        	item.addContent(elem);

        	elem = new Element("guid");
        	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend")+"vorgang/"+vorgang.getId()+"/uebersicht");
        	item.addContent(elem);
        	
        	elem = new Element("pubDate");
        	elem.addContent(dateFormat.format(aenderungsdatum));
        	item.addContent(elem);
            
            CoordinateReferenceSystem utm = CRS.decode("EPSG:25833");
        	CoordinateReferenceSystem geographic = CRS.decode("EPSG:4326");
            MathTransform transformation = CRS.findMathTransform(utm, geographic);
            Point point = (Point)JTS.transform(vorgang.getOvi(), transformation);
        	Coordinate coor = new Coordinate(point.getY(), point.getX());
        	point.getCoordinate().setCoordinate(coor);
        	
        	elem = new Element("point", georssNamespace);
        	elem.addContent(point.getY() + " " +point.getX());
        	item.addContent(elem);
    	
    	}
    	
    	Document doc = new Document();
    	doc.setRootElement(rss);
    	
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
    	cmd.setSize(50);
    	cmd.setOrder(2);
    	cmd.setOrderDirection(1);
    	cmd.setEinfacheSuche(VorgangDelegiertSuchenCommand.EinfacheSuche.offene);
    	cmd.setDelegiertAn(user.getDelegiertAn());
		
    	List<Vorgang> list = vorgangDao.listVorgang(cmd);
    	
    	Element rss = new Element("rss");
    	rss.setAttribute("version", "2.0");
        Namespace atomNamespace = Namespace.getNamespace("atom", "http://www.w3.org/2005/Atom");
        rss.addNamespaceDeclaration(atomNamespace);
        Namespace georssNamespace = Namespace.getNamespace("georss", "http://www.georss.org/georss");
        rss.addNamespaceDeclaration(georssNamespace);
    	Element channel = new Element("channel");
    	rss.addContent(channel);
    	
    	Element elem;
    	
    	//Title
    	elem = new Element("title");
    	elem.addContent("Klarschiff: zu bearbeitende Vorgänge");
    	channel.addContent(elem);
        
        //Atom:Link
    	elem = new Element("link", atomNamespace);
    	elem.setAttribute("href", settingsService.getPropertyValue("mail.server.baseurl.backend")+"xmlfeeds/feed/"+loginCrypt);
    	elem.setAttribute("rel", "self");
    	elem.setAttribute("type", "application/rss+xml");
    	channel.addContent(elem);
    	
    	//Link
    	elem = new Element("link");
    	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend")+"xmlfeeds/feed/"+loginCrypt);
    	channel.addContent(elem);
    	
    	//Description
    	elem = new Element("description");
    	elem.addContent("Übersicht Ihrer 50 aktuellsten zu bearbeitenden Vorgänge im Bürgerbeteiligungsportal Klarschiff");
    	channel.addContent(elem);
    	
    	//Language
    	elem = new Element("language");
    	elem.addContent("de-de");
    	channel.addContent(elem);
    	
    	//Copyright
    	elem = new Element("copyright");
    	elem.addContent("Hansestadt Rostock");
    	channel.addContent(elem);
    	
    	//Image
    	Element image = new Element("image");
    	channel.addContent(image);
    	elem = new Element("url");
    	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend")+"images/rssfeedImage.png");
    	image.addContent(elem);
    	elem = new Element("title");
    	elem.addContent("Klarschiff: zu bearbeitende Vorgänge");
    	image.addContent(elem);
    	elem = new Element("link");
    	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend")+"xmlfeeds/feed/"+loginCrypt);
    	image.addContent(elem);

    	for(Vorgang vorgang : list) {
    		Element item = new Element("item");
    		channel.addContent( item);

        	elem = new Element("title");
        	elem.addContent("#" + vorgang.getId() + " " + vorgang.getTyp().getText() + " (" + vorgang.getKategorie().getParent().getName() + " – " + vorgang.getKategorie().getName() + ")");
        	item.addContent(elem);

        	StringBuilder str = new StringBuilder();
        	str.append("<b>Status:</b> ");
        	str.append(vorgang.getStatus().getText());
        	str.append("<br/>");
            str.append("<b>Adresse: </b>");
        	str.append(vorgang.getAdresse());
        	str.append("<br/>");
        	str.append("<b>Zuständigkeit: </b>");
        	str.append(securityService.getZustaendigkeit(vorgang.getZustaendigkeit()).getDescription());
        	str.append(" (");
        	str.append(vorgang.getZustaendigkeitStatus().getText());
        	str.append(")");
        	if (StringUtils.isNotBlank(vorgang.getDelegiertAn())) {
        		str.append(", delegiert an: ");
        		str.append(securityService.getZustaendigkeit(vorgang.getDelegiertAn()).getDescription());
        	}
            str.append("<br/>");
            str.append("<a href=\"" + settingsService.getPropertyValue("mail.server.baseurl.backend")+"vorgang/delegiert/"+vorgang.getId()+"/uebersicht" + "\" target=\"_blank\">Vorgang in Klarschiff öffnen</a>");

        	elem = new Element("description");
        	elem.addContent(new CDATA(str.toString()));
        	item.addContent(elem);

        	elem = new Element("link");
        	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend")+"vorgang/delegiert/"+vorgang.getId()+"/uebersicht");
        	item.addContent(elem);

        	elem = new Element("guid");
        	elem.addContent(settingsService.getPropertyValue("mail.server.baseurl.backend")+"vorgang/delegiert/"+vorgang.getId()+"/uebersicht");
        	item.addContent(elem);
        	
        	elem = new Element("pubDate");
        	elem.addContent(dateFormat.format(vorgang.getDatum()));
        	item.addContent(elem);
        	
        	CoordinateReferenceSystem utm = CRS.decode("EPSG:25833");
        	CoordinateReferenceSystem geographic = CRS.decode("EPSG:4326");
            MathTransform transformation = CRS.findMathTransform(utm, geographic);
            Point point = (Point)JTS.transform(vorgang.getOvi(), transformation);
        	Coordinate coor = new Coordinate(point.getY(), point.getX());
        	point.getCoordinate().setCoordinate(coor);
        	
        	elem = new Element("point", georssNamespace);
        	elem.addContent(point.getY() + " " +point.getX());
        	item.addContent(elem);
    	
    	}
    	
    	Document doc = new Document();
    	doc.setRootElement(rss);
    	
		response.setHeader("Content-Type", "application/rss+xml");
    	XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(doc, response.getWriter());       
        response.setStatus(HttpServletResponse.SC_OK);
	}
}
