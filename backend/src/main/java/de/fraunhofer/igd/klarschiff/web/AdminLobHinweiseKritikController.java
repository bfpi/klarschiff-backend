package de.fraunhofer.igd.klarschiff.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.fraunhofer.igd.klarschiff.dao.LobHinweiseKritikDao;
import de.fraunhofer.igd.klarschiff.vo.LobHinweiseKritik;

/**
 * Controller für Lob/Hinweise/Kritik im Adminbereich
 * @author Sebastian Schwarz (Hansestadt Rostock)
 */
@SessionAttributes({"cmd"})
@RequestMapping("/admin/lobhinweisekritik")
@Controller
public class AdminLobHinweiseKritikController {
	
	@Autowired
	LobHinweiseKritikDao lobHinweiseKritikDao;
    
    @ModelAttribute("cmd")
    public AdminLobHinweiseKritikCommand initCommand() {
		AdminLobHinweiseKritikCommand cmd = new AdminLobHinweiseKritikCommand();
    	cmd.setSize(20);
    	cmd.setOrder(2);
    	cmd.setOrderDirection(1);
        return cmd;
    }
	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>/admin/lobhinweisekritik</code><br/>
	 * @param cmd Command
	 * @param modelMap Model in der ggf. Daten für die View abgelegt werden
	 * @return View, die zum Rendern des Request verwendet wird
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String lobhinweisekritik(@ModelAttribute(value = "cmd") AdminLobHinweiseKritikCommand cmd, ModelMap modelMap){
        modelMap.addAttribute("alleLobHinweiseKritik", lobHinweiseKritikDao.findLobHinweiseKritik(cmd));
        modelMap.put("maxPages", calculateMaxPages(cmd.getSize(), lobHinweiseKritikDao.countLobHinweiseKritik()));
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
