package fr.be.your.self.backend.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hazelcast.util.StringUtil;
import com.opencsv.CSVWriter;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.MappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import fr.be.your.self.backend.cache.CacheManager;
import fr.be.your.self.backend.dto.PermissionDto;
import fr.be.your.self.backend.dto.UserDto;
import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.backend.utils.AdminUtils;
import fr.be.your.self.backend.utils.CsvUtils;
import fr.be.your.self.backend.utils.SubscriptionCsv;
import fr.be.your.self.backend.utils.UserCsv;
import fr.be.your.self.backend.utils.UserCsvMappingStrategy;
import fr.be.your.self.backend.utils.UserUtils;
import fr.be.your.self.common.LoginType;
import fr.be.your.self.common.UserPermission;
import fr.be.your.self.common.UserStatus;
import fr.be.your.self.common.UserType;
import fr.be.your.self.dto.PageableResponse;
import fr.be.your.self.exception.BusinessException;
import fr.be.your.self.model.Permission;
import fr.be.your.self.model.SubscriptionType;
import fr.be.your.self.model.User;
import fr.be.your.self.model.UserConstants;
import fr.be.your.self.service.BaseService;
import fr.be.your.self.service.SubscriptionTypeService;
import fr.be.your.self.service.UserService;
import fr.be.your.self.util.NumberUtils;
import fr.be.your.self.util.StringUtils;

@Controller
@RequestMapping(Constants.PATH.WEB_ADMIN_PREFIX + "/" + UserController.NAME)
public class UserController extends BaseResourceController<User, User, UserDto, Integer>  {
	public static final String NAME = "users";

	public static String CSV_USERS_EXPORT_FILE = "users.csv";
	
	
	@Autowired
	private UserService userService;
	
	@Autowired
	SubscriptionTypeService subtypeService;

	@Autowired
	private CacheManager cacheManager;
	
	private static final Map<String, String[]> SORTABLE_COLUMNS = new HashMap<>();

	static {
		SORTABLE_COLUMNS.put("email", new String[] { "email" });
		SORTABLE_COLUMNS.put("fullName", new String[] { "fullName" });
		SORTABLE_COLUMNS.put("status", new String[] { "status" });
		SORTABLE_COLUMNS.put("subtype", new String[] { "subtype" });
		SORTABLE_COLUMNS.put("userType", new String[] { "userType" });

	}
	
	@Override
	protected String getFormView() {
		return "users/user-form";
	}
	
	@Override
	protected String getListView() {
		return "users/user-list";
	}
	
	@Override
	protected BaseService<User, Integer> getService() {
		return userService;
	}

	@Override
	protected String getName() {
		return NAME;
	}

	@Override
	protected String getDefaultPageTitle(String baseMessageKey) {
		return this.getMessage(baseMessageKey + ".page.title", "User management");
	}

	@Override
	protected String getUploadDirectoryName() {
		return this.dataSetting.getUploadFolder() + Constants.FOLDER.MEDIA.SESSION;
	}

	@Override
	protected User newDomain() {
		return new User();
	}

	@Override
	protected UserDto createDetailDto(User domain) {
		UserDto userDto =  new UserDto(domain);
		if (domain == null) { //This is the case when we create new User, if we update user then domain != null
			addDefaultPermissions(userDto);
		}
		
		return userDto;

	}

	@Override
	protected User createSimpleDto(User domain) {
		return domain;
	}

	@Override
	protected Map<String, String[]> getSortableColumns() {
		return SORTABLE_COLUMNS;
	}

	@Override
	protected void loadDetailFormOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, User domain, UserDto dto) throws BusinessException {
		super.loadPersonTitles(model);

		int editAccType = UserPermission.DENIED.getValue();
		int editPermissions = UserPermission.DENIED.getValue();
				
		PermissionDto permission = (PermissionDto) model.getAttribute("permission");
		
		editAccType = permission.getPermission(UserConstants.EDIT_ACCOUNT_TYPE_PATH);
		editPermissions = permission.getPermission(UserConstants.EDIT_PERMISSIONS_PATH);
		
		List<Integer> loginTypes = LoginType.getPossibleIntValues();
		List<String> userTypes = UserType.getPossibleStrValues();
		List<Integer> userStatuses = UserStatus.getPossibleIntValues();
		List<Integer> userPermissions = UserPermission.getPossibleIntValues();

		model.addAttribute("loginTypes", loginTypes);
		model.addAttribute("userTypes", userTypes);
		model.addAttribute("userStatuses", userStatuses);
		model.addAttribute("userPermissions", userPermissions);

		
		model.addAttribute("editAccType", editAccType);
		model.addAttribute("editPermissions", editPermissions);

	}
	
		
	
	@PostMapping("/create")
	@Transactional
    public String createDomain(
    		@Validated @ModelAttribute("dto") UserDto dto, 
    		HttpSession session, HttpServletRequest request, HttpServletResponse response, 
    		BindingResult result, RedirectAttributes redirectAttributes, Model model) {
		
        if (result.hasErrors()) {
        	String message = this.getMessage("users.error.save");
			setActionResultInModel(model, "create", "warning", message);
        	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
        }
                
        final User user = this.newDomain();
        dto.copyToDomain(user);
        
		boolean isAdminUser = UserUtils.isAdmin(user);
		boolean isAutoActivateAccount = isAdminUser ? this.dataSetting.isAutoActivateAdminAccount()
				: this.dataSetting.isAutoActivateAccount();
		
		String tempPwd = null;
		User savedUser;
		if (user.getLoginType() == LoginType.PASSWORD.getValue()) {
			tempPwd = UserUtils.assignPassword(user, getPasswordEncoder(), this.dataSetting.getTempPwdLength());
		}

		if (userService.existsEmail(user.getEmail())) {
        	String message = this.getMessage("users.error.email.existed");
			setActionResultInModel(model, "create", "warning", message);
        	
        	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
		}
			
		if (isAutoActivateAccount) {
			user.setStatus(UserStatus.ACTIVE.getValue());
		} else {
			setActivateCodeAndTimeout(user);
		}
		
		savedUser = userService.create(user);

		//Error
        if (savedUser == null || result.hasErrors()) {
        	String message = this.getMessage("users.error.save");
			setActionResultInModel(model, "create", "warning", message);
        	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
        }
        
		//For normal user, default value = "Denied"
		for (Permission permission : dto.getPermissions()) {
			permission.setUser(savedUser); // We need user id of saved user
			this.getPermissionService().saveOrUpdate(permission);
		}
		
		if (!isAutoActivateAccount) {
			if (savedUser.getStatus() == UserStatus.INACTIVE.getValue()) {
				String activateAccountUrl = AdminUtils.buildActivateAccountUrl(request);
				sendVerificationEmailToUser(activateAccountUrl, savedUser);
			}
			
			if (tempPwd != null) {
				this.getEmailSender().sendTemporaryPassword(savedUser.getEmail(), tempPwd);
			}
		}
        
        redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "create");
        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "success");
        
        return "redirect:" + this.getBaseURL();
    }
	
	
	@PostMapping("/update/{id}")
	@Transactional
    public String updateDomain(
    		@PathVariable("id") Integer id, 
    		@ModelAttribute @Validated UserDto dto, 
    		HttpSession session, HttpServletRequest request, HttpServletResponse response, 
    		BindingResult result, RedirectAttributes redirectAttributes, Model model) {
		
        if (result.hasErrors()) {
        	String message = this.getMessage("users.error.save");
			setActionResultInModel(model, "update", "warning", message);
        	dto.setId(id);
        	return this.redirectEditPage(session, request, response, redirectAttributes, model, id, dto);
        }
       
        User domain = this.userService.getById(id);
                
        if (domain == null) {
        	final ObjectError error = this.createIdNotFoundError(result, id);
        	result.addError(error);
        	
        	dto.setId(id);
        	return this.getFormView();
        }
        
        dto.copyToDomain(domain);
        
        final User updatedDomain = this.userService.update(domain);
        
        if (updatedDomain == null) {
        	String message = this.getMessage("users.error.save");
			setActionResultInModel(model, "update", "warning", message);
        	dto.setId(id);
        	return this.redirectEditPage(session, request, response, redirectAttributes, model, id, dto);
        }
        
        
        if (dto.getPermissions() != null) {
	        for (Permission permission : dto.getPermissions()) {
				permission.setUser(domain);
				this.getPermissionService().saveOrUpdate(permission);
			}
        }

        final String permissionCacheKey = Constants.CACHE.getPermission(id);
        this.cacheManager.updateItem(permissionCacheKey, null);
        
        redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update");
        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "success");
        
        return "redirect:" + this.getBaseURL() + "/current-page";
    }
	
	@PostMapping(value = { "/delete/{id}" })
	@Transactional
    public String deletePage(
    		@PathVariable(name = "id", required = true) Integer id,
    		HttpSession session, HttpServletRequest request, HttpServletResponse response, 
    		RedirectAttributes redirectAttributes, Model model) {
		
		final User domain = this.userService.getById(id);
		if (domain == null) {
			final String message = this.getIdNotFoundMessage(id);
			
			redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "delete");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
			return "redirect:" + this.getBaseURL() + "/current-page";
		}
		
		
		final boolean result = this.userService.delete(id);
		if (result) {
			final String permissionCacheKey = Constants.CACHE.getPermission(id);
			this.cacheManager.updateItem(permissionCacheKey, null);
			
			final String message = this.getDeleteSuccessMessage(id);
			
			redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "delete");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "success");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
			
			return "redirect:" + this.getBaseURL();
		}
		
		final String message = this.getDeleteByIdErrorMessage(id);
		
		redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "delete");
        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
        
		return "redirect:" + this.getBaseURL() + "/current-page";
	}
	
	@PostMapping("/exportcsv")
	public void exportCSV(@RequestParam(value="selected_id", required=false) List<Integer> userIds,
			HttpServletResponse response) throws Exception {

		// set file name and content type

		response.setContentType("text/csv");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + CSV_USERS_EXPORT_FILE + "\"");
		
		String headers[] = this.getMessage("csv.user.headers").split(",");
		final UserCsvMappingStrategy<UserCsv> mappingStrategy = new UserCsvMappingStrategy<>(headers);
		mappingStrategy.setType(UserCsv.class);
		
		// create a csv writer
		StatefulBeanToCsv<UserCsv> writer = new StatefulBeanToCsvBuilder<UserCsv>(response.getWriter())
				.withMappingStrategy(mappingStrategy)
				.withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
				.withSeparator(CSVWriter.DEFAULT_SEPARATOR)
				.withOrderedResults(false)
				.build();

		// write all users to csv file
		List<UserCsv> usersList = StreamSupport.stream(UserUtils.extractUserCsv(userService.findAllById(userIds), this.messageSource).spliterator(), false)
												.collect(Collectors.toList());
	
		writer.write(usersList);
	}
	
	@PostMapping(value = "/importcsv")
	@Transactional
	public String fileUpload(@RequestParam("file") MultipartFile file, 										
										Model model, 
										RedirectAttributes redirectAttributes,
										HttpServletRequest request) throws IOException, CsvRequiredFieldEmptyException {
		String message;

		MappingStrategy<UserCsv> strategy = new HeaderColumnNameMappingStrategy<>();
		strategy.setType(UserCsv.class);
		
		if (!checkFileAndHeader(file, redirectAttributes, strategy, new UserCsv())) {
			return "redirect:" + this.getBaseURL()  + "/upload_csv_form";
		}

		List<UserCsv> usersCsv;
		try {
			usersCsv = CsvUtils.SINGLETON.readCsvFile(file, strategy, UserCsv.class);
		} catch (Exception e) {
			message=this.getMessage("csv.error.user.readfile");
			setRedirectAttributes(redirectAttributes, "update", "warning", message);
			return "redirect:" + this.getBaseURL()  + "/upload_csv_form";		
		}
		
		List<User> users = UserUtils.convertUsersCsv(usersCsv, this.messageSource);	
		
		if (!validateData(redirectAttributes, users)) {
			return "redirect:" + this.getBaseURL()  + "/upload_csv_form";	
		}
		
		for (User user : users) {
			String tempPwd = UserUtils.assignPassword(user, getPasswordEncoder(), this.dataSetting.getTempPwdLength());
			if (user.getStatus() == UserStatus.INACTIVE.getValue()) {
				setActivateCodeAndTimeout(user);
			}
			addDefaultPermissions(user);
			User savedUser = userService.saveOrUpdate(user);

			this.getEmailSender().sendTemporaryPassword(user.getEmail(), tempPwd);
			if (user.getStatus() == UserStatus.INACTIVE.getValue()) {
				String activateAccountUrl = AdminUtils.buildActivateAccountUrl(request);
				sendVerificationEmailToUser(activateAccountUrl, user);
			}
		}
		
		message = this.getMessage("csv.upload.sucess");
		setRedirectAttributes(redirectAttributes, "update", "success", message);
		return "redirect:" + this.getBaseURL() + "/upload_csv_form";
	}

	private boolean validateData(RedirectAttributes redirectAttributes, List<User> users) {
		String message;
		for (User user : users) {
			if (StringUtils.isNullOrEmpty(user.getEmail()) || !EmailValidator.getInstance().isValid(user.getEmail())) {
				message=this.getMessage("csv.error.email.invalid", new Object[] {user.getFullName()});				
				setRedirectAttributes(redirectAttributes, "update", "warning", message);
				return false;
			} else {
				if (userService.existsEmail(user.getEmail())) {
					message=this.getMessage("csv.error.email.existed", new Object[] {user.getFullName()});	
					setRedirectAttributes(redirectAttributes, "update", "warning", message);
					return false;
				}
			}
		}
		
		return true;
	}

	
	// show upload csv form
	@GetMapping(value = "/upload_csv_form")
	public String showUploadUserForm(Model model) {
		return this.getName() + "/upload_csv_form";
	}
	

	// reset password user
	@RequestMapping(value = "/{id}/resetpassword")
	public String resetPasswordUser(@PathVariable("id") int id, Model model, final RedirectAttributes redirectAttributes) {
		String tempPwd = UserUtils.generateRandomPassword(this.dataSetting.getTempPwdLength());
		String encodedPwd = getPasswordEncoder().encode(tempPwd);
		User user = userService.getById(id);
		user.setPassword(encodedPwd);
		userService.saveOrUpdate(user);
		this.getEmailSender().sendTemporaryPassword(user.getEmail(), tempPwd);
		redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update");
	    redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "success");
	    String message = this.getMessage("users.reset.password.success.message");					
        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
        return "redirect:" + this.getBaseURL() + "/edit/" + id;
	}
	
	// resend verification email user
	@RequestMapping(value = "/{id}/resendverifemail")
	public String resendVerificationEmail(@PathVariable("id") int id, HttpServletRequest request, Model model
			, final RedirectAttributes redirectAttributes) {
		User user = userService.getById(id);
		String activateAccountUrl = AdminUtils.buildActivateAccountUrl(request);
		setActivateCodeAndTimeout(user);
		userService.saveOrUpdate(user);
		sendVerificationEmailToUser(activateAccountUrl, user);
		redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update");
	    redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "success");
	    String message = this.getMessage("users.resend.verification.email.success.message");					
        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
        return "redirect:" + this.getBaseURL() + "/edit/" + id;
	}
	

	
	@Override
	protected void loadListPageOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, Map<String, String> searchParams, PageableResponse<User> pageableDto) throws BusinessException {
		
		final String filterRole = searchParams.get("filterRole");
		final String filterStatus = searchParams.get("filterStatus");
		List<String> userTypes = UserType.getPossibleStrValues();
		List<Integer> userStatuses = UserStatus.getPossibleIntValues();
		Iterable<SubscriptionType> subtypes = this.subtypeService.findAll();
		final String searchSubtypeIds = searchParams.get("filterSubscriptionTypesIds");
		final List<Integer> filteredSubscriptionTypesIds = NumberUtils.parseIntegers(searchSubtypeIds, ",");
		
		model.addAttribute("filterRole", filterRole);
		model.addAttribute("filterStatus", filterStatus);
		model.addAttribute("userTypes", userTypes);
		model.addAttribute("userStatuses", userStatuses);
		model.addAttribute("subtypes", subtypes);
		model.addAttribute("filteredSubscriptionTypesIds", filteredSubscriptionTypesIds);
	}
	
	@Override
	protected PageableResponse<User> pageableSearch(Map<String, String> searchParams, PageRequest pageable, Sort sort) {
		final String search = searchParams.get("q");
		final String filterRole = searchParams.get("filterRole");
		Integer filterStatus = null;
		if (!StringUtil.isNullOrEmpty(searchParams.get("filterStatus"))) {
			filterStatus = NumberUtils.parseInt(searchParams.get("filterStatus"));
		}
		final List<Integer> filterSubscriptionTypesIds = NumberUtils.parseIntegers(searchParams.get("filterSubscriptionTypesIds"), ",");
		
		return this.userService.pageableSearch(search, filterRole, filterStatus, filterSubscriptionTypesIds, pageable, sort);
	}	

}