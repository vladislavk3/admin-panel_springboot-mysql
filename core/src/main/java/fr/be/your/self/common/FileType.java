package fr.be.your.self.common;

import java.util.ArrayList;
import java.util.List;

public enum FileType {
	DEGREE(0),
	MEDIA(1),
	UNKNOWN(-1);
		
	private final int value;
	
	FileType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	public boolean isValid() {
		return this.value != UNKNOWN.value;
	}
	
	public static final FileType parse(int value) {
		final FileType[] values = FileType.values();
		
		for (int i = 0; i < values.length; i++) {
			if (values[i].value == value) {
				return values[i];
			}
		}
		
		return UNKNOWN;
	}
	
	public static List<Integer> getPossibleIntValues(){
		List<Integer> res = new ArrayList<>();
		for (FileType fileType : FileType.values()) {
			if (fileType != UNKNOWN) {
				res.add(fileType.getValue());
			}
		}
		return res;
	}
	
	
}

