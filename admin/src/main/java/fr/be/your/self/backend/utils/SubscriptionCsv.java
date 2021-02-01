package fr.be.your.self.backend.utils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import fr.be.your.self.common.BusinessCodeType;
import fr.be.your.self.model.Subscription;

public class SubscriptionCsv {
	public static String DATE_FORMAT = "dd-MM-yyyy";

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
	
	@CsvBindByName(column = "Type d'abonnement", required = true)
	@CsvBindByPosition(position = 4)
	private String subtype;
	
	@CsvBindByName(column = "Canal", required = true)
	@CsvBindByPosition(position = 5)
	private String canal;
	
	@CsvBindByName(column = "Code")
	@CsvBindByPosition(position = 6)
	private String code;
	
	@CsvBindByName(column = "Code type")	
	@CsvBindByPosition(position = 7)
	private String codeType;
	
	@CsvBindByName(column = "Durée", required = true)
	@CsvBindByPosition(position = 8)
	private int duration;
	
	@CsvBindByName(column = "Date de début", required = true)
	@CsvBindByPosition(position = 9)
	private String startDate;
	
	@CsvBindByName(column = "Date de fin", required = true)	
	@CsvBindByPosition(position = 10)
	private String endDate;
	
	@CsvBindByName(column = "Résiliation demandée", required = true)
	@CsvBindByPosition(position = 11)
	private boolean terminationAsked;
	
	@CsvBindByName(column = "Prix", required = true)
	@CsvBindByPosition(position = 12)
	private BigDecimal price;
	
	@CsvBindByName(column = "Cadeau", required = true)
	@CsvBindByPosition(position = 13)
	private boolean giftCard;
	
	@CsvBindByName(column = "Payment gateway", required = true)
	@CsvBindByPosition(position = 14)
	private String paymenGateway;
	
	public SubscriptionCsv() {}
	
	public SubscriptionCsv(Subscription subscription) {
		this.title = subscription.getUser().getTitle();
		this.firstName = subscription.getUser().getFirstName();
		this.lastName = subscription.getUser().getLastName();
		this.email = subscription.getUser().getEmail();
		this.subtype = subscription.getSubtype().getName();
		this.canal = subscription.getCanal();
		if (subscription.getBusinessCode() != null) {
			this.code = subscription.getBusinessCode().getName();
			this.codeType = BusinessCodeType.toStrValue(subscription.getBusinessCode().getType());
		}
		this.duration = subscription.getDuration();
		
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		if (subscription.getSubscriptionStartDate() != null) {
			this.startDate = sdf.format(subscription.getSubscriptionStartDate());
		}
		if (subscription.getSubscriptionEndDate()  != null) {
			this.endDate = sdf.format(subscription.getSubscriptionEndDate());
		}
		
		this.terminationAsked = subscription.isTerminationAsked();
		this.price = subscription.getPrice();
		this.paymenGateway = subscription.getPaymentGateway();
		this.giftCard = subscription.isGiftCard();
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

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public String getCanal() {
		return canal;
	}

	public void setCanal(String canal) {
		this.canal = canal;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCodeType() {
		return codeType;
	}

	public void setCodeType(String codeType) {
		this.codeType = codeType;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public boolean isTerminationAsked() {
		return terminationAsked;
	}

	public void setTerminationAsked(boolean terminationAsked) {
		this.terminationAsked = terminationAsked;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public boolean isGiftCard() {
		return giftCard;
	}

	public void setGiftCard(boolean giftCard) {
		this.giftCard = giftCard;
	}

	public String getPaymenGateway() {
		return paymenGateway;
	}

	public void setPaymenGateway(String paymenGateway) {
		this.paymenGateway = paymenGateway;
	}

	public String getFullName() {
		return firstName + " " + lastName;
	}
	
	
	
	
}
