package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * VO für die Abbildung von Kommentaren.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
@Entity
public class Kommentar implements Serializable {

	/* --------------- Attribute ----------------------------*/

	/**
	 * Id des Kommentars
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	/**
	 * Vorgang zu dem der Kommentar gehört
	 */
    @NotNull
    @ManyToOne
    private Vorgang vorgang;

    /**
     * Text des Kommentars
     */
    @NotNull
    @Lob
    @Type(type="org.hibernate.type.TextType")
    private String text;

    /**
     * Erstellungszeit des Kommentars
     */
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date datum;
	
    /**
     * Letzte Bearbeitung des Kommentars
     */
    @Version
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    @Column(columnDefinition = "timestamp default current_timestamp")
	private Date zuletztBearbeitet;
	
	@NotNull
	@Column(columnDefinition = "integer default 0")
	private Integer anzBearbeitet;

    /**
     * Id des Benutzer, der den Kommentar erstellt hat
     */
	private String nutzer; 

    @Column(columnDefinition = "boolean default false")
    private boolean geloescht = false;

	/* --------------- GET + SET ----------------------------*/

	public Long getId() {
        return this.id;
    }

	public void setId(Long id) {
        this.id = id;
    }

	public Vorgang getVorgang() {
        return this.vorgang;
    }

	public void setVorgang(Vorgang vorgang) {
        this.vorgang = vorgang;
    }

	public String getText() {
        return this.text;
    }

	public void setText(String text) {
        this.text = text;
    }

	public Date getDatum() {
        return this.datum;
    }

	public void setDatum(Date datum) {
        this.datum = datum;
    }

    public Date getZuletztBearbeitet() {
        return this.zuletztBearbeitet;
    }

    public void setZuletztBearbeitet(Date datum) {
        this.zuletztBearbeitet = datum;
    }

	public Integer getAnzBearbeitet() {
		return anzBearbeitet;
	}

	public void setAnzBearbeitet(Integer anzBearbeitet) {
		this.anzBearbeitet = anzBearbeitet;
	}

	public String getNutzer() {
		return nutzer;
	}

	public void setNutzer(String nutzer) {
		this.nutzer = nutzer;
	}
	
	public boolean getGeloescht() {
		return geloescht;
	}

	public void setGeloescht(boolean geloescht) {
		this.geloescht = geloescht;
	}
}
