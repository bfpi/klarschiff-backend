package de.fraunhofer.igd.klarschiff.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class Log4jNdcFilter extends Filter {

	@Override
	public int decide(LoggingEvent event) {
		if (StringUtils.equals("log_DENY", event.getNDC())) return DENY;
		else if (StringUtils.equals("log_ACCEPT", event.getNDC())) return ACCEPT;
		else return NEUTRAL;
	}
}
