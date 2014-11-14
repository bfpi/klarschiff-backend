1. Pfad zu den neuen Bildern in migrate_data.sql konfigurieren (abschließender Slash):
  basepath = '/var/www/html/klarschiff_fotos/';

2. Datei "./run.sh" ausführen
  Voraussetzungen:
  Vorher bitte testen, ob Aufrufe "psql klarschiff_backend klarschiff_backend", "psql klarschiff_frontend klarschiff_frontend" und "psql mapbender mapbender" funktionieren, ggf. Passwort ermitteln.
  In der Datenbank klarschiff_backend muss die Funktion uuid_generate_v4() exisitieren, prüfen mit "\df uuid_generate_v4"
  Im Backend müssten die Tabellen im public-Schema und im Frontend im klarschiff-Schema sein.

3. Neu entstandene Bilder dem Tomcat-Nutzer übereignen:
  chown -R tomcat7:tomcat7 /var/www/html/klarschiff_fotos/

4. Alte Bilder verschieben oder löschen:
  mv /var/www/html/klarschiff_fotos/*{thumb,normal,gross}.jpg /backup-dir
  rm /var/www/html/klarschiff_fotos/*{thumb,normal,gross}.jpg

5. In der settings.properties des Backends Pfad zu den Bildern einfügen (abschließender Slash)
  image.path = /var/www/html/klarschiff_fotos/
  image.url = /klarschiff_fotos/

6. Backend deployen

7. DbLink-Skript per Admin-Funktion im Backend ausführen

8. Im Geoserver für die Layer vorgaenge und vorgaenge_rss den Feature Type erneut laden
