package fr.be.your.self.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.be.your.self.dto.PageableResponse;
import fr.be.your.self.model.Session;
import fr.be.your.self.repository.BaseRepository;
import fr.be.your.self.repository.SessionRepository;
import fr.be.your.self.service.SessionService;
import fr.be.your.self.util.StringUtils;

@Service
public class SessionServiceImpl extends BaseServiceImpl<Session, Integer> implements SessionService {
	
	@Autowired
	private SessionRepository repository;

	@Override
	protected BaseRepository<Session, Integer> getRepository() {
		return this.repository;
	}
	
	@Override
	public String getDefaultSort() {
		return "title|asc";
	}
	
	@Override
	@Transactional(readOnly = true)
	public long count(String text) {
		if (StringUtils.isNullOrSpace(text)) {
			return this.repository.count();
		}
		
		return this.repository.countByTitleContainsIgnoreCase(text);
	}

	@Override
	protected Iterable<Session> getList(String text, Sort sort) {
		return StringUtils.isNullOrSpace(text) 
				? this.repository.findAll(sort) 
				: this.repository.findAllByTitleContainsIgnoreCase(text, sort);
	}

	@Override
	protected Page<Session> getListByPage(String text, Pageable pageable) {
		return StringUtils.isNullOrSpace(text) 
				? this.repository.findAll(pageable)
				: this.repository.findAllByTitleContainsIgnoreCase(text, pageable);
	}

	@Override
	public long count(String text, List<Integer> categoryIds, Collection<Integer> voiceIds) {
		if (categoryIds == null || categoryIds.isEmpty()) {
			
			if (voiceIds == null || voiceIds.isEmpty()) {
				return this.count(text);
			}
			
			return this.repository.countByVoice(text, voiceIds);
		}
		
		if (voiceIds == null || voiceIds.isEmpty()) {
			return this.repository.countByCategory(text, categoryIds);
		}
		
		return this.repository.count(text, categoryIds, voiceIds);
	}

	@Override
	public PageableResponse<Session> pageableSearch(String text, 
			Collection<Integer> categoryIds, Collection<Integer> voiceIds, 
			Pageable pageable, Sort sort) {
		if (categoryIds == null || categoryIds.isEmpty()) {
			if (voiceIds == null || voiceIds.isEmpty()) {
				return this.pageableSearch(text, pageable, sort);
			}
		}
		
		if (pageable == null) {
			final List<Session> items = this.search(text, categoryIds, voiceIds, sort);
			return new PageableResponse<>(items);
		}
		
		if (categoryIds == null || categoryIds.isEmpty()) {
			final Page<Session> page = this.repository.findAllByVoice(text, voiceIds, pageable);
			return new PageableResponse<>(page);
		}
		
		if (voiceIds == null || voiceIds.isEmpty()) {
			final Page<Session> page = this.repository.findAllByCategory(text, categoryIds, pageable);
			return new PageableResponse<>(page);
		}
		
		final Page<Session> page = this.repository.findAll(text, categoryIds, voiceIds, pageable);
		return new PageableResponse<>(page);
	}

	@Override
	public List<Session> search(String text, Collection<Integer> categoryIds, Collection<Integer> voiceIds, Sort sort) {
		final Sort domainSort = sort == null ? Sort.unsorted() : sort;
		if (categoryIds == null || categoryIds.isEmpty()) {
			if (voiceIds == null || voiceIds.isEmpty()) {
				return this.search(text, sort);
			}
			
			final Iterable<Session> domains = this.repository.findAllByVoice(text, voiceIds, domainSort);
			return this.toList(domains);
		}
		
		if (voiceIds == null || voiceIds.isEmpty()) {
			final Iterable<Session> domains = this.repository.findAllByCategory(text, categoryIds, domainSort);
			return this.toList(domains);
		}
		
		final Iterable<Session> domains = this.repository.findAll(text, categoryIds, voiceIds, domainSort);	
		return this.toList(domains);
	}
}
