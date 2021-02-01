package fr.be.your.self.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import fr.be.your.self.model.Gift;

@Repository
public interface GiftRepository extends BaseRepository<Gift, Integer> {
	
	long countByNameContainsIgnoreCase(String name);
    
	Iterable<Gift> findAllByNameContainsIgnoreCase(String name, Sort sort);
    
    Page<Gift> findAllByNameContainsIgnoreCase(String name, Pageable pageable);
    
}
