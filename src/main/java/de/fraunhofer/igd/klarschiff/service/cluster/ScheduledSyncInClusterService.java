package de.fraunhofer.igd.klarschiff.service.cluster;

import java.lang.reflect.Method;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import de.fraunhofer.igd.klarschiff.dao.JobDao;

/**
 * Service, der die mit <code>@ScheduledSyncInCluster</code> annotierten Methoden identifiziert und
 * die Ausführung der Jobs initialisiert.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Service
public class ScheduledSyncInClusterService implements ApplicationContextAware {

  @Autowired
  JobDao jobDao;

  /**
   * Nachdem der ApplikationContext aufgebaut ist, werden alle Componenten im ApplicationContext
   * nach Methode gescant, die mit <code>@ScheduledSyncInCluster</code> annotiert sind. Die Methoden
   * werden als Job initialisiert, die im Cluster synchronisiert ausgeführt werden.
   * @param ctx ApplicationContext
   */
  @Override
  public void setApplicationContext(ApplicationContext ctx) throws BeansException {
    ThreadPoolTaskScheduler taskScheduler = ctx.getBean(ThreadPoolTaskScheduler.class);
    for (Object bean : ctx.getBeansWithAnnotation(Component.class).values()) {
      for (Method method : bean.getClass().getMethods()) {
        if (method.isAnnotationPresent(ScheduledSyncInCluster.class)) {
          try {
            ScheduledSyncInCluster scheduledSyncInCluster = method.getAnnotation(ScheduledSyncInCluster.class);

            MethodInvokingSyncInClusterRunnable runnable = new MethodInvokingSyncInClusterRunnable();
            runnable.setTargetMethod(method.getName());
            runnable.setTargetObject(bean);
            runnable.setJobDao(jobDao);
            runnable.setTruncateField(scheduledSyncInCluster.truncateField());
            runnable.setName(StringUtils.isBlank(scheduledSyncInCluster.name()) ? bean.getClass().getName() + "." + method.getName() : scheduledSyncInCluster.name());
            runnable.prepare();
            taskScheduler.schedule(runnable, new CronTrigger(scheduledSyncInCluster.cron()));
          } catch (Exception e) {
            throw new RuntimeException("ScheduledSyncInCluster kann nicht für " + bean.getClass().getName() + "." + method.getName() + " registriert werden.", e);
          }
          // do something
        }
      }
    }
  }
}
