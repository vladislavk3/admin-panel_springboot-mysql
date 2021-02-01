package fr.be.your.self.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
public class Gift extends PO<Integer> {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name = "Name", length = 120)
	@NotEmpty(message = "{gift.error.name.not.empty}")
	@NotNull(message = "{gift.error.name.not.empty}")
	private String name;
	
	@Column(name = "Duration")
	private int duration;

	@Lob
	@Column(name = "Description")
	private String description;
	
	@Column(name = "Price")
	private BigDecimal price;

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public String getDisplay() {
		return name;
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

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	
	public void copy(Gift domain) {
		domain.setName(name);
		domain.setDuration(duration);
		domain.setDescription(description);
		domain.setPrice(price);
	}
}
