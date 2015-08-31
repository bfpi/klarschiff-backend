package de.fraunhofer.igd.klarschiff.service.classification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveWithValues;
import de.fraunhofer.igd.klarschiff.dao.KategorieDao;
import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.service.security.Role;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.vo.Kategorie;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import de.fraunhofer.igd.klarschiff.vo.VorgangHistoryClasses;
import weka.classifiers.Classifier;

/**
 * Die Klasse stellt einen Service als Klassifikator bzw. Zuständigkeitsfinder für das System
 * bereit. Ein Vorgang kann dabei klassifiziert werden. Ergebnis dabei ist eine Liste von möglichen
 * Zuständigkeiten inkl. deren Relevanz. Damit der Klassifikator im laufenden Betrieb aktualisiert
 * bzw. erneuert werden kann, wird der eigentliche Klassifikator in einem Kontext gehalten.
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 * @author Marcus Kröller (Fraunhofer IGD)
 * @see de.fraunhofer.igd.klarschiff.service.classification.ClassificationContext
 */
@Service
public class ClassificationService {

  static final Logger logger = Logger.getLogger(ClassificationService.class);

  @Autowired
  FeatureService featureService;

  @Autowired
  VorgangDao vorgangDao;

  @Autowired
  KategorieDao kategorieDao;

  @Autowired
  SecurityService securityService;

  private final int maxCountForClassifiereTrainSet = 1000;
  protected int waitTimeToInitClassficationService = 10000;
  private ClassificationContext ctx;

  /**
   * Initialisiert den Klassifikator und setzt den Kontext beim Service. Das Initialisieren wird als
   * Thread ausgeführt, der erst eine Weile wartet, damit alle anderen Services, Repositorys etc.
   * (z.B. LDAP, JPA) richtig initialisiert sind.
   *
   * @see de.fraunhofer.igd.klarschiff.service.classification.ClassficationServiceInitThread
   */
  @PostConstruct
  public void init() {
    new ClassficationServiceInitThread(this);
  }

  /**
   * Ermittelt für einen gegebenen Kontext das Trainingset für den Klassifikator
   *
   * @param ctx Klassifikatorkontext
   * @return Liste mit Trainingsdaten
   * @throws Exception
   * @see de.fraunhofer.igd.klarschiff.dao.VorgangDao#findVorgangForTrainClassificator(int)
   */
  private Instances createTrainset(ClassificationContext ctx) throws Exception {
    Instances instances = new Instances(ctx.getDataset());
    //Vorgänge für das Training holen
    List<Vorgang> vorgaenge = vorgangDao.findVorgangForTrainClassificator(maxCountForClassifiereTrainSet);
    //Features für jeden Vorgang ermitteln
    for (Vorgang vorgang : vorgaenge) {
      logger.info(String.format("Classification featureService.createFeature vorgang (%s)",
        StringUtils.abbreviate(vorgang.getBeschreibung(), 15)));
      Instance instance = featureService.createFeature(vorgang, true, ctx);
      instance.setDataset(ctx.getDataset());
      instances.add(instance);
    }
    if (vorgaenge.size() < maxCountForClassifiereTrainSet) {
      //initiale Zuständigkeit bei den Kategorien hinzufügen
      for (Kategorie kategorie : kategorieDao.getKategorien()) {
        for (Instance instance : featureService.createFeature(kategorie, true, ctx)) {
          logger.debug("Classification featureService.createFeature kategorie (" + kategorie.getName() + ")");
          instance.setDataset(ctx.getDataset());
          instances.add(instance);
        }
      }
    }
    return instances;
  }

  /**
   * Ermittelt die Zuständigkeit für einen Vorgang bei einem gegebenen Kontext.
   *
   * @param vorgang Vorgang für den die Zuständigkeit ermittelt werden soll
   * @param ctx Klassifikatorkontext
   * @return Liste mit Zuständigkeiten und deren Relevanz
   * @throws Exception
   */
  private List<ClassificationResultEntry> classifierVorgang(Vorgang vorgang, ClassificationContext ctx) throws Exception {

    //Features erzeugen
    Instance instance = featureService.createFeature(vorgang, false, ctx);

    //dataset filtern
    Instances thisdataset = ctx.getDataset();

    RemoveWithValues filter = new RemoveWithValues();
    String[] options = new String[5];
    options[0] = "-C";   // attribute index
    options[1] = "2";    // 2
    options[2] = "-L";   //
    options[3] = String.valueOf(ctx.getAttributMap().get("kategorie").indexOfValue(
      vorgang.getKategorie().getId().toString()) + 1);
    options[4] = "-V";
    filter.setOptions(options);
    filter.setInputFormat(thisdataset);

    Instances newData = Filter.useFilter(thisdataset, filter);

    Classifier cModel = (Classifier) new NaiveBayesUpdateable();
    cModel.buildClassifier(newData);

    Evaluation eTest = new Evaluation(newData);
    eTest.evaluateModel(cModel, newData);
    //Dataset setzen
    instance.setDataset(newData);
    //Klassifizieren
    double[] distribution = cModel.distributionForInstance(instance);
    //Map erzeugen
    List<ClassificationResultEntry> classificationResult = new ArrayList<ClassificationResultEntry>();
    for (int i = 0; i < distribution.length; i++) {
      classificationResult.add(new ClassificationResultEntry(newData.classAttribute().value(i), distribution[i]));
    }
    //Sortieren
    Collections.sort(classificationResult, new Comparator<ClassificationResultEntry>() {
      @Override
      public int compare(ClassificationResultEntry o1, ClassificationResultEntry o2) {
        return -(o1.getWeight().compareTo(o2.getWeight()));
      }
    });

    if (logger.isDebugEnabled()) {
      logEvaluationInfos(eTest, instance, newData, classificationResult);
    }

    return classificationResult;
  }

  private void logEvaluationInfos(Evaluation e, Instance instance, Instances newData,
    List<ClassificationResultEntry> result) {

    logger.debug(e.toSummaryString());
    double[][] m = e.confusionMatrix();
    try {
      int rows = m.length;
      int columns = m[0].length;
      String str = "|\t";

      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < columns; j++) {
          str += m[i][j] + "\t";
        }

        logger.debug(str + "|");
        str = "|\t";
      }
    } catch (Exception ex) {
      logger.debug("ConfusionMatrix is empty!");
    }

    logger.debug("Dataset : (" + newData.toString() + ")");
    logger.debug("Instance: (" + instance.toString() + ")");

    for (ClassificationResultEntry entry : result) {
      logger.debug("ClassificationResult (" + entry.getClassValue() + ") (" + entry.getWeight() + ")");
    }
  }

  /**
   * Ermittelt die aktuell zu verwendene Zuständigkeit. Bei der Berechnung wird das Ergebnis des
   * Klassifikators sowie die History der bisher bereits verwendeten Zuständigkeiten berücksichtigt.
   * Die berechnete Zuständigkeit ist somit immer die Gruppe mit der höchsten Relevanz, die aber
   * noch nicht für die Zuständigkeit verwendet wurde. Die berechnete Zuständigkeit wird
   * abschließend in die History der Zuständigkeiten für den Vorgang mit aufgenommen.
   *
   * @param vorgang Vorgang für den die Zuständigkeit berechnet werden soll
   * @return berechnete Zuständigkeit
   */
  public Role calculateZustaendigkeitforVorgang(Vorgang vorgang) {
    try {
      ClassificationResultEntry result = null;
      //History für den vorgang ermittlen
      VorgangHistoryClasses history = vorgangDao.findVorgangHistoryClasses(vorgang);
      //Wenn der Dispatcher bereits in der History ist keine neue Klassifikation
      if (isDispatcherInVorgangHistoryClasses(history)) {
        return securityService.getDispatcherZustaendigkeit();
      }
      //Vorgang klassifizieren
      List<ClassificationResultEntry> classificationResult = classifierVorgang(vorgang, ctx);
      //Zuständigkeiten aus der History und aktuelle überspringen
      for (ClassificationResultEntry entry : classificationResult) {
        if ((history == null || !history.getHistoryClasses().contains(entry.getClassValue()))
          && !StringUtils.equals(vorgang.getZustaendigkeit(), entry.getClassValue())) {
          result = entry;
          break;
        }
      }

      //ggf. zuständigkeit an Dispatcher übergeben
      if (result == null) {
        result = new ClassificationResultEntry(securityService.getDispatcherZustaendigkeitId(), 0d);
      }

      //gewählte Zuständigkeit in der History speichern
      try {
        boolean isNew = false;
        if (history == null) {
          isNew = true;
          history = new VorgangHistoryClasses();
          history.setVorgang(vorgang);
        }
        history.getHistoryClasses().add(result.getClassValue());
        if (isNew) {
          vorgangDao.persist(history);
        } else {
          vorgangDao.merge(history);
        }
      } catch (Exception e) {
        logger.error("Eine zugewiesene Zuständigkeit konnte nicht in die VorgangHistoryClass aufgenommen werden.", e);
      }

      return securityService.getZustaendigkeit(result.getClassValue());
    } catch (Exception e) {
      logger.error("Die Zuständigkeit für den Vorgang konnte nicht ermittelt werden.", e);
      throw new RuntimeException("Die Zuständigkeit für den Vorgang konnte nicht ermittelt werden.", e);
    }
  }

  /**
   * Ermittelt, ob bereits der Dispatcher für den Vorgang zuständig war
   *
   * @param history Zuständigkeitshistory für den Vorgang
   * @return ja bzw. nein
   */
  private boolean isDispatcherInVorgangHistoryClasses(VorgangHistoryClasses history) {
    if (history == null) {
      return false;
    }
    if (history.getHistoryClasses() == null) {
      return false;
    }
    return history.getHistoryClasses().contains(securityService.getDispatcherZustaendigkeitId());
  }

  /**
   * Ermittelt, ob bereits der Dispatcher für den Vorgang zuständig war.
   *
   * @param vorgang
   * @return ja bzw. nein
   */
  public boolean isDispatcherInVorgangHistoryClasses(Vorgang vorgang) {
    return isDispatcherInVorgangHistoryClasses(vorgangDao.findVorgangHistoryClasses(vorgang));
  }

  /**
   * Aktualisiert den Klassifikator mit der aktuellen Zuständigkeit des Vorgangs. Dabei wird für den
   * gegebenen Vorgang ein Trainingsset erzeugt, womit der Klassifikator aktualisiert wird.
   *
   * @param vorgang Vorgang, der zur Aktualisierung verwendet wird
   */
  public void registerZustaendigkeitAkzeptiert(Vorgang vorgang) {
    try {
      updateClassifier(vorgang, ctx);
    } catch (Exception e) {
      logger.error("Der Klassifizierer konnte nicht mit einer akzeptierten Zuständigkeit aktualisiert werden.", e);
    }
  }

  /**
   * Aktualisiert den Klassifikator mit der aktuellen Zuständigkeit des Vorgangs. Dabei wird für den
   * gegebenen Vorgang ein Trainingsset erzeugt, womit der Klassifikator aktualisiert wird.
   *
   * @param vorgang Vorgang, der zur Aktualisierung verwendet wird
   * @param ctx Klassifikatorkontext
   * @throws Exception
   */
  private void updateClassifier(Vorgang vorgang, ClassificationContext ctx) throws Exception {
    Instance instance = featureService.createFeature(vorgang, true, ctx);
    instance.setDataset(ctx.getDataset());
    ctx.getClassifier().updateClassifier(instance);
  }

  /**
   * aktualisiert den Klassifikator komplett anhand einer Trainingsmenge. Der Klassifikatorkontext
   * wird danach beim Service neu gesetzt.
   *
   * @throws Exception
   * @see #createTrainset(ClassificationContext)
   */
  public void reBuildClassifier() throws Exception {
    logger.debug("rebuild ClassificationContext ..");
    ClassificationContext ctx = new ClassificationContext();
    //Klassifikator neu initialisieren
    ctx.setClassifier(new NaiveBayesUpdateable());
    //attributes und classAttribut neu initialisieren
    featureService.initClassificationContext(ctx);
    //Dataset neu laden
    ctx.setDataset(new Instances("", ctx.getAttributes(), 0));
    ctx.getDataset().setClass(ctx.getClassAttribute());
    //Trainingset neu erstellen
    Instances trainSet = createTrainset(ctx);
    //Klassifikator neu trainieren
    ctx.getClassifier().buildClassifier(trainSet);
    ctx.setDataset(trainSet);
    //neuen context setzen
    this.ctx = ctx;
    logger.debug("ClassificationContext is rebuilded");
  }
}
