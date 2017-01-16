package de.fraunhofer.igd.klarschiff.statistik;

import de.fraunhofer.igd.klarschiff.dao.StatistikDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.web.StatistikCommand;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

public class StatistikKumulativ extends StatistikCommon {

  int[] hauptkategorieIds = {1, 18, 35, 54, 66, 87, 111};
  int[] kategorieIds = {52, 53};

  public StatistikKumulativ(StatistikDao statistikDao, SecurityService securityService, VorgangDao vorgangDao) {
    this.statistikDao = statistikDao;
    this.securityService = securityService;
    this.vorgangDao = vorgangDao;
  }

  public HSSFWorkbook createStatistik(StatistikCommand cmd) throws IOException, ParseException {
    String file = "classpath:META-INF/templates/statistikKumulativ.xls";
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
    ueberschrift = ueberschrift.replace("#vorgang#", vorgangDao.getLastVorgang().getId().toString());
    cell.setCellValue(ueberschrift);
    
    

    row = sheet.getRow(36);
    cell = row.getCell(5);
    cell.setCellValue(sdf.format(c.getTime()));

    HashMap values = (HashMap) daten.get("Amt 66");

    /*
     * Erster Wert: Zeile in der CSV
     * Zweiter Wert: ID der Kategorie
     */
    Map<Integer, Integer> mapping = new HashMap<Integer, Integer>() {
      {
        put(1, 1);
        put(2, 35);
        put(3, 18);
        put(4, 54);
        put(5, 87);
        put(6, 66);
        put(7, 111);
      }
    };

    int startRow = 5;
    for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
      row = sheet.getRow(startRow + entry.getKey());
//      setCellValue(row, 4, "gesamt", entry, values);
//      setCellValue(row, 6, "duplikate", entry, values);
//      setCellValue(row, 7, "wirdNichtBearbeitet", entry, values);
    }
    startRow = 38;
    for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
      row = sheet.getRow(startRow + entry.getKey());
//      setCellValue(row, 3, "abgeschlossen", entry, values);
//      setCellValue(row, 5, "weiterhinOffen", entry, values);
    }

    values = (HashMap) daten.get("Amt 30");
    /*
     * Erster Wert: Zeile in der CSV
     * Zweiter Wert: ID der Kategorie
     */
    mapping = new HashMap<Integer, Integer>() {
      {
        put(1, 52);
        put(2, 53);
      }
    };
    startRow = 15;
    for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
      row = sheet.getRow(startRow + entry.getKey());
//      setCellValue(row, 4, "gesamt", entry, values);
//      setCellValue(row, 6, "duplikate", entry, values);
//      setCellValue(row, 7, "wirdNichtBearbeitet", entry, values);
    }
    startRow = 48;
    for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
      row = sheet.getRow(startRow + entry.getKey());
//      setCellValue(row, 3, "abgeschlossen", entry, values);
//      setCellValue(row, 5, "weiterhinOffen", entry, values);
    }

    row = sheet.getRow(20);
//    setCellMergedValue(row, 4, "gesamt", values, kategorieIds);
//    setCellMergedValue(row, 6, "duplikate", values, kategorieIds);
//    setCellMergedValue(row, 7, "wirdNichtBearbeitet", values, kategorieIds);
    row = sheet.getRow(53);
//    setCellMergedValue(row, 3, "abgeschlossen", values, kategorieIds);
//    setCellMergedValue(row, 5, "weiterhinOffen", values, kategorieIds);

    values = (HashMap) daten.get("Amt 32");
    row = sheet.getRow(23);
    setCellMergedValue(row, 4, "gesamt", values);
    setCellMergedValue(row, 6, "duplikate", values);
    setCellMergedValue(row, 7, "wirdNichtBearbeitet", values);
    row = sheet.getRow(56);
    setCellMergedValue(row, 3, "abgeschlossen", values);
    setCellMergedValue(row, 5, "weiterhinOffen", values);
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
    ueberschrift = ueberschrift.replace("#vorgang#", vorgangDao.getLastVorgang().getId().toString());
    cell.setCellValue(ueberschrift);
    /*
     * Erster Wert: Zeile in der CSV
     * Zweiter Wert: ID des Stadtteils
     */
    Map<Integer, Integer> mapping = new HashMap<Integer, Integer>() {
      {
        put(1, 8);
        put(2, 9);
        put(3, 10);
        put(4, 11);
        put(5, 12);
        put(6, 13);
        put(7, 14);
        put(8, 15);
        put(9, 16);
        put(10, 17);
        put(11, 18);
        put(12, 19);
        put(13, 20);
        put(14, 21);
        put(15, 22);
        put(16, 23);
      }
    };
    int startRow = 3;
    for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
      row = sheet.getRow(startRow + entry.getKey());
//      setCellValue(row, 4, "stadtteil_gesamt", entry, daten);
//      setCellValue(row, 6, "stadtteil_duplikate", entry, daten);
//      setCellValue(row, 7, "stadtteil_wirdNichtBearbeitet", entry, daten);
    }
    startRow = 31;
    for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
      row = sheet.getRow(startRow + entry.getKey());
//      setCellValue(row, 3, "stadtteil_abgeschlossen", entry, daten);
//      setCellValue(row, 5, "stadtteil_weiterhinOffen", entry, daten);
    }

    row = sheet.getRow(31);
    cell = row.getCell(5);
    cell.setCellValue(sdf.format(c.getTime()));
  }

  private HashMap getData(StatistikCommand cmd) throws ParseException {
    OUs.clear();
    HashMap zusammenfassung = new HashMap();

//    List<Object[]> gesamtHauptkategorien = statistikDao.getAnzahlErzeugteVorgaengeNachHauptkategorienInZeitraum(hauptkategorieIds, cmd.getZeitraumVon(), cmd.getZeitraumBis());
//    zusammenfassung = mergeResults(zusammenfassung, "gesamt", gesamtHauptkategorien);
//    List<Object[]> gesamtKategorien = statistikDao.getAnzahlErzeugteVorgaengeNachKategorienInZeitraum(kategorieIds, cmd.getZeitraumVon(), cmd.getZeitraumBis());
//    zusammenfassung = mergeResults(zusammenfassung, "gesamt", gesamtKategorien);
//
//    List<Object[]> duplikateHauptkategorien = statistikDao.getAnzahlVorgaengeNachHauptkategorienUndStatusInZeitraum(hauptkategorieIds, EnumVorgangStatus.duplikat, cmd.getZeitraumVon(), cmd.getZeitraumBis());
//    zusammenfassung = mergeResults(zusammenfassung, "duplikate", duplikateHauptkategorien);
//    List<Object[]> duplikateKategorien = statistikDao.getAnzahlVorgaengeNachKategorienUndStatusInZeitraum(kategorieIds, EnumVorgangStatus.duplikat, cmd.getZeitraumVon(), cmd.getZeitraumBis());
//    zusammenfassung = mergeResults(zusammenfassung, "duplikate", duplikateKategorien);
//
//    List<Object[]> wirdNichtBearbeitetHauptkategorien = statistikDao.getAnzahlVorgaengeNachHauptkategorienUndStatusInZeitraum(hauptkategorieIds, EnumVorgangStatus.wirdNichtBearbeitet, cmd.getZeitraumVon(), cmd.getZeitraumBis());
//    zusammenfassung = mergeResults(zusammenfassung, "wirdNichtBearbeitet", wirdNichtBearbeitetHauptkategorien);
//    List<Object[]> wirdNichtBearbeitetKategorien = statistikDao.getAnzahlVorgaengeNachKategorienUndStatusInZeitraum(kategorieIds, EnumVorgangStatus.wirdNichtBearbeitet, cmd.getZeitraumVon(), cmd.getZeitraumBis());
//    zusammenfassung = mergeResults(zusammenfassung, "wirdNichtBearbeitet", wirdNichtBearbeitetKategorien);
//
//    List<Object[]> abgeschlossenHauptkategorien = statistikDao.getAnzahlAbgeschlosseneVorgaengeNachHauptkategorienInZeitraum(hauptkategorieIds, cmd.getZeitraumVon(), cmd.getZeitraumBis());
//    zusammenfassung = mergeResults(zusammenfassung, "abgeschlossen", abgeschlossenHauptkategorien);
//    List<Object[]> abgeschlossenKategorien = statistikDao.getAnzahlAbgeschlosseneVorgaengeNachKategorienInZeitraum(kategorieIds, cmd.getZeitraumVon(), cmd.getZeitraumBis());
//    zusammenfassung = mergeResults(zusammenfassung, "abgeschlossen", abgeschlossenKategorien);
//
//    List<Object[]> weiterhinOffenHauptkategorien = statistikDao.getAnzahlOffeneVorgaengeNachHauptkategorienBis(hauptkategorieIds, cmd.getZeitraumBis());
//    zusammenfassung = mergeResults(zusammenfassung, "weiterhinOffen", weiterhinOffenHauptkategorien);
//    List<Object[]> weiterhinOffenKategorien = statistikDao.getAnzahlOffeneVorgaengeNachKategorienBis(kategorieIds, cmd.getZeitraumBis());
//    zusammenfassung = mergeResults(zusammenfassung, "weiterhinOffen", weiterhinOffenKategorien);

    return zusammenfassung;
  }
}
