package fr.be.your.self.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fr.be.your.self.model.SessionCategory;

@Repository
public interface SessionCategoryRepository extends BaseRepository<SessionCategory, Integer> {
	
	long countByNameContainsIgnoreCase(String name);
    
	// findAllByNameContainsIgnoreCase("value")
    Iterable<SessionCategory> findAllByNameContainsIgnoreCase(String name);
    
    Iterable<SessionCategory> findAllByNameContainsIgnoreCase(String name, Sort sort);
    
    Page<SessionCategory> findAllByNameContainsIgnoreCase(String name, Pageable pageable);
    
    // findAllByNameNotContainsIgnoreCase("value")
    Iterable<SessionCategory> findAllByNameNotContainsIgnoreCase(String name);
    
    // findAllByNameLikeIgnoreCase("%value%"), with %
    Iterable<SessionCategory> findAllByNameLikeIgnoreCase(String name);
    
    // findAllByNameStartsWithIgnoreCase("value")
    Iterable<SessionCategory> findAllByNameStartsWithIgnoreCase(String name);
    
    // findAllByNameEndsWithIgnoreCase("value")
    Iterable<SessionCategory> findAllByNameEndsWithIgnoreCase(String name);
    
    @Query("SELECT m FROM SessionCategory m WHERE m.name LIKE ?1%")
    List<SessionCategory> searchByNameStartsWith(String name);
    
    @Query(
    	value = "SELECT a FROM SessionCategory a WHERE (:name IS NULL OR :name = '' OR lower(a.name) LIKE lower(concat('%', :name,'%')))",
    	countQuery = "SELECT COUNT(a) FROM SessionCategory a WHERE (:name IS NULL OR :name = '' OR lower(a.name) LIKE lower(concat('%', :name,'%')))"
    )
    Page<SessionCategory> findAllSortBySessionCount(@Param("name") String name, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "UPDATE SessionCategory c SET c.sessionCount = (SELECT COUNT(s) FROM c.sessions s) WHERE c.id IN (?1)")
    int updateSessionCount(Collection<Integer> ids);
}
