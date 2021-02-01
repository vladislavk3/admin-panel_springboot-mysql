package fr.be.your.self.backend.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import fr.be.your.self.model.Address;
import fr.be.your.self.model.Permission;
import fr.be.your.self.model.Price;
import fr.be.your.self.model.ProfessionalEvent;
import fr.be.your.self.model.Specialty;
import fr.be.your.self.model.Subscription;
import fr.be.your.self.model.User;
import fr.be.your.self.model.DegreeFile;
import fr.be.your.self.model.MediaFile;

public class UserDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4192799688425161368L;
	private int id;
	private int status;
	private String firstName;
	private String lastName;
	private String title;
	private String email;
	private int loginType;
	private String referralCode;
	private String userType;
	private List<Permission> permissions;
	private String activateCode;
	private long activateTimeout;
	private String subtype;
	
	//Additional fields for professionals
	private String phoneNumber;
	private String formation;
	private String website;
	private String facebook;
	private String linkedin;
	private boolean supervised;
	private String profilePicture;
	private String description;
	private Address address;
	private List<DegreeFile> degreeFiles = new ArrayList<>();
	
	private List<MediaFile> mediaFiles;

	private List<Price> prices = new ArrayList<>();
	private List<ProfessionalEvent> events = new ArrayList<>();
	
	
	private MultipartFile uploadImageFile;
	
	private List<MultipartFile> degrees;
	private List<MultipartFile> medias;

	private String school;
	private Subscription subscription;
	private Specialty specialty;
	

	public UserDto() {
	}
	public UserDto(User domain) {
		this();
	
		if (domain != null) {
			this.activateCode = domain.getActivateCode();
			this.activateTimeout = domain.getActivateTimeout();
			this.email = domain.getEmail();
			this.firstName = domain.getFirstName();
			this.id = domain.getId();
			this.lastName = domain.getLastName();
			this.loginType = domain.getLoginType();
			this.permissions = domain.getPermissions();
			this.referralCode = domain.getReferralCode();
			this.status = domain.getStatus();
			this.title = domain.getTitle();
			this.userType = domain.getUserType();	
			if (domain.getSubscription() != null && domain.getSubscription().getSubtype()!=null) {
				this.setSubtype(domain.getSubscription().getSubtype().getName());
			}
			
			this.phoneNumber = domain.getPhoneNumber();
			this.formation = domain.getFormation();
			this.website = domain.getWebsite();
			this.facebook = domain.getFacebook();
			this.linkedin = domain.getLinkedin();
			this.supervised = domain.isSupervised();
			this.profilePicture = domain.getProfilePicture();
			this.description = domain.getDescription();
			this.address = domain.getAddress();
			this.degreeFiles = domain.getDegreeFiles();
			this.mediaFiles = domain.getMediaFiles();
			this.events = domain.getEvents();		
			this.prices = domain.getPrices();
			this.school = domain.getSchool();
			this.subscription  = domain.getSubscription();
			this.specialty = domain.getSpecialty();
		}
	}
	
	public void copyToDomain(User domain) {
		domain.setEmail(this.email);
		domain.setFirstName(this.firstName);
		domain.setId(this.id);
		domain.setLastName(this.lastName);
		domain.setLoginType(this.loginType);
		domain.setReferralCode(this.referralCode);
		domain.setStatus(this.status);
		domain.setTitle(this.title);
		domain.setUserType(this.userType);
		domain.setStatus(status);
	}
	
	//Used in professional page
	public void copyToDomainOfProfessional(User domain) {
		domain.setEmail(this.email);
		domain.setFirstName(this.firstName);
		domain.setId(this.id);
		domain.setLastName(this.lastName);
		domain.setPhoneNumber(phoneNumber);
		domain.setFormation(formation);
		domain.setWebsite(website);
		domain.setFacebook(facebook);
		domain.setLinkedin(linkedin);
		domain.setSupervised(supervised);
		//domain.setProfilePicture(profilePicture);
		domain.setDescription(description);	
		domain.setSchool(school);
		domain.setAddress(address);
		domain.setSpecialty(specialty);
	}


	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	
	public int getLoginType() {
		return loginType;
	}
	public void setLoginType(int loginType) {
		this.loginType = loginType;
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
	public List<Permission> getPermissions() {
		return permissions;
	}
	public void setPermissions(List<Permission> permissions) {
		this.permissions = permissions;
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
	
	public String getFullname() {
		return firstName + " " + lastName;
	}
	public String getSubtype() {
		return subtype;
	}
	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getFormation() {
		return formation;
	}
	public void setFormation(String formation) {
		this.formation = formation;
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
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public List<DegreeFile> getUserFiles() {
		return degreeFiles;
	}
	public void setUserFiles(List<DegreeFile> userFiles) {
		this.degreeFiles = userFiles;
	}
	
	
	public MultipartFile getUploadImageFile() {
		return uploadImageFile;
	}
	public void setUploadImageFile(MultipartFile uploadImageFile) {
		this.uploadImageFile = uploadImageFile;
	}
	public List<Price> getPrices() {
		return prices;
	}
	public void setPrices(List<Price> prices) {
		this.prices = prices;
	}
	public List<MultipartFile> getDegrees() {
		return degrees;
	}
	public void setDegrees(List<MultipartFile> degrees) {
		this.degrees = degrees;
	}
	public String getSchool() {
		return school;
	}
	public void setSchool(String school) {
		this.school = school;
	}
	public List<MultipartFile> getMedias() {
		return medias;
	}
	public void setMedias(List<MultipartFile> medias) {
		this.medias = medias;
	}
	public List<MediaFile> getMediaFiles() {
		return mediaFiles;
	}
	public void setMediaFiles(List<MediaFile> mediaFiles) {
		this.mediaFiles = mediaFiles;
	}
	public List<DegreeFile> getDegreeFiles() {
		return degreeFiles;
	}
	public void setDegreeFiles(List<DegreeFile> degreeFiles) {
		this.degreeFiles = degreeFiles;
	}
	public List<ProfessionalEvent> getEvents() {
		return events;
	}
	public void setEvents(List<ProfessionalEvent> events) {
		this.events = events;
	}
	public Subscription getSubscription() {
		return subscription;
	}
	public void setSubscription(Subscription subscription) {
		this.subscription = subscription;
	}
	public Specialty getSpecialty() {
		return specialty;
	}
	public void setSpecialty(Specialty specialty) {
		this.specialty = specialty;
	}
}
