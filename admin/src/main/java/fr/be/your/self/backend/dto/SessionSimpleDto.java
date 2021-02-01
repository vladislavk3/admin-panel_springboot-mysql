package fr.be.your.self.backend.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import fr.be.your.self.model.Session;
import fr.be.your.self.model.SessionCategory;
import fr.be.your.self.model.User;
import fr.be.your.self.util.StringUtils;

public class SessionSimpleDto extends BaseDto {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6485158892522293486L;

	private int id;

	private List<Integer> categoryIds;
	private List<SessionCategory> categories;
	
	private Integer voiceId;
	private String voiceName;
	
	@NotEmpty(message = "{session.error.title.not.empty}")
	@NotNull(message = "{session.error.title.not.empty}")
	private String title;

	private String image;

	private String contentFile;
	private String contentFileType;
	private String contentMimeType;

	private String duration;

	private String description;

	private boolean free;

	public SessionSimpleDto() {
		super();

		this.free = false;
		this.categoryIds = new ArrayList<Integer>();
	}

	public SessionSimpleDto(Session domain) {
		super(domain);

		this.free = false;
		this.categoryIds = new ArrayList<Integer>();
		
		if (domain != null) {
			this.id = domain.getId().intValue();
			this.title = domain.getTitle();
			this.image = domain.getImage();
			this.contentFile = domain.getContentFile();
			this.contentMimeType = domain.getContentMimeType();
			this.description = domain.getDescription();
			this.free = domain.isFree();
			this.categories = domain.getCategories();
			this.duration = StringUtils.formatDuration(domain.getDuration());
			
			final User voice = domain.getVoice();
			if (voice != null) {
				this.voiceId = voice.getId();
				this.voiceName = voice.getDisplay();
			}
			
			if (this.categories != null) {
				for (SessionCategory category : this.categories) {
					this.categoryIds.add(category.getId());
				}
			}
		}
	}

	public void copyToDomain(Session domain) {
		domain.setTitle(this.title);
		domain.setDescription(this.description);
		domain.setFree(this.free);
		domain.setDuration(StringUtils.parseDuration(this.duration));
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<Integer> getCategoryIds() {
		return categoryIds;
	}

	public void setCategoryIds(List<Integer> categoryIds) {
		this.categoryIds = categoryIds;
	}

	public List<SessionCategory> getCategories() {
		return categories;
	}

	public void setCategories(List<SessionCategory> categories) {
		this.categories = categories;
	}

	public Integer getVoiceId() {
		return voiceId;
	}

	public void setVoiceId(Integer voiceId) {
		this.voiceId = voiceId;
	}

	public String getVoiceName() {
		return voiceName;
	}

	public void setVoiceName(String voiceName) {
		this.voiceName = voiceName;
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

	public String getContentFileType() {
		return contentFileType;
	}

	public void setContentFileType(String contentFileType) {
		this.contentFileType = contentFileType;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
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
}
