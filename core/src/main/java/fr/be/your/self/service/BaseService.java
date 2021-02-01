package fr.be.your.self.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import fr.be.your.self.dto.PageableResponse;

public interface BaseService<T, K extends Serializable> {
	
	/**
	 * @return default data sort, format column|{asc|desc}
	 **/
	public String getDefaultSort();
	
	public T getById(K id);

	public List<T> getByIds(Collection<K> ids);
	
	public List<T> getByIds(@SuppressWarnings("unchecked") K...ids);
	
	public List<T> getAll(Sort sort);
	
	public long count(String text);

	public PageableResponse<T> pageableSearch(String text, Pageable pageable, Sort sort);

	public List<T> search(String text, Sort sort);
	
	public T create(T domain);

	public T update(T domain);

	public boolean delete(K id);

	public Iterable<T> findAll();
	
	public Page<T> getPaginatedObjects(Pageable pageable);

	public <S extends T> Iterable<S> saveAll(Iterable<S> entities);
}
