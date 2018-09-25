package de.fraunhofer.igd.klarschiff.service.poi;

import de.fraunhofer.igd.klarschiff.dao.GrenzenDao;
import de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.Verlauf;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import java.util.EnumMap;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Die Klasse stellt einen Service zur Erstellung von Excel-dokumenten bereit. Dabei werden
 * vorhandenen Excel-Dokumente (Templates) eingelesen und mit Daten gefüllt.<br/>
 * Die verwendeten Templates liegen an der folgenden Stelle: [templatePath][Template].xls
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Service
public class PoiService {

  @Autowired
  GrenzenDao grenzenDao;

  /**
   * Zur Verfügung stehende Templates von Excel-Dokumenten.
   */
  public enum Template {

    vorgangListe, vorgangDelegiertListe
  }
  String templatePath = "classpath:META-INF/templates/";

  Map<Template, String> templates = new HashMap<PoiService.Template, String>();

  /**
   * List ein Template ein.
   *
   * @param template Template, welches eingelesen werden soll.
   * @return eingelesenes Template
   * @throws IOException
   */
  private HSSFWorkbook readTemplate(Template template) throws IOException {
    String file = templates.get(template);
    if (file == null) {
      file = template.name() + ".xls";
    }
    file = templatePath + file;
    Resource resource = new DefaultResourceLoader().getResource(file);
    return new HSSFWorkbook(resource.getInputStream());
  }

  /**
   * Füllt eine Template mit den übergebenen Daten
   *
   * @param template Template, welches verwendet werden soll.
   * @param data Daten, die in das Template eingetragen werden sollen. Die genaue Struktur der Daten
   * ist der Implementierung zu entnehmen.
   * @return Template mit Daten
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public HSSFWorkbook createSheet(Template template, List data) throws Exception {
    HSSFWorkbook workbook = readTemplate(template);

    switch (template) {
      case vorgangListe: {
        Sheet sheet = workbook.getSheetAt(0);
        int r = 1;
        for (Object[] vorgangData : (List<Object[]>) data) {
          Vorgang vorgang = (Vorgang) vorgangData[0];
          EnumMap<EnumVorgangStatus, Long> times = calculateTimes(vorgang);

          Date aenderungsdatum = (Date) vorgangData[1];
          int unterstuetzer = (Integer) vorgangData[2];
          Row row = sheet.createRow(r);
          row.createCell(0).setCellValue(vorgang.getId());
          row.createCell(1).setCellValue(vorgang.getTyp().getText());
          row.createCell(2).setCellValue(vorgang.getDatum());
          row.createCell(3).setCellValue(aenderungsdatum);
          row.createCell(4).setCellValue(vorgang.getKategorie().getParent().getName());
          row.createCell(5).setCellValue(vorgang.getKategorie().getName());
          row.createCell(6).setCellValue(vorgang.getStatus().getText());
          row.createCell(7).setCellValue(vorgang.getAdresse());
          row.createCell(8).setCellValue(grenzenDao.findStadtteilGrenzeByVorgang(vorgang).getName());
          row.createCell(9).setCellValue(unterstuetzer);
          row.createCell(10).setCellValue(vorgang.getZustaendigkeit());
          if (vorgang.getZustaendigkeitStatus() != null) {
            row.createCell(11).setCellValue(vorgang.getZustaendigkeitStatus().getText());
          }
          row.createCell(12).setCellValue(vorgang.getDelegiertAn());
          row.createCell(13).setCellValue(vorgang.getPrioritaet().getText());
          row.createCell(14).setCellValue(formatTime(times.get(EnumVorgangStatus.offen))); // Dauer offen
          row.createCell(15).setCellValue(formatTime(times.get(EnumVorgangStatus.inBearbeitung))); // Dauer in Bearbeitung
          row.createCell(16).setCellValue(formatTime(times.get(EnumVorgangStatus.nichtLoesbar))); // Dauer wird nicht Bearbeitet
          row.createCell(17).setCellValue(formatTime(times.get(EnumVorgangStatus.duplikat))); // Dauer Duplikat
          row.createCell(18).setCellValue(formatTime(times.get(EnumVorgangStatus.geloest))); // Dauer abgeschlossen
          row.createCell(19).setCellValue(formatTime(times.get(EnumVorgangStatus.geloescht))); // Dauer gelöscht

          r++;
        }
      }
      break;
      case vorgangDelegiertListe: {
        Sheet sheet = workbook.getSheetAt(0);
        int r = 1;
        for (Vorgang vorgang : (List<Vorgang>) data) {
          Row row = sheet.createRow(r);
          row.createCell(0).setCellValue(vorgang.getId());
          row.createCell(1).setCellValue(vorgang.getTyp().getText());
          row.createCell(2).setCellValue(vorgang.getDatum());
          row.createCell(3).setCellValue(vorgang.getKategorie().getParent().getName());
          row.createCell(4).setCellValue(vorgang.getKategorie().getName());
          row.createCell(5).setCellValue(vorgang.getStatus().getText());
          row.createCell(6).setCellValue(vorgang.getAdresse());
          row.createCell(7).setCellValue(vorgang.getPrioritaet().getText());
          r++;
        }
      }
      break;

      default:
        throw new RuntimeException("Das Template wird nicht unterstützt.");
    }

    return workbook;
  }

  /* --------------- GET + SET ----------------------------*/
  public String getTemplatePath() {
    return templatePath;
  }

  public void setTemplatePath(String templatePath) {
    this.templatePath = templatePath;
  }

  public Map<Template, String> getTemplates() {
    return templates;
  }

  public void setTemplates(Map<Template, String> templates) {
    this.templates = templates;
  }

  private EnumMap<EnumVorgangStatus, Long> calculateTimes(Vorgang vorgang) {

    EnumMap<EnumVorgangStatus, Long> times = new EnumMap<EnumVorgangStatus, Long>(EnumVorgangStatus.class);
    EnumVorgangStatus status = null;
    Date datum = null;

    for (Verlauf verlauf : vorgang.getVerlauf()) {
      if (verlauf.getTyp() == EnumVerlaufTyp.vorgangBestaetigung) {
        status = EnumVorgangStatus.offen;
        datum = verlauf.getDatum();
      } else if (verlauf.getTyp().equals(EnumVerlaufTyp.status)) {
        long diff = 0;
        if (status != null && datum != null) {
          diff = (verlauf.getDatum().getTime() - datum.getTime());
          if (times.containsKey(status)) {
            diff += times.get(status);
          }
          times.put(status, diff);
        }
        for (EnumVorgangStatus value : EnumVorgangStatus.values()) {
          if (verlauf.getWertNeu().equals(value.getText())) {
            status = value;
            datum = verlauf.getDatum();
          }
        }
      }
    }

    if (status != null && datum != null) {
      long diff = (new Date().getTime() - datum.getTime());
      if (times.containsKey(status)) {
        diff += times.get(status);
      }
      times.put(status, diff);
    }

    return times;
  }

  private String formatTime(Long time) {
    if (time == null) {
      return "";
    }
    time = time / 1000;
    Long hours = time / (60 * 60);
    Long minutes = (time - (hours * (60 * 60))) / 60;
    return hours + ":" + minutes;
  }
}
