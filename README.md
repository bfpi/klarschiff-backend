# Klarschiff – Back-end

Basically, **Klarschiff** is a web application for public participation. It is hoped to be a helpful tool mainly for municipalities and other (public) administrative institutions to improve communication with their members (i.e. the people). The application consists of three parts fundamentally linked together: the back-end (provided and described here), the CitySDK Smart Participation API and the front-end.

The back-end of **Klarschiff** is a web application which can be used by municipalities and other (public) administrative institutions to manage all incoming events either reported via the corresponding front-end or reported via the back-end itself.

The project was initiated by the municipality of Rostock (Germany), more precisely its land registry office.


## Installation
### Voraussetzungen an den Arbeitsplatz
- Installiertes Java-JDK
- Apache Maven
### Voraussetzungen an den Webserver
- PostgreSQL-Datenbankserver mit installierter PostGIS-Erweiterung
- Apache Tomcat oder ein anderer Java-Applikationsserver mit aktiviertem *AJP Connector*
- Apache Webserver mit aktiviertem Modul *mod_rewrite*


### Vorbereitungen
- checkout / clone des Repositories in ein lokales Verzeichnis. Z.B.:

  ```bash
  sudo mkdir -p /usr/local
  cd /usr/local
  git clone https://github.com/bfpi/klarschiff-backend.git
  ```
- Installation der notwendigen Abhängigkeiten
  - Wechsel in das Repository-Verzeichnis
  - In Maven sind derzeit zwei notwendige Bibliotheken nicht vorhanden. Die Fehlenden Bibliotheken sind im Repository hinterlegt und können wie folgt in das Lokale Maven-Repository installiert werden:
    ```bash
    ./lib/install.sh
    ```
  - Konfiguration der Anwendung an die lokale Umgebung mittels Anpassungen in der `settings.properties` (Der Aufbau der `settings.properties` wird weiter unten im Bereich **Konfiguration der Applikation** beschrieben.)
  - Anschließend kann das .war-Archiv mit hilfe von Maven erzeugt werden.
    ```bash
    mvn clean package
    ```

## Konfiguration der Applikation
Für die Konfigurationsdateien mit vertraulichem Inhalt gibt es versionierbare Vorlagen mit dem Namen `xyz.sample.properties`. Diese müssen kopiert und entsprechend ohne das `sample` als `yxz.properties` benannt werden. Die für die Umgebung gültigen Werte werden dann in der `xyz.properties` konfiguriert.

### Konfigurationen in der `src/main/resources/settings.sample.properties`
Die folgenden Einstellungen können in der `settings.properties` vorgenommen werden:
#### Bereich 'context.'
  - `context.app.title`:
    - Name der Klarschiff Umgebung
  - `context.app.area`:
    - Stadt/Region für die das Klarschiff eingesetzt wird
  - `context.app.demo`:
    - Einstellung, ob Klarschiff im Demo-Betrieb laufen soll

#### Bereich 'mail.'
Einstellungen für den Mail-Versand aus der Anwendung
  - `mail.server.baseurl.backend`:
    - Die URL wird bei der Erzeugung von E-Mails verwendet und sollte auf die entsprechenden URLs des Backends verweisen.
  - `mail.server.baseurl.frontend`:
    - Die URL wird bei der Erzeugung von E-Mails verwendet und sollte auf die entsprechenden URLs des Frontends verweisen.
  - `mail.host`:
    - SMTP-Host, der für den Versand der E-Mails verwendet wird
  - `mail.smtp.starttls.enable`:
    - aktiviert (true) oder deaktiviert (false) die Verschlüsselung mit dem SMTP-Host; Eine Aktivierung wird z.B. bei der Verwendung eines Google-Mail-Accounts benötigt.
  - `mail.username`:
    - Benutzername für den SMTP-Zugang, wenn dieser benötigt wird
  - `mail.password`:
    - Passwort für den SMTP-Zugang, wenn dieser benötigt wird
  - `mail.from`:
    - Absenderadresse für die vom System versendeten E-Mails
  - `mail.sendAllMailsTo`:
    - Wenn hier eine E-Mail-Adresse angegeben wird, werden alle E-Mails an diese Adresse versendet. Dieses ist beispielsweise zum Test der E-Mail-Funktionen sinnvoll.
  - `mail.mailto.encoding`:
    - Konfiguration des zu verwendenden Encodings für den Mail-Versand

#### Bereich 'database.'
Einstellungen der verwendeten Datenbank für das Backend
  - `database.host`:
    - Host, des zu verwendenden Datenbank-Servers
  - `database.port`:
    - Port, des zu verwendenden Datenbank-Servers
  - `database.schema`:
    - Schema, innerhalb der verwendendeten Datenbank
  - `database.dbname`:
    - Name, der zu verwendenden Datenbank
  - `database.username`:
    - Benutzername für Zugriff auf die Datenbank
  - `database.password`:
    - Passwort für Zugriff auf die Datenbank

#### Bereich 'image.'
Konfigurationen zum speichern und laden von Bildern
  - `image.path`:
    - Lokaler Pfad, wo die Bilder abgelegt werden
  - `image.url`:
    - URL zum Aufruf der Bilder aus dem Web

#### Bereich 'resources.'
Einstellungen für die Automatische Erstellung von statische Dateien als Übersicht von aktuell aktiven Vorgängen
  - `resources.overview.path`:
    - Lokaler Pfad an dem die generierten Dateien abgelegt werden sollen
  - `resources.overview.entries_per_page`:
    - Anzahl der Vorgänge, die pro Seite angezeigt werden sollen (wird als Parameter mitgegeben)
  - `resources.overview.request_page`:
    - URL zum Frontend für die Erzeugung der einzelnen Seiten

#### Bereich 'job.'
Einstellungen für Automatische Bereinigungen
  - `job.monthsToArchivProbleme`:
    - Alter abgeschlossener Vorgänge vom Typ 'Problem' in Monaten, bis diese automatisch archiviert werden.
  - `job.monthsToArchivIdeen`:
    - Alter abgeschlossener Vorgänge vom Typ 'Idee' in Monaten, bis diese automatisch archiviert werden.
  - `job.hoursToRemoveUnbestaetigtVorgang`:
    - Alter von unbestätigten Vorgängen in Stunden, bis diese automatisch gelöscht werden.
  - `job.hoursToRemoveUnbestaetigtUnterstuetzer`:
    - Alter von unbestätigten Unterstützungen in Stunden, bis diese automatisch gelöscht werden.
  - `job.hoursToRemoveUnbestaetigtMissbrauchsmeldung`:
    - Alter von unbestätigten Missbrauchsmeldungen in Stunden, bis diese automatisch gelöscht werden.
  - `job.hoursToRemoveUnbestaetigtFoto`:
    - Alter von unbestätigten Fotos in Stunden, bis diese automatisch gelöscht werden.

#### Bereich 'geo.map.'
Konfigurationen zur Anzeige von Karten im Backend
  - `geo.map.projection`:
    - verwendete Projektion im System
  - `geo.map.maxExtent`:
    - Begrenzen der Daten der Karte
  - `geo.map.restrictedExtent`:
    - maximal anzuzeigende Größe der Karte
  - `geo.map.resolutions`:
    - verwendete Zoomstufen
  - `geo.map.units`:
    - Die Karteneinheiten für OpenLayers (z.B. m)
  - `geo.map.ovi.margin`:
    - Darzustellender Umkreis bei der Anzeige eines Ortes
  - `geo.map.layers.one.type`:
    - OpenLayers-Typ des ersten Karten-Layers (Stadtplan)
  - `geo.map.layers.one.params`:
    - OpenLayers-Parameter für die Anzeige des ersten Karten-Layers (Stadtplan)
  - `geo.map.layers.two.type`:
    - OpenLayers-Typ des zweiten Karten-Layers (Luftbild)
  - `geo.map.layers.two.params`:
    - OpenLayers-Parameter für die Anzeige des zweiten Karten-Layers (Luftbild)
  - `geo.map.layers.poi.type`:
    - OpenLayers-Typ des POI Karten-Layers
  - `geo.map.layers.poi.params`:
    - OpenLayers-Parameter für die Anzeige des POI Karten-Layers
  - `geo.map.extern.projection`:
    - Projektion im externen System (geo.map.extern.url)
  - `geo.map.extern.name`:
    - Name des externen System zur Anzeige eines Vorgangs
  - `geo.map.extern.url`:
    - URL zur Darstellung eines Vorganges in einem externen System (es können die Variablen %x%, %y% und %id% verwendet werden)
  - `geo.map.extern.extern.url`:
    - URL zur Darstellung eines Vorgangs in einem externen System, was von jedem Nutzer im Internet aufgerufen werden kann (es können die Variablen %x%, %y% und %id% verwendet werden)

#### Bereich 'geo.wfsvorgaenge.'
  - `geo.wfsvorgaenge.url`:
    - URL zum Aufruf der Vorgänge via WFS
  - `geo.wfsvorgaenge.featurens`:
    -  Ns für WFS-Feature (z.B. 'klarschiff')
  - `geo.wfsvorgaenge.featureprefix`:
    -  Paefix für WFS-Feature (z.B. 'klarschiff')
  - `geo.wfsvorgaenge.featuretype`:
    - Typ des Attributes im WFS-Feature (z.B. hro.klarschiff.meldungen)

#### Bereich 'geo.wfszufi.'
  - `geo.wfszufi.exception.handling`:
    - ExceptionHandling beim Initalisieren des WFS (warn - Fehlermeldungen werden in das Log geschrieben, error - der Start der Webanwendung wird bei einem Fehler abgebrochen)
  - `geo.wfszufi.ovi.buffer`:
    - Umkreis in Metern, der bei der Berechnung der Features für den Zuständigkeitsfinder berücksichtigt werden soll
  - `geo.wfszufi.capabilities.url`:
    - URL zum Aufruf der Capabilities des WFS
  - `geo.wfszufi.featureprefix`:
    -  Paefix für WFS-Feature (z.B. 'klarschiff_zufi' oder 'zufi')
  - `geo.wfszufi.bewirtschaftungskataster.featuretype`:
    - Typ des Attributes im WFS-Feature (z.B. hro.klarschiff-zustaendigkeitsfinder.bewirtschaftungskataster)
  - `geo.wfszufi.bewirtschaftungskataster.propertyname`:
    - Property-Name des Attributes im WFS-Feature (z.B. bewirtschafter)
  - `geo.wfszufi.bewirtschaftungskataster.geomname`:
    - Attributname der Geometrie beim WFS (z.B. geometrie)
  - `geo.wfszufi.flaechendaten.geomname`:
    - Attributname der Geometrie beim WFS (z.B. geometrie)

#### Bereich 'geo.adressensuche.'
  - `geo.adressensuche.url`:
    - URL zur Straßen- und Adresssuche

#### Bereich 'proxy.'
- Proxyeinstellungen, die der Server zur Kommunikation mit dem Internet benötigt. Diese werden z.B. für die Kommunikation mit dem WFS benötigt
  - `proxy.host`:
    - Host, des zu verwendenden Proxy-Servers
  - `proxy.port`:
    - Port, des zu verwendenden Proxy-Servers

#### Bereich 'ldap.'
  - `ldap.server.ldif`:
    - Wenn der Wert gesetzt ist, wird ein lokaler LDAP gestartet und die Daten aus der hier angegebenen LDIF-Datei werden verwendet.
  - `ldap.server.url`:
    - URL des zu verwendenden LDAP-Servers
  - `ldap.root`:
    - Rootpfad für die Anfragen an den LDAP
  - `ldap.managerDn`:
    - Benutzername für Zugriff auf den LDAP-Server
  - `ldap.managerPassword`:
    - Passwort für Zugriff auf den LDAP-Server
  - `ldap.userSearchBase`:
    - Pfad, in dem nach Benutzern gesucht werden soll
  - `ldap.userObjectClass`:
    - Objektklasse für Benutzer
  - `ldap.userSearchFilter`:
    - Filter zum Suchen von Benutzern
  - `ldap.userEmailFilter`:
    - Filter zum Suchen von Benutzern anhand der E-Mail-Adresse
  - `ldap.groupSearchBase`:
    - Pfad, in dem nach Gruppen gesucht werden soll
  - `ldap.groupObjectClass`:
    - Objektklasse für Gruppen
  - `ldap.groupRoleAttribute`:
    - Attribut, in dem bei den Gruppen die Rolle intern oder extern gesetzt ist
  - `ldap.groupSearchFilter`:
    - Attribut, in dem bei den Gruppen nach Benutzern gesucht wird
  - `ldap.groupObjectId`:
    - Attribut mit der ID für die Gruppe
  - `ldap.userAttributesMapping`:
    - Mapping für das Auslesen der Daten eines Benutzers beim LDAP. Format: [AttributNameAnwendung1]=[AttributNameLdap1],[AttributNameAnwendung2]=[AttributNameLdap2],...
  - `ldap.roleAttributesMapping`:
    - Mapping für das Auslesen der Daten einer Gruppe beim LDAP. Format: [AttributNameAnwendung1]=[AttributNameLdap1],[AttributNameAnwendung2]=[AttributNameLdap2],...
  - `ldap.connectionBaseEnvironment`:
    - Benutzerdefinierte Umgebungseigenschaften für LDAP-Kommunikation

#### Bereich 'show.'
  - `show.logins`:
    - Aktiviert (true) oder deaktiviert (false) eine Anzeige von statischen Logindaten unter dem Login

#### Bereich 'vorgang.idee.'
  - `vorgang.idee.unterstuetzer`:
    - Anzahl der Unterstützungen, die eine Idee benötigt, damit sie in der einfachen Suche angezeigt wird

#### Bereich 'vorgang.statusKommentar.'
  - `vorgang.statusKommentar.textlaengeMaximal`:
    - Konfiguration der maximalen Länge für öffentliche Statusinformationen

#### Bereich 'show.fehler.'
  - `show.fehler.details`:
    - Aktiviert (true) oder deaktiviert (false) die ausführliche Fehleranzeige in der Webanwendung

#### Bereich 'bug.tracking.'
  - `bug.tracking.url`:
    - URL auf ein Bugtracking-System, die bei einem Fehler angezeigt wird

#### Bereich 'show.' (2)
  - `show.connector`:
    - Aktiviert (true) oder deaktiviert (false) eine Anzeige des Connectors auf jeder Seite im Footer

#### Bereich 'version'
  - `version`:
    - Bezeichnung der Version, die im Footer angezeigt werden soll

#### Bereich 'auth.'
  - `auth.kod_code`:
    - Authorisierungsschlüssel für die erweiterte Kommunikation zwischen Klarschiff-CitySDK und Backend
  - `auth.internal_author_match`:
    - Regex zur Identifizierung von internen Benutzern via Author-Email

#### Bereich 'trust.'
  - `trust.level.one.mail_match`:
    - Regex zur Identifizierung von vertrauenswürdigen Erstellern via Author-Email
  - `trust.level.one.ldap_match`:
    - Filter zum Identifizierung von vertrauenswürdigen Erstellern via LDAP-Gruppen-Zugehörigkeit
  - `trust.level.two.mail_match`:
    - Regex zur Identifizierung von vertrauenswürdigen Erstellern via Author-Email
  - `trust.level.two.ldap_match`:
    - Filter zum Identifizierung von vertrauenswürdigen Erstellern via LDAP-Gruppen-Zugehörigkeit

#### Bereich 'd3.'
  - `d3.api`:
    - URL des d.3 API-Endpunkts
  - `d3.request.akte.check_existence`:
    - Pfad der API-Funktion zur Akten-/Dokumentensuche
  - `d3.request.akte.show`:
    - Pfad der API-Funktion zum Abrufen der Akten-/Dokumenten-ID
  - `d3.proxy.host`:
    - Host, des zu verwendenden Proxy-Servers für d.3-Anfragen
  - `d3.proxy.port`:
    - Port, des zu verwendenden Proxy-Servers für d.3-Anfragen

### URL-Umleitung und Direkt-Links

#### Änderungen an der .conf-Datei des Apache-Servers
- Konfiguration der Weiterleitung von Anfragen an das Backend mittels AJP-Connector
  ```bash
  <Location /backend>
    ProxyPass           ajp://localhost:8009/backend
    ProxyPassReverse    ajp://localhost:8009/backend

    # serverseitige Komprimierung der ausgelieferten Inhalte
    SetOutputFilter DEFLATE
    BrowserMatch \bMSIE !no-gzip !gzip-only-text/html
    SetEnvIfNoCase Request_URI \.(?:gif|jpe?g|png)$ no-gzip dont-vary
    Header append Vary User-Agent env=!dont-vary
  </Location>
  ```
- Aktivierung der RewriteEngine zum Umschreiben der angeforderten URL
  ```bash
    RewriteEngine on
    RewriteRule ^.*(fotoBestaetigung/.*)$               http://localhost:8080/backend/service/$1 [P,QSA,L]
    RewriteRule ^.*(missbrauchsmeldungBestaetigung/.*)$ http://localhost:8080/backend/service/$1 [P,QSA,L]
    RewriteRule ^.*(unterstuetzerBestaetigung/.*)$      http://localhost:8080/backend/service/$1 [P,QSA,L]
    RewriteRule ^.*(vorgangBestaetigung/.*)$            http://localhost:8080/backend/service/$1 [P,QSA,L]
    RewriteRule ^.*(vorgangLoeschen/.*)$                http://localhost:8080/backend/service/$1 [P,QSA,L]
  ```

## Produktive Version

**Klarschiff** läuft auf den Servern der Initiatoren als real produktive Version namens **Klarschiff.HRO**, ist aber nur intern erreichbar.

## Kontakt

Gerne können Sie die Initiatoren des Projekts per E-Mail kontaktieren: <klarschiff-hro@rostock.de>.

## License

[Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

Copyright © 2012-2018, Hanse- und Universitätsstadt Rostock

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.

You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
