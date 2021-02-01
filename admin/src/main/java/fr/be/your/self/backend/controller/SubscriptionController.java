package fr.be.your.self.backend.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.opencsv.CSVWriter;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.MappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import fr.be.your.self.backend.dto.SubscriptionDto;
import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.backend.utils.AdminUtils;
import fr.be.your.self.backend.utils.CsvUtils;
import fr.be.your.self.backend.utils.SubscriptionCsv;
import fr.be.your.self.backend.utils.SubscriptionCsvMappingStrategy;
import fr.be.your.self.backend.utils.UserUtils;
import fr.be.your.self.common.BusinessCodeStatus;
import fr.be.your.self.common.BusinessCodeType;
import fr.be.your.self.common.CanalType;
import fr.be.your.self.common.LoginType;
import fr.be.your.self.common.PaymentGateway;
import fr.be.your.self.common.PaymentStatus;
import fr.be.your.self.common.UserStatus;
import fr.be.your.self.common.UserType;
import fr.be.your.self.exception.BusinessException;
import fr.be.your.self.model.BusinessCode;
import fr.be.your.self.model.Subscription;
import fr.be.your.self.model.SubscriptionType;
import fr.be.your.self.model.User;
import fr.be.your.self.service.BaseService;
import fr.be.your.self.service.BusinessCodeService;
import fr.be.your.self.service.SubscriptionService;
import fr.be.your.self.service.SubscriptionTypeService;
import fr.be.your.self.service.UserService;
import fr.be.your.self.util.StringUtils;

@Controller
@RequestMapping(Constants.PATH.WEB_ADMIN_PREFIX + "/" + SubscriptionController.NAME)
public class SubscriptionController extends BaseResourceController<Subscription, Subscription, SubscriptionDto, Integer> {
	public static final String NAME = "subscription";

	@Autowired
	SubscriptionService subscriptionService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	SubscriptionTypeService subtypeService;
	
	@Autowired
	protected BusinessCodeService businessCodeService;
	
	public static String CSV_SUBSCRIPTION_EXPORT_FILE = "subscription.csv";

	
	private static final Map<String, String[]> SORTABLE_COLUMNS = new HashMap<>();

	static {
		SORTABLE_COLUMNS.put("name", new String[] { "name" });
		//TODO TVA add sortable columns
	}
	
	@Override
	protected String getFormView() {
		return "subscription/subscription-form";
	}
	
	@Override
	protected String getListView() {
		return "subscription/subscription-list";
	}
	
	@Override
	protected BaseService<Subscription, Integer> getService() {
		return subscriptionService;
	}

	@Override
	protected String getName() {
		return NAME;
	}

	@Override
	protected String getDefaultPageTitle(String baseMessageKey) {
		return this.getMessage(baseMessageKey + ".page.title", "Subscription management");
	}

	@Override
	protected String getUploadDirectoryName() {
		return this.dataSetting.getUploadFolder() + Constants.FOLDER.MEDIA.SESSION;
	}

	@Override
	protected Subscription newDomain() {
		return new Subscription();

	}

	@Override
	protected SubscriptionDto createDetailDto(Subscription domain) {
		return new SubscriptionDto(domain);
	}

	@Override
	protected Subscription createSimpleDto(Subscription domain) {
		return domain;
	}

	@Override
	protected Map<String, String[]> getSortableColumns() {
		return SORTABLE_COLUMNS;
	}


	@Override
	protected void loadDetailFormOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, Subscription domain, SubscriptionDto dto) throws BusinessException {
		
		final List<String> canals = CanalType.getPossibleStrValues();
		
		final List<Integer> durations = dataSetting.getSubscriptionDurations();
		final List<Integer> paymentStatuses = PaymentStatus.getPossibleIntValues();
		final List<String> paymentGateways = PaymentGateway.getPossibleStrValues();

		final String userDefaultSort = this.userService.getDefaultSort();
		final Sort userSort = this.getSortRequest(userDefaultSort);
		final List<User> users = userService.getAll(userSort);
		
		final String subtypeDefaultSort = this.subtypeService.getDefaultSort();
		final Sort subtypeSort = this.getSortRequest(subtypeDefaultSort);
		final List<SubscriptionType> subtypes = subtypeService.getAll(subtypeSort);
		
		Iterable<BusinessCode> codes =  codeService.findAll();
		Map<Integer, List<BusinessCode>> codeMap = getActiveCodeMap(codes);
		Set<Integer> codeTypes = codeMap.keySet();
		
		model.addAttribute("canals", canals);
		model.addAttribute("users", users);
		model.addAttribute("subtypes", subtypes);
		model.addAttribute("durations", durations);
		model.addAttribute("paymentStatuses", paymentStatuses);
		model.addAttribute("paymentGateways", paymentGateways);
		model.addAttribute("codeMap", codeMap);
		model.addAttribute("codeTypes", codeTypes);
		if (domain != null && domain.getBusinessCode() != null) {
			model.addAttribute("currentCodeId", domain.getBusinessCode().getId());
		} else {
			model.addAttribute("currentCodeId", SubscriptionDto.UNDEFINED_CODE);
		}

	}

	private Map<Integer, List<BusinessCode>>  getActiveCodeMap(Iterable<BusinessCode> codes) {
		Map<Integer, List<BusinessCode>> codeMap = new HashMap<Integer, List<BusinessCode>>();
		for (BusinessCode code : codes) {
			if (code.getStatus() != BusinessCodeStatus.ACTIVE.getValue()) {
				continue;
			}
			if (!codeMap.containsKey(code.getType())) {
				codeMap.put(code.getType(), new ArrayList<BusinessCode>());
			}
			codeMap.get(code.getType()).add(code);
		}
		return codeMap;
	}
	
	@PostMapping("/create")
	@Transactional
    public String createDomain(
    		@Validated @ModelAttribute("dto") SubscriptionDto dto,
    		@RequestParam(name = "codeId", required=false) Integer codeId,
    		HttpSession session, HttpServletRequest request, HttpServletResponse response, 
    		BindingResult result, RedirectAttributes redirectAttributes, Model model) {
		
        if (result.hasErrors()) {
        	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
        }
                
        final Subscription domain = this.newDomain();
        dto.copyToDomain(domain);
        
        if (codeId != null && codeId != SubscriptionDto.UNDEFINED_CODE) { 
        	BusinessCode code = codeService.getById(dto.getCodeId());
	        if (!validateData(dto, code, redirectAttributes, model, domain, "create")) {
	        	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
	        } else {
	        	domain.setBusinessCode(code);
	        }
        }
        
        
        User user = userService.getById(dto.getUserId());
        SubscriptionType subtype = subtypeService.getById(dto.getSubtypeId());
        domain.setUser(user);
        domain.setSubtype(subtype);;
        domain.setIpAddress(request.getRemoteAddr());
        
        final Subscription savedDomain = this.subscriptionService.create(domain);
        
        //Error
        if (savedDomain == null || result.hasErrors()) {
        	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
        }
        
        redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "create");
        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "success");
        
        return "redirect:" + this.getBaseURL();
    }

	private boolean validateData(SubscriptionDto dto, BusinessCode code, RedirectAttributes redirectAttributes,
			Model model, Subscription domain, String action) {
        	if (code == null) {
        		String message = this.getMessage("subscription.error.code.not.found");
        		setActionResultInModel(model, action, "warning", message);
				return false;
        	}

        	//Check that if we add new subscription using this code, we will not pass the maximum number of usage.      	
        	if ( code.isB2B() && isAddingNewUsage(domain, action, code) ) { 
        		final List<Integer> codeIds = Collections.singletonList(code.getId());
        		final Map<Integer, Integer> usedAmounts = this.businessCodeService.getUsedAmountByIds(codeIds);
        		int maxAmount;
    			if (code.isB2B_unique()) {
    				maxAmount = 1;
    			} else {
    				maxAmount = code.getMaxUserAmount();
    			}
    			if (usedAmounts != null) {
    				final Integer usedAmount = usedAmounts.get(code.getId());
    				if (usedAmount!= null && usedAmount >= maxAmount) {
    					String message = this.getMessage("subscription.error.code.max.amount", new Object[] {code.getName()});
    					setActionResultInModel(model, action, "warning", message);
    					return false;
    				}
    			}
        	}
       
		return true;
	}

	private boolean isAddingNewUsage(Subscription domain, String action, BusinessCode code) {
		if ("create".equals(action)) {
			return true;
		}
		if (domain.getBusinessCode()  == null || domain.getBusinessCode().getId() != code.getId()) { //Currently we are not using a business code OR the code we are using is not this one
			return true;
		}
		return false;
	}
	
	
	@PostMapping("/update/{id}")
	@Transactional
    public String updateDomain(
    		@PathVariable("id") Integer id, 
    		@ModelAttribute @Validated SubscriptionDto dto, 
    		@RequestParam(name = "codeId", required=false) Integer codeId,
    		HttpSession session, HttpServletRequest request, HttpServletResponse response, 
    		BindingResult result, RedirectAttributes redirectAttributes, Model model) {
		
        if (result.hasErrors()) {
        	dto.setId(id);
        	return this.getFormView();
        }
        

        Subscription domain = this.subscriptionService.getById(id);
        
        if (domain == null) {
        	final ObjectError error = this.createIdNotFoundError(result, id);
        	result.addError(error);
        	
        	dto.setId(id);
        	return this.getFormView();
        }
        
        
        dto.copyToDomain(domain);
        SubscriptionType subtype = this.subtypeService.getById(dto.getSubtypeId());
        User user = this.userService.getById(dto.getUserId());
        if (codeId != null && codeId != SubscriptionDto.UNDEFINED_CODE) {
        	BusinessCode code = codeService.getById(dto.getCodeId());
        	if (!validateData(dto, code, redirectAttributes, model, domain, "update")) {
	        	return this.redirectEditPage(session, request, response, redirectAttributes, model, id, dto);
	        } else {
	        	domain.setBusinessCode(code);
	        }
        }
                   
        
        if (codeId == SubscriptionDto.UNDEFINED_CODE) {
        	domain.setBusinessCode(null);
        }
        domain.setSubtype(subtype);
        domain.setUser(user);
        final Subscription savedDomain = this.subscriptionService.update(domain);
    
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
		
		final Subscription domain = this.subscriptionService.getById(id);
		if (domain == null) {
			final String message = this.getIdNotFoundMessage(id);
			
			redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "delete");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
			return "redirect:" + this.getBaseURL() + "/current-page";
		}
		
		
		final boolean result = this.subscriptionService.delete(id);
		if (result) {
	        
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
	
	@RequestMapping("/exportcsv")
	public void exportCsvFile(@RequestParam(value="selected_id", required=false) List<Integer> subscriptionIds,
			HttpServletResponse response) throws Exception {
		
		response.setContentType("text/csv");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + CSV_SUBSCRIPTION_EXPORT_FILE + "\"");
		
		String headers[] = this.getMessage("csv.subscription.headers").split(",");
		final SubscriptionCsvMappingStrategy<SubscriptionCsv> mappingStrategy = new SubscriptionCsvMappingStrategy<>(headers);
		mappingStrategy.setType(SubscriptionCsv.class);
		
		// create a csv writer
		StatefulBeanToCsv<SubscriptionCsv> writer = new StatefulBeanToCsvBuilder<SubscriptionCsv>(response.getWriter())
				.withMappingStrategy(mappingStrategy)
				.withQuotechar(CSVWriter.NO_QUOTE_CHARACTER).withSeparator(CSVWriter.DEFAULT_SEPARATOR)
				.withOrderedResults(false).build();

		// write subscriptions to csv file
		List<SubscriptionCsv> subscriptions = StreamSupport
				.stream(CsvUtils.extractSubscriptionCsv(subscriptionService.getByIds(subscriptionIds)).spliterator(), false)
				.collect(Collectors.toList());
	
		writer.write(subscriptions);
	}
	
	
	@PostMapping(value = "/importcsv")
	@Transactional
	public String fileUpload(@RequestParam("file") MultipartFile file, HttpServletRequest request, 
							RedirectAttributes redirectAttributes,Model model)
			throws IOException, ParseException, CsvRequiredFieldEmptyException {

		String message;
		MappingStrategy<SubscriptionCsv> strategy = new HeaderColumnNameMappingStrategy<>();
		strategy.setType(SubscriptionCsv.class);
		
		if (!checkFileAndHeader(file, redirectAttributes, strategy, new SubscriptionCsv())) {
			return "redirect:" + this.getBaseURL()  + "/upload_csv_form";
		}

		List<SubscriptionCsv> subscriptionsCsv;
		try {
			subscriptionsCsv = CsvUtils.SINGLETON.readCsvFile(file, strategy, SubscriptionCsv.class);
		} catch (Exception e) {
			message=this.getMessage("csv.error.subscription.readfile");
			setRedirectAttributes(redirectAttributes, "update", "warning", message);
			return "redirect:" + this.getBaseURL()  + "/upload_csv_form";		
		}					
		
		if (!validateData(redirectAttributes, subscriptionsCsv)) {
			return "redirect:" + this.getBaseURL()  + "/upload_csv_form";
		}

		for (SubscriptionCsv subCsv : subscriptionsCsv) {
			User user;
			Subscription sub = CsvUtils.SINGLETON.createSubscriptionFromCsv(subCsv);

			String email = subCsv.getEmail();
			if (this.userService.existsEmail(email)) {
				user = this.userService.getByEmail(email);
			} else {
				user = createNewUser(request, subCsv);
			}
			sub.setUser(user);
			SubscriptionType subtype = this.subtypeService.findAllByNameContainsIgnoreCase(subCsv.getSubtype()).iterator().next();
			sub.setSubtype(subtype);
			if (!StringUtils.isNullOrEmpty(subCsv.getCode())) {
				Iterable<BusinessCode> codes = this.codeService.findAllByNameContainsIgnoreCase(subCsv.getCode());
				if (codes != null && codes.iterator().hasNext()) {
					sub.setBusinessCode(codes.iterator().next());
				}
			}
			this.subscriptionService.create(sub);
		}
		
		
		message = this.getMessage("csv.upload.sucess");
		setRedirectAttributes(redirectAttributes, "update", "success", message);
		return "redirect:" + this.getBaseURL() + "/upload_csv_form";
	}

	private User createNewUser(HttpServletRequest request, SubscriptionCsv subCsv) {
		User user;
		// Create a new user with login type pwd;
		user = newUserFromSubscriptionCsv(subCsv);
		String tempPwd = UserUtils.assignPassword(user, getPasswordEncoder(), this.dataSetting.getTempPwdLength());
		setActivateCodeAndTimeout(user);
		addDefaultPermissions(user);
		user = this.userService.create(user);
		//send verification email
		this.getEmailSender().sendTemporaryPassword(user.getEmail(), tempPwd);
		String activateAccountUrl = AdminUtils.buildActivateAccountUrl(request);
		sendVerificationEmailToUser(activateAccountUrl, user);
		return user;
	}

	private User newUserFromSubscriptionCsv(SubscriptionCsv subCsv) {
		User user;
		user = new User();
		user.setFirstName(subCsv.getFirstName());
		user.setLastName(subCsv.getLastName());
		user.setEmail(subCsv.getEmail());
		user.setLoginType(LoginType.PASSWORD.getValue());
		user.setTitle(subCsv.getTitle());
		user.setStatus(UserStatus.INACTIVE.getValue());
		user.setUserType(UserType.B2C.getValue());
		return user;
	}

	

	private boolean validateData(RedirectAttributes redirectAttributes, List<SubscriptionCsv> subscriptionsCsv) {
		String message;
		
		if (subscriptionsCsv.size() > this.dataSetting.getCsvSubscriptionMaxNbLine()) {
			message=this.getMessage("csv.error.max.nb.line", new Object[] {this.dataSetting.getCsvSubscriptionMaxNbLine(), 
																			subscriptionsCsv.size()});
			setRedirectAttributes(redirectAttributes, "update", "warning", message);
			return false;	
		}
		
		for (SubscriptionCsv subscription : subscriptionsCsv) {
			if (StringUtils.isNullOrEmpty(subscription.getEmail()) || !EmailValidator.getInstance().isValid(subscription.getEmail())) {
				message=this.getMessage("csv.error.email.invalid", new Object[] {subscription.getFullName()});				
				setRedirectAttributes(redirectAttributes, "update", "warning", message);
				return false;
			}		
			if (!CanalType.getPossibleStrValues().contains(subscription.getCanal())) {
				message=this.getMessage("csv.error.canal.invalid", new Object[] {subscription.getCanal(), String.join(", ", CanalType.getPossibleStrValues())});	
				setRedirectAttributes(redirectAttributes, "update", "warning", message);
				return false;
			}
			if (!PaymentGateway.getPossibleStrValues().contains(subscription.getPaymenGateway())) {
				message=this.getMessage("csv.error.paymentgateway.invalid", new Object[] {subscription.getPaymenGateway(), String.join(", ", PaymentGateway.getPossibleStrValues())});	
				setRedirectAttributes(redirectAttributes, "update", "warning", message);
				return false;
			}
			if (StringUtils.isNullOrEmpty(subscription.getSubtype())) {		
				message=this.getMessage("csv.error.subscription.type.empty", new Object[] {subscription.getFullName()});				
				setRedirectAttributes(redirectAttributes, "update", "warning", message);
				return false;				
			} else {
				if (!this.subtypeService.existsByName(subscription.getSubtype())) {
					message=this.getMessage("csv.error.subscription.type.inexistent", new Object[] {subscription.getSubtype()});				
					setRedirectAttributes(redirectAttributes, "update", "warning", message);
					return false;
				}
			}
			if (!StringUtils.isNullOrEmpty(subscription.getCode())) {
				Iterable<BusinessCode> codes = this.codeService.findAllByNameContainsIgnoreCase(subscription.getCode());
				if (codes == null || !codes.iterator().hasNext() ) {
					message=this.getMessage("csv.error.code.inexistent", new Object[] {subscription.getCode()});				
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
	
}
