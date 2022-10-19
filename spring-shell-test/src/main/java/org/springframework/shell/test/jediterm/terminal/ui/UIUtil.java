package org.springframework.shell.test.jediterm.terminal.ui;

/**
 * @author traff
 */
public class UIUtil {
	public static final String OS_NAME = System.getProperty("os.name");
	public static final String OS_VERSION = System.getProperty("os.version").toLowerCase();

	protected static final String _OS_NAME = OS_NAME.toLowerCase();
	public static final boolean isWindows = _OS_NAME.startsWith("windows");
	public static final boolean isOS2 = _OS_NAME.startsWith("os/2") || _OS_NAME.startsWith("os2");
	public static final boolean isMac = _OS_NAME.startsWith("mac");
	public static final boolean isLinux = _OS_NAME.startsWith("linux");
	public static final boolean isUnix = !isWindows && !isOS2;

	public static final String JAVA_RUNTIME_VERSION = System.getProperty("java.runtime.version");
}
