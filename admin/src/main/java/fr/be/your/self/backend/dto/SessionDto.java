package fr.be.your.self.backend.dto;

import org.springframework.web.multipart.MultipartFile;

import fr.be.your.self.model.Session;

public class SessionDto extends SessionSimpleDto {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3830533931201988167L;

	private MultipartFile uploadImageFile;
	private MultipartFile uploadContentFile;

	public SessionDto() {
		super();
	}

	public SessionDto(Session domain) {
		super(domain);
	}

	public MultipartFile getUploadImageFile() {
		return uploadImageFile;
	}

	public void setUploadImageFile(MultipartFile uploadImageFile) {
		this.uploadImageFile = uploadImageFile;
	}

	public MultipartFile getUploadContentFile() {
		return uploadContentFile;
	}

	public void setUploadContentFile(MultipartFile uploadContentFile) {
		this.uploadContentFile = uploadContentFile;
	}
}
