package fr.be.your.self.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fr.be.your.self.model.Address;
import fr.be.your.self.repository.AddressRepository;
import fr.be.your.self.repository.BaseRepository;
import fr.be.your.self.service.AddressService;
import fr.be.your.self.util.StringUtils;

@Service
public class AddressServiceImpl extends BaseServiceImpl<Address, Integer> implements AddressService {
	@Autowired
	private AddressRepository repository;

	@Override
	public String getDefaultSort() {
		return "address|asc";

	}

	@Override
	public long count(String text) {
		if (StringUtils.isNullOrSpace(text)) {
			return this.repository.count();
		}

		return this.repository.countByAddressContainsIgnoreCase(text);
	}

	@Override
	protected BaseRepository<Address, Integer> getRepository() {
		return this.repository;
	}

	@Override
	protected Iterable<Address> getList(String text, Sort sort) {
		return StringUtils.isNullOrSpace(text) ? this.repository.findAll(sort)
				: this.repository.findAllByAddressContainsIgnoreCase(text, sort);
	}

	@Override
	protected Page<Address> getListByPage(String text, Pageable pageable) {
		return StringUtils.isNullOrSpace(text) ? this.repository.findAll(pageable)
				: this.repository.findAllByAddressContainsIgnoreCase(text, pageable);
	}

}
