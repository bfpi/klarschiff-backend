package de.fraunhofer.igd.klarschiff.service.init;

import java.io.File;
import java.util.List;
import java.util.UUID;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Example;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.dao.DataAccessException;
import de.fraunhofer.igd.klarschiff.service.cluster.ClusterUtil;
import de.fraunhofer.igd.klarschiff.util.LogUtil;
import de.fraunhofer.igd.klarschiff.vo.EnumText;
import de.fraunhofer.igd.klarschiff.vo.extra.EnumFreigabeStatus;
import de.fraunhofer.igd.klarschiff.vo.extra.EnumPrioritaet;
import de.fraunhofer.igd.klarschiff.vo.extra.EnumVerlaufTyp;
import de.fraunhofer.igd.klarschiff.vo.extra.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.vo.extra.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.extra.EnumZustaendigkeitStatus;
import org.hibernate.internal.SessionImpl;

/**
 * Thread in dem die Initialisierung vorgenommen wird.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class InitializeServiceThread extends Thread {

  Logger logger;

  SessionFactory sessionFactory;
  Session session;
  Transaction transaction;

  InitializeService initializeService;

  /**
   * Konstruktor zum erzeugen des Thread. Der Thread wird danach sofort gestartet.
   *
   * @param initializeService
   */
  @SuppressWarnings("static-access")
  public InitializeServiceThread(InitializeService initializeService) {
    setName(InitializeServiceThread.class.getSimpleName());
    this.initializeService = initializeService;
    logger = initializeService.getLogger();
    start();
  }

  /**
   * Methode zum Ausführen des Thread. Hierbei werden die folgenden Schritte vorgenommen:<br>
   * - Arbeitsverzeichnis für ApacheDS wird gesetzt und Inhalt des Verzeichnis wird ggf.
   * gelöscht.<br>
   * - Der Thread wartet eine definierte Zeit, bis andere Komponenten geladen wurden.<br>
   * - Die Werte für die Enums werden, wenn sie noch nicht vorhanden sind, in der DB
   * gespeichert.<br>
   * - Es wird mit der Anfrage <code>SELECT COUNT(*) FROM Kategorie</code> geprüft, ob bereits Werte
   * in der DB gespeichert sind.<br>
   * - Die Objekte aus der <code>initObjectList</code> werden in der DB gespeichert.<br>
   * - Das SQL-Script mit den Trigger und Triggerfunktionen zur Synchronisation der Frontend- und
   * BackendDB wird ausgeführt.
   */
  @SuppressWarnings({"static-access"})
  @Override
  public void run() {
    setApacheDSWorkDir();
    try {
      sleep(initializeService.getStartDelay());
    } catch (Exception e) {
    }

    initializeService.getLogger().debug("InitializeService started");

    if (initializeService.getEntityManager().createQuery("select count(o) from Kategorie o", Long.class).getSingleResult() > 0) {
      initializeService.getLogger().debug("init DB skiped");
    } else {
      sessionFactory = ((Session) initializeService.getEntityManager().getDelegate()).getSessionFactory();
      session = sessionFactory.openSession();
      session.setFlushMode(FlushMode.COMMIT);

      try {
        transaction = session.beginTransaction();
        //Tabellen mit initialen Daten füllen
        LogUtil.info("Initiale Daten werden überprüft und ggf. in die DB geschrieben ...");
        _initializeEnum();
        _initialize(initializeService.getInitObjectList());
        transaction.commit();
      } catch (Exception e) {
        try {
          transaction.rollback();
        } catch (Exception ex) {
        }
        initializeService.getLogger().error(e);
      }

      try {
        session.close();
      } catch (Exception ex) {
      }
      initializeService.getLogger().debug("InitializeService stopped");
    }
  }

  /**
   * Löscht das aktuell gesetzte Arbeitverzeichnis für ApacheDS
   */
  private void removeApacheDSWorkDir() {
    try {
      String apacheWorkDir = System.getProperty("apacheDSWorkDir");

      if (apacheWorkDir == null) {
        apacheWorkDir = System.getProperty("java.io.tmpdir") + File.separator + "apacheds-spring-security";
      }
      FileUtils.deleteQuietly(new File(apacheWorkDir));
    } catch (Exception e) {
    }
  }

  /**
   * Setzt ein eindeutiges Arbeitverzeichnis für ApacheDS für diese Serverinstanze und löscht das
   * Verzeichnis ggf.
   */
  private void setApacheDSWorkDir() {
    try {
      String id = ClusterUtil.getServerConnectorPort();
      if (id == null) {
        id = UUID.randomUUID().toString();
      }
      System.setProperty("apacheDSWorkDir", System.getProperty("java.io.tmpdir") + File.separator + "apacheds-spring-security " + id);

      removeApacheDSWorkDir();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Speichert die Objekte aus der Liste in der Datenbank. Dabei wird zuvor geprüft, ob die Objekte
   * bereits in der DB enthalten sind und ggf. geändert werden müssen.
   *
   * @param list Liste der zu speichernden Objekte
   */
  @SuppressWarnings("rawtypes")
  private void _initialize(List list) {
    for (Object o : list) {
      try {
        if (o instanceof List) {
          _initialize((List) o);
        } else {
          List l = findByExample(o);
          if (l.size() == 1) {
            //Mergen
            Object entity = l.get(0);
            Class clazz = Hibernate.getClass(entity);
            ClassMetadata metadata = sessionFactory.getClassMetadata(clazz);
            //Object id = metadata.getIdentifier(entity, EntityMode.POJO);
            Object id = metadata.getIdentifier(entity, (SessionImpl) session);
            String idPropertyName = metadata.getIdentifierPropertyName();
            BeanUtils.setProperty(o, idPropertyName, id);
            session.refresh(session.merge(o));
            logger.debug("Merge Object [" + o + "]");
          } else if (l.size() > 1) {
            logger.warn("find duplicate object in db for  [" + o + "]");
          } else {
            //neu speichern
            session.saveOrUpdate(o);
            logger.debug("Save Object [" + o + "]");
          }
        }
      } catch (Exception e) {
        logger.warn("failed to save Object [" + o + "]", e);
      }
    }

  }

  /**
   * Ermittelt ein in der DB vorhandenes Objekt, das zu dem angegebenen Objekt
   * (<code>exampleEntity</code>) passt.
   *
   * @param exampleEntity Objekt, nach dem in der DB gesucht werden soll.
   * @return gefundenes Objekt aus der DB oder <code>null</code> wenn kein passendes Objekt in der
   * DB gefunden werden kann.
   * @throws DataAccessException
   */
  @SuppressWarnings("rawtypes")
  private List findByExample(final Object exampleEntity) throws DataAccessException {

    Criteria executableCriteria = session.createCriteria(exampleEntity.getClass());
    executableCriteria.add(Example.create(exampleEntity));
    return executableCriteria.list();
  }

  /**
   * Speichert die Werte der Enums in der DB.
   */
  private void _initializeEnum() {
    for (EnumText _enum : de.fraunhofer.igd.klarschiff.vo.EnumFreigabeStatus.values()) {
      session.saveOrUpdate(new EnumFreigabeStatus().fill(_enum));
    }
    for (EnumText _enum : de.fraunhofer.igd.klarschiff.vo.EnumPrioritaet.values()) {
      session.saveOrUpdate(new EnumPrioritaet().fill(_enum));
    }
    for (EnumText _enum : de.fraunhofer.igd.klarschiff.vo.EnumVerlaufTyp.values()) {
      session.saveOrUpdate(new EnumVerlaufTyp().fill(_enum));
    }
    for (EnumText _enum : de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus.values()) {
      session.saveOrUpdate(new EnumVorgangStatus().fill(_enum));
    }
    for (EnumText _enum : de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp.values()) {
      session.saveOrUpdate(new EnumVorgangTyp().fill(_enum));
    }
    for (EnumText _enum : de.fraunhofer.igd.klarschiff.vo.EnumZustaendigkeitStatus.values()) {
      session.saveOrUpdate(new EnumZustaendigkeitStatus().fill(_enum));
    }
  }
}
