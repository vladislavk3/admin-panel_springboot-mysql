package fr.be.your.self.backend.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.be.your.self.model.User;
import fr.be.your.self.service.BaseService;
import fr.be.your.self.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController<User> {
	
	@Autowired
	private UserService userService;
	
	@Override
	protected BaseService<User, Integer> getService() {
		return this.userService;
	}
	
}
