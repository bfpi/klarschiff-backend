package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.Date;

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
    @Version
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date datum;

    /**
     * Id des Benutzer, der den Kommentar erstellt hat
     */
	private String nutzer; 

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

	public String getNutzer() {
		return nutzer;
	}

	public void setNutzer(String nutzer) {
		this.nutzer = nutzer;
	}
}
