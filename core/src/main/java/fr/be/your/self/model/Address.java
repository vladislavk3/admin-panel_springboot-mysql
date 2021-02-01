package fr.be.your.self.model;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class Address {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String address;
	private BigDecimal longitude;
	private BigDecimal latitude;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "address")
	private User user;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "address")
	private ProfessionalEvent event;

	public Address() {}
	
	public Address(String addr) {
		this.address = addr;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public BigDecimal getLongitude() {
		return longitude;
	}
	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}
	public BigDecimal getLatitude() {
		return latitude;
	}
	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public ProfessionalEvent getEvent() {
		return event;
	}
	public void setEvent(ProfessionalEvent event) {
		this.event = event;
	}
	
	public static Address newAddress(String addr) {
		Address address = new Address(addr);
		return address;
	}
	
}
