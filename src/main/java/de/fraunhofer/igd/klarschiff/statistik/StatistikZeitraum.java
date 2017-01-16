package de.fraunhofer.igd.klarschiff.statistik;

import de.fraunhofer.igd.klarschiff.dao.GrenzenDao;
import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.dao.StatistikDao;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;
import de.fraunhofer.igd.klarschiff.web.StatistikCommand;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

public class StatistikZeitraum extends StatistikCommon {

  private int rowCountKategorien = 0;
  private int rowCountStadtteile = 0;
  private ArrayList<Integer> headlineRows = new ArrayList<Integer>();

  public StatistikZeitraum(GrenzenDao grenzenDao, KategorieDao kategorieDao, StatistikDao statistikDao, SecurityService securityService, SettingsService settingsService) {
    this.grenzenDao = grenzenDao;
    this.kategorieDao = kategorieDao;
    this.statistikDao = statistikDao;
    this.securityService = securityService;
    this.settingsService = settingsService;
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

    Row tmpl_row_department_begin = sheet.getRow(4);
    Row tmpl_row_department_name = sheet.getRow(5);
    Row tmpl_row_department_kategory = sheet.getRow(6);
    Row tmpl_row_department_rest = sheet.getRow(7);
    Row tmpl_row_department_end = sheet.getRow(8);
    Row tmpl_row_sum_begin = sheet.getRow(9);
    Row tmpl_row_sum_counts = sheet.getRow(10);
    Row tmpl_row_sum_end = sheet.getRow(11);

    int current_row = 4;
    headlineRows.clear();
    rowCountKategorien += current_row;

    boolean next_department = true;
    int department_count = 1;
    while (next_department) {
      String department_name = settingsService.getPropertyValue("statistic.department." + department_count + ".name");
      String department_categories_main = settingsService.getPropertyValue("statistic.department." + department_count + ".categories.main");
      String department_categories_sub = settingsService.getPropertyValue("statistic.department." + department_count + ".categories.sub");
      if (department_name == null) {
        next_department = false;
        break;
      }
      department_count++;

      String categories = "";
      if (department_categories_main != null) {
        categories = department_categories_main;
      }
      if (department_categories_sub != null) {
        if (categories.length() == 0) {
          categories = department_categories_sub;
        } else {
          categories += "," + department_categories_sub;
        }
      }
      int categories_count = 0;
      if (categories.length() > 0) {
        categories_count = categories.split(",").length;
      }

      HashMap values = (HashMap) daten.get(department_name);

      copyRow(tmpl_row_department_begin, sheet, current_row);
      current_row++;

      headlineRows.add(current_row);
      Row new_row_department_name = copyRow(tmpl_row_department_name, sheet, current_row);
      cell = new_row_department_name.getCell(0);
      cell.setCellValue(department_name);
      new_row_department_name = setDefaultRowFormulas(new_row_department_name, (rowCountKategorien + 2));
      if (categories_count > 0) {
        cell = new_row_department_name.getCell(3);
        cell.setCellFormula("SUM(D" + (current_row + 2) + ":D" + (current_row + 2 + categories_count) + ")");
        cell = new_row_department_name.getCell(4);
        cell.setCellFormula("SUM(E" + (current_row + 2) + ":E" + (current_row + 2 + categories_count) + ")");
        cell = new_row_department_name.getCell(5);
        cell.setCellFormula("SUM(F" + (current_row + 2) + ":F" + (current_row + 2 + categories_count) + ")");
        cell = new_row_department_name.getCell(7);
        cell.setCellFormula("SUM(H" + (current_row + 2) + ":H" + (current_row + 2 + categories_count) + ")");
        cell = new_row_department_name.getCell(9);
        cell.setCellFormula("SUM(J" + (current_row + 2) + ":J" + (current_row + 2 + categories_count) + ")");
      } else {
        setCellMergedValue(new_row_department_name, 3, "vormonate", values);
        setCellMergedValue(new_row_department_name, 4, "neu", values);
        setCellMergedValue(new_row_department_name, 7, "abgeschlossen", values);
        setCellMergedValue(new_row_department_name, 9, "weiterhinOffen", values);
      }
      current_row++;

      if (categories_count > 0) {
        List<Long> kategorieIds = new ArrayList<Long>();
        for (String category : categories.split(",")) {
          if (category.length() == 0) {
            continue;
          }
          Long kategorieId = Long.parseLong(category);
          if (!kategorieIds.contains(kategorieId)) {
            kategorieIds.add(kategorieId);
          }
          Row new_row_department_category = copyRow(tmpl_row_department_kategory, sheet, current_row);
          new_row_department_category = setDefaultRowFormulas(new_row_department_category, (rowCountKategorien + 2));

          Kategorie kategorie = kategorieDao.findKategorie(kategorieId);
          cell = new_row_department_category.getCell(0);
          cell.setCellValue(kategorie.getName());

          setCellValue(new_row_department_category, 3, "vormonate", kategorie.getId(), values);
          setCellValue(new_row_department_category, 4, "neu", kategorie.getId(), values);
          setCellValue(new_row_department_category, 7, "abgeschlossen", kategorie.getId(), values);
          setCellValue(new_row_department_category, 9, "weiterhinOffen", kategorie.getId(), values);

          current_row++;
        }

        Row new_row_department_rest = copyRow(tmpl_row_department_rest, sheet, current_row);
        new_row_department_rest = setDefaultRowFormulas(new_row_department_rest, (rowCountKategorien + 2));
        setCellMergedValue(new_row_department_rest, 3, "vormonate", values, kategorieIds);
        setCellMergedValue(new_row_department_rest, 4, "neu", values, kategorieIds);
        setCellMergedValue(new_row_department_rest, 7, "abgeschlossen", values, kategorieIds);
        setCellMergedValue(new_row_department_rest, 9, "weiterhinOffen", values, kategorieIds);

        current_row++;
      }

      copyRow(tmpl_row_department_end, sheet, current_row);
      current_row++;
    }

    copyRow(tmpl_row_sum_begin, sheet, current_row);
    current_row++;

    row = copyRow(tmpl_row_sum_counts, sheet, current_row);
    setSummenRow(row, headlineRows);
    current_row++;

    copyRow(tmpl_row_sum_end, sheet, current_row);
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

    Row tmpl_row_stadtteil_name = sheet.getRow(5);
    Row tmpl_row_stadtteil_end = sheet.getRow(6);
    Row tmpl_row_sum_begin = sheet.getRow(7);
    Row tmpl_row_sum_counts = sheet.getRow(8);
    Row tmpl_row_sum_end = sheet.getRow(9);

    int current_row = 5;
    headlineRows.clear();
    List<Object[]> stadtteile = grenzenDao.findStadtteilGrenzen();
    rowCountStadtteile = (current_row + stadtteile.size() + 1);

    for (Object[] stadtteil : stadtteile) {
      headlineRows.add(current_row);

      row = copyRow(tmpl_row_stadtteil_name, sheet, current_row);
      row = setDefaultRowFormulas(row, (rowCountStadtteile + 2));

      cell = row.getCell(0);
      cell.setCellValue((String) stadtteil[1]);

      Long stadtteil_id = ((Integer) stadtteil[0]).longValue();
      setCellValue(row, 3, "stadtteil_vormonate", stadtteil_id, daten);
      setCellValue(row, 4, "stadtteil_neu", stadtteil_id, daten);
      setCellValue(row, 7, "stadtteil_abgeschlossen", stadtteil_id, daten);
      setCellValue(row, 9, "stadtteil_weiterhinOffen", stadtteil_id, daten);
      current_row++;
    }

    copyRow(tmpl_row_stadtteil_end, sheet, current_row);
    current_row++;

    copyRow(tmpl_row_sum_begin, sheet, current_row);
    current_row++;

    row = copyRow(tmpl_row_sum_counts, sheet, current_row);
    setSummenRow(row, headlineRows);
    current_row++;

    copyRow(tmpl_row_sum_end, sheet, current_row);
  }

  private HashMap getData(StatistikCommand cmd) throws ParseException {

    List<Long> hauptkategorieIds = new ArrayList<Long>();
    List<Long> unterkategorieIds = new ArrayList<Long>();

    boolean next_department = true;
    int department_count = 1;
    while (next_department) {
      String department_name = settingsService.getPropertyValue("statistic.department." + department_count + ".name");
      String department_categories_main = settingsService.getPropertyValue("statistic.department." + department_count + ".categories.main");
      String department_categories_sub = settingsService.getPropertyValue("statistic.department." + department_count + ".categories.sub");
      if (department_name == null) {
        next_department = false;
        break;
      }

      rowCountKategorien += 3;
      boolean hatKategorien = false;
      if (department_categories_main != null && department_categories_main.length() > 0) {
        for (String cat : department_categories_main.split(",")) {
          Long l = Long.parseLong(cat);
          if (!hauptkategorieIds.contains(l)) {
            hauptkategorieIds.add(l);
            rowCountKategorien++;
            hatKategorien = true;
          }
        }
      }
      if (department_categories_sub != null && department_categories_sub.length() > 0) {
        for (String cat : department_categories_sub.split(",")) {
          Long l = Long.parseLong(cat);
          if (!unterkategorieIds.contains(l)) {
            unterkategorieIds.add(l);
            rowCountKategorien++;
            hatKategorien = true;
          }
        }
      }
      // Zusätzliche Zeile für 'Rest'
      if (hatKategorien) {
        rowCountKategorien++;
      }

      department_count++;
    }

    OUs.clear();
    HashMap zusammenfassung = new HashMap();

    Calendar cZeitraumVon = Calendar.getInstance();
    cZeitraumVon.setTime(cmd.getZeitraumVon());
    cZeitraumVon.add(Calendar.DATE, -1);

    List<Object[]> vormonateKategorien = statistikDao.getAnzahlOffeneVorgaengeBis(cZeitraumVon.getTime());
    zusammenfassung = mergeResults(zusammenfassung, "vormonate", vormonateKategorien);

    List<Object[]> neueKategorien = statistikDao.getAnzahlNeueVorgaengeInZeitraum(cmd.getZeitraumVon(), cmd.getZeitraumBis());
    zusammenfassung = mergeResults(zusammenfassung, "neu", neueKategorien);

    List<Object[]> abgeschlosseneKategorien = statistikDao.getAnzahlAbgeschlosseneVorgaengeInZeitraum(cmd.getZeitraumVon(), cmd.getZeitraumBis());
    zusammenfassung = mergeResults(zusammenfassung, "abgeschlossen", abgeschlosseneKategorien);

    List<Object[]> weiterhinOffenKategorien = statistikDao.getAnzahlOffeneVorgaengeBis(cmd.getZeitraumBis());
    zusammenfassung = mergeResults(zusammenfassung, "weiterhinOffen", weiterhinOffenKategorien);

    return zusammenfassung;
  }

  private Row setDefaultRowFormulas(Row row, int sum_row) {
    int current_row = (row.getRowNum() + 1);

    Cell cell = row.getCell(5);
    cell.setCellFormula("D" + current_row + "+E" + current_row);

    cell = row.getCell(6);
    String form = "F" + current_row + "/F" + sum_row + "%";
    cell.setCellFormula("IF(ISERROR(" + form + "),0," + form + ")");

    cell = row.getCell(8);
    form = "H" + current_row + "/F" + current_row + "%";
    cell.setCellFormula("IF(ISERROR(" + form + "),0," + form + ")");

    cell = row.getCell(10);
    form = "J" + current_row + "/F" + current_row + "%";
    cell.setCellFormula("IF(ISERROR(" + form + "),0," + form + ")");

    return row;
  }

  private Row setSummenRow(Row row, List<Integer> add_rows) {

    Map<Integer, String> mapping = new HashMap<Integer, String>() {
      {
        put(3, "D");
        put(4, "E");
        put(5, "F");
        put(6, "G");
        put(7, "H");
        put(9, "J");
      }
    };

    Cell cell;
    String form = "";
    for (Map.Entry<Integer, String> entry : mapping.entrySet()) {
      form = "";
      for (Integer add_row : add_rows) {
        if (form.length() > 0) {
          form += "+";
        }
        form += entry.getValue() + (add_row + 1);
      }
      cell = row.getCell(entry.getKey());
      cell.setCellFormula(form);
    }
    cell = row.getCell(8);
    form = "H" + (row.getRowNum() + 1) + "/F" + (row.getRowNum() + 1) + "%";
    cell.setCellFormula("IF(ISERROR(" + form + "),0," + form + ")");

    cell = row.getCell(10);
    form = "J" + (row.getRowNum() + 1) + "/F" + (row.getRowNum() + 1) + "%";
    cell.setCellFormula("IF(ISERROR(" + form + "),0," + form + ")");

    return row;
  }
}
