package fr.be.your.self.backend.config.root;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import fr.be.your.self.util.StringUtils;

@Configuration
public class CORSConfig {
	
	@Value("${cors.allowed.origins}")
	private String allowedOrigins;
	
	@Bean
	public Filter corsFilter() {
		List<String> staticAllowedOrgins = null;
		
		if (!StringUtils.isNullOrSpace(this.allowedOrigins)) {
			staticAllowedOrgins = Arrays.asList(this.allowedOrigins.split(","));
		}
		
		return new SimpleCORSFilter(staticAllowedOrgins);
	}
	
	public static class SimpleCORSFilter implements Filter {
		
		private final Logger logger = LoggerFactory.getLogger(this.getClass());
		
		private List<String> allowedOrigins = Collections.emptyList();
		
		/** Default does not allow CORS for any origin */
		private boolean sameDomainOnly = true;
		
		public SimpleCORSFilter(List<String> staticAllowedOrgins) {
			if (!CollectionUtils.isEmpty(staticAllowedOrgins)) {
				this.sameDomainOnly = false;
				this.allowedOrigins = new ArrayList<String>(staticAllowedOrgins.size());
				
				for (String origin : staticAllowedOrgins) {
					this.allowedOrigins.add(getOrigin(origin));
				}
			}
		}

		@Override
		public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
			// If does not allow CORS for any origin, do not add any CORS header
			if (this.sameDomainOnly) {
				chain.doFilter(req, res);
				return;
			}
			
			// Add CORS headers for allowed origins
			final HttpServletResponse httpResponse = (HttpServletResponse) res;
			final HttpServletRequest httpRequest = ((HttpServletRequest) req);
			
			final String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
			if (this.isPathAllowedForCors(path)) {
				final String origin = httpRequest.getHeader("Origin");
				if (this.isOriginAllowed(origin)) {
					httpResponse.setHeader("Access-Control-Allow-Origin", origin);
				}
				
				httpResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
				httpResponse.setHeader("Access-Control-Max-Age", "3600");
				httpResponse.setHeader("Access-Control-Allow-Headers", "x-requested-with, accept, authorization, content-type, x-csrf-token, range");
				httpResponse.setHeader("Access-Control-Expose-Headers", "content-type, x-file-name, content-disposition, content-encoding, content-length, xsrf-token, accept-ranges, content-range");
				httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
			}
			
			if (httpRequest.getMethod().equals("OPTIONS")) {
	            try {
	                httpResponse.getWriter().print("OK");
	                httpResponse.getWriter().flush();
	            } catch (IOException e) {
	            	;
	            }
	        } else{
	    		chain.doFilter(req, res);
	        }
		}

		@Override
		public void init(FilterConfig filterConfig) throws ServletException {}

		@Override
		public void destroy() {}
		
		private boolean isOriginAllowed(String origin) {
			if (StringUtils.isNullOrSpace(origin)) {
				return false;
			}
			
			for (String allowedOrigin : this.allowedOrigins) {
				if ("*".equals(allowedOrigin) || allowedOrigin.equalsIgnoreCase(origin)) {
					return true;
				}
			}
			
			return false;
		}
		
		private boolean isPathAllowedForCors(String path) {
			if (path.startsWith("/account")) {
				// Allow cross-domain ping
				if (path.startsWith("/account/ping")) {
					return true;
				}
				
				return false;
			}
			
			return true;
		}

		private String getOrigin(String url) {
			if (!url.startsWith("http:") && !url.startsWith("https:")) {
				return url;
			}

			String origin = null;

			try {
				URI uri = new URI(url);
				origin = uri.getScheme() + "://" + uri.getAuthority();
			} catch (URISyntaxException e) {
				logger.error("Cannot parse URI from " + url, e);
			}

			return origin;
		}
	}
}
