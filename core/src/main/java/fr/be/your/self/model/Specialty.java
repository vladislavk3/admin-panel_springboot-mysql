package fr.be.your.self.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class Specialty {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "specialty")
	private User user;
	
	private boolean domicile;
	private boolean individual;
	private boolean grp;
	private boolean children;
	private boolean adult;
	private boolean pregnancy;
	private boolean sport;
	private boolean enterprise;
	private boolean teenager;

	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public boolean isDomicile() {
		return domicile;
	}
	public void setDomicile(boolean domicile) {
		this.domicile = domicile;
	}
	public boolean isIndividual() {
		return individual;
	}
	public void setIndividual(boolean individual) {
		this.individual = individual;
	}
	
	public boolean isGrp() {
		return grp;
	}
	public void setGrp(boolean grp) {
		this.grp = grp;
	}
	public boolean isChildren() {
		return children;
	}
	public void setChildren(boolean children) {
		this.children = children;
	}
	public boolean isAdult() {
		return adult;
	}
	public void setAdult(boolean adult) {
		this.adult = adult;
	}
	public boolean isPregnancy() {
		return pregnancy;
	}
	public void setPregnancy(boolean pregnancy) {
		this.pregnancy = pregnancy;
	}
	public boolean isSport() {
		return sport;
	}
	public void setSport(boolean sport) {
		this.sport = sport;
	}
	public boolean isEnterprise() {
		return enterprise;
	}
	public void setEnterprise(boolean enterprise) {
		this.enterprise = enterprise;
	}
	
	public boolean isTeenager() {
		return teenager;
	}
	public void setTeenager(boolean teenager) {
		this.teenager = teenager;
	}
}
