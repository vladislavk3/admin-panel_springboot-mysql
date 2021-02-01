package fr.be.your.self.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import fr.be.your.self.model.Functionality;

@Repository
public interface FunctionalityRepository extends BaseRepository<Functionality, Integer> {

	public Optional<Functionality> findByPath(String path);

	public long countByNameContainsIgnoreCase(String name);

	public Iterable<Functionality> findAllByNameContainsIgnoreCase(String name);

	public Iterable<Functionality> findAllByNameContainsIgnoreCase(String name, Sort sort);
	
	public Page<Functionality> findAllByNameContainsIgnoreCase(String name, Pageable pageable);
}
