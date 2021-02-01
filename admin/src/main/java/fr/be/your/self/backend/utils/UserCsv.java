package fr.be.your.self.backend.utils;


import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class UserCsv {
	//CsvBindByName => Used when we import CSV file
	//CsvBindByPosition => Used when we export CSV file
	
	@CsvBindByName(column = "Civilité", required = true)
    @CsvBindByPosition(position = 0)
	private String title;
	
	@CsvBindByName(column = "Nom", required = true)
    @CsvBindByPosition(position = 1)
	private String lastName;
	
	@CsvBindByName(column = "Prénom", required = true)
    @CsvBindByPosition(position = 2)
	private String firstName;
	
	@CsvBindByName(column = "Email", required = true)
    @CsvBindByPosition(position = 3)
	private String email;
	
	@CsvBindByName(column = "Login type", required = true)	
    @CsvBindByPosition(position = 4)
	private String loginType;
	
	@CsvBindByName(column = "Statut", required = true)
    @CsvBindByPosition(position = 5)
	private String status;
	
	@CsvBindByName(column = "Code parainnage")	
    @CsvBindByPosition(position = 6)
	private String referralCode;
	
	@CsvBindByName(column = "Role", required = true)	
    @CsvBindByPosition(position = 7)
	private String userType;

	@CsvBindByName(column = "Abonnement")	
    @CsvBindByPosition(position = 8)
	private String subscriptionType;
	
	public UserCsv() {
		
	}
	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getStatus() {
		return status;
	}

	
	public void setStatus(String status) {
		this.status = status;
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
	

	public String getSubscriptionType() {
		return subscriptionType;
	}

	public void setSubscriptionType(String subscriptionType) {
		this.subscriptionType = subscriptionType;
	}
	
	public String getLoginType() {
		return loginType;
	}

	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}

}
