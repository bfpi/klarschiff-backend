package de.fraunhofer.igd.klarschiff.context;

import javax.persistence.EntityManager;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

/**
 * Die Klasse wird verwendet um einen statischen Zugriff auf den ApplicationContext und den
 * EntityManager zu bekommen.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 * @see de.fraunhofer.igd.klarschiff.context.ApplicationContextProvider
 */
public class AppContext {

  private final static Logger logger = Logger.getLogger(AppContext.class);

  private static ApplicationContext ctx;
  private static EntityManager em;

  /**
   * @param applicationContext ApplicationContext
   */
  public static void setApplicationContext(ApplicationContext applicationContext) {
    ctx = applicationContext;
  }

  /**
   * @param entityManager EntityManager
   */
  public static void setEntityManager(EntityManager entityManager) {
    em = entityManager;
  }

  /**
   * @return ApplicationContext
   */
  public static ApplicationContext getApplicationContext() {
    return ctx;
  }

  /**
   * @return EntityManager
   */
  public static EntityManager getEntityManager() {
    logger.debug("getEntityManager()");
    return em;
  }
}
