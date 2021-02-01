package fr.be.your.self.common;

import java.util.ArrayList;
import java.util.List;

public enum PaymentGateway {
	STRIPE("STRIPE"),
	PAYPAL("PAYPAL"),
	B2B("B2B"),
	INAPP("INAPP");
	
	private final String value;
	
	PaymentGateway(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	public static List<String> getPossibleStrValues(){
		List<String> res = new ArrayList<>();
		for (PaymentGateway status : PaymentGateway.values()) {
			res.add(status.getValue());
		}
		
		return res;
	}
}
