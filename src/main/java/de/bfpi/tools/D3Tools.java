package de.bfpi.tools;

import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class D3Tools {

  @Autowired
  SecurityService securityService;

  @Autowired
  SettingsService settingsService;

  public Boolean documentExists(Vorgang vorgang) {
    Document document = executeRequest(vorgang.getD3CheckExistenceUrl());
    return (document != null && document.getRootElement().getValue().trim().equals("1"));
  }

  public String getDocumentId(Vorgang vorgang) {
    if (documentExists(vorgang)) {
      Document document = executeRequest(vorgang.getD3ShowUrl());
      return document.getRootElement().getValue().trim();
    }
    return null;
  }

  public String getCreateLink(Vorgang vorgang) {
    if (vorgang.getKategorie().getD3() != null) {
      String ret = vorgang.getKategorie().getD3().getUrl();
      ret = ret.replace("{ks_id}", vorgang.getId().toString());
      ret = ret.replace("{ks_user}", securityService.getCurrentUser().getName());
      ret = ret.replace("{ks_address}", vorgang.getAdresse());
      return ret;
    }
    return null;
  }

  private Document executeRequest(String request) {
    try {
      String url = settingsService.getPropertyValue("d3.api") + request;

      URL httpUrl = new URL(url);
      HttpURLConnection connection;
      if (!StringUtils.isBlank(settingsService.getPropertyValue("d3.proxy.host")) && !StringUtils.isBlank(settingsService.getPropertyValue("d3.proxy.port"))) {
        InetSocketAddress proxyInet = new InetSocketAddress(settingsService.getPropertyValue("d3.proxy.host"), Integer.parseInt(settingsService.getPropertyValue("d3.proxy.port")));
        Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyInet);
        connection = (HttpURLConnection) httpUrl.openConnection(proxy);
      } else {
        connection = (HttpURLConnection) httpUrl.openConnection();
      }
      connection.setRequestMethod("GET");

      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuilder content = new StringBuilder();
      while ((inputLine = in.readLine()) != null) {
        content.append(inputLine);
      }
      in.close();

      SAXBuilder builder = new SAXBuilder();
      StringReader response_in = new StringReader(content.toString());
      return builder.build(response_in);
    } catch (MalformedURLException ex) {
      Logger.getLogger(D3Tools.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ProtocolException | JDOMException ex) {
      Logger.getLogger(D3Tools.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(D3Tools.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }
}
