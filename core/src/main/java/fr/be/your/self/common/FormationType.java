package fr.be.your.self.common;

import java.util.ArrayList;
import java.util.List;

public enum FormationType {
	LICENCE("LICENCE"),
	MASTER("MASTER"),
	OTHER("OTHER"),
	UNKNOWN("UNKNOWN");
		
	private final String value;
	
	FormationType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	
	public static final FormationType parse(String value) {
		if (value == null || value.isEmpty()) {
			return UNKNOWN;
		}
		
		final FormationType[] values = FormationType.values();
		for (int i = 0; i < values.length; i++) {
			if (value.equalsIgnoreCase(values[i].value)) {
				return values[i];
			}
		}
		
		return UNKNOWN;
	}

	
	public static List<String> getPossibleStrValues(){
		List<String> res = new ArrayList<>();
		for (FormationType formationType : FormationType.values()) {
			if (formationType != UNKNOWN) {
				res.add(formationType.getValue());
			}
		}
		return res;
	}
	
}
