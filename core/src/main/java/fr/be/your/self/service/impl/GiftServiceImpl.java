package fr.be.your.self.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.be.your.self.model.Gift;
import fr.be.your.self.repository.BaseRepository;
import fr.be.your.self.repository.GiftRepository;
import fr.be.your.self.service.GiftService;
import fr.be.your.self.util.StringUtils;

@Service
public class GiftServiceImpl extends BaseServiceImpl<Gift, Integer> implements GiftService {
	
	@Autowired
	private GiftRepository repository;

	@Override
	protected BaseRepository<Gift, Integer> getRepository() {
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
	@Transactional(readOnly = true)
	protected Iterable<Gift> getList(String text, Sort sort) {
		return StringUtils.isNullOrSpace(text)
				? this.repository.findAll(sort) 
				: this.repository.findAllByNameContainsIgnoreCase(text, sort);
	}

	@Override
	@Transactional(readOnly = true)
	protected Page<Gift> getListByPage(String text, Pageable pageable) {
		return StringUtils.isNullOrSpace(text) 
				? this.repository.findAll(pageable)
				: this.repository.findAllByNameContainsIgnoreCase(text, pageable);
	}
}
