#!/usr/bin/env ruby

def load_config
  require 'yaml'
  YAML.load(File.open("config.yml"))
end

def load_from_wfs(wfs_config)
  url = wfs_config["url"]
  max = wfs_config["max"]
  typename = wfs_config["typename"]
  schluessel = wfs_config["schluessel"]
  raise "wfs:url missing" if url.nil? || url.empty?
  raise "wfs:max missing" if max.nil? || !max.is_a?(Numeric)
  raise "wfs:typename missing" if typename.nil? || typename.empty?
  raise "wfs:schluessel missing" if schluessel.nil? || schluessel.empty?
  raise "wfs:schluessel incorrect" if !schluessel.is_a?(Array) || schluessel.length < 1
  filter = "<Filter>"
  filter << "<OR>" if schluessel.length > 1 
  filter << schluessel.map do |s|
    "<PropertyIsEqualTo><PropertyName>schluessel</PropertyName><Literal>#{ s }</Literal></PropertyIsEqualTo>"
  end.join
  filter << "</OR>" if schluessel.length > 1 
  filter << "</Filter>"
  require 'open-uri'
  open("#{ url }?SERVICE=WFS&VERSION=1.1.0&REQUEST=GetFeature&TYPENAME=#{ typename }&" +
       "maxFeatures=#{ max }&filter=#{ URI::encode(filter) }") { |f|
    f.read
  }
end

def build_xml(text)
  require 'rexml/document'
  doc = REXML::Document.new(text)
end

def check_for_exception(xml)
  if ex = REXML::XPath.first(xml, "//ows:Exception")
    raise "Fehler beim WFS-Request: #{ ex }"
  end
end

def extract_gml(xml)
  '<gml:MultiSurface srsName="EPSG:25833"><gml:surfaceMembers>' +
    REXML::XPath.each(xml, "//gml:Polygon").map { |g| g.to_s }.join +
    '</gml:surfaceMembers></gml:MultiSurface>'
end

def update_db(gml, db_config)
  table = db_config["table"]
  column = db_config["column"]
  raise "wfs:table missing" if table.nil? || table.empty?
  raise "wfs:column missing" if column.nil? || column.empty?
  require 'pg'
  conn = PG.connect(db_config["connection"])
  conn.exec("UPDATE #{ table } SET #{ column } = ST_GeomFromGML('#{ gml.gsub(/'/, "\"") }')")
end

config = load_config
text = load_from_wfs(config["wfs"])
xml = build_xml(text)
check_for_exception(xml)
gml = extract_gml(xml)
puts gml
update_db(gml, config["db"])
