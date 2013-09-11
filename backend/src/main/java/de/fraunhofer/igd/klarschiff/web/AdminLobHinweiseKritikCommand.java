package de.fraunhofer.igd.klarschiff.web;

import java.io.Serializable;

/**
 * Command f�r Lob/Hinweise/Kritik im Adminbereich
 * <code>page</code>: die aktuelle Seitenzahl<br/>
 * <code>size</code>: die konfigurierte Anzahl von Eintr�gen pro Seite<br/>
 * <code>order</code>: die Spalte nach der sortiert wird<br/>
 * <code>orderDirection</code>: die Sortierreihenfolge (1:absteigend,default:aufsteigend)
 * @author Sebastian Schwarz (Hansestadt Rostock)
 */
@SuppressWarnings("serial")
public class AdminLobHinweiseKritikCommand implements Serializable {

	/* --------------- Attribute ----------------------------*/

	Integer page;
	Integer size;
	Integer order;
	Integer orderDirection;
    
    public String getOrderString() {
		switch(order) {
			case 0: return "o.id";
			case 1: return "o.vorgang";
			case 2: return "o.datum";
			case 3: return "o.autorEmail";
			case 4: return "o.empfaengerEmail";
			case 5: return "o.freitext";
			default: return "";
		}
	}
	
	public String getOrderDirectionString() {
		switch(orderDirection) {
            case 1: return "desc";
            default: return "asc";
        }
	}
	
	/* --------------- GET + SET ----------------------------*/

	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
    public Integer getOrder() {
		return order;
	}
	public void setOrder(Integer order) {
		this.order = order;
	}
	public Integer getOrderDirection() {
		return orderDirection;
	}
	public void setOrderDirection(Integer orderDirection) {
		this.orderDirection = orderDirection;
	}
	
	
}