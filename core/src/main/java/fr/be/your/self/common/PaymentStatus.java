package fr.be.your.self.common;

import java.util.ArrayList;
import java.util.List;

public enum PaymentStatus {
	PENDING(0),
	VALIDATED(1),
	FAILED(2),
	UNKNOWN(-1);
	
	private final int value;
	
	PaymentStatus(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	
	public static final PaymentStatus parse(int value) {
		final PaymentStatus[] values = PaymentStatus.values();
		
		for (int i = 0; i < values.length; i++) {
			if (values[i].value == value) {
				return values[i];
			}
		}
		
		return UNKNOWN;
	}
	
	public static List<Integer> getPossibleIntValues(){
		List<Integer> res = new ArrayList<>();
		for (PaymentStatus status : PaymentStatus.values()) {
			if (status != UNKNOWN) {
				res.add(status.getValue());
			}
		}
		
		return res;
	}
	
}
