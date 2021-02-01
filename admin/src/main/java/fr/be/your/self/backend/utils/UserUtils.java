package fr.be.your.self.backend.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import fr.be.your.self.common.LoginType;
import fr.be.your.self.common.UserStatus;
import fr.be.your.self.common.UserType;
import fr.be.your.self.common.Utils;
import fr.be.your.self.model.Subscription;
import fr.be.your.self.model.User;
import fr.be.your.self.util.StringUtils;

public final class UserUtils {
	
	public static boolean isAdmin(User user) {
		return UserType.ADMIN.getValue().equals(user.getUserType()); 
	}
	
	
	
	public static List<User> convertUsersCsv(List<UserCsv> usersCsv, MessageSource messageSource){
		List<User> users = new ArrayList<>();
		
		if (usersCsv == null || usersCsv.size() ==0) {
			return users;
		}
		
		for (UserCsv userCsv : usersCsv) {
			User user = new User();
			user.setTitle(userCsv.getTitle());
			user.setFirstName(userCsv.getFirstName());
			user.setLastName(userCsv.getLastName());
			user.setEmail(userCsv.getEmail());
			user.setStatus(UserUtils.findIntStatus(userCsv.getStatus(), messageSource));
			user.setReferralCode(userCsv.getReferralCode());
			user.setUserType(userCsv.getUserType());
			users.add(user);
			
		}
		return users;
		
	}
	
	public static String generateRandomPassword(int pwdLength) {
		return StringUtils.randomAlphanumeric(pwdLength);
	}
	
	
	public static List<UserCsv> extractUserCsv(Iterable<User> users, MessageSource messageSource) {
		List<UserCsv> returnList = new ArrayList<UserCsv>();
		if (users == null)
			return returnList;
		for (User user : users) {
			UserCsv userCSV = UserUtils.newUserCsv(user, messageSource);
			returnList.add(userCSV);
		}

		return returnList;
	}

	
	public static UserCsv newUserCsv(User user, MessageSource messageSource) {
		UserCsv userCsv = new UserCsv();
		userCsv.setTitle(user.getTitle());
		userCsv.setLastName(user.getLastName());
		userCsv.setFirstName( user.getFirstName());
		userCsv.setEmail(user.getEmail());
		userCsv.setLoginType(LoginType.toStrValue(user.getLoginType()));
		userCsv.setStatus(messageSource.getMessage("user.status." + user.getStatus(), null, LocaleContextHolder.getLocale()));
		userCsv.setReferralCode(user.getReferralCode());
		userCsv.setUserType(user.getUserType());
		Subscription sub = Utils.findSubscriptionUser(user);
		if (sub != null && sub.getSubtype() != null) {
			userCsv.setSubscriptionType( sub.getSubtype().getName());
		}
		return userCsv;
	}

	public static int findIntStatus(String status, MessageSource messageSource) {
		String active = messageSource.getMessage("user.status." + UserStatus.ACTIVE.getValue(), null, LocaleContextHolder.getLocale());
		if (active.equalsIgnoreCase(status)) {
			return UserStatus.ACTIVE.getValue();
		}
		return UserStatus.INACTIVE.getValue();
	}


	public static String assignPassword(User user, PasswordEncoder passwordEncoder, int pwdLength) {
		String tempPwd = UserUtils.generateRandomPassword(pwdLength);
		String encodedPwd = passwordEncoder.encode(tempPwd);
		user.setPassword(encodedPwd);
		return tempPwd;
	}
}
