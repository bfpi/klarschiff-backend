package de.fraunhofer.igd.klarschiff.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.fraunhofer.igd.klarschiff.dao.LobHinweiseKritikDao;

/**
 * Controller für Lob/Hinweise/Kritik im Adminbereich
 * @author Sebastian Schwarz (Hansestadt Rostock)
 */
@SessionAttributes({"cmdlobhinweisekritik"})
@RequestMapping("/admin/lobhinweisekritik")
@Controller
public class AdminLobHinweiseKritikController {
	
	@Autowired
	LobHinweiseKritikDao lobHinweiseKritikDao;
    
    @ModelAttribute("cmdlobhinweisekritik")
    public AdminLobHinweiseKritikCommand initCommand() {
		AdminLobHinweiseKritikCommand cmdlobhinweisekritik = new AdminLobHinweiseKritikCommand();
    	cmdlobhinweisekritik.setSize(20);
    	cmdlobhinweisekritik.setOrder(2);
    	cmdlobhinweisekritik.setOrderDirection(1);
        return cmdlobhinweisekritik;
    }
	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/admin/lobhinweisekritik</code><br/>
	 * @param cmdlobhinweisekritik Command
	 * @param modelMap Model in der ggf. Daten für die View abgelegt werden
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String lobhinweisekritik(@ModelAttribute(value = "cmdlobhinweisekritik") AdminLobHinweiseKritikCommand cmdlobhinweisekritik, ModelMap modelMap){
        modelMap.addAttribute("alleLobHinweiseKritik", lobHinweiseKritikDao.findLobHinweiseKritik(cmdlobhinweisekritik));
        modelMap.put("maxPages", calculateMaxPages(cmdlobhinweisekritik.getSize(), lobHinweiseKritikDao.countLobHinweiseKritik()));
        return "admin/lobhinweisekritik";
	}
    
    /**
	 * Ermittelt die Anzahl maximal benötigter Seiten aus:
	 * @param size gewünschter Anzahl an Elementen pro Seite
	 * @param count gegebener Anzahl an darzustellender Elemente
	 * @return maximal benötigte Seitenzahl
	 */
	private int calculateMaxPages(int size, long count)
    {
		float nrOfPages = (float) count / size;
		return (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
    }
}
