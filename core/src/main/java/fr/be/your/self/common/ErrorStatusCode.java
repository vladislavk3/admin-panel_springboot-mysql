package fr.be.your.self.common;

public enum ErrorStatusCode {
	
	UNAUTHORIZED("unauthorized"),
	INVALID_CREDENTIALS("invalid.credentials"),
	INVALID_PERMISSION("invalid.permission"),
	
	PROCESSING_ERROR("processing.error"),
	
	INVALID_ID("invalid.id"),
	INVALID_PARAMETER("invalid.parameter"),
	
	USERNAME_EXISTED("username.existed"),
	
	SUBSCRIPTION_EXISTED("subscription.existed");
	
	private final String value;

	private ErrorStatusCode(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
