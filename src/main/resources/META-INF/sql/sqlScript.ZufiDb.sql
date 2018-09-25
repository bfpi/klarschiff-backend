-- @author Niels Bennke, BFPI GmbH (bennke@bfpi.de

SET statement_timeout = 0;
SET client_encoding = 'UTF8';

DROP EXTENSION IF EXISTS postgis CASCADE;
CREATE EXTENSION postgis;

DROP TABLE IF EXISTS bewirtschaftung CASCADE;
CREATE TABLE bewirtschaftung (
  id SERIAL PRIMARY KEY,
  name CHARACTER VARYING(255) NOT NULL,
  schluessel INTEGER
);
SELECT AddGeometryColumn('bewirtschaftung', 'geom', 25833, 'MULTIPOLYGON', 2);
GRANT SELECT ON bewirtschaftung TO zufi;
