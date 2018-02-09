/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package com.redhat.fabric8analytics.eclipse.core.internal;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.content.AbstractContentBody;

public class PomContentBody extends AbstractContentBody {

	private String data;

	public PomContentBody(String data) {
		super(ContentType.TEXT_XML);
		this.data = data;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(data.getBytes());
	}

	@Override
	public long getContentLength() {
		return data.getBytes().length;
	}

	@Override
	public String getFilename() {
		return "pom.xml";
	}

	@Override
	public String getTransferEncoding() {
		return MIME.ENC_BINARY;
	}
}