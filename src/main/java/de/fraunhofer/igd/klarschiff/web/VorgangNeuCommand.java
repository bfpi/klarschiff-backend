package de.fraunhofer.igd.klarschiff.web;

import de.fraunhofer.igd.klarschiff.dao.GrenzenDao;
import static de.fraunhofer.igd.klarschiff.web.Assert.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Command für die Erstellung von Vorgängen im Backend. <br>
 * Beinhaltet Vorgangobjekt, Vorgangkategorie, Foto und Fotonamen
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
public class VorgangNeuCommand extends Command {

  Kategorie kategorie;
  MultipartFile foto;
  String fotoName;
  String zustaendigkeit;
  String zustaendigkeitFrontend;

  public VorgangNeuCommand() {
    vorgang = new Vorgang();
  }

  /**
   * Methode zur Prüfung eines neuen Vorganges auf Vollständigkeit benötigter Attribute sowie
   * Validität der E-Mail-Adresse<br>
   * Prüft auf Vorhandensein von: <b>Typ, Hauptkategorie, Unterkategorie, Position, E-Mail-Adresse
   * und Beschreibung</b> sowie auf Gültigkeit der übergebenen
   * <b>E-Mail-Adresse</b>.<br>
   *
   * @param result Bindingresult mit den Fehlermeldungen
   * @param kategorieDao KategorieDao
   * @param grenzenDao GrenzenDao
   * @param settingsService SettingsService
   */
  public void validate(BindingResult result, KategorieDao kategorieDao, GrenzenDao grenzenDao, SettingsService settingsService) {
    if (StringUtils.equals("Bitte beschreiben Sie Ihre Meldung genauer.", vorgang.getBeschreibung())) {
      vorgang.setBeschreibung("");
    }
    assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.typ", "Bitte geben Sie den Typ Ihres neuen Vorgangs an.");
    assertNotEmpty(this, result, Assert.EvaluateOn.ever, "kategorie", "Bitte geben Sie eine Hauptkategorie für Ihren neuen Vorgang an.");
    assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.kategorie", "Bitte geben Sie eine Unterkategorie für Ihren neuen Vorgang an.");
    assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.oviWkt", "Bitte tragen Sie die Position Ihres neuen Vorgangs in der Karte ein.");

    if (vorgang.getOvi() != null && !vorgang.getOvi().within(grenzenDao.getStadtgrenze().getGrenze())) {
      addErrorMessage(result, "vorgang.oviWkt", "Bitte setzen Sie die Position Ihres Vorgangs innerhalb des Bereichs " + settingsService.getPropertyValue("context.app.area") + ".");
    }

    assertNotEmpty(this, result, Assert.EvaluateOn.ever, "vorgang.autorEmail", "Bitte geben Sie eine E-Mail-Adresse an.");

    if (!StringUtils.isBlank(vorgang.getAutorEmail())) {
      assertEmail(this, result, Assert.EvaluateOn.ever, "vorgang.autorEmail", "Die angegebene E-Mail-Adresse ist nicht gültig.");
    }
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

  public String getZustaendigkeitFrontend() {
    return zustaendigkeitFrontend;
  }

  public void setZustaendigkeitFrontend(String zustaendigkeitFrontend) {
    this.zustaendigkeitFrontend = zustaendigkeitFrontend;
  }
}
