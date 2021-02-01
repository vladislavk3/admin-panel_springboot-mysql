package fr.be.your.self.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import fr.be.your.self.model.SubscriptionType;

@Repository
public interface SubscriptionTypeRepository extends BaseRepository<SubscriptionType, Integer>  {
	Boolean existsByName(String name);
	SubscriptionType findByName(String name);


	Page<SubscriptionType> findAllByNameContainsIgnoreCase(String text, Pageable pageable);

	Iterable<SubscriptionType> findAllByNameContainsIgnoreCase(String text);
	Iterable<SubscriptionType> findAllByNameContainsIgnoreCase(String text, Sort sort);
	
	long countByNameContainsIgnoreCase(String text);

}
