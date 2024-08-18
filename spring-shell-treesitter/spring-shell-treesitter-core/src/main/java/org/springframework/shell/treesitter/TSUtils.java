/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.shell.treesitter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public final class TSUtils {

	private static String getFullLibName(String libName) {
		String osName = System.getProperty("os.name").toLowerCase();
		String archName = System.getProperty("os.arch").toLowerCase();
		String ext;
		String os;
		String arch;
		if (osName.contains("windows")) {
			ext = "dll";
			os = "windows";
		}
		else if (osName.contains("linux")) {
			ext = "so";
			os = "linux-gnu";
		}
		else if (osName.contains("mac")) {
			ext = "dylib";
			os = "macos";
		}
		else {
			throw new RuntimeException(String.format("Does not support OS: %s", osName));
		}
		if (archName.contains("amd64") || archName.contains("x86_64")) {
			arch = "x86_64";
		}
		else if (archName.contains("aarch64")) {
			arch = "aarch64";
		}
		else {
			throw new RuntimeException(String.format("Does not support arch: %s", archName));
		}
		String[] parts = libName.split("/");
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (i == parts.length - 1) {
				stringBuilder.append(String.format("%s-%s-%s.%s", arch, os, parts[i], ext));
			} else {
				stringBuilder.append(parts[i]);
				stringBuilder.append("/");
			}
		}
		return stringBuilder.toString();
	}

	static private Path getLibStorePath() {
		String userDefinedPath = System.getProperty("tree-sitter-lib");
		if (userDefinedPath == null) {
			return Path.of(System.getProperty("user.home"), ".tree-sitter");
		}
		return Path.of(userDefinedPath);
	}

	private static long crc32(byte[] bytes) {
		Checksum crc32 = new CRC32();
		crc32.update(bytes, 0, bytes.length);
		return crc32.getValue();
	}

	private static byte[] readFile(File file) {
		try (
			InputStream inputStream = new FileInputStream(file);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			inputStream.transferTo(outputStream);
			return outputStream.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] readInputStream(InputStream inputStream) {
		try (
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			inputStream.transferTo(outputStream);
			return outputStream.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] readLib(String libName) {
		String fullLibName = getFullLibName(libName);
		InputStream inputStream = TSUtils.class.getClassLoader().getResourceAsStream(fullLibName);
		if (inputStream == null) {
			throw new RuntimeException(String.format("Can't open %s", fullLibName));
		}
		return readInputStream(inputStream);
	}

	/**
	 * Load native lib from class path by name convention.
	 *
	 * <p>
	 * Name convention: <code>arch-os-name.ext</code>
	 * <p>
	 * <code>arch</code>
	 * <ol>
	 * <li>x64: <code>x86_64</code></li>
	 * <li>arm64: <code>aarch64</code></li>
	 * </ol>
	 *
	 * @param libName Canonical name of the library. e.g. 'lib/foo', 'bar'
	 */
	public static void loadLib(String libName) {
		String fullLibName = getFullLibName(libName);
		Path filePath = getLibStorePath().resolve(fullLibName);
		File file = filePath.toFile();
		file.getParentFile().mkdirs();
		boolean shouldOverwrite = false;
		byte[] newFileBytes = null;
		if (file.exists()) {
			byte[] oldFileBytes = readFile(file);
			newFileBytes = readLib(libName);
			if (crc32(oldFileBytes) != crc32(newFileBytes)) {
				shouldOverwrite = true;
			}
		}
		else {
			shouldOverwrite = true;
		}
		if (shouldOverwrite) {
			if (newFileBytes == null) {
				newFileBytes = readLib(libName);
			}
			try (
				FileOutputStream outputStream = new FileOutputStream(file)) {
				new ByteArrayInputStream(newFileBytes).transferTo(outputStream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		System.load(file.getAbsolutePath());
	}

}
