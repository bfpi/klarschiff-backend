-- =======================================================================================
-- vim: set filetype=pgsql :
-- @author Stefan Audersch (Fraunhofer IGD)
-- @author Peter Koenig (WhereGroup)
-- @author Alexander Kruth (BFPI GmbH)
-- @author Niels Bennke (BFPI GmbH)

-- Hinweis: Variablen werden vor der Ausfuehrung durch die entsprechenden Werte ersetzt
-- =======================================================================================

-- #######################################################################################
-- # EnumVorgangStatus                                                                   #
-- #######################################################################################
-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_enum_vorgang_status()
RETURNS trigger AS $BODY$
DECLARE
  query text;

BEGIN
  PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} ' ||
    'user=${f_username} password=${f_password}');
  
  query := CASE TG_OP
    WHEN 'DELETE' THEN
      'DELETE FROM ${f_schema}.klarschiff_status WHERE id = ' || quote_literal(old.id)
    WHEN 'UPDATE' THEN
      'UPDATE ${f_schema}.klarschiff_status ' ||
      'SET "name" = ' || quote_literal(new."text") || ', ' ||
      'nid = ' || new.ordinal || ' ' || 
      'WHERE id = ' || quote_literal(new.id)
    WHEN 'INSERT' THEN
      'INSERT INTO ${f_schema}.klarschiff_status (id, "name", nid) ' ||
      'VALUES (' || quote_literal(new.id) || ', ' || quote_literal(new."text") || ', ' || 
        new.ordinal || ')'
    ELSE 
      'SELECT 1'
    END;

  RAISE DEBUG 'Query : %', query;
  EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
  PERFORM dblink_disconnect();

  IF TG_OP = 'DELETE' THEN 
    RETURN old;
  ELSIF TG_OP IN ('INSERT', 'UPDATE') THEN
    RETURN new;
  ELSE
    RETURN NULL;
  END IF;
EXCEPTION WHEN others THEN
  PERFORM dblink_disconnect();
  RAISE;
END;
$BODY$ LANGUAGE plpgsql VOLATILE COST 100;

-- Owner fuer die Triggerfunktion setzen
ALTER FUNCTION klarschiff_triggerfunction_enum_vorgang_status() OWNER TO ${b_username};

-- ggf. alten Trigger loeschen
DROP TRIGGER IF EXISTS klarschiff_trigger_enum_vorgang_status ON klarschiff_enum_vorgang_status CASCADE;

-- Trigger erzeugen
CREATE TRIGGER klarschiff_trigger_enum_vorgang_status
  BEFORE INSERT OR UPDATE OR DELETE
  ON klarschiff_enum_vorgang_status
  FOR EACH ROW EXECUTE PROCEDURE klarschiff_triggerfunction_enum_vorgang_status();

-- Test
-- INSERT INTO klarschiff_enum_vorgang_status (id, "text", ordinal) values ('test', 'test', 100);
-- UPDATE klarschiff_enum_vorgang_status SET text='Test' WHERE id='test';
-- DELETE FROM klarschiff_enum_vorgang_status WHERE id = 'test';


-- #######################################################################################
-- # EnumVorgangTyp                                                                      #
-- #######################################################################################
-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_enum_vorgang_typ()
RETURNS trigger AS $BODY$
DECLARE
  query text;

BEGIN
  PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} ' ||
    'user=${f_username} password=${f_password}');

  query := CASE TG_OP
    WHEN 'DELETE' THEN
      'DELETE FROM ${f_schema}.klarschiff_vorgangstyp WHERE id = ' || quote_literal(old.id)
    WHEN 'UPDATE' THEN
      'UPDATE ${f_schema}.klarschiff_vorgangstyp ' ||
      'SET "name" = ' || quote_literal(new."text") || ', ordinal = ' || new.ordinal || ' ' ||
      'WHERE id = ' || quote_literal(new.id)
    WHEN 'INSERT' THEN
      'INSERT INTO ${f_schema}.klarschiff_vorgangstyp (id, "name", ordinal) ' ||
      'VALUES (' || quote_literal(new.id) || ', ' || quote_literal(new."text") || ', ' || 
        new.ordinal || ')'
    ELSE
      'SELECT 1'
    END;

  RAISE DEBUG 'Query : %', query;
  EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
  PERFORM dblink_disconnect();

  IF TG_OP = 'DELETE' THEN
    RETURN old;
  ELSIF TG_OP IN ('INSERT', 'UPDATE') THEN
    RETURN new;
  ELSE
    RETURN NULL;
  END IF;
EXCEPTION WHEN others THEN
  PERFORM dblink_disconnect();
  RAISE;
END;
$BODY$ LANGUAGE plpgsql VOLATILE COST 100;

-- Owner fuer die Triggerfunktion setzen
ALTER FUNCTION klarschiff_triggerfunction_enum_vorgang_typ() OWNER TO ${b_username};

-- ggf. alten Trigger loeschen
DROP TRIGGER IF EXISTS klarschiff_trigger_enum_vorgang_typ ON klarschiff_enum_vorgang_typ CASCADE;

-- Trigger erzeugen
CREATE TRIGGER klarschiff_trigger_enum_vorgang_typ
  BEFORE INSERT OR UPDATE OR DELETE
  ON klarschiff_enum_vorgang_typ
  FOR EACH ROW EXECUTE PROCEDURE klarschiff_triggerfunction_enum_vorgang_typ();

-- Test
-- INSERT INTO klarschiff_enum_vorgang_typ (id, "text", ordinal) values ('test', 'test', 100);
-- UPDATE klarschiff_enum_vorgang_typ SET text='Test' WHERE id='test';
-- DELETE FROM klarschiff_enum_vorgang_typ WHERE id = 'test';


-- #######################################################################################
-- # GeoRss                                                                              #
-- #######################################################################################
-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_geo_rss()
RETURNS trigger AS $BODY$
DECLARE
  query text;

BEGIN
  PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

  IF TG_OP = 'DELETE' THEN
    query := 'DELETE FROM ${f_schema}.klarschiff_geo_rss WHERE id = ' || old.id;
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN old;

  ELSIF TG_OP = 'UPDATE' THEN
    query := 'UPDATE ${f_schema}.klarschiff_geo_rss ' ||
      'SET klarschiff_geo_rss_fid = ' || new.id || ', ' ||
      'ideen = ' || new.ideen || ', ' ||
      'ideen_kategorien = ' || quote_literal(new.ideen_kategorien) || ', ' || 
      'probleme = ' || new.probleme || ', ' ||
      'probleme_kategorien = ' || quote_literal(new.probleme_kategorien) || ', ' || 
      'the_geom = ' || quote_literal(new.ovi::text) || ' ' ||
      'WHERE id = ' || new.id;
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN new;

  ELSIF TG_OP = 'INSERT' THEN
    query := 'INSERT INTO ${f_schema}.klarschiff_geo_rss (id, klarschiff_geo_rss_fid, ' ||
      'ideen, ideen_kategorien, probleme, probleme_kategorien, the_geom) ' ||
      'VALUES (' || new.id || ', ' || new.id || ', ' ||
      new.ideen || ', ' || quote_literal(new.ideen_kategorien) || ', ' || 
      new.probleme || ', ' || quote_literal(new.probleme_kategorien) || ', ' ||
      quote_literal(new.ovi::text) || ')';
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN new;
   
  END IF;
   
  PERFORM dblink_disconnect();
  RETURN NULL;
EXCEPTION WHEN others THEN
  PERFORM dblink_disconnect();
  RAISE;
END;
$BODY$ LANGUAGE plpgsql VOLATILE COST 100;

-- Owner fuer die Triggerfunktion setzen
ALTER FUNCTION klarschiff_triggerfunction_geo_rss() OWNER TO ${b_username};

-- ggf. alten Trigger loeschen
DROP TRIGGER IF EXISTS klarschiff_trigger_geo_rss ON klarschiff_geo_rss CASCADE;

-- Trigger erzeugen
CREATE TRIGGER klarschiff_trigger_geo_rss
  BEFORE INSERT OR UPDATE OR DELETE
  ON klarschiff_geo_rss
  FOR EACH ROW EXECUTE PROCEDURE klarschiff_triggerfunction_geo_rss();

-- Test
--INSERT INTO klarschiff_geo_rss (id, ideen, probleme, ideen_kategorien, probleme_kategorien, ovi) VALUES (1000, true, true, '68', '1', '0106000020E96400000100000001030000000100000006000000012CB8D0E6D31241F1D9156712E55641E89B97B331E7124173DD6F1C8EE456412B634BB459D31241BB5BFEB862E15641ABA315B506BD12411BF24F8ECCE15641F5EC5E5FC6C71241BAB1EA588EE35641012CB8D0E6D31241F1D9156712E55641');
--UPDATE klarschiff_geo_rss SET ideen_kategorien = '68,73' WHERE id = 1000;
--DELETE FROM klarschiff_geo_rss WHERE id = 1000;


-- #######################################################################################
-- # Kategorie                                                                           #
-- #######################################################################################
-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_kategorie()
RETURNS trigger AS $BODY$
DECLARE
  query text;

BEGIN
  PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

  IF TG_OP = 'DELETE' THEN
    query := 'DELETE FROM ${f_schema}.klarschiff_kategorie WHERE id = ' || old.id;
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN old;

  ELSIF TG_OP = 'UPDATE' THEN
    query := 'UPDATE ${f_schema}.klarschiff_kategorie ' ||
      'SET "name" = ' || quote_literal(new."name") || ', ';
    --parent
    IF new.parent IS NOT NULL THEN
        query := query || 'parent = ' || new.parent || ', ';
     ELSE
        query := query || 'parent = NULL, ';
    END IF;
    --typ
    IF new.typ IS NOT NULL THEN
        query := query || 'vorgangstyp = ' || quote_literal(new.typ) || ', ';
     ELSE
        query := query || 'vorgangstyp = NULL, ';
    END IF;
    --naehere_beschreibung_notwendig
    IF new.naehere_beschreibung_notwendig IS NOT NULL THEN
        query := query || 'naehere_beschreibung_notwendig = ' || quote_literal(new.naehere_beschreibung_notwendig) || ', ';
     ELSE
        query := query || 'naehere_beschreibung_notwendig = NULL, ';
    END IF;
    --aufforderung  --########### @deprecated ############
    IF new.naehere_beschreibung_notwendig IS NULL OR new.naehere_beschreibung_notwendig = 'keine' THEN
        query := query || 'aufforderung = FALSE ';
     ELSE
        query := query || 'aufforderung = TRUE ';
    END IF;
    query := query || 'WHERE id = ' || new.id;
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN new;

  ELSIF TG_OP = 'INSERT' THEN
    query := 'INSERT INTO ${f_schema}.klarschiff_kategorie (id, "name", parent, ' ||
      'vorgangstyp, naehere_beschreibung_notwendig, aufforderung) ' ||
      'VALUES (' || new.id || ', ' || quote_literal(new."name") || ', '; 
    --parent
    IF new.parent IS NOT NULL THEN
        query := query || new.parent || ', ';
     ELSE
        query := query || 'NULL, ';
    END IF;
    --typ
    IF new.typ IS NOT NULL THEN
        query := query || quote_literal(new.typ) || ', ';
     ELSE
        query := query || 'NULL, ';
    END IF;
    --naehere_beschreibung_notwendig
    IF new.naehere_beschreibung_notwendig IS NOT NULL THEN
        query := query || quote_literal(new.naehere_beschreibung_notwendig) || ', ';
     ELSE
        query := query || 'NULL, ';
    END IF;
    --aufforderung  --########### @deprecated ############
    IF new.naehere_beschreibung_notwendig IS NULL OR new.naehere_beschreibung_notwendig = 'keine' THEN
        query := query || 'FALSE)';
     ELSE
        query := query || 'TRUE)';
    END IF;
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN new;
   
  END IF;
   
  PERFORM dblink_disconnect();
  RETURN NULL;
EXCEPTION WHEN others THEN
  PERFORM dblink_disconnect();
  RAISE;
END;
$BODY$ LANGUAGE plpgsql VOLATILE COST 100;

-- Owner fuer die Triggerfunktion setzen
ALTER FUNCTION klarschiff_triggerfunction_kategorie() OWNER TO ${b_username};

-- ggf. alten Trigger loeschen
DROP TRIGGER IF EXISTS klarschiff_trigger_kategorie ON klarschiff_kategorie CASCADE;

-- Trigger erzeugen
CREATE TRIGGER klarschiff_trigger_kategorie
  BEFORE INSERT OR UPDATE OR DELETE
  ON klarschiff_kategorie
  FOR EACH ROW EXECUTE PROCEDURE klarschiff_triggerfunction_kategorie();

-- Test
--INSERT INTO klarschiff_enum_vorgang_typ (id, "text", ordinal) values ('test', 'test', 100);
--INSERT INTO klarschiff_kategorie (id, "name", typ, parent, naehere_beschreibung_notwendig) values (1000, 'test0', 'test', NULL, 'keine');
--INSERT INTO klarschiff_kategorie (id, "name", typ, parent, naehere_beschreibung_notwendig) values (1001, 'test1', NULL, 1000, 'keine');
--UPDATE klarschiff_kategorie SET "name" = 'test01' WHERE id = 1000;
--DELETE FROM klarschiff_kategorie WHERE id = 1001;
--DELETE FROM klarschiff_kategorie WHERE id = 1000;
--DELETE FROM klarschiff_enum_vorgang_typ WHERE id = 'test';


-- #######################################################################################
-- # Missbrauchsmeldung                                                                  #
-- #######################################################################################
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_missbrauchsmeldung()
RETURNS trigger AS $BODY$
DECLARE
  query text;

BEGIN
  PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

  IF TG_OP = 'DELETE' THEN
    query := 'DELETE FROM ${f_schema}.klarschiff_missbrauchsmeldung WHERE id = ' || old.id;
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN old;

  ELSIF TG_OP = 'UPDATE' THEN
    query := 'UPDATE ${f_schema}.klarschiff_missbrauchsmeldung ' ||
      'SET datum = ' || quote_literal(new.datum) || ', vorgang = ' || new.vorgang || ', ';
    --datum_abarbeitung
    IF new.datum_abarbeitung IS NOT NULL THEN
        query := query || 'datum_abarbeitung = ' || quote_literal(new.datum_abarbeitung) || ', ';
     ELSE
        query := query || 'datum_abarbeitung = NULL, ';
    END IF;
    --datum_bestaetigung
    IF new.datum_bestaetigung IS NOT NULL THEN
        query := query || 'datum_bestaetigung = ' || quote_literal(new.datum_bestaetigung) || ' ';
     ELSE
        query := query || 'datum_bestaetigung = NULL ';
    END IF;
    query := query || 'WHERE id = ' || new.id;
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN new;

  ELSIF TG_OP = 'INSERT' THEN
    query := 'INSERT INTO ${f_schema}.klarschiff_missbrauchsmeldung (id, datum, vorgang, ' ||
      'datum_abarbeitung, datum_bestaetigung) ' ||
      'VALUES(' || new.id || ', ' || quote_literal(new.datum) || ', ' || new.vorgang || ', ';
    --datum_abarbeitung
    IF new.datum_abarbeitung IS NOT NULL THEN
        query := query || quote_literal(new.datum_abarbeitung) || ', ';
     ELSE
        query := query || 'NULL, ';
    END IF;
    --datum_bestaetigung
    IF new.datum_bestaetigung IS NOT NULL THEN
        query := query || quote_literal(new.datum_bestaetigung) || ')';
     ELSE
        query := query || 'NULL)';
    END IF;
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN new;
    
  END IF;

  PERFORM dblink_disconnect();
  RETURN NULL;
EXCEPTION WHEN others THEN
  PERFORM dblink_disconnect();
  RAISE;
END;
$BODY$ LANGUAGE plpgsql VOLATILE COST 100;

-- Owner fuer die Triggerfunktion setzen
ALTER FUNCTION klarschiff_triggerfunction_missbrauchsmeldung() OWNER TO ${b_username};

-- ggf. alten Trigger loeschen
DROP TRIGGER IF EXISTS klarschiff_trigger_missbrauchsmeldung ON klarschiff_missbrauchsmeldung CASCADE;

-- Trigger erzeugen
CREATE TRIGGER klarschiff_trigger_missbrauchsmeldung
  BEFORE INSERT OR UPDATE OR DELETE
  ON klarschiff_missbrauchsmeldung
  FOR EACH ROW EXECUTE PROCEDURE klarschiff_triggerfunction_missbrauchsmeldung();

-- Test
-- INSERT INTO klarschiff_missbrauchsmeldung (id, datum, datum_abarbeitung, datum_bestaetigung) VALUES (1000, '20130501', 9);
-- UPDATE klarschiff_missbrauchsmeldung SET datum_abarbeitung = '20130515', datum_bestaetigung = '20130527' WHERE id = 1000;
-- DELETE FROM klarschiff_missbrauchsmeldung WHERE id = 1000;


-- #######################################################################################
-- # StadtGrenze                                                                         #
-- #######################################################################################
-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_stadt_grenze()
RETURNS trigger AS $BODY$
DECLARE
  geom geometry;
  query text;

BEGIN
  PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

  IF TG_OP = 'DELETE' THEN
    query := 'DELETE FROM ${f_schema}.klarschiff_stadtgrenze_hro WHERE ogc_fid = ' || old.id;
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN old;

  ELSIF TG_OP = 'UPDATE' THEN
    geom = new.grenze;
    query := 'UPDATE ${f_schema}.klarschiff_stadtgrenze_hro ' ||
      'SET the_geom = ' || quote_literal(geom::text) || ' ' ||
      'WHERE ogc_fid = ' || new.id;
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN new;

  ELSIF TG_OP = 'INSERT' THEN
    geom = new.grenze;
    query := 'INSERT INTO ${f_schema}.klarschiff_stadtgrenze_hro (ogc_fid, the_geom) ' ||
      'VALUES (' || new.id || ', ' || quote_literal(geom::text) || ')';
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN new;
  
  END IF;
  
  PERFORM dblink_disconnect();
  RETURN NULL;
EXCEPTION WHEN others THEN
  PERFORM dblink_disconnect();
  RAISE;
END;
$BODY$ LANGUAGE plpgsql VOLATILE COST 100;

-- Owner fuer die Triggerfunktion setzen
ALTER FUNCTION klarschiff_triggerfunction_stadt_grenze() OWNER TO ${b_username};

-- ggf. alten Trigger loeschen
DROP TRIGGER IF EXISTS klarschiff_trigger_stadt_grenze ON klarschiff_stadt_grenze CASCADE;

-- Trigger erzeugen
CREATE TRIGGER klarschiff_trigger_stadt_grenze
  BEFORE INSERT OR UPDATE OR DELETE
  ON klarschiff_stadt_grenze
  FOR EACH ROW EXECUTE PROCEDURE klarschiff_triggerfunction_stadt_grenze();

-- Test
-- INSERT INTO klarschiff_stadt_grenze (id, grenze) VALUES (1000, st_geomfromtext('polygon((1 1, 0 0, 2 2, 1 1))', 25833));
-- UPDATE klarschiff_stadt_grenze SET grenze = st_geomfromtext('polygon((1 1, 0 0, 2 2, 3 3, 1 1))', 25833) WHERE id = 1000;
-- DELETE FROM klarschiff_stadt_grenze WHERE id = 1000;


-- #######################################################################################
-- # StadtteilGrenze                                                                     #
-- #######################################################################################
-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_stadtteil_grenze()
RETURNS trigger AS $BODY$
DECLARE
  geom geometry;
  query text;

BEGIN
  PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

  IF TG_OP = 'DELETE' THEN
    query := 'DELETE FROM ${f_schema}.klarschiff_stadtteile_hro WHERE ogc_fid = ' || old.id;
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN old;

  ELSIF TG_OP = 'UPDATE' THEN
    geom = new.grenze;
    query := 'UPDATE ${f_schema}.klarschiff_stadtteile_hro ' ||
      'SET bezeichnung = ' || quote_literal(new."name") || ', ' ||
      'the_geom = ' || quote_literal(geom::text) || ' ' ||
      'WHERE ogc_fid = ' || new.id;
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN new;

  ELSIF TG_OP = 'INSERT' THEN
    geom = new.grenze;
    query := 'INSERT INTO ${f_schema}.klarschiff_stadtteile_hro (ogc_fid, bezeichnung, the_geom) ' ||
      'VALUES (' || new.id || ', ' || quote_literal(new."name") || ', ' ||
      quote_literal(geom::text) || ')';
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN new;
  
  END IF;
  
  PERFORM dblink_disconnect();
  RETURN NULL;
EXCEPTION WHEN others THEN
  PERFORM dblink_disconnect();
  RAISE;
END;
$BODY$ LANGUAGE plpgsql VOLATILE COST 100;

-- Owner fuer die Triggerfunktion setzen
ALTER FUNCTION klarschiff_triggerfunction_stadtteil_grenze() OWNER TO ${b_username};

-- ggf. alten Trigger loeschen
DROP TRIGGER IF EXISTS klarschiff_trigger_stadtteil_grenze ON klarschiff_stadtteil_grenze CASCADE;

-- Trigger erzeugen
CREATE TRIGGER klarschiff_trigger_stadtteil_grenze
  BEFORE INSERT OR UPDATE OR DELETE
  ON klarschiff_stadtteil_grenze
  FOR EACH ROW EXECUTE PROCEDURE klarschiff_triggerfunction_stadtteil_grenze();

-- Test
-- INSERT INTO klarschiff_stadtteil_grenze (id, name, grenze) VALUES (1000, 'Testgrenze', st_geometryfromtext('polygon((1 1, 2 2, 3 3, 1 1))', 25833));
-- UPDATE klarschiff_stadtteil_grenze SET grenze = st_geometryfromtext('polygon((1 1, 0 0, 3 3, 1 1))', 25833), name = 'Neue Testgrenze' WHERE id = 1000;
-- DELETE FROM klarschiff_stadtteil_grenze WHERE id = 1000;


-- #######################################################################################
-- # Trashmail                                                                           #
-- #######################################################################################
-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_trashmail()
RETURNS trigger AS $BODY$
DECLARE
  query text;

BEGIN
  PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

  IF TG_OP = 'DELETE' THEN
    query := 'DELETE FROM ${f_schema}.klarschiff_trashmail_blacklist WHERE id = ' || old.id;
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN old;

  ELSIF TG_OP = 'UPDATE' THEN
    query := 'UPDATE ${f_schema}.klarschiff_trashmail_blacklist ' ||
      'SET pattern = ' || quote_literal(new.pattern) || ' ' || 
      'WHERE id = ' || new.id; 
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN new;

  ELSIF TG_OP = 'INSERT' THEN
    query := 'INSERT INTO ${f_schema}.klarschiff_trashmail_blacklist (id, pattern) ' ||
      'VALUES (' || new.id || ', ' || quote_literal(new.pattern ) || ')';
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN new;

  END IF;

  PERFORM dblink_disconnect();
  RETURN NULL;
EXCEPTION WHEN others THEN
  PERFORM dblink_disconnect();
  RAISE;
END;
$BODY$ LANGUAGE plpgsql VOLATILE COST 100;

-- Owner fuer die Triggerfunktion setzen
ALTER FUNCTION klarschiff_triggerfunction_trashmail() OWNER TO ${b_username};

-- ggf. alten Trigger loeschen
DROP TRIGGER IF EXISTS klarschiff_trigger_trashmail ON klarschiff_trashmail CASCADE;

-- Trigger erzeugen
CREATE TRIGGER klarschiff_trigger_trashmail
  BEFORE INSERT OR UPDATE OR DELETE 
  ON klarschiff_trashmail
  FOR EACH ROW EXECUTE PROCEDURE klarschiff_triggerfunction_trashmail();

-- Test
-- INSERT INTO klarschiff_trashmail (id, pattern) VALUES (426, '0815.ru');
-- UPDATE klarschiff_trashmail SET pattern = '0815a.ru' WHERE id = 426;
-- DELETE FROM klarschiff_trashmail WHERE id = 426;
  

-- #######################################################################################
-- # Unterstuetzer                                                                       #
-- #######################################################################################
-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_unterstuetzer()
RETURNS trigger AS $BODY$
DECLARE
  query text;

BEGIN
  PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

  IF TG_OP = 'DELETE' THEN
    query := 'DELETE FROM ${f_schema}.klarschiff_unterstuetzer WHERE id = ' || old.id;
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN old;

  ELSIF TG_OP = 'UPDATE' THEN
    query := 'UPDATE ${f_schema}.klarschiff_unterstuetzer ' ||
      'SET vorgang = ' || new.vorgang || ', ';
    --datum_bestaetigung
    IF new.datum_bestaetigung IS NOT NULL THEN
        query := query || 'datum = ' || quote_literal(new.datum_bestaetigung) || ' ';
     ELSE
        query := query || 'datum = NULL ';
    END IF;
    query := query || 'WHERE id = ' || new.id;
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN new;

  ELSIF TG_OP = 'INSERT' THEN
    query := 'INSERT INTO ${f_schema}.klarschiff_unterstuetzer (id, vorgang, datum) ' ||
      'VALUES (' || new.id || ', ' || new.vorgang || ', ';
    --datum_bestaetigung
    IF new.datum_bestaetigung IS NOT NULL THEN
        query := query || quote_literal(new.datum_bestaetigung) || ')';
     ELSE
        query := query || 'NULL)';
    END IF;
    RAISE DEBUG 'Query : %', query;
    EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
    PERFORM dblink_disconnect();
    RETURN new;
    
  END IF;
   
  PERFORM dblink_disconnect();
  RETURN NULL;
EXCEPTION WHEN others THEN
  PERFORM dblink_disconnect();
  RAISE;
END;
$BODY$ LANGUAGE plpgsql VOLATILE COST 100;

-- Owner fuer die Triggerfunktion setzen
ALTER FUNCTION klarschiff_triggerfunction_unterstuetzer() OWNER TO ${b_username};

-- ggf. alten Trigger loeschen
DROP TRIGGER IF EXISTS klarschiff_trigger_unterstuetzer ON klarschiff_unterstuetzer CASCADE;

-- Trigger erzeugen
CREATE TRIGGER klarschiff_trigger_unterstuetzer
  BEFORE INSERT OR UPDATE OR DELETE
  ON klarschiff_unterstuetzer
  FOR EACH ROW EXECUTE PROCEDURE klarschiff_triggerfunction_unterstuetzer();

-- Test
-- INSERT INTO klarschiff_unterstuetzer (id, datum, datum_bestaetigung, hash, vorgang) VALUES (769, '2011-07-31 19:54:23.881', NULL, '5qgfaijqe74t1k0d1knlbbl3lh', 9);
-- UPDATE klarschiff_unterstuetzer SET datum_bestaetigung = '2011-07-31 20:50:10' WHERE id = 769;
-- DELETE FROM klarschiff_unterstuetzer WHERE id = 769;


-- #######################################################################################
-- # Verlauf                                                                             #
-- #######################################################################################
-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_verlauf()
RETURNS trigger AS $BODY$
DECLARE
  query text;
BEGIN
  PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} ' ||
    'user=${f_username} password=${f_password}');

  query := 'UPDATE ${f_schema}.klarschiff_vorgang SET datum_statusaenderung = ' || CASE
    WHEN TG_OP = 'DELETE' THEN
      CASE WHEN old.typ = 'status' OR old.typ = 'erzeugt'
      THEN
        'NULL'
      ELSE
        'datum_statusaenderung'
      END ||
      ' WHERE id = ' || old.vorgang
    WHEN TG_OP IN ('INSERT', 'UPDATE') THEN
      CASE WHEN new.typ = 'status' OR new.typ = 'erzeugt' THEN
        quote_literal(new.datum)
      ELSE
        'datum_statusaenderung'
      END || ' WHERE id = ' || new.vorgang
    ELSE
      'datum_statusaenderung WHERE id IS NULL'
    END;

  RAISE DEBUG 'Query : %', query;
  EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
  PERFORM dblink_disconnect();

  IF TG_OP = 'DELETE' THEN
    RETURN old;
  ELSIF TG_OP IN ('INSERT', 'UPDATE') THEN
    RETURN new;
  ELSE
    RETURN NULL;
  END IF;

  PERFORM dblink_disconnect();
  RETURN NULL;
EXCEPTION WHEN others THEN
  PERFORM dblink_disconnect();
  RAISE;
END;
$BODY$ LANGUAGE plpgsql VOLATILE COST 100;

-- Owner fuer die Triggerfunktion setzen
ALTER FUNCTION klarschiff_triggerfunction_verlauf() OWNER TO ${b_username};

-- ggf. alten Trigger loeschen
DROP TRIGGER IF EXISTS klarschiff_trigger_verlauf ON klarschiff_verlauf CASCADE;

-- Trigger erzeugen
CREATE TRIGGER klarschiff_trigger_verlauf
  BEFORE INSERT OR UPDATE OR DELETE
  ON klarschiff_verlauf
  FOR EACH ROW EXECUTE PROCEDURE klarschiff_triggerfunction_verlauf();

-- Test
-- Get ID for valid "verlauf" from frontend: SELECT id, datum, datum_abgeschlossen FROM klarschiff.klarschiff_vorgang WHERE datum_abgeschlossen IS NULL ORDER BY id DESC;
-- INSERT INTO klarschiff_verlauf (id, typ, wert_neu, vorgang) VALUES (1000, 'status', NULL, 30);
-- INSERT INTO klarschiff_verlauf (id, typ, wert_neu, datum, vorgang) VALUES (1001, 'status', 'abgeschlossen', '20130527', 31);
-- INSERT INTO klarschiff_verlauf (id, typ, wert_neu, datum, vorgang) VALUES (1002, 'status', 'abgeschlo', '20130527', 32);
-- UPDATE klarschiff_verlauf SET typ = 'status', wert_neu = 'abgeschlossen', datum = now() WHERE id = 1000;
-- DELETE FROM klarschiff_verlauf WHERE id IN (1000, 1001, 1002);


-- #######################################################################################
-- # Vorgang                                                                             #
-- #######################################################################################
-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_vorgang()
RETURNS trigger AS $BODY$
DECLARE
  query text;

BEGIN
  PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

  query := CASE TG_OP
    WHEN 'DELETE' THEN
      'DELETE FROM ${f_schema}.klarschiff_vorgang WHERE id = ' || old.id
    WHEN 'UPDATE' THEN
      'UPDATE ${f_schema}.klarschiff_vorgang ' ||
      'SET datum = ' || quote_literal(new.datum::varchar(50)) || ', ' ||
      'vorgangstyp = ' || quote_literal(new.typ) || ', ' ||
      'the_geom = ' || quote_literal(new.ovi::text) || ', ' ||
      'status = ' || quote_literal(new.status) || ', ' ||
      'kategorieid = ' || new.kategorie || ', ' ||
      --betreff
      'titel = ' || CASE 
        WHEN new.betreff_freigabe_status = 'extern' AND new.betreff IS NOT NULL AND new.betreff <> '' THEN
          quote_literal(new.betreff)
        ELSE
          'NULL' 
        END || ', ' ||
      --details
      'details = ' || CASE
        WHEN new.details_freigabe_status = 'extern' AND new.details IS NOT NULL AND new.details <> '' THEN
          quote_literal(new.details)
        ELSE
          'NULL'
        END || ', ' ||
      --statusKommentar
      'bemerkung = ' || CASE
        WHEN new.status_kommentar IS NOT NULL THEN
          quote_literal(new.status_kommentar)
        ELSE
          'NULL'
        END || ', ' ||
      --fotoNormal
      'foto_normal = ' || CASE
        WHEN new.foto_freigabe_status = 'extern' AND new.foto_normal IS NOT NULL AND new.foto_normal <> '' THEN
          quote_literal(new.foto_normal)
        ELSE
          'NULL'
        END || ', ' ||
      --fotoThumb
      'foto_thumb = ' || CASE
        WHEN new.foto_freigabe_status = 'extern' AND new.foto_thumb IS NOT NULL AND new.foto_thumb <> '' THEN
          quote_literal(new.foto_thumb)
        ELSE
          'NULL'
        END || ', ' ||
      --fotoVorhanden
      'foto_vorhanden = ' || CASE 
        WHEN length(new.foto_normal) IS NOT NULL AND length(new.foto_thumb) IS NOT NULL THEN
          'TRUE'
        ELSE
          'FALSE'
        END || ', ' ||
      --fotoFreigegeben
      'foto_freigegeben = ' || CASE 
        WHEN new.foto_freigabe_status = 'extern' THEN
          'TRUE'
        ELSE
          'FALSE'
        END || ', ' ||
      --betreffVorhanden
      'betreff_vorhanden = ' || CASE
        WHEN new.betreff IS NOT NULL AND new.betreff <> '' THEN
          'TRUE'
        ELSE
          'FALSE'
        END || ', ' ||
      --betreffFreigegeben
      'betreff_freigegeben = ' || CASE
        WHEN new.betreff_freigabe_status = 'extern' THEN
          'TRUE'
        ELSE
          'FALSE'
        END || ', ' ||
      --detailsVorhanden
      'details_vorhanden = ' || CASE
        WHEN new.details IS NOT NULL AND new.details <> '' THEN
          'TRUE'
        ELSE
          'FALSE'
        END || ', ' ||
      --detailsFreigegeben
      'details_freigegeben = ' || CASE
        WHEN new.details_freigabe_status = 'extern' THEN
          'TRUE'
        ELSE
          'FALSE'
        END || ', ' ||
      --archiviert
      'archiviert = ' || CASE
        WHEN new.archiviert IS NOT NULL THEN
          new.archiviert
         ELSE
          'FALSE'
        END || ', ' ||
      --zustaendigkeit
      'zustaendigkeit = ' || CASE 
          WHEN new.zustaendigkeit_frontend IS NOT NULL AND new.zustaendigkeit_frontend <> '' THEN
            quote_literal(new.zustaendigkeit_frontend)
          ELSE
            'NULL'
        END || ' ' ||
      'WHERE id = ' || new.id
    WHEN 'INSERT' THEN
      'INSERT INTO ${f_schema}.klarschiff_vorgang (id, datum, vorgangstyp, ' ||
        'the_geom, status, kategorieid, titel, details, bemerkung, foto_normal, ' ||
        'foto_thumb, foto_vorhanden, foto_freigegeben, betreff_vorhanden, ' ||
        'betreff_freigegeben, details_vorhanden, details_freigegeben, archiviert, zustaendigkeit) ' ||
      'VALUES (' || new.id ||', ' || quote_literal(new.datum::varchar(50)) || ', ' ||
        quote_literal(new.typ) || ', ' || quote_literal(new.ovi::text) || ', ' ||
        quote_literal(new.status) || ', ' || new.kategorie || ', ' ||
        --betreff
        CASE 
          WHEN new.betreff_freigabe_status = 'extern' AND new.betreff IS NOT NULL AND new.betreff <> '' THEN
            quote_literal(new.betreff)
          ELSE
            'NULL'
        END || ', ' ||
        --details
        CASE 
          WHEN new.details_freigabe_status = 'extern' AND new.details IS NOT NULL AND new.details <> '' THEN
            quote_literal(new.details)
           ELSE
            'NULL'
        END || ', ' ||
        --statusKommentar
        CASE
          WHEN new.status_kommentar IS NOT NULL THEN
            quote_literal(new.status_kommentar)
          ELSE
            'NULL'
        END || ', ' ||
        --fotoNormal
        CASE
          WHEN new.foto_normal IS NOT NULL AND new.foto_freigabe_status = 'extern' THEN
            quote_literal(new.foto_normal)
          ELSE
            'NULL'
        END || ', ' ||
        --fotoThumb
        CASE
          WHEN new.foto_thumb IS NOT NULL AND new.foto_freigabe_status = 'extern' THEN
            quote_literal(new.foto_thumb)
          ELSE
            'NULL'
        END || ', ' ||
        --fotoVorhanden
        CASE
          WHEN length(new.foto_normal) IS NOT NULL AND length(new.foto_thumb) IS NOT NULL THEN
            'TRUE'
          ELSE
            'FALSE'
        END || ', ' ||
        --fotoFreigegeben
        CASE new.foto_freigabe_status
          WHEN 'extern' THEN
            'TRUE'
          ELSE
            'FALSE'
        END || ', ' ||
        --betreffVorhanden
        CASE
          WHEN new.betreff IS NOT NULL AND new.betreff <> '' THEN
            'TRUE'
          ELSE
            'FALSE'
        END || ', ' ||
        --betreffFreigegeben
        CASE new.betreff_freigabe_status
          WHEN 'extern' THEN
            'TRUE'
          ELSE
            'FALSE'
        END || ', ' ||
        --detailsVorhanden
        CASE 
          WHEN new.details IS NOT NULL AND new.details <> '' THEN
            'TRUE'
          ELSE
            'FALSE'
        END || ', ' ||
        --detailsFreigegeben
        CASE new.details_freigabe_status
          WHEN 'extern' THEN
            'TRUE'
          ELSE
            'FALSE'
        END || ', ' ||
        --archiviert
        CASE 
          WHEN new.archiviert IS NOT NULL THEN
            new.archiviert
          ELSE
            'FALSE'
        END || ', ' ||
        --zustaendigkeit
        CASE 
          WHEN new.zustaendigkeit_frontend IS NOT NULL AND new.zustaendigkeit_frontend <> '' THEN
            quote_literal(new.zustaendigkeit_frontend)
          ELSE
            'NULL'
        END || ')'
    ELSE
      'SELECT 1'
  END;

  RAISE DEBUG 'Query : %', query;
  EXECUTE 'SELECT dblink_exec(' || quote_literal(query) || ');';
  PERFORM dblink_disconnect();

  IF TG_OP = 'DELETE' THEN 
    RETURN old;
  ELSIF TG_OP IN ('INSERT', 'UPDATE') THEN
    RETURN new;
  ELSE
    RETURN NULL;
  END IF;
EXCEPTION WHEN others THEN
  PERFORM dblink_disconnect();
  RAISE;
END;
$BODY$ LANGUAGE plpgsql VOLATILE COST 100;

-- Owner fuer die Triggerfunktion setzen
ALTER FUNCTION klarschiff_triggerfunction_vorgang() OWNER TO ${b_username};

-- ggf. alten Trigger loeschen
DROP TRIGGER IF EXISTS klarschiff_trigger_vorgang ON klarschiff_vorgang CASCADE;

-- Trigger erzeugen
CREATE TRIGGER klarschiff_trigger_vorgang
  BEFORE INSERT OR UPDATE OR DELETE
  ON klarschiff_vorgang
  FOR EACH ROW EXECUTE PROCEDURE klarschiff_triggerfunction_vorgang();


-- #######################################################################################
-- # automatische Zuordnung einer Adresse für einen Vorgang                              #
-- #######################################################################################

-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_adresse()
RETURNS trigger AS $BODY$
DECLARE
  ergebnis_adresse record;
  ergebnis_strasse record;

BEGIN
  -- nur ausführen bei einem neuen Vorgang oder beim Aktualisieren eines Vorgangs (hier 
  -- aber nur, wenn sich die Geometrie ändert oder wenn im Adressfeld nix drinsteht!)
  IF (TG_OP = 'INSERT' OR (TG_OP = 'UPDATE' AND (NEW.adresse IS null OR NEW.adresse = '' OR NEW.adresse = 'nicht zuordenbar'))
    OR (TG_OP = 'UPDATE' AND (encode(NEW.ovi, 'base64') <> encode(OLD.ovi, 'base64'))))
  THEN
    -- Verbindung zur Adresstabelle aufbauen (hierfür verwendet man idealerweise die
    -- Tabelle für die Standortsuche, da die eh da ist)
    PERFORM dblink_connect('standortsuche_verbindung','hostaddr=${f_host} port=${f_port} ' ||
      'dbname=standortsuche user=standortsuche password=standortsuche');

    -- räumliche Abfrage durchführen, die genau einen Datensatz (oder NULL) als Ergebnis 
    -- liefert, der auch gleich in die oben deklarierte Variable geschrieben wird
    SELECT (standortsuche.strasse || ' ' || standortsuche.hausnummer || 
      COALESCE(standortsuche.hausnummerzusatz, '') || ' ' || 
      COALESCE(standortsuche.zusatz, '')) AS adresse, 
      ST_Distance(standortsuche.geom, NEW.ovi) AS distanz
    INTO ergebnis
    FROM klarschiff_vorgang, 
      dblink('standortsuche_verbindung', 'SELECT strasse, hausnummer, hausnummerzusatz, ' ||
        'zusatz, geom FROM standortsuche') AS standortsuche(strasse varchar, 
        hausnummer varchar, hausnummerzusatz varchar, zusatz varchar, geom geometry)
    WHERE standortsuche.hausnummer IS NOT null
      AND ST_DWithin(standortsuche.geom, NEW.ovi, 100) 
    ORDER BY ST_Distance(standortsuche.geom, NEW.ovi) LIMIT 1;

    -- räumliche Abfrage durchführen, die genau einen Datensatz (oder NULL) als Ergebnis 
    -- liefert, der auch gleich in die oben deklarierte Variable geschrieben wird
    SELECT standortsuche.strasse, 
      ST_Distance(standortsuche.geom, NEW.ovi) AS distanz
    INTO ergebnis_strasse
    FROM klarschiff_vorgang, 
      dblink('standortsuche_verbindung', 'SELECT strasse, geom FROM standortsuche.standortsuche WHERE strasse_id IS NOT NULL and hausnummer IS NULL') AS standortsuche(strasse varchar, geom geometry)
    WHERE ST_DWithin(standortsuche.geom, NEW.ovi, 100) 
    ORDER BY ST_Distance(standortsuche.geom, NEW.ovi) LIMIT 1;

    -- Verbindung zur Adresstabelle wieder schließen
    PERFORM dblink_disconnect('standortsuche_verbindung');
    
    -- falls das Ergebnis für Adressen nicht NULL ist und die Distanz der ermittelten Adresse zum 
    -- Vorgang kleiner gleich 50 m: Adresse zuweisen
    IF ergebnis_adresse.adresse IS NOT NULL AND ergebnis_adresse.distanz <= 50 THEN
      NEW.adresse := ergebnis_adresse.adresse;
    -- ansonsten, falls das Ergebnis für Adressen nicht NULL ist und die Distanz der ermittelten Adresse zum
    -- Vorgang kleiner gleich 100 m: "bei " + Adresse zuweisen
    ELSIF ergebnis_adresse.adresse IS NOT NULL AND ergebnis_adresse.distanz <= 100 THEN
      NEW.adresse := 'bei ' || ergebnis_adresse.adresse;
    -- ansonsten, falls das Ergebnis für Straßen nicht NULL ist und die Distanz der ermittelten Straße zum 
    -- Vorgang kleiner gleich 50 m: Straße zuweisen
    ELSIF ergebnis_strasse.strasse IS NOT NULL AND ergebnis_strasse.distanz <= 50 THEN
      NEW.adresse := ergebnis_strasse.strasse;
    -- ansonsten, falls das Ergebnis für Straßen nicht NULL ist und die Distanz der ermittelten Straße zum
    -- Vorgang kleiner gleich 100 m: "bei " + Straße zuweisen
    ELSIF ergebnis_strasse.strasse IS NOT NULL AND ergebnis_strasse.distanz <= 100 THEN
      NEW.adresse := 'bei ' || ergebnis_strasse.strasse;
    -- ansonsten: "nicht zuordenbar" zuweisen
    ELSE
      NEW.adresse := 'nicht zuordenbar';
    END IF;
  END IF;
  RETURN NEW;

EXCEPTION WHEN others THEN
  RAISE;
END;
$BODY$ LANGUAGE plpgsql VOLATILE COST 100;

-- Owner fuer die Triggerfunktion setzen
ALTER FUNCTION klarschiff_triggerfunction_adresse() OWNER TO ${b_username};

-- ggf. alten Trigger loeschen
DROP TRIGGER IF EXISTS klarschiff_trigger_adresse ON klarschiff_vorgang CASCADE;

-- Trigger erzeugen
CREATE TRIGGER klarschiff_trigger_adresse
  BEFORE INSERT OR UPDATE
  ON klarschiff_vorgang
  FOR EACH ROW EXECUTE PROCEDURE klarschiff_triggerfunction_adresse();
  


-- #####################################################################################################
-- # automatische Zuordnung der Information über das Eigentum des Flürstücks, in dem ein Vorgang liegt #
-- #####################################################################################################

-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_flurstueckseigentum()
RETURNS trigger AS $BODY$
DECLARE
  ergebnis record;

BEGIN
  -- nur ausführen bei einem neuen Vorgang oder beim Aktualisieren eines Vorgangs (hier 
  -- aber nur, wenn sich die Geometrie ändert oder wenn im Flurstückseigentumsfeld nix drinsteht!)
  IF (TG_OP = 'INSERT' OR (TG_OP = 'UPDATE' AND (NEW.flurstueckseigentum IS NULL OR NEW.flurstueckseigentum = ''))
    OR (TG_OP = 'UPDATE' AND (encode(NEW.ovi, 'base64') <> encode(OLD.ovi, 'base64'))))
  THEN
    -- Verbindung zur Flurstückseigentumstabelle aufbauen
    PERFORM dblink_connect('flurstueckseigentum_verbindung','hostaddr=${f_host} port=${f_port} ' ||
      'dbname=daten_privat user=lesen password=selen');

    -- räumliche Abfrage durchführen, die genau einen Datensatz (oder NULL) als Ergebnis 
    -- liefert, der auch gleich in die oben deklarierte Variable geschrieben wird
    SELECT eigentuemer.eigentum AS eigentum
    INTO ergebnis
    FROM klarschiff_vorgang, 
      dblink('flurstueckseigentum_verbindung', 'SELECT eigentuemer, geom FROM alk.eigentuemer_klarschiff') AS eigentuemer(eigentum varchar, geom geometry)
    WHERE ST_Covers(geom, NEW.ovi) LIMIT 1;

    -- Verbindung zur Flurstückseigentumstabelle wieder schließen
    PERFORM dblink_disconnect('flurstueckseigentum_verbindung');

    -- wenn das Ergebnis nicht NULL ist: Information über das Eigentum des Flürstücks zuweisen
    IF ergebnis.eigentum IS NOT NULL THEN
      NEW.flurstueckseigentum := ergebnis.eigentum;
      -- ansonsten: "nicht zuordenbar" zuweisen
    ELSE
      NEW.flurstueckseigentum := 'nicht zuordenbar';
    END IF;
  END IF;
  RETURN NEW;

EXCEPTION WHEN others THEN
  RAISE;
END;
$BODY$ LANGUAGE plpgsql VOLATILE COST 100;

-- Owner fuer die Triggerfunktion setzen
ALTER FUNCTION klarschiff_triggerfunction_flurstueckseigentum() OWNER TO ${b_username};

-- ggf. alten Trigger loeschen
DROP TRIGGER IF EXISTS klarschiff_trigger_flurstueckseigentum ON klarschiff_vorgang CASCADE;

-- Trigger erzeugen
CREATE TRIGGER klarschiff_trigger_flurstueckseigentum
  BEFORE INSERT OR UPDATE
  ON klarschiff_vorgang
  FOR EACH ROW EXECUTE PROCEDURE klarschiff_triggerfunction_flurstueckseigentum();
