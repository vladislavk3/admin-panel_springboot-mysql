package fr.be.your.self.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum BusinessCodeType {
	B2B_MULTIPLE(0),
	B2B_UNIQUE(1),
	B2C_DISCOUNT_100(2),
	B2C_DISCOUNT(3),
	GIFT_CARD(4),
	UNKNOWN(-1);
	
	static Map<Integer, String> intVal2StrMap= new HashMap<>();
	
	static {
		intVal2StrMap.put(0, "B2B_MULTIPLE");
		intVal2StrMap.put(1, "B2B_UNIQUE");
		intVal2StrMap.put(2, "B2C_DISCOUNT_100");
		intVal2StrMap.put(3, "B2C_DISCOUNT");
		intVal2StrMap.put(4, "GIFT_CARD");
		intVal2StrMap.put(-1, "UNKNOWN");

	}
	
	private final int value;
	
	BusinessCodeType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	public boolean isValid() {
		return this.value != UNKNOWN.value;
	}
	
	public static final BusinessCodeType parse(int value) {
		final BusinessCodeType[] values = BusinessCodeType.values();
		
		for (int i = 0; i < values.length; i++) {
			if (values[i].value == value) {
				return values[i];
			}
		}
		
		return UNKNOWN;
	}
	
	public static List<Integer> getPossibleIntValues(){
		List<Integer> res = new ArrayList<>();
		for (BusinessCodeType status : BusinessCodeType.values()) {
			if (status != UNKNOWN) {
				res.add(status.getValue());
			}
		}
		
		return res;
	}
	
	public static String toStrValue(int type) {
		return intVal2StrMap.get(type);
	}
}
