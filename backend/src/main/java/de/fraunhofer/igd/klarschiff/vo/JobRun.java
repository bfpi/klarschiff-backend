package de.fraunhofer.igd.klarschiff.vo;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * VO zum Registrieren von im Cluster synchronisierten Hintergrundjobs.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Entity
public class JobRun {

	/**
	 * Status bzw. Ergebnis des Jobs 
	 * @author Stefan Audersch (Fraunhofer IGD)
	 */
	public enum Ergebnis {gestartet, abgeschlossen, fehlerhaft };
	
	/* --------------- Attribute ----------------------------*/

	/**
	 * Id des Hintergrundjobs
	 */
	@Id
	String id;
	
	/**
	 * Name/Bezeichnung des Jobs
	 */
	String name;
	
	/**
	 * Startzeit des Jobs
	 */
	@DateTimeFormat(style = "S-")
	Date datum;

	/**
	 * ConnectorPort des Servers, der den Job ausführt
	 */
	String serverPort;
	
	/**
	 * IPs des Servers, der den Job ausführt
	 */
	String serverIp;
	
	/**
	 * Rechnername des Servers, der den Job ausführt
	 */
	String serverName;
	
	/**
	 * aktueller Status bzw. Ergebnis des Jobs 
	 */
    @NotNull
    @Enumerated(EnumType.STRING)
	Ergebnis ergebnis = Ergebnis.gestartet;
	
    /**
     * Fehlermeldung des Jobs
     */
    @Lob
    @Type(type="org.hibernate.type.TextType")
	String fehlermeldung;

	/* --------------- GET + SET ----------------------------*/

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public Date getDatum() {
		return datum;
	}

	public void setDatum(Date datum) {
		this.datum = datum;
	}

	public Ergebnis getErgebnis() {
		return ergebnis;
	}

	public void setErgebnis(Ergebnis ergebnis) {
		this.ergebnis = ergebnis;
	}

	public String getFehlermeldung() {
		return fehlermeldung;
	}

	public void setFehlermeldung(String fehlermeldung) {
		this.fehlermeldung = fehlermeldung;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
}
