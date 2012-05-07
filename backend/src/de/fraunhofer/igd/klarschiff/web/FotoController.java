package de.fraunhofer.igd.klarschiff.web;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Controller zur Fotodarstellung
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@RequestMapping("foto")
@Controller
public class FotoController {

	@Autowired
	VorgangDao vorgangDao;
	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>foto/normal/{id}.jpg</code><br/>
	 * Funktionsbeschreibung: Liefert in der Response das Foto zum gewählten Vorgang in Originalgröße
	 * @param id Vorgangs-ID
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value="/normal/{id}.jpg", method = RequestMethod.GET)
    @ResponseBody
    public void normal(@PathVariable("id") Long id, HttpServletResponse response) throws Exception
    {
		Vorgang vorgang = vorgangDao.findVorgang(id);
		response.setHeader("Content-Type", "image/jpg;charset=UTF-8");
		byte[] fotoNormalJpg = vorgang.getFotoNormalJpg();
		response.setHeader("Content-Length", fotoNormalJpg.length+"");
		OutputStream os = response.getOutputStream();
		os.write(fotoNormalJpg);
		response.setStatus(HttpServletResponse.SC_OK);
		response.flushBuffer();
    }

	
	/**
	 * Die Methode verarbeitet den GET-Request auf der URL <code>foto/normal/{id}.jpg</code><br/>
	 * Funktionsbeschreibung: Liefert in der Response das Foto zum gewählten Vorgang in Thumbnailgröße
	 * @param id Vorgangs-ID
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value="/thumb/{id}.jpg", method = RequestMethod.GET)
    @ResponseBody
    public void thumb(@PathVariable("id") Long id, HttpServletResponse response) throws Exception
    {
		Vorgang vorgang = vorgangDao.findVorgang(id);
		response.setHeader("Content-Type", "image/jpg;charset=UTF-8");
		response.setHeader("Content-Length", vorgang.getFotoThumbJpg().length+"");
		OutputStream os = response.getOutputStream();
		os.write(vorgang.getFotoThumbJpg());
		response.setStatus(HttpServletResponse.SC_OK);
		response.flushBuffer();
    }
}
