package com.aston.utils.mail;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class MailFactory {

	public String loadTemplate(String name) throws IOException {
		File f = new File(baseTemplatePath, name);
		return readFileContent(f, "utf-8");
	}

	public static String readFileContent(File f, String charset) throws IOException {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(f, "r");
			byte[] buffer = new byte[(int) raf.length()];
			raf.readFully(buffer);
			return new String(buffer, charset);
		} finally {
			if (raf != null)
				raf.close();
		}
	}

	public void sendMail(MailData mailData) throws Exception {
		try {
			// create a message
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(mailFrom));
			InternetAddress[] addressTo = new InternetAddress[mailData.mailTo.size()];
			for (int i = 0; i < mailData.mailTo.size(); i++) {
				addressTo[i] = new InternetAddress(mailData.mailTo.get(i));
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);
			if (mailData.replyTo != null)
				msg.setReplyTo(new InternetAddress[] { new InternetAddress(mailData.replyTo) });

			msg.setSubject(mailData.subject, "UTF-8");

			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();

			if (mailData.html) {
				mbp1.setContent(mailData.body, "text/html; charset=\"utf-8\"");
				mbp1.addHeader("MIME-Version", "1.0");
			} else {
				mbp1.setText(mailData.body);
			}

			// create the Multipart and add its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);

			if (mailData.attInfos != null) {
				for (AttInfo ai : mailData.attInfos) {
					// create the second message part
					MimeBodyPart mbpa = new MimeBodyPart();

					// attach the file to the message
					ByteArrayDataSource ds = new ByteArrayDataSource(ai.getContent(), ai.getMimeType());
					mbpa.setDataHandler(new DataHandler(ds));
					mbpa.setFileName(ai.getFileName());
					mp.addBodyPart(mbpa);
				}
			}

			// add the Multipart to the message
			msg.setContent(mp);

			// set the Date: header
			msg.setSentDate(new Date());

			// send the message
			Transport.send(msg);

		} catch (MessagingException mex) {
			Exception ex = null;
			if ((ex = mex.getNextException()) != null) {
				ex.printStackTrace();
			}
			throw new Exception("mail send error: " + mex.getMessage(), mex);
		}
	}

	private String baseTemplatePath;

	private Session session;

	private String mailFrom;

	public void setBaseTemplatePath(String baseTemplatePath) {
		this.baseTemplatePath = baseTemplatePath;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}
}
