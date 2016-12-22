package de.fraunhofer.igd.klarschiff.statistik;

import de.fraunhofer.igd.klarschiff.dao.StatistikDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class StatistikCommon {

  StatistikDao statistikDao;
  SecurityService securityService;
  VorgangDao vorgangDao;
  HashMap OUs = new HashMap();

  protected HashMap mergeResults(HashMap zusammenfassung, String prefix, List<Object[]> liste) {
    for (Object[] result : liste) {
      if (result[1] != null) {
        int anzahl = Integer.parseInt(result[0].toString());
        String zustaendigkeit = result[1].toString();
        String kategorieId = result[2].toString();
        String stadtteilId = result[4].toString();
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
    if(r != null) {
      OUs.put(zustaendigkeit, r.getOu());
    } else {
      OUs.put(zustaendigkeit, null);
      return null;
    }
    return r.getOu();
  }

  protected void setCellValue(Row row, int col, String prefix, Map.Entry<Integer, Integer> entry, HashMap values) {
    Cell cell = row.getCell(col);
    if (values.containsKey(prefix + entry.getValue())) {
      cell.setCellValue(Double.parseDouble(values.get(prefix + entry.getValue()).toString()));
    }
  }

  protected void setCellMergedValue(Row row, int col, String prefix, HashMap values) {
    setCellMergedValue(row, col, prefix, values, null);
  }

  protected void setCellMergedValue(Row row, int col, String prefix, HashMap values, int[] excludeKategorieIds) {
    if (values == null) {
      return;
    }
    int tmp = 0;
    Iterator itKeys = values.keySet().iterator();
    while (itKeys.hasNext()) {
      String key = (String) itKeys.next();

      if (key.startsWith(prefix)) {

        Integer[] newArray = ArrayUtils.toObject(excludeKategorieIds);
        if (key.startsWith(prefix) && (newArray == null
          || !Arrays.asList(newArray).contains(Integer.parseInt(key.replace(prefix, ""))))) {
          tmp += Integer.parseInt(values.get(key).toString());
        }
      }
    }
    Cell cell = row.getCell(col);
    cell.setCellValue(Double.parseDouble(String.valueOf(tmp)));
  }
  
}
