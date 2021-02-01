package fr.be.your.self.backend.controller;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import fr.be.your.self.backend.dto.PermissionDto;
import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.common.UserType;

@Controller
public class DefaultController extends BaseController {
	
	@GetMapping(path = { "/", "/home" })
    public String defaultHome(HttpServletRequest request, Model model) {
		request.getSession().removeAttribute("errorType");
		
		final Authentication oauth = SecurityContextHolder.getContext().getAuthentication();
		if (oauth == null || !oauth.isAuthenticated()) {
			return "redirect:" + Constants.PATH.AUTHENTICATION_PREFIX + Constants.PATH.AUTHENTICATION.LOGIN;	
		}
		
		final Collection<? extends GrantedAuthority> authorities = oauth.getAuthorities();
		if (authorities == null || authorities.isEmpty()) {
			return "redirect:" + Constants.PATH.AUTHENTICATION_PREFIX + Constants.PATH.AUTHENTICATION.LOGIN;
		}
		
		final PermissionDto permission = (PermissionDto) model.getAttribute("permission");
		if (permission == null) {
			return "redirect:" + Constants.PATH.AUTHENTICATION_PREFIX + Constants.PATH.AUTHENTICATION.LOGIN;
		}
		
		boolean needLogin = true;
		final Iterator<? extends GrantedAuthority> iteratorAuthorities = authorities.iterator();
		while (iteratorAuthorities.hasNext()) {
			final String role = iteratorAuthorities.next().getAuthority();
			
			if (UserType.ADMIN.getValue().equalsIgnoreCase(role)
					|| ("ROLE_" + UserType.ADMIN.getValue()).equalsIgnoreCase(role)) {
				needLogin = false;
				
				for (String path : Constants.PATH.WEB_ADMIN.PATHS) {
					final String functionPath = Constants.PATH.WEB_ADMIN_PREFIX + path;
					
					if (permission.hasPermission(functionPath)) {
						return "redirect:" + functionPath;		
					}
				}
				//If don't have permission to any functionality
				return hello();
			}
		}
		
		if (needLogin) {
			return "redirect:" + Constants.PATH.AUTHENTICATION_PREFIX + Constants.PATH.AUTHENTICATION.LOGIN;
		}
		
		return accessDenied(model);
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
    
    @GetMapping("/hello")
    public String hello() {
        return "home";
    }

    @GetMapping(Constants.PATH.ACCESS_DENIED)
    public String accessDenied(Model model) {
        return this.error(model);
    }
    
    @GetMapping(Constants.PATH.ERROR)
    public String error(Model model) {
    	model.addAttribute("displayHeader", this.dataSetting.isDisplayHeaderOnAuthPage());
		model.addAttribute("allowRegister", this.dataSetting.isAllowRegisterOnAuthPage());
		model.addAttribute("allowSocial", this.dataSetting.isAllowSocialOnAuthPage());
		
        return "error/403";
    }
}