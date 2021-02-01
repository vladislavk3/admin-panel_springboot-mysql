package fr.be.your.self.backend.controller;

import java.nio.file.Path;
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
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.be.your.self.backend.dto.SessionCategoryDto;
import fr.be.your.self.backend.dto.SessionCategorySimpleDto;
import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.exception.BusinessException;
import fr.be.your.self.model.SessionCategory;
import fr.be.your.self.service.BaseService;
import fr.be.your.self.service.SessionCategoryService;
import fr.be.your.self.util.StringUtils;

@Controller
@RequestMapping(Constants.PATH.WEB_ADMIN_PREFIX + "/" + SessionCategoryController.NAME)
public class SessionCategoryController extends BaseResourceController<SessionCategory, SessionCategorySimpleDto, SessionCategoryDto, Integer> {
	
	public static final String NAME = "session-category";
	
	private static final String BASE_MEDIA_URL = Constants.PATH.WEB_ADMIN_PREFIX 
			+ Constants.PATH.WEB_ADMIN.MEDIA 
			+ Constants.FOLDER.MEDIA.SESSION_CATEGORY;
	
	private static final Map<String, String[]> SORTABLE_COLUMNS = new HashMap<>();
	
	static {
		SORTABLE_COLUMNS.put("name", new String[] { "name" });
		SORTABLE_COLUMNS.put("sessionCount", new String[] { "sessionCount" });
		//SORTABLE_COLUMNS.put("sessionCount", new String[] { "(SELECT COUNT(s) FROM a.sessions)" });
	}
	
	@Autowired
	private SessionCategoryService mainService;
	
	@Override
	protected String getName() {
		return NAME;
	}
	
	@Override
	protected String getDefaultPageTitle(String baseMessageKey) {
		return this.getMessage(baseMessageKey + ".page.title", "Session Category management");
	}
	
	@Override
	protected String getUploadDirectoryName() {
		return this.dataSetting.getUploadFolder() + Constants.FOLDER.MEDIA.SESSION_CATEGORY;
	}
	
	@Override
	protected BaseService<SessionCategory, Integer> getService() {
		return this.mainService;
	}
	
	@Override
	protected Map<String, String[]> getSortableColumns() {
		return SORTABLE_COLUMNS;
	}

	@Override
	protected SessionCategory newDomain() {
		return new SessionCategory();
	}

	@Override
	protected SessionCategoryDto createDetailDto(SessionCategory domain) {
		return new SessionCategoryDto(domain);
	}

	@Override
	protected SessionCategorySimpleDto createSimpleDto(SessionCategory domain) {
		return new SessionCategorySimpleDto(domain);
	}

	@Override
	protected String getBaseMediaURL() {
		return BASE_MEDIA_URL;
	}
	
	/*
	@Override
	protected PageableResponse<SessionCategory> pageableSearch(Map<String, String> searchParams, PageRequest pageable,
			Sort sort) {
		if (sort == null || sort.getOrderFor("(SELECT COUNT(s) FROM a.sessions)") == null) {
			return super.pageableSearch(searchParams, pageable, sort);
		}
		
		String search = "";
		if (searchParams != null ) {
			search = searchParams.get("q");
		}
		
		final JpaSort newSort = JpaSort.unsafe(sort.getOrderFor("(SELECT COUNT(s) FROM a.sessions)").getDirection(), "(SELECT COUNT(s) FROM a.sessions)");
		final PageRequest newPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), newSort);
		
		return this.mainService.pageableSearchSortSessionCount(search, newPageable);
	}
	*/
	
	@Override
	protected void loadDetailFormOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, SessionCategory domain, SessionCategoryDto dto) throws BusinessException {
		super.loadDetailFormOptions(session, request, response, model, domain, dto);
		
		final String supportImageTypes = String.join(",", this.dataSetting.getImageMimeTypes());
		final String supportImageExtensions = String.join(",", this.dataSetting.getImageFileExtensions());
		final long supportImageSize = this.dataSetting.getImageMaxFileSize();
		
		model.addAttribute("supportImageTypes", supportImageTypes);
		model.addAttribute("supportImageExtensions", supportImageExtensions);
		model.addAttribute("supportImageSize", supportImageSize);
		model.addAttribute("supportImageSizeLabel", StringUtils.formatFileSize(supportImageSize));
	}

	@PostMapping("/create")
	@Transactional
    public String createDomain(
    		@ModelAttribute("dto") @Validated SessionCategoryDto dto, 
    		HttpSession session, HttpServletRequest request, HttpServletResponse response, 
    		BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
        	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
        }
        
        // ====> Validate image file
        final MultipartFile uploadImageFile = dto.getUploadImageFile();
        if (uploadImageFile == null || uploadImageFile.isEmpty()) {
        	final ObjectError error = this.createRequiredFieldError(result, "image", "Image required");
        	result.addError(error);
        	
        	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
        }
        
        // ====> Process upload image file
        final Path uploadImageFilePath = this.processUploadImageFile(uploadImageFile, result);
        if (uploadImageFilePath == null) {
        	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
        }
        
        // ====> Update domain
        final String uploadImageFileName = uploadImageFilePath.getFileName().toString();
        
        final SessionCategory domain = this.newDomain();
        dto.copyToDomain(domain);
        
        domain.setImage(uploadImageFileName);
        
        final SessionCategory savedDomain = this.mainService.create(domain);
        
        // ====> Error, delete upload file
        if (savedDomain == null || result.hasErrors()) {
        	this.deleteUploadFile(uploadImageFilePath);
        	
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
    		@ModelAttribute("dto") @Validated SessionCategoryDto dto, 
    		HttpSession session, HttpServletRequest request, HttpServletResponse response, 
    		BindingResult result, RedirectAttributes redirectAttributes, Model model) {
		
        if (result.hasErrors()) {
        	dto.setId(id);
        	return this.redirectEditPage(session, request, response, redirectAttributes, model, id, dto);
        }
        
        SessionCategory domain = this.mainService.getById(id);
        if (domain == null) {
        	final ObjectError error = this.createIdNotFoundError(result, id);
        	result.addError(error);
        	
        	dto.setId(id);
        	return this.redirectEditPage(session, request, response, redirectAttributes, model, id, dto);
        }
        
        dto.copyToDomain(domain);
        
        // ====> Process upload image and content file
        String deleteImageFileName = null;
        Path uploadImageFilePath = null;
        
        final MultipartFile uploadImageFile = dto.getUploadImageFile();
        if (uploadImageFile != null && !uploadImageFile.isEmpty()) {
        	deleteImageFileName = domain.getImage();
        	
        	// ====> Process upload image file
        	uploadImageFilePath = this.processUploadImageFile(uploadImageFile, result);
            if (uploadImageFilePath == null) {
            	dto.setId(id);
            	return this.redirectEditPage(session, request, response, redirectAttributes, model, id, dto);
            }
            
            final String uploadImageFileName = uploadImageFilePath.getFileName().toString();
        	domain.setImage(uploadImageFileName);
        }
        
        final SessionCategory savedDomain = this.mainService.update(domain);
        
        // ====> Error, delete upload file
        if (savedDomain == null || result.hasErrors()) {
        	this.deleteUploadFile(uploadImageFilePath);
        	
        	if (!result.hasErrors()) {
	        	final ObjectError error = this.createProcessingError(result);
	        	result.addError(error);
        	}
        	
        	dto.setId(id);
        	return this.redirectEditPage(session, request, response, redirectAttributes, model, id, dto);
        }
        
        // ====> Success, delete old image file
        this.deleteUploadFile(deleteImageFileName);
        
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
		
		final SessionCategory domain = this.mainService.getById(id);
		if (domain == null) {
			final String message = this.getIdNotFoundMessage(id);
			
			redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "delete");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
			return "redirect:" + this.getBaseURL() + "/current-page";
		}
		
		final String deleteImageFileName = domain.getImage();
		
		final boolean result = this.mainService.delete(id);
		if (result) {
			// ====> Success, delete old image file
			this.deleteUploadFile(deleteImageFileName);
	        
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
