package de.fraunhofer.igd.klarschiff.service.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.fraunhofer.igd.klarschiff.context.AppContext;

/**
 * Bean zum Abbilden der Daten eines Benutzers aus dem LDAP. die Bean enthält zusätzlich weitere Funktionen,
 * um auf die Rollen des Benutzers leicht zugreifen zu können.
 * @see ContextMapper 
 * @author Stefan Audersch (Fraunhofer IGD)
 */
public class User {
	String id;
	String name;
	String email;
	String dn;
	
	
	/**
	 * Ermittelt die Zuständigkeiten des Benutzers.
	 * @return Zuständigkeiten des Benutzers
	 */
	public List<Role> getZustaendigkeiten() {
		return AppContext.getApplicationContext().getBean(SecurityService.class).getZustaendigkeiten(id, true);
	}

	
	/**
	 * Ermittelt die DelgiertAn-Rollen des Benutzers.
	 * @return DelgiertAn-Rollen des Benutzers
	 */
	public List<Role> getDelegiertAn() {
		return AppContext.getApplicationContext().getBean(SecurityService.class).getDelegiertAn(id);
	}
	
	
	/**
	 * Ermittelt, ob der Benutzer ein externer Nutzer ist.
	 * @return <code>true</code> - externe Nutzer
	 */
	public boolean getUserExtern() {
		return AppContext.getApplicationContext().getBean(SecurityService.class).isUserExtern(id);
	}
	

	/**
	 * Ermittelt, ob der Benutzer ein interne Nutzer ist.
	 * @return <code>true</code> - interne Nutzer
	 */
	public boolean getUserIntern() {
		return AppContext.getApplicationContext().getBean(SecurityService.class).isUserIntern(id);
	}
    
    /**
	 * Ermittelt, ob der Benutzer ein Admin ist.
	 * @return <code>true</code> - Admins
	 */
	public boolean getUserAdmin() {
		return AppContext.getApplicationContext().getBean(SecurityService.class).isUserAdmin(id);
	}
    
    /**
	 * Ermittelt, ob der Benutzer ein Koordinator ist.
	 * @return <code>true</code> - Admins
	 */
	public boolean getUserKoordinator() {
		return AppContext.getApplicationContext().getBean(SecurityService.class).isUserKoordinator(id);
	}    
    
    public static List<String> toString(Collection<User> users) {
		List<String> _users = new ArrayList<String>();
		for (User user : users) _users.add(user.getName());
		return _users;
	}
	
	/**
	 * Ermittelt die Zuständigkeiten des Benutzers.
	 * @return Zuständigkeiten des Benutzers
	 */
	public List<String> getAussendienstKoordinatorZustaendigkeiten() {
		return AppContext.getApplicationContext().getBean(SecurityService.class).getAussendienstKoordinatorZustaendigkeiten(id);
	}
	
	/**
	 * Ermittelt die Zuständigkeiten des Benutzers.
	 * @return Zuständigkeiten des Benutzers
	 */
	public List<String> getAussendienstTeams() {
		return AppContext.getApplicationContext().getBean(SecurityService.class).getAussendienstTeam(id);
	}
    
	
    /* --------------- GET + SET ----------------------------*/

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDn() {
		return dn;
	}
	public void setDn(String dn) {
		this.dn = dn;
	}
}
