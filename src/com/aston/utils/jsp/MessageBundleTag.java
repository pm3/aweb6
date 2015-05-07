package com.aston.utils.jsp;

import java.io.IOException;
import java.text.MessageFormat;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.aston.utils.servlet.format.Formatter;
import com.aston.utils.servlet.provider.ProviderFilter;

public class MessageBundleTag extends BodyTagSupport {

	private static final long serialVersionUID = 1L;

	private String key = null;
	private Object[] vals = new Object[5];
	private String[] styles = new String[5];
	private boolean useVals = false;

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public void release() {
		super.release();
		this.key = null;
		for (int i = 0; i < 5; i++) {
			this.vals[i] = null;
			this.styles[i] = null;
		}
		this.useVals = false;
	}

	public void setVal0(Object val) {
		this.vals[0] = val;
		this.useVals = true;
	}

	public void setVal1(Object val) {
		this.vals[1] = val;
		this.useVals = true;
	}

	public void setVal2(Object val) {
		this.vals[2] = val;
		this.useVals = true;
	}

	public void setVal3(Object val) {
		this.vals[3] = val;
		this.useVals = true;
	}

	public void setVal4(Object val) {
		this.vals[4] = val;
		this.useVals = true;
	}

	public void setStyle0(String s) {
		this.styles[0] = s;
	}

	public void setStyle1(String s) {
		this.styles[1] = s;
	}

	public void setStyle2(String s) {
		this.styles[2] = s;
	}

	public void setStyle3(String s) {
		this.styles[3] = s;
	}

	public void setStyle4(String s) {
		this.styles[4] = s;
	}

	@Override
	public int doStartTag() throws JspException {
		try {
			Formatter f = (Formatter) pageContext.getRequest().getAttribute(ProviderFilter.FKEY);
			if (f == null)
				throw new IOException("request has not initialized Formatter");
			String v = f.mb(key);
			if (useVals) {
				for (int i = 0; i < 5; i++) {
					if (vals[i] != null)
						vals[i] = f.format(vals[i], styles[i]);
				}
				v = MessageFormat.format(v, vals);
			}

			pageContext.getOut().write(v);
		} catch (IOException ex) {
			throw new JspException(ex.toString(), ex);
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws JspException {
		return EVAL_PAGE;
	}
}
