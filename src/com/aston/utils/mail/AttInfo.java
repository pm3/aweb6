package com.aston.utils.mail;

public class AttInfo {
	private final String fileName;

	private final String mimeType;

	private final byte[] content;

	public AttInfo(String fileName, String mimeType, byte[] content) {
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.content = content;
	}

	public String getFileName() {
		return fileName;
	}

	public String getMimeType() {
		return mimeType;
	}

	public byte[] getContent() {
		return content;
	}
}
