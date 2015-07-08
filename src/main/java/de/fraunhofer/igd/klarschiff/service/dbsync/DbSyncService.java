package de.fraunhofer.igd.klarschiff.service.dbsync;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.util.ClassPathResourceUtil;
import de.fraunhofer.igd.klarschiff.util.LogUtil;
import de.fraunhofer.igd.klarschiff.util.SqlScriptUtil;

/**
 * Die Klasse stellt einen Service für die Synchronisation der Frontend- und BackendDB bereit. Für
 * die Synchronisation wird zur Laufzeit DbLink verwendet, wofür Trigger und Triggerfunktionen bei
 * der BackendDb eingerichtet werden. Im Initialisierungsscript für die Trigger und
 * Triggerfunktionen können die Parameter für die FrontendDb durch die Platzhalter
 * <code>%host%</code>, <code>%port%</code>, <code>%dbname%</code>, <code>%username%</code> und
 * <code>%password%</code> angegeben werden. Eine Synchronisation kann bei Bedarf über einen
 * Kommandozeilenaufruf erfolgen.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 * @author Hani Samara (Fraunhofer IGD)
 */
@Service
public class DbSyncService {

  public static final Logger logger = Logger.getLogger(DbSyncService.class);

  static final String FS = System.getProperty("file.separator");

  static final String F_HOST = "Frontend_Server";
  static final String F_PORT = "Frontend_Port";
  static final String F_SCHEMA = "Frontend_Schema";
  static final String F_DBNAME = "Frontend_Database";
  static final String F_USERNAME = "Frontend_Login";
  static final String F_PASSWORD = "Frontend_Password";
  static final String B_HOST = "Backend_Server";
  static final String B_PORT = "Backend_Port";
  static final String B_SCHEMA = "Backend_Schema";
  static final String B_DBNAME = "Backend_Database";
  static final String B_USERNAME = "Backend_Login";
  static final String B_PASSWORD = "Backend_Password";
  static final String LOG_DIR = "log_dir";

  @PersistenceContext(unitName = "persistenceUnit")
  private EntityManager entityManager;

  @Autowired
  SettingsService settingsService;

  String sqlScriptDbLinkFile;
  String sqlScriptFrontendDbFile;
  String frontendDbHost;
  String frontendDbPort;
  String frontendDbSchema;
  String frontendDbDbName;
  String frontendDbUsername;
  String frontendDbPassword;
  String backendDbHost;
  String backendDbPort;
  String backendDbSchema;
  String backendDbDbName;
  String backendDbUsername;
  String backendDbPassword;

  private Properties scriptValuesMap;

  @PostConstruct
  public void init() {
    scriptValuesMap = new Properties();
    scriptValuesMap.setProperty("f_host", getFrontendDbHost());
    scriptValuesMap.setProperty("f_port", getFrontendDbPort());
    scriptValuesMap.setProperty("f_schema", getFrontendDbSchema());
    scriptValuesMap.setProperty("f_dbname", getFrontendDbDbName());
    scriptValuesMap.setProperty("f_username", getFrontendDbUsername());
    scriptValuesMap.setProperty("f_password", getFrontendDbPassword());
    scriptValuesMap.setProperty("b_username", getBackendDbUsername());
  }

  public Logger getLogger() {
    return logger;
  }

  /**
   * Initialisiert die notwendigen Trigger und Triggerfunktionen für das DbLink anhand eines in der
   * Konfiguration vorgegebenen Scriptes. Die DB-Parameter für die FrontendDB können im Script durch
   * die Platzhalter <code>%host%</code>, <code>%port%</code>, <code>%dbname%</code>,
   * <code>%username%</code> und <code>%password%</code> angegeben werden.
   *
   * @param session Session zum Zugriff auf die DB
   * @param state Soll das Script ausgeführt werden bzw. was soll bei einem Fehler passieren
   */
  public void executeSqlScriptDbLink(Session session, SqlScriptUtil.State state) {
    if (state != SqlScriptUtil.State.disabled) {
      LogUtil.info("SQL-Script für DbLink wird ausgeführt ...");
    }
    SqlScriptUtil.executeSqlScript(session, getSqlScriptDbLink(), state);
  }

  /**
   * Initialisiert die notwendigen Trigger und Triggerfunktionen für das DbLink anhand eines in der
   * Konfiguration vorgegebenen Scriptes. Eine Session für die DB wird vom EntityManager geholt. Die
   * DB-Parameter für die FrontendDB können im Script durch die Platzhalter <code>%host%</code>,
   * <code>%port%</code>, <code>%dbname%</code>, <code>%username%</code> und <code>%password%</code>
   * angegeben werden.
   *
   * @param state Soll das Script ausgeführt werden bzw. was soll bei einem Fehler passieren
   */
  public void executeSqlScriptDbLink(SqlScriptUtil.State state) throws HibernateException {
    SessionFactory sessionFactory;
    Session session = null;

    try {
      sessionFactory = ((Session) entityManager.getDelegate()).getSessionFactory();
      session = sessionFactory.openSession();
      session.setFlushMode(FlushMode.COMMIT);

      executeSqlScriptDbLink(session, state);

    } catch (HibernateException e) {
      switch (state) {
        case error:
          throw e;
        case warn:
          logger.error(e);
      }
    } finally {
      try {
        session.close();
      } catch (Exception ex) {
      }
    }

  }

  /**
   * Liest das SQL-Script mit den Trigger und Triggerfunktionen zum Synchronisieren der Frontend-
   * und BackendDB, wobei die Platzhalter <code>%host%</code>, <code>%port%</code>,
   * <code>%dbname%</code>, <code>%username%</code> und <code>%password%</code> entsprechend durch
   * Werte ersetzt werden.
   *
   * @return SQL-Script mit den Trigger und Triggerfunktionen zum Synchronisieren der Frontend- und
   * BackendDB
   */
  public String getSqlScriptDbLink() {
    String script;
    try {
      script = ClassPathResourceUtil.readFile(getSqlScriptDbLinkFile());
      script = StrSubstitutor.replace(script, scriptValuesMap);
      return script;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void executeSqlScriptFrontendDb(SqlScriptUtil.State state) throws HibernateException {
    if (state == SqlScriptUtil.State.disabled) {
      return;
    }
    LogUtil.info("SQL-Script zum Erzeugen der FrontendDb wird ausgeführt ...");
    SessionFactory sessionFactory = null;
    Session session = null;
    try {
      sessionFactory = new org.hibernate.cfg.Configuration()
        .setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
        .setProperty("hibernate.connection.url", "jdbc:postgresql://" + frontendDbHost + ":" + frontendDbPort + "/" + frontendDbDbName)
        .setProperty("hibernate.connection.username", frontendDbUsername)
        .setProperty("hibernate.connection.password", frontendDbPassword)
        .setProperty("hibernate.dialect", "org.hibernatespatial.postgis.PostgisDialect")
        .setProperty("hibernate.hbm2ddl.auto", "update")
        //.setProperty("hibernate.show_sql", "true")
        //.setProperty("hibernate.format_sql", "true")
        .buildSessionFactory();
      session = sessionFactory.openSession();
      SqlScriptUtil.executeSqlScript(session, getSqlScriptFrontendDb(), state);
    } catch (HibernateException e) {
      switch (state) {
        case error:
          throw e;
        case warn:
          logger.error(e);
      }
    } finally {
      try {
        session.close();
      } catch (Exception ex) {
      }
      try {
        sessionFactory.close();
      } catch (Exception ex) {
      }
    }
  }

  public String getSqlScriptFrontendDb() {
    String script;
    try {
      script = ClassPathResourceUtil.readFile(getSqlScriptFrontendDbFile());
      script = StrSubstitutor.replace(script, scriptValuesMap);
      return script;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String getFrontendDbSchema() {
    return frontendDbSchema;
  }

  public void setFrontendDbSchema(String frontendDbSchema) {
    this.frontendDbSchema = frontendDbSchema;
  }

  public String getBackendDbHost() {
    return backendDbHost;
  }

  public void setBackendDbHost(String backendDbHost) {
    this.backendDbHost = backendDbHost;
  }

  public String getBackendDbPort() {
    return backendDbPort;
  }

  public void setBackendDbPort(String backendDbPort) {
    this.backendDbPort = backendDbPort;
  }

  public String getBackendDbSchema() {
    return backendDbSchema;
  }

  public void setBackendDbSchema(String backendDbSchema) {
    this.backendDbSchema = backendDbSchema;
  }

  public String getBackendDbDbName() {
    return backendDbDbName;
  }

  public void setBackendDbDbName(String backendDbDbName) {
    this.backendDbDbName = backendDbDbName;
  }

  public String getBackendDbUsername() {
    return backendDbUsername;
  }

  public void setBackendDbUsername(String backendDbUsername) {
    this.backendDbUsername = backendDbUsername;
  }

  public String getBackendDbPassword() {
    return backendDbPassword;
  }

  public void setBackendDbPassword(String backendDbPassword) {
    this.backendDbPassword = backendDbPassword;
  }

  public void setFrontendDbHost(String frontendDbHost) {
    this.frontendDbHost = frontendDbHost;
  }

  public void setFrontendDbPort(String frontendDbPort) {
    this.frontendDbPort = frontendDbPort;
  }

  public void setFrontendDbDbName(String frontendDbDbName) {
    this.frontendDbDbName = frontendDbDbName;
  }

  public void setFrontendDbUsername(String frontendDbUsername) {
    this.frontendDbUsername = frontendDbUsername;
  }

  public void setFrontendDbPassword(String frontendDbPassword) {
    this.frontendDbPassword = frontendDbPassword;
  }

  public void setSqlScriptDbLinkFile(String sqlScriptDbLinkFile) {
    this.sqlScriptDbLinkFile = sqlScriptDbLinkFile;
  }

  public String getSqlScriptDbLinkFile() {
    return sqlScriptDbLinkFile;
  }

  public String getFrontendDbHost() {
    return frontendDbHost;
  }

  public String getFrontendDbPort() {
    return frontendDbPort;
  }

  public String getFrontendDbDbName() {
    return frontendDbDbName;
  }

  public String getFrontendDbUsername() {
    return frontendDbUsername;
  }

  public String getFrontendDbPassword() {
    return frontendDbPassword;
  }

  public String getSqlScriptFrontendDbFile() {
    return sqlScriptFrontendDbFile;
  }

  public void setSqlScriptFrontendDbFile(String sqlScriptFrontendDbFile) {
    this.sqlScriptFrontendDbFile = sqlScriptFrontendDbFile;
  }
}
