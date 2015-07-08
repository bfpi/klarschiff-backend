package de.fraunhofer.igd.klarschiff.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.vo.EnumVorgangTyp;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;
import flexjson.JSONSerializer;

/**
 * Controller zum Abfragen von Kategorien (JSON)
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@RequestMapping("/kategorien")
@Controller
public class KategorieController {

  public static Logger logger = Logger.getLogger(KategorieController.class);

  @Autowired
  KategorieDao kategorieDao;

  /**
   * Die Methode verarbeitet den GET-Request mit <code>"Accept=application/json"</code>auf der URL
   * <code>/kategorien</code> bei vorhandenem Parameter <code>kategorie</code> (Long)<br/>
   * Funktionsbeschreibung: Liefert die Unterkategorien zur im Parameter übergebenen Kategorie-ID
   * als JSON
   *
   * @param kategorie Kategorie-ID
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/kategorie", params = {"kategorie"}, method = RequestMethod.GET, headers = "Accept=application/json")
  @ResponseBody
  public Object childesForKategorieJson(@RequestParam("kategorie") Long kategorie) {
    List<Kategorie> list;
    try {
      list = kategorieDao.findKategorie(kategorie).getChildren();
    } catch (Exception e) {
      list = new ArrayList<Kategorie>();
    }
    return new JSONSerializer().include("id", "nameEscapeHtml").exclude("*.class", "name", "parent", "version").serialize(list);
  }

  /**
   * Die Methode verarbeitet den GET-Request mit <code>"Accept=application/json"</code>auf der URL
   * <code>/kategorien</code> bei vorhandenem Parameter <code>typ</code> (Long)<br/>
   * Funktionsbeschreibung: Liefert eine Liste aller Haupt-Kategorien zum im Parameter übergebenen
   * Vorgangs-Typ als JSON.
   *
   * @param typ Typ-Bezeichner
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/kategorieTyp", params = {"typ"}, method = RequestMethod.GET, headers = "Accept=application/json")
  @ResponseBody
  public Object childesForTypJson(@RequestParam("typ") String typ) {
    List<Kategorie> list;
    try {
      list = kategorieDao.findRootKategorienForTyp(EnumVorgangTyp.valueOf(typ));
    } catch (Exception e) {
      list = new ArrayList<Kategorie>();
    }
    return new JSONSerializer().include("id", "nameEscapeHtml").exclude("*.class", "name", "parent", "version").serialize(list);
  }

  /**
   * Die Methode verarbeitet den GET-Request mit <code>"Accept=application/json"</code>auf der URL
   * <code>/kategorien/viewNaehereBeschreibung</code><br/>
   * Funktionsbeschreibung: Liefert eine detailliertere Beschreibung der über beide Parameter
   * bestimmten Kategorie.
   *
   * @param hauptkategorie
   * @param unterkategorie
   * @return View, die zum Rendern des Request verwendet wird
   */
  @RequestMapping(value = "/viewNaehereBeschreibung", method = RequestMethod.GET, headers = "Accept=application/json")
  @ResponseBody
  public Object viewNaehereBeschreibung(
    @RequestParam(value = "hauptkategorie", required = false) Long hauptkategorie,
    @RequestParam(value = "unterkategorie", required = false) Long unterkategorie) {
    return new JSONSerializer().include("string").serialize(kategorieDao.viewNaehereBeschreibung(hauptkategorie, unterkategorie));
  }
}