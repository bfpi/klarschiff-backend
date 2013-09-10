package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * VO für die Empfänger redaktioneller E-Mails
 * @author Sebastian Schwarz (Hansestadt Rostock)
 */
@SuppressWarnings("serial")
@Entity
public class RedaktionEmpfaenger implements Serializable {

	/* --------------- Attribute ----------------------------*/

	/**
	 * ID des Empfängers von redaktionellen E-Mails
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    /**
	 * Zugehörigkeit des Empfängers zu einer Zuständigkeit
	 */ 
	String zustaendigkeit;
    
    /**
	 * E-Mail-Adresse des Empfängers
	 */
    @NotNull
	String email;
    
    /**
     * Eskalationsstufe, in der der Empfänger redaktionelle E-Mails erhalten soll
     */
    @NotNull
    Short stufe;
    
    /**
     * Tage, die zwischen dem Versenden redaktioneller E-Mails an den Empfänger verstreichen sollen
     */
    @NotNull
    Short tageZwischenMails;
	
	/**
	 * Zeitpunkt des letzen Versandes einer redaktionellen E-Mail an den Empfänger
	 */
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    Date letzteMail;
	
	/**
	 * Soll Empfänger auch E-Mails mit Lob, Kritik und Hinweisen empfangen?
	 */
    Boolean empfaengerLobHinweiseKritik;
	
	/* --------------- GET + SET ----------------------------*/

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
    
    public String getZustaendigkeit() {
		return zustaendigkeit;
	}
	public void setZustaendigkeit(String zustaendigkeit) {
		this.zustaendigkeit = zustaendigkeit;
	}
    
    public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
    
    public Short getStufe() {
		return stufe;
	}
	public void setStufe(short stufe) {
		this.stufe = stufe;
	}
    
    public Short getTageZwischenMails() {
		return tageZwischenMails;
	}
	public void setTageZwischenMails(short tageZwischenMails) {
		this.tageZwischenMails = tageZwischenMails;
	}
    
    public Date getLetzteMail() {
		return letzteMail;
	}
	public void setLetzteMail(Date letzteMail) {
		this.letzteMail = letzteMail;
	}
    
    public Boolean getEmpfaengerLobHinweiseKritik() {
		return empfaengerLobHinweiseKritik;
	}
	public void setEmpfaengerLobHinweiseKritik(Boolean empfaengerLobHinweiseKritik) {
		this.empfaengerLobHinweiseKritik = empfaengerLobHinweiseKritik;
	}
}
