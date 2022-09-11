package com.jediterm.terminal.ui;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.jediterm.terminal.CursorShape;
import com.jediterm.terminal.DefaultTerminalCopyPasteHandler;
import com.jediterm.terminal.SubstringFinder;
import com.jediterm.terminal.TerminalCopyPasteHandler;
import com.jediterm.terminal.TerminalDisplay;
import com.jediterm.terminal.TerminalOutputStream;
import com.jediterm.terminal.TerminalStarter;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.emulator.charset.CharacterSets;
import com.jediterm.terminal.emulator.mouse.MouseMode;
import com.jediterm.terminal.model.LinesBuffer;
import com.jediterm.terminal.model.SelectionUtil;
import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalLine;
import com.jediterm.terminal.model.TerminalLineIntervalHighlighting;
import com.jediterm.terminal.model.TerminalSelection;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.jediterm.terminal.util.Pair;
import com.jediterm.typeahead.TerminalTypeAheadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerminalPanel implements TerminalDisplay {

	private static final Logger LOG = LoggerFactory.getLogger(TerminalPanel.class);

	public static final double SCROLL_SPEED = 0.05;

	/*font related*/
	private Font myNormalFont;
	private Font myItalicFont;
	private Font myBoldFont;
	private Font myBoldItalicFont;
	private int myDescent = 0;
	protected Dimension myCharSize = new Dimension();
	private boolean myMonospaced;
	protected Dimension myTermSize = new Dimension(80, 24);

	private TerminalStarter myTerminalStarter = null;

	private MouseMode myMouseMode = MouseMode.MOUSE_REPORTING_NONE;
	private TerminalSelection mySelection = null;

	private final TerminalCopyPasteHandler myCopyPasteHandler;

	private TerminalPanelListener myTerminalPanelListener;

	private final SettingsProvider mySettingsProvider;
	private final TerminalTextBuffer myTerminalTextBuffer;

	final private StyleState myStyleState;

	/*scroll and cursor*/
	final private TerminalCursor myCursor = new TerminalCursor();

	private boolean myScrollingEnabled = true;
	protected int myClientScrollOrigin;
	private final List<KeyListener> myCustomKeyListeners = new CopyOnWriteArrayList<>();

	private String myWindowTitle = "Terminal";

	private String myInputMethodUncommittedChars;

	private Timer myRepaintTimer;
	private AtomicInteger scrollDy = new AtomicInteger(0);

	private int myBlinkingPeriod = 500;
	private TerminalCoordinates myCoordsAccessor;

	private SubstringFinder.FindResult myFindResult;

	private final TerminalKeyHandler myTerminalKeyHandler = new TerminalKeyHandler();
	private TerminalTypeAheadManager myTypeAheadManager;
	private volatile boolean myBracketedPasteMode;

	public TerminalPanel(SettingsProvider settingsProvider, TerminalTextBuffer terminalTextBuffer, StyleState styleState) {
		mySettingsProvider = settingsProvider;
		myTerminalTextBuffer = terminalTextBuffer;
		myStyleState = styleState;
		myTermSize.width = terminalTextBuffer.getWidth();
		myTermSize.height = terminalTextBuffer.getHeight();
		myCopyPasteHandler = createCopyPasteHandler();

		// updateScrolling(true);
	}

	void setTypeAheadManager(TerminalTypeAheadManager typeAheadManager) {
		myTypeAheadManager = typeAheadManager;
	}

	protected TerminalCopyPasteHandler createCopyPasteHandler() {
		return new DefaultTerminalCopyPasteHandler();
	}

	public TerminalPanelListener getTerminalPanelListener() {
		return myTerminalPanelListener;
	}

	protected void initFont() {
		myNormalFont = createFont();
		myBoldFont = myNormalFont.deriveFont(Font.BOLD);
		myItalicFont = myNormalFont.deriveFont(Font.ITALIC);
		myBoldItalicFont = myNormalFont.deriveFont(Font.BOLD | Font.ITALIC);

		establishFontMetrics();
	}

	public boolean isLocalMouseAction(MouseEvent e) {
		return mySettingsProvider.forceActionOnMouseReporting() || (isMouseReporting() == e.isShiftDown());
	}

	public boolean isRemoteMouseAction(MouseEvent e) {
		return isMouseReporting() && !e.isShiftDown();
	}

	protected boolean isRetina() {
		return UIUtil.isRetina();
	}

	public void setBlinkingPeriod(int blinkingPeriod) {
		myBlinkingPeriod = blinkingPeriod;
	}

	public void setCoordAccessor(TerminalCoordinates coordAccessor) {
		myCoordsAccessor = coordAccessor;
	}

	@Override
	public void terminalMouseModeSet(MouseMode mode) {
		myMouseMode = mode;
	}

	private boolean isMouseReporting() {
		return myMouseMode != MouseMode.MOUSE_REPORTING_NONE;
	}

	protected Font createFont() {
		return mySettingsProvider.getTerminalFont();
	}

	protected void drawImage(Graphics2D gfx, BufferedImage image, int x, int y, ImageObserver observer) {
		gfx.drawImage(image, x, y,
						image.getWidth(), image.getHeight(), observer);
	}

	protected BufferedImage createBufferedImage(int width, int height) {
		return new BufferedImage(width, height,
						BufferedImage.TYPE_INT_RGB);
	}

	public void setTerminalStarter(final TerminalStarter terminalStarter) {
		myTerminalStarter = terminalStarter;
		// sizeTerminalFromComponent();
	}

	public void addCustomKeyListener(KeyListener keyListener) {
		myCustomKeyListeners.add(keyListener);
	}

	public void removeCustomKeyListener(KeyListener keyListener) {
		myCustomKeyListeners.remove(keyListener);
	}

	public void setTerminalPanelListener(final TerminalPanelListener terminalPanelListener) {
		myTerminalPanelListener = terminalPanelListener;
	}

	private void establishFontMetrics() {
		final BufferedImage img = createBufferedImage(1, 1);
		final Graphics2D graphics = img.createGraphics();
		graphics.setFont(myNormalFont);

		final float lineSpacing = mySettingsProvider.getLineSpacing();
		final FontMetrics fo = graphics.getFontMetrics();

		myCharSize.width = fo.charWidth('W');
		int fontMetricsHeight = fo.getHeight();
		myCharSize.height = (int)Math.ceil(fontMetricsHeight * lineSpacing);
		myDescent = fo.getDescent();
		if (LOG.isDebugEnabled()) {
			// The magic +2 here is to give lines a tiny bit of extra height to avoid clipping when rendering some Apple
			// emoji, which are slightly higher than the font metrics reported character height :(
			int oldCharHeight = fontMetricsHeight + (int) (lineSpacing * 2) + 2;
			int oldDescent = fo.getDescent() + (int)lineSpacing;
			LOG.debug("charHeight=" + oldCharHeight + "->" + myCharSize.height +
				", descent=" + oldDescent + "->" + myDescent);
		}

		myMonospaced = isMonospaced(fo);
		if (!myMonospaced) {
			LOG.info("WARNING: Font " + myNormalFont.getName() + " is non-monospaced");
		}

		img.flush();
		graphics.dispose();
	}

	private static boolean isMonospaced(FontMetrics fontMetrics) {
		boolean isMonospaced = true;
		int charWidth = -1;
		for (int codePoint = 0; codePoint < 128; codePoint++) {
			if (Character.isValidCodePoint(codePoint)) {
				char character = (char) codePoint;
				if (isWordCharacter(character)) {
					int w = fontMetrics.charWidth(character);
					if (charWidth != -1) {
						if (w != charWidth) {
							isMonospaced = false;
							break;
						}
					} else {
						charWidth = w;
					}
				}
			}
		}
		return isMonospaced;
	}

	private static boolean isWordCharacter(char character) {
		return Character.isLetterOrDigit(character);
	}

	protected void setupAntialiasing(Graphics graphics) {
		if (graphics instanceof Graphics2D) {
			Graphics2D myGfx = (Graphics2D) graphics;
			final Object mode = mySettingsProvider.useAntialiasing() ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
							: RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
			final RenderingHints hints = new RenderingHints(
							RenderingHints.KEY_TEXT_ANTIALIASING, mode);
			myGfx.setRenderingHints(hints);
		}
	}

	private boolean hasUncommittedChars() {
		return myInputMethodUncommittedChars != null && myInputMethodUncommittedChars.length() > 0;
	}

	// also called from com.intellij.terminal.JBTerminalPanel
	public void handleKeyEvent(KeyEvent e) {
		final int id = e.getID();
		if (id == KeyEvent.KEY_PRESSED) {
			for (KeyListener keyListener : myCustomKeyListeners) {
				keyListener.keyPressed(e);
			}
		} else if (id == KeyEvent.KEY_TYPED) {
			for (KeyListener keyListener : myCustomKeyListeners) {
				keyListener.keyTyped(e);
			}
		}
	}

	public int getPixelWidth() {
		return myCharSize.width * myTermSize.width + getInsetX();
	}

	public int getPixelHeight() {
		return myCharSize.height * myTermSize.height;
	}

	public int getColumnCount() {
		return myTermSize.width;
	}

	public int getRowCount() {
		return myTermSize.height;
	}

	public String getWindowTitle() {
		return myWindowTitle;
	}

	protected int getInsetX() {
		return 4;
	}

	KeyListener getTerminalKeyListener() {
		return myTerminalKeyHandler;
	}

	public enum TerminalCursorState {
		SHOWING, HIDDEN, NO_FOCUS;
	}

	public class TerminalCursor {

		// cursor state
		protected Point myCursorCoordinates = new Point();
		private CursorShape myShape = CursorShape.BLINK_BLOCK;

		// terminal modes
		private boolean myShouldDrawCursor = true;
		private boolean myBlinking = true;

		private long myLastCursorChange;
		private boolean myCursorHasChanged;

		public void setX(int x) {
			myCursorCoordinates.x = x;
			cursorChanged();
		}

		public void setY(int y) {
			myCursorCoordinates.y = y;
			cursorChanged();
		}

		public int getCoordX() {
			if (myTypeAheadManager != null) {
				return myTypeAheadManager.getCursorX() - 1;
			}
			return myCursorCoordinates.x;
		}

		public int getCoordY() {
			return myCursorCoordinates.y - 1 - myClientScrollOrigin;
		}

		public void setShouldDrawCursor(boolean shouldDrawCursor) {
			myShouldDrawCursor = shouldDrawCursor;
		}

		public void setBlinking(boolean blinking) {
			myBlinking = blinking;
		}

		public boolean isBlinking() {
			return myBlinking && (getBlinkingPeriod() > 0);
		}

		public void cursorChanged() {
			myCursorHasChanged = true;
			myLastCursorChange = System.currentTimeMillis();
			// repaint();
		}

		public void changeStateIfNeeded() {
		}

		void setShape(CursorShape shape) {
			this.myShape = shape;
		}
	}

	private int getBlinkingPeriod() {
		if (myBlinkingPeriod != mySettingsProvider.caretBlinkingMs()) {
			setBlinkingPeriod(mySettingsProvider.caretBlinkingMs());
		}
		return myBlinkingPeriod;
	}

	protected void drawImage(Graphics2D g, BufferedImage image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2) {
		g.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
	}

	protected Font getFontToDisplay(char c, TextStyle style) {
		boolean bold = style.hasOption(TextStyle.Option.BOLD);
		boolean italic = style.hasOption(TextStyle.Option.ITALIC);
		// workaround to fix Swing bad rendering of bold special chars on Linux
		if (bold && mySettingsProvider.DECCompatibilityMode() && CharacterSets.isDecBoxChar(c)) {
			return myNormalFont;
		}
		return bold ? (italic ? myBoldItalicFont : myBoldFont)
						: (italic ? myItalicFont : myNormalFont);
	}

	// Called in a background thread with myTerminalTextBuffer.lock() acquired
	public void scrollArea(final int scrollRegionTop, final int scrollRegionSize, int dy) {
		scrollDy.addAndGet(dy);
		mySelection = null;
	}

	public void setCursor(final int x, final int y) {
		myCursor.setX(x);
		myCursor.setY(y);
	}

	@Override
	public void setCursorShape(CursorShape shape) {
		myCursor.setShape(shape);
		switch (shape) {
			case STEADY_BLOCK:
			case STEADY_UNDERLINE:
			case STEADY_VERTICAL_BAR:
				myCursor.myBlinking = false;
				break;
			case BLINK_BLOCK:
			case BLINK_UNDERLINE:
			case BLINK_VERTICAL_BAR:
				myCursor.myBlinking = true;
				break;
		}
	}

	public void beep() {
		if (mySettingsProvider.audibleBell()) {
			Toolkit.getDefaultToolkit().beep();
		}
	}

	public Rectangle getBounds(TerminalLineIntervalHighlighting highlighting) {
		TerminalLine line = highlighting.getLine();
		int index = myTerminalTextBuffer.findScreenLineIndex(line);
		if (index >= 0 && !highlighting.isDisposed()) {
			return getBounds(new LineCellInterval(index, highlighting.getStartOffset(), highlighting.getEndOffset() + 1));
		}
		return null;
	}

	private Rectangle getBounds(LineCellInterval cellInterval) {
		Point topLeft = new Point(cellInterval.getStartColumn() * myCharSize.width + getInsetX(),
			cellInterval.getLine() * myCharSize.height);
		return new Rectangle(topLeft, new Dimension(myCharSize.width * cellInterval.getCellCount(), myCharSize.height));
	}

	public TerminalTextBuffer getTerminalTextBuffer() {
		return myTerminalTextBuffer;
	}

	public TerminalSelection getSelection() {
		return mySelection;
	}

	@Override
	public boolean ambiguousCharsAreDoubleWidth() {
		return mySettingsProvider.ambiguousCharsAreDoubleWidth();
	}

	@Override
	public void setBracketedPasteMode(boolean enabled) {
		myBracketedPasteMode = enabled;
	}

	public LinesBuffer getScrollBuffer() {
		return myTerminalTextBuffer.getHistoryBuffer();
	}

	@Override
	public void setCursorVisible(boolean shouldDrawCursor) {
		myCursor.setShouldDrawCursor(shouldDrawCursor);
	}

	@Override
	public void setBlinkingCursor(boolean enabled) {
		myCursor.setBlinking(enabled);
	}

	public TerminalCursor getTerminalCursor() {
		return myCursor;
	}

	public TerminalOutputStream getTerminalOutputStream() {
		return myTerminalStarter;
	}

	@Override
	public void setWindowTitle(String name) {
		myWindowTitle = name;
		if (myTerminalPanelListener != null) {
			myTerminalPanelListener.onTitleChanged(myWindowTitle);
		}
	}

	public void selectAll() {
		mySelection = new TerminalSelection(new Point(0, -myTerminalTextBuffer.getHistoryLinesCount()),
			new Point(myTermSize.width, myTerminalTextBuffer.getScreenLinesCount()));
	}

	private String getSelectionText() {
		if (mySelection != null) {
			Pair<Point, Point> points = mySelection.pointsForRun(myTermSize.width);

			if (points.first != null || points.second != null) {
				return SelectionUtil
								.getSelectionText(points.first, points.second, myTerminalTextBuffer);

			}
		}

		return null;
	}

	protected boolean openSelectionAsURL() {
		if (Desktop.isDesktopSupported()) {
			try {
				String selectionText = getSelectionText();

				if (selectionText != null) {
					Desktop.getDesktop().browse(new URI(selectionText));
				}
			} catch (Exception e) {
				//ok then
			}
		}
		return false;
	}

	public void clearBuffer() {
		clearBuffer(true);
	}

	/**
	 * @param keepLastLine true to keep last line (e.g. to keep terminal prompt)
	 *                     false to clear entire terminal panel (relevant for terminal console)
	 */
	protected void clearBuffer(boolean keepLastLine) {
		if (!myTerminalTextBuffer.isUsingAlternateBuffer()) {
			myTerminalTextBuffer.clearHistory();

			if (myCoordsAccessor != null) {
				if (keepLastLine) {
					if (myCoordsAccessor.getY() > 0) {
						TerminalLine lastLine = myTerminalTextBuffer.getLine(myCoordsAccessor.getY() - 1);
						myTerminalTextBuffer.clearAll();
						myCoordsAccessor.setY(0);
						myCursor.setY(1);
						myTerminalTextBuffer.addLine(lastLine);
					}
				}
				else {
					myTerminalTextBuffer.clearAll();
					myCoordsAccessor.setX(0);
					myCoordsAccessor.setY(1);
					myCursor.setX(0);
					myCursor.setY(1);
				}
			}

			// myBoundedRangeModel.setValue(0);
			// updateScrolling(true);

			// myClientScrollOrigin = myBoundedRangeModel.getValue();
		}
	}


	private static boolean isAltPressedOnly(KeyEvent e) {
		int modifiersEx = e.getModifiersEx();
		return (modifiersEx & InputEvent.ALT_DOWN_MASK) != 0 &&
						(modifiersEx & InputEvent.ALT_GRAPH_DOWN_MASK) == 0 &&
						(modifiersEx & InputEvent.CTRL_DOWN_MASK) == 0 &&
						(modifiersEx & InputEvent.SHIFT_DOWN_MASK) == 0;
	}

	private boolean processCharacter(KeyEvent e) {
		if (isAltPressedOnly(e) && mySettingsProvider.altSendsEscape()) {
			return false;
		}
		char keyChar = e.getKeyChar();
		final char[] obuffer;
		obuffer = new char[]{keyChar};

		if (keyChar == '`' && (e.getModifiersEx() & InputEvent.META_DOWN_MASK) != 0) {
			// Command + backtick is a short-cut on Mac OSX, so we shouldn't type anything
			return false;
		}

		myTerminalStarter.sendString(new String(obuffer), true);

		return true;
	}

	private boolean processTerminalKeyTyped(KeyEvent e) {
		if (hasUncommittedChars()) {
			return false;
		}

		if (!Character.isISOControl(e.getKeyChar())) { // keys filtered out here will be processed in processTerminalKeyPressed
			try {
				return processCharacter(e);
			}
			catch (Exception ex) {
				LOG.error("Error sending typed key to emulator", ex);
			}
		}
		return false;
	}

	private class TerminalKeyHandler extends KeyAdapter {

		private boolean myIgnoreNextKeyTypedEvent;

		public TerminalKeyHandler() {
		}

		public void keyPressed(KeyEvent e) {
			myIgnoreNextKeyTypedEvent = false;
		}

		public void keyTyped(KeyEvent e) {
			if (myIgnoreNextKeyTypedEvent || processTerminalKeyTyped(e)) {
				e.consume();
			}
		}
	}

	public void dispose() {
		myRepaintTimer.stop();
	}
}
