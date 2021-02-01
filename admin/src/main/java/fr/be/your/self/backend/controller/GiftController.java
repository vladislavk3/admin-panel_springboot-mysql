package fr.be.your.self.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.dto.PageableResponse;
import fr.be.your.self.exception.BusinessException;
import fr.be.your.self.model.Gift;
import fr.be.your.self.service.BaseService;
import fr.be.your.self.service.GiftService;

@Controller
@RequestMapping(Constants.PATH.WEB_ADMIN_PREFIX + "/" + GiftController.NAME)
public class GiftController extends BaseResourceController<Gift, Gift, Gift, Integer> {
	
	public static final String NAME = "gift";
	
	private static final String BASE_MEDIA_URL = Constants.PATH.WEB_ADMIN_PREFIX 
			+ Constants.PATH.WEB_ADMIN.MEDIA 
			+ Constants.FOLDER.MEDIA.GIFT;
	
	private static final Map<String, String[]> SORTABLE_COLUMNS = new HashMap<>();
	
	static {
		SORTABLE_COLUMNS.put("name", new String[] { "name" });
		SORTABLE_COLUMNS.put("price", new String[] { "price" });
		SORTABLE_COLUMNS.put("duration", new String[] { "duration" });
	}
	
	@Autowired
	private GiftService mainService;
	
	@Override
	protected String getName() {
		return NAME;
	}
	
	@Override
	protected String getDefaultPageTitle(String baseMessageKey) {
		return this.getMessage(baseMessageKey + ".page.title", "Gift management");
	}
	
	@Override
	protected String getUploadDirectoryName() {
		return this.dataSetting.getUploadFolder() + Constants.FOLDER.MEDIA.GIFT;
	}
	
	@Override
	protected BaseService<Gift, Integer> getService() {
		return this.mainService;
	}
	
	@Override
	protected Map<String, String[]> getSortableColumns() {
		return SORTABLE_COLUMNS;
	}

	@Override
	protected Gift newDomain() {
		return new Gift();
	}

	@Override
	protected Gift createDetailDto(Gift domain) {
		return domain == null ? new Gift() : domain;
	}

	@Override
	protected Gift createSimpleDto(Gift domain) {
		return domain == null ? new Gift() : domain;
	}

	@Override
	protected String getBaseMediaURL() {
		return BASE_MEDIA_URL;
	}
	
	@Override
	protected void loadListPageOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, Map<String, String> searchParams, PageableResponse<Gift> pageableDto)
			throws BusinessException {
		super.loadListPageOptions(session, request, response, model, searchParams, pageableDto);
		
		model.addAttribute("priceUnitSymbol", this.dataSetting.getPriceUnitSymbol());
		model.addAttribute("priceUnitName", this.dataSetting.getPriceUnitName());
	}

	@Override
	protected void loadDetailFormOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, Gift domain, Gift dto) throws BusinessException {
		super.loadDetailFormOptions(session, request, response, model, domain, dto);
		
		model.addAttribute("priceUnitSymbol", this.dataSetting.getPriceUnitSymbol());
		model.addAttribute("priceUnitName", this.dataSetting.getPriceUnitName());
		
		final Set<Integer> availableDurations = this.dataSetting.getGiftDurations();
		model.addAttribute("availableDurations", availableDurations);
		
		if (domain == null && dto.getDuration() <= 0 && !availableDurations.isEmpty()) {
			dto.setDuration(availableDurations.iterator().next());
		}
	}
	
	@PostMapping("/create")
	@Transactional
    public String createDomain(
    		@ModelAttribute("dto") @Validated Gift dto, 
    		HttpSession session, HttpServletRequest request, HttpServletResponse response, 
    		BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
        	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
        }
        
        final Gift domain = this.newDomain();
        dto.copy(domain);
        
        final Gift savedDomain = this.mainService.create(domain);
        
        // ====> Error, delete upload file
        if (savedDomain == null || result.hasErrors()) {
        	if (!result.hasErrors()) {
	        	final ObjectError error = this.createProcessingError(result);
	        	result.addError(error);
        	}
        	
        	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
        }
        
        redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "create");
        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "success");
        
        return "redirect:" + this.getBaseURL();
    }
	
	@PostMapping("/update/{id}")
	@Transactional
    public String updateDomain(
    		@PathVariable("id") Integer id, 
    		@ModelAttribute("dto") @Validated Gift dto, 
    		HttpSession session, HttpServletRequest request, HttpServletResponse response, 
    		BindingResult result, RedirectAttributes redirectAttributes, Model model) {
		
        if (result.hasErrors()) {
        	dto.setId(id);
        	return this.redirectEditPage(session, request, response, redirectAttributes, model, id, dto);
        }
        
        Gift domain = this.mainService.getById(id);
        if (domain == null) {
        	final ObjectError error = this.createIdNotFoundError(result, id);
        	result.addError(error);
        	
        	dto.setId(id);
        	return this.redirectEditPage(session, request, response, redirectAttributes, model, id, dto);
        }
        
        dto.copy(domain);
        
        final Gift savedDomain = this.mainService.update(domain);
        
        // ====> Error
        if (savedDomain == null || result.hasErrors()) {
        	if (!result.hasErrors()) {
	        	final ObjectError error = this.createProcessingError(result);
	        	result.addError(error);
        	}
        	
        	dto.setId(id);
        	return this.redirectEditPage(session, request, response, redirectAttributes, model, id, dto);
        }
        
        // ====> Success
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
		
		final Gift domain = this.mainService.getById(id);
		if (domain == null) {
			final String message = this.getIdNotFoundMessage(id);
			
			redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "delete");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
			return "redirect:" + this.getBaseURL() + "/current-page";
		}
		
		final boolean result = this.mainService.delete(id);
		if (result) {
			// ====> Success
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
}
