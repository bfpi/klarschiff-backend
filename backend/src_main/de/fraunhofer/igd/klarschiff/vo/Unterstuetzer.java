package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * VO zum Abbilden der Unterstützer/Unterstützungen
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
@Entity
public class Unterstuetzer implements Serializable {

	/* --------------- Attribute ----------------------------*/

	/**
	 * Id der Unterstützung
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * Vorgang zu dem die Unterstützung gehört
	 */
	@ManyToOne
    private Vorgang vorgang;
	
	/**
	 * Hash zum Bestatigen der Unterstützung
	 */
	@Size(max = 32)
    private String hash;
    
	/**
	 * Erstellungszeit der Unterstützung
	 */
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date datum;

    /**
     * Bestätigungszeit der Unterstützung
     */
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date datumBestaetigung;

	/* --------------- GET + SET ----------------------------*/

	public Vorgang getVorgang() {
		return vorgang;
	}

	public void setVorgang(Vorgang vorgang) {
		this.vorgang = vorgang;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Date getDatum() {
		return datum;
	}

	public void setDatum(Date datum) {
		this.datum = datum;
	}

	public Date getDatumBestaetigung() {
		return datumBestaetigung;
	}

	public void setDatumBestaetigung(Date datumBestaetigung) {
		this.datumBestaetigung = datumBestaetigung;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
