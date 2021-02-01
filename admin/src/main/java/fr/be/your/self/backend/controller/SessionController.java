package fr.be.your.self.backend.controller;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.be.your.self.backend.dto.IdNameDto;
import fr.be.your.self.backend.dto.SessionDto;
import fr.be.your.self.backend.dto.SessionSimpleDto;
import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.dto.PageableResponse;
import fr.be.your.self.exception.BusinessException;
import fr.be.your.self.model.Session;
import fr.be.your.self.model.SessionCategory;
import fr.be.your.self.model.User;
import fr.be.your.self.service.BaseService;
import fr.be.your.self.service.SessionCategoryService;
import fr.be.your.self.service.SessionService;
import fr.be.your.self.service.UserService;
import fr.be.your.self.util.NumberUtils;
import fr.be.your.self.util.StringUtils;

@Controller
@RequestMapping(Constants.PATH.WEB_ADMIN_PREFIX + "/" + SessionController.NAME)
public class SessionController extends BaseResourceController<Session, SessionSimpleDto, SessionDto, Integer> {
	
	public static final String NAME = "session";
	
	private static final String BASE_MEDIA_URL = Constants.PATH.WEB_ADMIN_PREFIX 
			+ Constants.PATH.WEB_ADMIN.MEDIA 
			+ Constants.FOLDER.MEDIA.SESSION;
	
	private static final Map<String, String[]> SORTABLE_COLUMNS = new HashMap<>();
	
	static {
		SORTABLE_COLUMNS.put("title", new String[] { "title" });
		SORTABLE_COLUMNS.put("voice", new String[] { "voice.firstName", "voice.lastName" });
		SORTABLE_COLUMNS.put("duration", new String[] { "duration" });
		SORTABLE_COLUMNS.put("free", new String[] { "free" });
		SORTABLE_COLUMNS.put("created", new String[] { "created" });
	}
	
	@Autowired
	private SessionService mainService;
	
	@Autowired
	private SessionCategoryService sessionCategoryService;
	
	@Autowired
	private UserService userService;
	
	@Override
	protected String getName() {
		return NAME;
	}
	
	@Override
	protected String getDefaultPageTitle(String baseMessageKey) {
		return this.getMessage(baseMessageKey + ".page.title", "Session management");
	}
	
	@Override
	protected String getUploadDirectoryName() {
		return this.dataSetting.getUploadFolder() + Constants.FOLDER.MEDIA.SESSION;
	}
	
	@Override
	protected BaseService<Session, Integer> getService() {
		return this.mainService;
	}
	
	@Override
	protected Map<String, String[]> getSortableColumns() {
		return SORTABLE_COLUMNS;
	}

	@Override
	protected Session newDomain() {
		return new Session();
	}

	@Override
	protected SessionDto createDetailDto(Session domain) {
		final SessionDto dto = new SessionDto(domain);
		
		if (this.isValidAudioContentType(dto.getContentMimeType())) {
			dto.setContentFileType(Constants.MEDIA_TYPE.AUDIO);
		} else if (this.isValidVideoContentType(dto.getContentMimeType())) {
			dto.setContentFileType(Constants.MEDIA_TYPE.VIDEO);
		} else {
			dto.setContentFileType(Constants.MEDIA_TYPE.IMAGE);
		}
		
		return dto;
	}

	@Override
	protected SessionSimpleDto createSimpleDto(Session domain) {
		final SessionSimpleDto dto = new SessionSimpleDto(domain);
		
		if (this.isValidAudioContentType(dto.getContentMimeType())) {
			dto.setContentFileType(Constants.MEDIA_TYPE.AUDIO);
		} else if (this.isValidVideoContentType(dto.getContentMimeType())) {
			dto.setContentFileType(Constants.MEDIA_TYPE.VIDEO);
		} else {
			dto.setContentFileType(Constants.MEDIA_TYPE.IMAGE);
		}
		
		return dto;
	}

	@Override
	protected String getBaseMediaURL() {
		return BASE_MEDIA_URL;
	}
	
	@Override
	protected void loadListPageOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, Map<String, String> searchParams, PageableResponse<SessionSimpleDto> pageableDto) throws BusinessException {
		super.loadListPageOptions(session, request, response, model, searchParams, pageableDto);
		
		final String categoryDefaultSort = this.sessionCategoryService.getDefaultSort();
		final Sort categorySort = this.getSortRequest(categoryDefaultSort);
		final List<SessionCategory> sessionCategories = this.sessionCategoryService.getAll(categorySort);
		
		final String searchCategoryIds = searchParams.get("filterCategoryIds");
		final List<Integer> filteredCategoryIds = NumberUtils.parseIntegers(searchCategoryIds, ",");
		
		final List<IdNameDto<Integer>> filteredVoices = new ArrayList<>();
		
		final String userDefaultSort = this.userService.getDefaultSort();
		final Sort userSort = this.getSortRequest(userDefaultSort);
		
		final String searchVoiceIds = searchParams.get("filterVoiceIds");
		final List<Integer> filteredVoiceIds = NumberUtils.parseIntegers(searchVoiceIds, ",");
		if (filteredVoiceIds != null && !filteredVoiceIds.isEmpty()) {
			final List<User> professionals = this.userService.getActivateProfessionals(filteredVoiceIds, userSort);
			
			for (User professional : professionals) {
				filteredVoices.add(new IdNameDto<Integer>(professional));
			}
		} else {
			/*
			final PageRequest pageable = this.getPageRequest(1, this.dataSetting.getDefaultDropdownSearchPageSize(), userSort);
			final PageableResponse<User> professionals = this.userService.searchProfessionalByName(null, pageable, userSort);
			
			if (professionals.getItems() != null) {
				for (User professional : professionals.getItems()) {
					filteredVoices.add(new IdNameDto(professional));
				}
			}
			*/
		}
		
		model.addAttribute("sessionCategories", sessionCategories);
		model.addAttribute("filteredCategoryIds", filteredCategoryIds);
		model.addAttribute("filteredVoiceIds", filteredVoiceIds);
		model.addAttribute("filteredVoices", filteredVoices);
	}

	@Override
	protected void loadDetailFormOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, Session domain, SessionDto dto) throws BusinessException {
		super.loadDetailFormOptions(session, request, response, model, domain, dto);
		
		final String supportImageTypes = String.join(",", this.dataSetting.getImageMimeTypes());
		final String supportImageExtensions = String.join(",", this.dataSetting.getImageFileExtensions());
		final long supportImageSize = this.dataSetting.getImageMaxFileSize();
		
		final String supportAudioTypes = String.join(",", this.dataSetting.getAudioMimeTypes());
		final String supportAudioExtensions = String.join(",", this.dataSetting.getAudioFileExtensions());
		final long supportAudioSize = this.dataSetting.getAudioMaxFileSize();
		
		final String supportVideoTypes = String.join(",", this.dataSetting.getVideoMimeTypes());
		final String supportVideoExtensions = String.join(",", this.dataSetting.getVideoFileExtensions());
		final long supportVideoSize = this.dataSetting.getVideoMaxFileSize();
		
		final String supportMediaTypes = String.join(",", this.dataSetting.getMediaMimeTypes());
		final String supportMediaExtensions = String.join(",", this.dataSetting.getMediaFileExtensions());
		final long supportMediaSize = supportVideoSize > supportAudioSize ? supportVideoSize : supportAudioSize;
		
		model.addAttribute("supportImageTypes", supportImageTypes);
		model.addAttribute("supportImageExtensions", supportImageExtensions);
		model.addAttribute("supportImageSize", supportImageSize);
		model.addAttribute("supportImageSizeLabel", StringUtils.formatFileSize(supportImageSize));
		
		model.addAttribute("supportAudioTypes", supportAudioTypes);
		model.addAttribute("supportAudioExtensions", supportAudioExtensions);
		model.addAttribute("supportAudioSize", supportAudioSize);
		model.addAttribute("supportAudioSizeLabel", StringUtils.formatFileSize(supportAudioSize));
		
		model.addAttribute("supportVideoTypes", supportVideoTypes);
		model.addAttribute("supportVideoExtensions", supportVideoExtensions);
		model.addAttribute("supportVideoSize", supportVideoSize);
		model.addAttribute("supportVideoSizeLabel", StringUtils.formatFileSize(supportVideoSize));
		
		model.addAttribute("supportMediaTypes", supportMediaTypes);
		model.addAttribute("supportMediaExtensions", supportMediaExtensions);
		model.addAttribute("supportMediaSize", supportMediaSize);
		model.addAttribute("supportMediaSizeLabel", StringUtils.formatFileSize(supportMediaSize));
		
		final String categoryDefaultSort = this.sessionCategoryService.getDefaultSort();
		final Sort categorySort = this.getSortRequest(categoryDefaultSort);
		final List<SessionCategory> sessionCategories = this.sessionCategoryService.getAll(categorySort);
		model.addAttribute("sessionCategories", sessionCategories);
		
		if (domain != null) {
			final User voice = domain.getVoice();
			if (voice != null) {
				model.addAttribute("voiceName", voice.getDisplay());
			}
		}
	}

	@Override
	protected PageableResponse<Session> pageableSearch(Map<String, String> searchParams, PageRequest pageable, Sort sort) {
		final String search = searchParams.get("q");
		final List<Integer> filterCategoryIds = NumberUtils.parseIntegers(searchParams.get("filterCategoryIds"), ",");
		final List<Integer> filterVoiceIds = NumberUtils.parseIntegers(searchParams.get("filterVoiceIds"), ",");
		
		return this.mainService.pageableSearch(search, filterCategoryIds, filterVoiceIds, pageable, sort);
	}

	@PostMapping("/create")
	@Transactional
    public String createDomain(
    		@Validated @ModelAttribute("dto") SessionDto dto, 
    		@RequestParam(value = "categoryIds" , required = false) Integer[] categoryIds,
    		HttpSession session, HttpServletRequest request, HttpServletResponse response, 
    		BindingResult result, RedirectAttributes redirectAttributes, Model model) {
		
        if (result.hasErrors()) {
        	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
        }
        
        // ====> Session category
        final List<SessionCategory> categories = this.sessionCategoryService.getByIds(categoryIds);
        
        // ====> Session voice 
        User voice = null;
        
        final Integer voiceId = dto.getVoiceId();
        if (voiceId != null && voiceId > 0) {
    		voice = this.userService.getById(voiceId);
    		
    		if (voice == null) {
    			final ObjectError error = this.createFieldError(result, "voiceId", "not.found", new Object[] { voiceId }, "Cannot find voice #" + voiceId);
            	result.addError(error);
            	
            	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
    		}
        }
        
        // ====> Validate image and content file
        final MultipartFile uploadImageFile = dto.getUploadImageFile();
        if (uploadImageFile == null || uploadImageFile.isEmpty()) {
        	final ObjectError error = this.createRequiredFieldError(result, "image", "Image is required");
        	result.addError(error);
        	
        	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
        }
        
        final MultipartFile uploadContentFile = dto.getUploadContentFile();
        if (uploadContentFile == null || uploadContentFile.isEmpty()) {
        	final ObjectError error = this.createRequiredFieldError(result, "contentFile", "Content file is required");
        	result.addError(error);
        	
        	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
        }
        
        // ====> Process upload image and content file
        final Path uploadImageFilePath = this.processUploadImageFile(uploadImageFile, result);
        if (uploadImageFilePath == null) {
        	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
        }
        
        final Path uploadContentFilePath = this.processUploadContentFile(uploadContentFile, result);
        if (uploadContentFilePath == null) {
        	this.deleteUploadFile(uploadImageFilePath);
        	
        	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
        }
        
        // ====> Update domain
        final String uploadImageFileName = uploadImageFilePath.getFileName().toString();
        final String uploadContentFileName = uploadContentFilePath.getFileName().toString();
        final String contentFileContentType = this.getFileContentType(uploadContentFilePath, this.dataSetting.getMediaMimeTypes());
        
        try {
	        final Session domain = this.newDomain();
	        dto.copyToDomain(domain);
	        
	        domain.setVoice(voice);
	        domain.setCategories(categories);
	        domain.setImage(uploadImageFileName);
	        domain.setContentFile(uploadContentFileName);
	        domain.setContentMimeType(contentFileContentType);
	        
	        final Session savedDomain = this.mainService.create(domain);
	        
	        // ====> Error, delete upload file
	        if (savedDomain == null || result.hasErrors()) {
	        	this.deleteUploadFile(uploadImageFilePath);
	        	this.deleteUploadFile(uploadContentFilePath);
	        	
	        	if (!result.hasErrors()) {
		        	final ObjectError error = this.createProcessingError(result);
		        	result.addError(error);
	        	}
	        	
	        	return this.redirectAddNewPage(session, request, response, redirectAttributes, model, dto);
	        }
	        
	        if (categories != null && !categories.isEmpty()) {
	        	final List<Integer> updateCategoryIds = new ArrayList<Integer>();
	        	
	        	for (SessionCategory category : categories) {
	        		updateCategoryIds.add(category.getId());
				}
	        	
	        	this.sessionCategoryService.updateSessionCount(updateCategoryIds);
	        }
	        
	        redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "create");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "success");
	        
	        return "redirect:" + this.getBaseURL();
        } catch (Exception ex) {
        	this.deleteUploadFile(uploadImageFilePath);
        	this.deleteUploadFile(uploadContentFilePath);
        	
			throw ex;
		}
    }
	
	@PostMapping("/update/{id}")
	@Transactional
    public String updateDomain(
    		@PathVariable("id") Integer id, 
    		@Validated @ModelAttribute("dto") SessionDto dto, 
    		@RequestParam(value = "categoryIds" , required = false) Integer[] categoryIds,
    		HttpSession session, HttpServletRequest request, HttpServletResponse response, 
    		BindingResult result, RedirectAttributes redirectAttributes, Model model) {
		
        if (result.hasErrors()) {
        	dto.setId(id);
        	return this.redirectEditPage(session, request, response, redirectAttributes, model, id, dto);
        }
        
        Session domain = this.mainService.getById(id);
        if (domain == null) {
        	final ObjectError error = this.createIdNotFoundError(result, id);
        	result.addError(error);
        	
        	dto.setId(id);
        	return this.redirectEditPage(session, request, response, redirectAttributes, model, id, dto);
        }
        
        dto.copyToDomain(domain);
        
        final List<Integer> updateCategoryIds = new ArrayList<Integer>();
        
        final List<SessionCategory> currentCategories = domain.getCategories();
        if (currentCategories != null) {
        	for (SessionCategory category : currentCategories) {
        		updateCategoryIds.add(category.getId());
			}
        }
        
        // ====> Session category
        final List<SessionCategory> categories = this.sessionCategoryService.getByIds(categoryIds);
        domain.setCategories(categories);
        
        if (categories != null) {
        	for (SessionCategory category : categories) {
        		updateCategoryIds.add(category.getId());
			}
        }
        
        // ====> Session voice 
        final Integer voiceId = dto.getVoiceId();
        if (voiceId == null) {
        	domain.setVoice(null);
        } else {
        	final User currentVoice = domain.getVoice();
        	
        	if (currentVoice == null || currentVoice.getId() != voiceId) {
        		final User voice = this.userService.getById(voiceId);
        		
        		if (voice == null) {
        			final ObjectError error = this.createFieldError(result, "voiceId", "not.found", new Object[] { voiceId }, "Cannot find voice #" + voiceId);
                	result.addError(error);
                	
                	dto.setId(id);
                	return this.redirectEditPage(session, request, response, redirectAttributes, model, id, dto);
        		}
        		
        		domain.setVoice(voice);
        	}
        }
        
        // ====> Process upload image and content file
        String deleteImageFileName = null;
        Path uploadImageFilePath = null;
        
        String deleteContentFileName = null;
        Path uploadContentFilePath = null;
        
        try {
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
	        
	        final MultipartFile uploadContentFile = dto.getUploadContentFile();
	        if (uploadContentFile != null && !uploadContentFile.isEmpty()) {
	        	deleteContentFileName = domain.getContentFile();
	        	
	        	// ====> Process upload content file
	        	uploadContentFilePath = this.processUploadContentFile(uploadContentFile, result);
	        	if (uploadContentFilePath == null) {
	    			this.deleteUploadFile(uploadImageFilePath);
	    			
	            	dto.setId(id);
	            	return this.redirectEditPage(session, request, response, redirectAttributes, model, id, dto);
	        	}
	        	
	        	final String uploadContentFileName = uploadContentFilePath.getFileName().toString();
	        	final String contentFileContentType = this.getFileContentType(uploadContentFilePath, this.dataSetting.getMediaMimeTypes());
	        	
	        	domain.setContentFile(uploadContentFileName);
	        	domain.setContentMimeType(contentFileContentType);
	        }
	        
	        final Session savedDomain = this.mainService.update(domain);
	        
	        // ====> Error, delete upload file
	        if (savedDomain == null || result.hasErrors()) {
	        	this.deleteUploadFile(uploadImageFilePath);
	        	this.deleteUploadFile(uploadContentFilePath);
	        	
	        	if (!result.hasErrors()) {
		        	final ObjectError error = this.createProcessingError(result);
		        	result.addError(error);
	        	}
	        	
	        	dto.setId(id);
	        	return this.redirectEditPage(session, request, response, redirectAttributes, model, id, dto);
	        }
	        
	        if (!updateCategoryIds.isEmpty()) {
	        	this.sessionCategoryService.updateSessionCount(updateCategoryIds);
	        }
	        
	        // ====> Success, delete old image file
	        this.deleteUploadFile(deleteImageFileName);
	        this.deleteUploadFile(deleteContentFileName);
	        
	        redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "success");
	        
	        return "redirect:" + this.getBaseURL() + "/current-page";
	        
        } catch (Exception ex) {
        	this.deleteUploadFile(uploadImageFilePath);
        	this.deleteUploadFile(uploadContentFilePath);
        	
			throw ex;
		}
    }
	
	@PostMapping(value = { "/delete/{id}" })
	@Transactional
    public String deletePage(
    		@PathVariable(name = "id", required = true) Integer id,
    		HttpSession session, HttpServletRequest request, HttpServletResponse response, 
    		RedirectAttributes redirectAttributes, Model model) {
		
		final Session domain = this.mainService.getById(id);
		if (domain == null) {
			final String message = this.getIdNotFoundMessage(id);
			
			redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "delete");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
			return "redirect:" + this.getBaseURL() + "/current-page";
		}
		
		final String deleteImageFileName = domain.getImage();
		final String deleteContentFileName = domain.getContentFile();
		
		final boolean result = this.mainService.delete(id);
		if (result) {
			// Success, delete old media files
			final List<SessionCategory> categories = domain.getCategories();
			if (categories != null && categories.isEmpty()) {
				final List<Integer> updateCategoryIds = new ArrayList<Integer>();
				
				for (SessionCategory category : categories) {
					updateCategoryIds.add(category.getId());
				}
				
				if (!updateCategoryIds.isEmpty()) {
		        	this.sessionCategoryService.updateSessionCount(updateCategoryIds);
		        }
			}
			
			this.deleteUploadFile(deleteImageFileName);
			this.deleteUploadFile(deleteContentFileName);
	        
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
