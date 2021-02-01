package fr.be.your.self.backend;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import fr.be.your.self.backend.config.rest.RestConfig;
import fr.be.your.self.backend.config.web.WebMvcConfig;
import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.common.BusinessCodeType;
import fr.be.your.self.common.UserPermission;
import fr.be.your.self.common.UserStatus;
import fr.be.your.self.common.UserType;
import fr.be.your.self.model.Functionality;
import fr.be.your.self.model.Permission;
import fr.be.your.self.model.Slideshow;
import fr.be.your.self.model.SplashScreen;
import fr.be.your.self.model.User;
import fr.be.your.self.model.UserConstants;
import fr.be.your.self.repository.FunctionalityRepository;
import fr.be.your.self.repository.PermissionRepository;
import fr.be.your.self.repository.SlideshowRepository;
import fr.be.your.self.repository.SplashScreenRepository;
import fr.be.your.self.repository.UserRepository;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@ComponentScan(basePackages = { "fr.be.your.self.backend.config.root" })
@EntityScan(basePackages = { "fr.be.your.self.model" })
@EnableJpaRepositories(basePackages = { "fr.be.your.self.repository" })
public class AdminApplication implements CommandLineRunner {
	
	private static final Logger logger = LoggerFactory.getLogger(AdminApplication.class);
	
	private static final String WEB_URL_PATTERN = "/";
	private static final String API_URL_PATTERN = Constants.PATH.API_PREFIX + "/*";
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private FunctionalityRepository functionalityRepository;
	
	@Autowired
	private PermissionRepository permissionRepository;
	
	@Autowired
	private SlideshowRepository slideshowRepository;
	
	@Autowired
	private SplashScreenRepository splashScreenRepository;
	
	public static void main(String[] args) {
		System.getProperties().setProperty("java.net.preferIPv4Stack", "true");
		SpringApplication.run(AdminApplication.class, args);
	}
	
	@Bean
	public ServletRegistrationBean<DispatcherServlet> apiServletRegistration() {
		AnnotationConfigWebApplicationContext apiServletContext = new AnnotationConfigWebApplicationContext();
        apiServletContext.register(RestConfig.class);
        apiServletContext.scan("fr.be.your.self.backend.config.rest");
        
        DispatcherServlet apiDispatcherServlet = new DispatcherServlet(apiServletContext);
        
		ServletRegistrationBean<DispatcherServlet> apiServletRegistration = new ServletRegistrationBean<>();
		apiServletRegistration.setName("apiDispatcherServlet");
		apiServletRegistration.setServlet(apiDispatcherServlet);
		apiServletRegistration.addUrlMappings(API_URL_PATTERN);
        apiServletRegistration.setLoadOnStartup(1);
        apiServletRegistration.setAsyncSupported(true);

        return apiServletRegistration;
	}
	
	@Bean
	public ServletRegistrationBean<DispatcherServlet> webServletRegistration() {
		AnnotationConfigWebApplicationContext webServletContext = new AnnotationConfigWebApplicationContext();
		webServletContext.register(WebMvcConfig.class);
		webServletContext.scan("fr.be.your.self.backend.config.web");
        
        DispatcherServlet webDispatcherServlet = new DispatcherServlet(webServletContext);
        
		ServletRegistrationBean<DispatcherServlet> webServletRegistration = new ServletRegistrationBean<>();
		webServletRegistration.setName("webDispatcherServlet");
		webServletRegistration.setServlet(webDispatcherServlet);
		webServletRegistration.addUrlMappings(WEB_URL_PATTERN);
        webServletRegistration.setLoadOnStartup(2);
        webServletRegistration.setAsyncSupported(true);
        
        return webServletRegistration;
	}
	
	@Override
	public void run(String... args) throws Exception {
		final String adminEmail = "admin@gmail.com";
		
		try {
			final Optional<Slideshow> currentSlideshow = slideshowRepository.findFirstByStartDateIsNull();
			if (!currentSlideshow.isPresent()) {
				final Slideshow defaultSlideshow = new Slideshow();
				slideshowRepository.save(defaultSlideshow);
			}
			
			final Iterable<SplashScreen> defaultSplashScreens = splashScreenRepository.findAll();
			if (defaultSplashScreens == null || !defaultSplashScreens.iterator().hasNext()) {
				final SplashScreen defaultSplashScreen = new SplashScreen();
				defaultSplashScreen.setId(1);
				
				splashScreenRepository.save(defaultSplashScreen);
			}
			
			User adminUser = userRepository.findByEmail(adminEmail);
			
			if (adminUser == null) {
				final String password = this.passwordEncoder.encode("123456");
				
				adminUser = new User(adminEmail, password, UserType.ADMIN.getValue(), "Administrator", "");
				adminUser.setStatus(UserStatus.ACTIVE.getValue());
				
				adminUser = userRepository.save(adminUser);
			} else if (!UserType.ADMIN.getValue().equalsIgnoreCase(adminUser.getUserType())) {
				adminUser.setUserType(UserType.ADMIN.getValue());
				
				adminUser = userRepository.save(adminUser);
			}
			
			// User management
			{
				final String path = "/users";
				final String name = "User Management";
				
				this.updatePermission(adminUser, path, name, UserPermission.WRITE);
			}
			
			// Professional management
			{
				final String path = "/professional";
				final String name = "Professional Management";
				
				this.updatePermission(adminUser, path, name, UserPermission.WRITE);
			}
			
			// Subscription type management
			{
				final String path = "/subtype";
				final String name = "Subscription Type Management";
				
				this.updatePermission(adminUser, path, name, UserPermission.WRITE);
			}
			
			// Subscription management
			{
				final String path = "/subscription";
				final String name = "Subscription Management";
				
				this.updatePermission(adminUser, path, name, UserPermission.WRITE);
			}
			
			// Session group management
			{
				final String path = "/session-category";
				final String name = "Session Category";
				
				this.updatePermission(adminUser, path, name, UserPermission.WRITE);
			}
			
			// Session management
			{
				final String path = "/session";
				final String name = "Session";
				
				this.updatePermission(adminUser, path, name, UserPermission.WRITE);
			}
			
			// Edit Account Type
			{
				final String path = UserConstants.EDIT_ACCOUNT_TYPE_PATH;
				final String name = "Edit Account Type";
				
				this.updatePermission(adminUser, path, name, UserPermission.WRITE);
			}
			
			// Edit Permissions
			{
				final String path = UserConstants.EDIT_PERMISSIONS_PATH;
				final String name = "Edit Permissions";
				
				this.updatePermission(adminUser, path, name, UserPermission.WRITE);
			}
			
			// Discount code management
			{
				final String path = "/discount-code";
				final String name = "Discount code management";
				
				this.updatePermission(adminUser, path, name, UserPermission.WRITE);
			}
			
			{
				final String path = "/discount-code-" + BusinessCodeType.B2B_MULTIPLE.getValue();
				//final String name = "B2B multiple code management";
				
				//this.updatePermission(adminUser, path, name, UserPermission.WRITE);
				this.deleteFunctionality(adminUser, path);
			}
			
			{
				final String path = "/discount-code-" + BusinessCodeType.B2B_UNIQUE.getValue();
				//final String name = "B2B unique code management";
				
				//this.updatePermission(adminUser, path, name, UserPermission.WRITE);
				this.deleteFunctionality(adminUser, path);
			}
			
			{
				final String path = "/discount-code-" + BusinessCodeType.B2C_DISCOUNT_100.getValue();
				//final String name = "B2C discount 100% code management";
				
				//this.updatePermission(adminUser, path, name, UserPermission.WRITE);
				this.deleteFunctionality(adminUser, path);
			}
			
			{
				final String path = "/discount-code-" + BusinessCodeType.B2C_DISCOUNT.getValue();
				//final String name = "B2C discount code management";
				
				//this.updatePermission(adminUser, path, name, UserPermission.WRITE);
				this.deleteFunctionality(adminUser, path);
			}
			
			{
				final String path = "/discount-code-" + BusinessCodeType.GIFT_CARD.getValue();
				//final String name = "Gift card management";
				
				//this.updatePermission(adminUser, path, name, UserPermission.WRITE);
				this.deleteFunctionality(adminUser, path);
			}
			
			// Gift management
			{
				final String path = "/gift";
				final String name = "Gift management";
				
				this.updatePermission(adminUser, path, name, UserPermission.WRITE);
			}
			
			// Slideshow management
			{
				final String path = "/slideshow";
				final String name = "Slideshow management";
				
				this.updatePermission(adminUser, path, name, UserPermission.WRITE);
			}
			
			// Splash screen management
			{
				final String path = "/splash-screen";
				final String name = "Splash screen management";
				
				this.updatePermission(adminUser, path, name, UserPermission.WRITE);
			}
			
			// Create temp user
			{
				final String name = "David Jame";
				final String email = "david.jame@gmail.com";
				this.createProfessional(name, email);
				
			}
			
			{
				final String name = "David Beckam";
				final String email = "david.beckam@gmail.com";
				this.createProfessional(name, email);
				
			}
		} catch (Exception ex) {
			logger.error("Initialize database is error!", ex);
		}
		
		try {
			this.initializeCache();
		} catch (Exception ex) {
			logger.error("Initialize cache is error!", ex);
		}
	}
	
	private Permission updatePermission(User adminUser, String path, String name, UserPermission userPermission) {
		final Optional<Functionality> optionalFunctionality = this.functionalityRepository.findByPath(path);
		Functionality functionality = optionalFunctionality.isPresent() ? optionalFunctionality.get() : null;
		if (functionality == null) {
			functionality = new Functionality();
			functionality.setPath(path);
			functionality.setName(name);
			
			functionality = this.functionalityRepository.save(functionality);
		}
		
		final Optional<Permission> optionalPermission = this.permissionRepository.findByUserIdAndFunctionalityId(adminUser.getId(), functionality.getId());
		Permission permission = optionalPermission.isPresent() ? optionalPermission.get() : null;
		if (permission == null) {
			permission = new Permission();
			permission.setUser(adminUser);
			permission.setFunctionality(functionality);
			permission.setUserPermission(userPermission.getValue());
			
			permission = this.permissionRepository.save(permission);
		} else if (permission.getUserPermission() != userPermission.getValue()) {
			permission.setUserPermission(userPermission.getValue());
			
			permission = this.permissionRepository.save(permission);
		}
		
		return permission;
	}
	
	private void deleteFunctionality(User adminUser, String path) {
		final Optional<Functionality> optionalFunctionality = this.functionalityRepository.findByPath(path);
		if (!optionalFunctionality.isPresent()) {
			return;
		}
		
		Functionality functionality = optionalFunctionality.get();
		if (functionality == null) {
			return;
		}
		
		final Optional<Permission> optionalPermission = this.permissionRepository.findByUserIdAndFunctionalityId(adminUser.getId(), functionality.getId());
		if (optionalPermission.isPresent()) {
			final Permission permission = optionalPermission.get();
			
			if (permission != null) {
				this.permissionRepository.delete(permission);
			}
		}
		
		this.functionalityRepository.delete(functionality);
	}
	
	private User createProfessional(String name, String email) {
		User user = userRepository.findByEmail(email);
		
		if (user != null) {
			return user;
		}
		
		final String password = this.passwordEncoder.encode("123456");
		
		user = new User(email, password, UserType.PROFESSIONAL.getValue(), name, "");
		user.setStatus(UserStatus.ACTIVE.getValue());
		
		return userRepository.save(user);
	}
	
	private void initializeCache() {
		
	}
}
