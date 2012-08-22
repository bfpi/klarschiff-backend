-- @author Stefan Audersch (Fraunhofer IGD)
-- Hinweis: Variablen werden vor der Ausfuehrung durch die entsprechenden Werte ersetzt



-- #######################################################################################
-- # Initialisierung                                                                     #
-- #######################################################################################

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;



-- #######################################################################################
-- # Schema                                                                              #
-- #######################################################################################

--CREATE SCHEMA ${f_schema};
--ALTER SCHEMA ${f_schema} OWNER TO ${f_username};



-- #######################################################################################
-- # Initialisierung                                                                     #
-- #######################################################################################


SET search_path = ${f_schema}, public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;



-- #######################################################################################
-- # alte Tabellen entfernen                                                             #
-- #######################################################################################

DROP TABLE IF EXISTS klarschiff_geo_rss CASCADE;
DROP TABLE IF EXISTS klarschiff_kategorie CASCADE;
DROP TABLE IF EXISTS klarschiff_missbrauchsmeldung CASCADE;
DROP TABLE IF EXISTS klarschiff_stadtgrenze_hro CASCADE;
DROP TABLE IF EXISTS klarschiff_stadtteile_hro CASCADE;
DROP TABLE IF EXISTS klarschiff_status CASCADE;
DROP TABLE IF EXISTS klarschiff_trashmail_blacklist CASCADE;
DROP TABLE IF EXISTS klarschiff_unterstuetzer CASCADE;
DROP TABLE IF EXISTS klarschiff_vorgang CASCADE;
DROP TABLE IF EXISTS klarschiff_vorgangstyp CASCADE;



-- #######################################################################################
-- # Tabellen erzeugen                                                                   #
-- #######################################################################################

-- Name: klarschiff_geo_rss; Type: TABLE; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
CREATE TABLE klarschiff_geo_rss (
    id integer NOT NULL,
    klarschiff_geo_rss_fid integer NOT NULL,               --########### @deprecated ############
    the_geom geometry,
    ideen boolean NOT NULL,
    ideen_kategorien character varying(255),
    probleme boolean NOT NULL,
    probleme_kategorien character varying(255),
    CONSTRAINT enforce_dims_the_geom CHECK ((ndims(the_geom) = 2)),
    CONSTRAINT enforce_srid_the_geom CHECK ((srid(the_geom) = 25833))
);
ALTER TABLE klarschiff_geo_rss OWNER TO ${f_username};


-- Name: klarschiff_kategorie; Type: TABLE; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
CREATE TABLE klarschiff_kategorie (
    id bigint NOT NULL,
    name character varying(200) NOT NULL,
    parent bigint,
    vorgangstyp character varying(255),
	aufforderung boolean,                                  --########### @deprecated ############
    naehere_beschreibung_notwendig character varying(255)
);
ALTER TABLE klarschiff_kategorie OWNER TO ${f_username};


-- Name: klarschiff_missbrauchsmeldung; Type: TABLE; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
CREATE TABLE klarschiff_missbrauchsmeldung (
    id bigint NOT NULL,
    datum timestamp without time zone NOT NULL,
    datum_abarbeitung timestamp without time zone,
    datum_bestaetigung timestamp without time zone,
    vorgang bigint
);
ALTER TABLE klarschiff_missbrauchsmeldung OWNER TO ${f_username};


-- Name: klarschiff_stadtgrenze_hro; Type: TABLE; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
CREATE TABLE klarschiff_stadtgrenze_hro (
    ogc_fid integer NOT NULL,
    the_geom geometry,
    CONSTRAINT enforce_dims_the_geom CHECK ((ndims(the_geom) = 2)),
    CONSTRAINT enforce_geotype_the_geom CHECK (((geometrytype(the_geom) = 'POLYGON'::text) OR (the_geom IS NULL))),
    CONSTRAINT enforce_srid_the_geom CHECK ((srid(the_geom) = 25833))
);
ALTER TABLE klarschiff_stadtgrenze_hro OWNER TO ${f_username};


-- Name: klarschiff_stadtteile_hro; Type: TABLE; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
CREATE TABLE klarschiff_stadtteile_hro (
    ogc_fid integer NOT NULL,
    bezeichnung character varying,
    the_geom geometry,
    CONSTRAINT enforce_dims_the_geom CHECK ((ndims(the_geom) = 2)),
    CONSTRAINT enforce_geotype_the_geom CHECK (((geometrytype(the_geom) = 'POLYGON'::text) OR (the_geom IS NULL))),
    CONSTRAINT enforce_srid_the_geom CHECK ((srid(the_geom) = 25833))
);
ALTER TABLE klarschiff_stadtteile_hro OWNER TO ${f_username};


-- Name: klarschiff_status; Type: TABLE; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
CREATE TABLE klarschiff_status (
    name character varying(300),
    id character varying(255) NOT NULL,
    nid integer
);
ALTER TABLE klarschiff_status OWNER TO ${f_username};


-- Name: klarschiff_trashmail_blacklist; Type: TABLE; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
CREATE TABLE klarschiff_trashmail_blacklist (
    id bigint NOT NULL,
    pattern character varying(255)
);
ALTER TABLE klarschiff_trashmail_blacklist OWNER TO ${f_username};


-- Name: klarschiff_unterstuetzer; Type: TABLE; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
CREATE TABLE klarschiff_unterstuetzer (
    id bigint NOT NULL,
    datum timestamp without time zone,
    vorgang bigint
);
ALTER TABLE klarschiff_unterstuetzer OWNER TO ${f_username};


-- Name: klarschiff_vorgang; Type: TABLE; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
CREATE TABLE klarschiff_vorgang (
    id bigint NOT NULL,
    datum timestamp without time zone,
    datum_abgeschlossen timestamp without time zone,
    details text,
    kategorieid bigint,
    the_geom geometry,
    titel character varying(300),
    vorgangstyp character varying(255),
    status character varying(255),
    bemerkung character varying(300),
    foto_normal_jpg bytea,
    foto_thumb_jpg bytea,
    foto_vorhanden boolean,
    foto_freigegeben boolean,
    betreff_vorhanden boolean,
    betreff_freigegeben boolean,
    details_vorhanden boolean,
    details_freigegeben boolean,
    archiviert boolean,
    CONSTRAINT enforce_dims_the_geom CHECK ((ndims(the_geom) = 2)),
    CONSTRAINT enforce_geotype_the_geom CHECK (((geometrytype(the_geom) = 'POINT'::text) OR (the_geom IS NULL))),
    CONSTRAINT enforce_srid_the_geom CHECK ((srid(the_geom) = 25833))
);
ALTER TABLE klarschiff_vorgang OWNER TO ${f_username};


-- Name: klarschiff_vorgangstyp; Type: TABLE; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
CREATE TABLE klarschiff_vorgangstyp (
    name character varying(300),
    id character varying(255) NOT NULL,
    ordinal integer
);
ALTER TABLE klarschiff_vorgangstyp OWNER TO ${f_username};



-- #######################################################################################
-- # Primaerschluessel                                                                   #
-- #######################################################################################

-- Name: frontend_vorgang_pkey; Type: CONSTRAINT; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
ALTER TABLE ONLY klarschiff_vorgang
    ADD CONSTRAINT frontend_vorgang_pkey PRIMARY KEY (id);


-- Name: klarschiff_geo_rss_pkey; Type: CONSTRAINT; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
ALTER TABLE ONLY klarschiff_geo_rss
    ADD CONSTRAINT klarschiff_geo_rss_pkey PRIMARY KEY (id);


-- Name: klarschiff_kategorie_pkey; Type: CONSTRAINT; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
ALTER TABLE ONLY klarschiff_kategorie
    ADD CONSTRAINT klarschiff_kategorie_pkey PRIMARY KEY (id);


-- Name: klarschiff_missbrauchsmeldung_id_key; Type: CONSTRAINT; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
ALTER TABLE ONLY klarschiff_missbrauchsmeldung
    ADD CONSTRAINT klarschiff_missbrauchsmeldung_id_key UNIQUE (id);


-- Name: klarschiff_status_pkey; Type: CONSTRAINT; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
ALTER TABLE ONLY klarschiff_status
    ADD CONSTRAINT klarschiff_status_pkey PRIMARY KEY (id);


-- Name: klarschiff_trashmail_blacklist_pkey; Type: CONSTRAINT; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
ALTER TABLE ONLY klarschiff_trashmail_blacklist
    ADD CONSTRAINT klarschiff_trashmail_blacklist_pkey PRIMARY KEY (id);


-- Name: klarschiff_unterstuetzer_pkey; Type: CONSTRAINT; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
ALTER TABLE ONLY klarschiff_unterstuetzer
    ADD CONSTRAINT klarschiff_unterstuetzer_pkey PRIMARY KEY (id);


-- Name: klarschiff_vorgangstyp_pkey; Type: CONSTRAINT; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
ALTER TABLE ONLY klarschiff_vorgangstyp
    ADD CONSTRAINT klarschiff_vorgangstyp_pkey PRIMARY KEY (id);


-- Name: stadtgrenze_hro_pk; Type: CONSTRAINT; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
ALTER TABLE ONLY klarschiff_stadtgrenze_hro
    ADD CONSTRAINT stadtgrenze_hro_pk PRIMARY KEY (ogc_fid);


-- Name: stadtteile_hro_pk; Type: CONSTRAINT; Schema: ${f_schema}; Owner: ${f_username}; Tablespace: 
ALTER TABLE ONLY klarschiff_stadtteile_hro
    ADD CONSTRAINT stadtteile_hro_pk PRIMARY KEY (ogc_fid);

  
    
-- #######################################################################################
-- # Fremdschluessel                                                                     #
-- #######################################################################################

-- Name: klarschiff_kategorie_vorgangstyp_fkey; Type: FK CONSTRAINT; Schema: ${f_schema}; Owner: ${f_username}
ALTER TABLE ONLY klarschiff_kategorie
    ADD CONSTRAINT klarschiff_kategorie_vorgangstyp_fkey FOREIGN KEY (vorgangstyp) REFERENCES klarschiff_vorgangstyp(id);

-- Name: klarschiff_vorgang_vorgangstyp_fkey; Type: FK CONSTRAINT; Schema: ${f_schema}; Owner: ${f_username}
ALTER TABLE ONLY klarschiff_vorgang
    ADD CONSTRAINT klarschiff_vorgang_vorgangstyp_fkey FOREIGN KEY (vorgangstyp) REFERENCES klarschiff_vorgangstyp(id);

-- Name: klarschiff_vorgang_kategorieid_fkey; Type: FK CONSTRAINT; Schema: ${f_schema}; Owner: ${f_username}
ALTER TABLE ONLY klarschiff_vorgang
    ADD CONSTRAINT klarschiff_vorgang_kategorieid_fkey FOREIGN KEY (kategorieid) REFERENCES klarschiff_kategorie(id);

-- Name: klarschiff_vorgang_status_fkey; Type: FK CONSTRAINT; Schema: ${f_schema}; Owner: ${f_username}
ALTER TABLE ONLY klarschiff_vorgang
    ADD CONSTRAINT klarschiff_vorgang_status_fkey FOREIGN KEY (status) REFERENCES klarschiff_status(id);

-- Name: klarschiff_unterstuetzer_vorgang_fkey; Type: FK CONSTRAINT; Schema: ${f_schema}; Owner: ${f_username}
ALTER TABLE ONLY klarschiff_unterstuetzer
    ADD CONSTRAINT klarschiff_unterstuetzer_vorgang_fkey FOREIGN KEY (vorgang) REFERENCES klarschiff_vorgang(id);

-- Name: klarschiff_missbrauchsmeldung_vorgang_fkey; Type: FK CONSTRAINT; Schema: ${f_schema}; Owner: ${f_username}
ALTER TABLE ONLY klarschiff_missbrauchsmeldung
    ADD CONSTRAINT klarschiff_missbrauchsmeldung_vorgang_fkey FOREIGN KEY (vorgang) REFERENCES klarschiff_vorgang(id);


    
-- #######################################################################################
-- # Triggerfunktionen                                                                   #
-- #######################################################################################

-- Name: klarschiff_triggerfunction_vorgang(); Type: FUNCTION; Schema: ${f_schema}; Owner: ${f_username}
CREATE OR REPLACE FUNCTION klarschiff_triggerfunction_vorgang() RETURNS trigger AS
$BODY$
DECLARE
	basepath text;
	
	filedata bytea;
	filename text;
	
	_oid oid;
	_fd int;
	_function_result int;

BEGIN
   	--basepath = '/nfs/web/klarschiff/fotos/', 'c:/temp/;
	basepath = '${f_exportimage_path}';

	-- ########## Foto normal ##########
	filename = basepath||'ks_'||new.id||'_normal.jpg';
	IF new.foto_normal_jpg IS NOT NULL THEN
		filedata = new.foto_normal_jpg;
	ELSE
		filedata = ''::bytea;
	END IF;
	
	-- ########## Foto normal - Datei erzeugen ##########
	--temporaer ein oid erzeugen
	_oid = lo_create(-1);

	--oid oeffnen
	_fd = lo_open(_oid, 131072);

	--Daten in das OID schreiben
	_function_result = lowrite(_fd, filedata);

	--oid schliessen
	_function_result = lo_close(_fd);

	--oid in Datei exportieren
	_function_result = lo_export(_oid, filename);

	--oid entfernen
	_function_result = lo_unlink(_oid);

	-- ########## Foto thumb ##########
	filename = basepath||'ks_'||new.id||'_thumb.jpg';
	IF new.foto_thumb_jpg IS NOT NULL THEN
		filedata = new.foto_thumb_jpg;
	ELSE
		filedata = ''::bytea;
	END IF;
	
	-- ########## Foto thumb - Datei erzeugen ##########
	--temporaer ein oid erzeugen
	_oid = lo_create(-1);

	--oid oeffnen
	_fd = lo_open(_oid, 131072);

	--Daten in das OID schreiben
	_function_result = lowrite(_fd, filedata);

	--oid schliessen
	_function_result = lo_close(_fd);

	--oid in Datei exportieren
	_function_result = lo_export(_oid, filename);

	--oid entfernen
	_function_result = lo_unlink(_oid);
	
	RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql VOLATILE COST 100;

ALTER FUNCTION klarschiff_triggerfunction_vorgang() OWNER TO ${f_username};



-- #######################################################################################
-- # Trigger                                                                             #
-- #######################################################################################

DROP TRIGGER IF EXISTS klarschiff_trigger_vorgang ON klarschiff_vorgang CASCADE;

CREATE TRIGGER klarschiff_trigger_vorgang 
	AFTER INSERT OR UPDATE 
	ON klarschiff_vorgang 
	FOR EACH ROW 
	EXECUTE PROCEDURE klarschiff_triggerfunction_vorgang();


	
-- #######################################################################################
-- # Views                                                                               #
-- #######################################################################################

CREATE OR REPLACE VIEW klarschiff_wfs AS 
	SELECT 
		v.id, 
		v.datum, 
		to_char(v.datum_abgeschlossen, 'DD.MM.YYYY'::text)::character varying AS datum_abgeschlossen, 
		v.details, 
		v.bemerkung, 
		v.kategorieid,
		k.parent AS hauptkategorieid,
		v.the_geom, 
		v.titel, 
		v.vorgangstyp, 
		v.status,
		(
			SELECT COUNT(*)
			FROM klarschiff_unterstuetzer u 
			WHERE 
				v.id = u.vorgang AND 
				u.datum IS NOT NULL
		) AS unterstuetzer,
		v.foto_vorhanden, 
		v.foto_freigegeben,
        v.betreff_vorhanden,
        v.betreff_freigegeben,
        v.details_vorhanden,
        v.details_freigegeben
	FROM 
		klarschiff_vorgang v, 
		klarschiff_kategorie k
	WHERE 
		v.kategorieid = k.id AND
		v.status <> 'geloescht' AND 
		v.archiviert <> TRUE AND 
		v.status <> 'duplikat' AND 
		v.id NOT IN (
			SELECT m.vorgang
			FROM klarschiff_missbrauchsmeldung m
			WHERE 
				m.datum_bestaetigung IS NOT NULL AND 
				m.datum_abarbeitung IS NULL AND
				m.vorgang = v.id
		);
		
ALTER TABLE klarschiff_wfs OWNER TO ${f_username};

CREATE VIEW klarschiff_wfs_tmpl AS
    SELECT 
    	v.id, 
    	v.datum, 
    	v.details, 
    	v.bemerkung, 
    	v.kategorieid, 
    	k.name AS kategorie_name, 
    	v.hauptkategorieid, 
    	kh.name AS hauptkategorie_name, 
    	v.the_geom, 
    	v.titel, 
    	v.vorgangstyp, 
    	t.name AS vorgangstyp_name, 
    	v.status, 
    	s.name AS status_name, 
    	v.unterstuetzer, 
    	v.foto_vorhanden, 
		v.foto_freigegeben,
        v.betreff_vorhanden,
        v.betreff_freigegeben,
        v.details_vorhanden,
        v.details_freigegeben
    FROM 
    	klarschiff_wfs v,
    	klarschiff_status s,
    	klarschiff_kategorie k,
    	klarschiff_kategorie kh,
    	klarschiff_vorgangstyp t
    WHERE
    	v.status = s.id AND
    	v.kategorieid = k.id AND
    	v.hauptkategorieid = kh.id AND
    	v.vorgangstyp = t.id;

ALTER TABLE klarschiff.klarschiff_wfs_tmpl OWNER TO ${f_username};

-- #######################################################################################
-- # Update matadata table 'geometry_columns'                                            #
-- #######################################################################################

SELECT populate_geometry_columns();
