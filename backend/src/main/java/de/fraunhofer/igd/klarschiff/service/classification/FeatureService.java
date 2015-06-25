package de.fraunhofer.igd.klarschiff.service.classification;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
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
 * Der FeatureService dient zum Initialisieren und Berechnen der Features, so wie sie vom Klassifikator für das Initialisieren, Trainieren
 * und Klassifizieren benötigt werden.
 * Bei der Berechnung der Features werden ggf. nicht änderbare Featurewerte persistiert. 
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
	
	List<String> bewirtschaftungFeatures;
	List<String> flaechenFeatures;
	
	
	/**
	 * Initialisiert den Klassifikatorkontext mit den für die Klassifikation verwendeten Features.
	 * @param classificationContext Kontext, der initialisiert werden soll
	 */
	@SuppressWarnings("unchecked")
	public void initClassificationContext(ClassificationContext classificationContext) {
		FastVector attributes = new FastVector();
		
		//Zuständigkeit
		List<String> zustaendigkeiten = new ArrayList<String>();
		for(Role zustaendigkeit : securityService.getAllZustaendigkeiten(false)) zustaendigkeiten.add(zustaendigkeit.getId());
		Attribute classAttribute = Attribute.createClassAttribute("zustaendigkeit", zustaendigkeiten);
		attributes.addElement(classAttribute);
		
		//Kategorien
		List<String> kategorien = new ArrayList<String>();
		for(Kategorie kategoerie : kategorieDao.getKategorien()) kategorien.add(kategoerie.getId()+"");
		attributes.addElement(Attribute.createAttribute("kategorie", kategorien, true));

		//Bewirtschaftung vom WFS
		for(String b : bewirtschaftungFeatures) {
                logger.debug("attributes.appendElements(Attribute.createGeoAttributes(geo_bewirtschaftung_"+b+","+
                        settingsService.getPropertyValue("geo.wfs.zufiprefex")+":bewirtschaftung,...,"+
                        settingsService.getPropertyValue("geo.wfs.geomname")+"...)");
		attributes.appendElements(Attribute.createGeoAttributes(
				"geo_bewirtschaftung_"+b, 
				settingsService.getPropertyValue("geo.wfs.zufiprefex")+":bewirtschaftung", 
				"bewirtschafter", 
				b, 
        settingsService.getPropertyValue("geo.wfs.geomname") , 
				false));
		}
		
		//Flächentypen vom WFS
		for(String f : flaechenFeatures) {
                        logger.debug("attributes.appendElements(Attribute.createGeoAttributes(...,"+
                                settingsService.getPropertyValue("geo.wfs.zufiprefex")+":"+f+","+
                                settingsService.getPropertyValue("geo.wfs.geomname")+"...)");
			attributes.appendElements(Attribute.createGeoAttributes(
					"geo_"+f, 
          settingsService.getPropertyValue("geo.wfs.zufiprefex")+":"+f,
          settingsService.getPropertyValue("geo.wfs.geomname") ,
					false));
		}
		Map<String, Attribute> attributMap = new HashMap<String, Attribute>();
		for (Enumeration<Attribute> iter=attributes.elements(); iter.hasMoreElements(); ) {
			Attribute attribute = iter.nextElement();
			attributMap.put(attribute.getName(), attribute);
		}
		classificationContext.setAttributes(attributes);
		classificationContext.setAttributMap(attributMap);
		classificationContext.setClassAttribute(classAttribute);
	}

	
	/**
	 * Entfernt nicht änderbare Features aus der DB
	 * @param vorgang
	 */
	public void removeNonUpdatableFeatures(Vorgang vorgang) {
		
		
		try {
			VorgangFeatures vorgangFeatures = vorgangDao.findVorgangFeatures(vorgang);
            if (vorgangFeatures != null)
                vorgangDao.remove(vorgangFeatures);
		} catch (Exception e) {
			logger.error("Entfernen eines nicht änderbaren Features aus der Datenbank fehlgeschlagen.", e);
		}
	}
	
	/**
	 * Ermittelt für einen Vorgang die Features für den Klassifikator. Featurewerte, die nicht änderbar sind und für den Vorgang 
	 * bereits gespeichert wurden, werden nicht neu berechnet, sondern aus der DB gelesen.
	 * Nach der Berechnung der Features werden nicht änderbare Features in der DB gespeichert.
	 * @param vorgang Vorgang, für den die Features berechnet werden sollen
	 * @param inclClassAttribute Soll der Wert der aktuellen Zuständigkeit ebenfalls mit in die Features aufgenommen werden (z.B. für ein Trainingsset)?
	 * @param ctx Klassifikatorkontext
	 * @return berechnete Features
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Instance createFeature(Vorgang vorgang, boolean inclClassAttribute, ClassificationContext ctx) throws Exception {
		Instance instance = new Instance(ctx.getAttributes().size());
		
		VorgangFeatures vorgangFeatures = vorgangDao.findVorgangFeatures(vorgang);

		for (Enumeration<Attribute> iter=ctx.getAttributes().elements(); iter.hasMoreElements(); )
		{
			Attribute attribute = iter.nextElement();
			if (vorgangFeatures!=null && !attribute.isUpdateble()) {
				//Wert ggf. aus vorgangFeatures nehmen 
				if (vorgangFeatures.getFeatures().containsKey(attribute.getName())) {
                                        logger.debug("createFeature fuer Vorgang Wert ggf. aus vorgangFeatures nehmen Attribut ("+attribute.getName()+") set value ("+vorgangFeatures.getFeatures().get(attribute.getName()) +")" );
					if (attribute.isNominal()) {
						instance.setValue(attribute, vorgangFeatures.getFeatures().get(attribute.getName()));
					} else {
						instance.setValue(attribute, Double.parseDouble(vorgangFeatures.getFeatures().get(attribute.getName())));
					}
				}
			} else {
				//Wert neu berechnen
                                logger.debug("createFeature fuer Vorgang Wert neu berechnen Attribut ("+attribute.getName() +")");
				if (attribute.getName().equals("kategorie")) {
					instance.setValue(attribute, vorgang.getKategorie().getId()+"");
                                        logger.debug("createFeature instance.setValue Attribut:("+attribute.getName()+") value:("+ vorgang.getKategorie().getId() +")");
				} else if (attribute.getName().equals("zustaendigkeit")) {
					if (inclClassAttribute) {
                                                logger.info("createFeature instance.setValue Zustaendigkeit Attribut:("+attribute.getName()+") Zustaendigkeit:("+vorgang.getZustaendigkeit()+")");
						if (vorgang.getZustaendigkeitStatus()==EnumZustaendigkeitStatus.akzeptiert){
                                                        int valIndex = attribute.indexOfValue(vorgang.getZustaendigkeit()+"");
                                                        if (valIndex == -1){
                                                            logger.debug("FEHLER createFeature instance.setValue Attribut:("+attribute.getName()+") Zustaendigkeit:("+vorgang.getZustaendigkeit()+") ist nicht definiert (keine interne AD-Gruppe)");
                                                            //throw new Exception("FEHLER createFeature instance.setValue Attribut:("+attribute.getName()+") Zustaendigkeit:("+vorgang.getZustaendigkeit()+") ist nicht definiert (keine interne AD-Gruppe)");
                                                        }else{
							instance.setValue(attribute, vorgang.getZustaendigkeit()+"");
                                                        }
                                                }
						else throw new Exception("Zust�ndigkeit des Vorganges kann nicht als Feature mit aufgenommen werden, da die Zust�ndigkeit noch nicht akzeptiert ist.");
					}
				} else if (attribute.isGeoAttribute()){
					Double value = geoService.calculateFeature(vorgang.getOvi(), attribute);
					if (value!=null && value > 0) instance.setValue(attribute, value);
				} else throw new Exception("Unbekanntes Feature: "+attribute.getName());
			}
		}
		
		if (vorgangFeatures==null) {
			//vorgangFeatures neu anlegen
                        logger.info("createFeature vorgangFeatures neu anlegen");
			vorgangFeatures = new VorgangFeatures();
			vorgangFeatures.setVorgang(vorgang);
			//einzelne Werte in vorgangFeatures ablegen
			for (Enumeration<Attribute> iter=ctx.getAttributes().elements(); iter.hasMoreElements(); )
			{
				Attribute attribute = iter.nextElement();
				if (!attribute.isUpdateble) { 
                                    if ( !instance.isMissing(attribute)) {
					vorgangFeatures.getFeatures().put(attribute.getName(), (attribute.isNominal()) ? instance.stringValue(attribute) : instance.value(attribute)+"");
			}
                                }
			}
			//vorgangFeatures speichern
			vorgangDao.persist(vorgangFeatures);
		}
                
		return instance;
	}

	
	/**
	 * Erzeugt eine Liste von Features nur auf Basis der Kategorie. Der Klassifikator wird bis zu einer bestimmten Trainingsmenge zusätzlich
	 * mit initialen Zuständigkeiten, die für die einzelnen Kategorien definiert werden können, trainiert. Die Erzeugung der Features
	 * für dieses Trainingsset erfolgt auf Basis dieser Funktion.
	 * @param kategorie Kategorie, für die die Features ermittelt werden sollen
	 * @param inclClassAttribute Soll die Zuständigkeit mit in die Features aufgenommen werden?
	 * @param ctx Klassifikatorkontext
	 * @return Liste mit Features (eine Kategorie kann initial für mehrere Zuständigkeiten gedacht sein)
	 */
	public List<Instance> createFeature(Kategorie kategorie, boolean inclClassAttribute, ClassificationContext ctx) {
	
		List<Instance> instances = new ArrayList<Instance>(); 
			
		for (String zustaendigkeit : kategorie.getInitialZustaendigkeiten()) {
                        String[] zustaendigkeitpair = zustaendigkeit.split(";");
                        String zustaendigkeitpairstring = zustaendigkeit;
                        zustaendigkeit = zustaendigkeitpair[0];
                        String flaeche = "";
			try {
                                logger.debug("createFeature fuer die Kategorie ("+kategorie.getName()+") initiale Zustaendigkeit:("+zustaendigkeit+")  versuch.");
				Instance instance = new Instance(ctx.getAttributes().size());
				instance.setValue(ctx.getAttributMap().get("kategorie") ,kategorie.getId()+"");
				if (inclClassAttribute){
                                    int valIndex = ctx.getAttributMap().get("zustaendigkeit").indexOfValue(zustaendigkeit);
                                    if (valIndex == -1){
                                        logger.info("FEHLER createFeature fuer die Kategorie ("+kategorie.getName()+") initiale Zustaendigkeit:("+zustaendigkeit+") ist nicht definiert (keine interne AD-Gruppe)");
                                        //throw new Exception("FEHLER createFeature instance.setValue Attribut:("+attribute.getName()+") Zustaendigkeit:("+vorgang.getZustaendigkeit()+") ist nicht definiert (keine interne AD-Gruppe)");
                                    }else{
                                        instance.setValue(ctx.getAttributMap().get("zustaendigkeit"), zustaendigkeit);
                                    }
                                    if(zustaendigkeitpair.length > 1){
                                        flaeche = zustaendigkeitpair[1];
                                        flaeche = StringUtils.replace(flaeche,"$komma$", ",");
                                        logger.debug("createFeature fuer die Kategorie ("+kategorie.getName()+") initiale Zustaendigkeit:("+zustaendigkeit+") FLAECHE: geo_"+flaeche+"_innerhalb = 1 .");
                                        instance.setValue(ctx.getAttributMap().get("geo_"+flaeche+"_innerhalb"), 1);
                                    }
				instances.add(instance);
                                }
			} catch (Exception e) {
				logger.info("FEHLER Initiale Zustaendigkeit ("+zustaendigkeitpairstring+") fuer die Kategorie ("+kategorie.getName()+") ist fehlgeschlagen.");
			}
		}
                
		return instances;
	}
	
	/* --------------- GET + SET ----------------------------*/
	
	public List<String> getBewirtschaftungFeatures() {
		return bewirtschaftungFeatures;
	}


	public void setBewirtschaftungFeatures(List<String> bewirtschaftungFeatures) {
		this.bewirtschaftungFeatures = bewirtschaftungFeatures;
	}


	public List<String> getFlaechenFeatures() {
		return flaechenFeatures;
	}


	public void setFlaechenFeatures(List<String> flaechenFeatures) {
		this.flaechenFeatures = flaechenFeatures;
	}


}
