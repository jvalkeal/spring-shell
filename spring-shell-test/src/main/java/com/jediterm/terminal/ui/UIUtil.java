package com.jediterm.terminal.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.Map;

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

	public static String getJavaVmVendor() {
		return System.getProperty("java.vm.vendor");
	}

	public static void applyRenderingHints(final Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		Toolkit tk = Toolkit.getDefaultToolkit();
		//noinspection HardCodedStringLiteral
		Map map = (Map)tk.getDesktopProperty("awt.font.desktophints");
		if (map != null) {
			g2d.addRenderingHints(map);
		}
	}
}
