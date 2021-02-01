package fr.be.your.self.backend.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.dto.PageableResponse;
import fr.be.your.self.exception.BusinessException;
import fr.be.your.self.model.SplashScreen;
import fr.be.your.self.service.BaseService;
import fr.be.your.self.service.SplashScreenService;

@Controller
@RequestMapping(Constants.PATH.WEB_ADMIN_PREFIX + "/" + SplashScreenController.NAME)
public class SplashScreenController extends BaseResourceController<SplashScreen, SplashScreen, SplashScreen, Integer> {
	
	public static final String NAME = "splash-screen";
	
	private static final String BASE_MEDIA_URL = Constants.PATH.WEB_ADMIN_PREFIX 
			+ Constants.PATH.WEB_ADMIN.MEDIA 
			+ Constants.FOLDER.MEDIA.SPLASH_SCREEN;
	
	private static final Map<String, String[]> SORTABLE_COLUMNS = new HashMap<>();
	
	static {
		SORTABLE_COLUMNS.put("id", new String[] { "id" });
		SORTABLE_COLUMNS.put("created", new String[] { "created" });
	}
	
	@Autowired
	private SplashScreenService mainService;
	
	@Override
	protected String getName() {
		return NAME;
	}
	
	@Override
	protected String getDefaultPageTitle(String baseMessageKey) {
		return this.getMessage(baseMessageKey + ".page.title", "Splash screen management");
	}
	
	@Override
	protected String getUploadDirectoryName() {
		return this.dataSetting.getUploadFolder() + Constants.FOLDER.MEDIA.SPLASH_SCREEN;
	}
	
	@Override
	protected BaseService<SplashScreen, Integer> getService() {
		return this.mainService;
	}
	
	@Override
	protected Map<String, String[]> getSortableColumns() {
		return SORTABLE_COLUMNS;
	}

	@Override
	protected SplashScreen newDomain() {
		return new SplashScreen();
	}

	@Override
	protected SplashScreen createDetailDto(SplashScreen domain) {
		return domain == null ? new SplashScreen() : domain;
	}

	@Override
	protected SplashScreen createSimpleDto(SplashScreen domain) {
		return domain == null ? new SplashScreen() : domain;
	}

	@Override
	protected String getBaseMediaURL() {
		return BASE_MEDIA_URL;
	}
	
	@Override
	protected String getListView() {
		return "pages/splash-screen-form";
	}

	@Override
	protected String getFormView() {
		return "pages/splash-screen-form";
	}
	
	private void loadPageOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model) throws BusinessException {
		SplashScreen mainDomain = this.mainService.getMainSplashScreen();
		
		if (mainDomain == null) {
			mainDomain = new SplashScreen();
			mainDomain = this.mainService.create(mainDomain);
		}
		
		model.addAttribute("dto", mainDomain);
		model.addAttribute("action", "update/" + mainDomain.getId());
	}
	
	@Override
	protected void loadListPageOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, Map<String, String> searchParams, PageableResponse<SplashScreen> pageableDto)
			throws BusinessException {
		super.loadListPageOptions(session, request, response, model, searchParams, pageableDto);
		
		this.loadPageOptions(session, request, response, model);
	}

	@Override
	protected void loadDetailFormOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, SplashScreen domain, SplashScreen dto) throws BusinessException {
		super.loadDetailFormOptions(session, request, response, model, domain, dto);
		
		this.loadPageOptions(session, request, response, model);
	}
	
	@PostMapping("/update/{id}")
	@Transactional
    public String updateDomain(
    		@PathVariable("id") Integer id, 
    		@ModelAttribute("dto") @Validated SplashScreen dto, 
    		HttpSession session, HttpServletRequest request, HttpServletResponse response, 
    		BindingResult result, RedirectAttributes redirectAttributes, Model model) {
		
		if (result.hasErrors()) {
        	final FieldError fieldError = result.getFieldError();
        	final String message = this.getMessage(fieldError.getCode(), fieldError.getDefaultMessage());
        	
        	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
        }
        
        final String text = dto.getText();
        /*
        if (StringUtils.isNullOrSpace(text)) {
        	final String message = this.getRequiredFieldMessage("text", "Text required");
        	
        	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
	        return "redirect:" + this.getBaseURL() + "/current-page";
        }
        */
        
        SplashScreen domain = this.mainService.getById(id);
        if (domain == null) {
        	final String message = this.getIdNotFoundMessage(id);
        	
        	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
	        return "redirect:" + this.getBaseURL() + "/current-page";
        }
        
        domain.setText(text);
        final SplashScreen savedDomain = this.mainService.update(domain);
        
        // ====> Error
        if (savedDomain == null || result.hasErrors()) {
        	final FieldError fieldError = result.getFieldError();
        	final String message = fieldError == null 
        			? this.getProcessingError()
        			: this.getMessage(fieldError.getCode(), fieldError.getDefaultMessage());
        	
        	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
	        return "redirect:" + this.getBaseURL() + "/current-page";
        }
        
        // ====> Success
        redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update");
        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "success");
        
        return "redirect:" + this.getBaseURL() + "/current-page";
    }
}
