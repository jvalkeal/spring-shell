package org.springframework.shell;

import org.springframework.boot.ExitCodeGenerator;

public class ExitCodeException extends RuntimeException implements ExitCodeGenerator {

	private final int exitCode;

	public ExitCodeException(Throwable cause, int exitCode) {
		super(cause);
		this.exitCode = exitCode;
	}

	@Override
	public int getExitCode() {
		return exitCode;
	}
}
