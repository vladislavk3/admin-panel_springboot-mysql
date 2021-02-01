package fr.be.your.self.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import fr.be.your.self.dto.PageableResponse;
import fr.be.your.self.model.BusinessCode;

public interface BusinessCodeService extends BaseService<BusinessCode, Integer> {

	public boolean existsName(String name);

	public long count(String text, String beneficiary, Collection<Integer> types);

	public PageableResponse<BusinessCode> pageableSearch(String text, String beneficiary, Collection<Integer> types, 
			Pageable pageable, Sort sort);

	public List<BusinessCode> search(String text, String beneficiary, Collection<Integer> types, Sort sort);
	
	public Map<Integer, Integer> getUsedAmountByIds(Collection<Integer> ids);

	public Iterable<BusinessCode> findAllByNameContainsIgnoreCase(String code);
}
