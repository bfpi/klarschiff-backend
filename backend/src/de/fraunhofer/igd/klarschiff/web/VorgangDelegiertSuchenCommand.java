package de.fraunhofer.igd.klarschiff.web;

import java.io.Serializable;

/**
 * Command für die Vorgangsuche im Backend für Externe (Delegierte)<br />
 * Beinhaltet Suchfeld für einfache Suche sowie Attribute für die Ergebnisdarstellung: <br/>
 * <code>page</code>: die aktuelle Seitenzahl<br/>
 * <code>size</code>: die konfigurierte Anzahl von Einträgen pro Seite<br/>
 * <code>order</code>: die Spalte nach der sortiert wird<br/>
 * <code>orderDirection</code>: die Sortierreihenfolge (1:absteigend,default:aufsteigend)
 * @author Stefan Audersch (Fraunhofer IGD)
 */
@SuppressWarnings("serial")
public class VorgangDelegiertSuchenCommand implements Serializable {

	public enum EinfacheSuche { offene, abgeschlossene };

	Integer page;
	Integer size;
	Integer order;
	Integer orderDirection;
		
	// einfach Suche
	EinfacheSuche einfacheSuche;

	public String getOrderString() {
		switch(order) {
			case 0: return "vo.id";
			case 1: return "vo.typ";
			case 2: return "vo.datum";
			case 3: return "vo.kategorie.parent.name,vo.kategorie.name";
			case 4: return "vo.statusOrdinal";
			case 5: return "vo.zustaendigkeit";
			case 6: return "vo.prioritaetOrdinal";
			default: return "";
		}
	}
	
	public String getOrderDirectionString() {
		switch(orderDirection) {
		case 1: return "desc";
		default: return "asc";
	}
	}
	
	
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

	public EinfacheSuche getEinfacheSuche() {
		return einfacheSuche;
	}

	public void setEinfacheSuche(EinfacheSuche einfacheSuche) {
		this.einfacheSuche = einfacheSuche;
	}
}
