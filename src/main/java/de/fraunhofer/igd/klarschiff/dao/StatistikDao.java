package de.fraunhofer.igd.klarschiff.dao;

import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import java.text.SimpleDateFormat;
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
   * Holt die Anzahl der 'gelösten' Vorgänge eingeschränkt auf die übergebenen Kategorie-IDS
   * im Zeitraum gruppiert nach Zuständigkeit, Kategorie und Stadtteil
   */
  public List<Object[]> getAnzahlAbgeschlosseneVorgaengeInZeitraum(EnumVorgangTyp typ, Date von, Date bis) {
    Calendar cVon = Calendar.getInstance();
    cVon.setTime(von);

    Calendar cBis = Calendar.getInstance();
    cBis.setTime(bis);
    cBis.add(Calendar.DATE, 1);

    return entityManager.createNativeQuery("select count(kvorg.id), kvorg.zustaendigkeit, kk.id, kk.parent, kk.name as kk_name, ksg.id stadtteil, ksg.name as ksg_name from klarschiff_verlauf kverl "
      + "  left join klarschiff_vorgang kvorg on kverl.vorgang = kvorg.id "
      + "  left join klarschiff_stadtteil_grenze ksg on ST_Within(kvorg.ovi, ksg.grenze) "
      + "  inner join klarschiff_kategorie kk on kk.id = kvorg.kategorie "
      + "where "
      + "  kverl.id in ( "
      + "    select distinct on (vorgang) id from klarschiff_verlauf kverl_last where typ in ('status', 'erzeugt') and "
      + "      kverl_last.datum between '" + sdf.format(cVon.getTime()) + "' and '" + sdf.format(cBis.getTime()) + "' order by vorgang, datum desc "
      + "  ) and "
      + "  (kverl.typ = 'status' and kverl.wert_neu IN ('gelöst')) "
      + "  and kvorg.typ = '" + typ + "' and kvorg.status NOT IN ('duplikat', 'geloescht', 'nichtLoesbar') "
      + "group by kvorg.zustaendigkeit, kk.id, kk.parent, kk.name, ksg.id, ksg.name order by kvorg.zustaendigkeit, kk.name")
      .getResultList();
  }

  /*
   * Holt die Anzahl der 'erzeugten' Vorgänge eingeschränkt auf die übergebenen Kategorie-IDS
   * gruppiert nach Zuständigkeit, Hauptkategorie und Stadtteil
   */
  public List<Object[]> getAnzahlErzeugteVorgaengeInZeitraum(EnumVorgangTyp typ, Date von, Date bis) {
    Calendar cVon = Calendar.getInstance();
    cVon.setTime(von);

    Calendar cBis = Calendar.getInstance();
    cBis.setTime(bis);
    cBis.add(Calendar.DATE, 1);

    return entityManager.createNativeQuery("select count(kvorg.id), kvorg.zustaendigkeit, kk.id, kk.parent, kk.name as kk_name, ksg.id stadtteil, ksg.name as ksg_name from klarschiff_verlauf kverl "
      + "  inner join ( "
      + "	select vorgang from klarschiff_verlauf where typ in ('erzeugt') and datum between '" + sdf.format(cVon.getTime()) + "' and '" + sdf.format(cBis.getTime()) + "' "
      + "  ) kverl_erzeug on kverl.vorgang = kverl_erzeug.vorgang "
      + "  left join klarschiff_vorgang kvorg on kverl.vorgang = kvorg.id "
      + "  left join klarschiff_stadtteil_grenze ksg on ST_Within(kvorg.ovi, ksg.grenze) "
      + "  inner join klarschiff_kategorie kk on kk.id = kvorg.kategorie "
      + "where "
      + "  kverl.id in ( "
      + "    select distinct on (vorgang) id from klarschiff_verlauf kverl_last where typ in ('status', 'erzeugt') and "
      + "      kverl_last.datum between '" + sdf.format(cVon.getTime()) + "' and '" + sdf.format(cBis.getTime()) + "' order by vorgang, datum desc "
      + "  ) and "
      + "  (kverl.typ = 'erzeugt' or (kverl.typ = 'status' and kverl.wert_neu NOT IN ('gelöscht'))) "
      + "  and kvorg.typ = '" + typ + "' and kvorg.status NOT IN ('duplikat', 'geloescht', 'nichtLoesbar') "
      + "group by kvorg.zustaendigkeit, kk.id, kk.parent, kk.name, ksg.id, ksg.name order by kvorg.zustaendigkeit, kk.name")
      .getResultList();
  }

  /*
   * Holt die Anzahl der 'neuen' Vorgänge eingeschränkt auf die übergebenen Hauptkategorie-IDS
   * im Zeitraum gruppiert nach Zuständigkeit, Hauptkategorie und Stadtteil
   */
  public List<Object[]> getAnzahlNeueVorgaengeInZeitraum(EnumVorgangTyp typ, Date von, Date bis) {
    Calendar cVon = Calendar.getInstance();
    cVon.setTime(von);

    Calendar cBis = Calendar.getInstance();
    cBis.setTime(bis);
    cBis.add(Calendar.DATE, 1);

    return entityManager.createNativeQuery("select count(kvorg.id), kvorg.zustaendigkeit, kk.id, kk.parent, kk.name as kk_name, ksg.id stadtteil, ksg.name as ksg_name from klarschiff_verlauf kverl "
      + "  inner join ( "
      + "	select vorgang from klarschiff_verlauf where typ in ('erzeugt') and datum between '" + sdf.format(cVon.getTime()) + "' and '" + sdf.format(cBis.getTime()) + "' "
      + "  ) kverl_erzeug on kverl.vorgang = kverl_erzeug.vorgang "
      + "  left join klarschiff_vorgang kvorg on kverl.vorgang = kvorg.id "
      + "  left join klarschiff_stadtteil_grenze ksg on ST_Within(kvorg.ovi, ksg.grenze) "
      + "  inner join klarschiff_kategorie kk on kk.id = kvorg.kategorie "
      + "where "
      + "  kverl.id in ( "
      + "    select distinct on (vorgang) id from klarschiff_verlauf kverl_last where typ in ('status', 'erzeugt') and "
      + "      kverl_last.datum between '" + sdf.format(cVon.getTime()) + "' and '" + sdf.format(cBis.getTime()) + "' order by vorgang, datum desc "
      + "  ) and "
      + "  (kverl.typ = 'erzeugt' or (kverl.typ = 'status' and kverl.wert_neu NOT IN ('Duplikat', 'wird nicht bearbeitet', 'gelöscht'))) "
      + "  and kvorg.typ = '" + typ + "' and kvorg.status NOT IN ('duplikat', 'geloescht', 'nichtLoesbar') "
      + "group by kvorg.zustaendigkeit, kk.id, kk.parent, kk.name, ksg.id, ksg.name order by kvorg.zustaendigkeit, kk.name")
      .getResultList();
  }

  /*
   * Holt die Anzahl der 'offenen' Vorgänge eingeschränkt auf die übergebenen Kategorie-IDS
   * gruppiert nach Zuständigkeit, Kategorie und Stadtteil
   */
  public List<Object[]> getAnzahlOffeneVorgaengeBis(EnumVorgangTyp typ, Date date) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    // +1 Tag
    c.add(Calendar.DATE, 1);

    Query q = entityManager.createNativeQuery("select count(kvorg.id), kvorg.zustaendigkeit, kk.id, kk.parent, kk.name as kk_name, ksg.id stadtteil, ksg.name as ksg_name from klarschiff_verlauf kverl "
      + "  left join klarschiff_vorgang kvorg on kverl.vorgang = kvorg.id "
      + "  left join klarschiff_stadtteil_grenze ksg on ST_Within(kvorg.ovi, ksg.grenze) "
      + "  inner join klarschiff_kategorie kk on kk.id = kvorg.kategorie "
      + "where "
      + "  kverl.id in ( "
      + "    select distinct on (vorgang) id from klarschiff_verlauf kverl_last where typ in ('status', 'erzeugt') and "
      + "      kverl_last.datum < '" + sdf.format(c.getTime()) + "' order by vorgang, datum desc "
      + "  ) and "
      + "  (kverl.typ = 'erzeugt' or (kverl.typ = 'status' and kverl.wert_neu NOT IN ('gelöst', 'Duplikat', 'wird nicht bearbeitet', 'gelöscht'))) "
      + "  and kvorg.typ = '" + typ + "' and kvorg.status NOT IN ('duplikat', 'geloescht', 'nichtLoesbar') "
      + "group by kvorg.zustaendigkeit, kk.id, kk.parent, kk.name, ksg.id, ksg.name order by kvorg.zustaendigkeit, kk.name");
    return q.getResultList();
  }
}
