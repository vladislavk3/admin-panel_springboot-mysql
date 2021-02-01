package fr.be.your.self.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fr.be.your.self.model.ProfessionalEvent;
import fr.be.your.self.repository.BaseRepository;
import fr.be.your.self.repository.ProfessionalEventRepository;
import fr.be.your.self.service.ProfessionalEventService;
import fr.be.your.self.util.StringUtils;

@Service
public class ProfessionalEventServiceImpl extends BaseServiceImpl<ProfessionalEvent, Integer> implements ProfessionalEventService {

	@Autowired
	ProfessionalEventRepository repository;
	
	@Override
	public String getDefaultSort() {
		return "date|asc";
	}

	@Override
	public long count(String text) {
		if (StringUtils.isNullOrSpace(text)) {
			return this.repository.count();
		} else 
			throw new UnsupportedOperationException("Not yet support count with text");
	}

	@Override
	protected BaseRepository<ProfessionalEvent, Integer> getRepository() {
		return repository;
	}

	@Override
	protected Iterable<ProfessionalEvent> getList(String text, Sort sort) {
		if ( StringUtils.isNullOrSpace(text) ) {
			return this.repository.findAll(sort);
		} else 
			throw new UnsupportedOperationException("Not yet support getList with text");
	}

	@Override
	protected Page<ProfessionalEvent> getListByPage(String text, Pageable pageable) {
		if ( StringUtils.isNullOrSpace(text) ) {
			return this.repository.findAll(pageable);
		} else 
			throw new UnsupportedOperationException("Not yet support getListByPage with text");
	}

}
