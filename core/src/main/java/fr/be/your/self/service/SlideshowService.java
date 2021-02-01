package fr.be.your.self.service;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import fr.be.your.self.dto.PageableResponse;
import fr.be.your.self.model.Slideshow;
import fr.be.your.self.model.SlideshowImage;

public interface SlideshowService extends BaseService<Slideshow, Integer> {

	public Slideshow getCurrentSlideshow();
	
	public long countAvailaible(Date startDate);

	public PageableResponse<Slideshow> searchAvailaible(Date startDate, Pageable pageable, Sort sort);

	public List<Slideshow> searchAvailaible(Date startDate, Sort sort);
	
	public SlideshowImage getImage(int imageId);
	
	public SlideshowImage createImage(SlideshowImage image);
	
	public SlideshowImage updateImage(SlideshowImage image);
	
	public boolean deleteImage(int imageId);
	
	public boolean updateImageIndex(int imageId, int index);
	
	public int getMaxImageIndex(int id);
	
	public long countImage(int id);
}
