package de.fraunhofer.igd.klarschiff.web;

import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.fraunhofer.igd.klarschiff.util.StreamUtil;

@Controller
public class DokumentationController {
	
	@Autowired
	ServletContext servletContext;
	
	@RequestMapping(value="/dokumentation/index", method = RequestMethod.GET)
    public String dokumentationIndex() {
		return "dokumentation/index";
	}

	@RequestMapping(value="/dokumentation/api", method = RequestMethod.GET)
	public String dokumentationApi() {
		return "dokumentation/api";
	}
	
	@RequestMapping(value="/dokumentation/{id}.htm", method = RequestMethod.GET)
    public String dokumentation(@PathVariable("id") String id, Model model) throws Exception {
		String s = StreamUtil.readInputStreamToString(servletContext.getResourceAsStream("/dokumentation/"+id+".html"), "UTF-8");
		s = StringUtils.substringAfter(s, "<body>");
		s = StringUtils.substringBefore(s, "<body>");
		s = s.replaceAll("href=\"Benutzerhandbuch", "href=\"Benutzerhandbuch.htm");
		s = s.replaceAll("href=\"Administrationshandbuch", "href=\"Administrationshandbuch.htm");
		s = s.replaceAll("href=\"Entwicklerdokumentation", "href=\"Entwicklerdokumentation.htm");

		model.addAttribute("content", s);
		model.addAttribute("externUrlId", id);
		return "dokumentation/"+id;
	}

//	@RequestMapping(value="/dokumentation/benutzer", method = RequestMethod.GET)
//	public String dokumentationBenutzer(Model model, HttpServletRequest request) {
//		return "dokumentation/benutzer";
//	}
	
	@RequestMapping(value="/dokumentation/{id}.html", method = RequestMethod.GET)
	@ResponseBody
	public void dokumentationHtml(
			@PathVariable("id") String id, 
//			@RequestParam(value = "intern", defaultValue="false") boolean intern, 
			Model model, 
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		String s = StreamUtil.readInputStreamToString(servletContext.getResourceAsStream("/dokumentation/"+id+".html"), "UTF-8");
		s = s.replaceFirst("</head>", "<link rel=\"stylesheet\" type=\"text/css\" href=\"../styles/styles.css\"><title>Klarschiff Backend - "+id+"</title></head>");
		s = s.replaceFirst("<body>", "<body><center><div id=\"documentationContent\">");
		s = s.replaceFirst("</body>", "</div></center></body>");
//		if (!intern) {
			s = s.replaceAll("href=\"Benutzerhandbuch", "href=\"Benutzerhandbuch.html");
			s = s.replaceAll("href=\"Administrationshandbuch", "href=\"Administrationshandbuch.html");
			s = s.replaceAll("href=\"Entwicklerdokumentation", "href=\"Entwicklerdokumentation.html");
//		}
		response.setHeader("Content-Type", "text/html;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter writer = response.getWriter();
		writer.write(s);
		response.flushBuffer();
	}
	
//	@RequestMapping(value="/dokumentation/admin", method = RequestMethod.GET)
//	public String dokumentationAdmin(Model model, HttpServletRequest request) {
//		return "dokumentation/admin";
//	}
//	
//	@RequestMapping(value="/dokumentation/entwickler", method = RequestMethod.GET)
//	public String dokumentationEntwickler(Model model, HttpServletRequest request) {
//		return "dokumentation/entwickler";
//	}
//	
//	@RequestMapping(value="/dokumentation/api", method = RequestMethod.GET)
//	public String dokumentationApidoc(Model model, HttpServletRequest request) {
//		return "dokumentation/api";
//	}
	
}
