ALTER TABLE klarschiff.klarschiff_vorgang
ADD COLUMN foto_normal varchar(255),
ADD COLUMN foto_thumb varchar(255);

DROP TRIGGER klarschiff_trigger_vorgang ON klarschiff.klarschiff_vorgang CASCADE;
DROP FUNCTION klarschiff.klarschiff_triggerfunction_vorgang;

DROP VIEW klarschiff.klarschiff_wfs_tmpl;
DROP VIEW klarschiff.klarschiff_wfs;
CREATE VIEW klarschiff.klarschiff_wfs AS 
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
      FROM klarschiff.klarschiff_unterstuetzer u 
      WHERE 
        v.id = u.vorgang AND 
        u.datum IS NOT NULL
    ) AS unterstuetzer,
    v.foto_vorhanden,
    v.foto_freigegeben,
    v.foto_normal,
    v.foto_thumb,
    v.betreff_vorhanden,
    v.betreff_freigegeben,
    v.details_vorhanden,
    v.details_freigegeben,
    v.zustaendigkeit
  FROM 
    klarschiff.klarschiff_vorgang v, 
    klarschiff.klarschiff_kategorie k
  WHERE 
    v.kategorieid = k.id AND
    v.status <> 'geloescht' AND 
    v.archiviert <> TRUE AND 
    v.status <> 'duplikat' AND 
    v.id NOT IN (
      SELECT m.vorgang
      FROM klarschiff.klarschiff_missbrauchsmeldung m
      WHERE 
        m.datum_bestaetigung IS NOT NULL AND 
        m.datum_abarbeitung IS NULL AND
        m.vorgang = v.id
    )
  ORDER BY v.datum DESC;
ALTER VIEW klarschiff.klarschiff_wfs OWNER TO klarschiff_frontend;

CREATE VIEW klarschiff.klarschiff_wfs_tmpl AS
    SELECT 
      v.id, 
      v.datum, 
      v.details, 
      v.bemerkung, 
      v.kategorieid, 
      k.name AS kategorie_name, 
      v.hauptkategorieid, 
      kh.name AS hauptkategorie_name, 
      v.the_geom::geometry(Point,25833) AS the_geom, 
      v.titel, 
      v.vorgangstyp, 
      t.name AS vorgangstyp_name, 
      v.status, 
      s.name AS status_name, 
      v.unterstuetzer, 
      v.foto_vorhanden,
      v.foto_freigegeben,
      v.foto_normal, 
      v.foto_thumb,
      v.betreff_vorhanden,
      v.betreff_freigegeben,
      v.details_vorhanden,
      v.details_freigegeben,
      v.zustaendigkeit
    FROM 
      klarschiff.klarschiff_wfs v,
      klarschiff.klarschiff_status s,
      klarschiff.klarschiff_kategorie k,
      klarschiff.klarschiff_kategorie kh,
      klarschiff.klarschiff_vorgangstyp t
    WHERE
      v.status = s.id AND
      v.kategorieid = k.id AND
      v.hauptkategorieid = kh.id AND
      v.vorgangstyp = t.id;
ALTER VIEW klarschiff.klarschiff_wfs_tmpl OWNER TO klarschiff_frontend;

ALTER TABLE klarschiff.klarschiff_vorgang
DROP COLUMN foto_normal_jpg,
DROP COLUMN foto_thumb_jpg;
