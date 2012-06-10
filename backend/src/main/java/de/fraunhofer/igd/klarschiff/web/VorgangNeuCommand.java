package de.fraunhofer.igd.klarschiff.web;

import static de.fraunhofer.igd.klarschiff.web.Assert.*;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.vo.EnumNaehereBeschreibungNotwendig;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Command für die Erstellung von Vorgängen im Backend. <br />
 * Beinhaltet Vorgangobjekt, Vorgangkategorie, Foto und Fotonamen
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
public class VorgangNeuCommand implements Serializable {

	Vorgang vorgang = new Vorgang();
	Kategorie kategorie;
	MultipartFile foto;
	String fotoName;
	String zustaendigkeit;
	/**
	 * Methode zum prüfen eines neuen Vorganges auf Vollständigkeit benötigter Attribute sowie Validität der 
	 * E-Mail-Adresse<br/>
	 * Prüft auf Vorhandensein von:<b> Typ, OVI-Position, Kategorie, Vorgang-Kategorie</b> und in 
	 * Abhängigkeit von Kategorie: <b>Betreff</b> und/oder <b>Details</b> sowie auf gültigkeit der übergebenen <b>E-Mail-Adresse</b>.
	 * <br/>
	 * @param result Bindingresult mit den Fehlermeldungen
	 * @param kategorieDao
	 */
	public void validate(BindingResult result, KategorieDao kategorieDao) {
		if (StringUtils.equals("Bitte geben Sie einen Betreff an.",vorgang.getBetreff())) vorgang.setBetreff("");
		if (StringUtils.equals("Bitte beschreiben Sie Ihre Meldung genauer.",vorgang.getDetails())) vorgang.setDetails("");
		assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.typ", "Bitte geben Sie den Typ Ihres Vorgangs an.");
		assertNotEmpty(this, result, Assert.EvaluateOn.ever, "kategorie", "Bitte geben Sie eine Hauptkategorie an.");
		assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.kategorie", "Bitte geben Sie eine Unterkategorie an.");
		assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.oviWkt", "Bitte zeichnen Sie die Position Ihres Vorgangs in der Karte ein.");

		if (!StringUtils.isBlank(vorgang.getAutorEmail())) assertEmail(this, result, Assert.EvaluateOn.ever, "vorgang.autorEmail", "Die E-Mail-Adresse ist nicht gültig.");
		EnumNaehereBeschreibungNotwendig naehereBeschreibungNotwendig = kategorieDao.viewNaehereBeschreibung(kategorie, vorgang.getKategorie());
		switch (naehereBeschreibungNotwendig) {
		case betreff:
			assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.betreff", "Bitte geben Sie einen Betreff an.");			
			break;
		case details:
			assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.details", "Bitte beschreiben Sie Ihre Meldung genauer.");			
			break;
		case betreffUndDetails:
			assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.betreff", "Bitte geben Sie einen Betreff an.");
			assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.details", "Bitte beschreiben Sie Ihre Meldung genauer.");		
			break;
		}
	}
	
	
	public Vorgang getVorgang() {
		return vorgang;
	}
	public void setVorgang(Vorgang vorgang) {
		this.vorgang = vorgang;
	}
	public Kategorie getKategorie() {
		return kategorie;
	}
	public void setKategorie(Kategorie kategorie) {
		this.kategorie = kategorie;
	}


	public MultipartFile getFoto() {
		return foto;
	}
	public void setFoto(MultipartFile foto) {
		this.foto = foto;
	}


	public String getFotoName() {
		return fotoName;
	}
	public void setFotoName(String fotoName) {
		this.fotoName = fotoName;
	}


	public String getZustaendigkeit() {
		return zustaendigkeit;
	}


	public void setZustaendigkeit(String zustaendigkeit) {
		this.zustaendigkeit = zustaendigkeit;
	}
}
