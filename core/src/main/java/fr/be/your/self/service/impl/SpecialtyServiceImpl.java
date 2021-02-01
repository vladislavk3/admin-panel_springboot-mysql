package fr.be.your.self.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import fr.be.your.self.model.Specialty;
import fr.be.your.self.repository.BaseRepository;
import fr.be.your.self.repository.SpecialtyRepository;
import fr.be.your.self.service.SpecialtyService;
import fr.be.your.self.util.StringUtils;

public class SpecialtyServiceImpl extends BaseServiceImpl<Specialty, Integer> implements SpecialtyService {
	@Autowired
	SpecialtyRepository repository;
	
	@Override
	public String getDefaultSort() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count(String text) {
		if (StringUtils.isNullOrSpace(text)) {
			return this.repository.count();
		} else 
			throw new UnsupportedOperationException("Not yet support count with text");
	}

	@Override
	protected BaseRepository<Specialty, Integer> getRepository() {
		return repository;
	}

	@Override
	protected Iterable<Specialty> getList(String text, Sort sort) {
		if ( StringUtils.isNullOrSpace(text) ) {
			return this.repository.findAll(sort);
		} else 
			throw new UnsupportedOperationException("Not yet support getList with text");
	}

	@Override
	protected Page<Specialty> getListByPage(String text, Pageable pageable) {
		if ( StringUtils.isNullOrSpace(text) ) {
			return this.repository.findAll(pageable);
		} else 
			throw new UnsupportedOperationException("Not yet support getListByPage with text");
	}

}
