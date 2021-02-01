package fr.be.your.self.backend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.be.your.self.backend.dto.IdNameDto;
import fr.be.your.self.backend.setting.Constants;
import fr.be.your.self.dto.PageableResponse;
import fr.be.your.self.model.PO;
import fr.be.your.self.service.SessionCategoryService;
import fr.be.your.self.service.UserService;
import fr.be.your.self.util.StringUtils;

@Controller
@RequestMapping(Constants.PATH.WEB_ADMIN_PREFIX + "/" + CommonController.NAME)
public class CommonController extends BaseController {
	
	public static final String NAME = "common";
	
	@Autowired
	private SessionCategoryService categoryService;
	
	@Autowired
	private UserService userService;
	
	@GetMapping(value = { "/search" })
	@ResponseBody
    public ResponseEntity<?> searchCommonData(HttpSession session, HttpServletRequest request, 
    		HttpServletResponse response, Model model,
    		@RequestParam(name = "dataType", required = true) String dataType,
    		@RequestParam(name = "q", required = false) String search,
    		@RequestParam(name = "page", required = false) Integer page,
    		@RequestParam(name = "size", required = false) Integer size) {
		
		if ("discount-code".equalsIgnoreCase(dataType)) {
			return null;
		}
		
		final PageableResponse<? extends PO<Integer>> domains;
		if ("session-category".equalsIgnoreCase(dataType)) {
			final Sort sort = this.getSortRequest(this.categoryService.getDefaultSort());
			final PageRequest pageable = this.getPageRequest(page, size, sort);
			domains = this.categoryService.pageableSearch(search, pageable, sort);
		} else if ("professional".equalsIgnoreCase(dataType)) {
			final Sort sort = this.getSortRequest(this.userService.getDefaultSort());
			final PageRequest pageable = this.getPageRequest(page, size, sort);
			domains = this.userService.searchProfessionalByName(search, pageable, sort);
		} else {
			domains = null;
		}
		
		final PageableResponse<IdNameDto<Integer>> results = new PageableResponse<>();
		if (domains == null) {
			return new ResponseEntity<>(results, HttpStatus.OK);
		}
		
		results.setPageIndex(domains.getPageIndex());
		results.setPageSize(domains.getPageSize());
		results.setTotalItems(domains.getTotalItems());
		results.setTotalPages(domains.getTotalPages());
		
		for (PO<Integer> domain : domains.getItems()) {
			final IdNameDto<Integer> dto = new IdNameDto<>(domain);
			results.addItem(dto);
		}
		
		return new ResponseEntity<>(results, HttpStatus.OK);
	}
	
	protected final PageRequest getPageRequest(Integer page, Integer size, Sort sort) {
        if (page == null || page < 1) {
            page = 1;
        }

        if (size == null || size < 1) {
        	size = this.dataSetting.getDefaultDropdownSearchPageSize();
        }
        
        return PageRequest.of(page - 1, size, sort);
    }
	
	protected final Sort getSortRequest(String sortQuery) {
		final List<Order> orders = new ArrayList<Order>();
		
        if (!StringUtils.isNullOrSpace(sortQuery)) {
        	final String[] sortProperties = sortQuery.split(";");
        	
        	for (String sortProperty : sortProperties) {
        		final String[] sortValues = sortProperty.split("\\|");
        		if (sortValues.length != 2) {
        			continue;
        		}
        		
    			final Optional<Direction> direction = Direction.fromOptionalString(sortValues[1]);
    			final Order order = new Order(direction.isPresent() ? direction.get() : Direction.ASC, sortValues[0]);
    			orders.add(order);
			}
        }
        
        return Sort.by(orders);
	}
}
