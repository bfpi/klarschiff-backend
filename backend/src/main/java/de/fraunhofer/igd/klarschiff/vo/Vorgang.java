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
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

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
	

	@Size(max = 300)
    private String adresse;


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
	@Size(max = 500)
    private String statusKommentar;
    
	/**
	 * Erstsichtung erfolgt
	 */
	private boolean erstsichtungErfolgt = false;

	/**
	 * Foto
	 */
    @Type(type="org.hibernate.type.BinaryType") 
    byte[] fotoNormalJpg;

    /**
     * Foto des Vorganges als Vorschaubild
     */
    @Type(type="org.hibernate.type.BinaryType") 
    byte[] fotoThumbJpg;
    
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
	 * Delegiert an (Id der Rolle)
	 */
	String delegiertAn;
	
	/**
	 * Liste der Kommentare
	 */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vorgang")
    private List<Kommentar> kommentare = new ArrayList<Kommentar>();

    /**
     * Liste der Verlaufseinträge
     */
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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vorgang")
    private List<Unterstuetzer> unterstuetzer = new ArrayList<Unterstuetzer>();

    /**
     * Liste der Missbrauchsmeldungen
     */
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
    
	/* --------------- transient ----------------------------*/
    
    @Transient
    private static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 25833);
    
    @Transient
    private static WKTReader wktReader = new WKTReader(geometryFactory);
    
    @Transient
    private static WKTWriter wktWriter = new WKTWriter();

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
     * Existiert ein Foto zum Vorgang?
     * @return <code>true</code> - es exisitiert eine Foto
     */
    @Transient
    public boolean getFotoExists() {
    	return (fotoNormalJpg!=null);
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

	public List<Kommentar> getKommentare() {
        return this.kommentare;
    }

	public void setKommentare(List<Kommentar> kommentare) {
        this.kommentare = kommentare;
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

	public void setUnterstuetzer(List<Unterstuetzer> unterstuetzer) {
        this.unterstuetzer = unterstuetzer;
    }

	public String getZustaendigkeit() {
		return zustaendigkeit;
	}

	public void setZustaendigkeit(String zustaendigkeit) {
		this.zustaendigkeit = zustaendigkeit;
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

	public byte[] getFotoNormalJpg() {
		return fotoNormalJpg;
	}

	public void setFotoNormalJpg(byte[] fotoNormalJpg) {
		this.fotoNormalJpg = fotoNormalJpg;
	}

	public byte[] getFotoThumbJpg() {
		return fotoThumbJpg;
	}

	public void setFotoThumbJpg(byte[] fotoThumbJpg) {
		this.fotoThumbJpg = fotoThumbJpg;
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

	public boolean getErstsichtungErfolgt() {
		return erstsichtungErfolgt;
	}

	public void setErstsichtungErfolgt(boolean erstsichtungErfolgt) {
		this.erstsichtungErfolgt = erstsichtungErfolgt;
	}
}
