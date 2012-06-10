package de.fraunhofer.igd.klarschiff.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;

/**
 * Klasse zum Ausführen eines SQL-Scriptes in der DB. Diese Klasse ist speziell für die Ausführung des Scriptes zum
 * Initialisieren der Trigger und Triggerfunktionen in der BackendDB gedacht.
 * @author Stefan Audersch (Fraunhofer IGD)
 * @author Hani Samara (Fraunhofer IGD)
 */
public class SqlScriptUtil implements Work {

	public static final Logger logger = Logger.getLogger(SqlScriptUtil.class);
	public enum State { disabled, warn, error };
	
	private String sqlScript;
	private State state;
	
	
	/**
	 * Führt ein SQL-Script aus.
	 * @param session Session auf der eine Transaktion zum Ausführen geöffnet werden soll
	 * @param sqlScript SQL-Script
	 * @param state Soll das Script ausgeführt werden bzw. was soll bei einem Fehler passieren
	 */
	public static void executeSqlScript(Session session, String sqlScript, State state) throws HibernateException {
		Transaction transaction = null;

		if (state==State.disabled) return;
		try {
			transaction = session.beginTransaction();			
			session.doWork(new SqlScriptUtil(sqlScript, state));
  			transaction.commit();
		} catch (HibernateException e) {
			try { transaction.rollback(); }catch (Exception ex) {}
			switch(state) {
				case error:
					throw e;
				case warn:
					logger.error(e);
			}
		}
	}

	
	/**
	 * Initialisierung
	 * @param sqlScript Script welches ausgeführt werden soll
	 */
	private SqlScriptUtil(String sqlScript, State state) {
		this.sqlScript = sqlScript;
		this.state = state;
	}
	
	
	/**
	 * Ausführen des Scriptes.
	 */
	@Override
	public void execute(Connection connection) throws SQLException {
		try {
			logger.debug("SqlScript wird ausgeführt.");
			PreparedStatement statement = connection.prepareStatement(sqlScript);
			statement.execute();
		} catch (Exception e) {
			switch(state) {
				case error:
					throw new SQLException(e);
				case warn:
					logger.error("Fehler bei der Ausführung des sqlScriptes:", e);
			}
		}
	}
}