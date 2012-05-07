package de.fraunhofer.igd.klarschiff.service.init;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import de.fraunhofer.igd.klarschiff.service.dbsync.DbSyncService;
import de.fraunhofer.igd.klarschiff.util.SqlScriptUtil;


/**
 * Klasse zum Initialisieren der DB mit Werten die in der Konfiguration vergegeben werden. Die eigentliche 
 * Initialisierung erfolgt innerhalb eines Thread.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class InitializeService {

	private final static Logger logger = Logger.getLogger(InitializeService.class);

	@PersistenceContext(unitName="persistenceUnit")
	private EntityManager entityManager;
	
	@Autowired
	DbSyncService dbSyncService;
	
	/**
	 * Aktionen für das Ausführen eines SQL-Scriptes (z.B. Initialisierung des DbLink mit Trigger und Triggerfunktionen
	 * für die Synchronisystion der Frontend- und BackendDB)
	 *
	 * <code>disabled</code> - SQL-Script wird nicht ausgeführt <br/>
	 * <code>warn</code> - SQL-Script wird ausgeführt, Fehler werden ggf. geloggt und die Ausführung des Servers wird bei einem ggf. aufgetretenen Fehler fortgeführt <br/>
	 * <code>error</code> - SQL-Script wird ausgeführt, bei einem Fehler wird die Asuführung des Servers abgeberochen
	 */
	
	/**
	 * Legt fest, ob die Initalisierung beim Start des Servers ausgeführt werden soll.
	 */
	boolean enable = true;

	/**
	 * Legt fest, wie die Ausführung des SQL-Scriptes zur Initialisierung des DbLink mit Trigger und Triggerfunktionen
	 * für die Synchronisystion der Frontend- und BackendDB ausgeführt werden soll.
	 */
	SqlScriptUtil.State executeSqlScriptDbLink = SqlScriptUtil.State.disabled;
	SqlScriptUtil.State executeSqlScriptFrontendDb = SqlScriptUtil.State.disabled;
	
	Long startDelay = new Long(1000);

	/**
	 * Liste mit den in der DB zu initalisierenden Objekten
	 */
	List<Object> initObjectList = new ArrayList<Object>();
	
	
	/**
	 * Start des Thread zum Ausführen der Initialisierung der DB.
	 * @throws Exception
	 */
    @PostConstruct
    public void afterPropertiesSet() throws Exception
    {
    	if (enable)	new InitializeServiceThread(this);
    }

	/* --------------- GET + SET ----------------------------*/

    public boolean getEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public Long getStartDelay() {
		return startDelay;
	}

	public void setStartDelay(Long startDelay) {
		this.startDelay = startDelay;
	}

	public static Logger getLogger() {
		return logger;
	}

	public List<Object> getInitObjectList() {
		return initObjectList;
	}

	public void setInitObjectList(List<Object> initObjectList) {
		this.initObjectList = initObjectList;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	public SqlScriptUtil.State getExecuteSqlScriptDbLink() {
		return executeSqlScriptDbLink;
	}

	public void setExecuteSqlScriptDbLink(SqlScriptUtil.State executeSqlScriptDbLink) {
		this.executeSqlScriptDbLink = executeSqlScriptDbLink;
	}

	public DbSyncService getDbSyncService() {
		return dbSyncService;
	}

	public SqlScriptUtil.State getExecuteSqlScriptFrontendDb() {
		return executeSqlScriptFrontendDb;
	}

	public void setExecuteSqlScriptFrontendDb(
			SqlScriptUtil.State executeSqlScriptFrontendDb) {
		this.executeSqlScriptFrontendDb = executeSqlScriptFrontendDb;
	}
}


