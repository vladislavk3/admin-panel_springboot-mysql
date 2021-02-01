package fr.be.your.self.security.oauth2;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class DisabledUsernameException extends UsernameNotFoundException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8155527943142994594L;

	public DisabledUsernameException(String msg) {
		super(msg);
	}
}
