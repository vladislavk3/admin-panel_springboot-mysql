package fr.be.your.self.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
public class Session extends PO<Integer> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "Title", length = 255)
	@NotEmpty(message = "{session.error.title.not.empty}")
	@NotNull(message = "{session.error.title.not.empty}")
	private String title;
	
	@Column(name = "Image", length = 255)
	private String image;

	@Column(name = "ContentFile", length = 255)
	private String contentFile;

	@Column(name = "ContentMimeType", length = 50)
	private String contentMimeType;
	
	@Column(name = "Duration")
	private int duration;

	@Lob
	@Column(name = "Description")
	private String description;

	@Column(name = "Free")
	private boolean free;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "Session_Categories", 
			joinColumns = @JoinColumn(name = "SessionId"), 
			inverseJoinColumns = @JoinColumn(name = "CategoryId"))
    private List<SessionCategory> categories;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "voiceId")
	private User voice;
	
	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public String getDisplay() {
		return this.title;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getContentFile() {
		return contentFile;
	}

	public void setContentFile(String contentFile) {
		this.contentFile = contentFile;
	}

	public String getContentMimeType() {
		return contentMimeType;
	}

	public void setContentMimeType(String contentMimeType) {
		this.contentMimeType = contentMimeType;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isFree() {
		return free;
	}

	public void setFree(boolean free) {
		this.free = free;
	}

	public List<SessionCategory> getCategories() {
		return categories;
	}

	public void setCategories(List<SessionCategory> categories) {
		this.categories = categories;
	}

	public User getVoice() {
		return voice;
	}

	public void setVoice(User voice) {
		this.voice = voice;
	}
}
