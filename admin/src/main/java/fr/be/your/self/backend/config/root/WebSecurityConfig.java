package fr.be.your.self.backend.config.root;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.header.writers.frameoptions.AllowFromStrategy;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter.XFrameOptionsMode;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.common.UserType;
import fr.be.your.self.security.oauth2.DefaultAccessDeniedHandler;
import fr.be.your.self.security.oauth2.DefaultAuthenticationEntryPoint;
import fr.be.your.self.security.oauth2.DefaultLoginFailureHandler;
import fr.be.your.self.security.oauth2.DefaultLoginSuccessHandler;
import fr.be.your.self.security.oauth2.DefaultLogoutSuccessHandler;
import fr.be.your.self.security.oauth2.DefaultUserDetailsService;
import fr.be.your.self.util.StringUtils;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	private static final String AUTHORIZE_URL = Constants.PATH.AUTHENTICATION_PREFIX + Constants.PATH.AUTHENTICATION.AUTHORIZE;
	
	private static final String LOGIN_URL = Constants.PATH.AUTHENTICATION_PREFIX + Constants.PATH.AUTHENTICATION.LOGIN;
	private static final String LOGOUT_URL = Constants.PATH.AUTHENTICATION_PREFIX + Constants.PATH.AUTHENTICATION.LOGOUT;
	
	private static final String REGISTER_URL = Constants.PATH.AUTHENTICATION_PREFIX + Constants.PATH.AUTHENTICATION.REGISTER;
	private static final String ACTIVATE_URL = Constants.PATH.AUTHENTICATION_PREFIX + Constants.PATH.AUTHENTICATION.ACTIVATE;
	private static final String SEND_ACTIVATE_CODE_URL = Constants.PATH.AUTHENTICATION_PREFIX + Constants.PATH.AUTHENTICATION.SEND_ACTIVATE_CODE;
	
	private static final String FORGOT_PASSWORD_URL = Constants.PATH.AUTHENTICATION_PREFIX + Constants.PATH.AUTHENTICATION.FORGOT_PASSWORD;
	private static final String ACTIVATE_RESET_PASSWORD_URL = Constants.PATH.AUTHENTICATION_PREFIX + Constants.PATH.AUTHENTICATION.ACTIVATE_RESET_PASSWORD;
	
	private static final String ACCESS_DENIED_URL = Constants.PATH.ACCESS_DENIED;
	
	private static final String API_URL_PREFIX = Constants.PATH.API_PREFIX + "/";
	
	@Value("${url.webview}")
    private String defaultRedirectUrl;
	
	@Value("${cors.allowed.origins}")
	private String allowedOrigins;
    
	@Value("${jwt.secret}")
	private String secret;
	
	@Value("${auth.remember.me.key}")
	private String rememberMeKey;
	
	@Value("${auth.remember.me.validity}")
	private int rememberMeValidity;
	
	@Bean
    public AllowFromStrategy allowFromStrategy() {
    	List<String> listAllowedOrgins = Arrays.asList(this.allowedOrigins.split(","));
		return new AllowFromStrategyImpl(listAllowedOrgins);
	}

	@Bean
    public PasswordEncoder passwordEncoder() {
    	return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
	
	@Bean
	@Override
	public UserDetailsService userDetailsService() {
		return new DefaultUserDetailsService();
	}
	
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
 		return super.authenticationManagerBean();
	}
	
	/********************* URL Configuration ********************/
	@Bean
	public AuthenticationProvider authenticationProvider() {
	    DaoAuthenticationProvider impl = new DaoAuthenticationProvider();
	    impl.setUserDetailsService(userDetailsService());
	    impl.setHideUserNotFoundExceptions(false);
	    impl.setPasswordEncoder(this.passwordEncoder());
	    
	    return impl ;
	}
	
	@Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
    	return new DefaultLoginSuccessHandler();
    }
	
	@Bean
	public LogoutSuccessHandler logoutSuccessHandler() {
		return new DefaultLogoutSuccessHandler();
	}
	
	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return new DefaultAccessDeniedHandler(ACCESS_DENIED_URL);
	}
	
	@Bean
	public AuthenticationEntryPoint authenticationEntryPoint() throws Exception {
		return new DefaultAuthenticationEntryPoint(LOGIN_URL, API_URL_PREFIX);
	}
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(this.authenticationProvider());
	}
	
	/********************* Security Configuration **************/
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
        		"/images/**", 
        		"/css/**",
        		"/js/**",
        		"/fonts/**",
        		"/assets/**",
        		"/error",
        		"/api/image/**",
        		"/api/notoken/**",
        		"/api/service/**",
        		"/websocket/**",
        		"/ws/**"
    		);
    }
    
	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		// @formatter:off
		httpSecurity
			.authorizeRequests()
				.antMatchers(LOGIN_URL, ACTIVATE_URL, REGISTER_URL, SEND_ACTIVATE_CODE_URL, 
						FORGOT_PASSWORD_URL, ACTIVATE_RESET_PASSWORD_URL, ACCESS_DENIED_URL).permitAll()
				.antMatchers("/", "/home", "/about", "/error").permitAll()
			.and()
        		.authorizeRequests()
        			.anyRequest().hasRole(UserType.USER.getValue())
        	.and()
        		.exceptionHandling()
            		.accessDeniedHandler(this.accessDeniedHandler())
            		.authenticationEntryPoint(this.authenticationEntryPoint())
    		// XXX: put CSRF protection back into this endpoint
            .and()
	        	.csrf()
	            	.requireCsrfProtectionMatcher(new AntPathRequestMatcher(AUTHORIZE_URL)).disable()
	        .logout()
	            .logoutSuccessUrl(LOGIN_URL)
	            .logoutUrl(LOGOUT_URL)
	            .deleteCookies("JSESSIONID", CookieLocaleResolver.DEFAULT_COOKIE_NAME)
	            .logoutSuccessHandler(this.logoutSuccessHandler())
	            .permitAll()
	        .and()
		        .formLogin()
					.loginPage(LOGIN_URL)
					.defaultSuccessUrl(this.defaultRedirectUrl)
					.failureHandler(new DefaultLoginFailureHandler(LOGIN_URL)) // + "?authentication_error=true"))
					.permitAll()
			.and()
				.rememberMe()
					.key(this.rememberMeKey)
					.tokenValiditySeconds(this.rememberMeValidity)
					.rememberMeParameter("rememberMe")
			.and()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
			; 
		// @formatter:on
		
		// If no allowed origin, mean only allow from same domain
        if (!StringUtils.isNullOrSpace(allowedOrigins)) {
        	httpSecurity.headers().addHeaderWriter(new XFrameOptionsHeaderWriter(allowFromStrategy()));
        } else {
        	httpSecurity.headers().addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsMode.SAMEORIGIN));
        }
        
		//httpSecurity.addFilterBefore(this.jwtRequestFilter(), UsernamePasswordAuthenticationFilter.class);
	}
    
    private class AllowFromStrategyImpl implements AllowFromStrategy {
    	
    	private final Logger logger = LoggerFactory.getLogger(this.getClass());

		private Collection<String> allowedOrigins = Collections.emptyList();

		public AllowFromStrategyImpl(Collection<String> alloweds) {
			super();
			
			if (alloweds != null && !alloweds.isEmpty()) {
				this.allowedOrigins = new ArrayList<String>(alloweds.size());
				for (String origin : alloweds) {
					this.allowedOrigins.add(getOrigin(origin));
				}
			}
		}

		protected boolean isAllowed(String origin) {
			if (StringUtils.isNullOrSpace(origin)) {
				return false;
			}
			
			return allowedOrigins.contains(origin) || allowedOrigins.contains("*");
		}

		@Override
		public String getAllowFromValue(HttpServletRequest request) {
			String origin = request.getHeader("Origin");
			if (isAllowed(origin)) {
				return origin;
			}
			return null;
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
