package de.fraunhofer.igd.klarschiff.service.security;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import de.fraunhofer.igd.klarschiff.dao.VorgangDao;
import de.fraunhofer.igd.klarschiff.util.SystemUtil;
import de.fraunhofer.igd.klarschiff.vo.Kommentar;
import de.fraunhofer.igd.klarschiff.vo.Vorgang;

/**
 * Die Klasse stellt einen Service bereit über den die Daten zu Benutzer und deren Rollen bzw. Zuständigkeiten auf der Basis des LDAP ermittelt werden können.
 * Anfragen an das LDAP werden dabei i.d.R. mit Hilfe der Klasse <code>SecurityServiceLdap</code> gecacht, um wiederholte Anfragen an das LDAP zu vermeiden.
 * @author Stefan Audersch (Fraunhofer IGD)
 * @author Hani Samara (Fraunhofer IGD)
 */
@Service
public class SecurityService {
	static final Logger logger = Logger.getLogger(SecurityService.class);
	
	@Autowired
	VorgangDao vorgangDao;
	
	SecurityServiceLdap securityServiceLdap;
	
	String root;
	String userAttributesMapping;
	String roleAttributesMapping;
	String userSearchBase;
	String userObjectClass;
	String userSearchFilter;
	String groupSearchBase;
	String groupObjectClass;
	String groupSearchFilter;
	String groupObjectId;
	String groupRoleAttribute;
	String groupIntern = "intern";
	String groupExtern = "extern";
	String groupAdmin = "admin";
	String groupDispatcher = "dispatcher";
	
	private ContextMapper<User> userContextMapper;
	private ContextMapper<Role> roleContextMapper;
	private UserLoginContextMapper userLoginContextMapper;
	
	static final String FS = System.getProperty("file.separator");

	/**
	 * Initialisiert den Service. Dabei werden die Mapper für Benutzer und Rollen initialisiert.
	 */
	@PostConstruct
	public void init() {
		userContextMapper = new ContextMapper<User>(User.class, userAttributesMapping, root);
		roleContextMapper = new ContextMapper<Role>(Role.class, roleAttributesMapping, root);
		userLoginContextMapper = new UserLoginContextMapper(groupSearchFilter);
	}
	
	
	/**
	 * Ermittelt ob der aktuelle Benutzer Adminrechte hat.
	 * @return <code>true</code> - der Benutzer hat Adminrechte
	 */
	public boolean isCurrentUserAdmin() {
		for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities())
			if (authority.getAuthority().equals("ROLE_ADMIN")) return true;
		return false;
	}
	
	
	/**
	 * Ermittelt ob der Benutzer Adminrechte hat.
	 * @param login Benutzer, für den überprüft werden soll, ob er Adminrechte hat.
	 * @return <code>true</code> - der Benutzer hat Adminrechte
	 */
	public boolean isUserAdmin(String login) {
		User user = getUser(login);
		if (user==null) return false;
		List<Role> role = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+StringUtils.replace(groupSearchFilter, "{0}", user.getDn())+")("+groupRoleAttribute+"="+groupAdmin+"))", roleContextMapper);
		return (role.size()==0) ? false : true;
	}
	
	
	/**
	 * Ermittelt ob der aktuelle Benutzer ein Dispatcher ist.
	 * @return <code>true</code> - der Benutzer ist ein Dispatcher
	 */
	public boolean isCurrentUserDispatcher() {
		for (GrantedAuthority authority : SecurityContextHolder.getContext().getAuthentication().getAuthorities())
			if (authority.getAuthority().equals("ROLE_DISPATCHER")) return true;
		return false;
	}
	
	
	/**
	 * Ermittelt ob der Benutzer ein Dispatcher ist.
	 * @param login Benutzer, für den überprüft werden soll, ob er ein Dispatcher ist.
	 * @return <code>true</code> - der Benutzer ist ein Dispatcher
	 */
	public boolean isUserDispatcher(String login) {
		User user = getUser(login);
		if (user==null) return false;
		List<Role> role = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+StringUtils.replace(groupSearchFilter, "{0}", user.getDn())+")("+groupRoleAttribute+"="+groupDispatcher+"))", roleContextMapper);
		return (role.size()==0) ? false : true;
	}
	
	
	/**
	 * Ermittelt für den aktuellen Benutzer die Benutzerdaten.
	 * @return Benutzerdaten des Benutzers; <code>null</code> wenn die Benutzerdaten nicht ermittelt werden konnten
	 */
	public User getCurrentUser() {
		try {
			return getUser(SecurityContextHolder.getContext().getAuthentication().getName());
		} catch (Exception e) {
			return null;
		}
	}

	
	/**
	 * Ermittelt die Benutzerdaten für einen Benutzer.
	 * @param login Benutzer für den die Benutzerdaten ermittelt werden sollen
	 * @return Benutzerdaten für den Benutzer; <code>null</code> wenn die Benutzerdaten nicht ermittelt werden konnten
	 */
	public User getUser(String login) {
		try {
			List<User> users = securityServiceLdap.getObjectListFromLdap(userSearchBase, "(&(objectclass="+userObjectClass+")("+StringUtils.replace(userSearchFilter, "{0}", login)+"))", userContextMapper);
			return users.get(0);
		} catch (Exception e) {
			return null;
		}
	}

	
	/**
	 * Ermittelt die Benutzer-E-Mail-Adresse für einen Benutzer in einer gegebenen Rolle anhand des Benutzernamens.
	 * @param userName Benutzername, für den die Benutzer-E-Mail-Adresse ermittelt werden soll
	 * @param roleId Rolle, auf die die Suche beschränkt werden soll
	 * @return Benutzer-E-Mail-Adresse; <code>null</code> wenn die Benutzer-E-Mail-Adresse nicht ermittelt werden konnte
	 */
	public String getUserEmailForRoleByName(String userName, String roleId) {
		String userEmail = new String();
		List<User> allUsersForRole = getAllUserForRole(roleId);
        
        for (User user : allUsersForRole) {
            if (user.getName().equals(userName))
                userEmail = user.getEmail();
        }
        
        if (userEmail != null || userEmail != "")
            return userEmail;
        else
            return null;
	}
	
	
	/**
	 * Ermittelt alle Benutzer, die für das Backend einen Zugang haben.
	 * @return List der Benutzer und deren Benutzerdaten
	 */
	public List<User> getAllUser(){
		//alle UserLogins in den Rollen ermitteln
		List<List<String>> usersLoginList = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(objectclass="+groupObjectClass+")", userLoginContextMapper);
		//Set
		Set<String> userLoginSet = new HashSet<String>();
		for(List<String> list : usersLoginList)
			userLoginSet.addAll(list);
		//User ermitteln
		List<User> userList = new ArrayList<User>();
		for(Iterator<String> iter = userLoginSet.iterator(); iter.hasNext(); )
			userList.add(getUser(iter.next()));
        
        Collections.sort(userList, new Comparator<User>() {
            public int compare(User u1, User u2) {
                return u1.getId().compareTo(u2.getId());
            }
        });
        
		return userList;
	}
	
	
	/**
	 * Ermittelt alle Benutzer für eine gegebene Rolle.
	 * @param roleId Rolle, für die die Benutzer ermittelt werden sollen
	 * @return Liste von Benutzern
	 */
	public List<User> getAllUserForRole(String roleId){
		//alle UserLogins in den Rollen ermitteln
		List<List<String>> usersLoginList = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+groupObjectId+"="+roleId+"))", userLoginContextMapper);
		//Set
		Set<String> userLoginSet = new HashSet<String>();
		for(List<String> list : usersLoginList)
			userLoginSet.addAll(list);
		//User ermitteln
		List<User> userList = new ArrayList<User>();
		for(Iterator<String> iter = userLoginSet.iterator(); iter.hasNext(); )
			userList.add(getUser(iter.next()));
		return userList;
	}
	
	
	/**
	 * Ermittelt alle Benutzernamen für eine gegebene Rolle.
	 * @param roleId Rolle, für die die Benutzer ermittelt werden sollen
	 * @return Liste von Benutzernamen
	 */
	public List<String> getAllUserNamesForRole(String roleId){
		//alle UserLogins in den Rollen ermitteln
		List<List<String>> usersLoginList = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+groupObjectId+"="+roleId+"))", userLoginContextMapper);
		//Set
		Set<String> userLoginSet = new HashSet<String>();
		for(List<String> list : usersLoginList)
			userLoginSet.addAll(list);
		//User ermitteln
		List<String> userNameList = new ArrayList<String>();
		for(Iterator<String> iter = userLoginSet.iterator(); iter.hasNext(); )
			userNameList.add(getUser(iter.next()).getName());
		return userNameList;
	}

	
	/**
	 * Ermittelt die E-Mailadressen der Benutzer für eine Rolle
	 * @param roleId Rolle, für den die Benutzer und dann deren E-Mailadressen ermittelt werden sollen
	 * @return Array mit E-Mailadressen
	 */
	public String[] getAllUserEmailsForRole(String roleId) {
		List<String> reciever = new ArrayList<String>();
		for (User user : getAllUserForRole(roleId))
			if (!StringUtils.isBlank(user.getEmail()))
				reciever.add(user.getEmail());
		return reciever.toArray(new String[0]);
	}
	
	
	/**
	 * Ermittelt die Rolle für die Dispatcher.
	 * @return Rolle für die Dispatcher
	 */
	public Role getDispatcherZustaendigkeit() {
		return getZustaendigkeit(groupDispatcher);
	}
	
	
	/**
	 * Gibt die Id für die Rolle der Dispatcher zurück.
	 * @return Id für die Rolle der Dispatcher
	 */
	public String getDispatcherZustaendigkeitId() {
		return groupDispatcher;
	}

	
	/**
	 * Ermittelt die Rollendaten für eine Zuständigkeit.
	 * @param id Id der Rolle bzw. Zuständigkeit
	 * @return ermittelte Rolle 
	 */
	public Role getZustaendigkeit(String id) {
		try {
			List <Role> roles = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+groupObjectId+"="+id+"))", roleContextMapper);
			return roles.get(0);
		} catch (Exception e) {
			return null;
		}
	}
    
    
    /**
	 * Ermittelt die Liste der Zuständigkeiten für den aktuellen Benutzer.  
	 * @param inclDispatcher incl. der Dispatcherrolle?
	 * @return Liste mit den Zuständigkeiten
	 */
	public List<Role> getCurrentZustaendigkeiten(boolean inclDispatcher) {
		return getZustaendigkeiten(SecurityContextHolder.getContext().getAuthentication().getName(), inclDispatcher);
	}
	
	/**
	 * Ermittelt die Liste der Zuständigkeiten für einen Benutzer
	 * @param login Benutzer für den die Zuständigkeiten ermittelt werden sollen
	 * @param inclDispatcher incl. der Dispatcherrolle?
	 * @return Liste mit den Zuständigkeiten
	 */
	public List<Role> getZustaendigkeiten(String login, boolean inclDispatcher) {
		if (isUserAdmin(login)) {
			return getAllZustaendigkeiten(inclDispatcher);
		} else {
			User user = getUser(login);
			if (user==null) throw new RuntimeException();
			String dispatcherFilter = inclDispatcher ? "" : "(!("+groupObjectId+"="+groupDispatcher+"))";

			return securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+groupRoleAttribute+"="+groupIntern+")"+dispatcherFilter+"("+StringUtils.replace(groupSearchFilter, "{0}", user.getDn())+"))", roleContextMapper);
		}
	}

	
	/**
	 * Ermittelt ob ein Benutzer für ein Vorgang zuständig ist.
	 * @param login Benutzer
	 * @param vorgang Vorgang
	 * @return <code>true</code> - Benutzer ist für den Vorgang zuständig
	 */
	public boolean isZustaendigForVorgang(String login, Vorgang vorgang) {
		for (Role zustaendigkeit : getZustaendigkeiten(login, true))
			if (StringUtils.equals(zustaendigkeit.getId(), vorgang.getZustaendigkeit())) return true;
		return false;
	}

	
	/**
	 * Ermittelt ob der aktuelle Benutzer für ein Vorgang zuständig ist.
	 * @param vorgang Vorgang
	 * @return <code>true</code> - aktueller Benutzer ist für den Vorgang zuständig
	 */
	public boolean isCurrentZustaendigForVorgang(Vorgang vorgang) {
		return isZustaendigForVorgang(SecurityContextHolder.getContext().getAuthentication().getName(), vorgang);
	}
	
    
    /**
	 * Ermittelt alle im System vorhandenen Zuständigkeiten.
	 * @param inclDispatcher incl. der Dispatcherrolle?
	 * @return Liste mit allen Zuständigkeiten
	 */
	public List<Role> getAllZustaendigkeiten(boolean inclDispatcher) {
		String dispatcherFilter = inclDispatcher ? "" : "(!("+groupObjectId+"="+groupDispatcher+"))";
		List <Role> allZustaendigkeiten = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+groupRoleAttribute+"="+groupIntern+")"+dispatcherFilter+")", roleContextMapper);
        
        Collections.sort(allZustaendigkeiten, new Comparator<Role>() {
            public int compare(Role r1, Role r2) {
                return r1.getId().compareTo(r2.getId());
            }
        });
             
		return allZustaendigkeiten;
	}

	
	/**
	 * Ermittelt die Rollen zum Delegieren für einen Benutzer
	 * @param login Benutzer für den die Rollen ermittelt werden sollen
	 * @return Liste mit den Rollen
	 */
	public List<Role> getDelegiertAn(String login) {
		if (isUserAdmin(login)) return getAllDelegiertAn();
		User user = getUser(login);
		if (user==null) throw new RuntimeException();
		List <Role> delegiertAn = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+groupRoleAttribute+"="+groupExtern+")("+StringUtils.replace(groupSearchFilter, "{0}", user.getDn())+"))", roleContextMapper);
		return delegiertAn;
	}

	
	/**
	 * Ermittelt die Rollen zum Delegieren für den aktuellen Benutzer
	 * @return Liste mit den Rollen
	 */
	public List<Role> getCurrentDelegiertAn() {
		return getDelegiertAn(SecurityContextHolder.getContext().getAuthentication().getName());
	}

	
	/**
	 * Ermittelt alle im System vorhandenen Rollen zum Delegieren
	 * @return Liste mit allen Rollen zum Delegieren
	 */
	public List<Role> getAllDelegiertAn() {
		List <Role> allDelegiertAn = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+groupRoleAttribute+"="+groupExtern+"))", roleContextMapper);
        
        Collections.sort(allDelegiertAn, new Comparator<Role>() {
            public int compare(Role r1, Role r2) {
                return r1.getId().compareTo(r2.getId());
            }
        });
        
		return allDelegiertAn;
	}

	
	/**
	 * Ermittelt ob der aktuelle Benutzer ein externer Benutzer ist.
	 * @return <code>true</code> - aktueller Benutzer ist eine externe Benutzer
	 */
	public boolean isCurrentUserExtern() {
		return isUserExtern(SecurityContextHolder.getContext().getAuthentication().getName());
	}
	

	/**
	 * Ermittelt ob der Benutzer ein externer Benutzer ist.
	 * @param login Benutzer
	 * @return <code>true</code> - Benutzer ist eine externe Benutzer
	 */
	public boolean isUserExtern(String login) {
		if (isUserAdmin(login)) return true;
		User user = getUser(login);
		if (user==null) return false;
		List <Role> role = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+StringUtils.replace(groupSearchFilter, "{0}", user.getDn())+")("+groupRoleAttribute+"="+groupExtern+"))", roleContextMapper);
		return (role.size()==0) ? false : true;
	}
	
	
	/**
	 * Ermittelt ob der aktuelle Benutzer ein interner Benutzer ist.
	 * @return <code>true</code> - aktueller Benutzer ist eine interne Benutzer
	 */
	public boolean isCurrentUserIntern() {
		return isUserIntern(SecurityContextHolder.getContext().getAuthentication().getName());
	}


	/**
	 * Ermittelt ob der Benutzer ein interner Benutzer ist.
	 * @param login Benutzer
	 * @return <code>true</code> - Benutzer ist eine interne Benutzer
	 */
	public boolean isUserIntern(String login) {
		if (isUserAdmin(login)) return true;
		User user = getUser(login);
		if (user==null) return false;
		List <Role> role = securityServiceLdap.getObjectListFromLdap(groupSearchBase, "(&(objectclass="+groupObjectClass+")("+StringUtils.replace(groupSearchFilter, "{0}", user.getDn())+")("+groupRoleAttribute+"="+groupIntern+"))", roleContextMapper);
		return (role.size()==0) ? false : true;
	}

	
	/**
	 * Ermittelt ob der aktuelle Benutzer für den Vorgang zuständig ist.
	 * @param vorgangId Vorgang
	 * @return <code>true</code> - aktueller Benutzer ist für den Vorgang zuständig
	 */
	public boolean isCurrentZustaendigkeiten(Long vorgangId) {
		//Zuständigkeit für den Vorgang ermitteln
		String zustaendigkeit = vorgangDao.getZustaendigkeitForVorgang(vorgangId);
		if (StringUtils.isBlank(zustaendigkeit)) {
			return isCurrentUserAdmin();
		} else {
			for (Role role : getCurrentZustaendigkeiten(true))
				if (StringUtils.equals(role.getId(), zustaendigkeit)) return true;
		}
		return false;
	}


	/**
	 * Ermittelt ob der Vorgang an den aktuelle Benutzer delegiert wurde.
	 * @param vorgangId Vorgang
	 * @return <code>true</code> - Vorgang ist an den aktueller Benutzer delegiert
	 */
	public boolean isCurrentDelegiertAn(Long vorgangId) {
		//DelegiertAn für den Vorgang ermitteln
		String delegiertAn = vorgangDao.getDelegiertAnForVorgang(vorgangId);
		if (StringUtils.isBlank(delegiertAn)) {
			return isCurrentUserAdmin();
		} else {
			for (Role role : getCurrentDelegiertAn())
				if (StringUtils.equals(role.getId(), delegiertAn)) return true;
		}
		return false;
	}	
	

	/**
	 * Erzeugt aus einem String einen MD5-Hash. diese wird beispielsweise zum Erzeugen der URL für Bestätigungen verwendet.
	 * @param str String für den der Hash erstellt werden soll
	 * @return MD5-Hash
	 */
	public String createHash(String str) {
		try {
			 MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			 messageDigest.update(str.getBytes(), 0, str.length());
			 return new BigInteger(1, messageDigest.digest()).toString(32);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public String installCertificates(String pass) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(bos, true);
		try {
			//Zertifikate ermitteln
			List<File> certFiles = Arrays.asList(new DefaultResourceLoader().getResource("classpath:META-INF/certificates/").getFile().listFiles());
			
			SystemUtil.printlnSystemVariables();
			
			Collections.sort(certFiles, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			
			for(File certFile : certFiles) {
				pw.println("############# "+certFile.getName()+" #############\n");
				//Zertifikate installieren
				CommandLine cmdLine = new CommandLine(System.getProperty("java.home")+FS+"bin"+FS+"keytool");
				cmdLine.addArgument("-import");
				cmdLine.addArgument("-trustcacerts");
				cmdLine.addArgument("-alias");
				cmdLine.addArgument(StringUtils.substringAfterLast(StringUtils.substringBeforeLast(certFile.getName(), "."), "_"));
				cmdLine.addArgument("-file");
				cmdLine.addArgument(certFile.getAbsolutePath());
				cmdLine.addArgument("-keystore");
				cmdLine.addArgument(System.getProperty("java.home")+FS+"lib"+FS+"security"+File.separatorChar+"cacerts");
				if (StringUtils.isNotBlank(pass)) {
					cmdLine.addArgument("-storepass");
					cmdLine.addArgument(pass);
				}
				pw.println(cmdLine.toString()+"\n");
				DefaultExecutor executor = new DefaultExecutor();
				DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
				executor.setStreamHandler(new PumpStreamHandler(bos));
				executor.setWatchdog(new ExecuteWatchdog(60*1000));
				executor.execute(cmdLine, resultHandler);
				resultHandler.waitFor();
				pw.println("ExitValue: "+resultHandler.getExitValue()+"\n");
				if (resultHandler.getException()!=null)
					pw.println("Exception: "+resultHandler.getException().getMessage()+"\n");
			}
			return new String(bos.toByteArray());
		} catch (Exception e) {
			return new String(bos.toByteArray()) + "\n\nException: " + e.getMessage();
		}
	}

	/**
	 * Prüft, ob der aktuelle Nutzer einen Kommentar bearbeiten darf.
	 * @param kommentar Zu prüfender Kommentar
	 * @return Darf der Nutzer bearbeiten
	 */
	public boolean mayCurrentUserEditKommentar(Kommentar kommentar) {
		return this.getCurrentUser().getName().equals(kommentar.getNutzer());
	}
	
		
	/* --------------- GET + SET ----------------------------*/

	public String getUserAttributesMapping() {
		return userAttributesMapping;
	}

	public void setUserAttributesMapping(String userAttributesMapping) {
		this.userAttributesMapping = userAttributesMapping;
	}

	public String getRoleAttributesMapping() {
		return roleAttributesMapping;
	}

	public void setRoleAttributesMapping(String roleAttributesMapping) {
		this.roleAttributesMapping = roleAttributesMapping;
	}

	public String getUserSearchBase() {
		return userSearchBase;
	}

	public void setUserSearchBase(String userSearchBase) {
		this.userSearchBase = userSearchBase;
	}

	public String getUserObjectClass() {
		return userObjectClass;
	}

	public void setUserObjectClass(String userObjectClass) {
		this.userObjectClass = userObjectClass;
	}

	public String getUserSearchFilter() {
		return userSearchFilter;
	}

	public void setUserSearchFilter(String userSearchFilter) {
		this.userSearchFilter = userSearchFilter;
	}

	public String getGroupSearchBase() {
		return groupSearchBase;
	}

	public void setGroupSearchBase(String groupSearchBase) {
		this.groupSearchBase = groupSearchBase;
	}

	public String getGroupObjectClass() {
		return groupObjectClass;
	}

	public void setGroupObjectClass(String groupObjectClass) {
		this.groupObjectClass = groupObjectClass;
	}

	public String getGroupSearchFilter() {
		return groupSearchFilter;
	}

	public void setGroupSearchFilter(String groupSearchFilter) {
		this.groupSearchFilter = groupSearchFilter;
	}

	public String getGroupRoleAttribute() {
		return groupRoleAttribute;
	}

	public void setGroupRoleAttribute(String groupRoleAttribute) {
		this.groupRoleAttribute = groupRoleAttribute;
	}

	public String getGroupIntern() {
		return groupIntern;
	}

	public void setGroupIntern(String groupIntern) {
		this.groupIntern = groupIntern;
	}

	public String getGroupExtern() {
		return groupExtern;
	}

	public void setGroupExtern(String groupExtern) {
		this.groupExtern = groupExtern;
	}


	public String getGroupObjectId() {
		return groupObjectId;
	}


	public void setGroupObjectId(String groupObjectId) {
		this.groupObjectId = groupObjectId;
	}


	public String getGroupAdmin() {
		return groupAdmin;
	}


	public void setGroupAdmin(String groupAdmin) {
		this.groupAdmin = groupAdmin;
	}


	public SecurityServiceLdap getSecurityServiceLdap() {
		return securityServiceLdap;
	}


	public void setSecurityServiceLdap(SecurityServiceLdap securityServiceLdap) {
		this.securityServiceLdap = securityServiceLdap;
	}

	public String getRoot() {
		return root;
	}


	public void setRoot(String root) {
		this.root = root;
	}
}
