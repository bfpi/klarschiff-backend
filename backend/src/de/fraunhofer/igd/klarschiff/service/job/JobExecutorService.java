package de.fraunhofer.igd.klarschiff.service.job;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Service;

/**
 * Die Klasse stellt einen Service zum Ausführen von Jobs bereit. Die Jobs werden dabei durch eine Pool verwaltet,
 * so dass nicht beliebig viele Jobs zur gleichen Zeit ausgeführt werden. Dieses wird beispielsweise für das Versenden 
 * von E-Mails verwendet.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Service
public class JobExecutorService {

	int corePoolSize = 2;
	int maxPooSize = 4;
	long keepAliveTime = 10;
	int workQueueSize = 30;
	
	ThreadPoolExecutor threadPool;
	ArrayBlockingQueue<Runnable> workQueue;
	
	/**
	 * Initialisierung der Queue und des Pools
	 */
	@PostConstruct
	public void init() {
		workQueue = new ArrayBlockingQueue<Runnable>(workQueueSize);
		threadPool = new ThreadPoolExecutor(corePoolSize, maxPooSize, keepAliveTime, TimeUnit.MINUTES, workQueue);
	}
	
	
	/**
	 * Stoppen des Pools.
	 */
	@PreDestroy
	public void stop() {
		threadPool.shutdown();
	}
	
	
	/**
	 * Ausführen eines Jobs im Pool
	 * @param job Job, der ausgeführt werden soll.
	 */
	public void runJob(Runnable job) {
		threadPool.execute(job);
	}

    /* --------------- GET + SET ----------------------------*/
	
	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPooSize() {
		return maxPooSize;
	}

	public void setMaxPooSize(int maxPooSize) {
		this.maxPooSize = maxPooSize;
	}

	public long getKeepAliveTime() {
		return keepAliveTime;
	}

	public void setKeepAliveTime(long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public int getWorkQueueSize() {
		return workQueueSize;
	}

	public void setWorkQueueSize(int workQueueSize) {
		this.workQueueSize = workQueueSize;
	}
}
