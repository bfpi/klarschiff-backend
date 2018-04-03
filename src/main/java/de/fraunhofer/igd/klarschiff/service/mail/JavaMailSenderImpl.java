package de.fraunhofer.igd.klarschiff.service.mail;

import java.util.Properties;
import org.apache.commons.lang.StringUtils;

/**
 * Erweiterung der Klasse <code>org.springframework.mail.javamail.JavaMailSenderImpl</code>, die
 * auch den Versand von E-Mails über eine SMTP-Provider mit STARTTLS erlaubt.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class JavaMailSenderImpl extends org.springframework.mail.javamail.JavaMailSenderImpl {

  /**
   * Setzt den Benutzernamen für dem SMTP-Versand.
   *
   * @param username Benutzername
   */
  @Override
  public void setUsername(String username) {
    if (!StringUtils.isBlank(username)) {
      super.setUsername(username);
    }
  }

  /**
   * Setzt das Passwort für dem SMTP-Versand.
   *
   * @param password Passwort
   */
  @Override
  public void setPassword(String password) {
    if (!StringUtils.isBlank(password)) {
      super.setPassword(password);
    }
  }

  /**
   * Aktiviert den STARTTLS-Support.
   *
   * @param smtpStarttlsEnable Boolean ob STARTTLS Aktiviert werden soll
   */
  public void setSmtpStarttlsEnable(boolean smtpStarttlsEnable) {
    if (smtpStarttlsEnable) {
      Properties prop = new Properties();
      prop.setProperty("mail.smtp.starttls.enable", "true");
      setJavaMailProperties(prop);
    }
  }
}
