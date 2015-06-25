package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import static de.bfpi.tools.GeoTools.transformPosition;
import static de.bfpi.tools.GeoTools.wgs84Projection;
import de.fraunhofer.igd.klarschiff.service.security.SecurityService;
import de.fraunhofer.igd.klarschiff.service.security.User;
import de.fraunhofer.igd.klarschiff.service.settings.PropertyPlaceholderConfigurer;
import de.fraunhofer.igd.klarschiff.service.settings.SettingsService;
import javax.persistence.Column;
import javax.persistence.OneToOne;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Where;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

/**
 * VO zum Abbilden eines Vorganges.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
@Entity
public class Vorgang implements Serializable {

	/* --------------- Attribute ----------------------------*/
	
	/**
	 * Id des Vorganges
	 */
	@Id
	@TableGenerator(
            name="VorgangSequence", 
            table="klarschiff_VorgangSequence",
            initialValue=1,
            allocationSize=1)
    @GeneratedValue(strategy=GenerationType.TABLE, generator="VorgangSequence")
    private Long id;

	/**
	 * Zeitpunkt der letzten Änderung
	 */
	@Version
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "S-")
    private Date version;

	/**
	 * Erstellungszeitpunkt
	 */
	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "S-")
	private Date datum;

	/**
	 * Vorgangstyp
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
	private EnumVorgangTyp typ;

	/**
	 * Betreff
	 */
	@Size(max = 300)
    private String betreff;
	
    /**
	 * Adresse
	 */
	@Size(max = 300)
    private String adresse;
    
    /**
	 * Information über das Eigentum des Flürstücks, in dem der Vorgang liegt
	 */
	@Size(max = 300)
    private String flurstueckseigentum;

	/**
	 * Freigabestatus des Betreffs
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
	private EnumFreigabeStatus betreffFreigabeStatus = EnumFreigabeStatus.intern;
	
	/**
	 * Details
	 */
    @Lob
    @Type(type="org.hibernate.type.TextType")
    private String details;

    /**
     * Freigabestatus der Details
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private EnumFreigabeStatus detailsFreigabeStatus = EnumFreigabeStatus.intern;

    /**
     * geographische Position/Ort
     */
    @JsonIgnore
    @Type(type = "org.hibernatespatial.GeometryUserType")
    private Point ovi;
    
    /**
     * E-Mail-Adresse des Erstellers
     */
    @Size(max = 300)
    private String autorEmail;

    /**
     * Hash zum Bestätigen des Vorganges
     */
	@Size(max = 32)
    private String hash;

	/**
	 * Vorgangsstatus
	 */
    @NotNull
    @Enumerated(EnumType.STRING)
    private EnumVorgangStatus status;
    
    /**
     * Vorgangsstatus als Integer. (Das erlaubt das einfachere Sortieren 
     * der Vorgänge in der Ergebnistabelle mit Hilfe einer angepassten DB-Anfrage)
     */
    @SuppressWarnings("unused")
	@NotNull
    private EnumVorgangStatus statusOrdinal;
    
    /**
     * Kommentar zum Status
     */
	private String statusKommentar;
    
	/**
	 * Erstsichtung erfolgt
	 */
	private boolean erstsichtungErfolgt = false;
    
	/**
	 * Foto
	 */
	private String fotoGross;
    
	/**
	 * Foto
	 */
    private String fotoNormal;

    /**
     * Foto des Vorganges als Vorschaubild
     */
    private String fotoThumb;
    
    /**
     * Freigabestatus des Foto
     */
    @Enumerated(EnumType.STRING)
    private EnumFreigabeStatus fotoFreigabeStatus = EnumFreigabeStatus.intern;;
    
    /**
     * Zuständigkeit (Id der Rolle) für den Vorgang
     */
    String zustaendigkeit;
    
    /**
     * Status der Zuständigkeit
     */
	@Enumerated(EnumType.STRING)
    EnumZustaendigkeitStatus zustaendigkeitStatus;
    
    /**
     * Zuständigkeit für Frontend (also ausführlicher Standort bzw. Name des Amtes; Locality der Rolle) für den Vorgang
     */
    String zustaendigkeitFrontend;

	/**
	 * Delegiert an (Id der Rolle)
	 */
	String delegiertAn;
	
	/**
	 * Liste der Kommentare
	 */
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vorgang")
    @Where(clause = "geloescht = 'false'")
    private List<Kommentar> kommentare = new ArrayList<Kommentar>();
	
	/**
	 * Liste von Lob, Hinweisen oder Kritik zum Vorgang
	 */
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vorgang")
    private List<LobHinweiseKritik> lobHinweiseKritik = new ArrayList<LobHinweiseKritik>();

    /**
     * Liste der Verlaufseinträge
     */
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vorgang")
    private List<Verlauf> verlauf = new ArrayList<Verlauf>();

    /**
     * Kategorie
     */
    @ManyToOne
    private Kategorie kategorie;

    /**
     * Liste der Unterstützungen
     */
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vorgang")
    private List<Unterstuetzer> unterstuetzer = new ArrayList<Unterstuetzer>();

    /**
     * Liste der Missbrauchsmeldungen
     */
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vorgang")
    @OrderBy("datum ASC")
    private List<Missbrauchsmeldung> missbrauchsmeldungen = new ArrayList<Missbrauchsmeldung>();
    
    /**
     * Priorität
     */
    @NotNull
	@Enumerated(EnumType.STRING)
    EnumPrioritaet prioritaet;
    
    /**
     * Priorität als Integer. (Das erlaubt das einfachere Sortieren 
     * der Vorgänge in der Ergebnistabelle mit Hilfe einer angepassten DB-Anfrage)
     */
    @NotNull
    EnumPrioritaet prioritaetOrdinal;
    
    /**
     * Flag zum Markieren archivierte Vorgänge
     */
    Boolean archiviert;
    
    /**
     * ein Wunsch nach einem Foto wurde geäußert
     */
    @Column( nullable = false, columnDefinition = "boolean default false")
    Boolean fotowunsch;
    
	/* --------------- transient ----------------------------*/
    
    @Transient
    private static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 25833);
    
    @Transient
    private static WKTReader wktReader = new WKTReader(geometryFactory);
    
    @Transient
    private static WKTWriter wktWriter = new WKTWriter();
    
    @Transient
    private static SettingsService settingsService = new SettingsService();
    
    @Transient
    private static final String internalProjection =
      PropertyPlaceholderConfigurer.getPropertyValue("geo.map.projection");

    /**
     * securityService wird benötigt, um das Trust-Level zu ermitteln
     */
    @JsonIgnore
    @Transient
    private SecurityService securityService;

    /**
     *Auftrag zu dem Vorgang
     */
    @JsonIgnore
    @OneToOne(mappedBy = "vorgang", cascade = CascadeType.ALL)
    private Auftrag auftrag;

    /**
     * Setzen der Position als WKT
     * @param oviWkt Position als WKT
     * @throws Exception
     */
    @Transient
    public void setOviWkt(String oviWkt) throws Exception {
    	ovi = (StringUtils.isBlank(oviWkt)) ? null : (Point)wktReader.read(oviWkt);
    }

    /**
     * Lesen der Position als WKT
     * @return Position als WKT
     */
    @Transient
    public String getOviWkt() {
    	return (ovi==null) ? null : wktWriter.write(ovi);
    }

    /**
     * Lesen der Position als LatLong
     * @return Position als LatLong
     * @throws org.opengis.referencing.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    @Transient
    public String getPositionWGS84() throws FactoryException, MismatchedDimensionException, TransformException {
      if (ovi == null) {
        return null;
      }

      return transformPosition(ovi, internalProjection, wgs84Projection).toString();
    }

    /**
     * Schreiben der WGS84Position als LatLong
     * @param position
     * @throws com.vividsolutions.jts.io.ParseException
     * @throws org.opengis.referencing.FactoryException
     * @throws org.opengis.referencing.operation.TransformException
     */
    @Transient
    public void setPositionWGS84(String position)
      throws ParseException, FactoryException, MismatchedDimensionException, TransformException  {

      if (position != null) {
        ovi = transformPosition((Point) wktReader.read(position),
          wgs84Projection, internalProjection);
      }
    }
    
    /**
     * Existiert ein Foto zum Vorgang?
     * @return <code>true</code> - es exisitiert eine Foto
     */
    @Transient
    public boolean getFotoExists() {
    	return (fotoNormal!=null);
    }
    
	
	/* --------------- GET + SET ----------------------------*/
	public Long getId() {
        return this.id;
    }

	public void setId(Long id) {
        this.id = id;
    }

	public Date getVersion() {
        return this.version;
    }

	public void setVersion(Date version) {
        this.version = version;
    }


	public String getBetreff() {
        return this.betreff;
    }

	public void setBetreff(String betreff) {
        this.betreff = betreff;
    }

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}
    
    public String getFlurstueckseigentum() {
		return flurstueckseigentum;
	}

	public void setFlurstueckseigentum(String flurstueckseigentum) {
		this.flurstueckseigentum = flurstueckseigentum;
	}

	public String getDetails() {
        return this.details;
    }

	public void setDetails(String details) {
        this.details = details;
    }

	public EnumFreigabeStatus getBetreffFreigabeStatus() {
        return this.betreffFreigabeStatus;
    }

	public void setBetreffFreigabeStatus(EnumFreigabeStatus betreffFreigabeStatus) {
        this.betreffFreigabeStatus = betreffFreigabeStatus;
    }

	public EnumFreigabeStatus getDetailsFreigabeStatus() {
        return this.detailsFreigabeStatus;
    }

	public void setDetailsFreigabeStatus(EnumFreigabeStatus detailsFreigabeStatus) {
        this.detailsFreigabeStatus = detailsFreigabeStatus;
    }

	public Point getOvi() {
        return this.ovi;
    }

	public void setOvi(Point ovi) {
        this.ovi = ovi;
    }

	public EnumVorgangTyp getTyp() {
        return this.typ;
    }

	public void setTyp(EnumVorgangTyp typ) {
        this.typ = typ;
    }

	public Date getDatum() {
        return this.datum;
    }

	public void setDatum(Date datum) {
        this.datum = datum;
    }

	public String getAutorEmail() {
        return this.autorEmail;
    }

	public void setAutorEmail(String autorEmail) {
        this.autorEmail = autorEmail;
    }
  
  public Boolean autorIntern() {
    if(this.autorEmail == null) {
      return false;
    }
    return this.autorEmail.matches(settingsService.getPropertyValue("auth.internal_author_match"));
  }

  public void setSecurityService(SecurityService securityService) {
    this.securityService = securityService;
  }
  
  public Boolean autorAussendienst() {
    if(this.autorEmail == null || securityService == null) {
      return false;
    }
    User user = securityService.getUserByEmail(this.autorEmail);
    if(user == null) {
      return false;
    }
    List<String> teams = user.getAussendienstTeams();
    if(teams == null || teams.isEmpty()) {
      return false;
    }
    return true;
  }
  
  public Integer getTrustLevel() {
    int trust_level = 0;
    
    if(getStatus() != EnumVorgangStatus.gemeldet) {
      trust_level = 1;
      if(autorAussendienst()) {
        trust_level = 3;
      } else if(autorIntern()) {
        trust_level = 2;
      }
    }
    return trust_level;
  }
  
	public List<Kommentar> getKommentare() {
        return this.kommentare;
    }

	public void setKommentare(List<Kommentar> kommentare) {
        this.kommentare = kommentare;
    }

	public List<LobHinweiseKritik> getLobHinweiseKritik() {
        return this.lobHinweiseKritik;
    }

	public void setLobHinweiseKritik(List<LobHinweiseKritik> lobHinweiseKritik) {
        this.lobHinweiseKritik = lobHinweiseKritik;
    }

	public List<Verlauf> getVerlauf() {
        return this.verlauf;
    }

	public void setVerlauf(List<Verlauf> verlauf) {
        this.verlauf = verlauf;
    }

	public Kategorie getKategorie() {
        return this.kategorie;
    }

	public void setKategorie(Kategorie kategorie) {
        this.kategorie = kategorie;
    }

	public List<Unterstuetzer> getUnterstuetzer() {
        return this.unterstuetzer;
    }

  public Integer getUnterstuetzerCount() {
    if(this.unterstuetzer == null) {
      return 0;
    }
    int unt = 0;
    for(Unterstuetzer u : this.unterstuetzer) {
      if(u.getDatumBestaetigung() != null) {
        unt++;
      }
    }
    return unt;
  }

	public void setUnterstuetzer(List<Unterstuetzer> unterstuetzer) {
        this.unterstuetzer = unterstuetzer;
    }

	public String getZustaendigkeit() {
		return zustaendigkeit;
	}

	public void setZustaendigkeit(String zustaendigkeit) {
		this.zustaendigkeit = zustaendigkeit;
	}
    
    public String getZustaendigkeitFrontend() {
		return zustaendigkeitFrontend;
	}

	public void setZustaendigkeitFrontend(String zustaendigkeitFrontend) {
		this.zustaendigkeitFrontend = zustaendigkeitFrontend;
	}

	public void setZustaendigkeitStatus(EnumZustaendigkeitStatus zustaendigkeitStatus) {
		this.zustaendigkeitStatus = zustaendigkeitStatus;
	}

	public EnumVorgangStatus getStatus() {
		return status;
	}

	/**
	 * Die Methode setzt sowohl das Attribut <code>status</code> als auch das Attribut <code>statusOrdinal</code>
	 * @param status Vorgangsstatus
	 */
	public void setStatus(EnumVorgangStatus status) {
		this.status = status;
		this.statusOrdinal = status;
	}
  
  public Date getStatusDatum() {
    for(int i = verlauf.size() - 1; i >= 0; i--) {
      Verlauf v = verlauf.get(i);
      if(v.getTyp() == EnumVerlaufTyp.erzeugt || v.getTyp() == EnumVerlaufTyp.status) {
        return v.getDatum();
      }
    }
    
    return null;
  }

	public EnumZustaendigkeitStatus getZustaendigkeitStatus() {
		return zustaendigkeitStatus;
	}

	public EnumPrioritaet getPrioritaet() {
		return prioritaet;
	}

	/**
	 * Die Methode setzt sowohl das Attribut <code>prioritaet</code> als auch das Attribut <code>sprioritaetOrdinal</code>
	 * @param prioritaet
	 */
	public void setPrioritaet(EnumPrioritaet prioritaet) {
		this.prioritaet = prioritaet;
		this.prioritaetOrdinal = prioritaet;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getFotoGross() {
		if(fotoGross == null)
			return fotoNormal;
		else
			return fotoGross;
	}

	public void setFotoGross(String fotoGross) {
		this.fotoGross = fotoGross;
	}
    
	public String getFotoNormal() {
		return fotoNormal;
	}

	public void setFotoNormal(String fotoNormal) {
		this.fotoNormal = fotoNormal;
	}

	public String getFotoThumb() {
		return fotoThumb;
	}

	public void setFotoThumb(String fotoThumb) {
		this.fotoThumb = fotoThumb;
	}
    
	public EnumFreigabeStatus getFotoFreigabeStatus() {
		return fotoFreigabeStatus;
	}

	public void setFotoFreigabeStatus(EnumFreigabeStatus fotoFreigabeStatus) {
		this.fotoFreigabeStatus = fotoFreigabeStatus;
	}

	public List<Missbrauchsmeldung> getMissbrauchsmeldungen() {
		return missbrauchsmeldungen;
	}

	public void setMissbrauchsmeldungen(List<Missbrauchsmeldung> missbrauchsmeldungen) {
		this.missbrauchsmeldungen = missbrauchsmeldungen;
	}

	public String getStatusKommentar() {
		return statusKommentar;
	}

	public void setStatusKommentar(String statusKommentar) {
		this.statusKommentar = statusKommentar;
	}

	public String getDelegiertAn() {
		return delegiertAn;
	}

	public void setDelegiertAn(String delegiertAn) {
		this.delegiertAn = delegiertAn;
	}

	public Boolean getArchiviert() {
		return archiviert;
	}

	public void setArchiviert(boolean archiviert) {
		this.archiviert = archiviert;
	}

  public Boolean getFotowunsch() {
    return fotowunsch == null ? false : fotowunsch;
  }

  public void setFotowunsch(boolean fotowunsch) {
    this.fotowunsch = fotowunsch;
  }
  
	public boolean getErstsichtungErfolgt() {
		return erstsichtungErfolgt;
	}

	public void setErstsichtungErfolgt(boolean erstsichtungErfolgt) {
		this.erstsichtungErfolgt = erstsichtungErfolgt;
	}

  public Auftrag getAuftrag() {
    return auftrag;
  }

  public void setAuftrag(Auftrag auftrag) {
    this.auftrag = auftrag;
  }

  public String getAuftragTeam() {
    if(getAuftrag() == null) {
      return null;
    }
    return getAuftrag().getTeam();
  }
  
  public Date getAuftragDatum() {
    if(getAuftrag() == null) {
      return null;
    }
    return getAuftrag().getDatum();
  }

  public String getAuftragStatus() {
    if(getAuftrag() == null) {
      return null;
    }
    return getAuftrag().getStatus().getText();
  }
  
  public Integer getAuftragPrioritaet() {
    if(getAuftrag() == null) {
      return null;
    }
    return getAuftrag().getPrioritaet();
  }
  
}
