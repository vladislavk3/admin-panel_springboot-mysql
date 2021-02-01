package fr.be.your.self.service;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import fr.be.your.self.dto.PageableResponse;
import fr.be.your.self.model.Session;

public interface SessionService extends BaseService<Session, Integer> {
	
	public long count(String text, List<Integer> categoryIds, Collection<Integer> voiceIds);

	public PageableResponse<Session> pageableSearch(String text, 
			Collection<Integer> categoryIds, Collection<Integer> voiceIds, 
			Pageable pageable, Sort sort);

	public List<Session> search(String text, 
			Collection<Integer> categoryIds, Collection<Integer> voiceIds, Sort sort);
}
