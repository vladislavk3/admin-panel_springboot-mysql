package fr.be.your.self.backend.dto;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import fr.be.your.self.model.Slideshow;
import fr.be.your.self.model.SlideshowImage;

public class SlideshowDto extends BaseDto {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6033044975093317666L;

	private int id;
	
	private Date startDate;

	private List<SlideshowImage> images;
	
	private String link;
	private MultipartFile uploadImageFile;

	public SlideshowDto() {
		super();
		
		this.images = Collections.emptyList();
	}

	public SlideshowDto(Slideshow domain) {
		super(domain);
		
		if (domain != null) {
			this.id = domain.getId().intValue();
			this.startDate = domain.getStartDate();
			this.images = domain.getImages();
		}
		
		if (this.images == null) {
			this.images = Collections.emptyList();
		} else if (!this.images.isEmpty()) {
			Collections.sort(this.images, new Comparator<SlideshowImage>() {
				@Override
				public int compare(SlideshowImage image1, SlideshowImage image2) {
					return image1.getIndex() - image2.getIndex();
				}
			});
		}
	}

	public void copyToDomain(Slideshow domain) {
		domain.setStartDate(startDate);
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public List<SlideshowImage> getImages() {
		return images;
	}

	public void setImages(List<SlideshowImage> images) {
		this.images = images;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public MultipartFile getUploadImageFile() {
		return uploadImageFile;
	}

	public void setUploadImageFile(MultipartFile uploadImageFile) {
		this.uploadImageFile = uploadImageFile;
	}
}
