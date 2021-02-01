package fr.be.your.self.backend.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.model.User;
import fr.be.your.self.service.BaseService;
import fr.be.your.self.service.UserService;

//@Controller
@RequestMapping(Constants.PATH.WEB_ADMIN_PREFIX + "/" + AdminUserController.NAME)
public class AdminUserController extends BaseResourceController<User, User, User, Integer> {
	
	public static final String NAME = "admin-user";
	
	@Autowired
	private UserService userService;
	
	@Override
	protected String getName() {
		return NAME;
	}
	
	@Override
	protected String getDefaultPageTitle(String baseMessageKey) {
		return this.getMessage("title.admin.user", "User management");
	}
	
	@Override
	protected String getUploadDirectoryName() {
		return this.dataSetting.getUploadFolder() + Constants.FOLDER.MEDIA.AVATAR;
	}

	@Override
	protected BaseService<User, Integer> getService() {
		return this.userService;
	}
	
	@Override
	protected Map<String, String[]> getSortableColumns() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected User newDomain() {
		return new User();
	}
	
	@Override
	protected User createDetailDto(User domain) {
		if (domain == null) {
			return new User();
		}
		
		return domain;
	}

	@Override
	protected User createSimpleDto(User domain) {
		return domain;
	}

	@PostMapping("/create")
    public String createDomain(@Valid User user, 
    		HttpSession session, HttpServletRequest request, 
    		HttpServletResponse response, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "pages/" + this.getName() + "-form";
        }
         
        this.userService.update(user);
        return "index";
    }
	
	@PostMapping("/update/{id}")
    public String updateDomain(@PathVariable("id") Integer id, @Valid User user, 
    		HttpSession session, HttpServletRequest request, 
    		HttpServletResponse response, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "pages/" + this.getName() + "-form";
        }
         
        this.userService.update(user);
        return "index";
    }
}
