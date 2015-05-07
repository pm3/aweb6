package com.aston.utils.mail;

import java.util.ArrayList;
import java.util.List;

public class MailData {
	List<String> mailTo = new ArrayList<String>();
	String replyTo = null;
	String subject = null;
	String body = null;
	List<AttInfo> attInfos = null;
	boolean html = false;

	public void addMailTo(String mail) {
		mailTo.add(mail);
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void addAtt(AttInfo att) {
		if (attInfos == null)
			attInfos = new ArrayList<AttInfo>();
		attInfos.add(att);
	}

	public void setHtml(boolean html) {
		this.html = html;
	}
}
