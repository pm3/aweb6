package com.aston.utils.jsp;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.aston.utils.servlet.format.Formatter;
import com.aston.utils.servlet.provider.ProviderFilter;

public class FormatTag extends BodyTagSupport {

	private static final long serialVersionUID = 1L;

	private Object val = null;
	private String style = null;
	private boolean raw = false;

	public void setVal(Object val) {
		this.val = val;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public void setRaw(boolean raw) {
		this.raw = raw;
	}

	@Override
	public void release() {
		super.release();
		this.val = null;
		this.style = null;
		this.raw = false;
	}

	@Override
	public int doStartTag() throws JspException {
		try {
			Formatter f = (Formatter) pageContext.getRequest().getAttribute(ProviderFilter.FKEY);
			if (f == null)
				throw new IOException("request has not initialized Formatter");
			String sval = f.format(val, style);
			if (sval != null) {
				if (raw)
					pageContext.getOut().write(sval);
				else
					escapeXml(sval, pageContext.getOut());
			}

		} catch (IOException ex) {
			throw new JspException(ex.toString(), ex);
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws JspException {
		return EVAL_PAGE;
	}

	public static void escapeXml(String s, Writer w) throws IOException {
		int max = s.length();
		for (int i = 0; i < max; i++) {
			char ch = s.charAt(i);
			if (ch == '&')
				w.append("&amp;");
			else if (ch == '<')
				w.append("&lt;");
			else if (ch == '>')
				w.append("&gt;");
			else if (ch == '"')
				w.append("&#034;");
			else if (ch == '\'')
				w.append("&#039;");
			else
				w.append(ch);
		}
	}
}
