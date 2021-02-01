package fr.be.your.self.common;

import java.util.ArrayList;
import java.util.List;

public enum UserStatus {
	INACTIVE(0),
	ACTIVE(1),
	UNKNOWN(-1);
		
	private final int value;
	
	UserStatus(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	public boolean isValid() {
		return this.value != UNKNOWN.value;
	}
	
	public static final UserStatus parse(int value) {
		final UserStatus[] values = UserStatus.values();
		
		for (int i = 0; i < values.length; i++) {
			if (values[i].value == value) {
				return values[i];
			}
		}
		
		return UNKNOWN;
	}
	
	public static List<Integer> getPossibleIntValues(){
		List<Integer> res = new ArrayList<>();
		for (UserStatus userStatus : UserStatus.values()) {
			if (userStatus != UNKNOWN) {
				res.add(userStatus.getValue());
			}
		}
		return res;
	}
	
	public static List<String> getPossibleStrValues(){
		List<String> res = new ArrayList<>();
		for (UserStatus userStatus : UserStatus.values()) {
			if (userStatus != UNKNOWN) {
				res.add(String.valueOf(userStatus.getValue()));
			}
		}
		return res;
	}
	
	public static String getStatusDescription(int val) {
		switch (val) {
			case 0: return "INACTIVE";
			case 1: return "ACTIVE";
			default: return "UNKNOWN";	
		}		
	}
	
}

