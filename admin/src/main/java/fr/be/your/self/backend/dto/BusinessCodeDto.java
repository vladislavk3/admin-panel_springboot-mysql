package fr.be.your.self.backend.dto;

import java.math.BigDecimal;
import java.util.Date;

import fr.be.your.self.common.BusinessCodeDiscountType;
import fr.be.your.self.common.BusinessCodeStatus;
import fr.be.your.self.common.BusinessCodeType;
import fr.be.your.self.model.BusinessCode;

public class BusinessCodeDto extends BaseDto {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8615171831998913884L;

	private Integer id;
	
	private String name;

	/**
	 * {@link BusinessCodeType#getValue()}
	 **/
	private int type;

	/**
	 * {@link BusinessCodeStatus#getValue()}
	 **/
	private int status;

	private Date startDate;

	private Date endDate;

	/**
	 * Beneficiary (company) for B2B
	 **/
	private String beneficiary; // company

	/**
	 * Maximum users amount for B2B
	 **/
	private int maxUserAmount;

	/**
	 * Number of times the code has been used for B2B
	 **/
	private int usedAmount;

	/**
	 * Price of the deal (euro) for B2B
	 **/
	private BigDecimal dealPrice;

	/**
	 * Price paid per user (recalculated if the price of the deal or the maximum
	 * amount of users is changed)
	 **/
	private BigDecimal pricePerUser;

	/**
	 * The type of reduction for B2C, with other is percentage always
	 * {@link BusinessCodeDiscountType#getValue()}
	 **/
	private int discountType;

	/**
	 * A value of reduction for B2C, with B2C100 is 100 always
	 **/
	private BigDecimal discountValue;

	private boolean editable;

	public BusinessCodeDto() {
		super();

		this.dealPrice = BigDecimal.ZERO;
		this.pricePerUser = BigDecimal.ZERO;
		this.editable = true;
	}

	public BusinessCodeDto(BusinessCode domain) {
		super(domain);

		this.dealPrice = BigDecimal.ZERO;
		this.pricePerUser = BigDecimal.ZERO;
		this.editable = true;

		if (domain != null) {
			this.id = domain.getId();
			this.name = domain.getName();
			this.type = domain.getType();
			this.status = domain.getStatus();
			this.beneficiary = domain.getBeneficiary();
			this.maxUserAmount = domain.getMaxUserAmount();
			//this.usedAmount = domain.getUsedAmount();
			this.dealPrice = domain.getDealPrice();
			this.pricePerUser = domain.getPricePerUser();
			this.discountType = domain.getDiscountType();
			this.discountValue = domain.getDiscountValue();
			this.startDate = domain.getStartDate();
			this.endDate = domain.getEndDate();
		}
	}

	public void copyToDomain(BusinessCode domain, boolean createMode) {
		domain.setStatus(status);
		domain.setBeneficiary(beneficiary);
		domain.setMaxUserAmount(maxUserAmount);
		//domain.setUsedAmount(usedAmount);
		domain.setDealPrice(dealPrice);
		domain.setDiscountType(discountType);
		domain.setDiscountValue(discountValue);
		domain.setStartDate(startDate);
		domain.setEndDate(endDate);
		domain.setName(name);
		
		if (createMode) {
			domain.setType(type);			
		}
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getBeneficiary() {
		return beneficiary;
	}

	public void setBeneficiary(String beneficiary) {
		this.beneficiary = beneficiary;
	}

	public int getMaxUserAmount() {
		return maxUserAmount;
	}

	public void setMaxUserAmount(int maxUserAmount) {
		this.maxUserAmount = maxUserAmount;
	}

	public int getUsedAmount() {
		return usedAmount;
	}

	public void setUsedAmount(int usedAmount) {
		this.usedAmount = usedAmount;
	}

	public BigDecimal getDealPrice() {
		return dealPrice;
	}

	public void setDealPrice(BigDecimal dealPrice) {
		this.dealPrice = dealPrice;
	}

	public BigDecimal getPricePerUser() {
		return pricePerUser;
	}

	public void setPricePerUser(BigDecimal pricePerUser) {
		this.pricePerUser = pricePerUser;
	}

	public int getDiscountType() {
		return discountType;
	}

	public void setDiscountType(int discountType) {
		this.discountType = discountType;
	}

	public BigDecimal getDiscountValue() {
		return discountValue;
	}

	public void setDiscountValue(BigDecimal discountValue) {
		this.discountValue = discountValue;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
}
