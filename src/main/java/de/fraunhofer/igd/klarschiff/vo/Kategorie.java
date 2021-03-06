package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import de.fraunhofer.igd.klarschiff.context.AppContext;
import javax.persistence.OneToOne;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * VO für die Kategorien der Vorgänge. <br/>
 * Hauptkategorien haben ein <code>typ</code> aber kein <code>parent</code><br/>
 * Unterkategorien haben keinen <code>typ</code> aber ein <code>parent</code>
 *
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
@Entity
public class Kategorie implements Serializable {

  /**
   * Id der Kategorie
   */
  @Id
  @TableGenerator(
    name = "KategorieSequence",
    table = "klarschiff_KategorieSequence",
    initialValue = 1,
    allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "KategorieSequence")
  private Long id;

  /**
   * Typ der Kategorie
   */
  @Enumerated(EnumType.STRING)
  private EnumVorgangTyp typ;

  /**
   * Name der Kategorie
   */
  @NotNull
  @Size(max = 200)
  private String name;

  /**
   * übergeordnete Kategorie
   */
  @ManyToOne
  private Kategorie parent;

  /**
   * untergeordnete Kategorien
   */
  @JsonIgnore
  @ManyToMany(cascade = CascadeType.ALL, mappedBy = "parent")
  @OrderBy(value = "name")
  private List<de.fraunhofer.igd.klarschiff.vo.Kategorie> children
    = new ArrayList<de.fraunhofer.igd.klarschiff.vo.Kategorie>();

  /**
   * Liste von intialen Zuständigkeiten für die Vorgänge mit der Kategorie
   */
  @JsonIgnore
  @ElementCollection(fetch = FetchType.EAGER)
  private List<String> initialZustaendigkeiten;

  @NotNull
  @Column(columnDefinition = "boolean default false")
  private boolean geloescht = false;

  /**
   * D3-Akte
   */
  @OneToOne
  private D3 d3;

  /**
   * Gibt den Namen der Kategorie als "escaped HTML" zurück.
   *
   * @return escaped HTML
   */
  @Transient
  public String getNameEscapeHtml() {
    return StringEscapeUtils.escapeHtml(getName());
  }

  /**
   * Setzt die Liste der initialen Zuständigkeiten
   *
   * @param zustaendigkeiten initiale zuständigkeiten als komma-separierter String
   */
  @Transient
  public void setInitialZustaendigkeit(String zustaendigkeiten) {
    initialZustaendigkeiten = new ArrayList<String>();
    for (String zustaendigkeit : zustaendigkeiten.split(",")) {
      if (!StringUtils.isBlank(zustaendigkeit)) {
        initialZustaendigkeiten.add(zustaendigkeit.trim());
      }
    }
  }

  /**
   * Ermittelt die Kategorie anhand der Id. (Die Methode wird für das Binding bei WebMVC benötigt.)
   *
   * @param id Id der Kategorie
   * @return die Kategorie zu der Id oder null falls Id null
   */
  public static Kategorie findKategorie(Long id) {
    if (id == null) {
      return null;
    }
    return AppContext.getEntityManager().find(Kategorie.class, id);
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Kategorie getParent() {
    return this.parent;
  }

  public void setParent(Kategorie parent) {
    this.parent = parent;
  }

  public List<Kategorie> getChildren() {
    return this.children;
  }

  public void setChildren(List<Kategorie> children) {
    this.children = children;
  }

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public EnumVorgangTyp getTyp() {
    return typ;
  }

  public void setTyp(EnumVorgangTyp typ) {
    this.typ = typ;
  }

  public boolean isGeloescht() {
    return geloescht;
  }

  public void setGeloescht(boolean geloescht) {
    this.geloescht = geloescht;
  }

  public D3 getD3() {
    return d3;
  }

  public void setD3(D3 d3) {
    this.d3 = d3;
  }

  public List<String> getInitialZustaendigkeiten() {
    return initialZustaendigkeiten;
  }

  public void setInitialZustaendigkeiten(List<String> initialZustaendigkeiten) {
    this.initialZustaendigkeiten = initialZustaendigkeiten;
	}
}
