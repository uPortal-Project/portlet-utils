package org.jasig.portlet.utils.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * No-nonsense implementation of {@link CrossContextRestApiInvoker} without 
 * extra config options or hooks they may be desired at some point (though I 
 * can't guess them now).
 * 
 * @author awills
 */
public class SimpleCrossContextRestApiInvoker implements CrossContextRestApiInvoker {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public RestResponse invoke(HttpServletRequest req, HttpServletResponse res, String uri) {
		final Map<String, String[]> params = Collections.emptyMap();
		return invoke(req, res, uri, params);
	}

	@Override
	public RestResponse invoke(HttpServletRequest req, HttpServletResponse res, String uri, Map<String, String[]> params) {
		
		// Assertions.
		if (req == null) {
			final String msg = "Argument 'req' cannot be null";
			throw new IllegalArgumentException(msg);
		}
		if (res == null) {
			final String msg = "Argument 'res' cannot be null";
			throw new IllegalArgumentException(msg);
		}
		if (uri == null) {
			final String msg = "Argument 'uri' cannot be null";
			throw new IllegalArgumentException(msg);
		}
		if (!uri.startsWith("/") || uri.indexOf("/", 1) == -1) {
			final String msg = "Argument 'uri' must begin with a '/' character, " +
					"followed by the contextName, followed by another '/' then " +
					"the URI within the specified context";
			throw new IllegalArgumentException(msg);
		}
		if (params == null) {
			final String msg = "Argument 'params' cannot be null";
			throw new IllegalArgumentException(msg);
		}
		
		log.debug("Invoking REST API at URI (before applying parameters):  {}", uri);

		try {
			final UriTuple uriTuple = parseUriTuple(uri, params);
			log.debug("Invoking REST API where contextName={} and URI={}", 
						uriTuple.getContextName(), uriTuple.getUri());
			return doInvoke(req, res, uriTuple);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	
	}
	
	/*
	 * Implementation
	 */
	
	private UriTuple parseUriTuple(String uri, Map<String, String[]> params) throws UnsupportedEncodingException {

		String requestUri = null;
		String queryString = "";  // default
		
		final int queryStringBegin = uri.indexOf("?");
		if (queryStringBegin == -1) {
			// Simple case -- no querystring
			requestUri = uri;
		} else {
			// Complex case -- uri+querystring
			requestUri = uri.substring(0, queryStringBegin);
			queryString = uri.substring(queryStringBegin);  // will already contain the '?' character
		}
		
		// Inject requestUri params
		for (Map.Entry<String, String[]> y : params.entrySet()) {
			final String token = "{" + y.getKey() + "}";
			final String[] values = y.getValue();
			switch (values.length) {
				case 0:
					// Strange -- I guess we omit it?
					while(requestUri.contains(token)) {
						// Don't use String.replaceAll b/c token looks like an illegal regex
						requestUri = requestUri.replace(token, "");
					}
					break;
				case 1:
					// This is healthy & normal
					final String inject = URLEncoder.encode(values[0], "UTF-8");
					while(requestUri.contains(token)) {
						// Don't use String.replaceAll b/c token looks like an illegal regex
						requestUri = requestUri.replace(token, inject);
					}
					break;
				default:
					// OOPS! --  can't have more than 1 value for a requestUri token
					final String msg = "Can't support multiple values for non-querystring URI token:  " + token;
					throw new IllegalArgumentException(msg);
			}
		}
		
		// Inject queryString params
		for (Map.Entry<String, String[]> y : params.entrySet()) {
			final String paramName = y.getKey();
			final String token = "\\{" + paramName + "\\}";
			if (queryString.contains(token)) {
				final StringBuilder value = new StringBuilder();
				for (String s : y.getValue()) {
					final String inject = URLEncoder.encode(s, "UTF-8");
					value.append(value.length() != 0 ? "&" : "");
					value.append(paramName).append("=").append(inject);
				}
				queryString = queryString.replace(token, value);
			}
		}
		
		// Split into contextName+uri
		final int contextSeparatorPos = requestUri.indexOf("/", 1);  // A valid input starts with a slash, followed by the contextName
		final String contextName = requestUri.substring(0, contextSeparatorPos);  // Includes leading '/'
		requestUri = requestUri.substring(contextSeparatorPos);  // Includes leading '/'

		return new UriTuple(contextName, requestUri + queryString);
		
	}
	
	private RestResponse doInvoke(HttpServletRequest req, HttpServletResponse res, UriTuple tuple) {

		try {
			ServletContext ctx = req.getSession().getServletContext()
								.getContext(tuple.getContextName());
			RequestDispatcher rd = ctx.getRequestDispatcher(tuple.getUri());
			HttpServletResponseWrapperImpl responseWrapper = new HttpServletResponseWrapperImpl(res);
			rd.include(req, responseWrapper);
			RestResponse rslt = new RestResponse(
					responseWrapper.getOutputAsString(), 
					responseWrapper.getContentType());
			return rslt;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/*
	 * Nested Types
	 */
	
	private static final class HttpServletResponseWrapperImpl extends HttpServletResponseWrapper {
		
		private StringWriter writer = null;
		private ServletOutputStreamImpl outputStream = null;
		
		public HttpServletResponseWrapperImpl(HttpServletResponse res) {
			super(res);
		}
		
		@Override
		public ServletOutputStream getOutputStream() {
			if (writer != null) {
				final String msg = "The method HttpServletResponse.getWriter has " +
						"already been called;  call either getOutputStream or " +
						"getWriter, but not both";
				throw new IllegalStateException(msg);
			}
			outputStream = new ServletOutputStreamImpl();
			return outputStream;
		}
		
		@Override
		public PrintWriter getWriter() {
			if (outputStream != null) {
				final String msg = "The method HttpServletResponse.getOutputStream has " +
						"already been called;  call either getOutputStream or " +
						"getWriter, but not both";
				throw new IllegalStateException(msg);
			}
			writer = new StringWriter();
			return new PrintWriter(writer);
		}
		
		public String getOutputAsString() {
			if (writer == null && outputStream == null) {
				final String msg = "Neither HttpServletResponse.getWriter nor " +
						"HttpServletResponse.getOutputStream has not been called";
				throw new IllegalStateException(msg);
			}
			return writer != null
					? writer.toString()
					: outputStream.toString();
		}
		
	}
	
	private static final class ServletOutputStreamImpl extends ServletOutputStream {
		
		private final ByteArrayOutputStream enclosed = new ByteArrayOutputStream();

		@Override
		public void write(int b) throws IOException {
			enclosed.write(b);
		}
		
		@Override
		public String toString() {
			return enclosed.toString();
		}
		
	}
	
	private static final class UriTuple {
		private final String contextName;
		private final String uri;

		public UriTuple(String contextName, String uri) {
			this.contextName = contextName;
			this.uri = uri;
		}
		
		public String getContextName() {
			return contextName;
		}

		public String getUri() {
			return uri;
		}

	}

}
