package fr.be.your.self.service.impl;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.be.your.self.dto.PageableResponse;
import fr.be.your.self.model.SessionCategory;
import fr.be.your.self.repository.BaseRepository;
import fr.be.your.self.repository.SessionCategoryRepository;
import fr.be.your.self.service.SessionCategoryService;
import fr.be.your.self.util.StringUtils;

@Service
public class SessionCategoryServiceImpl extends BaseServiceImpl<SessionCategory, Integer> implements SessionCategoryService {
	
	@Autowired
	private SessionCategoryRepository repository;

	@Override
	protected BaseRepository<SessionCategory, Integer> getRepository() {
		return this.repository;
	}
	
	@Override
	public String getDefaultSort() {
		return "name|asc";
	}
	
	@Override
	@Transactional(readOnly = true)
	public long count(String text) {
		if (StringUtils.isNullOrSpace(text)) {
			return this.repository.count();
		}
		
		return this.repository.countByNameContainsIgnoreCase(text);
	}

	@Override
	protected Iterable<SessionCategory> getList(String text, Sort sort) {
		return StringUtils.isNullOrSpace(text)
				? this.repository.findAll(sort) 
				: this.repository.findAllByNameContainsIgnoreCase(text, sort);
	}

	@Override
	protected Page<SessionCategory> getListByPage(String text, Pageable pageable) {
		return StringUtils.isNullOrSpace(text) 
				? this.repository.findAll(pageable)
				: this.repository.findAllByNameContainsIgnoreCase(text, pageable);
	}

	@Override
	public int updateSessionCount(Collection<Integer> ids) {
		if (ids == null || ids.isEmpty()) {
			return 0;
		}
		
		return this.repository.updateSessionCount(ids);
	}

	@Override
	public PageableResponse<SessionCategory> pageableSearchSortSessionCount(String text, Pageable pageable) {
		final Page<SessionCategory> page = this.repository.findAllSortBySessionCount(text, pageable);
		
		return new PageableResponse<>(page);
	}
}
