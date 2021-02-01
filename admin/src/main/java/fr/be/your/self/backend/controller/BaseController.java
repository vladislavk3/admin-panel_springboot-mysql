package fr.be.your.self.backend.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import fr.be.your.self.backend.cache.CacheManager;
import fr.be.your.self.backend.dto.PermissionDto;
import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.backend.setting.DataSetting;
import fr.be.your.self.model.Functionality;
import fr.be.your.self.model.Permission;
import fr.be.your.self.security.oauth2.AuthenticationUserDetails;
import fr.be.your.self.service.PermissionService;
import fr.be.your.self.util.StringUtils;

public abstract class BaseController {
	
	private static final String BASE_AVATAR_URL = Constants.PATH.WEB_ADMIN_PREFIX 
			+ Constants.PATH.WEB_ADMIN.MEDIA 
			+ Constants.FOLDER.MEDIA.AVATAR;
	
	@Autowired
	protected MessageSource messageSource;
	
	@Autowired
	protected DataSetting dataSetting;
	
	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private CacheManager cacheManager;
	
	protected void initAttributes(HttpSession session, HttpServletRequest request, 
			HttpServletResponse response, Model model, PermissionDto permission) {
	}
	
	@ModelAttribute
	protected void initModelAttribute(HttpSession session, HttpServletRequest request, 
			HttpServletResponse response, Model model) {
		
		PermissionDto permission = null;
		Integer userId = null;
		String displayName = null;
		String userAvatar = null;
		
		final Authentication oauth = SecurityContextHolder.getContext().getAuthentication();
		if (oauth != null && oauth.isAuthenticated()) {
			final Object principal = oauth.getPrincipal();
			
			if (principal instanceof AuthenticationUserDetails) {
				final AuthenticationUserDetails userDetails = (AuthenticationUserDetails) principal;
				final String fullName = userDetails.getFullName();
				final String avatar = userDetails.getAvatar();
				
				displayName = oauth.getName();
				if (!StringUtils.isNullOrSpace(fullName)) {
					displayName = fullName;
				}
				
				if (!StringUtils.isNullOrSpace(avatar)) {
					userAvatar = avatar;
				}
				
				userId = userDetails.getUserId();
				
				final String permissionCacheKey = Constants.CACHE.getPermission(userId);
				permission = this.cacheManager.getItemValue(permissionCacheKey, PermissionDto.class);
				
				if (permission == null) {
					permission = new PermissionDto();
					
					final Iterable<Permission> userPermissions = this.permissionService.getPermissionByUserId(userId);
					if (userPermissions != null) {
						for (Permission userPermission : userPermissions) {
							final Functionality functionality = userPermission.getFunctionality();
							permission.addPermission(functionality.getPath(), userPermission.getUserPermission());
						}
					}
					
					this.cacheManager.updateItem(permissionCacheKey, permission);
				}
			}
		}
		
		if (permission == null) {
			permission = new PermissionDto();
		}
		
		if (StringUtils.isNullOrSpace(displayName)) {
			displayName = this.getMessage("user.anonymous", "Anonymous");
		}
		
		String servletPath = request.getServletPath();
		final int partIndex = servletPath.indexOf("/", 1);
		if (partIndex > 0) {
			servletPath = servletPath.substring(0, partIndex);
		}
		
		model.addAttribute("userId", userId);
		model.addAttribute("userAvatar", userAvatar);
		model.addAttribute("displayName", displayName);
		model.addAttribute("permission", permission);
		
		model.addAttribute("allowRegister", this.dataSetting.isAllowRegisterOnAuthPage());
		model.addAttribute("baseAvatarURL", BASE_AVATAR_URL);
		model.addAttribute("currentFunction", servletPath);
		
		this.initAttributes(session, request, response, model, permission);
	}
	
	protected String getMessage(String key, Object[] args, String defaultValue) {
		return this.messageSource.getMessage(key, args, defaultValue, LocaleContextHolder.getLocale());
	}
	
	protected String getMessage(String key, Object[] args) {
		return this.getMessage(key, args, null);
	}
	
	protected String getMessage(String key, String defaultValue) {
		return this.getMessage(key, null, defaultValue);
	}
	
	protected String getMessage(String key) {
		return this.getMessage(key, null, null);
	}
}
