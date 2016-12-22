package de.fraunhofer.igd.klarschiff.statistik;

import de.fraunhofer.igd.klarschiff.dao.StatistikDao;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.web.StatistikCommand;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

public class StatistikZeitraum extends StatistikCommon {

  int[] hauptkategorieIds = {1, 18, 35, 54, 66, 87, 111};
  int[] kategorieIds = {52, 53};

  public StatistikZeitraum(StatistikDao statistikDao, SecurityService securityService) {
    this.statistikDao = statistikDao;
    this.securityService = securityService;
  }

  public HSSFWorkbook createStatistik(StatistikCommand cmd) throws IOException, ParseException {
    String file = "classpath:META-INF/templates/statistikZeitraum.xls";
    Resource resource = new DefaultResourceLoader().getResource(file);

    HSSFWorkbook workbook = new HSSFWorkbook(resource.getInputStream());

    HashMap daten = getData(cmd);
    updateSheetValuesKategorien(workbook.getSheetAt(0), cmd, daten);
    updateSheetValuesStadtteile(workbook.getSheetAt(1), cmd, daten);

    return workbook;
  }

  private void updateSheetValuesKategorien(Sheet sheet, StatistikCommand cmd, HashMap daten) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    Row row = sheet.getRow(0);
    Cell cell = row.getCell(0);
    String ueberschrift = cell.getStringCellValue();

    Calendar c = Calendar.getInstance();
    c.setTime(cmd.getZeitraumVon());
    ueberschrift = ueberschrift.replace("#von#", sdf.format(c.getTime()));
    c.setTime(cmd.getZeitraumBis());
    ueberschrift = ueberschrift.replace("#bis#", sdf.format(c.getTime()));
    cell.setCellValue(ueberschrift);

    HashMap values = (HashMap) daten.get("Amt 66");

    /*
     * Erster Wert: Zeile in der CSV
     * Zweiter Wert: ID der Kategorie
     */
    Map<Integer, Integer> mapping = new HashMap<Integer, Integer>() {
      {
        put(6, 1);
        put(7, 35);
        put(8, 18);
        put(9, 54);
        put(10, 87);
        put(11, 66);
        put(12, 111);
      }
    };

    for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
      row = sheet.getRow(entry.getKey());
      setCellValue(row, 3, "vormonate", entry, values);
      setCellValue(row, 4, "neu", entry, values);
      setCellValue(row, 7, "abgeschlossen", entry, values);
      setCellValue(row, 9, "weiterhinOffen", entry, values);
    }

    values = (HashMap) daten.get("Amt 30");
    /*
     * Erster Wert: Zeile in der CSV
     * Zweiter Wert: ID der Kategorie
     */
    mapping = new HashMap<Integer, Integer>() {
      {
        put(16, 52);
        put(17, 53);
      }
    };
    for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
      row = sheet.getRow(entry.getKey());
      setCellValue(row, 3, "vormonate", entry, values);
      setCellValue(row, 4, "neu", entry, values);
      setCellValue(row, 7, "abgeschlossen", entry, values);
      setCellValue(row, 9, "weiterhinOffen", entry, values);
    }

    row = sheet.getRow(20);
    setCellMergedValue(row, 3, "vormonate", values, kategorieIds);
    setCellMergedValue(row, 4, "neu", values, kategorieIds);
    setCellMergedValue(row, 7, "abgeschlossen", values, kategorieIds);
    setCellMergedValue(row, 9, "weiterhinOffen", values, kategorieIds);

    values = (HashMap) daten.get("Amt 32");
    row = sheet.getRow(23);
    setCellMergedValue(row, 3, "vormonate", values);
    setCellMergedValue(row, 4, "neu", values);
    setCellMergedValue(row, 7, "abgeschlossen", values);
    setCellMergedValue(row, 9, "weiterhinOffen", values);
  }

  private void updateSheetValuesStadtteile(Sheet sheet, StatistikCommand cmd, HashMap daten) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    String prefix = "kategorie_";

    Row row = sheet.getRow(0);
    Cell cell = row.getCell(0);
    String ueberschrift = cell.getStringCellValue();

    Calendar c = Calendar.getInstance();
    c.setTime(cmd.getZeitraumVon());
    ueberschrift = ueberschrift.replace("#von#", sdf.format(c.getTime()));
    c.setTime(cmd.getZeitraumBis());
    ueberschrift = ueberschrift.replace("#bis#", sdf.format(c.getTime()));
    cell.setCellValue(ueberschrift);

    /*
     * Erster Wert: Zeile in der CSV
     * Zweiter Wert: ID des Stadtteils
     */
    Map<Integer, Integer> mapping = new HashMap<Integer, Integer>() {
      {
        put(4, 8);
        put(5, 9);
        put(6, 10);
        put(7, 11);
        put(8, 12);
        put(9, 13);
        put(10, 14);
        put(11, 15);
        put(12, 16);
        put(13, 17);
        put(14, 18);
        put(15, 19);
        put(16, 20);
        put(17, 21);
        put(18, 22);
        put(19, 23);
      }
    };

    for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
      row = sheet.getRow(entry.getKey());

      setCellValue(row, 3, "stadtteil_vormonate", entry, daten);
      setCellValue(row, 4, "stadtteil_neu", entry, daten);
      setCellValue(row, 7, "stadtteil_abgeschlossen", entry, daten);
      setCellValue(row, 9, "stadtteil_weiterhinOffen", entry, daten);
    }
  }

  private HashMap getData(StatistikCommand cmd) throws ParseException {
    OUs.clear();
    HashMap zusammenfassung = new HashMap();

    Calendar cZeitraumVon = Calendar.getInstance();
    cZeitraumVon.setTime(cmd.getZeitraumVon());
    cZeitraumVon.add(Calendar.DATE, -1);

    List<Object[]> vormonateHauptkategorien = statistikDao.getAnzahlOffeneVorgaengeNachHauptkategorienBis(hauptkategorieIds, cZeitraumVon.getTime());
    zusammenfassung = mergeResults(zusammenfassung, "vormonate", vormonateHauptkategorien);
    List<Object[]> vormonateKategorien = statistikDao.getAnzahlOffeneVorgaengeNachKategorienBis(kategorieIds, cZeitraumVon.getTime());
    zusammenfassung = mergeResults(zusammenfassung, "vormonate", vormonateKategorien);

    List<Object[]> neueHauptkategorien = statistikDao.getAnzahlNeueVorgaengeNachHauptkategorienInZeitraum(hauptkategorieIds, cmd.getZeitraumVon(), cmd.getZeitraumBis());
    zusammenfassung = mergeResults(zusammenfassung, "neu", neueHauptkategorien);
    List<Object[]> neueKategorien = statistikDao.getAnzahlNeueVorgaengeNachKategorienInZeitraum(kategorieIds, cmd.getZeitraumVon(), cmd.getZeitraumBis());
    zusammenfassung = mergeResults(zusammenfassung, "neu", neueKategorien);

    List<Object[]> abgeschlosseneHauptkategorien = statistikDao.getAnzahlAbgeschlosseneVorgaengeNachHauptkategorienInZeitraum(hauptkategorieIds, cmd.getZeitraumVon(), cmd.getZeitraumBis());
    zusammenfassung = mergeResults(zusammenfassung, "abgeschlossen", abgeschlosseneHauptkategorien);
    List<Object[]> abgeschlosseneKategorien = statistikDao.getAnzahlAbgeschlosseneVorgaengeNachKategorienInZeitraum(kategorieIds, cmd.getZeitraumVon(), cmd.getZeitraumBis());
    zusammenfassung = mergeResults(zusammenfassung, "abgeschlossen", abgeschlosseneKategorien);

    List<Object[]> weiterhinOffenHauptkategorien = statistikDao.getAnzahlOffeneVorgaengeNachHauptkategorienBis(hauptkategorieIds, cmd.getZeitraumBis());
    zusammenfassung = mergeResults(zusammenfassung, "weiterhinOffen", weiterhinOffenHauptkategorien);
    List<Object[]> weiterhinOffenKategorien = statistikDao.getAnzahlOffeneVorgaengeNachKategorienBis(kategorieIds, cmd.getZeitraumBis());
    zusammenfassung = mergeResults(zusammenfassung, "weiterhinOffen", weiterhinOffenKategorien);

    return zusammenfassung;
  }
}
