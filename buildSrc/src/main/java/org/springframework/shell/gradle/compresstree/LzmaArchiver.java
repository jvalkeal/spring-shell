package org.springframework.shell.gradle.compresstree;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;

public class LzmaArchiver extends CommonsCompressArchiver {

	public LzmaArchiver(File xzFile) {
		super(xzFile);
	}

	@Override
	protected LZMACompressorInputStream read(InputStream in) throws IOException {
		return new LZMACompressorInputStream(in);
	}

	@Override
	protected String getSchemePrefix() {
		return "lzma:";
	}
}
