package fr.be.your.self.common;

public enum BusinessCodeDiscountType {
	PERCENTAGE(0),
	AMOUNT(1),
	UNKNOWN(-1);
	
	private final int value;
	
	BusinessCodeDiscountType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	public boolean isValid() {
		return this.value != UNKNOWN.value;
	}
	
	public static final BusinessCodeDiscountType parse(int value) {
		final BusinessCodeDiscountType[] values = BusinessCodeDiscountType.values();
		
		for (int i = 0; i < values.length; i++) {
			if (values[i].value == value) {
				return values[i];
			}
		}
		
		return UNKNOWN;
	}
}
