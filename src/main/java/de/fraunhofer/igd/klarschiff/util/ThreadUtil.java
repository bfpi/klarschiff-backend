package de.fraunhofer.igd.klarschiff.util;

/**
 * Die Klasse hilft beim Auffinden von Threads.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class ThreadUtil {

  /**
   * Finden von Threads anhand des Namens
   *
   * @param name Name des Threads der gesucht werden soll.
   * @return <code>null</code> - wenn kein Thread mit dem Namen gefunden wurde; sonst der gefundene
   * Thread
   */
  public static Thread findThreadByName(String name) {
    ThreadGroup group = Thread.currentThread().getThreadGroup();

    while (group.getParent() != null) {
      group = group.getParent();
    }
    Thread[] threadList = new Thread[group.activeCount() + 5];
    group.enumerate(threadList, true);
    for (Thread thread : threadList) {
      if (thread != null && thread.getName().equals(name)) {
        return thread;
      }
    }
    return null;
  }
}
