package fr.be.your.self.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Slideshow extends PO<Integer> {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "startDate", nullable = true)
	private Date startDate;
	
	//@Temporal(TemporalType.TIMESTAMP)
    //@Column(name = "endDate", nullable = true)
	//private Date endDate;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "slideshow")
    private List<SlideshowImage> images;
	
	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public String getDisplay() {
		return "" + id;
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
	
	/*
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	*/
	
	public List<SlideshowImage> getImages() {
		return images;
	}

	public void setImages(List<SlideshowImage> images) {
		this.images = images;
	}
}
