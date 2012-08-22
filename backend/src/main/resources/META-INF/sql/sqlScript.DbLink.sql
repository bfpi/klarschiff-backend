-- @author Stefan Audersch (Fraunhofer IGD)
-- @author Peter Koenig (WhereGroup)
-- Hinweis: Variablen werden vor der Ausfuehrung durch die entsprechenden Werte ersetzt

-- #######################################################################################
-- # EnumVorgangStatus                                                                   #
-- #######################################################################################
	
-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_enum_vorgang_status() RETURNS trigger AS
$BODY$
DECLARE
	query text;

BEGIN
	PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

	IF (TG_OP = 'DELETE') THEN
    
		query := 'DELETE FROM ${f_schema}.klarschiff_status WHERE id='''''||old.id||'''''';
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
      
		PERFORM dblink_disconnect();
		RETURN old;

	ELSIF (TG_OP = 'UPDATE') THEN
    
		query := '
			UPDATE ${f_schema}.klarschiff_status 
			SET 
				"name"='''''||new."text"||''''', 
				nid='|| new.ordinal||' 
			WHERE 
				id='''''||new.id||'''''';
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
    
		PERFORM dblink_disconnect();
		RETURN new;

	ELSIF (TG_OP = 'INSERT') THEN
	
		query := '
			INSERT INTO ${f_schema}.klarschiff_status (id, "name", nid) 
			VALUES (
				'''''||new.id||''''', 
				'''''||new."text"||''''', 
				'||new.ordinal||'
			)';
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
		
		PERFORM dblink_disconnect();
		RETURN new;
	
	END IF;
	
	PERFORM dblink_disconnect();
	RETURN NULL;
EXCEPTION WHEN others THEN
	PERFORM dblink_disconnect();
	RAISE EXCEPTION '(%)', SQLERRM;
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
	FOR EACH ROW
	EXECUTE PROCEDURE klarschiff_triggerfunction_enum_vorgang_status();

-- Test
--INSERT INTO klarschiff_enum_vorgang_status (id, "text", ordinal) values ('test', 'test', 100);
--UPDATE klarschiff_enum_vorgang_status SET text='Test' WHERE id='test';
--DELETE FROM klarschiff_enum_vorgang_status WHERE id = 'test';

	
	
-- #######################################################################################
-- # EnumVorgangTyp                                                                      #
-- #######################################################################################
	
-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_enum_vorgang_typ() RETURNS trigger AS
$BODY$
DECLARE
	query text;

BEGIN
	PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

	IF (TG_OP = 'DELETE') THEN
    
		query := 'DELETE FROM ${f_schema}.klarschiff_vorgangstyp WHERE id='''''||old.id||'''''';
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
      
		PERFORM dblink_disconnect();
		RETURN old;

	ELSIF (TG_OP = 'UPDATE') THEN
    
		query := '
			UPDATE ${f_schema}.klarschiff_vorgangstyp 
			SET 
				"name"='''''||new."text"||''''', 
				ordinal='|| new.ordinal||' 
			WHERE id='''''||new.id||'''''';
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
    
		PERFORM dblink_disconnect();
		RETURN new;

	ELSIF (TG_OP = 'INSERT') THEN
	
		query := '
			INSERT INTO ${f_schema}.klarschiff_vorgangstyp (id, "name", ordinal) 
			VALUES (
				'''''||new.id||''''', 
				'''''||new."text"||''''', 
				'||new.ordinal||'
			)';
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
		
		PERFORM dblink_disconnect();
		RETURN new;
	
	END IF;
	
	PERFORM dblink_disconnect();
	RETURN NULL;
EXCEPTION WHEN others THEN
	PERFORM dblink_disconnect();
	RAISE EXCEPTION '(%)', SQLERRM;
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
	FOR EACH ROW
	EXECUTE PROCEDURE klarschiff_triggerfunction_enum_vorgang_typ();

-- Test
--INSERT INTO klarschiff_enum_vorgang_typ (id, "text", ordinal) values ('test', 'test', 100);
--UPDATE klarschiff_enum_vorgang_typ SET text='Test' WHERE id='test';
--DELETE FROM klarschiff_enum_vorgang_typ WHERE id = 'test';
	
	
	
-- #######################################################################################
-- # GeoRss                                                                              #
-- #######################################################################################

-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_geo_rss() RETURNS trigger AS
$BODY$
DECLARE
	query text;

BEGIN
	PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

	IF (TG_OP = 'DELETE') THEN
  
		query := 'DELETE FROM ${f_schema}.klarschiff_geo_rss WHERE id='||old.id;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
		
		PERFORM dblink_disconnect();
		RETURN old;

	ELSIF (TG_OP = 'UPDATE') THEN
  
		query := '
			UPDATE ${f_schema}.klarschiff_geo_rss 
			SET 
				klarschiff_geo_rss_fid='||new.id||',
				ideen='||new.ideen||', 
				probleme='||new.probleme||', 
				ideen_kategorien='''''||new.ideen_kategorien||''''', 
				probleme_kategorien='''''||new.probleme_kategorien||''''', 
				the_geom='''''||new.ovi::text||''''' 
			WHERE id='||new.id;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
		
		PERFORM dblink_disconnect();
		RETURN new;

	ELSIF (TG_OP = 'INSERT') THEN
      
		query := '
			INSERT INTO ${f_schema}.klarschiff_geo_rss (id, klarschiff_geo_rss_fid, ideen, probleme, ideen_kategorien, probleme_kategorien, the_geom) 
			VALUES (
				'||new.id||', 
				'||new.id||', 
				'||new.ideen||', 
				'||new.probleme||', 
				'''''||new.ideen_kategorien||''''', 
				'''''||new.probleme_kategorien||''''', 
				'''''||new.ovi::text||'''''
			)';
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
		
		PERFORM dblink_disconnect();
		RETURN new;
   
	END IF;
   
	PERFORM dblink_disconnect();
	RETURN NULL;
EXCEPTION WHEN others THEN
	PERFORM dblink_disconnect();
	RAISE EXCEPTION '(%)', SQLERRM;
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
	FOR EACH ROW
	EXECUTE PROCEDURE klarschiff_triggerfunction_geo_rss();

-- Test
--INSERT INTO klarschiff_geo_rss (id, ideen, probleme, ideen_kategorien, probleme_kategorien, ovi) VALUES (1000, true, true, '68', '1', '0106000020E96400000100000001030000000100000006000000012CB8D0E6D31241F1D9156712E55641E89B97B331E7124173DD6F1C8EE456412B634BB459D31241BB5BFEB862E15641ABA315B506BD12411BF24F8ECCE15641F5EC5E5FC6C71241BAB1EA588EE35641012CB8D0E6D31241F1D9156712E55641');
--UPDATE klarschiff_geo_rss SET ideen_kategorien = '68,73' WHERE id = 1000;
--DELETE FROM klarschiff_geo_rss WHERE id = 1000;

	
	
-- #######################################################################################
-- # Kategorie                                                                           #
-- #######################################################################################

-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_kategorie() RETURNS trigger AS
$BODY$
DECLARE
	query text;

BEGIN
	PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

	IF (TG_OP = 'DELETE') THEN
  
		query := 'DELETE FROM ${f_schema}.klarschiff_kategorie WHERE id='||old.id;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
		
		PERFORM dblink_disconnect();
		RETURN old;

	ELSIF (TG_OP = 'UPDATE') THEN
  
		query := '
			UPDATE ${f_schema}.klarschiff_kategorie 
			SET "name"='''''||new."name"||''''', ';
		--parent
		IF new.parent IS NOT NULL THEN
  			query := query||'parent='||new.parent||', ';
 		ELSE
  			query := query||'parent=NULL, ';
		END IF;
		--typ
		IF new.typ IS NOT NULL THEN
  			query := query||'vorgangstyp='''''||new.typ||''''', ';
 		ELSE
  			query := query||'vorgangstyp=NULL, ';
		END IF;
		--naehere_beschreibung_notwendig
		IF new.naehere_beschreibung_notwendig IS NOT NULL THEN
  			query := query||'naehere_beschreibung_notwendig='''''||new.naehere_beschreibung_notwendig||''''', ';
 		ELSE
  			query := query||'naehere_beschreibung_notwendig=NULL, ';
		END IF;
		--aufforderung  --########### @deprecated ############
		IF (new.naehere_beschreibung_notwendig IS NULL OR new.naehere_beschreibung_notwendig='keine') THEN
  			query := query||'aufforderung=FALSE ';
 		ELSE
  			query := query||'aufforderung=TRUE ';
		END IF;
		query := query||'WHERE id='||new.id;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
		
		PERFORM dblink_disconnect();
		RETURN new;

	ELSIF (TG_OP = 'INSERT') THEN
      
		query := '
			INSERT INTO ${f_schema}.klarschiff_kategorie (id, "name", parent, vorgangstyp, naehere_beschreibung_notwendig, aufforderung) 
			VALUES ('||new.id||', '''''||new."name"||''''', '; 
		--parent
		IF new.parent IS NOT NULL THEN
  			query := query||new.parent||', ';
 		ELSE
  			query := query||'NULL, ';
		END IF;
		--typ
		IF new.typ IS NOT NULL THEN
  			query := query||''''''||new.typ||''''', ';
 		ELSE
  			query := query||'NULL, ';
		END IF;
		--naehere_beschreibung_notwendig
		IF new.naehere_beschreibung_notwendig IS NOT NULL THEN
  			query := query||''''''||new.naehere_beschreibung_notwendig||''''', ';
 		ELSE
  			query := query||'NULL, ';
		END IF;
		--aufforderung  --########### @deprecated ############
		IF (new.naehere_beschreibung_notwendig IS NULL OR new.naehere_beschreibung_notwendig='keine') THEN
  			query := query||'FALSE)';
 		ELSE
  			query := query||'TRUE)';
		END IF;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
		
		PERFORM dblink_disconnect();
		RETURN new;
   
	END IF;
   
	PERFORM dblink_disconnect();
	RETURN NULL;
EXCEPTION WHEN others THEN
	PERFORM dblink_disconnect();
	RAISE EXCEPTION '(%)', SQLERRM;
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
	FOR EACH ROW
	EXECUTE PROCEDURE klarschiff_triggerfunction_kategorie();

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

CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_missbrauchsmeldung() RETURNS trigger AS
$BODY$
DECLARE
	query text;

BEGIN
	PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

	IF (TG_OP = 'DELETE') THEN

		query := 'DELETE FROM ${f_schema}.klarschiff_missbrauchsmeldung WHERE id='||old.id;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
  
		PERFORM dblink_disconnect();
		RETURN old;

	ELSIF (TG_OP = 'UPDATE') THEN

		query := '
			UPDATE ${f_schema}.klarschiff_missbrauchsmeldung 
			SET datum='''''||new.datum||''''', vorgang='||new.vorgang||', ';
		--datum_abarbeitung
		IF new.datum_abarbeitung IS NOT NULL THEN
  			query := query||'datum_abarbeitung='''''||new.datum_abarbeitung||''''', ';
 		ELSE
  			query := query||'datum_abarbeitung=NULL, ';
		END IF;
		--datum_bestaetigung
		IF new.datum_bestaetigung IS NOT NULL THEN
  			query := query||'datum_bestaetigung='''''||new.datum_bestaetigung||''''' ';
 		ELSE
  			query := query||'datum_bestaetigung=NULL ';
		END IF;
		query := query||' WHERE id='||new.id;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
   
		PERFORM dblink_disconnect();
		RETURN new;

	ELSIF (TG_OP = 'INSERT') THEN
	
		query := '
			INSERT INTO ${f_schema}.klarschiff_missbrauchsmeldung (id, datum, vorgang, datum_abarbeitung, datum_bestaetigung) 
			VALUES('||new.id||', '''''||new.datum||''''', '||new.vorgang||', ';
		--datum_abarbeitung
		IF new.datum_abarbeitung IS NOT NULL THEN
  			query := query||''''''||new.datum_abarbeitung||''''', ';
 		ELSE
  			query := query||'NULL, ';
		END IF;
		--datum_bestaetigung
		IF new.datum_bestaetigung IS NOT NULL THEN
  			query := query||''''''||new.datum_bestaetigung||''''')';
 		ELSE
  			query := query||'NULL)';
		END IF;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';

		PERFORM dblink_disconnect();
		RETURN new;
		
	END IF;

	PERFORM dblink_disconnect();
	RETURN NULL;
EXCEPTION WHEN others THEN
	PERFORM dblink_disconnect();
	RAISE EXCEPTION '(%)', SQLERRM;
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
	FOR EACH ROW
	EXECUTE PROCEDURE klarschiff_triggerfunction_missbrauchsmeldung();

-- Test
--INSERT INTO klarschiff_missbrauchsmeldung () VALUES ();
--UPDATE klarschiff_missbrauchsmeldung SET  WHERE id=;
--DELETE FROM klarschiff_missbrauchsmeldung WHERE id=;

	

-- #######################################################################################
-- # StadtGrenze                                                                         #
-- #######################################################################################

-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_stadt_grenze() RETURNS trigger AS
$BODY$
DECLARE
	geom geometry;
	query text;

BEGIN
	PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

	IF (TG_OP = 'DELETE') THEN
    
		query := 'DELETE FROM ${f_schema}.klarschiff_stadtgrenze_hro WHERE ogc_fid='''''||old.id||'''''';
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
      
		PERFORM dblink_disconnect();
		RETURN old;

	ELSIF (TG_OP = 'UPDATE') THEN
    
		geom=new.grenze;
		query := '
			UPDATE ${f_schema}.klarschiff_stadtgrenze_hro 
			SET 
				the_geom='''''||geom::text||''''' 
			WHERE ogc_fid='||new.id;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
    
		PERFORM dblink_disconnect();
		RETURN new;

	ELSIF (TG_OP = 'INSERT') THEN
	
		geom=new.grenze;
		query := '
			INSERT INTO ${f_schema}.klarschiff_stadtgrenze_hro (ogc_fid, the_geom) 
			VALUES (
				'||new.id||', 
				'''''||geom::text||'''''
			)';
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
		
		PERFORM dblink_disconnect();
		RETURN new;
	
	END IF;
	
	PERFORM dblink_disconnect();
	RETURN NULL;
EXCEPTION WHEN others THEN
	PERFORM dblink_disconnect();
	RAISE EXCEPTION '(%)', SQLERRM;
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
	FOR EACH ROW
	EXECUTE PROCEDURE klarschiff_triggerfunction_stadt_grenze();

-- Test

	

-- #######################################################################################
-- # StadtteilGrenze                                                                     #
-- #######################################################################################

-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_stadtteil_grenze() RETURNS trigger AS
$BODY$
DECLARE
	geom geometry;
	query text;

BEGIN
	PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

	IF (TG_OP = 'DELETE') THEN
    
		query := 'DELETE FROM ${f_schema}.klarschiff_stadtteile_hro WHERE ogc_fid='''''||old.id||'''''';
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
      
		PERFORM dblink_disconnect();
		RETURN old;

	ELSIF (TG_OP = 'UPDATE') THEN
    
		geom=new.grenze;
		query := '
			UPDATE ${f_schema}.klarschiff_stadtteile_hro 
			SET 
				bezeichnung='''''||new."name"||''''',  
				the_geom='''''||geom::text||''''' 
			WHERE ogc_fid='||new.id;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
    
		PERFORM dblink_disconnect();
		RETURN new;

	ELSIF (TG_OP = 'INSERT') THEN
	
		geom=new.grenze;
		query := '
			INSERT INTO ${f_schema}.klarschiff_stadtteile_hro (ogc_fid, bezeichnung, the_geom) 
			VALUES (
				'||new.id||', 
				'''''||new."name"||''''', 
				'''''||geom::text||'''''
			)';
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
		
		PERFORM dblink_disconnect();
		RETURN new;
	
	END IF;
	
	PERFORM dblink_disconnect();
	RETURN NULL;
EXCEPTION WHEN others THEN
	PERFORM dblink_disconnect();
	RAISE EXCEPTION '(%)', SQLERRM;
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
	FOR EACH ROW
	EXECUTE PROCEDURE klarschiff_triggerfunction_stadtteil_grenze();

-- Test
	
	

-- #######################################################################################
-- # Trashmail                                                                           #
-- #######################################################################################

-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_trashmail() RETURNS trigger AS
$BODY$
DECLARE
	query text;

BEGIN
	PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

	IF (TG_OP = 'DELETE') THEN
		query := 'DELETE FROM ${f_schema}.klarschiff_trashmail_blacklist WHERE id='||old.id;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
		
		PERFORM dblink_disconnect();
		RETURN old;

    ELSIF (TG_OP = 'UPDATE') THEN
        
		query := 'UPDATE ${f_schema}.klarschiff_trashmail_blacklist SET pattern='''''||new.pattern||''''' WHERE id='||new.id; 
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
		
		PERFORM dblink_disconnect();
		RETURN new;

    ELSIF (TG_OP = 'INSERT') THEN

		query := 'INSERT INTO ${f_schema}.klarschiff_trashmail_blacklist (id, pattern) VALUES ('||new.id||', '''''||new.pattern||''''')';
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';

		PERFORM dblink_disconnect();
		RETURN new;

	END IF;

	PERFORM dblink_disconnect();
	RETURN NULL;
EXCEPTION WHEN others THEN
	PERFORM dblink_disconnect();
	RAISE EXCEPTION '(%)', SQLERRM;
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
	FOR EACH ROW 
	EXECUTE PROCEDURE klarschiff_triggerfunction_trashmail();

-- Test
--INSERT INTO klarschiff_trashmail (id, pattern) VALUES (426, '0815.ru');
--UPDATE klarschiff_trashmail SET pattern='0815a.ru' WHERE id=426;
--DELETE FROM klarschiff_trashmail WHERE id=426;
	
	

-- #######################################################################################
-- # Unterstuetzer                                                                       #
-- #######################################################################################

-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_unterstuetzer() RETURNS trigger AS
$BODY$
DECLARE
	query text;

BEGIN
	PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

	IF (TG_OP = 'DELETE') THEN
	
		query := 'DELETE FROM ${f_schema}.klarschiff_unterstuetzer WHERE id='||old.id;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
		
		PERFORM dblink_disconnect();
		RETURN old;

    ELSIF (TG_OP = 'UPDATE') THEN
    
		query := '
			UPDATE ${f_schema}.klarschiff_unterstuetzer 
			SET vorgang='||new.vorgang||', ';
		--datum_bestaetigung
		IF new.datum_bestaetigung IS NOT NULL THEN
  			query := query||'datum='''''||new.datum_bestaetigung||''''' ';
 		ELSE
  			query := query||'datum=NULL ';
		END IF;
		query := query||' WHERE id='||new.id;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';

		PERFORM dblink_disconnect();
		RETURN new;

	ELSIF (TG_OP = 'INSERT') THEN
    
		query := '
			INSERT INTO ${f_schema}.klarschiff_unterstuetzer (id, vorgang, datum) 
			VALUES ('||new.id||', '||new.vorgang||', ';
		--datum_bestaetigung
		IF new.datum_bestaetigung IS NOT NULL THEN
  			query := query||''''''||new.datum_bestaetigung||''''')';
 		ELSE
  			query := query||'NULL)';
		END IF;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';

		PERFORM dblink_disconnect();
		RETURN new;
		
	END IF;
   
	PERFORM dblink_disconnect();
	RETURN NULL;
EXCEPTION WHEN others THEN
	PERFORM dblink_disconnect();
	RAISE EXCEPTION '(%)', SQLERRM;
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
	FOR EACH ROW
	EXECUTE PROCEDURE klarschiff_triggerfunction_unterstuetzer();

-- Test
--INSERT INTO klarschiff_unterstuetzer (id, datum, datum_bestaetigung, hash, vorgang) VALUES (769, '2011-07-31 19:54:23.881',NULL,'5qgfaijqe74t1k0d1knlbbl3lh',9);
--INSERT INTO klarschiff_unterstuetzer (id, datum, datum_bestaetigung, hash, vorgang) VALUES (770, '2011-07-31 20:44:06.462', '2011-07-31 20:50:10', '3o9lqmb9g3ria2lh33hiovjh2j', 9);
--INSERT INTO klarschiff_unterstuetzer (id, datum, datum_bestaetigung, hash, vorgang) VALUES (769, '2011-07-31 19:54:23.881',NULL,'5qgfaijqe74t1k0d1knlbbl3lh',9);
--UPDATE klarschiff_unterstuetzer SET datum_bestaetigung = '2011-07-31 20:50:10' WHERE id = 769;
--DELETE FROM klarschiff_unterstuetzer WHERE id=770;



-- #######################################################################################
-- # Verlauf                                                                             #
-- #######################################################################################

-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_verlauf()
  RETURNS trigger AS
$BODY$
DECLARE
	query text;

BEGIN
	PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');

	IF (TG_OP = 'DELETE') THEN
    
		query := 'UPDATE ${f_schema}.klarschiff_vorgang SET ';
        IF (old.typ='status' AND (old.wert_neu='abgeschlossen' OR old.wert_neu='wird nicht bearbeitet')) THEN
  			query := query||'datum_abgeschlossen=NULL ';
        ELSE
            query := query||'datum_abgeschlossen=datum_abgeschlossen ';
		END IF;
        query := query||'WHERE id='||old.vorgang;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
		
		PERFORM dblink_disconnect();
		RETURN old;

    ELSIF (TG_OP = 'UPDATE') THEN
    
		query := 'UPDATE ${f_schema}.klarschiff_vorgang SET ';
        IF (new.typ='status' AND (new.wert_neu='abgeschlossen' OR new.wert_neu='wird nicht bearbeitet')) THEN
  			query := query||'datum_abgeschlossen='''''||new.datum||''''' ';
        ELSIF (new.typ='status' AND NOT (new.wert_neu='abgeschlossen' OR new.wert_neu='wird nicht bearbeitet')) THEN
        	query := query||'datum_abgeschlossen=NULL ';
 		ELSE
  			query := query||'datum_abgeschlossen=datum_abgeschlossen ';
		END IF;
        query := query||'WHERE id='||new.vorgang;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
		
		PERFORM dblink_disconnect();
		RETURN new;

    ELSIF (TG_OP = 'INSERT') THEN

		query := 'UPDATE ${f_schema}.klarschiff_vorgang SET ';
        IF (new.typ='status' AND (new.wert_neu='abgeschlossen' OR new.wert_neu='wird nicht bearbeitet')) THEN
  			query := query||'datum_abgeschlossen='''''||new.datum||''''' ';
        ELSIF (new.typ='status' AND NOT (new.wert_neu='abgeschlossen' OR new.wert_neu='wird nicht bearbeitet')) THEN
        	query := query||'datum_abgeschlossen=NULL ';
 		ELSE
  			query := query||'datum_abgeschlossen=datum_abgeschlossen ';
		END IF;
        query := query||'WHERE id='||new.vorgang;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
		
		PERFORM dblink_disconnect();
		RETURN new;

	END IF;

	PERFORM dblink_disconnect();
	RETURN NULL;
EXCEPTION WHEN others THEN
	PERFORM dblink_disconnect();
	RAISE EXCEPTION '(%)', SQLERRM;
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
	FOR EACH ROW
	EXECUTE PROCEDURE klarschiff_triggerfunction_verlauf();



-- #######################################################################################
-- # Vorgang                                                                             #
-- #######################################################################################

-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_vorgang() RETURNS trigger AS
$BODY$
DECLARE
  	foto_normal text;          -- Backend = Frontend: foto_normal_jpg
  	foto_thumb text;           -- Backend = Frontend: foto_thumb_jpg
	query text;

BEGIN
	PERFORM dblink_connect('hostaddr=${f_host} port=${f_port} dbname=${f_dbname} user=${f_username} password=${f_password}');
  
	IF (TG_OP = 'DELETE') THEN

		query := 'DELETE FROM ${f_schema}.klarschiff_vorgang WHERE id='||old.id;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';
		
		PERFORM dblink_disconnect();
		RETURN old;

	ELSIF (TG_OP = 'UPDATE') THEN
	
		query := '
			UPDATE klarschiff.klarschiff_vorgang 
			SET datum='''''||new.datum::varchar(50)||''''', vorgangstyp='''''||new.typ||''''', the_geom='''''||new.ovi::text||''''', status='''''||new.status||''''', kategorieid='||new.kategorie||', ';
		--betreff
		IF (new.betreff_freigabe_status='extern' AND new.betreff IS NOT NULL AND new.betreff <> '') THEN
  			query := query||'titel='''''||new.betreff||''''', ';
 		ELSE
  			query := query||'titel='''''''', ';
		END IF;
		--details
		IF (new.details_freigabe_status='extern' AND new.details IS NOT NULL AND new.details <> '') THEN
  			query := query||'details='''''||new.details||''''', ';
 		ELSE
  			query := query||'details='''''''', ';
		END IF;
		--statusKommentar
		IF new.status_kommentar IS NOT NULL THEN
  			query := query||'bemerkung='''''||new.status_kommentar||''''', ';
 		ELSE
  			query := query||'bemerkung='''''''', ';
		END IF;
		--fotoNormalJpg & fotoThumbJpg
		IF (new.foto_normal_jpg IS NOT NULL AND new.foto_freigabe_status='extern') THEN
			foto_normal = encode(new.foto_normal_jpg, 'base64');
			foto_thumb = encode(new.foto_thumb_jpg, 'base64');
			query := query||'foto_normal_jpg=decode('''''||foto_normal||''''', ''''base64''''), ';
			query := query||'foto_thumb_jpg=decode('''''||foto_thumb||''''', ''''base64''''), ';
		ELSE	
			query := query||'foto_normal_jpg=NULL, ';
			query := query||'foto_thumb_jpg=NULL, ';
		END IF;
        --fotoVorhanden
		IF (length(new.foto_normal_jpg) IS NOT NULL AND length(new.foto_thumb_jpg) IS NOT NULL) THEN
  			query := query||'foto_vorhanden=TRUE, ';
 		ELSE
  			query := query||'foto_vorhanden=FALSE, ';
		END IF;
        --fotoFreigegeben
		IF (new.foto_freigabe_status='extern') THEN
  			query := query||'foto_freigegeben=TRUE, ';
 		ELSE
  			query := query||'foto_freigegeben=FALSE, ';
		END IF;
        --betreffVorhanden
		IF (new.betreff IS NOT NULL AND new.betreff <> '') THEN
  			query := query||'betreff_vorhanden=TRUE, ';
 		ELSE
  			query := query||'betreff_vorhanden=FALSE, ';
		END IF;
        --betreffFreigegeben
		IF (new.betreff_freigabe_status='extern') THEN
  			query := query||'betreff_freigegeben=TRUE, ';
 		ELSE
  			query := query||'betreff_freigegeben=FALSE, ';
		END IF;
        --detailsVorhanden
		IF (new.details IS NOT NULL AND new.details <> '') THEN
  			query := query||'details_vorhanden=TRUE, ';
 		ELSE
  			query := query||'details_vorhanden=FALSE, ';
		END IF;
        --detailsFreigegeben
		IF (new.details_freigabe_status='extern') THEN
  			query := query||'details_freigegeben=TRUE, ';
 		ELSE
  			query := query||'details_freigegeben=FALSE, ';
		END IF;
		--archiviert
		IF new.archiviert IS NOT NULL THEN
  			query := query||'archiviert='||new.archiviert;
 		ELSE
  			query := query||'archiviert=FALSE';
		END IF;
		query := query||' WHERE id='||new.id;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';

		PERFORM dblink_disconnect();
		RETURN new;

    ELSIF (TG_OP = 'INSERT') THEN
    
		query := '
			INSERT INTO klarschiff.klarschiff_vorgang (id, datum, vorgangstyp, the_geom, status, kategorieid, titel, details, bemerkung, foto_normal_jpg, foto_thumb_jpg, foto_vorhanden, foto_freigegeben, betreff_vorhanden, betreff_freigegeben, details_vorhanden, details_freigegeben, archiviert)
			VALUES ('||new.id||', '''''||new.datum::varchar(50)||''''', '''''||new.typ||''''', '''''||new.ovi::text||''''', '''''||new.status||''''', '||new.kategorie||', ';
		--betreff
		IF (new.betreff_freigabe_status='extern' AND new.betreff IS NOT NULL AND new.betreff <> '') THEN
  			query := query||''''''||new.betreff||''''', ';
 		ELSE
  			query := query||''''''''', ';
		END IF;
		--details
		IF (new.details_freigabe_status='extern' AND new.details IS NOT NULL AND new.details <> '') THEN
  			query := query||''''''||new.details||''''', ';
 		ELSE
  			query := query||''''''''', ';
		END IF;
		--statusKommentar
		IF new.status_kommentar IS NOT NULL THEN
  			query := query||''''''||new.status_kommentar||''''', ';
 		ELSE
  			query := query||''''''''', ';
		END IF;
		--fotoNormalJpg & fotoThumbJpg
		IF (new.foto_normal_jpg IS NOT NULL AND new.foto_freigabe_status='extern') THEN
			foto_normal = encode(new.foto_normal_jpg, 'base64');
			foto_thumb = encode(new.foto_thumb_jpg, 'base64');
			query := query||'decode('''''||foto_normal||''''', ''''base64''''), ';
			query := query||'decode('''''||foto_thumb||''''', ''''base64''''), ';
		ELSE	
			query := query||'NULL, ';
			query := query||'NULL, ';
		END IF;
        --fotoVorhanden
		IF (length(new.foto_normal_jpg) IS NOT NULL AND length(new.foto_thumb_jpg) IS NOT NULL) THEN
  			query := query||'TRUE, ';
 		ELSE
  			query := query||'FALSE, ';
		END IF;
        --fotoFreigegeben
		IF (new.foto_freigabe_status='extern') THEN
  			query := query||'TRUE, ';
 		ELSE
  			query := query||'FALSE, ';
		END IF;
        --betreffVorhanden
		IF (new.betreff IS NOT NULL AND new.betreff <> '') THEN
  			query := query||'TRUE, ';
 		ELSE
  			query := query||'FALSE, ';
		END IF;
        --betreffFreigegeben
		IF (new.betreff_freigabe_status='extern') THEN
  			query := query||'TRUE, ';
 		ELSE
  			query := query||'FALSE, ';
		END IF;
        --detailsVorhanden
		IF (new.details IS NOT NULL AND new.details <> '') THEN
  			query := query||'TRUE, ';
 		ELSE
  			query := query||'FALSE, ';
		END IF;
        --detailsFreigegeben
		IF (new.details_freigabe_status='extern') THEN
  			query := query||'TRUE, ';
 		ELSE
  			query := query||'FALSE, ';
		END IF;
		--archiviert
		IF new.archiviert IS NOT NULL THEN
  			query := query||new.archiviert||')';
 		ELSE
  			query := query||'FALSE)';
		END IF;
		--RAISE NOTICE 'Query : %', query;
		EXECUTE 'SELECT dblink_exec('''||query||''');';

		PERFORM dblink_disconnect();
		RETURN new;

	END IF;
	
	PERFORM dblink_disconnect();
	RETURN NULL;
EXCEPTION WHEN others THEN
	PERFORM dblink_disconnect();
	RAISE EXCEPTION '(%)', SQLERRM;
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
	FOR EACH ROW
	EXECUTE PROCEDURE klarschiff_triggerfunction_vorgang();



-- #######################################################################################
-- # automatische Zuordnung einer Adresse für einen Vorgang                              #
-- #######################################################################################

-- Triggerfunktion erzeugen
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_adresse()
  RETURNS trigger AS
$BODY$
    DECLARE
        ergebnis record;

    BEGIN
        -- nur ausführen bei einem neuen Vorgang oder beim Aktualisieren eines Vorgangs (hier aber nur, wenn sich die Geometrie ändert oder wenn im Adressfeld nix drinsteht!)
        IF (TG_OP = 'INSERT' OR (TG_OP = 'UPDATE' AND (NEW.adresse IS null OR NEW.adresse = '')) OR (TG_OP = 'UPDATE' AND (encode(NEW.ovi, 'base64') <> encode(OLD.ovi, 'base64')))) THEN
        
            -- Verbindung zur Adresstabelle aufbauen (hierfür verwendet man idealerweise die Tabelle für die Standortsuche, da die eh da ist)
            PERFORM dblink_connect('standortsuche_verbindung','hostaddr=${f_host} port=${f_port} dbname=standortsuche user=standortsuche password=standortsuche');
            
            -- räumliche Abfrage durchführen, die genau einen Datensatz (oder NULL) als Ergebnis liefert, der auch gleich in die oben deklarierte Variable geschrieben wird
            SELECT (standortsuche.strasse || ' ' || standortsuche.hausnummer || COALESCE(standortsuche.hausnummerzusatz,'') || ' ' || COALESCE(standortsuche.zusatz,'')) AS adresse, ST_Distance(standortsuche.geom, NEW.ovi) AS distanz INTO ergebnis
                FROM klarschiff_vorgang, dblink('standortsuche_verbindung','SELECT strasse, hausnummer, hausnummerzusatz, zusatz, geom FROM standortsuche') AS standortsuche(strasse varchar, hausnummer varchar, hausnummerzusatz varchar, zusatz varchar, geom geometry)
                    WHERE standortsuche.hausnummer IS NOT null
                    AND ST_DWithin(standortsuche.geom, NEW.ovi, 100) 
                        ORDER BY ST_Distance(standortsuche.geom, NEW.ovi) LIMIT 1;
            
            -- Verbindung zur Adresstabelle wieder schließen
            PERFORM dblink_disconnect('standortsuche_verbindung');
            
            -- wenn das Ergebnis nicht NULL ist und die Distanz der ermittelten Adresse zum Vorgang kleiner gleich 50 m: Adresse zuweisen
            IF (ergebnis.adresse IS NOT NULL AND ergebnis.distanz <= 50) THEN
                NEW.adresse := ergebnis.adresse;
            -- wenn das Ergebnis nicht NULL ist und die Distanz der ermittelten Adresse zum Vorgang kleiner gleich 100 m: "bei " + Adresse zuweisen
            ELSIF (ergebnis.adresse IS NOT NULL AND ergebnis.distanz <= 100) THEN
                NEW.adresse := 'bei ' || ergebnis.adresse;
            -- ansonsten: "nicht zuordenbar" zuweisen
            ELSE
                NEW.adresse := 'nicht zuordenbar';
            END IF;
        
        END IF;
        
        RETURN NEW;
        
        EXCEPTION WHEN others THEN
            RAISE EXCEPTION '(%)', SQLERRM;
    END;
    $BODY$
  LANGUAGE plpgsql VOLATILE COST 100;

-- Owner fuer die Triggerfunktion setzen
ALTER FUNCTION klarschiff_triggerfunction_adresse() OWNER TO ${b_username};

-- ggf. alten Trigger loeschen
DROP TRIGGER IF EXISTS klarschiff_trigger_adresse ON klarschiff_vorgang CASCADE;

-- Trigger erzeugen
CREATE TRIGGER klarschiff_trigger_adresse
  BEFORE INSERT OR UPDATE
  ON klarschiff_vorgang
  FOR EACH ROW
  EXECUTE PROCEDURE klarschiff_triggerfunction_adresse();
