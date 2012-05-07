package de.fraunhofer.igd.klarschiff.context;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

/**
 * Die Klasse wird verwendet um einen statischen Zugriff auf den ApplicationContext und
 * den EntityManager zu bekommen.
 * @author Stefan Audersch (Fraunhofer IGD)
 * @see de.fraunhofer.igd.klarschiff.context.ApplicationContextProvider
 */
public class AppContext {
	private final static Logger logger = Logger.getLogger(AppContext.class);

	private static ApplicationContext ctx;
	private static EntityManager em;

	public static void setApplicationContext(ApplicationContext applicationContext) {
		ctx = applicationContext;
	}

	public static void setEntityManager(EntityManager entityManager) {
		em = entityManager;
	}
	
	public static ApplicationContext getApplicationContext() {
		return ctx;
	}

	public static EntityManager getEntityManager() {
		logger.debug("getEntityManager()");
		return em;
	}
}
