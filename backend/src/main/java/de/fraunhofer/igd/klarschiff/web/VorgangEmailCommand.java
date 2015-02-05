package de.fraunhofer.igd.klarschiff.web;

import java.io.Serializable;

import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Command für den Mailversand im Backend <br />
 * Beinhaltet ein Vorgangs-Objekt sowie Emaildetails (Absenderadresse und -name, Empfängeradresse und Mailtext).
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
public class VorgangEmailCommand implements Serializable {

	Vorgang vorgang;
	String fromEmail;
	String fromName;
	String toEmail;
	String text;
	
	boolean sendAutor = true;
	boolean sendKarte = true;
	boolean sendFoto = true;
	boolean sendKommentare = true;
	boolean sendMissbrauchsmeldungen = true;
	
	public Vorgang getVorgang() {
		return vorgang;
	}
	public void setVorgang(Vorgang vorgang) {
		this.vorgang = vorgang;
	}
	public String getFromEmail() {
		return fromEmail;
	}
	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}
	public String getFromName() {
		return fromName;
	}
	public void setFromName(String fromName) {
		this.fromName = fromName;
	}
	public String getToEmail() {
		return toEmail;
	}
	public void setToEmail(String toEmail) {
		this.toEmail = toEmail;
	}
	public String getText() {
		return text;
	}
	public boolean getSendKommentare() {
		return sendKommentare;
	}
	public void setSendKommentare(boolean sendKommentare) {
		this.sendKommentare = sendKommentare;
	}
	public boolean getSendFoto() {
		return sendFoto;
	}
	public void setSendFoto(boolean sendFoto) {
		this.sendFoto = sendFoto;
	}
	public void setText(String text) {
		this.text = text;
	}
	public boolean getSendKarte() {
		return sendKarte;
	}
	public void setSendKarte(boolean sendKarte) {
		this.sendKarte = sendKarte;
	}
	public boolean getSendMissbrauchsmeldungen() {
		return sendMissbrauchsmeldungen;
	}
	public void setSendMissbrauchsmeldungen(boolean sendMissbrauchsmeldungen) {
		this.sendMissbrauchsmeldungen = sendMissbrauchsmeldungen;
	}
	public boolean getSendAutor() {
		return sendAutor;
	}
	public void setSendAutor(boolean sendAutor) {
		this.sendAutor = sendAutor;
	}
}
