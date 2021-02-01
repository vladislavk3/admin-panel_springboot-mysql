package fr.be.your.self.service.impl;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.be.your.self.model.SplashScreen;
import fr.be.your.self.repository.BaseRepository;
import fr.be.your.self.repository.SplashScreenRepository;
import fr.be.your.self.service.SplashScreenService;

@Service
public class SplashScreenServiceImpl extends BaseServiceImpl<SplashScreen, Integer> implements SplashScreenService {
	
	@Autowired
	private SplashScreenRepository repository;

	@Override
	protected BaseRepository<SplashScreen, Integer> getRepository() {
		return this.repository;
	}
	
	@Override
	public String getDefaultSort() {
		return "id|asc";
	}
	
	@Override
	@Transactional(readOnly = true)
	public long count(String text) {
		return this.repository.count();
	}

	@Override
	@Transactional(readOnly = true)
	protected Iterable<SplashScreen> getList(String text, Sort sort) {
		return this.repository.findAll(sort);
	}

	@Override
	@Transactional(readOnly = true)
	protected Page<SplashScreen> getListByPage(String text, Pageable pageable) {
		return this.repository.findAll(pageable);
	}

	@Override
	public SplashScreen getMainSplashScreen() {
		final Iterable<SplashScreen> domains = this.repository.findAll();
		if (domains == null) {
			return null;
		}
		
		final Iterator<SplashScreen> iterator = domains.iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		}
		
		return null;
	}
}
