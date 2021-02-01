package fr.be.your.self.backend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.be.your.self.backend.dto.BusinessCodeDto;
import fr.be.your.self.backend.dto.PermissionDto;
import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.common.BusinessCodeStatus;
import fr.be.your.self.common.BusinessCodeType;
import fr.be.your.self.common.ErrorStatusCode;
import fr.be.your.self.common.UserPermission;
import fr.be.your.self.dto.PageableResponse;
import fr.be.your.self.exception.BusinessException;
import fr.be.your.self.model.BusinessCode;
import fr.be.your.self.util.NumberUtils;

@Controller
@RequestMapping(Constants.PATH.WEB_ADMIN_PREFIX + "/" + DiscountCodeController.NAME)
public class DiscountCodeController extends BaseCodeController {
	public static final String NAME = "discount-code";
	
	private static final Map<String, String[]> SORTABLE_COLUMNS = new HashMap<>();
	
	private static final int[] AVAILABLE_CODE_TYPES = new int[] { 
			BusinessCodeType.B2B_MULTIPLE.getValue(),
			BusinessCodeType.B2B_UNIQUE.getValue(),
			BusinessCodeType.B2C_DISCOUNT_100.getValue(),
			BusinessCodeType.B2C_DISCOUNT.getValue()
	};
	
	static {
		SORTABLE_COLUMNS.put("name", new String[] { "name" });
		SORTABLE_COLUMNS.put("type", new String[] { "type" });
		SORTABLE_COLUMNS.put("status", new String[] { "status" });
		SORTABLE_COLUMNS.put("startDate", new String[] { "startDate" });
		SORTABLE_COLUMNS.put("endDate", new String[] { "endDate" });
		SORTABLE_COLUMNS.put("maxUserAmount", new String[] { "maxUserAmount" });
		SORTABLE_COLUMNS.put("beneficiary", new String[] { "beneficiary" });
		SORTABLE_COLUMNS.put("dealPrice", new String[] { "dealPrice" });
		SORTABLE_COLUMNS.put("pricePerUser", new String[] { "pricePerUser" });
		SORTABLE_COLUMNS.put("discountType", new String[] { "discountType" });
		SORTABLE_COLUMNS.put("discountValue", new String[] { "discountValue" });
	}
	
	@Override
	protected String getName() {
		return NAME;
	}
	
	@Override
	protected String getDefaultPageTitle(String baseMessageKey) {
		return this.getMessage(baseMessageKey + ".page.title", "Discount code management");
	}
	
	@Override
	protected Map<String, String[]> getSortableColumns() {
		return SORTABLE_COLUMNS;
	}

	@Override
	protected UserPermission getGlobalPermission(Model model, PermissionDto permission) {
		final String pageName = this.getName();
		
		if (permission.hasWritePermission(pageName)) {
			return UserPermission.WRITE;
		}
		
		UserPermission userPermission = UserPermission.DENIED;
		if (permission.hasPermission(pageName)) {
			userPermission = UserPermission.READONLY;
		}
		
		for (int i = 0; i < AVAILABLE_CODE_TYPES.length; i++) {
			final String permissionPath = pageName + "-" + AVAILABLE_CODE_TYPES[i];
			
			if (permission.hasWritePermission(permissionPath)) {
				return UserPermission.WRITE;
			}
			
			if (permission.hasPermission(permissionPath)) {
				userPermission = UserPermission.READONLY;
			}
		}
		
		return userPermission;
	}
	
	@Override
	protected void loadDetailFormOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, BusinessCode domain, BusinessCodeDto dto) throws BusinessException {
		super.loadDetailFormOptions(session, request, response, model, domain, dto);
		
		final PermissionDto permission = (PermissionDto) model.getAttribute("permission");
		if (permission == null) {
			throw new BusinessException(ErrorStatusCode.INVALID_PERMISSION);
		}
		
		if (dto.getStatus() != BusinessCodeStatus.ACTIVE.getValue() 
				&& dto.getStatus() != BusinessCodeStatus.INACTIVE.getValue()) {
			dto.setStatus(BusinessCodeStatus.ACTIVE.getValue());
		}
		
		final List<Integer> availableCodeTypes = new ArrayList<Integer>();
		
		final String pageName = this.getName();
		final boolean hasGlobalWritePermission = permission.hasWritePermission(pageName);
		final int codeType = dto.getType();
		
		for (int i = 0; i < AVAILABLE_CODE_TYPES.length; i++) {
			final String permissionPath = pageName + "-" + AVAILABLE_CODE_TYPES[i];
			
			if (hasGlobalWritePermission || permission.hasWritePermission(permissionPath)) {
				availableCodeTypes.add(AVAILABLE_CODE_TYPES[i]);
				
				if (dto.getType() == BusinessCodeType.UNKNOWN.getValue()) {
					dto.setType(AVAILABLE_CODE_TYPES[i]);
				}
			} else if (AVAILABLE_CODE_TYPES[i] == codeType && !permission.hasPermission(permissionPath)) {
				throw new BusinessException(ErrorStatusCode.INVALID_PERMISSION);
			}
		}
		
		model.addAttribute("availableCodeTypes", availableCodeTypes);
	}
	
	@Override
	protected void loadListPageOptions(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			Model model, Map<String, String> searchParams, PageableResponse<BusinessCodeDto> pageableDto)
			throws BusinessException {
		super.loadListPageOptions(session, request, response, model, searchParams, pageableDto);
		
		final PermissionDto permission = (PermissionDto) model.getAttribute("permission");
		if (permission == null) {
			throw new BusinessException(ErrorStatusCode.INVALID_PERMISSION);
		}
		
		final String pageName = this.getName();
		final boolean hasGlobalWritePermission = permission.hasPermission(pageName);
		
		final List<Integer> availableCodeTypes = new ArrayList<Integer>();
		for (int i = 0; i < AVAILABLE_CODE_TYPES.length; i++) {
			final String permissionPath = pageName + "-" + AVAILABLE_CODE_TYPES[i];
			
			if (hasGlobalWritePermission || permission.hasPermission(permissionPath)) {
				availableCodeTypes.add(AVAILABLE_CODE_TYPES[i]);
			}
		}
		
		model.addAttribute("availableCodeTypes", availableCodeTypes);
		
		final String beneficiary = searchParams.get("beneficiary");
		model.addAttribute("beneficiary", beneficiary == null ? "" : beneficiary.trim());
		
		final List<Integer> filteredCodeTypes = NumberUtils.parseIntegers(searchParams.get("filterCodeTypes"), ",");
		model.addAttribute("filteredCodeTypes", filteredCodeTypes);
	}

	@Override
	protected PageableResponse<BusinessCode> pageableSearch(Map<String, String> searchParams, PageRequest pageable, Sort sort) {
		final String search = searchParams.get("q");
		final String beneficiary = searchParams.get("beneficiary");
		final List<Integer> filteredTypes = NumberUtils.parseIntegers(searchParams.get("filterCodeTypes"), ",");
		
		return this.mainService.pageableSearch(search, beneficiary, filteredTypes, pageable, sort);
	}
	
	@Override
	protected void validateCreateDomain(BindingResult result, Model model, BusinessCode domain) {
		final int codeType = domain.getType();
		for (int i = 0; i < AVAILABLE_CODE_TYPES.length; i++) {
			if (codeType == AVAILABLE_CODE_TYPES[i]) {
				return;
			}
		}
		
		final ObjectError fieldError = this.createFieldError(result, "type", "invalid", new Object[] { codeType }, "Invalid code type");
		result.addError(fieldError);
	}

	@Override
	protected void validateUpdateDomain(BindingResult result, Model model, BusinessCode domain) {
		this.validateCreateDomain(result, model, domain);
	}

	@Override
	protected String validateDeleteDomain(Model model, BusinessCode domain) {
		return null;
	}
}
