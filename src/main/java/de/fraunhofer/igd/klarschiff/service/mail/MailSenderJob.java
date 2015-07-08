package de.fraunhofer.igd.klarschiff.service.mail;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.mail.SimpleMailMessage;

/**
 * Mit der Klasse kann ein Job zum Versand eine E-Mail erzeugt werden.
 *
 * @see de.fraunhofer.igd.klarschiff.service.job.JobExecutorService
 * @author Stefan Audersch (Fraunhofer IGD)
 *
 */
public class MailSenderJob implements Runnable {

  Logger logger = Logger.getLogger(MailSenderJob.class);
  MailService mailService;
  Object msg;

  /**
   * Erzeugt einen Job zum E-Mailversand.
   *
   * @param mailService Mailservice der zum Versenden der E-Mail verwendet werden soll.
   * @param msg Erstellte E-Mail als MimeMassage.
   */
  public MailSenderJob(MailService mailService, MimeMessage msg) {
    this.mailService = mailService;
    this.msg = msg;
  }

  /**
   * Erzeugt einen Job zum E-Mailversand.
   *
   * @param mailService Mailservice der zum Versenden der E-Mail verwendet werden soll.
   * @param msg Erstellte E-Mail als SimpleMailMessage.
   */
  public MailSenderJob(MailService mailService, SimpleMailMessage msg) {
    this.mailService = mailService;
    this.msg = msg;
  }

  /**
   * Methode die beim Versenden der E-Mail ausgeführt wird.
   */
  @Override
  public void run() {
    try {
      if (msg instanceof SimpleMailMessage) {
        SimpleMailMessage _msg = (SimpleMailMessage) msg;
        logger.debug("Send E-Mail: " + _msg.getSubject());
        if (!StringUtils.isBlank(mailService.getSendAllMailsTo())) {
          _msg.setTo(mailService.getSendAllMailsTo());
        }
        mailService.getMailSender().send(_msg);
      } else if (msg instanceof MimeMessage) {
        MimeMessage _msg = (MimeMessage) msg;
        logger.debug("Send E-Mail: " + _msg.getSubject());
        if (!StringUtils.isBlank(mailService.getSendAllMailsTo())) {
          _msg.setRecipient(RecipientType.TO, new InternetAddress(mailService.getSendAllMailsTo()));
        }
        mailService.getMailSender().send(_msg);
      } else {
        throw new Exception();
      }
    } catch (Exception e) {
      logger.fatal(e);
    }
  }
}
