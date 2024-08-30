package org.springframework.shell.gradle.compresstree;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

public class XzArchiver extends CommonsCompressArchiver {

	public XzArchiver(File xzFile) {
		super(xzFile);
	}

	@Override
	protected XZCompressorInputStream read(InputStream in) throws IOException {
		return new XZCompressorInputStream(in, true);
	}

	@Override
	protected String getSchemePrefix() {
		return "xz:";
	}
}
