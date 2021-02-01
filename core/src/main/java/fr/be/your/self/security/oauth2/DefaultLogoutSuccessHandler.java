package fr.be.your.self.security.oauth2;

import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

public class DefaultLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

	public DefaultLogoutSuccessHandler() {
	    super();
	    
	    this.setUseReferer(false);
    }
}
