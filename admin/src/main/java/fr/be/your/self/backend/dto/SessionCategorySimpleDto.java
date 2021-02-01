package fr.be.your.self.backend.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import fr.be.your.self.model.SessionCategory;

public class SessionCategorySimpleDto extends BaseDto {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1298542000833888431L;

	private int id;
	
	@NotEmpty(message = "{session.category.error.name.not.empty}")
	@NotNull(message = "{session.category.error.name.not.empty}")
	private String name;
	
	private String image;
	private int sessionCount;
	
	public SessionCategorySimpleDto() {
		super();
		
		this.sessionCount = 0;
	}

	public SessionCategorySimpleDto(SessionCategory domain) {
		super(domain);
		
		this.sessionCount = 0;
		if (domain != null) {
			this.id = domain.getId().intValue();
			this.name = domain.getName();
			this.image = domain.getImage();
			
			this.sessionCount = domain.getSessionCount();
			//final List<Session> sessions = domain.getSessions();
			//this.sessionCount = sessions == null ? 0 : sessions.size();
		}
	}
	
	public void copyToDomain(SessionCategory domain) {
		domain.setName(this.getName());
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public int getSessionCount() {
		return sessionCount;
	}

	public void setSessionCount(int sessionCount) {
		this.sessionCount = sessionCount;
	}
}
