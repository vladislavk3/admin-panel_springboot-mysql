package fr.be.your.self.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fr.be.your.self.model.Functionality;
import fr.be.your.self.repository.BaseRepository;
import fr.be.your.self.repository.FunctionalityRepository;
import fr.be.your.self.service.FunctionalityService;
import fr.be.your.self.util.StringUtils;

@Service
public class FunctionalityServiceImpl extends BaseServiceImpl<Functionality, Integer> implements FunctionalityService {
	
	@Autowired
	private FunctionalityRepository repository;

	@Override
	protected BaseRepository<Functionality, Integer> getRepository() {
		return this.repository;
	}
	
	@Override
	public String getDefaultSort() {
		return "name|asc";
	}

	@Override
	public long count(String text) {
		if (StringUtils.isNullOrSpace(text)) {
			return this.repository.count();
		}
		
		return this.repository.countByNameContainsIgnoreCase(text);
	}

	@Override
	protected Iterable<Functionality> getList(String text, Sort sort) {
		return StringUtils.isNullOrSpace(text) 
				? this.repository.findAll(sort) 
				: this.repository.findAllByNameContainsIgnoreCase(text, sort);
	}

	@Override
	protected Page<Functionality> getListByPage(String text, Pageable pageable) {
		return StringUtils.isNullOrSpace(text) 
				? this.repository.findAll(pageable)
				: this.repository.findAllByNameContainsIgnoreCase(text, pageable);
	}
}
