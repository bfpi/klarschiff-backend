package de.fraunhofer.igd.klarschiff.service.classification;

import de.fraunhofer.igd.klarschiff.service.init.InitializeServiceThread;
import de.fraunhofer.igd.klarschiff.util.LogUtil;
import de.fraunhofer.igd.klarschiff.util.ThreadUtil;

/**
 * Der Thread dient zum Initialisieren des Klassifikators. Nach dem Start des Threads
 * wird zunächst eine gegebene Zeit gewartet.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class ClassficationServiceInitThread extends Thread {	
	ClassificationService classificationService;
	
	
	/**
	 * Initialisieren des Threads
	 * @param classificationService ClassificationService, der initialisiert werden soll
	 */
	public ClassficationServiceInitThread(ClassificationService classificationService) {
		setName(ClassficationServiceInitThread.class.getSimpleName());
		this.classificationService = classificationService;
		start();
	}
	
	
	/**
	 * Warten und initialisieren des Klassifikators
	 */
	@SuppressWarnings("static-access")
	public void run() {
		try {
			sleep(classificationService.waitTimeToInitClassficationService);
			Thread thread = ThreadUtil.findThreadByName(InitializeServiceThread.class.getSimpleName());
			while(thread!=null && thread.isAlive()) sleep(100);
			classificationService.logger.debug("init ClassificationService");
			LogUtil.info("Zuständigkeitsfinder wird initialisiert ...");
			classificationService.reBuildClassifier();
		} catch (Exception e) {
			classificationService.logger.error("Fehler beim Initialisieren des ClassificationService", e);
		}
		LogUtil.info("Server klarschiff.backend ist gestartet.");
	}
}