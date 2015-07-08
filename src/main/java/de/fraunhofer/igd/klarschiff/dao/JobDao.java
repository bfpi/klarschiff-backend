package de.fraunhofer.igd.klarschiff.dao;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.fraunhofer.igd.klarschiff.vo.JobRun;
import de.fraunhofer.igd.klarschiff.vo.JobRun.Ergebnis;

/**
 * Die DAO-Klasse unterstützt bei der Synchronisation von Jobs in einem Cluster. Hierzu können die
 * laufenden Jobs in der DB registriert werden.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Repository
public class JobDao {

  private static final Logger logger = Logger.getLogger(JobDao.class);

  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

  @PersistenceContext
  EntityManager em;

  /**
   * Registriert einen Job in der DB. Die Id des Jobs wird dabei für die Synchronisation der Jobs
   * verwendet. Die Id setzt sich aus einem abgerundetem Datum und dem Namen des Jobs zusammen
   *
   * @param datum Zeit bei der der Job gestartet wurde
   * @param name Name des Jobs
   * @param serverIp IPs des aktuellen Servers
   * @param serverName Rechnername des aktuellen Servers
   * @param serverConnectorPort ConnectionPort auf dem der aktuellen Server läuft
   * @param truncateField Feld bei der Zeit, auf das abgerundet werden soll
   * @return Id des Job oder <code>null</code>, wenn der Job bereits von einer anderen
   * Serverinstance ausgeführt wird.
   */
  @Transactional
  public String registerJobRun(Date datum, String name, String serverIp, String serverName, String serverConnectorPort, int truncateField) {
    try {
      String id = dateFormat.format(DateUtils.truncate(datum, truncateField)) + " " + name;

      EntityManager entityManager = em.getEntityManagerFactory().createEntityManager();
      entityManager.getTransaction().begin();
      NDC.push("log_DENY");
      try {
        entityManager.createNativeQuery("INSERT INTO klarschiff_job_run (id, ergebnis) VALUES (:id, :ergebnis)")
          .setParameter("id", id)
          .setParameter("ergebnis", Ergebnis.gestartet.name())
          .executeUpdate();
        entityManager.getTransaction().commit();
        entityManager.close();
      } catch (Exception e) {
        entityManager.getTransaction().rollback();
        entityManager.close();
        throw e;
      } finally {
        NDC.remove();
        //org.hibernate.util.JDBCExceptionReporter x;
      }

      JobRun jobRun = em.find(JobRun.class, id);
      jobRun.setName(name);
      jobRun.setDatum(datum);
      jobRun.setServerIp(serverIp);
      jobRun.setServerName(serverName);
      jobRun.setServerPort(serverConnectorPort);
      em.merge(jobRun);
      return id;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Aktualisiert das Ergebnis des Jobs in der Datenbank
   *
   * @param id Id des Jobs
   * @param ergebnis Ergebnis des Jobs (<code>abgeschlossen</code>, <code>fehlerhaft</code>)
   * @param exception
   */
  @Transactional
  public void updateJobRun(String id, Ergebnis ergebnis, Throwable exception) {
    JobRun jobRun = em.find(JobRun.class, id);
    jobRun.setErgebnis(ergebnis);
    if (exception != null) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      PrintWriter pw = new PrintWriter(bos, true);
      exception.printStackTrace(pw);

      while (exception != null) {
        //SQLException NextException
        if (exception instanceof SQLException && ((SQLException) exception).getNextException() != null) {
          pw.println("\n---------- SQLException NextException ------------\n");
          ((SQLException) exception).getNextException().printStackTrace(pw);
        }

        exception = exception.getCause();
      }
      jobRun.setFehlermeldung(new String(bos.toByteArray()));
    }
    em.merge(jobRun);
  }

  /**
   * Ermittelt die Anzahl der korrekt abgeschlossen Jobs, die im Cluster ausgeführt wurden.
   *
   * @return Anzahl der korrekt ausgeführten Jobs
   */
  public Long getAnzahlAbgeschlosseneJobs() {
    return (Long) em.createQuery("SELECT COUNT(*) FROM JobRun j WHERE j.ergebnis=:ergebnis").setParameter("ergebnis", JobRun.Ergebnis.abgeschlossen).getSingleResult();
  }

  /**
   * Ermittelt die fehlerhaft ausgeführten Jobs, die im Cluster ausgeführt wurden.
   *
   * @return Liste der fehlerhaft ausgeführten Jobs, die im Cluster ausgeführt wurden.
   */
  @SuppressWarnings("unchecked")
  public List<JobRun> getFehlerhafteJobs() {
    return em.createQuery("SELECT j FROM JobRun j WHERE j.ergebnis=:ergebnis ORDER BY j.datum DESC").setParameter("ergebnis", JobRun.Ergebnis.fehlerhaft).getResultList();
  }
}
