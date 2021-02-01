package fr.be.your.self.backend.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.opencsv.bean.MappingStrategy;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import fr.be.your.self.backend.dto.PermissionDto;
import fr.be.your.self.backend.dto.UserDto;
import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.backend.setting.DataSetting;
import fr.be.your.self.common.ErrorStatusCode;
import fr.be.your.self.common.UserPermission;
import fr.be.your.self.dto.PageableResponse;
import fr.be.your.self.engine.EmailSender;
import fr.be.your.self.exception.BusinessException;
import fr.be.your.self.model.Functionality;
import fr.be.your.self.model.PO;
import fr.be.your.self.model.Permission;
import fr.be.your.self.model.User;
import fr.be.your.self.service.BaseService;
import fr.be.your.self.service.BusinessCodeService;
import fr.be.your.self.service.FunctionalityService;
import fr.be.your.self.service.PermissionService;
import fr.be.your.self.util.StringUtils;

public abstract class BaseResourceController<T extends PO<K>, SimpleDto, DetailDto, K extends Serializable>
		extends BaseController {
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private EmailSender emailSender;
	
	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	FunctionalityService functionalityService;
	
	@Autowired
	DataSetting dataSetting;
	
	@Autowired
	BusinessCodeService codeService;

	private static final String ACCESS_DENIED_URL = Constants.PATH.ACCESS_DENIED;

	protected static final String TOAST_ACTION_KEY = "toastAction";
	protected static final String TOAST_STATUS_KEY = "toastStatus";
	protected static final String TOAST_TITLE_KEY = "toastTitle";
	protected static final String TOAST_MESSAGE_KEY = "toastMessage";

	protected Logger logger;

	private Class<T> domainClazz;
	private Class<SimpleDto> simpleDtoClazz;
	private Class<DetailDto> detailDtoClazz;

	@SuppressWarnings("unchecked")
	public BaseResourceController() {
		super();

		Class<?> superClazz = this.getClass();
		Type genericType = superClazz.getGenericSuperclass();
		while (!(genericType instanceof ParameterizedType)) {
			superClazz = superClazz.getSuperclass();
			genericType = superClazz.getGenericSuperclass();

			if (superClazz.equals(BaseResourceController.class)) {
				break;
			}
		}

		if (genericType instanceof Class) {
			throw new RuntimeException();
		}

		final ParameterizedType parameterizedType = (ParameterizedType) genericType;
		this.domainClazz = ((Class<T>) parameterizedType.getActualTypeArguments()[0]);
		this.simpleDtoClazz = ((Class<SimpleDto>) parameterizedType.getActualTypeArguments()[1]);
		this.detailDtoClazz = ((Class<DetailDto>) parameterizedType.getActualTypeArguments()[2]);

		this.logger = LoggerFactory.getLogger(this.getClass());
	}

	protected abstract BaseService<T, K> getService();

	protected abstract String getName();

	protected abstract String getDefaultPageTitle(String baseMessageKey);

	protected abstract String getUploadDirectoryName();

	protected abstract T newDomain();

	protected abstract DetailDto createDetailDto(T domain);

	protected abstract SimpleDto createSimpleDto(T domain);

	protected abstract Map<String, String[]> getSortableColumns();

	protected String getBaseMediaURL() {
		return null;
	}

	protected void loadListPageOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, Map<String, String> searchParams, PageableResponse<SimpleDto> pageableDto)
			throws BusinessException {
	}

	protected void loadDetailFormOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, T domain, DetailDto dto) throws BusinessException {
		
	}

	protected void loadPersonTitles(Model model){
		String titleValues = this.getMessage("title.values");
		String[] titlesVal = titleValues.split(",");
		List<String> titles = Arrays.asList(titlesVal);
		model.addAttribute("titles", titles);	
	}
	
	
	protected boolean isEditableDomain(Model model, T domain) {
		return domain != null;
	}

	protected boolean isViewableDomain(Model model, T domain) {
		return domain != null;
	}

	protected UserPermission getGlobalPermission(Model model, PermissionDto permission) {
		final String pageName = this.getName();
		if (pageName == AccountController.NAME) { //Everybody has access to his/her settings
			return UserPermission.WRITE;
		}
		if (permission.hasWritePermission(pageName)) {
			return UserPermission.WRITE;
		}

		if (permission.hasPermission(pageName)) {
			return UserPermission.READONLY;
		}

		return UserPermission.DENIED;
	}

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		final String dateFormat = this.getMessage("locale.format.date", "dd-MM-yyyy");
	    final SimpleDateFormat df = new SimpleDateFormat(dateFormat);
	    
	    binder.registerCustomEditor(Date.class, new CustomDateEditor(df, true));
	}
	
	@Override
	public void initAttributes(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, PermissionDto permission) {

		final UserPermission globalPermission = this.getGlobalPermission(model, permission);
		final boolean writePermission = UserPermission.WRITE == globalPermission;
		final boolean readPermission = writePermission || UserPermission.READONLY == globalPermission;

		if (!readPermission) {
			try {
				response.sendRedirect(ACCESS_DENIED_URL);

				return;
			} catch (IOException e) {
			}
		}

		final String pageName = this.getName();
		final String baseMessageKey = pageName.replace('-', '.');

		model.addAttribute("writePermission", writePermission);
		model.addAttribute("readPermission", readPermission);

		model.addAttribute("pageName", pageName);
		model.addAttribute("baseMessageKey", baseMessageKey);
		model.addAttribute("pageTitle", this.getDefaultPageTitle(baseMessageKey));
		model.addAttribute("rootURL", this.getRootURL());
		model.addAttribute("baseURL", this.getBaseURL());

		final String baseImageURL = this.getBaseMediaURL();
		if (!StringUtils.isNullOrSpace(baseImageURL)) {
			model.addAttribute("baseMediaURL", baseImageURL);
		}

		final String dateFormat = this.getMessage("locale.format.date", "dd-MM-yyyy");
		final String dateTimeFormat = this.getMessage("locale.format.date.time", "dd-MM-yyyy HH:mm");
		final String GOOGLE_MAP_API_KEY = this.dataSetting.getGoogleMapApiKey();

		model.addAttribute("dateFormat", dateFormat);
		model.addAttribute("dateTimeFormat", dateTimeFormat);
		model.addAttribute("GOOGLE_MAP_API_KEY", GOOGLE_MAP_API_KEY);

	}

	@GetMapping(value = { "", "/search" })
	public String searchPage(HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "size", required = false) Integer size,
			@RequestParam(name = "sort", required = false) String sortQuery,
			@RequestParam(name = "action", required = false) String action,
			@RequestParam Map<String, String> searchParams) {

		if ("update-filter".equalsIgnoreCase(action)) {
			Map<String, String> currentSearchParams = this.getSearchSessionValue(session);

			for (Entry<String, String> currentSearchItem : currentSearchParams.entrySet()) {
				if (!searchParams.containsKey(currentSearchItem.getKey())) {
					searchParams.put(currentSearchItem.getKey(), currentSearchItem.getValue());
				}
			}

			if (page == null || page <= 0) {
				page = (Integer) session.getAttribute(this.getPageIndexSessionKey());
			}

			if (size == null) {
				size = (Integer) session.getAttribute(this.getPageSizeSessionKey());
			}

			if (StringUtils.isNullOrSpace(sortQuery)) {
				sortQuery = (String) session.getAttribute(this.getSortSessionKey());
			}
		}

		if (page == null || page <= 0) {
			page = 1;
		}

		if (size == null) {
			size = this.dataSetting.getDefaultPageSize();
		}

		if (StringUtils.isNullOrSpace(sortQuery)) {
			sortQuery = this.getService().getDefaultSort();
		}

		final Sort sort = this.getSortRequest(sortQuery);
		final PageRequest pageable = this.getPageRequest(page, size, sort);

		// Save session
		session.setAttribute(this.getSearchSessionKey(), searchParams);
		session.setAttribute(this.getSortSessionKey(), sortQuery);

		if (pageable != null) {
			session.setAttribute(this.getPageIndexSessionKey(), pageable.getPageNumber() + 1);
			session.setAttribute(this.getPageSizeSessionKey(), pageable.getPageSize());
		}

		return this.listPage(session, request, response, model, searchParams, sortQuery, pageable, sort);
	}

	@GetMapping(value = { "/page" })
	public String changePageNormal(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, @RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "size", required = false) Integer size) {

		Map<String, String> searchParams = this.getSearchSessionValue(session);
		String sortQuery = (String) session.getAttribute(this.getSortSessionKey());

		if (page == null || page <= 0) {
			page = (Integer) session.getAttribute(this.getPageIndexSessionKey());
		}

		if (size == null || size <= 0) {
			size = (Integer) session.getAttribute(this.getPageSizeSessionKey());
		}

		if (StringUtils.isNullOrSpace(sortQuery)) {
			sortQuery = this.getService().getDefaultSort();
		}

		final Sort sort = this.getSortRequest(sortQuery);
		final PageRequest pageable = this.getPageRequest(page, size, sort);

		// Save session
		if (pageable != null) {
			session.setAttribute(this.getPageIndexSessionKey(), pageable.getPageNumber() + 1);
		}

		return this.listPage(session, request, response, model, searchParams, sortQuery, pageable, sort);
	}

	@GetMapping(value = { "/page/{page}" })
	public String changePage(HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model,
			@PathVariable(name = "page", required = true) Integer page) {
		return this.changePageNormal(session, request, response, model, page, null);
	}

	@GetMapping(value = { "/current-page" })
	public String currentPage(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, @RequestParam(name = "sort", required = false) String sortQuery) {

		Map<String, String> searchParams = this.getSearchSessionValue(session);

		Integer size = (Integer) session.getAttribute(this.getPageSizeSessionKey());
		Integer page = (Integer) session.getAttribute(this.getPageIndexSessionKey());

		if (StringUtils.isNullOrSpace(sortQuery)) {
			sortQuery = (String) session.getAttribute(this.getSortSessionKey());

			if (StringUtils.isNullOrSpace(sortQuery)) {
				sortQuery = this.getService().getDefaultSort();
			}
		}

		final Sort sort = this.getSortRequest(sortQuery);
		final PageRequest pageable = this.getPageRequest(page, size, sort);

		// Save session
		session.setAttribute(this.getSortSessionKey(), sortQuery);

		return this.listPage(session, request, response, model, searchParams, sortQuery, pageable, sort);
	}

	@SuppressWarnings("unchecked")
	private String listPage(HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model,
			Map<String, String> searchParams, String sortQuery, PageRequest pageable, Sort sort) {

		final PageableResponse<T> domainPage = this.pageableSearch(searchParams, pageable, sort);
		if (domainPage == null) {
			throw new BusinessException(ErrorStatusCode.PROCESSING_ERROR);
		}

		final PageableResponse<SimpleDto> result;
		if (this.domainClazz == this.simpleDtoClazz) {
			result = (PageableResponse<SimpleDto>) domainPage;
		} else {
			result = new PageableResponse<>();
			result.setPageIndex(domainPage.getPageIndex());
			result.setPageSize(domainPage.getPageSize());
			result.setTotalItems(domainPage.getTotalItems());
			result.setTotalPages(domainPage.getTotalPages());

			for (T domain : domainPage.getItems()) {
				final SimpleDto dto = this.createSimpleDto(domain);

				if (dto != null) {
					result.addItem(dto);
				}
			}
		}

		this.loadListPageOptions(session, request, response, model, searchParams, result);

		// Store properties
		final String baseMessageKey = this.getName().replace('-', '.');
		final String titleKey = baseMessageKey + ".page.title";
		model.addAttribute("formTitle", this.getMessage(titleKey));
		
		String search = searchParams != null ? searchParams.get("q") : null;
		model.addAttribute("search", search == null ? "" : search);
		model.addAttribute("sort", sortQuery == null ? "" : sortQuery);

		if (pageable != null) {
			model.addAttribute("page", pageable.getPageNumber() + 1);
			model.addAttribute("size", pageable.getPageSize());
		}

		final String pageSizeNameKey = baseMessageKey + ".option.display.page.size.name";
		model.addAttribute("pageSizeName", this.getMessage(pageSizeNameKey));

		final Set<Integer> supportPageSizes = this.dataSetting.getSupportPageSizes();
		model.addAttribute("supportPageSizes", supportPageSizes);

		// Store result
		model.addAttribute("result", result);

		return this.getListView();
	}

	protected PageableResponse<T> pageableSearch(Map<String, String> searchParams, PageRequest pageable, Sort sort) {
		String search = "";
		if (searchParams != null ) {
			search = searchParams.get("q");
		}
		return this.getService().pageableSearch(search, pageable, sort);
	}

	@GetMapping(value = { "/create" })
	public String addNewPage(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			RedirectAttributes redirectAttributes, Model model) {
		return this.redirectAddNewPage(session, request, response, redirectAttributes, model, null);
	}

	protected String redirectAddNewPage(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			RedirectAttributes redirectAttributes, Model model, DetailDto dto) {
		if (dto == null) {
			dto = this.createDetailDto(null);

			if (dto == null) {
				redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "create");
				redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");

				return "redirect:" + this.getBaseURL() + "/current-page";
			}
		}

		try {
			this.loadDetailFormOptions(session, request, response, model, null, dto);
		} catch (BusinessException ex) {
			this.logger.error("Business error", ex);

			redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "create");
			redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");

			return "redirect:" + this.getBaseURL() + "/current-page";
		} catch (Exception ex) {
			this.logger.error("Process error", ex);

			redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "create");
			redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");

			return "redirect:" + this.getBaseURL() + "/current-page";
		}

		// Store result
		final String titleKey = this.getName().replace('-', '.') + ".create.page.title";
		model.addAttribute("formTitle", this.getMessage(titleKey));

		model.addAttribute("action", "create");
		model.addAttribute("dto", dto);

		return this.getFormView();
	}

	@GetMapping(value = { "/edit/{id}" })
	public String editPage(@PathVariable(name = "id", required = true) K id, HttpSession session,
			HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes,
			Model model) {
		return this.redirectEditPage(session, request, response, redirectAttributes, model, id, null);
	}

	@SuppressWarnings("unchecked")
	protected String redirectEditPage(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			RedirectAttributes redirectAttributes, Model model, K id, DetailDto dto) {

		T domain = null;
		if (this.domainClazz == this.detailDtoClazz) {
			domain = (T) dto;
		}

		if (domain == null) {
			domain = this.getService().getById(id);

			if (!this.isEditableDomain(model, domain)) {
				final String message = this.getIdNotFoundMessage(id);

				redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "edit");
				redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
				redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);

				return "redirect:" + this.getBaseURL() + "/current-page";
			}
		}

		if (dto == null) {
			dto = this.createDetailDto(domain);

			if (dto == null) {
				redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "edit");
				redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");

				return "redirect:" + this.getBaseURL() + "/current-page";
			}
		}

		try {
			this.loadDetailFormOptions(session, request, response, model, domain, dto);
		} catch (BusinessException ex) {
			this.logger.error("Business error", ex);

			redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "edit");
			redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");

			return "redirect:" + this.getBaseURL() + "/current-page";
		} catch (Exception ex) {
			this.logger.error("Process error", ex);

			redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "edit");
			redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");

			return "redirect:" + this.getBaseURL() + "/current-page";
		}

		// Store result
		final String titleKey = this.getName().replace('-', '.') + ".edit.page.title";
		final Object[] titleParams = new Object[] { domain.getDisplay() };
		model.addAttribute("formTitle", this.getMessage(titleKey, titleParams));

		model.addAttribute("action", "update/" + id);
		model.addAttribute("dto", dto);

		return this.getFormView();
	}

	@RequestMapping(value = { "/view/{id}" }, method = { RequestMethod.GET, RequestMethod.POST })
	public String viewPage(@PathVariable(name = "id", required = true) K id, HttpSession session,
			HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes,
			Model model) {

		final T domain = this.getService().getById(id);
		if (!this.isViewableDomain(model, domain)) {
			final String message = this.getIdNotFoundMessage(id);

			redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "view");
			redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
			redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);

			return "redirect:" + this.getBaseURL() + "/current-page";
		}

		final DetailDto dto = this.createDetailDto(domain);
		if (dto == null) {
			redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "view");
			redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");

			return "redirect:" + this.getBaseURL() + "/current-page";
		}

		try {
			this.loadDetailFormOptions(session, request, response, model, domain, dto);
		} catch (BusinessException ex) {
			this.logger.error("Business error", ex);

			redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "view");
			redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");

			return "redirect:" + this.getBaseURL() + "/current-page";
		} catch (Exception ex) {
			this.logger.error("Process error", ex);

			redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "view");
			redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");

			return "redirect:" + this.getBaseURL() + "/current-page";
		}

		// Store result
		final String titleKey = this.getName().replace('-', '.') + ".view.page.title";
		final Object[] titleParams = new Object[] { domain.getDisplay() };
		model.addAttribute("formTitle", this.getMessage(titleKey, titleParams));

		model.addAttribute("action", "view/" + id);
		model.addAttribute("dto", dto);

		model.addAttribute("writePermission", false);

		return this.getFormView();
	}

	/*
	 * public final User getCurrentUser(HttpSession session, Model model) { Object
	 * accessToken = session.getAttribute(SESSION_KEY_ACCESS_TOKEN);
	 * 
	 * if (accessToken != null) { final Object currentUser =
	 * model.getAttribute("currentUser");
	 * 
	 * if (currentUser != null && currentUser instanceof User) { return (User)
	 * currentUser; }
	 * 
	 * User authenticationUser =
	 * userRepository.findByAccessToken(accessToken.toString()); if
	 * (authenticationUser != null) { model.addAttribute("currentUser",
	 * authenticationUser); }
	 * 
	 * return authenticationUser; }
	 * 
	 * return null; }
	 */

	/**************************************************************************************/
	/*********************
	 * OPTION PROPERTIES
	 **********************************************/
	/**************************************************************************************/
	protected final String getRootURL() {
		return Constants.PATH.WEB_ADMIN_PREFIX;
	}

	protected final String getBaseURL() {
		return Constants.PATH.WEB_ADMIN_PREFIX + "/" + this.getName();
	}

	protected String getListView() {
		return "pages/" + this.getName() + "-list";
	}

	protected String getFormView() {
		return "pages/" + this.getName() + "-form";
	}

	protected final String getSearchSessionKey() {
		return this.getName() + "_SEARCH";
	}

	protected final String getSortSessionKey() {
		return this.getName() + "_SORT";
	}

	protected final String getPageIndexSessionKey() {
		return this.getName() + "_PAGE_INDEX";
	}

	protected final String getPageSizeSessionKey() {
		return this.getName() + "_PASE_SIZE";
	}

	@SuppressWarnings("unchecked")
	protected Map<String, String> getSearchSessionValue(HttpSession session) {
		try {
			return (Map<String, String>) session.getAttribute(this.getSearchSessionKey());
		} catch (Exception ex) {
		}

		return Collections.emptyMap();
	}

	protected final Sort getSortRequest(String sortQuery) {
		if (StringUtils.isNullOrSpace(sortQuery)) {
			return Sort.unsorted();
		}
		
		final List<Order> orders = new ArrayList<Order>();
		final String[] sortProperties = sortQuery.split(";");
		final Map<String, String[]> sortableColumns = this.getSortableColumns();
		
		for (String sortProperty : sortProperties) {
			final String[] sortValues = sortProperty.split("\\|");
			if (sortValues.length != 2) {
				continue;
			}

			final String[] sortColumns = sortableColumns.get(sortValues[0]);
			if (sortColumns != null) {
				final Optional<Direction> direction = Direction.fromOptionalString(sortValues[1]);
				final Direction sortDirection = direction.isPresent() ? direction.get() : Direction.ASC;
				
				for (String sortColumn : sortColumns) {
					orders.add(new Order(sortDirection, sortColumn));	
				}
			}
		}

		return Sort.by(orders);
	}

	protected final PageRequest getPageRequest(Integer page, Integer size, Sort sort) {
		if (page == null || page < 1) {
			page = 1;
		}

		if (size == null || size < 1) {
			return null;
			// size = this.dataSetting.getDefaultPageSize();
		}

		return PageRequest.of(page - 1, size, sort);
	}

	/**************************************************************************************/
	/*********************
	 * CREATE FORM ERROR MESSAGE
	 **************************************/
	/**************************************************************************************/
	protected final ObjectError createFieldError(BindingResult result, String fieldName, String errorMessageCode,
			Object[] messageArguments, String defaultMessage) {
		final String baseMessageKey = this.getName().replace('-', '.');

		final List<String> fieldNameMessageCodes = Arrays.asList(fieldName.split("(?=\\p{Lu})"));
		fieldNameMessageCodes.replaceAll(String::toLowerCase);

		final String fieldNameMessageCode = String.join(".", fieldNameMessageCodes);
		final String messageCode = baseMessageKey + ".error." + fieldNameMessageCode + "." + errorMessageCode;

		return new FieldError(result.getObjectName(), fieldName, null, false, new String[] { messageCode },
				messageArguments, defaultMessage);
	}

	protected final ObjectError createFieldError(BindingResult result, String fieldName, String errorMessageCode,
			String defaultMessage) {
		return this.createFieldError(result, fieldName, errorMessageCode, null, defaultMessage);
	}

	protected final ObjectError createFieldError(BindingResult result, String fieldName, String messageCode) {
		return createFieldError(result, fieldName, messageCode, null, "");
	}

	protected final ObjectError createRequiredFieldError(BindingResult result, String fieldName,
			String defaultMessage) {
		return this.createFieldError(result, fieldName, "required", null, defaultMessage);
	}

	protected final ObjectError createInvalidImageError(BindingResult result, String fileContentType, String fieldName,
			String defaultMessage) {
		final String supportImageExtensions = String.join(", ", this.dataSetting.getImageFileExtensions());
		final Object[] messageArguments = new String[] { supportImageExtensions };

		return this.createFieldError(result, fieldName, "invalid", messageArguments, defaultMessage);
	}

	protected final ObjectError createTooLargeImageError(BindingResult result, long fileSize, String fieldName,
			String defaultMessage) {
		final String formatFileSize = StringUtils.formatFileSize(this.dataSetting.getImageMaxFileSize());
		final Object[] messageArguments = new Object[] { formatFileSize };

		return this.createFieldError(result, fieldName, "too.large", messageArguments, defaultMessage);
	}

	protected final ObjectError createInvalidMediaError(BindingResult result, String fileContentType, String fieldName,
			String defaultMessage) {
		final String supportMediaExtensions = String.join(", ", this.dataSetting.getMediaFileExtensions());
		final Object[] messageArguments = new String[] { supportMediaExtensions };

		return this.createFieldError(result, fieldName, "invalid", messageArguments, defaultMessage);
	}

	protected final ObjectError createTooLargeMediaError(BindingResult result, long fileSize, String fieldName,
			String defaultMessage) {
		final String formatFileSize = StringUtils.formatFileSize(this.dataSetting.getMediaMaxFileSize());
		final Object[] messageArguments = new Object[] { formatFileSize };

		return this.createFieldError(result, fieldName, "too.large", messageArguments, defaultMessage);
	}

	protected final ObjectError createIdNotFoundError(BindingResult result, K id) {
		final Object[] messageArguments = id == null ? null : new Object[] { id };

		return new ObjectError(result.getObjectName(), new String[] { "error.id.not.found" }, messageArguments,
				"Not found");
	}

	protected final ObjectError createProcessingError(BindingResult result) {
		return new ObjectError(result.getObjectName(), new String[] { "error.processing" }, null, "Processing error");
	}

	/**************************************************************************************/
	/********************** MESSAGE METHODS ***********************************************/
	/**************************************************************************************/
	protected final String getFieldErrorMessage(String fieldName, String errorMessageCode,
			Object[] messageArguments, String defaultMessage) {
		final String baseMessageKey = this.getName().replace('-', '.');

		final List<String> fieldNameMessageCodes = Arrays.asList(fieldName.split("(?=\\p{Lu})"));
		fieldNameMessageCodes.replaceAll(String::toLowerCase);

		final String fieldNameMessageCode = String.join(".", fieldNameMessageCodes);
		final String messageCode = baseMessageKey + ".error." + fieldNameMessageCode + "." + errorMessageCode;
		
		return this.getMessage(messageCode, messageArguments, defaultMessage);
	}
	
	protected final String getRequiredFieldMessage(String fieldName, String defaultMessage) {
		return this.getFieldErrorMessage(fieldName, "required", null, defaultMessage);
	}
	
	protected final String getInvalidFieldMessage(String fieldName, String defaultMessage) {
		return this.getFieldErrorMessage(fieldName, "invalid", null, defaultMessage);
	}
	
	protected final String getProcessingError() {
		return this.getMessage("error.processing", "Processing error");
	}
	
	protected final String getIdNotFoundMessage(K idValue) {
		final String baseMessageKey = this.getName().replace('-', '.');
		final String dataName = this.getMessage(baseMessageKey + ".name");

		final String[] messageParams = new String[] { dataName, idValue == null ? "" : idValue.toString() };
		final String message = this.getMessage("error.message.id.not.found", messageParams, null);

		if (message.startsWith(dataName)) {
			return StringUtils.upperCaseFirst(message);
		}

		return message;
	}

	protected final String getDeleteByIdErrorMessage(K idValue) {
		final String baseMessageKey = this.getName().replace('-', '.');
		final String dataName = this.getMessage(baseMessageKey + ".name");

		final String[] messageParams = new String[] { dataName, idValue == null ? "" : idValue.toString() };
		final String message = this.getMessage("error.message.cannot.delete.id", messageParams, null);

		if (message.startsWith(dataName)) {
			return StringUtils.upperCaseFirst(message);
		}

		return message;
	}

	protected final String getDeleteSuccessMessage(K idValue) {
		final String baseMessageKey = this.getName().replace('-', '.');
		final String dataName = this.getMessage(baseMessageKey + ".name");

		final String[] messageParams = new String[] { dataName, idValue == null ? "" : idValue.toString() };
		final String message = this.getMessage("success.message.delete", messageParams, null);

		if (message.startsWith(dataName)) {
			return StringUtils.upperCaseFirst(message);
		}

		return message;
	}

	/**************************************************************************************/
	/********************** PROCESS UPLOAD FILES ******************************************/
	/**************************************************************************************/
	protected final Path processUploadImageFile(final MultipartFile uploadImageFile, BindingResult result) {
		final String uploadFileName = uploadImageFile.getOriginalFilename();
		final long uploadFileSize = uploadImageFile.getSize();

		if (!this.isValidImageFileSize(uploadFileSize)) {
			final ObjectError error = this.createTooLargeImageError(result, uploadFileSize, "image", "Image is too large");
			result.addError(error);

			this.logger.error("Image file {0} with size {1} is too large!", uploadFileName, uploadFileSize);
			return null;
		}

		final Path uploadImageFilePath = this.uploadFile(uploadImageFile);
		if (uploadImageFilePath == null) {
			final ObjectError error = this.createProcessingError(result);
			result.addError(error);

			return null;
		}

		final String uploadContentType = this.getFileContentType(uploadImageFilePath,
				this.dataSetting.getImageMimeTypes());
		if (StringUtils.isNullOrSpace(uploadContentType) || !this.isValidImageContentType(uploadContentType)) {
			this.deleteUploadFile(uploadImageFilePath);

			final ObjectError error = this.createInvalidImageError(result, uploadContentType, "image",
					"Image is invalid");
			result.addError(error);

			this.logger.error("Image file {0} with content type {1} is invalid!", uploadFileName, uploadContentType);
			return null;
		}

		return uploadImageFilePath;
	}

	protected Path processUploadContentFile(final MultipartFile uploadContentFile, BindingResult result) {
		final String uploadFileName = uploadContentFile.getOriginalFilename();
		final long uploadFileSize = uploadContentFile.getSize();

		if (!this.isValidMediaFileSize(uploadFileSize)) {
			final ObjectError error = this.createTooLargeImageError(result, uploadFileSize, "contentFile",
					"Content file is too large");
			result.addError(error);

			this.logger.error("Content file {0} with size {1} is too large!", uploadFileName, uploadFileSize);
			return null;
		}

		final Path uploadContentFilePath = this.uploadFile(uploadContentFile);
		if (uploadContentFilePath == null) {
			final ObjectError error = this.createProcessingError(result);
			result.addError(error);

			return null;
		}

		final String uploadContentType = this.getFileContentType(uploadContentFilePath,
				this.dataSetting.getMediaMimeTypes());
		if (StringUtils.isNullOrSpace(uploadContentType)) {
			this.deleteUploadFile(uploadContentFilePath);

			final ObjectError error = this.createInvalidMediaError(result, uploadContentType, "contentFile",
					"Content file is invalid");
			result.addError(error);

			this.logger.error("Content file {0} with content type {1} is invalid!", uploadFileName, uploadContentType);
			return null;
		}

		// Validate content type and file size
		if (this.isValidAudioContentType(uploadContentType)) {
			if (!this.isValidAudioFileSize(uploadFileSize)) {
				this.deleteUploadFile(uploadContentFilePath);

				final ObjectError error = this.createTooLargeImageError(result, uploadFileSize, "contentFile",
						"Content audio file is too large");
				result.addError(error);

				this.logger.error("Content audio file {0} with size {1} is too large!", uploadFileName, uploadFileSize);
				return null;
			}
		} else if (this.isValidVideoContentType(uploadContentType)) {
			if (!this.isValidVideoFileSize(uploadFileSize)) {
				this.deleteUploadFile(uploadContentFilePath);

				final ObjectError error = this.createTooLargeImageError(result, uploadFileSize, "contentFile",
						"Content video file is too large");
				result.addError(error);

				this.logger.error("Content video file {0} with size {1} is too large!", uploadFileName, uploadFileSize);
				return null;
			}
		} else {
			this.deleteUploadFile(uploadContentFilePath);

			final ObjectError error = this.createInvalidMediaError(result, uploadContentType, "contentFile",
					"Content file is invalid");
			result.addError(error);

			this.logger.error("Content file {0} with content type {1} is invalid!", uploadFileName, uploadContentType);
			return null;
		}

		return uploadContentFilePath;
	}

	protected Path uploadFile(final MultipartFile mediaFile) {
		return uploadFile(mediaFile, false);
	}
	protected Path uploadFile(final MultipartFile mediaFile, boolean withOriginalName) {
		final String uploadDirectoryName = this.getUploadDirectoryName();
		final String fileName = mediaFile.getOriginalFilename();
		final int dotIndex = fileName.lastIndexOf(".");
		final String fileExtension = dotIndex > 0 ? fileName.substring(dotIndex) : ".png";
		final String fileNameWithoutExtension = fileName.substring(0, dotIndex);
		try {
			final File directory = new File(uploadDirectoryName);
			if (!directory.exists()) {
				directory.mkdirs();
			}
		} catch (Exception ex) {
			this.logger.error("Cannot create media directory", ex);

			return null;
		}

		final String uploadFileName;
		
		if (!withOriginalName) {
			uploadFileName = UUID.randomUUID().toString() + fileExtension;
		} else {
			uploadFileName = fileNameWithoutExtension + "_" + Instant.now().getEpochSecond() + fileExtension;
		}
		final Path uploadFilePath = Paths.get(uploadDirectoryName + "/" + uploadFileName);

		try {
			final byte[] mediaBytes = mediaFile.getBytes();
			Files.write(uploadFilePath, mediaBytes);
		} catch (Exception ex) {
			this.logger.error("Cannot write media data", ex);

			return null;
		}

		return uploadFilePath;
	}

	protected boolean deleteUploadFile(String deleteFileName) {
		if (StringUtils.isNullOrSpace(deleteFileName)) {
			return false;
		}

		final Path mediaFilePath = Paths.get(this.getUploadDirectoryName() + "/" + deleteFileName);

		return deleteUploadFile(mediaFilePath);
	}

	protected boolean deleteUploadFile(final Path filePath) {
		if (filePath == null) {
			return false;
		}

		try {
			Files.delete(filePath);
			return true;
		} catch (IOException ex) {
			this.logger.error("Cannot delete media file", ex);
		}

		return false;
	}

	protected String getFileContentType(final Path filePath, Set<String> validContentTypes) {
		try {
			final String contentType = Files.probeContentType(filePath);

			if (!StringUtils.isNullOrSpace(contentType) && validContentTypes.contains(contentType)) {
				return contentType.toLowerCase();
			}
		} catch (Exception ex) {
			this.logger.error("Cannot retrieve file mime type", ex);
		}

		try {
			final File file = filePath.toFile();
			final String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file);

			if (!StringUtils.isNullOrSpace(contentType) && validContentTypes.contains(contentType)) {
				return contentType.toLowerCase();
			}
		} catch (Exception ex) {
			this.logger.error("Cannot retrieve file mime type", ex);
		}

		try {
			final String fileAbsolutePath = filePath.toFile().getAbsolutePath();
			final String contentType = URLConnection.guessContentTypeFromName(fileAbsolutePath);

			if (!StringUtils.isNullOrSpace(contentType) && validContentTypes.contains(contentType)) {
				return contentType.toLowerCase();
			}
		} catch (Exception ex) {
			this.logger.error("Cannot retrieve file mime type", ex);
		}

		try {
			final String fileName = filePath.toFile().getName();
			final String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);

			if (!StringUtils.isNullOrSpace(contentType) && validContentTypes.contains(contentType)) {
				return contentType.toLowerCase();
			}
		} catch (Exception ex) {
			this.logger.error("Cannot retrieve file mime type", ex);
		}

		try {
			final String fileName = filePath.toFile().getName();
			final int dotIndex = fileName.lastIndexOf(".");

			if (dotIndex > 0) {
				final String fileExtension = fileName.substring(dotIndex);
				final String mimeType = this.dataSetting.getDefaultMimeTypeMapping(fileExtension);

				if (!StringUtils.isNullOrSpace(mimeType) && validContentTypes.contains(mimeType)) {
					return mimeType.toLowerCase();
				}
			}
		} catch (Exception ex) {
			this.logger.error("Cannot retrieve file mime type", ex);
		}

		return null;
	}

	protected boolean isValidImageContentType(final String contentType) {
		if (StringUtils.isNullOrSpace(contentType)) {
			return false;
		}

		return this.dataSetting.getImageMimeTypes().contains(contentType);
	}

	protected boolean isValidImageFileSize(final long fileSize) {
		final long maxFileSize = this.dataSetting.getImageMaxFileSize();
		return maxFileSize <= 0 || maxFileSize >= fileSize;
	}

	protected boolean isValidAudioContentType(final String contentType) {
		if (StringUtils.isNullOrSpace(contentType)) {
			return false;
		}

		return this.dataSetting.getAudioMimeTypes().contains(contentType);
	}

	protected boolean isValidAudioFileSize(final long fileSize) {
		final long maxFileSize = this.dataSetting.getAudioMaxFileSize();
		return maxFileSize <= 0 || maxFileSize >= fileSize;
	}

	protected boolean isValidVideoContentType(final String contentType) {
		if (StringUtils.isNullOrSpace(contentType)) {
			return false;
		}

		return this.dataSetting.getVideoMimeTypes().contains(contentType);
	}

	protected boolean isValidVideoFileSize(final long fileSize) {
		final long maxFileSize = this.dataSetting.getVideoMaxFileSize();
		return maxFileSize <= 0 || maxFileSize >= fileSize;
	}

	protected boolean isValidMediaContentType(final String contentType) {
		if (StringUtils.isNullOrSpace(contentType)) {
			return false;
		}

		return this.dataSetting.getMediaMimeTypes().contains(contentType);
	}

	protected boolean isValidMediaFileSize(final long fileSize) {
		long maxFileSize = this.dataSetting.getAudioMaxFileSize();
		if (maxFileSize < this.dataSetting.getVideoMaxFileSize()) {
			maxFileSize = this.dataSetting.getVideoMaxFileSize();
		}

		return maxFileSize <= 0 || maxFileSize >= fileSize;
	}

	public PasswordEncoder getPasswordEncoder() {
		return passwordEncoder;
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public EmailSender getEmailSender() {
		return emailSender;
	}

	public void setEmailSender(EmailSender emailSender) {
		this.emailSender = emailSender;
	}
	
	public PermissionService getPermissionService() {
		return permissionService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}
	protected List<Permission> createDefaultPermissions(User user) {
		Iterable<Functionality> functionalities = functionalityService.findAll();
		List<Permission> permissions = new ArrayList<Permission>();

		for (Functionality func : functionalities) {
			Permission permission = new Permission();
			permission.setUser(user);
			permission.setFunctionality(func);
			permission.setUserPermission(UserPermission.DENIED.getValue());
			permissions.add(permission);
		}
		return permissions;
	}
	
	protected void addDefaultPermissions(UserDto userdto) {
		List<Permission> permissions = createDefaultPermissions(new User());
		userdto.setPermissions(permissions);
	}
	
	protected void addDefaultPermissions(User user) {
		List<Permission> permissions = createDefaultPermissions(user);
		user.setPermissions(permissions);
	}
	
	protected boolean sendVerificationEmailToUser(String activateAccountUrl, User savedUser) {

		boolean success = this.getEmailSender().sendActivateUser(savedUser.getEmail(), activateAccountUrl,
				savedUser.getActivateCode());
		return success;
	}

	//generate a new activation code and timeout for a user
	void setActivateCodeAndTimeout(User user) {
		String activateCode = StringUtils.randomAlphanumeric(this.dataSetting.getActivateCodeLength());
		long activateCodeTimeout = (new Date().getTime() / (60 * 1000))
				+ this.dataSetting.getActivateCodeTimeout();

		user.setActivateCode(activateCode);
		user.setActivateTimeout(activateCodeTimeout);
	}
	

	protected void setRedirectAttributes(RedirectAttributes redirectAttributes, String action, String status, String message) {
		redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, action);
		redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, status);
		redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);
	}
	
	protected void setRedirectAttributes(RedirectAttributes redirectAttributes, String action, String status) {
		redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, action);
		redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, status);
	}
	
	protected void setActionResultInModel(Model model, String action, String status, String message) {
		model.addAttribute(TOAST_ACTION_KEY, action);
		model.addAttribute(TOAST_STATUS_KEY,  status);
		model.addAttribute(TOAST_MESSAGE_KEY, message);
	}
	
	protected <T1> boolean verifyHeaderColumns(MultipartFile file, MappingStrategy<T1> strategy, T1 sampleObject) throws IOException, CsvRequiredFieldEmptyException {
		Reader reader = new InputStreamReader(file.getInputStream());
		BufferedReader br = new BufferedReader(reader);
        String line = br.readLine();
        String[] elements = line.split(",");
        Set<String> headers = new HashSet<>();
        for (String header : elements) {
        	headers.add(header.toUpperCase());
        }
        br.close();
		
		Set<String> originalHeaders = new HashSet<>();
		originalHeaders.addAll(Arrays.asList(strategy.generateHeader(sampleObject)));
		
		return headers.equals(originalHeaders);
	}
	
	protected <T1> boolean checkFileAndHeader(MultipartFile file, RedirectAttributes redirectAttributes,
			MappingStrategy<T1> strategy, T1 sampleObject) throws IOException, CsvRequiredFieldEmptyException {
		String message;
		//Check if file is empty
		if (file.isEmpty()) {
			message = this.getMessage("common.file.upload.empty");
			setRedirectAttributes(redirectAttributes, "update", "warning", message);	
			return false;
		}
	
		//Check headers
		boolean columnsMatch = verifyHeaderColumns(file, strategy, sampleObject);
		if (!columnsMatch) {
			message = this.getMessage("csv.error.headers.or.file");
			setRedirectAttributes(redirectAttributes, "update", "warning", message);
			return false;

		}
		return true;
	}
}
