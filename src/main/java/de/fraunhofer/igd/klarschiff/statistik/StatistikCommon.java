package de.fraunhofer.igd.klarschiff.statistik;

import de.fraunhofer.igd.klarschiff.dao.GrenzenDao;
import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.dao.StatistikDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class StatistikCommon {

  GrenzenDao grenzenDao;
  KategorieDao kategorieDao;
  StatistikDao statistikDao;
  SecurityService securityService;
  SettingsService settingsService;
  VorgangDao vorgangDao;
  HashMap OUs = new HashMap();
  HashMap kategorien = new HashMap();

  protected HashMap mergeResults(HashMap zusammenfassung, String prefix, List<Object[]> liste) {
    for (Object[] result : liste) {
      if (result[1] != null) {
        int anzahl = Integer.parseInt(result[0].toString());
        String zustaendigkeit = result[1].toString();
        String kategorieId = result[2].toString();
        String parentId = result[3].toString();
        String stadtteilId = result[5].toString();
        String ou = findOu(zustaendigkeit);

        if (ou != null) {
          HashMap tmpHash = new HashMap();
          if (zusammenfassung.containsKey(ou)) {
            tmpHash = (HashMap) zusammenfassung.get(ou);
          }

          int tmpAnzKategorie = 0;
          String kategoryKey = prefix + kategorieId;
          if (tmpHash.containsKey(kategoryKey)) {
            tmpAnzKategorie = Integer.parseInt(tmpHash.get(kategoryKey).toString());
          }
          tmpHash.put(kategoryKey, tmpAnzKategorie + anzahl);
          zusammenfassung.put(ou, tmpHash);

          if (parentId != null) {
            tmpAnzKategorie = 0;
            kategoryKey = prefix + parentId;
            if (tmpHash.containsKey(kategoryKey)) {
              tmpAnzKategorie = Integer.parseInt(tmpHash.get(kategoryKey).toString());
            }
            tmpHash.put(kategoryKey, tmpAnzKategorie + anzahl);
            zusammenfassung.put(ou, tmpHash);
          }

          int tmpAnzStadtteil = 0;
          String stadtteilKey = "stadtteil_" + prefix + stadtteilId;
          if (zusammenfassung.containsKey(stadtteilKey)) {
            tmpAnzStadtteil = Integer.parseInt(zusammenfassung.get(stadtteilKey).toString());
          }
          zusammenfassung.put(stadtteilKey, tmpAnzStadtteil + anzahl);
        }
      }
    }
    return zusammenfassung;
  }

  protected String findOu(String zustaendigkeit) {
    if (OUs.containsKey(zustaendigkeit)) {
      return (String) OUs.get(zustaendigkeit);
    }
    Role r = securityService.getZustaendigkeit(zustaendigkeit);
    if (r != null) {
      String ou = null;

      boolean next_department = true;
      int department_count = 1;
      while (next_department) {
        String department_name = settingsService.getPropertyValue("statistic.department." + department_count + ".name");
        if (department_name == null) {
          next_department = false;
        } else {
          if (department_name.equals(r.getOu())) {
            ou = r.getOu();
            next_department = false;
          }
        }
        department_count++;
      }
      OUs.put(zustaendigkeit, ou);
      return ou;
    } else {
      OUs.put(zustaendigkeit, null);
      return null;
    }
  }

  protected Row copyRow(Row sourceRow, Sheet worksheet, int destinationRowNum) {
    Row newRow = worksheet.createRow(destinationRowNum);

    for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
      Cell oldCell = sourceRow.getCell(i);
      Cell newCell = newRow.createCell(i);

      if (oldCell != null) {
        newCell.setCellStyle(oldCell.getCellStyle());
        newCell.setCellType(oldCell.getCellType());
        switch (oldCell.getCellType()) {
          case Cell.CELL_TYPE_BLANK:
            break;
          case Cell.CELL_TYPE_BOOLEAN:
            newCell.setCellValue(oldCell.getBooleanCellValue());
            break;
          case Cell.CELL_TYPE_FORMULA:
            newCell.setCellFormula(oldCell.getCellFormula());
            break;
          case Cell.CELL_TYPE_NUMERIC:
            newCell.setCellValue(oldCell.getNumericCellValue());
            break;
          case Cell.CELL_TYPE_STRING:
            newCell.setCellValue(oldCell.getRichStringCellValue());
            break;
        }
      }
    }
    return newRow;
  }

  protected void setCellValue(Row row, int col, String prefix, Long entry_id, HashMap values) {
    Cell cell = row.getCell(col);
    if (values.containsKey(prefix + entry_id)) {
      cell.setCellValue(Double.parseDouble(values.get(prefix + entry_id).toString()));
    }
  }

  protected void setCellMergedValue(Row row, int col, String prefix, HashMap values) {
    setCellMergedValue(row, col, prefix, values, null);
  }

  protected void setCellMergedValue(Row row, int col, String prefix, HashMap values, List<Long> excludeKategorieIds) {
    if (values == null) {
      return;
    }
    int tmp = 0;
    Iterator itKeys = values.keySet().iterator();
    while (itKeys.hasNext()) {
      String key = (String) itKeys.next();

      if (key.startsWith(prefix)) {

        Long kategorieId = Long.parseLong(key.replace(prefix, ""));
        Kategorie kategorie = null;
        if (kategorien.containsKey(kategorieId)) {
          kategorie = (Kategorie) kategorien.get(kategorieId);
        } else {
          kategorie = kategorieDao.findKategorie(kategorieId);
          kategorien.put(kategorieId, kategorie);
        }

        if (key.startsWith(prefix) && kategorie.getParent() != null
          && (excludeKategorieIds == null || ((!excludeKategorieIds.contains(kategorieId) && !excludeKategorieIds.contains(kategorie.getParent().getId()))))) {
          tmp += Integer.parseInt(values.get(key).toString());
        }
      }
    }
    Cell cell = row.getCell(col);
    cell.setCellValue(Double.parseDouble(String.valueOf(tmp)));
  }

}
