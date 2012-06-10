package de.fraunhofer.igd.klarschiff.service.cluster;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Calendar;

/**
 * Annotation zum kennzeichnen von Jobs, die in einem Cluster nur von einer Serverinstanz gleichzeitig ausgeführt werden soll.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ScheduledSyncInCluster {
	
	/**
	 * Ein cron-Ausdruck, der die Ausführungszeit des Jobs festlegt.
	 * @return ein cron-Ausdruck
	 */
	String cron();
	
	/**
	 * Die Ausführungszeit des Jobs wird zur Synchronisation verwendet. Damit kleine Unterschiede bei den Servern nicht zu
	 * einer fehlerhaften Interpretation führen, wird die Ausführungszeit des Jobs auf ein Feld abgerundet.
	 * @return (default <code>Calendar.MINUTE</code>)
	 * @see java.util.Calendar
	 */
	int truncateField() default Calendar.MINUTE;
	
	/**
	 * Name des Jobs, damit dieser in der DB-Tabelle besser identifiziert werden kann.
	 * @return Name des Jobs
	 */
	String name() default "";
}
