package fr.be.your.self.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.be.your.self.common.UserStatus;
import fr.be.your.self.common.UserType;
import fr.be.your.self.common.Utils;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "email" }))
public class User extends PO<Integer> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private int status;

	private String firstName;

	private String lastName;

	private String title;

	@Column(name = "email", length = 255)
	private String email;

	@JsonIgnore
	private String password;

	private String socialLogin;

	private int loginType;

	private String referralCode;

	private String userType;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Permission> permissions;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Subscription> subscriptions;

	public List<Subscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(List<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	private String activateCode;
	private long activateTimeout;

	private String resetPasswordCode;
	private long resetPasswordTimeout;

	/************* Fields for professional ****************/
	private String phoneNumber;
	private String formation;
	private String website;
	private String facebook;
	private String linkedin;
	private boolean supervised;
	private String profilePicture;
	private String school;

	@Lob
	private String description;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "address_id")
	private Address address;
	
	@OneToOne(fetch = FetchType.LAZY, cascade =  {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
	@JoinColumn(name = "specialty_id")
	private Specialty specialty;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user" , cascade = CascadeType.ALL, orphanRemoval = true )
	private List<Price> prices;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DegreeFile> degreeFiles;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MediaFile> mediaFiles;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProfessionalEvent> events;

	public User() {
	}

	public User(String email, String encodedPassword, String userType, String firstName, String lastName) {
		this.email = email;
		this.password = encodedPassword;
		this.userType = userType;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public String getDisplay() {
		return this.getFullName();
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getFullName() {
		return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
	}

	public List<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<Permission> permissions) {
		this.permissions = permissions;
	}

	public List<DegreeFile> getDegreeFiles() {
		return degreeFiles;
	}

	public void setDegreeFiles(List<DegreeFile> degreeFiles) {
		this.degreeFiles = degreeFiles;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSocialLogin() {
		return socialLogin;
	}

	public void setSocialLogin(String socialLogin) {
		this.socialLogin = socialLogin;
	}

	public String getReferralCode() {
		return referralCode;
	}

	public void setReferralCode(String referralCode) {
		this.referralCode = referralCode;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public int getLoginType() {
		return loginType;
	}

	public void setLoginType(int loginType) {
		this.loginType = loginType;
	}

	public String getActivateCode() {
		return activateCode;
	}

	public void setActivateCode(String activateCode) {
		this.activateCode = activateCode;
	}

	public long getActivateTimeout() {
		return activateTimeout;
	}

	public void setActivateTimeout(long activateTimeout) {
		this.activateTimeout = activateTimeout;
	}

	public String getResetPasswordCode() {
		return resetPasswordCode;
	}

	public void setResetPasswordCode(String resetPasswordCode) {
		this.resetPasswordCode = resetPasswordCode;
	}

	public long getResetPasswordTimeout() {
		return resetPasswordTimeout;
	}

	public void setResetPasswordTimeout(long resetPasswordTimeout) {
		this.resetPasswordTimeout = resetPasswordTimeout;
	}

	public String getStatusDescription() {
		return UserStatus.getStatusDescription(status);
	}

	public String getUserTypeDescription() {
		return UserType.getStatusDescription(userType);
	}

	public Subscription getSubscription() {
		Subscription sub = Utils.findSubscriptionUser(this);
		return sub;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public String getFormation() {
		return formation;
	}

	public void setFormation(String formation) {
		this.formation = formation;
	}

	public boolean isSupervised() {
		return supervised;
	}

	public void setSupervised(boolean supervised) {
		this.supervised = supervised;
	}

	public String getProfilePicture() {
		return profilePicture;
	}

	public void setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<ProfessionalEvent> getEvents() {
		return events;
	}

	public void setEvents(List<ProfessionalEvent> events) {
		this.events = events;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getFacebook() {
		return facebook;
	}

	public void setFacebook(String facebook) {
		this.facebook = facebook;
	}

	public String getLinkedin() {
		return linkedin;
	}

	public void setLinkedin(String linkedin) {
		this.linkedin = linkedin;
	}

	public List<Price> getPrices() {
		return prices;
	}

	public void setPrices(List<Price> prices) {
		this.prices = prices;
	}

	public String getSchool() {
		return school;
	}

	public void setSchool(String school) {
		this.school = school;
	}

	public List<MediaFile> getMediaFiles() {
		return mediaFiles;
	}

	public void setMediaFiles(List<MediaFile> mediaFiles) {
		this.mediaFiles = mediaFiles;
	}
	
	
	public Specialty getSpecialty() {
		return specialty;
	}

	public void setSpecialty(Specialty specialty) {
		this.specialty = specialty;
	}
	
}