package de.fraunhofer.igd.klarschiff.web;

import java.text.DecimalFormat;

import org.apache.commons.lang.StringEscapeUtils;

@SuppressWarnings("serial")
public class BackendControllerException extends Exception {
	
	private int code;
	private String message = "Es ist ein Fehler aufgetreten.";
	
	public BackendControllerException(int code, String detailedmessage, Throwable exception)
	{
		super(detailedmessage, exception);
		this.code = code;
	}
	public BackendControllerException(int code, String detailedmessage, String message, Throwable exception)
	{
		super(detailedmessage, exception);
		this.code = code;
		this.message = message;
	}
	public BackendControllerException(int code, String detailedmessage)
	{
		super(detailedmessage);
		this.code = code;
	}
	public BackendControllerException(int code, String detailedmessage, String message)
	{
		super(detailedmessage);
		this.code = code;
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return new DecimalFormat("000").format(code)+"#"+StringEscapeUtils.escapeHtml(super.getMessage())+"#"+StringEscapeUtils.escapeHtml(message)+" (Fehler "+new DecimalFormat("000").format(code)+")";
	}
}
