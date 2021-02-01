package fr.be.your.self.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
public class SessionCategory extends PO<Integer> {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name = "Name", length = 120)
	@NotEmpty(message = "{session.category.error.name.not.empty}")
	@NotNull(message = "{session.category.error.name.not.empty}")
	private String name;
	
	@Column(name = "Image", length = 255)
	private String image;

	@Column(name = "SessionCount")
	private int sessionCount;
	
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "categories")
    private List<Session> sessions;
	
	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public String getDisplay() {
		return this.name;
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
	
	public List<Session> getSessions() {
		return sessions;
	}

	public void setSessions(List<Session> sessions) {
		this.sessions = sessions;
	}
	
	public int getSessionCount() {
		return sessionCount;
	}

	public void setSessionCount(int sessionCount) {
		this.sessionCount = sessionCount;
	}

	@PrePersist
	protected void onCreate() {
		this.sessionCount = this.sessions == null ? 0 : this.sessions.size();
	}

	@PreUpdate
	protected void onUpdate() {
		this.sessionCount = this.sessions == null ? 0 : this.sessions.size();
	}
}
