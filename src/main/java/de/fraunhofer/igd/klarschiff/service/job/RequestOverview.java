package de.fraunhofer.igd.klarschiff.service.job;

import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangStatus;
import de.fraunhofer.igd.klarschiff.web.VorgangSuchenCommand;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.log4j.Logger;

public class RequestOverview {

  private static final Logger logger = Logger.getLogger(JobsService.class);

  /**
   * Die Klasse erzeugt die Liste der Vorgänge als Statische Dateien für das Frontend.
   *
   * @param settingsService SettingsService
   * @param vorgangDao VorgangDao
   */
  public void create(SettingsService settingsService, VorgangDao vorgangDao) {
    try {
      String resourcesPath = settingsService.getPropertyValue("resources.overview.path");
      String resourcesRequestPage = settingsService.getPropertyValue("resources.overview.request_page");

      if (resourcesPath.length() > 0 && resourcesRequestPage.length() > 0) {
        File requestsPath = new File(resourcesPath);
        if (requestsPath.exists()) {
          FileDeleteStrategy.FORCE.delete(requestsPath);
        }
        requestsPath.mkdir();

        VorgangSuchenCommand cmd = new VorgangSuchenCommand();
        cmd.setSuchtyp(VorgangSuchenCommand.Suchtyp.erweitert);
        cmd.setOrder(0);
        cmd.setOrderDirection(0);
        cmd.setUeberspringeVorgaengeMitMissbrauchsmeldungen(true);
        cmd.setErweitertArchiviert(false);
        cmd.setErweitertVorgangStatus(EnumVorgangStatus.aussendienstVorgangStatus());
        List<Object[]> vg = vorgangDao.getVorgaenge(cmd);

        int eintraegeProSeite = Integer.parseInt(settingsService.getPropertyValue("resources.overview.entries_per_page"));
        int anzahl = vg.size();
        int seiten = (anzahl - (anzahl % eintraegeProSeite)) / eintraegeProSeite;
        int letzteSeite = anzahl % eintraegeProSeite;
        if (letzteSeite > 0) {
          seiten++;
        }

        for (int i = 1; i <= seiten; i++) {
          StringBuilder content = new StringBuilder();
          String url = new String(resourcesRequestPage);
          url = url.replaceAll("%page%", i + "");
          url = url.replaceAll("%per_page%", eintraegeProSeite + "");
          url = url.replaceAll("%pages%", seiten + "");
          URLConnection connection = new URL(url).openConnection();
          BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
          String inputLine;

          while ((inputLine = reader.readLine()) != null) {
            content.append(inputLine);
          }
          reader.close();

          File file = new File(resourcesPath + i + ".html");
          BufferedWriter output = null;
          try {
            output = new BufferedWriter(new FileWriter(file));
            output.write(content.toString());
          } catch (IOException e) {
            logger.error("Vorgang-Übersicht konnte nicht erneuert werden.", e);
          } finally {
            if (output != null) {
              output.close();
            }
          }
        }
      }
    } catch (MalformedURLException e) {
      logger.error("Vorgang-Übersicht konnte nicht erneuert werden.", e);
    } catch (IOException e) {
      logger.error("Vorgang-Übersicht konnte nicht erneuert werden.", e);
    }
  }
}
