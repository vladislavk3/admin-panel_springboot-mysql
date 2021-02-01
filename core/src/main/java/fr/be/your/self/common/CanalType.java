package fr.be.your.self.common;

import java.util.ArrayList;
import java.util.List;

public enum CanalType {
	WEB("WEB"),
	APP("APP");
	
	private final String value;
	
	CanalType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	public static List<String> getPossibleStrValues(){
		List<String> res = new ArrayList<>();
		for (CanalType type : CanalType.values()) {
			res.add(type.getValue());
		}
		return res;
	}
}
