package de.fraunhofer.igd.klarschiff.dao;

import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Die Dao-Klasse erlaubt das Ermitteln von Daten aus der DB für die Statistik.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Repository
public class StatisticDao {

  @PersistenceContext
  EntityManager entityManager;

  @Autowired
  VorgangDao vorgangDao;

  @Autowired
  SecurityService securityService;

  @Autowired
  SettingsService settingsService;

  /**
   * Gibt eine Liste mit offenen Vorgängen zurück, zu denen mindestens eine Missbrauchsmeldung
   * vorhanden ist.
   *
   * @return Liste der Vorgänge
   */
  @SuppressWarnings("unchecked")
  public List<Vorgang> findVorgaengeMissbrauchsmeldungen() {
    HqlQueryHelper query = new HqlQueryHelper(securityService)
      .addFromTables("Vorgang vo JOIN vo.missbrauchsmeldungen mi WITH mi.datumBestaetigung IS NOT NULL AND mi.datumAbarbeitung IS NULL")
      .addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
      .orderBy("vo.id");
    vorgangDao.addGroupByVorgang(query, true);
    processZustaendigkeitDelegiertAn(query);
    return query.getResultList(entityManager);
  }

  /**
   * Gibt eine Liste mit offenen Vorgängen zurück, deren Zuständigkeit noch nicht akzeptiert wurde.
   *
   * @param datum Datum der letzten Bearbeitung
   * @return Liste der Vorgänge
   */
  @SuppressWarnings("unchecked")
  public List<Vorgang> findVorgaengeOffenNichtAkzeptiert(Date datum) {
    HqlQueryHelper query = (new HqlQueryHelper(securityService)).addSelectAttribute("vo")
      .addFromTables("Vorgang vo")
      .addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
      .addWhereConditions("vo.status = 'offen'")
      .addWhereConditions("vo.zustaendigkeitStatus != 'akzeptiert'")
      .addWhereConditions("vo.version <= :datum").addParameter("datum", datum)
      .orderBy("vo.id");
    processZustaendigkeitDelegiertAn(query);
    return query.getResultList(entityManager);
  }

  /**
   * Gibt eine Liste mit Vorgängen im Status 'in Bearbeitung' zurück, die keine öffentliche
   * Statusinformation haben.
   *
   * @param datum Datum der letzten Bearbeitung
   * @return Liste der Vorgänge
   */
  @SuppressWarnings("unchecked")
  public List<Vorgang> findVorgaengeInbearbeitungOhneStatusKommentar(Date datum) {
    HqlQueryHelper query = (new HqlQueryHelper(securityService)).addSelectAttribute("vo")
      .addFromTables("Vorgang vo")
      .addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
      .addWhereConditions("vo.status = 'inBearbeitung'")
      .addWhereConditions("(vo.statusKommentar IS NULL OR vo.statusKommentar = '')")
      .addWhereConditions("vo.version <= :datum").addParameter("datum", datum)
      .orderBy("vo.id");
    processZustaendigkeitDelegiertAn(query);
    return query.getResultList(entityManager);
  }

  /**
   * Gibt eine Liste mit offenen Vorgängen zurück, bei denen die anzahl der notwendigen Unterstützer
   * noch nicht erreicht wurde.
   *
   * @param datum Datum der letzten Bearbeitung
   * @return Liste der Vorgänge
   */
  @SuppressWarnings("unchecked")
  public List<Vorgang> findVorgaengeIdeeOffenOhneUnterstuetzung(Date datum) {
    HqlQueryHelper query = (new HqlQueryHelper(securityService)).addSelectAttribute("vo")
      .addFromTables("Vorgang vo JOIN vo.verlauf ve")
      .addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
      .addWhereConditions("vo.typ = 'idee'")
      .addWhereConditions("vo.status = 'offen'")
      .addWhereConditions("vo.erstsichtungErfolgt = TRUE")
      .addWhereConditions("ve.typ = 'zustaendigkeitAkzeptiert'")
      .addWhereConditions("ve.datum <= :datum").addParameter("datum", datum)
      .addWhereConditions("(SELECT COUNT(*) FROM Unterstuetzer un WHERE un.vorgang = vo.id) < :unterstuetzer").addParameter("unterstuetzer", settingsService.getVorgangIdeeUnterstuetzer())
      .orderBy("vo.id");
    processZustaendigkeitDelegiertAn(query);
    return query.getResultList(entityManager);
  }

  /**
   * Gibt eine Liste mit nicht Lösbaren Vorgängen zurück, die keine öffentliche Statusinformation
   * haben.
   *
   * @return Liste der Vorgänge
   */
  @SuppressWarnings("unchecked")
  public List<Vorgang> findVorgaengeNichtLoesbarOhneStatuskommentar() {
    HqlQueryHelper query = (new HqlQueryHelper(securityService)).addSelectAttribute("vo")
      .addFromTables("Vorgang vo")
      .addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
      .addWhereConditions("vo.status = 'nichtLoesbar'")
      .addWhereConditions("(vo.statusKommentar IS NULL OR vo.statusKommentar = '')")
      .orderBy("vo.id");
    processZustaendigkeitDelegiertAn(query);
    return query.getResultList(entityManager);
  }

  /**
   * Gibt eine Liste mit Vorgängen im Status 'in Bearbeitung' zurück, deren Zuständigkeit aber noch
   * nicht akzeptiert wurde.
   *
   * @return Liste der Vorgänge
   */
  @SuppressWarnings("unchecked")
  public List<Vorgang> findVorgaengeNichtMehrOffenNichtAkzeptiert() {
    HqlQueryHelper query = (new HqlQueryHelper(securityService)).addSelectAttribute("vo")
      .addFromTables("Vorgang vo")
      .addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
      .addWhereConditions("vo.status NOT IN ('gemeldet','offen')")
      .addWhereConditions("vo.zustaendigkeitStatus != 'akzeptiert'")
      .orderBy("vo.id");
    processZustaendigkeitDelegiertAn(query);
    return query.getResultList(entityManager);
  }

  /**
   * Gibt eine Liste mit Vorgängen zurück, deren Beschreibung und/oder Foto nicht für die
   * öffentlichkeit Freigegeben wurden.
   *
   * @return Liste der Vorgänge
   */
  @SuppressWarnings("unchecked")
  public List<Vorgang> findVorgaengeOhneRedaktionelleFreigaben() {
    HqlQueryHelper query = (new HqlQueryHelper(securityService)).addSelectAttribute("vo")
      .addFromTables("Vorgang vo")
      .addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
      .addWhereConditions("vo.status IN ('offen', 'inBearbeitung', 'nichtLoesbar', 'geloest')")
      .addWhereConditions("vo.erstsichtungErfolgt = TRUE")
      .addWhereConditions("((vo.beschreibung IS NOT NULL AND vo.beschreibung != '' AND (beschreibungFreigabeStatus IS NULL OR beschreibungFreigabeStatus = 'intern')) OR (vo.fotoThumb IS NOT NULL AND (fotoFreigabeStatus IS NULL OR fotoFreigabeStatus = 'intern')))")
      .orderBy("vo.id");
    processZustaendigkeitDelegiertAn(query);
    return query.getResultList(entityManager);
  }

  /**
   * Gibt eine Übersicht die Status-Verteilung von offenen Vorgängen zurück.
   *
   * @param onlyCurrentZustaendigkeitDelegiertAn Nur die eigene Zuständigkeit oder Vorgänge die an
   * den aktuellen Nutzer übergeben wurden berücksichtigen.
   * @return Liste der Vorgänge
   */
  @SuppressWarnings("unchecked")
  public List<Object[]> getStatusVerteilung(boolean onlyCurrentZustaendigkeitDelegiertAn) {
    HqlQueryHelper query = new HqlQueryHelper(securityService)
      .addFromTables("Vorgang vo")
      .addSelectAttribute("vo.status")
      .addSelectAttribute("COUNT(vo.id)")
      .addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
      .addGroupByAttribute("vo.status")
      .addGroupByAttribute("vo.statusOrdinal")
      .orderBy("vo.statusOrdinal");
    if (onlyCurrentZustaendigkeitDelegiertAn) {
      processZustaendigkeitDelegiertAn(query);
    }
    return query.getResultList(entityManager);
  }

  /**
   * Fügt an die Query die Bedingungen hinzu, dass nur die eigene Zuständigkeit oder Vorgänge die an
   * den aktuellen Nutzer übergeben wurden berücksichtigt werden.
   *
   * @param query Query an die die Bedingungen hinzugefügt werden sollen.
   */
  public void processZustaendigkeitDelegiertAn(HqlQueryHelper query) {
    List<Role> zustaendigkeiten = securityService.getCurrentZustaendigkeiten(true);
    List<Role> delegiertAn = securityService.getCurrentDelegiertAn();

    //Zuständigkeit & DelegiertAn
    if (!CollectionUtils.isEmpty(zustaendigkeiten) && !CollectionUtils.isEmpty(delegiertAn)) {
      query.addWhereConditions("(vo.zustaendigkeit IN (:zustaendigkeit) OR vo.delegiertAn IN (:delegiertAn))")
        .addParameter("zustaendigkeit", Role.toString(zustaendigkeiten))
        .addParameter("delegiertAn", Role.toString(delegiertAn));
    } else if (!CollectionUtils.isEmpty(zustaendigkeiten)) {
      query.addWhereConditions("vo.zustaendigkeit IN (:zustaendigkeit)").addParameter("zustaendigkeit", Role.toString(zustaendigkeiten));
    } else if (!CollectionUtils.isEmpty(delegiertAn)) {
      query.addWhereConditions("vo.delegiertAn IN (:delegiertAn)").addParameter("delegiertAn", Role.toString(delegiertAn));
    }
  }

  /**
   * Gibt eine Liste mit Vorgängen zurück, die als Letztes angelegt wurden.
   *
   * @param maxResult Maximale Anzahl der Vorgänge
   * @return Liste der Vorgänge
   */
  @SuppressWarnings("unchecked")
  public List<Vorgang> findNeuesteVorgaenge(int maxResult) {
    HqlQueryHelper query = (new HqlQueryHelper(securityService)).addSelectAttribute("vo")
      .addFromTables("Vorgang vo")
      .addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
      .addWhereConditions("vo.status IN ('offen', 'inBearbeitung', 'nichtLoesbar', 'geloest')")
      .orderBy("vo.prioritaetOrdinal DESC, vo.delegiertAn ASC, vo.zustaendigkeitStatus DESC, vo.erstsichtungErfolgt ASC, vo.id DESC");
    processZustaendigkeitDelegiertAn(query);
    query.maxResults(maxResult);
    return query.getResultList(entityManager);
  }

  /**
   * Gibt eine Liste mit Vorgängen des aktuellen Nutzers zurück, an denen Änderungen vorgenommen
   * wurden.
   *
   * @param maxResult Maximale Anzahl der Vorgänge
   * @return Liste der Vorgänge
   */
  @SuppressWarnings("unchecked")
  public List<Vorgang> findEigeneVorgaenge(int maxResult, Date datum) {
    HqlQueryHelper query = (new HqlQueryHelper(securityService)).addSelectAttribute("vo")
      .addFromTables("Verlauf ve JOIN ve.vorgang vo")
      .addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
      .addWhereConditions("ve.datum >= :datum").addParameter("datum", datum)
      .addWhereConditions("ve.typ IN (:typen)").addParameter("typen", Arrays.asList(EnumVerlaufTyp.relevantBeiLetztenAktivitaeten()))
      .addWhereConditions("vo.status IN ('offen', 'inBearbeitung', 'nichtLoesbar', 'geloest')")
      .orderBy("vo.prioritaetOrdinal DESC, vo.delegiertAn ASC, vo.zustaendigkeitStatus DESC, vo.erstsichtungErfolgt ASC, vo.id DESC");
    processZustaendigkeitDelegiertAn(query);
    query.distinctEnable = true;
    query.maxResults(maxResult);
    return query.getResultList(entityManager);
  }

  /**
   * Gibt eine Liste mit Vorgängen, die die aktuelle Rolle als erstes akzeptiert hatte, die aber
   * inzwischen an andere Rollen überwiesen wurden.
   *
   * @param maxResult Maximale Anzahl der Vorgänge
   * @return Liste der Vorgänge
   */
  @SuppressWarnings("unchecked")
  public List<Vorgang> findEhemaligeVorgaenge(int maxResult) {
    List<Role> zustaendigkeiten = securityService.getCurrentZustaendigkeiten(true);
    if (CollectionUtils.isEmpty(zustaendigkeiten)) {
      return new ArrayList<Vorgang>();
    }
    HqlQueryHelper query = (new HqlQueryHelper(securityService)).addSelectAttribute("vo")
      .addFromTables("Vorgang vo")
      .addWhereConditions("(vo.archiviert IS NULL OR vo.archiviert = FALSE)")
      .addWhereConditions("vo.initialeAkzeptierteZustaendigkeit IN (:zustaendigkeit)").addParameter("zustaendigkeit", Role.toString(zustaendigkeiten))
      .addWhereConditions("vo.zustaendigkeit != vo.initialeAkzeptierteZustaendigkeit")
      .orderBy("vo.zustaendigkeitStatus desc, vo.id asc");
    processZustaendigkeitDelegiertAn(query);
    query.maxResults(maxResult);
    return query.getResultList(entityManager);
  }
}
