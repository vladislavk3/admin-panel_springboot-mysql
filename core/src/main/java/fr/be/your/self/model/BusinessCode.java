package fr.be.your.self.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import fr.be.your.self.common.BusinessCodeDiscountType;
import fr.be.your.self.common.BusinessCodeStatus;
import fr.be.your.self.common.BusinessCodeType;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "Name" }))
public class BusinessCode extends PO<Integer> {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name = "Name", length = 60)
	private String name;

	/**
	 * {@link BusinessCodeType#getValue()}
	 **/
	private int type;

	/**
	 * {@link BusinessCodeStatus#getValue()}
	 **/
	private int status;

	@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "startDate", nullable = true)
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "endDate", nullable = true)
	private Date endDate;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "businessCode" , cascade = CascadeType.ALL, orphanRemoval = true )
	private List<Subscription> subscriptions;

	/**
	 * Beneficiary (company) for B2B
	 **/
	private String beneficiary; // company

	/**
	 * Maximum users amount for B2B, with B2C always be 1
	 **/
	private int maxUserAmount;

	/**
	 * Number of times the code has been used for B2B
	 **/
	//private int usedAmount;

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

	@Override
	public Integer getId() {
		return this.id;
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
	
	/*
	public int getUsedAmount() {
		return usedAmount;
	}

	public void setUsedAmount(int usedAmount) {
		this.usedAmount = usedAmount;
	}
	*/
	
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
	
	@PrePersist
	@PreUpdate
	protected void calcPricePerUser() {
		pricePerUser = BigDecimal.ZERO;
		
		if (dealPrice != null && dealPrice.signum() > 0 && maxUserAmount > 0) {
			pricePerUser = dealPrice.divide(new BigDecimal(maxUserAmount), 2, BigDecimal.ROUND_HALF_UP);
		}
	}
	
	public boolean isB2B() {
		return this.getType() == BusinessCodeType.B2B_MULTIPLE.getValue()
				|| this.getType() == BusinessCodeType.B2B_UNIQUE.getValue();
	}
	
	public boolean isB2B_unique() {
		return this.getType() == BusinessCodeType.B2B_UNIQUE.getValue();
	}
			
}
