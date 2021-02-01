package fr.be.your.self.backend.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.be.your.self.backend.dto.SubscriptionTypeDto;
import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.common.CanalType;
import fr.be.your.self.exception.BusinessException;
import fr.be.your.self.model.SubscriptionType;
import fr.be.your.self.service.BaseService;
import fr.be.your.self.service.SubscriptionTypeService;

@Controller
@RequestMapping(Constants.PATH.WEB_ADMIN_PREFIX + "/" + SubscriptionTypeController.NAME)
public class SubscriptionTypeController
		extends BaseResourceController<SubscriptionType, SubscriptionType, SubscriptionTypeDto, Integer> {

	public static final String NAME = "subtype";

	private static final Map<String, String[]> SORTABLE_COLUMNS = new HashMap<>();

	static {
		SORTABLE_COLUMNS.put("name", new String[] { "name" });
		SORTABLE_COLUMNS.put("duration", new String[] { "duration" });
		SORTABLE_COLUMNS.put("canal", new String[] { "canal" });
		SORTABLE_COLUMNS.put("price", new String[] { "price" });
		SORTABLE_COLUMNS.put("autoRenew", new String[] { "autoRenew" });
		SORTABLE_COLUMNS.put("status", new String[] { "status" });
	}

	private static final String BASE_MEDIA_URL = Constants.PATH.WEB_ADMIN_PREFIX + Constants.PATH.WEB_ADMIN.MEDIA
			+ Constants.FOLDER.MEDIA.SESSION;

	@Autowired
	private SubscriptionTypeService subTypeService;

	@Override
	protected Map<String, String[]> getSortableColumns() {
		return SORTABLE_COLUMNS;
	}

	@Override
	protected String getFormView() {
		return "subtype/subtype_form";
	}

	@Override
	protected String getListView() {
		return "subtype/subtype-list";
	}

	@Override
	protected void loadDetailFormOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, SubscriptionType domain, SubscriptionTypeDto dto) throws BusinessException {

		final List<String> canals = CanalType.getPossibleStrValues();
		final List<Integer> durations = dataSetting.getSubscriptionDurations();

		model.addAttribute("canals", canals);
		model.addAttribute("durations", durations);

	}

	@PostMapping("/create")
	@Transactional
	public String createDomain(@ModelAttribute @Validated SubscriptionTypeDto dto, HttpSession session,
			HttpServletRequest request, HttpServletResponse response, BindingResult result,
			RedirectAttributes redirectAttributes, Model model) {

		if (result.hasErrors()) {
			return this.getFormView();
		}

		final SubscriptionType domain = this.newDomain();
		dto.copyToDomain(domain);

		final SubscriptionType savedDomain = this.subTypeService.create(domain);

		// Error, delete upload file
		if (savedDomain == null || result.hasErrors()) {

			return this.getFormView();
		}
		redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "create");
		redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "success");

		return "redirect:" + this.getBaseURL();
	}

	@PostMapping("/update/{id}")
	@Transactional
	public String updateDomain(@PathVariable("id") Integer id, @ModelAttribute @Validated SubscriptionTypeDto dto,
			HttpSession session, HttpServletRequest request, HttpServletResponse response, BindingResult result,
			RedirectAttributes redirectAttributes, Model model) {

		if (result.hasErrors()) {
			dto.setId(id);
			return this.getFormView();
		}

		SubscriptionType domain = this.subTypeService.getById(id);
		if (domain == null) {
			final ObjectError error = this.createIdNotFoundError(result, id);
			result.addError(error);

			dto.setId(id);
			return this.getFormView();
		}

		dto.copyToDomain(domain);

		final SubscriptionType savedDomain = this.subTypeService.update(domain);

		redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "update");
		redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "success");

		return "redirect:" + this.getBaseURL() + "/current-page";
	}

	@PostMapping(value = { "/delete/{id}" })
	@Transactional
	public String deletePage(@PathVariable(name = "id", required = true) Integer id, HttpSession session,
			HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes,
			Model model) {

		final SubscriptionType domain = this.subTypeService.getById(id);
		if (domain == null) {
			final String message = this.getIdNotFoundMessage(id);

			redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "delete");
			redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
			redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);

			return "redirect:" + this.getBaseURL() + "/current-page";
		}

		final boolean result = this.subTypeService.delete(id);
		if (result) {

			return "redirect:" + this.getBaseURL();
		}

		final String message = this.getDeleteByIdErrorMessage(id);

		redirectAttributes.addFlashAttribute(TOAST_ACTION_KEY, "delete");
		redirectAttributes.addFlashAttribute(TOAST_STATUS_KEY, "warning");
		redirectAttributes.addFlashAttribute(TOAST_MESSAGE_KEY, message);

		return "redirect:" + this.getBaseURL() + "/current-page";
	}

	@Override
	protected String getName() {
		return NAME;
	}

	@Override
	protected String getDefaultPageTitle(String baseMessageKey) {
		return this.getMessage(baseMessageKey + ".page.title", "Subscription type management");
	}

	@Override
	protected String getUploadDirectoryName() {
		return null;
	}

	@Override
	protected BaseService<SubscriptionType, Integer> getService() {
		return this.subTypeService;
	}

	@Override
	protected SubscriptionType newDomain() {
		return new SubscriptionType();
	}

	@Override
	protected SubscriptionTypeDto createDetailDto(SubscriptionType domain) {
		return new SubscriptionTypeDto(domain);
	}

	@Override
	protected SubscriptionType createSimpleDto(SubscriptionType domain) {
		return domain;
	}

	@Override
	protected String getBaseMediaURL() {
		return BASE_MEDIA_URL;
	}

}
