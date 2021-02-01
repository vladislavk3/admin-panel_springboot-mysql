package fr.be.your.self.security.oauth2;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import fr.be.your.self.common.UserStatus;
import fr.be.your.self.common.UserType;
import fr.be.your.self.model.User;
import fr.be.your.self.repository.UserRepository;
import fr.be.your.self.util.StringUtils;

public class DefaultUserDetailsService implements UserDetailsService {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultUserDetailsService.class);
	
	private static final String ROLE_GRANTED_AUTHORITY_PREFIX = "ROLE_";
	
	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		final UsernameNotFoundException exception = new UsernameNotFoundException("User not found with username: " + username);
		final User user = this.userRepository.findByEmail(username);
		
		if (user == null) {
			logger.error("Cannot find the user " + username, exception);
			
			throw exception;
		}
		
		if (user.getStatus() != UserStatus.ACTIVE.getValue()) {
			logger.error("The user " + username + " is not active", exception);
			
		    throw new DisabledUsernameException("User is disabled: " + username);
		}
		
		final String userType = user.getUserType();
		if (StringUtils.isNullOrSpace(userType)) {
			logger.error("Don't have user type for user " + username, exception);
			
			throw exception;
		}
		
		final List<GrantedAuthority> roles = Arrays.asList(
				new SimpleGrantedAuthority(ROLE_GRANTED_AUTHORITY_PREFIX + userType), 
				new SimpleGrantedAuthority(ROLE_GRANTED_AUTHORITY_PREFIX + UserType.USER.getValue()));
		
		final AuthenticationUserDetails authenticationUser = new AuthenticationUserDetails(
				user.getId(), user.getEmail(), 
				user.getPassword(), roles);
		
		authenticationUser.setFullName(user.getFullName());
		
		// TODO: PhatPQ => Initialize authentication user properties
		authenticationUser.setAvatar(null);
		
		return authenticationUser;
	}
}
