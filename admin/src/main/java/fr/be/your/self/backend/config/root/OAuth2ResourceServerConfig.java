package fr.be.your.self.backend.config.root;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

import fr.be.your.self.security.oauth2.InMemoryCsrfTokenRepository;

@Configuration
@EnableResourceServer
public class OAuth2ResourceServerConfig extends ResourceServerConfigurerAdapter {
	
	@Value("${oauth2.realm}")
	private String realm;
	
	@Value("${oauth2.signed.request.verifier.key}")
	private String signedRequestVerifierKey;
	
	@Autowired
	private DefaultTokenServices tokenServices;
	
	private CsrfTokenRepository csrfTokenRepository;
	private RequestMatcher csrfProtectionMatcher;
	private RequestMatcher csrfRenewTokenMatcher;
	
	/*
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }
 
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("123");
        return converter;
    }
 
    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        return defaultTokenServices;
    }
    */
	
	public OAuth2ResourceServerConfig() {
		this.csrfTokenRepository = new InMemoryCsrfTokenRepository();
		
		this.csrfProtectionMatcher = new RequestMatcher() {
			@Override
			public boolean matches(HttpServletRequest request) {
				return HttpMethod.POST.matches(request.getMethod())
						|| HttpMethod.PUT.matches(request.getMethod())
						|| HttpMethod.DELETE.matches(request.getMethod());
			}
		}; 
		
		this.csrfRenewTokenMatcher = this.csrfProtectionMatcher;
	}
	
	@Override
	public void configure(ResourceServerSecurityConfigurer resources) {
		// @formatter:off
		resources
			.resourceId(this.realm)
			.tokenServices(this.tokenServices);
		// @formatter:on
	}
	
	@Override
	public void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.requestMatchers().antMatchers(
					"/oauth/users/**", 
					// "/oauth/clients/**",
					"/oauth/userinfo", 
					"/api/**",
					"/report/**",
					"/websocket/**")
			.and()
				.authorizeRequests()
					// Revoke token endpoint
					.regexMatchers(HttpMethod.DELETE, "/oauth/users/([^/].*?)/tokens/.*")
						.access("#oauth2.isUser() and hasRole('ROLE_USR')")
						
					// Get user's info endpoint
					.regexMatchers(HttpMethod.GET, "/oauth/userinfo")
						.access("#oauth2.isUser() and hasRole('ROLE_USR')")
						
					// List token for a user
					//.regexMatchers(HttpMethod.GET, "/oauth/clients/([^/].*?)/users/.*")
					//	.access("#oauth2.clientHasRole('ROLE_CLIENT') and (hasRole('ROLE_USER') or #oauth2.isClient()) and #oauth2.hasScope('read')")
					
					// List token for a client
					//.regexMatchers(HttpMethod.GET, "/oauth/clients/.*")
					//	.access("#oauth2.clientHasRole('ROLE_CLIENT') and #oauth2.isClient() and #oauth2.hasScope('read')")
					
					// Endpoints for role admin
					.regexMatchers(HttpMethod.GET, "/api/admin/.*")
						.access("#oauth2.isUser() and hasRole('ROLE_ADM')")
					
					.regexMatchers(HttpMethod.GET, "/api/test/.*")
						.access("#oauth2.isUser() and hasRole('ROLE_ADM')")
					
					// Endpoints for all user
					.regexMatchers(HttpMethod.GET, "/api/service/.*")
						.access("#oauth2.isUser() and hasRole('ROLE_USR')")

					// All other endpoint
					.regexMatchers("/.*")
						.access("hasRole('ROLE_USR')")
			.and()
				.addFilterAfter(this.csrfFilter(), FilterSecurityInterceptor.class)
				.addFilterAfter(new CsrfGrantingFilter(), CsrfFilter.class)
		;
		// @formatter:on
	}
	
	private Filter csrfFilter() {
		final CsrfFilter csrfFilter = new CsrfFilter(this.csrfTokenRepository);
		csrfFilter.setRequireCsrfProtectionMatcher(this.csrfProtectionMatcher);
		
		return csrfFilter;
	}
	
	class CsrfGrantingFilter implements Filter {
		@Override
		public void init(FilterConfig filterConfig) throws ServletException {}

		@Override
		public void doFilter(ServletRequest servletRequest, 
				ServletResponse servletResponse, FilterChain filterChain)
				throws IOException, ServletException {
			
			CsrfToken csrf = (CsrfToken) servletRequest.getAttribute(CsrfToken.class.getName());
			
			final HttpServletRequest request = (HttpServletRequest) servletRequest;
			final HttpServletResponse response = (HttpServletResponse) servletResponse;
			if (csrfRenewTokenMatcher == null || csrfRenewTokenMatcher.matches(request)) {
				csrf = csrfTokenRepository.generateToken(request);
				csrfTokenRepository.saveToken(csrf, request, response);
			}
			
			if (csrf != null) {
				final String token = csrf.getToken();
				if (token != null) {
					response.setHeader("XSRF-TOKEN", token);
				}
			}
			
			filterChain.doFilter(servletRequest, servletResponse);
		}

		@Override
		public void destroy() {}
	}
}
