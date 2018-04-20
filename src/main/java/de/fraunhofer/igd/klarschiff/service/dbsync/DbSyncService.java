package de.fraunhofer.igd.klarschiff.service.dbsync;

import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;

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
    scriptValuesMap.setProperty("b_username", getBackendDbUsername());
  }

  public Logger getLogger() {
    return logger;
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
}
