package core.azure.spring.samples.messaging;

import java.io.File;
import java.util.List;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

public class SendMail {

	public void sendMailWithAttachment  (
						JavaMailSender jms,
						String to,
						String from,
						String subject,
						String body,
						List<String> attachments
				) throws MailException {
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws Exception {
				mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
				mimeMessage.setFrom(new InternetAddress(from));
				mimeMessage.setSubject(subject);
				mimeMessage.setText(body);
				
				for (String attachment : attachments) {
					FileSystemResource file = new FileSystemResource(new File(attachment));
					MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
					helper.addAttachment(attachment, file);
				}
			}
		};
		jms.send(preparator);			
	}
}
