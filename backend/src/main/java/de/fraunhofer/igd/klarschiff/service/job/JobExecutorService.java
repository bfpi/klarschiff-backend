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

	int corePoolSize = 5;
	int maxPoolSize = 10;
	long keepAliveTime = 24;
	
	ThreadPoolExecutor threadPool = null;
	ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue(1000);
	
    
	/**
	 * Initialisierung der Queue und des Pools
	 */
	@PostConstruct
	public void init() {
		threadPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.HOURS, workQueue);
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

	public int getmaxPoolSize() {
		return maxPoolSize;
	}

	public void setmaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public long getKeepAliveTime() {
		return keepAliveTime;
	}

	public void setKeepAliveTime(long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}
}
