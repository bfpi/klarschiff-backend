package de.fraunhofer.igd.klarschiff.vo.extra;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import de.fraunhofer.igd.klarschiff.vo.EnumText;

/**
 * Die Klasse legt fest, wie die Daten der Enums in der DB abgelegt werden sollen.
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@MappedSuperclass
public class EnumBean {

	/* --------------- Attribute ----------------------------*/

	/**
	 * Id bzw. Name des Enums
	 */
	@Id
	String id;
	
	/**
	 * Text des Enums
	 */
	String text;
	
	/**
	 * Enum als Ordinal bzw. Integer
	 */
	int ordinal;

	/* --------------- transient ----------------------------*/

	/**
	 * Hilfsfunktion zum einfachen Erzeugen der (Hilfs-)Objekte des Enums ausgehend von den ursprünglichen Enums
	 * @param enumText ursprüngliches Enum 
	 */
	@Transient
	public EnumBean fill(EnumText enumText) {
		id = enumText.name();
		ordinal = enumText.ordinal();
		text = enumText.getText();
		return this;
	}

	/* --------------- GET + SET ----------------------------*/

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}
}
