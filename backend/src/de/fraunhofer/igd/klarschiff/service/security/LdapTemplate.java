package de.fraunhofer.igd.klarschiff.service.security;

import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.PartialResultException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.springframework.ldap.core.ContextMapperCallbackHandler;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.core.NameClassPairCallbackHandler;
import org.springframework.ldap.core.SearchExecutor;
import org.springframework.ldap.support.LdapUtils;

public class LdapTemplate extends org.springframework.ldap.core.LdapTemplate {

	private static final Logger logger = Logger.getLogger(LdapTemplate.class);

	private boolean ignorePartialResultException = false;
	private boolean ignoreNameNotFoundException = false;
	
	public LdapTemplate() {
		super();
	}

	/**
	 * Constructor to setup instance directly.
	 * 
	 * @param contextSource the ContextSource to use.
	 */
	public LdapTemplate(ContextSource contextSource) {
		super(contextSource);
	}

	public void setIgnorePartialResultException(boolean ignore) {
		super.setIgnorePartialResultException(ignore);
		this.ignorePartialResultException = ignore;
	}

	public void setIgnoreNameNotFoundException(boolean ignore) {
		super.setIgnoreNameNotFoundException(ignore);
		this.ignoreNameNotFoundException = ignore;
	}
	

	public void search(final String base, final String filter, final SearchControls controls,
			NameClassPairCallbackHandler handler, DirContextProcessor processor) {

		// Create a SearchExecutor to perform the search.
		SearchExecutor se = new LdapSearchExecutor(base, filter, controls);
		if (handler instanceof ContextMapperCallbackHandler) {
			assureReturnObjFlagSet(controls);
		}
		search(se, handler, processor);
	}

	
	public void search(SearchExecutor se, NameClassPairCallbackHandler handler, DirContextProcessor processor) {
		logger.debug("\n########################################################################## LdapTemplate.search() ###############################################################################################");
		DirContext ctx = null;

		NamingEnumeration results = null;
		RuntimeException ex = null;
		try {

			int tryCount = 0;
			int maxTryCount = 10;
			boolean succes = false;
			
			while(succes==false && tryCount<maxTryCount) {
				try {
					tryCount++;
					ctx = getContextSource().getReadOnlyContext();
					try {
						logger.debug(logHelper("se", se));
						logger.debug(logHelper("handler", handler));
						logger.debug(logHelper("processor", processor));
						logger.debug(logHelper("ctx", ctx));
					} catch (Exception e) {}
					
					processor.preProcess(ctx);
					results = se.executeSearch(ctx);
					succes = true;
				} catch (Exception e) {
					if (tryCount==maxTryCount) {
						logger.error("fehlerhafter LdapTemplate.search[try:"+tryCount+"](se:"+se+" handler:"+handler+" processor:"+processor, e);
						e.printStackTrace();
						throw e;
					} else {
						logger.debug("fehlerhafter LdapTemplate.search[try:"+tryCount+"](se:"+se+" handler:"+handler+" processor:"+processor, e);
						closeContextAndNamingEnumeration(ctx, results);
					}
				}
			}			
			
			while (results.hasMore()) {
				NameClassPair result = (NameClassPair) results.next();
				handler.handleNameClassPair(result);
			}
		}
		catch (NameNotFoundException e) {
			// It is possible to ignore errors caused by base not found
			if (ignoreNameNotFoundException) {
				logger.warn("Base context not found, ignoring: " + e.getMessage());
			}
			else {
				ex = LdapUtils.convertLdapException(e);
			}
		}
		catch (PartialResultException e) {
			// Workaround for AD servers not handling referrals correctly.
			if (ignorePartialResultException) {
				logger.debug("PartialResultException encountered and ignored", e);
			}
			else {
				ex = LdapUtils.convertLdapException(e);
			}
		}
		catch (javax.naming.NamingException e) {
			ex = LdapUtils.convertLdapException(e);
		}
		catch (Exception e) {
			if (ex instanceof RuntimeException) ex = (RuntimeException)e;
			else ex = new RuntimeException(e);
		}
		finally {
			try {
				processor.postProcess(ctx);
			}
			catch (javax.naming.NamingException e) {
				if (ex == null) {
					ex = LdapUtils.convertLdapException(e);
				}
				else {
					// We already had an exception from above and should ignore
					// this one.
					logger.debug("Ignoring Exception from postProcess, " + "main exception thrown instead", e);
				}
			}
			closeContextAndNamingEnumeration(ctx, results);
			// If we got an exception it should be thrown.
			if (ex != null) {
				throw ex;
			}
		}
	}
	
	private static String logHelper(String name, Object o) {
		return ("LdapTemplate.search\n  "+name+"="+ToStringBuilder.reflectionToString(o, ToStringStyle.MULTI_LINE_STYLE).replaceAll("\n", "\n    "));
	}

	
	/**
	 * Make sure the returnObjFlag is set in the supplied SearchControls. Set it
	 * and log if it's not set.
	 * 
	 * @param controls the SearchControls to check.
	 */
	private void assureReturnObjFlagSet(SearchControls controls) {
		Validate.notNull(controls);
		if (!controls.getReturningObjFlag()) {
			logger.info("The returnObjFlag of supplied SearchControls is not set but a ContextMapper is used - setting flag to true");
			controls.setReturningObjFlag(true);
		}
	}

	
	private void closeContextAndNamingEnumeration(DirContext ctx, NamingEnumeration results) {

		closeNamingEnumeration(results);
		closeContext(ctx);
	}

	/**
	 * Close the supplied DirContext if it is not null. Swallow any exceptions,
	 * as this is only for cleanup.
	 * 
	 * @param ctx the context to close.
	 */
	private void closeContext(DirContext ctx) {
		if (ctx != null) {
			try {
				ctx.close();
			}
			catch (Exception e) {
				// Never mind this.
			}
		}
	}

	/**
	 * Close the supplied NamingEnumeration if it is not null. Swallow any
	 * exceptions, as this is only for cleanup.
	 * 
	 * @param results the NamingEnumeration to close.
	 */
	private void closeNamingEnumeration(NamingEnumeration results) {
		if (results != null) {
			try {
				results.close();
			}
			catch (Exception e) {
				// Never mind this.
			}
		}
	}
}

class LdapSearchExecutor implements SearchExecutor {
	public String base;
	public String filter;
	public SearchControls controls;
	
	public LdapSearchExecutor(String base, String filter, SearchControls controls) {
		this.base = base;
		this.filter = filter;
		this.controls = controls;
	}
	
	public NamingEnumeration executeSearch(DirContext ctx) throws javax.naming.NamingException {
		return ctx.search(base, filter, controls);
	}
}
