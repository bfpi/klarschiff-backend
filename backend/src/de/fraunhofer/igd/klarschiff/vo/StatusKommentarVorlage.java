package de.fraunhofer.igd.klarschiff.vo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.apache.commons.lang.StringUtils;

/**
 * VO zum Abbilden der Vorlagen f�r den Statuskommentar
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@Entity
public class StatusKommentarVorlage {

	/* --------------- Attribute ----------------------------*/

	/**
	 * Id der Vorlagen f�r den Statuskommentar
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	/**
	 * Titel der Vorlagen f�r den Statuskommentar
	 */
	@Size(max = 100)
    private String titel;

	/**
	 * Text der Vorlagen f�r den Statuskommentar
	 */
	@Size(max = 300)
	private String text;

	/* --------------- TRANSIENT ----------------------------*/
	
	/**
	 * Lesen des Titels der Vorlagen f�r den Statuskommentar. titel mit einer L�nge �ber 20 Zeichen werden abgeschnitten und mit "..." beendet.
	 */
	@Transient
	public String getTitelAbbreviate() {
		String str = (!StringUtils.isBlank(titel)) ? titel : text;
		return StringUtils.abbreviate(StringUtils.replace(str,"\n", " "), 20);
	}
	
	
	/* --------------- GET + SET ----------------------------*/

	public Long getId() {
        return this.id;
	}

	public void setId(Long id) {
        this.id = id;
    }

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTitel() {
		return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}
}
