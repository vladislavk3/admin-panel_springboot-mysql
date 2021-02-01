package fr.be.your.self.security.oauth2;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

public class DefaultLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	public DefaultLoginFailureHandler() {
		super();
	}

	public DefaultLoginFailureHandler(String defaultFailureUrl) {
		super(defaultFailureUrl);
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		if (exception instanceof DisabledUsernameException) {
			request.getSession().setAttribute("errorType", "disabled");
		} else {
			request.getSession().setAttribute("errorType", "credentials");
		}
		
		super.onAuthenticationFailure(request, response, exception);
	}
	
}
