package com.jediterm.terminal.ui.settings;

import java.awt.Color;
import java.awt.Font;

import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.emulator.ColorPaletteImpl;
import com.jediterm.terminal.model.LinesBuffer;
import com.jediterm.terminal.model.TerminalTypeAheadSettings;
import com.jediterm.terminal.ui.UIUtil;

public class DefaultSettingsProvider implements SettingsProvider {

	@Override
	public ColorPalette getTerminalColorPalette() {
		return UIUtil.isWindows ? ColorPaletteImpl.WINDOWS_PALETTE : ColorPaletteImpl.XTERM_PALETTE;
	}

	@Override
	public Font getTerminalFont() {
		String fontName;
		if (UIUtil.isWindows) {
			fontName = "Consolas";
		} else if (UIUtil.isMac) {
			fontName = "Menlo";
		} else {
			fontName = "Monospaced";
		}
		return new Font(fontName, Font.PLAIN, (int)getTerminalFontSize());
	}

	@Override
	public float getTerminalFontSize() {
		return 14;
	}

	@Override
	public TextStyle getDefaultStyle() {
		return new TextStyle(TerminalColor.BLACK, TerminalColor.WHITE);
	}

	@Override
	public TextStyle getSelectionColor() {
		return new TextStyle(TerminalColor.WHITE, TerminalColor.rgb(82, 109, 165));
	}

	@Override
	public TextStyle getFoundPatternColor() {
		return new TextStyle(TerminalColor.BLACK, TerminalColor.rgb(255, 255, 0));
	}

	@Override
	public TextStyle getHyperlinkColor() {
		return new TextStyle(TerminalColor.awt(Color.BLUE), TerminalColor.WHITE);
	}

	@Override
	public boolean useInverseSelectionColor() {
		return true;
	}

	@Override
	public boolean copyOnSelect() {
		return emulateX11CopyPaste();
	}

	@Override
	public boolean pasteOnMiddleMouseClick() {
		return emulateX11CopyPaste();
	}

	@Override
	public boolean emulateX11CopyPaste() {
		return false;
	}

	@Override
	public boolean useAntialiasing() {
		return true;
	}

	@Override
	public int maxRefreshRate() {
		return 50;
	}

	@Override
	public boolean audibleBell() {
		return true;
	}

	@Override
	public boolean enableMouseReporting() {
		return true;
	}

	@Override
	public int caretBlinkingMs() {
		return 505;
	}

	@Override
	public boolean scrollToBottomOnTyping() {
		return true;
	}

	@Override
	public boolean DECCompatibilityMode() {
		return true;
	}

	@Override
	public boolean forceActionOnMouseReporting() {
		return false;
	}

	@Override
	public int getBufferMaxLinesCount() {
		return LinesBuffer.DEFAULT_MAX_LINES_COUNT;
	}

	@Override
	public boolean altSendsEscape() {
		return true;
	}

	@Override
	public boolean ambiguousCharsAreDoubleWidth() {
		return false;
	}

	@Override
	public TerminalTypeAheadSettings getTypeAheadSettings() {
		return TerminalTypeAheadSettings.DEFAULT;
	}
}
