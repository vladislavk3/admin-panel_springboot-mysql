package fr.be.your.self.backend.dto;

import java.io.Serializable;

import fr.be.your.self.model.PO;

public class IdNameDto<ID extends Serializable> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6593294570689278955L;

	private ID id;
	private String name;

	public IdNameDto() {
		super();
	}

	public IdNameDto(PO<ID> domain) {
		super();
		
		if (domain != null) {
			this.id = domain.getId();
			this.name = domain.getDisplay();
		}
	}
	
	public IdNameDto(ID id, String name) {
		super();
		this.id = id;
		this.name = name;
	}


	public ID getId() {
		return id;
	}

	public void setId(ID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
