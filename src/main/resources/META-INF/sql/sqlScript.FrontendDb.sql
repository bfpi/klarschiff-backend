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
    vorgangstyp character varying(255)
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
    datum_statusaenderung timestamp without time zone,
    details text,
    kategorieid bigint,
    the_geom geometry,
    titel character varying(300),
    vorgangstyp character varying(255),
    status character varying(255),
    bemerkung character varying,
    foto_normal character varying(255),
    foto_thumb character varying(255),
    foto_vorhanden boolean,
    foto_freigegeben boolean,
    beschreibung_vorhanden boolean,
    beschreibung_freigegeben boolean,
    archiviert boolean,
    zustaendigkeit character varying(255),
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
-- # Views                                                                               #
-- #######################################################################################

CREATE OR REPLACE VIEW klarschiff_wfs AS 
	SELECT 
		v.id, 
		v.datum, 
		to_char(v.datum, 'DD.MM.YYYY'::text)::character varying AS datum_erstellt, 
		to_char(v.datum_statusaenderung, 'DD.MM.YYYY'::text)::character varying AS datum_statusaenderung, 
		v.details, 
		v.bemerkung, 
		v.kategorieid,
		k.parent AS hauptkategorieid,
		v.the_geom::geometry(Point,25833) AS the_geom, 
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
		v.foto_normal,
		v.foto_thumb,
		v.beschreibung_vorhanden,
		v.beschreibung_freigegeben,
		v.zustaendigkeit
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
		)
	ORDER BY v.datum DESC;
		
ALTER TABLE klarschiff_wfs OWNER TO ${f_username};

CREATE OR REPLACE VIEW klarschiff_wfs_georss AS
    SELECT
        v.id AS meldung,
        initcap(v.vorgangstyp::text) AS typ,
        hk.name::text AS hauptkategorie,
        uk.parent::smallint AS hauptkategorie_id,
        uk.name::text AS unterkategorie,
        uk.id::smallint AS unterkategorie_id,
        CASE
            WHEN v.status::text = 'wirdNichtBearbeitet'::text THEN 'wird nicht bearbeitet'::text
            ELSE regexp_replace(v.status::text, '([A-Z])'::text, ' \1'::text, 'g'::text)
        END AS status,
        CASE
            WHEN (( SELECT count(*) AS count
               FROM klarschiff.klarschiff_unterstuetzer u
              WHERE v.id = u.vorgang AND u.datum IS NOT NULL))::smallint > 0 THEN (( SELECT count(*) AS count
               FROM klarschiff.klarschiff_unterstuetzer u
              WHERE v.id = u.vorgang AND u.datum IS NOT NULL))::text
            ELSE 'bisher keine'::text
        END AS unterstuetzungen,
        CASE
            WHEN v.beschreibung IS NOT NULL AND v.beschreibung <> ''::text AND v.beschreibung_freigegeben IS TRUE AND v.beschreibung_vorhanden IS TRUE THEN v.beschreibung
            WHEN v.status::text = 'offen'::text AND v.beschreibung_vorhanden IS TRUE AND v.beschreibung_freigegeben IS FALSE THEN 'redaktionelle Prüfung ausstehend'::text
            WHEN v.status::text <> 'offen'::text AND v.beschreibung_vorhanden IS TRUE AND v.beschreibung_freigegeben IS FALSE THEN 'redaktionell nicht freigegeben'::text
            ELSE 'nicht vorhanden'::text
        END AS beschreibung,
        CASE
            WHEN v.foto_thumb IS NOT NULL AND v.foto_thumb::text <> ''::text AND v.foto_freigegeben IS TRUE AND v.foto_vorhanden IS TRUE THEN ((((('<br/><a href="http://support.klarschiff-hro.de/fotos/'::text || v.foto_normal::text) || '" target="_blank" title="große Ansicht öffnen…"><img src="http://support.klarschiff-hro.de/fotos/'::text) || v.foto_thumb::text) || '" alt="'::text) || v.foto_thumb::text) || '" /></a>'::text
            WHEN v.status::text = 'offen'::text AND v.foto_vorhanden IS TRUE AND v.foto_freigegeben IS FALSE THEN 'redaktionelle Prüfung ausstehend'::text
            WHEN v.status::text <> 'offen'::text AND v.foto_vorhanden IS TRUE AND v.foto_freigegeben IS FALSE THEN 'redaktionell nicht freigegeben'::text
            ELSE 'nicht vorhanden'::text
        END AS foto,
        CASE
            WHEN v.bemerkung IS NOT NULL AND v.bemerkung::text <> ''::text THEN v.bemerkung::text
            ELSE 'nicht vorhanden'::text
        END AS info_der_verwaltung,
        CASE
            WHEN to_char(v.datum::timestamp with time zone, 'TZ'::text) = 'CEST'::text THEN to_char(v.datum::timestamp with time zone, 'Dy, DD Mon YYYY HH24:MI:SS +0200'::text)
            ELSE to_char(v.datum::timestamp with time zone, 'Dy, DD Mon YYYY HH24:MI:SS +0100'::text)
        END AS datum,
        ST_X(ST_Transform(v.the_geom, 4326))::text AS x,
        ST_Y(ST_Transform(v.the_geom, 4326))::text AS y,
        v.the_geom::geometry(Point,25833) AS geometrie
    FROM
        klarschiff.klarschiff_vorgang v,
        klarschiff.klarschiff_kategorie hk,
        klarschiff.klarschiff_kategorie uk
    WHERE
        v.kategorieid = uk.id AND
        uk.parent = hk.id AND
        (v.status::text <> ALL (ARRAY['duplikat'::text, 'geloescht'::text])) AND
        v.archiviert IS NOT TRUE AND
        NOT (v.id IN (
            SELECT
                m.vorgang
            FROM
                klarschiff.klarschiff_missbrauchsmeldung m
            WHERE
                m.datum_bestaetigung IS NOT NULL AND
                m.datum_abarbeitung IS NULL AND
                m.vorgang = v.id))
    ORDER BY
        v.datum DESC;

ALTER TABLE klarschiff.klarschiff_wfs_georss OWNER TO ${f_username};

-- #######################################################################################
-- # Update matadata table 'geometry_columns'                                            #
-- #######################################################################################

SELECT populate_geometry_columns();
