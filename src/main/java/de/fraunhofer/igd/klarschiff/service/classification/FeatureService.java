package de.fraunhofer.igd.klarschiff.service.classification;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import weka.core.FastVector;
import weka.core.Instance;
import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.geo.GeoService;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import de.fraunhofer.igd.klarschiff.vo.EnumZustaendigkeitStatus;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import de.fraunhofer.igd.klarschiff.vo.VorgangFeatures;

/**
 * Der FeatureService dient zum Initialisieren und Berechnen der Features, so wie sie vom
 * Klassifikator für das Initialisieren, Trainieren und Klassifizieren benötigt werden. Bei der
 * Berechnung der Features werden ggf. nicht änderbare Featurewerte persistiert.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 * @author Marcus Kröller (Fraunhofer IGD)
 */
@Service
public class FeatureService {

  static final Logger logger = Logger.getLogger(FeatureService.class);

  @Autowired
  KategorieDao kategorieDao;

  @Autowired
  VorgangDao vorgangDao;

  @Autowired
  GeoService geoService;

  @Autowired
  SecurityService securityService;

  @Autowired
  SettingsService settingsService;

  List<String> bewirtschaftungskatasterClasses;
  List<String> flaechendatenFeaturetypes;

  /**
   * Initialisiert den Klassifikatorkontext mit den für die Klassifikation verwendeten Features.
   *
   * @param classificationContext Kontext, der initialisiert werden soll
   */
  @SuppressWarnings("unchecked")
  public void initClassificationContext(ClassificationContext classificationContext) {
    FastVector attributes = new FastVector();

    //Zuständigkeit
    List<String> zustaendigkeiten = new ArrayList<String>();
    for (Role zustaendigkeit : securityService.getAllZustaendigkeiten(false)) {
      zustaendigkeiten.add(zustaendigkeit.getId());
    }
    Attribute classAttribute = Attribute.createClassAttribute("zustaendigkeit", zustaendigkeiten);
    attributes.addElement(classAttribute);

    //Kategorien
    List<String> kategorien = new ArrayList<String>();
    for (Kategorie kategoerie : kategorieDao.getKategorien()) {
      kategorien.add(kategoerie.getId().toString());
    }
    attributes.addElement(Attribute.createAttribute("kategorie", kategorien, true));

    if (bewirtschaftungskatasterClasses != null) {
      //Bewirtschaftungskataster aus dem WFS für Zuständigkeitsfinder
      for (String b : bewirtschaftungskatasterClasses) {
        String prefix = "geo_bewirtschaftung_" + b;
        String type = String.format("%s:%s", 
          settingsService.getPropertyValue("geo.wfszufi.featureprefix"),
          settingsService.getPropertyValue("geo.wfszufi.bewirtschaftungskataster.featuretype"));
        String propertyName = settingsService.getPropertyValue("geo.wfszufi.bewirtschaftungskataster.propertyname");
        String geomName = settingsService.getPropertyValue("geo.wfszufi.bewirtschaftungskataster.geomname");

        logger.debug(String.format("attributes.appendElements(Attribute.createGeoAttributes(%s, %s, %s, %s, %s, false))",
          prefix, type, propertyName, b, geomName));

        attributes.appendElements(Attribute.createGeoAttributes(prefix, type, propertyName, b, geomName, false));
      }
    }

    if (flaechendatenFeaturetypes != null) {
      //Flächendaten-Featuretypes aus dem WFS für Zuständigkeitsfinder
      for (String f : flaechendatenFeaturetypes) {
        String prefix = "geo_" + f;
        String type = settingsService.getPropertyValue("geo.wfszufi.featureprefix") + ":" + f;
        String geomName = settingsService.getPropertyValue("geo.wfszufi.flaechendaten.geomname");

        logger.debug(String.format("attributes.appendElements(Attribute.createGeoAttributes(%s, %s, %s, false))",
          prefix, type, geomName));

        attributes.appendElements(Attribute.createGeoAttributes(prefix, type, geomName, false));
      }
    }

    Map<String, Attribute> attributMap = new HashMap<String, Attribute>();
    for (Enumeration<Attribute> iter = attributes.elements(); iter.hasMoreElements();) {
      Attribute attribute = iter.nextElement();
      attributMap.put(attribute.getName(), attribute);
    }
    classificationContext.setAttributes(attributes);
    classificationContext.setAttributMap(attributMap);
    classificationContext.setClassAttribute(classAttribute);
  }

  /**
   * Entfernt nicht änderbare Features aus der DB
   *
   * @param vorgang
   */
  public void removeNonUpdatableFeatures(Vorgang vorgang) {

    try {
      VorgangFeatures vorgangFeatures = vorgangDao.findVorgangFeatures(vorgang);
      if (vorgangFeatures != null) {
        vorgangDao.remove(vorgangFeatures);
      }
    } catch (Exception e) {
      logger.error("Entfernen eines nicht änderbaren Features aus der Datenbank fehlgeschlagen.", e);
    }
  }

  /**
   * Ermittelt für einen Vorgang die Features für den Klassifikator. Featurewerte, die nicht
   * änderbar sind und für den Vorgang bereits gespeichert wurden, werden nicht neu berechnet,
   * sondern aus der DB gelesen. Nach der Berechnung der Features werden nicht änderbare Features in
   * der DB gespeichert.
   *
   * @param vorgang Vorgang, für den die Features berechnet werden sollen
   * @param inclClassAttribute Soll der Wert der aktuellen Zuständigkeit ebenfalls mit in die
   * Features aufgenommen werden (z.B. für ein Trainingsset)?
   * @param ctx Klassifikatorkontext
   * @return berechnete Features
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public Instance createFeature(Vorgang vorgang, boolean inclClassAttribute, ClassificationContext ctx) throws Exception {
    Instance instance = new Instance(ctx.getAttributes().size());

    VorgangFeatures vorgangFeatures = vorgangDao.findVorgangFeatures(vorgang);

    for (Enumeration<Attribute> iter = ctx.getAttributes().elements(); iter.hasMoreElements();) {
      Attribute attribute = iter.nextElement();
      if (vorgangFeatures != null && !attribute.isUpdateble()) {
        //Wert ggf. aus vorgangFeatures nehmen
        if (vorgangFeatures.getFeatures().containsKey(attribute.getName())) {
          logger.debug(String.format("createFeature fuer Vorgang Wert ggf. aus vorgangFeatures nehmen "
            + "Attribut (%s) set value (%s)", attribute.getName(),
            vorgangFeatures.getFeatures().get(attribute.getName())));
          if (attribute.isNominal()) {
            instance.setValue(attribute, vorgangFeatures.getFeatures().get(attribute.getName()));
          } else {
            instance.setValue(attribute, Double.parseDouble(vorgangFeatures.getFeatures().get(attribute.getName())));
          }
        }
      } else {
        //Wert neu berechnen
        logger.debug("createFeature fuer Vorgang Wert neu berechnen Attribut (" + attribute.getName() + ")");
        if (attribute.getName().equals("kategorie")) {
          instance.setValue(attribute, vorgang.getKategorie().getId().toString());
          logger.debug(String.format("createFeature instance.setValue Attribut: (%s) value: (%s)",
            attribute.getName(), vorgang.getKategorie().getId()));
        } else if (attribute.getName().equals("zustaendigkeit")) {
          if (inclClassAttribute) {
            logger.info(String.format("createFeature instance.setValue Zustaendigkeit Attribut: (%s) "
              + "Zustaendigkeit: (%s)", attribute.getName(), vorgang.getZustaendigkeit()));
            if (vorgang.getZustaendigkeitStatus() == EnumZustaendigkeitStatus.akzeptiert) {
              if (attribute.indexOfValue(vorgang.getZustaendigkeit()) == -1) {
                logger.error(String.format("createFeature instance.setValue Attribut: (%s) "
                  + "Zustaendigkeit: (%s) ist nicht definiert (keine interne AD-Gruppe)",
                  attribute.getName(), vorgang.getZustaendigkeit()));
              } else {
                instance.setValue(attribute, vorgang.getZustaendigkeit());
              }
            } else {
              throw new Exception("Zuständigkeit des Vorganges kann nicht als Feature mit aufgenommen "
                + "werden, da die Zuständigkeit noch nicht akzeptiert ist.");
            }
          }
        } else if (attribute.isGeoAttribute()) {
          Double value = geoService.calculateFeature(vorgang.getOvi(), attribute);
          if (value == null) {
            logger.debug("createFeature fuer Vorgang Wert neu berechnen Ovi null");
          } else {
            logger.debug("createFeature fuer Vorgang Wert neu berechnen Ovi " + value.toString());
            if (value > 0) {
              instance.setValue(attribute, value);
            } else {
              logger.debug("createFeature fuer Vorgang Wert neu berechnen Ovi 0 -> null");
            }
          }
        } else {
          throw new Exception("Unbekanntes Feature: " + attribute.getName());
        }
      }
    }

    if (vorgangFeatures == null) {
      //vorgangFeatures neu anlegen
      logger.info("createFeature vorgangFeatures neu anlegen");
      vorgangFeatures = new VorgangFeatures();
      vorgangFeatures.setVorgang(vorgang);
      //einzelne Werte in vorgangFeatures ablegen
      for (Enumeration<Attribute> iter = ctx.getAttributes().elements(); iter.hasMoreElements();) {
        Attribute attribute = iter.nextElement();
        if (!attribute.isUpdateble) {
          if (!instance.isMissing(attribute)) {
            vorgangFeatures.getFeatures().put(attribute.getName(), (attribute.isNominal())
              ? instance.stringValue(attribute) : String.valueOf(instance.value(attribute)));
          }
        }
      }
      //vorgangFeatures speichern
      vorgangDao.persist(vorgangFeatures);
    }

    return instance;
  }

  /**
   * Erzeugt eine Liste von Features nur auf Basis der Kategorie. Der Klassifikator wird bis zu
   * einer bestimmten Trainingsmenge zusätzlich mit initialen Zuständigkeiten, die für die einzelnen
   * Kategorien definiert werden können, trainiert. Die Erzeugung der Features für dieses
   * Trainingsset erfolgt auf Basis dieser Funktion.
   *
   * @param kategorie Kategorie, für die die Features ermittelt werden sollen
   * @param inclClassAttribute Soll die Zuständigkeit mit in die Features aufgenommen werden?
   * @param ctx Klassifikatorkontext
   * @return Liste mit Features (eine Kategorie kann initial für mehrere Zuständigkeiten gedacht
   * sein)
   */
  public List<Instance> createFeature(Kategorie kategorie, boolean inclClassAttribute, ClassificationContext ctx) {

    List<Instance> instances = new ArrayList<Instance>();

    for (String initialZustaendigkeit : kategorie.getInitialZustaendigkeiten()) {
      String[] pair = initialZustaendigkeit.split(";");
      String zustaendigkeit = pair[0];
      try {
        logger.debug(String.format("createFeature fuer die Kategorie (%s) initiale Zustaendigkeit: (%s) versuch.",
          kategorie.getName(), zustaendigkeit));
        Instance instance = new Instance(ctx.getAttributes().size());
        instance.setValue(ctx.getAttributMap().get("kategorie"), kategorie.getId().toString());
        if (inclClassAttribute) {
          int valIndex = ctx.getAttributMap().get("zustaendigkeit").indexOfValue(zustaendigkeit);
          if (valIndex == -1) {
            logger.error(String.format("createFeature fuer die Kategorie (%s) initiale Zustaendigkeit: (%s) "
              + "ist nicht definiert (keine interne AD-Gruppe)", kategorie.getName(), zustaendigkeit));
          } else {
            instance.setValue(ctx.getAttributMap().get("zustaendigkeit"), zustaendigkeit);
          }
          if (pair.length > 1) {
            String attrName = "geo_" + pair[1].replace("$komma$", ",") + "_innerhalb";
            logger.debug(String.format("createFeature fuer die Kategorie (%s) initiale Zustaendigkeit: (%s) "
              + " Fläche: %s = 1.", kategorie.getName(), zustaendigkeit, attrName));
            Attribute attr = ctx.getAttributMap().get(attrName);
            if (attr != null) {
              instance.setValue(attr, 1);
            } else {
              logger.error(String.format("Attribut '%s' ist im Instanzkontext nicht definiert "
                + "=> keine Berücksichtigung der flächenabhängigen Zuständigkeit %s für Kategorie %s!",
                attrName, zustaendigkeit, kategorie.getName()));
            }
          }
          instances.add(instance);
        }
      } catch (Exception e) {
        logger.error("Exception: " + e.getMessage());
        e.printStackTrace();
        logger.error(String.format("Initiale Zustaendigkeit (%s) fuer die Kategorie (%s) ist fehlgeschlagen.",
          initialZustaendigkeit, kategorie.getName()));
      }
    }

    return instances;
  }

  public List<String> getBewirtschaftungskatasterClasses() {
    return bewirtschaftungskatasterClasses;
  }

  public void setBewirtschaftungskatasterClasses(List<String> bewirtschaftungskatasterClasses) {
    this.bewirtschaftungskatasterClasses = bewirtschaftungskatasterClasses;
  }

  public List<String> getFlaechendatenFeaturetypes() {
    return flaechendatenFeaturetypes;
  }

  public void setFlaechendatenFeaturetypes(List<String> flaechendatenFeaturetypes) {
    this.flaechendatenFeaturetypes = flaechendatenFeaturetypes;
  }
}
