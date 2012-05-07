package de.fraunhofer.igd.klarschiff.context;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Repository;

/**
 * Die Klasse wird verwendet um einen statischen Zugriff auf den ApplicationContext und
 * den EntityManger zu bekommen. Die Klasse AppContext wird mit Hilfe dieser Klasse 
 * initialisiert.
 * @author Stefan Audersch (Fraunhofer IGD)
 * @see de.fraunhofer.igd.klarschiff.context.AppContext
 */
@Repository
public class ApplicationContextProvider implements ApplicationContextAware {

	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		AppContext.setApplicationContext(ctx);
	}

	@PersistenceContext
	public void setEntityManager(EntityManager entityManager) throws BeansException {
		AppContext.setEntityManager(entityManager);
	}
}
