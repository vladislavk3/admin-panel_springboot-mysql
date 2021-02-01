package fr.be.your.self.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fr.be.your.self.model.SubscriptionType;
import fr.be.your.self.repository.BaseRepository;
import fr.be.your.self.repository.SubscriptionTypeRepository;
import fr.be.your.self.service.SubscriptionTypeService;
import fr.be.your.self.util.StringUtils;

@Service
public class SubscriptionTypeServiceImpl extends BaseServiceImpl<SubscriptionType, Integer> implements SubscriptionTypeService {

	@Autowired
	SubscriptionTypeRepository subtypeRepo;

	@Override
	protected BaseRepository<SubscriptionType, Integer> getRepository() {
		return subtypeRepo;
	}

	@Override
	public String getDefaultSort() {
		return "name|asc";
	}

	@Override
	public long count(String text) {
		if (StringUtils.isNullOrSpace(text)) {
			return this.subtypeRepo.count();
		}

		return this.subtypeRepo.countByNameContainsIgnoreCase(text);
	}

	@Override
	protected Iterable<SubscriptionType> getList(String text, Sort sort) {
		return StringUtils.isNullOrSpace(text) ? this.subtypeRepo.findAll(sort)
				: this.subtypeRepo.findAllByNameContainsIgnoreCase(text, sort);
	}

	@Override
	protected Page<SubscriptionType> getListByPage(String text, Pageable pageable) {
		return StringUtils.isNullOrSpace(text) ? this.subtypeRepo.findAll(pageable)
				: this.subtypeRepo.findAllByNameContainsIgnoreCase(text, pageable);
	}

	@Override
	public SubscriptionType saveOrUpdate(SubscriptionType subtype) {
		return this.subtypeRepo.save(subtype);
	}

	@Override
	public Iterable<SubscriptionType> findAllByNameContainsIgnoreCase(String text) {
		return this.subtypeRepo.findAllByNameContainsIgnoreCase(text);
	}

	@Override
	public Boolean existsByName(String name) {
		return this.subtypeRepo.existsByName(name);
	}

	@Override
	public SubscriptionType findByName(String name) {
		try {
			return this.subtypeRepo.findByName(name);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}

	

}
