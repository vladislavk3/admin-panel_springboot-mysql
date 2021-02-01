package fr.be.your.self.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.be.your.self.dto.PageableResponse;
import fr.be.your.self.model.Slideshow;
import fr.be.your.self.model.SlideshowImage;
import fr.be.your.self.repository.BaseRepository;
import fr.be.your.self.repository.SlideshowImageRepository;
import fr.be.your.self.repository.SlideshowRepository;
import fr.be.your.self.service.SlideshowService;

@Service
public class SlideshowServiceImpl extends BaseServiceImpl<Slideshow, Integer> implements SlideshowService {
	
	@Autowired
	private SlideshowRepository repository;

	@Autowired
	private SlideshowImageRepository imageRepository;
	
	@Override
	protected BaseRepository<Slideshow, Integer> getRepository() {
		return this.repository;
	}
	
	@Override
	public String getDefaultSort() {
		return "startDate|asc";
	}
	
	@Override
	@Transactional(readOnly = true)
	public long count(String text) {
		return this.repository.count();
	}

	@Override
	@Transactional(readOnly = true)
	protected Iterable<Slideshow> getList(String text, Sort sort) {
		return this.repository.findAll(sort);
	}

	@Override
	@Transactional(readOnly = true)
	protected Page<Slideshow> getListByPage(String text, Pageable pageable) {
		return this.repository.findAll(pageable);
	}

	@Override
	public Slideshow getCurrentSlideshow() {
		final Optional<Slideshow> domain = this.repository.findFirstByStartDateIsNullOrStartDateLessThanEqualOrderByStartDateDesc(new Date());
		
		if (domain.isPresent()) {
			return domain.get();
		}
		
		return null;
	}

	@Override
	public long countAvailaible(Date startDate) {
		if (startDate == null) {
			return this.repository.count();
		}
		
		return this.repository.countByStartDateGreaterThan(startDate);
	}

	@Override
	public PageableResponse<Slideshow> searchAvailaible(Date startDate, Pageable pageable, Sort sort) {
		if (pageable == null) {
			final List<Slideshow> items = this.searchAvailaible(startDate, sort);
			return new PageableResponse<>(items);
		}
		
		if (startDate == null) {
			final Page<Slideshow> page = this.repository.findAll(pageable);
			return new PageableResponse<>(page);
		}
		
		final Page<Slideshow> page = this.repository.findAllByStartDateGreaterThan(startDate, pageable);
		return new PageableResponse<>(page);
	}

	@Override
	public List<Slideshow> searchAvailaible(Date startDate, Sort sort) {
		final Sort domainSort = sort == null ? Sort.unsorted() : sort;
		
		if (startDate == null) {
			final Iterable<Slideshow> domains = this.repository.findAll(domainSort);
			return this.toList(domains);
		}
		
		final Iterable<Slideshow> domains = this.repository.findAllByStartDateGreaterThan(startDate, domainSort);
		return this.toList(domains);
	}

	@Override
	public SlideshowImage getImage(int imageId) {
		final Optional<SlideshowImage> image = this.imageRepository.findById(imageId);
		return image.isPresent() ? image.get() : null;
	}

	@Override
	public SlideshowImage createImage(SlideshowImage image) {
		return this.imageRepository.save(image);
	}

	@Override
	public SlideshowImage updateImage(SlideshowImage image) {
		return this.imageRepository.save(image);
	}

	@Override
	public boolean deleteImage(int imageId) {
		this.imageRepository.deleteById(imageId);
		return true;
	}

	@Override
	public boolean updateImageIndex(int imageId, int index) {
		return this.imageRepository.updateIndex(imageId, index) > 0;
	}

	@Override
	public int getMaxImageIndex(int id) {
		final Integer maxIndex = this.imageRepository.getMaxIndex(id);
		return maxIndex == null ? 0 : maxIndex.intValue();
	}

	@Override
	public long countImage(int id) {
		return this.imageRepository.countBySlideshow(id);
	}
}
