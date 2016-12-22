package de.fraunhofer.igd.klarschiff.dao;

import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class StatistikDao {

  @PersistenceContext
  EntityManager entityManager;

  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

  /*
   * Holt die Anzahl der 'abgeschlossenen' Vorgänge eingeschränkt auf die übergebenen Hauptkategorie-IDS
   * im Zeitraum gruppiert nach Zuständigkeit, Kategorie und Stadtteil
   */
  public List<Object[]> getAnzahlAbgeschlosseneVorgaengeNachHauptkategorienInZeitraum(int[] hauptkategorieIds, Date von, Date bis) {
    Calendar cVon = Calendar.getInstance();
    cVon.setTime(von);

    Calendar cBis = Calendar.getInstance();
    cBis.setTime(bis);
    cBis.add(Calendar.DATE, 1);

    String kategorieIDs = Arrays.toString(hauptkategorieIds);
    kategorieIDs = kategorieIDs.replaceAll("\\[|\\]", "");

    return entityManager.createNativeQuery("select count(kvorg.id), kvorg.zustaendigkeit, kk.parent, kkp.name, ksg.id stadtteil, ksg.name from klarschiff_verlauf kverl "
      + "  left join klarschiff_vorgang kvorg on kverl.vorgang = kvorg.id "
      + "  left join klarschiff_kategorie kk on kk.id = kvorg.kategorie "
      + "  left join klarschiff_stadtteil_grenze ksg on ST_Within(kvorg.ovi, ksg.grenze) "
      + "  inner join klarschiff_kategorie kkp on kk.parent = kkp.id and kkp.id in (" + kategorieIDs + ") "
      + "where "
      + "  kverl.id in ( "
      + "    select distinct on (vorgang) id from klarschiff_verlauf kverl_last where typ in ('status', 'erzeugt') and "
      + "      kverl_last.datum between '" + sdf.format(cVon.getTime()) + "' and '" + sdf.format(cBis.getTime()) + "' order by vorgang, datum desc "
      + "  ) and "
      + "  (kverl.typ = 'status' and kverl.wert_neu IN ('abgeschlossen')) "
      + "group by kvorg.zustaendigkeit, kk.parent, kkp.name, ksg.id, ksg.name order by kvorg.zustaendigkeit, kkp.name")
      .getResultList();
  }

  /*
   * Holt die Anzahl der 'abgeschlossenen' Vorgänge eingeschränkt auf die übergebenen Kategorie-IDS
   * im Zeitraum gruppiert nach Zuständigkeit, Kategorie und Stadtteil
   */
  public List<Object[]> getAnzahlAbgeschlosseneVorgaengeNachKategorienInZeitraum(int[] kategorieIds, Date von, Date bis) {
    Calendar cVon = Calendar.getInstance();
    cVon.setTime(von);

    Calendar cBis = Calendar.getInstance();
    cBis.setTime(bis);
    cBis.add(Calendar.DATE, 1);

    String kategorieIDs = Arrays.toString(kategorieIds);
    kategorieIDs = kategorieIDs.replaceAll("\\[|\\]", "");

    return entityManager.createNativeQuery("select count(kvorg.id), kvorg.zustaendigkeit, kk.id, kk.name, ksg.id stadtteil, ksg.name from klarschiff_verlauf kverl "
      + "  left join klarschiff_vorgang kvorg on kverl.vorgang = kvorg.id "
      + "  left join klarschiff_stadtteil_grenze ksg on ST_Within(kvorg.ovi, ksg.grenze) "
      + "  inner join klarschiff_kategorie kk on kk.id = kvorg.kategorie and kk.id in (" + kategorieIDs + ") "
      + "where "
      + "  kverl.id in ( "
      + "    select distinct on (vorgang) id from klarschiff_verlauf kverl_last where typ in ('status', 'erzeugt') and "
      + "      kverl_last.datum between '" + sdf.format(cVon.getTime()) + "' and '" + sdf.format(cBis.getTime()) + "' order by vorgang, datum desc "
      + "  ) and "
      + "  (kverl.typ = 'status' and kverl.wert_neu IN ('abgeschlossen')) "
      + "  and zustaendigkeit like 'a30%' "
      + "group by kvorg.zustaendigkeit, kk.id, kk.name, ksg.id, ksg.name order by kvorg.zustaendigkeit, kk.name")
      .getResultList();
  }

  /*
   * Holt die Anzahl der 'erzeugten' Vorgänge eingeschränkt auf die übergebenen Hauptkategorie-IDS
   * gruppiert nach Zuständigkeit, Hauptkategorie und Stadtteil
   */
  public List<Object[]> getAnzahlErzeugteVorgaengeNachHauptkategorienInZeitraum(int[] hauptkategorieIds, Date von, Date bis) {
    Calendar cVon = Calendar.getInstance();
    cVon.setTime(von);

    Calendar cBis = Calendar.getInstance();
    cBis.setTime(bis);
    cBis.add(Calendar.DATE, 1);

    String kategorieIDs = Arrays.toString(hauptkategorieIds);
    kategorieIDs = kategorieIDs.replaceAll("\\[|\\]", "");

       return entityManager.createNativeQuery("select count(kvorg.id), kvorg.zustaendigkeit, kk.parent, kkp.name, ksg.id stadtteil, ksg.name from klarschiff_verlauf kverl "
      + "  inner join ( "
      + "	select vorgang from klarschiff_verlauf where typ in ('erzeugt') and datum between '" + sdf.format(cVon.getTime()) + "' and '" + sdf.format(cBis.getTime()) + "' "
      + "  ) kverl_erzeug on kverl.vorgang = kverl_erzeug.vorgang "
      + "  left join klarschiff_vorgang kvorg on kverl.vorgang = kvorg.id "
      + "  left join klarschiff_kategorie kk on kk.id = kvorg.kategorie "
      + "  left join klarschiff_stadtteil_grenze ksg on ST_Within(kvorg.ovi, ksg.grenze) "
      + "  inner join klarschiff_kategorie kkp on kk.parent = kkp.id and kkp.id in (" + kategorieIDs + ") "
      + "where "
      + "  kverl.id in ( "
      + "    select distinct on (vorgang) id from klarschiff_verlauf kverl_last where typ in ('status', 'erzeugt') and "
      + "      kverl_last.datum between '" + sdf.format(cVon.getTime()) + "' and '" + sdf.format(cBis.getTime()) + "' order by vorgang, datum desc "
      + "  ) and "
      + "  (kverl.typ = 'erzeugt' or (kverl.typ = 'status' and kverl.wert_neu NOT IN ('gelöscht'))) "
      + "group by kvorg.zustaendigkeit, kk.parent, kkp.name, ksg.id, ksg.name order by kvorg.zustaendigkeit, kkp.name")
      .getResultList();
  }

  /*
   * Holt die Anzahl der 'erzeugten' Vorgänge eingeschränkt auf die übergebenen Kategorie-IDS
   * gruppiert nach Zuständigkeit, Hauptkategorie und Stadtteil
   */
  public List<Object[]> getAnzahlErzeugteVorgaengeNachKategorienInZeitraum(int[] kategorieIds, Date von, Date bis) {
    Calendar cVon = Calendar.getInstance();
    cVon.setTime(von);

    Calendar cBis = Calendar.getInstance();
    cBis.setTime(bis);
    cBis.add(Calendar.DATE, 1);

    String kategorieIDs = Arrays.toString(kategorieIds);
    kategorieIDs = kategorieIDs.replaceAll("\\[|\\]", "");

    return entityManager.createNativeQuery("select count(kvorg.id), kvorg.zustaendigkeit, kk.id, kk.name, ksg.id stadtteil, ksg.name from klarschiff_verlauf kverl "
      + "  inner join ( "
      + "	select vorgang from klarschiff_verlauf where typ in ('erzeugt') and datum between '" + sdf.format(cVon.getTime()) + "' and '" + sdf.format(cBis.getTime()) + "' "
      + "  ) kverl_erzeug on kverl.vorgang = kverl_erzeug.vorgang "
      + "  left join klarschiff_vorgang kvorg on kverl.vorgang = kvorg.id "
      + "  left join klarschiff_stadtteil_grenze ksg on ST_Within(kvorg.ovi, ksg.grenze) "
      + "  inner join klarschiff_kategorie kk on kk.id = kvorg.kategorie and kk.id in (" + kategorieIDs + ") "
      + "where "
      + "  kverl.id in ( "
      + "    select distinct on (vorgang) id from klarschiff_verlauf kverl_last where typ in ('status', 'erzeugt') and "
      + "      kverl_last.datum between '" + sdf.format(cVon.getTime()) + "' and '" + sdf.format(cBis.getTime()) + "' order by vorgang, datum desc "
      + "  ) and "
      + "  (kverl.typ = 'erzeugt' or (kverl.typ = 'status' and kverl.wert_neu NOT IN ('gelöscht'))) "
      + "  and zustaendigkeit like 'a30%' "
      + "group by kvorg.zustaendigkeit, kk.id, kk.name, ksg.id, ksg.name order by kvorg.zustaendigkeit, kk.name")
      .getResultList();
  }

  /*
   * Holt die Anzahl der 'neuen' Vorgänge eingeschränkt auf die übergebenen Hauptkategorie-IDS
   * im Zeitraum gruppiert nach Zuständigkeit, Hauptkategorie und Stadtteil
   */
  public List<Object[]> getAnzahlNeueVorgaengeNachHauptkategorienInZeitraum(int[] hauptkategorieIds, Date von, Date bis) {
    Calendar cVon = Calendar.getInstance();
    cVon.setTime(von);

    Calendar cBis = Calendar.getInstance();
    cBis.setTime(bis);
    cBis.add(Calendar.DATE, 1);

    String kategorieIDs = Arrays.toString(hauptkategorieIds);
    kategorieIDs = kategorieIDs.replaceAll("\\[|\\]", "");

    return entityManager.createNativeQuery("select count(kvorg.id), kvorg.zustaendigkeit, kk.parent, kkp.name, ksg.id stadtteil, ksg.name from klarschiff_verlauf kverl "
      + "  inner join ( "
      + "	select vorgang from klarschiff_verlauf where typ in ('erzeugt') and datum between '" + sdf.format(cVon.getTime()) + "' and '" + sdf.format(cBis.getTime()) + "' "
      + "  ) kverl_erzeug on kverl.vorgang = kverl_erzeug.vorgang "
      + "  left join klarschiff_vorgang kvorg on kverl.vorgang = kvorg.id "
      + "  left join klarschiff_kategorie kk on kk.id = kvorg.kategorie "
      + "  left join klarschiff_stadtteil_grenze ksg on ST_Within(kvorg.ovi, ksg.grenze) "
      + "  inner join klarschiff_kategorie kkp on kk.parent = kkp.id and kkp.id in (" + kategorieIDs + ") "
      + "where "
      + "  kverl.id in ( "
      + "    select distinct on (vorgang) id from klarschiff_verlauf kverl_last where typ in ('status', 'erzeugt') and "
      + "      kverl_last.datum between '" + sdf.format(cVon.getTime()) + "' and '" + sdf.format(cBis.getTime()) + "' order by vorgang, datum desc "
      + "  ) and "
      + "  (kverl.typ = 'erzeugt' or (kverl.typ = 'status' and kverl.wert_neu NOT IN ('Duplikat', 'wird nicht bearbeitet', 'gelöscht'))) "
      + "group by kvorg.zustaendigkeit, kk.parent, kkp.name, ksg.id, ksg.name order by kvorg.zustaendigkeit, kkp.name")
      .getResultList();
  }

  /*
   * Holt die Anzahl der 'neuen' Vorgänge eingeschränkt auf die übergebenen Hauptkategorie-IDS
   * im Zeitraum gruppiert nach Zuständigkeit, Hauptkategorie und Stadtteil
   */
  public List<Object[]> getAnzahlNeueVorgaengeNachKategorienInZeitraum(int[] kategorieIds, Date von, Date bis) {
    Calendar cVon = Calendar.getInstance();
    cVon.setTime(von);

    Calendar cBis = Calendar.getInstance();
    cBis.setTime(bis);
    cBis.add(Calendar.DATE, 1);

    String kategorieIDs = Arrays.toString(kategorieIds);
    kategorieIDs = kategorieIDs.replaceAll("\\[|\\]", "");

    return entityManager.createNativeQuery("select count(kvorg.id), kvorg.zustaendigkeit, kk.id, kk.name, ksg.id stadtteil, ksg.name from klarschiff_verlauf kverl "
      + "  inner join ( "
      + "	select vorgang from klarschiff_verlauf where typ in ('erzeugt') and datum between '" + sdf.format(cVon.getTime()) + "' and '" + sdf.format(cBis.getTime()) + "' "
      + "  ) kverl_erzeug on kverl.vorgang = kverl_erzeug.vorgang "
      + "  left join klarschiff_vorgang kvorg on kverl.vorgang = kvorg.id "
      + "  left join klarschiff_stadtteil_grenze ksg on ST_Within(kvorg.ovi, ksg.grenze) "
      + "  inner join klarschiff_kategorie kk on kk.id = kvorg.kategorie and kk.id in (" + kategorieIDs + ") "
      + "where "
      + "  kverl.id in ( "
      + "    select distinct on (vorgang) id from klarschiff_verlauf kverl_last where typ in ('status', 'erzeugt') and "
      + "      kverl_last.datum between '" + sdf.format(cVon.getTime()) + "' and '" + sdf.format(cBis.getTime()) + "' order by vorgang, datum desc "
      + "  ) and "
      + "  (kverl.typ = 'erzeugt' or (kverl.typ = 'status' and kverl.wert_neu NOT IN ('Duplikat', 'wird nicht bearbeitet', 'gelöscht'))) "
      + "  and zustaendigkeit like 'a30%' "
      + "group by kvorg.zustaendigkeit, kk.id, kk.name, ksg.id, ksg.name order by kvorg.zustaendigkeit, kk.name")
      .getResultList();
  }

  /*
   * Holt die Anzahl der 'offenen' Vorgänge eingeschränkt auf die übergebenen Hauptkategorie-IDS
   * gruppiert nach Zuständigkeit, Hauptkategorie und Stadtteil
   */
  public List<Object[]> getAnzahlOffeneVorgaengeNachHauptkategorienBis(int[] hauptkategorieIds, Date date) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    // +1 Tag
    c.add(Calendar.DATE, 1);

    String kategorieIDs = Arrays.toString(hauptkategorieIds);
    kategorieIDs = kategorieIDs.replaceAll("\\[|\\]", "");

    Query q = entityManager.createNativeQuery("select count(kvorg.id), kvorg.zustaendigkeit, kk.parent, kkp.name, ksg.id stadtteil, ksg.name from klarschiff_verlauf kverl "
      + "  left join klarschiff_vorgang kvorg on kverl.vorgang = kvorg.id "
      + "  left join klarschiff_kategorie kk on kk.id = kvorg.kategorie "
      + "  left join klarschiff_stadtteil_grenze ksg on ST_Within(kvorg.ovi, ksg.grenze) "
      + "  inner join klarschiff_kategorie kkp on kk.parent = kkp.id and kkp.id in (" + kategorieIDs + ") "
      + "where "
      + "  kverl.id in ( "
      + "    select distinct on (vorgang) kverl_last.id from klarschiff_verlauf kverl_last where kverl_last.typ in ('status', 'erzeugt') and "
      + "      kverl_last.datum < '" + sdf.format(c.getTime()) + "' order by kverl_last.vorgang, kverl_last.datum desc "
      + "  ) and "
      + "  (kverl.typ = 'erzeugt' or (kverl.typ = 'status' and kverl.wert_neu NOT IN ('abgeschlossen', 'Duplikat', 'wird nicht bearbeitet', 'gelöscht'))) "
      + "group by kvorg.zustaendigkeit, kk.parent, kkp.name, ksg.id, ksg.name order by kvorg.zustaendigkeit, kkp.name");
    return q.getResultList();
  }

  /*
   * Holt die Anzahl der 'offenen' Vorgänge eingeschränkt auf die übergebenen Kategorie-IDS
   * gruppiert nach Zuständigkeit, Kategorie und Stadtteil
   */
  public List<Object[]> getAnzahlOffeneVorgaengeNachKategorienBis(int[] kategorieIds, Date date) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    // +1 Tag
    c.add(Calendar.DATE, 1);

    String kategorieIDs = Arrays.toString(kategorieIds);
    kategorieIDs = kategorieIDs.replaceAll("\\[|\\]", "");

    Query q = entityManager.createNativeQuery("select count(kvorg.id), kvorg.zustaendigkeit, kk.id, kk.name, ksg.id stadtteil, ksg.name from klarschiff_verlauf kverl "
      + "  left join klarschiff_vorgang kvorg on kverl.vorgang = kvorg.id "
      + "  left join klarschiff_stadtteil_grenze ksg on ST_Within(kvorg.ovi, ksg.grenze) "
      + "  inner join klarschiff_kategorie kk on kk.id = kvorg.kategorie and kk.id in (" + kategorieIDs + ") "
      + "where "
      + "  kverl.id in ( "
      + "    select distinct on (vorgang) id from klarschiff_verlauf kverl_last where typ in ('status', 'erzeugt') and "
      + "      kverl_last.datum < '" + sdf.format(c.getTime()) + "' order by vorgang, datum desc "
      + "  ) and "
      + "  (kverl.typ = 'erzeugt' or (kverl.typ = 'status' and kverl.wert_neu NOT IN ('abgeschlossen', 'Duplikat', 'wird nicht bearbeitet', 'gelöscht'))) "
      + "  and zustaendigkeit like 'a30%' "
      + "group by kvorg.zustaendigkeit, kk.id, kk.name, ksg.id, ksg.name order by kvorg.zustaendigkeit, kk.name");
    return q.getResultList();
  }

  /*
   * Holt die Anzahl der Vorgänge eingeschränkt auf den übergebenen Status und die übergebenen Hauptkategorie-IDS
   * gruppiert nach Zuständigkeit, Hauptkategorie und Stadtteil
   */
  public List<Object[]> getAnzahlVorgaengeNachHauptkategorienUndStatusInZeitraum(int[] hauptkategorieIds, EnumVorgangStatus status, Date von, Date bis) {
    Calendar cVon = Calendar.getInstance();
    cVon.setTime(von);

    Calendar cBis = Calendar.getInstance();
    cBis.setTime(bis);
    cBis.add(Calendar.DATE, 1);

    String kategorieIDs = Arrays.toString(hauptkategorieIds);
    kategorieIDs = kategorieIDs.replaceAll("\\[|\\]", "");

    Query q = entityManager.createNativeQuery("select count(kvorg.id), kvorg.zustaendigkeit, kk.parent, kkp.name, ksg.id stadtteil, ksg.name from klarschiff_verlauf kverl "
      + "  left join klarschiff_vorgang kvorg on kverl.vorgang = kvorg.id "
      + "  left join klarschiff_kategorie kk on kk.id = kvorg.kategorie "
      + "  left join klarschiff_stadtteil_grenze ksg on ST_Within(kvorg.ovi, ksg.grenze) "
      + "  inner join klarschiff_kategorie kkp on kk.parent = kkp.id and kkp.id in (" + kategorieIDs + ") "
      + "where "
      + "  kverl.id in ( "
      + "    select distinct on (vorgang) id from klarschiff_verlauf kverl_last where typ = 'status' and "
      + "      datum between '" + sdf.format(cVon.getTime()) + "' and '" + sdf.format(cBis.getTime()) + "' order by kverl_last.vorgang, kverl_last.datum desc "
      + "  ) and kverl.typ = 'status' and kverl.wert_neu = '" + status.getText() + "' "
      + "group by kvorg.zustaendigkeit, kk.parent, kkp.name, ksg.id, ksg.name order by kvorg.zustaendigkeit, kkp.name");
    return q.getResultList();
  }

  /*
   * Holt die Anzahl der Vorgänge eingeschränkt auf den übergebenen Status und die übergebenen Hauptkategorie-IDS
   * gruppiert nach Zuständigkeit, Hauptkategorie und Stadtteil
   */
  public List<Object[]> getAnzahlVorgaengeNachKategorienUndStatusInZeitraum(int[] kategorieIds, EnumVorgangStatus status, Date von, Date bis) {
    Calendar cVon = Calendar.getInstance();
    cVon.setTime(von);

    Calendar cBis = Calendar.getInstance();
    cBis.setTime(bis);
    cBis.add(Calendar.DATE, 1);

    String kategorieIDs = Arrays.toString(kategorieIds);
    kategorieIDs = kategorieIDs.replaceAll("\\[|\\]", "");

    Query q = entityManager.createNativeQuery("select count(kvorg.id), kvorg.zustaendigkeit, kk.id, kk.name, ksg.id stadtteil, ksg.name from klarschiff_verlauf kverl "
      + "  left join klarschiff_vorgang kvorg on kverl.vorgang = kvorg.id "
      + "  left join klarschiff_stadtteil_grenze ksg on ST_Within(kvorg.ovi, ksg.grenze) "
      + "  inner join klarschiff_kategorie kk on kk.id = kvorg.kategorie and kk.id in (" + kategorieIDs + ") "
      + "where "
      + "  kverl.id in ( "
      + "    select distinct on (vorgang) id from klarschiff_verlauf kverl_last where typ = 'status' and "
      + "      datum between '" + sdf.format(cVon.getTime()) + "' and '" + sdf.format(cBis.getTime()) + "' order by kverl_last.vorgang, kverl_last.datum desc "
      + "  ) and kverl.typ = 'status' and kverl.wert_neu = '" + status.getText() + "' and zustaendigkeit like 'a30%' "
      + "group by kvorg.zustaendigkeit, kk.id, kk.name, ksg.id, ksg.name order by kvorg.zustaendigkeit, kk.name");
    return q.getResultList();
  }
}
