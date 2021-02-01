package fr.be.your.self.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fr.be.your.self.model.Price;
import fr.be.your.self.repository.BaseRepository;
import fr.be.your.self.repository.PriceRepository;
import fr.be.your.self.service.PriceService;
import fr.be.your.self.util.StringUtils;

@Service
public class PriceServiceImpl extends BaseServiceImpl<Price, Integer> implements PriceService {
	@Autowired
	private PriceRepository repository;
	
	@Override
	public String getDefaultSort() {
		return "price|asc";

	}

	@Override
	public long count(String text) {
		if (StringUtils.isNullOrSpace(text)) {
			return this.repository.count();
		}

		return this.repository.countByLabelContainsIgnoreCase(text);
	}

	@Override
	protected BaseRepository<Price, Integer> getRepository() {
		return repository;
	}

	@Override
	protected Iterable<Price> getList(String text, Sort sort) {
		if ( StringUtils.isNullOrSpace(text) ) {
			return this.repository.findAll(sort);
		} else throw new UnsupportedOperationException("Not yet support getList() with text");
	}

	@Override
	protected Page<Price> getListByPage(String text, Pageable pageable) {
		if ( StringUtils.isNullOrSpace(text) ) {
			return this.repository.findAll(pageable);
		} else throw new UnsupportedOperationException("Not yet support getListByPage() with text");
		
	}

}
