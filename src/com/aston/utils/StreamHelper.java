package com.aston.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamHelper {

	public static void copy(InputStream is, OutputStream os, int bufSize) throws IOException {
		byte[] buf = new byte[bufSize];
		int len = 0;

		while ((len = is.read(buf)) > 0) {
			os.write(buf, 0, len);
		}
	}

	public static byte[] stream2bytea(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
		copy(is, bos, 512);
		return bos.toByteArray();
	}

	public static byte[] file2bytea(File f) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			copy(fis, bos, 512);
			fis.close();
		} catch (IOException e) {
			if (fis != null)
				fis.close();
			throw e;
		}
		return bos.toByteArray();
	}

	public static String stream2String(InputStream is, String encoding) throws IOException {
		byte[] data = stream2bytea(is);
		return encoding != null ? new String(data, encoding) : new String(data);
	}

	public static String file2String(File f, String encoding) throws IOException {
		byte[] data = file2bytea(f);
		return encoding != null ? new String(data, encoding) : new String(data);
	}

}
