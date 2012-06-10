package de.fraunhofer.igd.klarschiff.service.cluster;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.springframework.scheduling.support.MethodInvokingRunnable;

import de.fraunhofer.igd.klarschiff.dao.JobDao;
import de.fraunhofer.igd.klarschiff.vo.JobRun;

/**
 * Runnable-Klasse, die vor dem Ausführen des eigentlichen Jobs mit Hilfe der <code>JobDao</code> überprüft, ob
 * der Job bereits von einem anderem Server im Cluster ausgeführt wurde. 
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class MethodInvokingSyncInClusterRunnable extends MethodInvokingRunnable {

	int truncateField;
	String name;
	
	JobDao jobDao;
	
	/**
	 * Führt einen Job aus und registiret diesen über die DB, wenn dieser noch nicht von einem anderen Server im Cluster 
	 * ausgeführt wurde.
	 */
	@Override
	public Object invoke() throws InvocationTargetException, IllegalAccessException {
		String id=null;
		try {
			id = jobDao.registerJobRun(new Date(), name, ClusterUtil.getServerIp(), ClusterUtil.getServerName(), ClusterUtil.getServerConnectorPort(), truncateField);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (id!=null) {
			logger.info("Job ["+name+"] wird ausgeführt.");
			try {
				Object result = super.invoke();
				jobDao.updateJobRun(id, JobRun.Ergebnis.abgeschlossen, null);
				return result;
			} catch (InvocationTargetException e) {
				jobDao.updateJobRun(id, JobRun.Ergebnis.fehlerhaft, e);
				throw e;
			} catch (IllegalAccessException e) {
				jobDao.updateJobRun(id, JobRun.Ergebnis.fehlerhaft, e);
				throw e;
			}
		} else {
			logger.info("Job ["+name+"] wurde bereits von einem anderem Client im Cluster registriert. Job wurde abgebrochen.");
			return null;
		}
	}

    /* --------------- GET + SET ----------------------------*/

	public int getTruncateField() {
		return truncateField;
	}

	public void setTruncateField(int truncateField) {
		this.truncateField = truncateField;
	}

	public JobDao getJobDao() {
		return jobDao;
	}

	public void setJobDao(JobDao jobDao) {
		this.jobDao = jobDao;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
