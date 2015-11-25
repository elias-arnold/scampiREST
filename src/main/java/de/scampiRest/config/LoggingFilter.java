package de.scampiRest.config;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException{

		try {
			HttpServletRequest request = (HttpServletRequest) req;
			String traceMessage = new Date() + ";" + request.getSession().getId() + ";" + request.getRequestURI();
			logger.info(traceMessage);
		} catch (Exception e) {
			logger.error("Error logging trace", e);
		}
		
		chain.doFilter(req, res);
	}

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

}
