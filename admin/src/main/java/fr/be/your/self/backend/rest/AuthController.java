package fr.be.your.self.backend.rest;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import fr.be.your.self.backend.dto.UserCreateRequest;
import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.common.ErrorStatusCode;
import fr.be.your.self.common.UserType;
import fr.be.your.self.dto.StatusResponse;
import fr.be.your.self.exception.InvalidException;
import fr.be.your.self.exception.ValidationException;
import fr.be.your.self.model.User;
import fr.be.your.self.service.UserService;
import fr.be.your.self.util.StringUtils;

//@CrossOrigin
@RestController
@RequestMapping(Constants.PATH.AUTHENTICATION_PREFIX)
public class AuthController {
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;
    
    //@Autowired
    //private JwtToken jwtToken;

    //@Autowired
    //private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private GoogleIdTokenVerifier googleIdTokenVerifier;
    
    @GetMapping("/me")
	public ResponseEntity<?> profile(final Principal principal) throws Exception {
    	return ResponseEntity.ok(principal);
    }
    
    @PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody UserCreateRequest body) throws Exception {
		final String email = body.getEmail();
		
		if (StringUtils.isNullOrSpace(email)) {
			throw new InvalidException(ErrorStatusCode.INVALID_PARAMETER, "email");
		}
		
		if (this.userService.existsEmail(email)) {
			throw new ValidationException(ErrorStatusCode.USERNAME_EXISTED, "Username already existed", email);
		}

		final String password = body.getPassword();
		final String encodedPassword = this.passwordEncoder.encode(password);
		final String firstname = body.getFirstname();
		final String lastname = body.getLastname();
		
		final User domain = new User(email, encodedPassword, UserType.B2C.getValue(), firstname, lastname);
		final User savedDomain = this.userService.create(domain);
		
		final StatusResponse result = new StatusResponse(true);
		result.addData("data", savedDomain.getId());
		
		// TODO: PhatPQ - RestAPI => Send email to notify
		return ResponseEntity.ok(result);
	}
    
    /*
    @RequestMapping(value = "/google/verify-token", method = RequestMethod.POST)
    public ResponseEntity<?> verifyGoogleToken(@RequestBody TokenRequest tokenRequest) throws Exception {
    	final GoogleIdToken idToken = this.googleIdTokenVerifier.verify(tokenRequest.getToken());
		final Payload payload = idToken.getPayload();
		final String userId = payload.getSubject();
		
        final UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(userId);
        final String token = this.jwtToken.generateToken(userDetails);
        final AuthenticatedUserResponse authenticatedUser = this.generateAuthenticatedUser(userDetails);
        
        return ResponseEntity.ok(new TokenResponse(authenticatedUser, AuthenticationStrategy.JWT.getValue(), token));
    }
    
    private AuthenticatedUserResponse generateAuthenticatedUser(UserDetails user) {
    	final Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
    	
    	final AuthenticatedUserResponse jwtUser = new AuthenticatedUserResponse();
    	jwtUser.setEmail(user.getUsername());
    	
    	if (!authorities.isEmpty()) {
    		jwtUser.setRole(authorities.iterator().next().getAuthority());
    	}
    	
    	if (user instanceof JwtUserDetails) {
    		final JwtUserDetails jwtUserDetails = (JwtUserDetails) user;
    		
    		jwtUser.setFullname(jwtUserDetails.getFullname());
        	jwtUser.setAvatar(jwtUserDetails.getAvatar());
        	
        	// TODO: PhatPQ - RestAPI
    	}
    	
    	return jwtUser;
    }
    */
}