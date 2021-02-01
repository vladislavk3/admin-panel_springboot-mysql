package fr.be.your.self.backend.engine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import fr.be.your.self.engine.EmailSender;
import fr.be.your.self.util.StringUtils;

public class DefaultEmailSender implements EmailSender {

	private static final Logger logger = LoggerFactory.getLogger(DefaultEmailSender.class);

	@Autowired
	private MailSender mailSender;

	@Autowired
	private SimpleMailMessage defaultMessage;

	private String activateUserSubject;
	private String activateUserBody;

	private String forgotPasswordSubject;
	private String forgotPasswordBody;
	
	private String tempPasswordSubject;
	private String tempPasswordBody;	

	public DefaultEmailSender(String activateUserSubject, String activateUserBody, String forgotPasswordSubject,
			String forgotPasswordBody, String tempPasswordSubject, String tempPasswordBody) {
		this.activateUserSubject = activateUserSubject;
		this.activateUserBody = activateUserBody;
		this.forgotPasswordSubject = forgotPasswordSubject;
		this.forgotPasswordBody = forgotPasswordBody;
		this.tempPasswordSubject = tempPasswordSubject;
		this.tempPasswordBody = tempPasswordBody;
	}

	@Override
	public boolean send(String to, String subject, String body) {
		try {
			final SimpleMailMessage message = new SimpleMailMessage();
			this.defaultMessage.copyTo(message);

			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);

			this.mailSender.send(message);

			return true;
		} catch (Exception ex) {
			logger.error("Send email error", ex);
		}

		return false;
	}

	@Override
	public boolean sendActivateUser(String email, String activateUrl, String activateCode) {
		final String realUrl = activateUrl + (activateUrl.indexOf("?") > 0 ? "&" : "?") + "code=" + activateCode;

		final String mailBody = this.activateUserBody
				.replace("[ActivateCode]", activateCode)
				.replace("[ActivateUrl]", realUrl);

		if (JavaMailSender.class.isAssignableFrom(this.mailSender.getClass())) {
			try {
				final JavaMailSender javaMailSender = (JavaMailSender) this.mailSender;

				final MimeMessage message = javaMailSender.createMimeMessage();
				message.setSubject(this.activateUserSubject, "UTF-8");

				final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
				helper.setFrom(this.defaultMessage.getFrom());

				final String[] bcc = this.defaultMessage.getBcc();
				if (bcc != null && bcc.length > 0 && !StringUtils.isNullOrEmpty(bcc[0])) {
					helper.setBcc(bcc);
				}

				final String[] cc = this.defaultMessage.getCc();
				if (cc != null && cc.length > 0 && !StringUtils.isNullOrEmpty(cc[0])) {
					helper.setCc(cc);
				}

				helper.setTo(email);
				helper.setText(mailBody, true);

				javaMailSender.send(message);

				return true;
			} catch (MessagingException ex) {
				logger.error("Invalid email message", ex);
			} catch (Exception ex) {
				logger.error("Send email error", ex);
			}
		}

		return this.send(email, this.activateUserSubject, mailBody);
	}

	@Override
	public boolean sendForgotPassword(String email, String forgotPasswordUrl, String forgotPasswordCode) {
		final String realUrl = forgotPasswordUrl + (forgotPasswordUrl.indexOf("?") > 0 ? "&" : "?") + "code="
				+ forgotPasswordCode;

		final String mailBody = this.forgotPasswordBody
				.replace("[ForgotPasswordCode]", forgotPasswordCode)
				.replace("[ForgotPasswordUrl]", realUrl);

		if (JavaMailSender.class.isAssignableFrom(this.mailSender.getClass())) {
			try {
				final JavaMailSender javaMailSender = (JavaMailSender) this.mailSender;
				
				final MimeMessage message = javaMailSender.createMimeMessage();
				message.setSubject(this.forgotPasswordSubject, "UTF-8");

				final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
				helper.setFrom(defaultMessage.getFrom());

				final String[] bcc = defaultMessage.getBcc();
				if (bcc != null && bcc.length > 0 && !StringUtils.isNullOrEmpty(bcc[0])) {
					helper.setBcc(bcc);
				}

				final String[] cc = defaultMessage.getCc();
				if (cc != null && cc.length > 0 && !StringUtils.isNullOrEmpty(cc[0])) {
					helper.setCc(cc);
				}

				helper.setTo(email);
				helper.setText(mailBody, true);

				javaMailSender.send(message);

				return true;
			} catch (MessagingException ex) {
				logger.error("Invalid email message", ex);
			} catch (Exception ex) {
				logger.error("Send email error", ex);
			}
		}

		return this.send(email, this.forgotPasswordSubject, mailBody);
	}

	@Override
	public boolean sendTemporaryPassword(String email, String tempPassword) {
		final String mailBody = this.tempPasswordBody
				.replace("[TemporaryPassword]", tempPassword);
		
		if (JavaMailSender.class.isAssignableFrom(this.mailSender.getClass())) {
			try {
				final JavaMailSender javaMailSender = (JavaMailSender) this.mailSender;
				
				final MimeMessage message = javaMailSender.createMimeMessage();
				message.setSubject(this.tempPasswordSubject, "UTF-8");

				final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
				helper.setFrom(defaultMessage.getFrom());

				final String[] bcc = defaultMessage.getBcc();
				if (bcc != null && bcc.length > 0 && !StringUtils.isNullOrEmpty(bcc[0])) {
					helper.setBcc(bcc);
				}

				final String[] cc = defaultMessage.getCc();
				if (cc != null && cc.length > 0 && !StringUtils.isNullOrEmpty(cc[0])) {
					helper.setCc(cc);
				}

				helper.setTo(email);
				helper.setText(mailBody, true);

				javaMailSender.send(message);

				return true;
			} catch (MessagingException ex) {
				logger.error("Invalid email message", ex);
			} catch (Exception ex) {
				logger.error("Send email error", ex);
			}
		}

		return this.send(email, this.tempPasswordSubject, mailBody);
	}
}
