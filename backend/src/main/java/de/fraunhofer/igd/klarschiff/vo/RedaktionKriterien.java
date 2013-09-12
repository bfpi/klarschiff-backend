package de.fraunhofer.igd.klarschiff.vo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * VO für die Redaktionskriterien
 * @author Sebastian Schwarz (Hansestadt Rostock)
 */
@SuppressWarnings("serial")
@Entity
public class RedaktionKriterien implements Serializable {

	/* --------------- Attribute ----------------------------*/

	/**
	 * ID des Redaktionskriteriums
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    /**
     * Eskalationsstufe
     */
    @NotNull
    Short stufe;
    
    /**
     * Tage, die Vorgänge mit dem Status 'offen', die bisher nicht akzeptiert wurden, mindestens zugewiesen sein sollen
     */
    @NotNull
    Short tageOffenNichtAkzeptiert;
    
    /**
     * Tage, die Vorgänge mit dem Status 'in Bearbeitung', die bisher keine Info der Verwaltung aufweisen, unverändert geblieben sein sollen
     */
    @NotNull
    Short tageInbearbeitungOhneStatusKommentar;
    
    /**
     * Tage, die Ideen mit dem Status 'offen', die bisher nicht die Zahl der notwendigen Unterstützungen aufweisen, seit der Erstsichtung überdauert haben sollen
     */
    @NotNull
    Short tageIdeeOffenOhneUnterstuetzung;
    
    /**
     * Vorgänge ausweisen mit dem Status 'wird nicht bearbeitet', die bisher keine Info der Verwaltung aufweisen?
     */
    @NotNull
    Boolean wirdnichtbearbeitetOhneStatuskommentar;
    
    /**
     * Vorgänge ausweisen mit dem Status 'offen', die bisher nicht akzeptiert wurden?
     */
    @NotNull
    Boolean nichtMehrOffenNichtAkzeptiert;
    
    /**
     * Vorgänge ausweisen, die ihre Erstsichtung bereits hinter sich haben, deren Betreff, Details und/oder Foto aber noch nicht freigegeben wurde?
     */
    @NotNull
    Boolean ohneRedaktionelleFreigaben;
    
    /**
     * Vorgänge ausweisen, die auf Grund von Kommunikationsfehlern keine Einträge in den Datenfeldern 'zustaendigkeit' und 'zustaendigkeit_status' aufweisen?
     */
    @NotNull
    Boolean ohneZustaendigkeit;
	
	/* --------------- GET + SET ----------------------------*/

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
    
    public Short getStufe() {
		return stufe;
	}
	public void setStufe(short stufe) {
		this.stufe = stufe;
	}
    
    public Short getTageOffenNichtAkzeptiert() {
		return tageOffenNichtAkzeptiert;
	}
	public void setTageOffenNichtAkzeptiert(short tageOffenNichtAkzeptiert) {
		this.tageOffenNichtAkzeptiert = tageOffenNichtAkzeptiert;
	}
    
    public Short getTageInbearbeitungOhneStatusKommentar() {
		return tageInbearbeitungOhneStatusKommentar;
	}
	public void setTageInbearbeitungOhneStatusKommentar(short tageInbearbeitungOhneStatusKommentar) {
		this.tageInbearbeitungOhneStatusKommentar = tageInbearbeitungOhneStatusKommentar;
	}
    
    public Short getTageIdeeOffenOhneUnterstuetzung() {
		return tageIdeeOffenOhneUnterstuetzung;
	}
	public void setTageIdeeOffenOhneUnterstuetzung(short tageIdeeOffenOhneUnterstuetzung) {
		this.tageIdeeOffenOhneUnterstuetzung = tageIdeeOffenOhneUnterstuetzung;
	}
    
    public Boolean getWirdnichtbearbeitetOhneStatuskommentar() {
		return wirdnichtbearbeitetOhneStatuskommentar;
	}
	public void setWirdnichtbearbeitetOhneStatuskommentar(boolean wirdnichtbearbeitetOhneStatuskommentar) {
		this.wirdnichtbearbeitetOhneStatuskommentar = wirdnichtbearbeitetOhneStatuskommentar;
	}
    
    public Boolean getNichtMehrOffenNichtAkzeptiert() {
		return nichtMehrOffenNichtAkzeptiert;
	}
	public void setNichtMehrOffenNichtAkzeptiert(boolean nichtMehrOffenNichtAkzeptiert) {
		this.nichtMehrOffenNichtAkzeptiert = nichtMehrOffenNichtAkzeptiert;
	}
    
    public Boolean getOhneRedaktionelleFreigaben() {
		return ohneRedaktionelleFreigaben;
	}
	public void setOhneRedaktionelleFreigaben(boolean ohneRedaktionelleFreigaben) {
		this.ohneRedaktionelleFreigaben = ohneRedaktionelleFreigaben;
	}
    
    public Boolean getOhneZustaendigkeit() {
		return ohneZustaendigkeit;
	}
	public void setOhneZustaendigkeit(boolean ohneZustaendigkeit) {
		this.ohneZustaendigkeit = ohneZustaendigkeit;
	}
}
