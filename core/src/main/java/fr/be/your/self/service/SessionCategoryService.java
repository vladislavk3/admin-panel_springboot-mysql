package fr.be.your.self.service;

import java.util.Collection;

import org.springframework.data.domain.Pageable;

import fr.be.your.self.dto.PageableResponse;
import fr.be.your.self.model.SessionCategory;

public interface SessionCategoryService extends BaseService<SessionCategory, Integer> {
	
	int updateSessionCount(Collection<Integer> ids);
	
	public PageableResponse<SessionCategory> pageableSearchSortSessionCount(String text, Pageable pageable);
}
