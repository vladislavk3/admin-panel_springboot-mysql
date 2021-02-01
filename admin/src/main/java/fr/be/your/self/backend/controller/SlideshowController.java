package fr.be.your.self.backend.controller;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.be.your.self.backend.dto.SlideshowDto;
import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.dto.PageableResponse;
import fr.be.your.self.exception.BusinessException;
import fr.be.your.self.model.Slideshow;
import fr.be.your.self.model.SlideshowImage;
import fr.be.your.self.service.BaseService;
import fr.be.your.self.service.SlideshowService;
import fr.be.your.self.util.StringUtils;

@Controller
@RequestMapping(Constants.PATH.WEB_ADMIN_PREFIX + "/" + SlideshowController.NAME)
public class SlideshowController extends BaseResourceController<Slideshow, SlideshowDto, SlideshowDto, Integer> {
	
	public static final String NAME = "slideshow";
	
	private static final String BASE_MEDIA_URL = Constants.PATH.WEB_ADMIN_PREFIX 
			+ Constants.PATH.WEB_ADMIN.MEDIA 
			+ Constants.FOLDER.MEDIA.SLIDE_SHOW;
	
	private static final Map<String, String[]> SORTABLE_COLUMNS = new HashMap<>();
	
	static {
		SORTABLE_COLUMNS.put("startDate", new String[] { "startDate", });
		SORTABLE_COLUMNS.put("endDate", new String[] { "endDate" });
	}
	
	@Autowired
	private SlideshowService mainService;
	
	@Override
	protected String getName() {
		return NAME;
	}
	
	@Override
	protected String getDefaultPageTitle(String baseMessageKey) {
		return this.getMessage(baseMessageKey + ".page.title", "Slideshow management");
	}
	
	@Override
	protected String getUploadDirectoryName() {
		return this.dataSetting.getUploadFolder() + Constants.FOLDER.MEDIA.SLIDE_SHOW;
	}
	
	@Override
	protected BaseService<Slideshow, Integer> getService() {
		return this.mainService;
	}
	
	@Override
	protected Map<String, String[]> getSortableColumns() {
		return SORTABLE_COLUMNS;
	}

	@Override
	protected Slideshow newDomain() {
		return new Slideshow();
	}

	@Override
	protected SlideshowDto createDetailDto(Slideshow domain) {
		return new SlideshowDto(domain);
	}

	@Override
	protected SlideshowDto createSimpleDto(Slideshow domain) {
		return new SlideshowDto(domain);
	}

	@Override
	protected String getBaseMediaURL() {
		return BASE_MEDIA_URL;
	}
	
	@Override
	protected String getListView() {
		return "pages/slideshow-form";
	}

	@Override
	protected String getFormView() {
		return "pages/slideshow-form";
	}

	@Override
	protected PageableResponse<Slideshow> pageableSearch(Map<String, String> searchParams, PageRequest pageable, Sort sort) {
		final Date today = DateUtils.truncate(new Date(), Calendar.DATE);
		
		return this.mainService.searchAvailaible(today, null, sort);
	}

	private void loadPageOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model) throws BusinessException {
		final String supportImageTypes = String.join(",", this.dataSetting.getImageMimeTypes());
		final String supportImageExtensions = String.join(",", this.dataSetting.getImageFileExtensions());
		final long supportImageSize = this.dataSetting.getImageMaxFileSize();
		
		model.addAttribute("supportImageTypes", supportImageTypes);
		model.addAttribute("supportImageExtensions", supportImageExtensions);
		model.addAttribute("supportImageSize", supportImageSize);
		model.addAttribute("supportImageSizeLabel", StringUtils.formatFileSize(supportImageSize));
		
		final Slideshow currentSlideshow = this.mainService.getCurrentSlideshow();
		final SlideshowDto mainDto = new SlideshowDto(currentSlideshow);
		
		model.addAttribute("mainDto", mainDto);
		
		final Date today = DateUtils.truncate(new Date(), Calendar.DATE);
		
		final SlideshowDto newScheduleDto = new SlideshowDto();
		newScheduleDto.setStartDate(today);
		model.addAttribute("newScheduleDto", newScheduleDto);
		
		final SlideshowDto editScheduleDto = new SlideshowDto();
		editScheduleDto.setStartDate(today);
		model.addAttribute("editScheduleDto", editScheduleDto);
		
		final SlideshowDto slideShowItemDto = new SlideshowDto();
		slideShowItemDto.setStartDate(today);
		model.addAttribute("slideShowItemDto", slideShowItemDto);
	}
	
	@Override
	protected void loadListPageOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, Map<String, String> searchParams, PageableResponse<SlideshowDto> pageableDto)
			throws BusinessException {
		super.loadListPageOptions(session, request, response, model, searchParams, pageableDto);
		this.loadPageOptions(session, request, response, model);
	}

	@Override
	protected void loadDetailFormOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, Slideshow domain, SlideshowDto dto) throws BusinessException {
		super.loadDetailFormOptions(session, request, response, model, domain, dto);
		this.loadPageOptions(session, request, response, model);
	}
	
	@PostMapping("/create")
	@Transactional
    public String createDomain(
    		@ModelAttribute("newScheduleDto") @Validated SlideshowDto dto, 
    		HttpSession session, HttpServletRequest request, HttpServletResponse response, 
    		BindingResult result, RedirectAttributes redirectAttributes, Model model) {
		
		if (dto.getStartDate() == null) {
			final String message = this.getRequiredFieldMessage("startDate", "Date required");
        	
        	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "create.schedule");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
	        return "redirect:" + this.getBaseURL() + "/current-page";
		}
		
		if (dto.getStartDate().getTime() < new Date().getTime()) {
			final String message = this.getInvalidFieldMessage("startDate", "Date is invalid");
        	
        	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "create.schedule");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
	        return "redirect:" + this.getBaseURL() + "/current-page";
		}
		
        // ====> Validate image file
        final MultipartFile uploadImageFile = dto.getUploadImageFile();
        if (uploadImageFile == null || uploadImageFile.isEmpty()) {
        	final String message = this.getRequiredFieldMessage("image", "Image required");
        	
        	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "create.schedule");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
	        return "redirect:" + this.getBaseURL() + "/current-page";
        }
        
        // ====> Process upload image file
        final Path uploadImageFilePath = this.processUploadImageFile(uploadImageFile, result);
        if (uploadImageFilePath == null) {
        	final FieldError fieldError = result.getFieldError();
        	final String message = fieldError == null 
        			? this.getInvalidFieldMessage("image", "Image is invalid")
        			: this.getMessage(fieldError.getCode(), fieldError.getDefaultMessage());
        	
        	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "create.schedule");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
	        return "redirect:" + this.getBaseURL() + "/current-page";
        }
        
        // ====> Update domain
        final String uploadImageFileName = uploadImageFilePath.getFileName().toString();
        
        final Slideshow domain = this.newDomain();
        dto.copyToDomain(domain);
        
        try {
        	final Slideshow savedDomain = this.mainService.create(domain);
        	
        	// ====> Error, delete upload file
            if (savedDomain == null || result.hasErrors()) {
            	this.deleteUploadFile(uploadImageFilePath);
            	
            	final FieldError fieldError = result.getFieldError();
            	final String message = fieldError == null 
            			? this.getProcessingError()
            			: this.getMessage(fieldError.getCode(), fieldError.getDefaultMessage());
            	
            	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "create.schedule");
    	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
    	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
    	        
    	        return "redirect:" + this.getBaseURL() + "/current-page";
            }
	        
            // ====> Create new image
	        final SlideshowImage newImage = new SlideshowImage();
	        newImage.setSlideshow(domain);
	        newImage.setImage(uploadImageFileName);
	        newImage.setLink(dto.getLink());
	        newImage.setIndex(1);
	        
	        final SlideshowImage savedImage = this.mainService.createImage(newImage);
	        
	        // ====> Error, delete upload file
	        if (savedImage == null || result.hasErrors()) {
	        	this.deleteUploadFile(uploadImageFilePath);
	        	
	        	final FieldError fieldError = result.getFieldError();
            	final String message = fieldError == null 
            			? this.getProcessingError()
            			: this.getMessage(fieldError.getCode(), fieldError.getDefaultMessage());
            	
            	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "create.schedule");
    	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
    	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
    	        
    	        return "redirect:" + this.getBaseURL() + "/current-page";
	        }
	        
	        // ====> Success
	        redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update.main");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "success");
	        
	        return "redirect:" + this.getBaseURL() + "/current-page#schedule-link-" + savedDomain.getId();
        } catch (Exception ex) {
        	this.deleteUploadFile(uploadImageFilePath);
        	
        	throw ex;
		}
    }
	
	@PostMapping("/update/{id}")
	@Transactional
    public String updateScheduleDomain(
    		@PathVariable("id") Integer id, 
    		@ModelAttribute("editScheduleDto") @Validated SlideshowDto dto,
    		HttpSession session, HttpServletRequest request, HttpServletResponse response, 
    		BindingResult result, RedirectAttributes redirectAttributes, Model model) {
		
        if (result.hasErrors()) {
        	final FieldError fieldError = result.getFieldError();
        	final String message = this.getMessage(fieldError.getCode(), fieldError.getDefaultMessage());
        	
        	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update.schedule");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
	        return "redirect:" + this.getBaseURL() + "/current-page";
        }
        
        Slideshow domain = id == 0 
        		? this.mainService.create(new Slideshow())
        		: this.mainService.getById(id);
        
        if (domain == null) {
        	final String message = this.getIdNotFoundMessage(id);
        	
        	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update.schedule");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
	        return "redirect:" + this.getBaseURL() + "/current-page";
        }
        
        // ====> Validate image file
        final MultipartFile uploadImageFile = dto.getUploadImageFile();
        if (uploadImageFile == null || uploadImageFile.isEmpty()) {
        	final String message = this.getRequiredFieldMessage("image", "Image required");
        	
        	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update.schedule");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
	        return "redirect:" + this.getBaseURL() + "/current-page";
        }
        
        // ====> Process upload image file
        final Path uploadImageFilePath = this.processUploadImageFile(uploadImageFile, result);
        if (uploadImageFilePath == null) {
        	final FieldError fieldError = result.getFieldError();
        	final String message = fieldError == null 
        			? this.getInvalidFieldMessage("image", "Image is invalid")
        			: this.getMessage(fieldError.getCode(), fieldError.getDefaultMessage());
        	
        	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update.schedule");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
	        return "redirect:" + this.getBaseURL() + "/current-page";
        }
        
        // ====> Create new image
        try {
        	final String uploadImageFileName = uploadImageFilePath.getFileName().toString();
        	
	        final SlideshowImage newImage = new SlideshowImage();
	        newImage.setSlideshow(domain);
	        newImage.setImage(uploadImageFileName);
	        newImage.setLink(dto.getLink());
	        newImage.setIndex(this.mainService.getMaxImageIndex(id) + 1);
	        
	        final SlideshowImage savedImage = this.mainService.createImage(newImage);
	        
	        // ====> Error
	        if (savedImage == null || result.hasErrors()) {
	        	this.deleteUploadFile(uploadImageFilePath);
	        	
	        	final FieldError fieldError = result.getFieldError();
            	final String message = fieldError == null 
            			? this.getProcessingError()
            			: this.getMessage(fieldError.getCode(), fieldError.getDefaultMessage());
            	
            	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update.schedule");
    	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
    	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
    	        
    	        return "redirect:" + this.getBaseURL() + "/current-page";
	        }
	        
	        // ====> Success
	        redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update.main");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "success");
	        
	        return "redirect:" + this.getBaseURL() + "/current-page#schedule-link-" + id;
        } catch (Exception ex) {
        	this.deleteUploadFile(uploadImageFilePath);
        	
        	throw ex;
		}
    }
	
	@PostMapping("/update-item/{id}/{imageId}")
	@Transactional
    public String updateSlideshowItem(
    		@PathVariable("id") Integer id,
    		@PathVariable("imageId") Integer imageId, 
    		@ModelAttribute("slideShowItemDto") @Validated SlideshowDto dto,
    		HttpSession session, HttpServletRequest request, HttpServletResponse response, 
    		BindingResult result, RedirectAttributes redirectAttributes, Model model) {
		
        if (result.hasErrors()) {
        	final FieldError fieldError = result.getFieldError();
        	final String message = this.getMessage(fieldError.getCode(), fieldError.getDefaultMessage());
        	
        	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update.schedule");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
	        return "redirect:" + this.getBaseURL() + "/current-page#schedule-link-" + id;
        }
        
        final SlideshowImage domain = this.mainService.getImage(imageId);
        if (domain == null) {
        	final String message = this.getIdNotFoundMessage(imageId);
        	
        	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update.schedule");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	        
	        return "redirect:" + this.getBaseURL() + "/current-page#schedule-link-" + id;
        }
        
        // ====> Validate image file
        Path uploadImageFilePath = null;
        
        final MultipartFile uploadImageFile = dto.getUploadImageFile();
        if (uploadImageFile != null && !uploadImageFile.isEmpty()) {
	        // ====> Process upload image file
	        uploadImageFilePath = this.processUploadImageFile(uploadImageFile, result);
	        if (uploadImageFilePath == null) {
	        	final FieldError fieldError = result.getFieldError();
	        	final String message = fieldError == null 
	        			? this.getInvalidFieldMessage("image", "Image is invalid")
	        			: this.getMessage(fieldError.getCode(), fieldError.getDefaultMessage());
	        	
	        	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update.schedule");
		        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
		        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
		        
		        return "redirect:" + this.getBaseURL() + "/current-page#schedule-link-" + id;
	        }
        }
        
        // ====> Create new image
        try {
        	final String deleteImageFileName = domain.getImage();
        	
        	domain.setLink(dto.getLink());
        	
        	if (uploadImageFilePath != null) {
        		final String uploadImageFileName = uploadImageFilePath.getFileName().toString();
        		domain.setImage(uploadImageFileName);
        	}
	        
	        final SlideshowImage savedImage = this.mainService.updateImage(domain);
	        
	        // ====> Error
	        if (savedImage == null || result.hasErrors()) {
	        	this.deleteUploadFile(uploadImageFilePath);
	        	
	        	final FieldError fieldError = result.getFieldError();
            	final String message = fieldError == null 
            			? this.getProcessingError()
            			: this.getMessage(fieldError.getCode(), fieldError.getDefaultMessage());
            	
            	redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update.schedule");
    	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
    	        redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
    	        
    	        return "redirect:" + this.getBaseURL() + "/current-page#schedule-link-" + id;
	        }
	        
	        if (uploadImageFilePath != null) {
	        	this.deleteUploadFile(deleteImageFileName);
	        }
	        
	        // ====> Success
	        redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update.main");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "success");
	        
	        return "redirect:" + this.getBaseURL() + "/current-page#schedule-link-" + id;
        } catch (Exception ex) {
        	this.deleteUploadFile(uploadImageFilePath);
        	
        	throw ex;
		}
    }
	
	@PostMapping(value = { "/delete-image/{type}/{imageId}" })
	@Transactional
    public String deletePage(
    		@PathVariable(name = "type", required = true) String slideshowType,
    		@PathVariable(name = "imageId", required = true) Integer imageId,
    		HttpSession session, HttpServletRequest request, HttpServletResponse response, 
    		RedirectAttributes redirectAttributes, Model model) {
		
		final SlideshowImage domain = this.mainService.getImage(imageId);
		if (domain == null) {
			redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "delete." + slideshowType + ".image");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
	        
			return "redirect:" + this.getBaseURL() + "/current-page";
		}
		
		final String deleteImageFileName = domain.getImage();
		
		final boolean result = this.mainService.deleteImage(imageId);
		if (result) {
			// ====> Success, delete old image file
			this.deleteUploadFile(deleteImageFileName);
			
			try {
				final Slideshow slideshow = domain.getSlideshow();
				
				/*
				if (slideshow.getStartDate() != null 
						&& this.mainService.countImage(slideshow.getId()) == 0) {
					this.mainService.delete(slideshow.getId());
				}
				*/
				
				redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "delete." + slideshowType + ".image");
		        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "success");
				
		        return "redirect:" + this.getBaseURL() + "/current-page#schedule-link-" + slideshow.getId();
			} catch (Exception ex) {
				this.logger.error("Cannot delete slideshow", ex);
			}
			
			redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "delete." + slideshowType + ".image");
	        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "success");
			
	        return "redirect:" + this.getBaseURL() + "/current-page";
		}
		
		redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "delete." + slideshowType + ".image");
        redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
        
		return "redirect:" + this.getBaseURL() + "/current-page";
	}
}
