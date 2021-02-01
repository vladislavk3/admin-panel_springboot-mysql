package fr.be.your.self.backend.utils;

import javax.servlet.http.HttpServletRequest;

import fr.be.your.self.backend.setting.Constants;

public class AdminUtils {
	private static final String ACTIVATE_URL = Constants.PATH.WEB_ADMIN_PREFIX 
			+ Constants.PATH.AUTHENTICATION_PREFIX 
			+ Constants.PATH.AUTHENTICATION.ACTIVATE;
	
	public static String buildActivateAccountUrl(HttpServletRequest request) {
		String activateAccountUrl = request.getScheme() + "://" + request.getServerName() + ":"
				+ request.getServerPort() + request.getContextPath();

		if (activateAccountUrl.endsWith("/")) {
			activateAccountUrl = activateAccountUrl.substring(0, activateAccountUrl.length() - 1);
		}
		activateAccountUrl += ACTIVATE_URL;

		return activateAccountUrl;
	}
}
