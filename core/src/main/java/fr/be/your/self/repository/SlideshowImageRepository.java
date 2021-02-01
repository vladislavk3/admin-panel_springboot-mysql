package fr.be.your.self.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.be.your.self.model.SlideshowImage;

@Repository
public interface SlideshowImageRepository extends BaseRepository<SlideshowImage, Integer> {
	
	@Modifying
    @Query("UPDATE SlideshowImage SET index = ?2 WHERE id = ?1")
    int updateIndex(int id, int index);
	
    @Query("SELECT MAX(e.index) + 1 FROM SlideshowImage e WHERE e.slideshow.id = :slideshowId")
    Integer getMaxIndex(@Param("slideshowId") int slideshowId);
    
    @Query("SELECT COUNT(e) FROM SlideshowImage e WHERE e.slideshow.id = :slideshowId")
    long countBySlideshow(@Param("slideshowId") int slideshowId);
}
