package fr.be.your.self.repository;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import fr.be.your.self.model.Slideshow;

@Repository
public interface SlideshowRepository extends BaseRepository<Slideshow, Integer> {
	
	Optional<Slideshow> findFirstByStartDateIsNullOrStartDateLessThanEqualOrderByStartDateDesc(Date date);
	
	long countByStartDateGreaterThan(Date date);
	
    Iterable<Slideshow> findAllByStartDateGreaterThan(Date date, Sort sort);
    
    Page<Slideshow> findAllByStartDateGreaterThan(Date date, Pageable pageable);
    
    //long countByStartDateIsNotNull();
    
    //Iterable<Slideshow> findAllByStartDateIsNotNull(Sort sort);
    
    //Page<Slideshow> findAllByStartDateIsNotNull(Pageable pageable);
    
    Optional<Slideshow> findFirstByStartDateIsNull();
}
