CREATE OR REPLACE FUNCTION write_jpg_file(id bigint, fototype text, filedata bytea) RETURNS text AS $$
DECLARE
  _oid oid;
  _fd int;
  _function_result int;
  filename text;
  basepath text;
BEGIN
  IF filedata IS NULL THEN
    RETURN NULL;
  END IF;

  basepath = '/srv/www/htdocs/klarschiff_fotos/';
  -- Fotonamen generieren
  filename = 'ks_' || id || '_' || fototype || '_' || uuid_generate_v4() || '.jpg';

  -- Foto in Dateisystem schreiben
  _oid = lo_create(-1);
  _fd = lo_open(_oid, 131072);
  _function_result = lowrite(_fd, filedata);
  _function_result = lo_close(_fd);
  _function_result = lo_export(_oid, basepath || filename);
  _function_result = lo_unlink(_oid);
  RETURN filename;
END;
$$ LANGUAGE plpgsql;

-- Fotonamen in Datenbank schreiben
UPDATE klarschiff_vorgang SET
  foto_gross = write_jpg_file(id, 'gross', foto_gross_jpg),
  foto_normal = write_jpg_file(id, 'normal', foto_normal_jpg),
  foto_thumb = write_jpg_file(id, 'thumb', foto_thumb_jpg);

DROP FUNCTION write_jpg_file(bigint, text, bytea);
