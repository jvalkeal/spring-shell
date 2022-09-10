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
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.URI;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jediterm.terminal.CursorShape;
import com.jediterm.terminal.DefaultTerminalCopyPasteHandler;
import com.jediterm.terminal.SubstringFinder;
import com.jediterm.terminal.SubstringFinder.FindResult.FindItem;
import com.jediterm.terminal.TerminalCopyPasteHandler;
import com.jediterm.terminal.TerminalDisplay;
import com.jediterm.terminal.TerminalOutputStream;
import com.jediterm.terminal.TerminalStarter;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.TextStyle.Option;
import com.jediterm.terminal.emulator.charset.CharacterSets;
import com.jediterm.terminal.emulator.mouse.MouseMode;
import com.jediterm.terminal.model.LinesBuffer;
import com.jediterm.terminal.model.SelectionUtil;
import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalLine;
import com.jediterm.terminal.model.TerminalLineIntervalHighlighting;
import com.jediterm.terminal.model.TerminalModelListener;
import com.jediterm.terminal.model.TerminalSelection;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.model.hyperlinks.LinkInfo;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.jediterm.terminal.util.Pair;
import com.jediterm.typeahead.Ascii;
import com.jediterm.typeahead.TerminalTypeAheadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerminalPanel /*  extends JComponent */ implements TerminalDisplay, TerminalActionProvider {

	private static final Logger LOG = LoggerFactory.getLogger(TerminalPanel.class);
	private static final long serialVersionUID = -1048763516632093014L;

	public static final double SCROLL_SPEED = 0.05;

	/*font related*/
	private Font myNormalFont;
	private Font myItalicFont;
	private Font myBoldFont;
	private Font myBoldItalicFont;
	private int myDescent = 0;
	private int mySpaceBetweenLines = 0;
	protected Dimension myCharSize = new Dimension();
	private boolean myMonospaced;
	protected Dimension myTermSize = new Dimension(80, 24);
	private boolean myInitialSizeSyncDone = false;

	private TerminalStarter myTerminalStarter = null;

	private MouseMode myMouseMode = MouseMode.MOUSE_REPORTING_NONE;
	private Point mySelectionStartPoint = null;
	private TerminalSelection mySelection = null;

	private final TerminalCopyPasteHandler myCopyPasteHandler;

	private TerminalPanelListener myTerminalPanelListener;

	private final SettingsProvider mySettingsProvider;
	private final TerminalTextBuffer myTerminalTextBuffer;

	final private StyleState myStyleState;

	/*scroll and cursor*/
	final private TerminalCursor myCursor = new TerminalCursor();

	//we scroll a window [0, terminal_height] in the range [-history_lines_count, terminal_height]
	private final BoundedRangeModel myBoundedRangeModel = new DefaultBoundedRangeModel(0, 80, 0, 80);

	private boolean myScrollingEnabled = true;
	protected int myClientScrollOrigin;
	private final List<KeyListener> myCustomKeyListeners = new CopyOnWriteArrayList<>();

	private String myWindowTitle = "Terminal";

	private TerminalActionProvider myNextActionProvider;
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

		updateScrolling(true);
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

	protected void handleMouseWheelEvent(MouseWheelEvent e, JScrollBar scrollBar) {
		if (e.isShiftDown() || e.getUnitsToScroll() == 0 || Math.abs(e.getPreciseWheelRotation()) < 0.01) {
			return;
		}
		moveScrollBar(e.getUnitsToScroll());
		e.consume();
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

	public void setFindResult(SubstringFinder.FindResult findResult) {
		myFindResult = findResult;
		// repaint();
	}

	public SubstringFinder.FindResult getFindResult() {
		return myFindResult;
	}

	public FindItem selectPrevFindResultItem() {
		return selectPrevOrNextFindResultItem(false);
	}

	public FindItem selectNextFindResultItem() {
		return selectPrevOrNextFindResultItem(true);
	}

	protected FindItem selectPrevOrNextFindResultItem(boolean next) {
		if (myFindResult != null) {
			SubstringFinder.FindResult.FindItem item = next ? myFindResult.nextFindItem() : myFindResult.prevFindItem();
			if (item != null) {
				mySelection = new TerminalSelection(new Point(item.getStart().x, item.getStart().y - myTerminalTextBuffer.getHistoryLinesCount()),
								new Point(item.getEnd().x, item.getEnd().y - myTerminalTextBuffer.getHistoryLinesCount()));
				if (mySelection.getStart().y < getTerminalTextBuffer().getHeight() / 2) {
					myBoundedRangeModel.setValue(mySelection.getStart().y - getTerminalTextBuffer().getHeight() / 2);
				} else {
					myBoundedRangeModel.setValue(0);
				}
				// repaint();
				return item;
			}
		}
		return null;
	}

	@Override
	public void terminalMouseModeSet(MouseMode mode) {
		myMouseMode = mode;
	}

	private boolean isMouseReporting() {
		return myMouseMode != MouseMode.MOUSE_REPORTING_NONE;
	}

	/**
	 * Scroll to bottom to ensure the cursor will be visible.
	 */
	private void scrollToBottom() {
		// Scroll to bottom even if the cursor is on the last line, i.e. it's currently visible.
		// This will address the cases when the scroll is fixed to show some history lines, Enter is hit and after
		// Enter processing, the cursor will be pushed out of visible area unless scroll is reset to screen buffer.
		int delta = 1;
		int zeroBasedCursorY = myCursor.myCursorCoordinates.y - 1;
		if (zeroBasedCursorY + delta >= myBoundedRangeModel.getValue() + myBoundedRangeModel.getExtent()) {
			myBoundedRangeModel.setValue(0);
		}
	}

	private void pageUp() {
		moveScrollBar(-myTermSize.height);
	}

	private void pageDown() {
		moveScrollBar(myTermSize.height);
	}

	private void scrollUp() {
		moveScrollBar(-1);
	}

	private void scrollDown() {
		moveScrollBar(1);
	}

	private void moveScrollBar(int k) {
		myBoundedRangeModel.setValue(myBoundedRangeModel.getValue() + k);
	}

	protected Font createFont() {
		return mySettingsProvider.getTerminalFont();
	}

	private Cell panelPointToCell(Point p) {
		int x = Math.min((p.x - getInsetX()) / myCharSize.width, getColumnCount() - 1);
		x = Math.max(0, x);
		int y = Math.min(p.y / myCharSize.height, getRowCount() - 1) + myClientScrollOrigin;
		return new Cell(y, x);
	}

	private void copySelection(Point selectionStart,
														 Point selectionEnd,
														 boolean useSystemSelectionClipboardIfAvailable) {
		if (selectionStart == null || selectionEnd == null) {
			return;
		}
		String selectionText = SelectionUtil.getSelectionText(selectionStart, selectionEnd, myTerminalTextBuffer);
		if (selectionText.length() != 0) {
			myCopyPasteHandler.setContents(selectionText, useSystemSelectionClipboardIfAvailable);
		}
	}

	private void pasteFromClipboard(boolean useSystemSelectionClipboardIfAvailable) {
		String text = myCopyPasteHandler.getContents(useSystemSelectionClipboardIfAvailable);

		if (text == null) {
			return;
		}

		try {
			// Sanitize clipboard text to use CR as the line separator.
			// See https://github.com/JetBrains/jediterm/issues/136.
			if (!UIUtil.isWindows) {
				// On Windows, Java automatically does this CRLF->LF sanitization, but
				// other terminals on Unix typically also do this sanitization, so
				// maybe JediTerm also should.
				text = text.replace("\r\n", "\n");
			}
			text = text.replace('\n', '\r');

			if (myBracketedPasteMode) {
				text = "\u001b[200~" + text + "\u001b[201~";
			}
			myTerminalStarter.sendString(text, true);
		} catch (RuntimeException e) {
			LOG.info("", e);
		}
	}

	private String getClipboardString() {
		return myCopyPasteHandler.getContents(false);
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
		mySpaceBetweenLines = Math.max(0, ((myCharSize.height - fontMetricsHeight) / 2) * 2);
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
		private boolean myCursorIsShown; // blinking state
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

		private boolean cursorShouldChangeBlinkState(long currentTime) {
			return currentTime - myLastCursorChange > getBlinkingPeriod();
		}

		public void changeStateIfNeeded() {
			// if (!isFocusOwner()) {
			// 	return;
			// }
			// long currentTime = System.currentTimeMillis();
			// if (cursorShouldChangeBlinkState(currentTime)) {
			// 	myCursorIsShown = !myCursorIsShown;
			// 	myLastCursorChange = currentTime;
			// 	myCursorHasChanged = false;
			// 	repaint();
			// }
		}

		private TerminalCursorState computeBlinkingState() {
			if (!isBlinking() || myCursorHasChanged || myCursorIsShown) {
				return TerminalCursorState.SHOWING;
			}
			return TerminalCursorState.HIDDEN;
		}

		private TerminalCursorState computeCursorState() {
			if (!myShouldDrawCursor) {
				return TerminalCursorState.HIDDEN;
			}
			// if (!isFocusOwner()) {
			// 	return TerminalCursorState.NO_FOCUS;
			// }
			return computeBlinkingState();
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

	private TextStyle getInversedStyle(TextStyle style) {
		TextStyle.Builder builder = new TextStyle.Builder(style);
		builder.setOption(Option.INVERSE, !style.hasOption(Option.INVERSE));
		if (style.getForeground() == null) {
			builder.setForeground(myStyleState.getForeground());
		}
		if (style.getBackground() == null) {
			builder.setBackground(myStyleState.getBackground());
		}
		return builder.build();
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

	// should be called on EDT
	public void scrollToShowAllOutput() {
		myTerminalTextBuffer.lock();
		try {
			int historyLines = myTerminalTextBuffer.getHistoryLinesCount();
			if (historyLines > 0) {
				int termHeight = myTermSize.height;
				myBoundedRangeModel.setRangeProperties(-historyLines, historyLines + termHeight, -historyLines,
						termHeight, false);
				TerminalModelListener modelListener = new TerminalModelListener() {
					@Override
					public void modelChanged() {
						int zeroBasedCursorY = myCursor.myCursorCoordinates.y - 1;
						if (zeroBasedCursorY + historyLines >= termHeight) {
							myTerminalTextBuffer.removeModelListener(this);
							SwingUtilities.invokeLater(() -> {
								myTerminalTextBuffer.lock();
								try {
									myBoundedRangeModel.setRangeProperties(0, myTermSize.height,
											-myTerminalTextBuffer.getHistoryLinesCount(), myTermSize.height, false);
								} finally {
									myTerminalTextBuffer.unlock();
								}
							});
						}
					}
				};
				myTerminalTextBuffer.addModelListener(modelListener);
				myBoundedRangeModel.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						myBoundedRangeModel.removeChangeListener(this);
						myTerminalTextBuffer.removeModelListener(modelListener);
					}
				});
			}
		} finally {
			myTerminalTextBuffer.unlock();
		}
	}

	private void updateScrolling(boolean forceUpdate) {
		int dy = scrollDy.getAndSet(0);
		if (dy == 0 && !forceUpdate) {
			return;
		}
		if (myScrollingEnabled) {
			int value = myBoundedRangeModel.getValue();
			int historyLineCount = myTerminalTextBuffer.getHistoryLinesCount();
			if (value == 0) {
				myBoundedRangeModel
								.setRangeProperties(0, myTermSize.height, -historyLineCount, myTermSize.height, false);
			} else {
				// if scrolled to a specific area, update scroll to keep showing this area
				myBoundedRangeModel.setRangeProperties(
								Math.min(Math.max(value + dy, -historyLineCount), myTermSize.height),
								myTermSize.height,
								-historyLineCount,
								myTermSize.height, false);
			}
		} else {
			myBoundedRangeModel.setRangeProperties(0, myTermSize.height, 0, myTermSize.height, false);
		}
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

	/**
	 * @deprecated use {@link #getVerticalScrollModel()} instead
	 */
	@Deprecated
	public BoundedRangeModel getBoundedRangeModel() {
		return myBoundedRangeModel;
	}

	public BoundedRangeModel getVerticalScrollModel() {
		return myBoundedRangeModel;
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

	// protected JPopupMenu createPopupMenu(LinkInfo linkInfo, MouseEvent e) {
	// 	JPopupMenu popup = new JPopupMenu();
	// 	LinkInfo.PopupMenuGroupProvider popupMenuGroupProvider = linkInfo != null ? linkInfo.getPopupMenuGroupProvider() : null;
	// 	if (popupMenuGroupProvider != null) {
	// 		TerminalAction.addToMenu(popup, new TerminalActionProvider() {
	// 			@Override
	// 			public List<TerminalAction> getActions() {
	// 				return popupMenuGroupProvider.getPopupMenuGroup(e);
	// 			}

	// 			@Override
	// 			public TerminalActionProvider getNextProvider() {
	// 				return TerminalPanel.this;
	// 			}

	// 			@Override
	// 			public void setNextProvider(TerminalActionProvider provider) {
	// 			}
	// 		});
	// 	}
	// 	else {
	// 		TerminalAction.addToMenu(popup, this);
	// 	}

	// 	return popup;
	// }

	public void setScrollingEnabled(boolean scrollingEnabled) {
		myScrollingEnabled = scrollingEnabled;

		SwingUtilities.invokeLater(() -> updateScrolling(true));
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

	// @Override
	// public List<TerminalAction> getActions() {
	// 	return List.of(
	// 					new TerminalAction(mySettingsProvider.getOpenUrlActionPresentation(), input -> {
	// 						return openSelectionAsURL();
	// 					}).withEnabledSupplier(this::selectionTextIsUrl),
	// 					new TerminalAction(mySettingsProvider.getCopyActionPresentation(), this::handleCopy) {
	// 						@Override
	// 						public boolean isEnabled(KeyEvent e) {
	// 							return e != null || mySelection != null;
	// 						}
	// 					}.withMnemonicKey(KeyEvent.VK_C),
	// 					new TerminalAction(mySettingsProvider.getPasteActionPresentation(), input -> {
	// 						handlePaste();
	// 						return true;
	// 					}).withMnemonicKey(KeyEvent.VK_P).withEnabledSupplier(() -> getClipboardString() != null),
	// 					new TerminalAction(mySettingsProvider.getSelectAllActionPresentation(), input -> {
	// 						selectAll();
	// 						return true;
	// 					}),
	// 					new TerminalAction(mySettingsProvider.getClearBufferActionPresentation(), input -> {
	// 						clearBuffer();
	// 						return true;
	// 					}).withMnemonicKey(KeyEvent.VK_K).withEnabledSupplier(() -> !myTerminalTextBuffer.isUsingAlternateBuffer()).separatorBefore(true),
	// 					new TerminalAction(mySettingsProvider.getPageUpActionPresentation(), input -> {
	// 						pageUp();
	// 						return true;
	// 					}).withEnabledSupplier(() -> !myTerminalTextBuffer.isUsingAlternateBuffer()).separatorBefore(true),
	// 					new TerminalAction(mySettingsProvider.getPageDownActionPresentation(), input -> {
	// 						pageDown();
	// 						return true;
	// 					}).withEnabledSupplier(() -> !myTerminalTextBuffer.isUsingAlternateBuffer()),
	// 					new TerminalAction(mySettingsProvider.getLineUpActionPresentation(), input -> {
	// 						scrollUp();
	// 						return true;
	// 					}).withEnabledSupplier(() -> !myTerminalTextBuffer.isUsingAlternateBuffer()).separatorBefore(true),
	// 					new TerminalAction(mySettingsProvider.getLineDownActionPresentation(), input -> {
	// 						scrollDown();
	// 						return true;
	// 					}));
	// }

	public void selectAll() {
		mySelection = new TerminalSelection(new Point(0, -myTerminalTextBuffer.getHistoryLinesCount()),
			new Point(myTermSize.width, myTerminalTextBuffer.getScreenLinesCount()));
	}

	private Boolean selectionTextIsUrl() {
		String selectionText = getSelectionText();
		if (selectionText != null) {
			try {
				URI uri = new URI(selectionText);
				//noinspection ResultOfMethodCallIgnored
				uri.toURL();
				return true;
			} catch (Exception e) {
				//pass
			}
		}
		return false;
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

			myBoundedRangeModel.setValue(0);
			updateScrolling(true);

			myClientScrollOrigin = myBoundedRangeModel.getValue();
		}
	}

	// @Override
	// public TerminalActionProvider getNextProvider() {
	// 	return myNextActionProvider;
	// }

	// @Override
	// public void setNextProvider(TerminalActionProvider provider) {
	// 	myNextActionProvider = provider;
	// }

	private boolean processTerminalKeyPressed(KeyEvent e) {
		if (hasUncommittedChars()) {
			return false;
		}

		try {
			final int keycode = e.getKeyCode();
			final char keychar = e.getKeyChar();

			// numLock does not change the code sent by keypad VK_DELETE
			// although it send the char '.'
			if (keycode == KeyEvent.VK_DELETE && keychar == '.') {
				myTerminalStarter.sendBytes(new byte[]{'.'}, true);
				return true;
			}
			// CTRL + Space is not handled in KeyEvent; handle it manually
			if (keychar == ' ' && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
				myTerminalStarter.sendBytes(new byte[]{Ascii.NUL}, true);
				return true;
			}

			final byte[] code = myTerminalStarter.getCode(keycode, e.getModifiers());
			if (code != null) {
				myTerminalStarter.sendBytes(code, true);
				if (mySettingsProvider.scrollToBottomOnTyping() && isCodeThatScrolls(keycode)) {
					scrollToBottom();
				}
				return true;
			}
			if (isAltPressedOnly(e) && Character.isDefined(keychar) && mySettingsProvider.altSendsEscape()) {
				// Cannot use e.getKeyChar() on macOS:
				//  Option+f produces e.getKeyChar()='ƒ' (402), but 'f' (102) is needed.
				//  Option+b produces e.getKeyChar()='∫' (8747), but 'b' (98) is needed.
				myTerminalStarter.sendString(new String(new char[]{Ascii.ESC, simpleMapKeyCodeToChar(e)}), true);
				return true;
			}
			if (Character.isISOControl(keychar)) { // keys filtered out here will be processed in processTerminalKeyTyped
				return processCharacter(e);
			}
		}
		catch (Exception ex) {
			LOG.error("Error sending pressed key to emulator", ex);
		}
		return false;
	}

	private static char simpleMapKeyCodeToChar(KeyEvent e) {
		// zsh requires proper case of letter
		if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
			return Character.toUpperCase((char) e.getKeyCode());
		}
		return Character.toLowerCase((char) e.getKeyCode());
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

		if (mySettingsProvider.scrollToBottomOnTyping()) {
			scrollToBottom();
		}
		return true;
	}

	private static boolean isCodeThatScrolls(int keycode) {
		return keycode == KeyEvent.VK_UP
						|| keycode == KeyEvent.VK_DOWN
						|| keycode == KeyEvent.VK_LEFT
						|| keycode == KeyEvent.VK_RIGHT
						|| keycode == KeyEvent.VK_BACK_SPACE
						|| keycode == KeyEvent.VK_INSERT
						|| keycode == KeyEvent.VK_DELETE
						|| keycode == KeyEvent.VK_ENTER
						|| keycode == KeyEvent.VK_HOME
						|| keycode == KeyEvent.VK_END
						|| keycode == KeyEvent.VK_PAGE_UP
						|| keycode == KeyEvent.VK_PAGE_DOWN;
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
			if (TerminalAction.processEvent(TerminalPanel.this, e) || processTerminalKeyPressed(e)) {
				e.consume();
				myIgnoreNextKeyTypedEvent = true;
			}
		}

		public void keyTyped(KeyEvent e) {
			if (myIgnoreNextKeyTypedEvent || processTerminalKeyTyped(e)) {
				e.consume();
			}
		}
	}

	private void handlePaste() {
		pasteFromClipboard(false);
	}

	private void handlePasteSelection() {
		pasteFromClipboard(true);
	}

	/**
	 * Copies selected text to clipboard.
	 * @param unselect true to unselect currently selected text
	 * @param useSystemSelectionClipboardIfAvailable true to use {@link Toolkit#getSystemSelection()} if available
	 */
	private void handleCopy(boolean unselect, boolean useSystemSelectionClipboardIfAvailable) {
		if (mySelection != null) {
			Pair<Point, Point> points = mySelection.pointsForRun(myTermSize.width);
			copySelection(points.first, points.second, useSystemSelectionClipboardIfAvailable);
			if (unselect) {
				mySelection = null;
				// repaint();
			}
		}
	}

	private boolean handleCopy(KeyEvent e) {
		boolean ctrlC = e != null && e.getKeyCode() == KeyEvent.VK_C && e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK;
		boolean sendCtrlC = ctrlC && mySelection == null;
		handleCopy(ctrlC, false);
		return !sendCtrlC;
	}

	private void handleCopyOnSelect() {
		handleCopy(false, true);
	}

	/**
	 * InputMethod implementation
	 * For details read http://docs.oracle.com/javase/7/docs/technotes/guides/imf/api-tutorial.html
	 */
	// @Override
	// protected void processInputMethodEvent(InputMethodEvent e) {
	// 	int commitCount = e.getCommittedCharacterCount();

	// 	if (commitCount > 0) {
	// 		myInputMethodUncommittedChars = null;
	// 		AttributedCharacterIterator text = e.getText();
	// 		if (text != null) {
	// 			StringBuilder sb = new StringBuilder();

	// 			//noinspection ForLoopThatDoesntUseLoopVariable
	// 			for (char c = text.first(); commitCount > 0; c = text.next(), commitCount--) {
	// 				if (c >= 0x20 && c != 0x7F) { // Hack just like in javax.swing.text.DefaultEditorKit.DefaultKeyTypedAction
	// 					sb.append(c);
	// 				}
	// 			}

	// 			if (sb.length() > 0) {
	// 				myTerminalStarter.sendString(sb.toString(), true);
	// 			}
	// 		}
	// 	} else {
	// 		myInputMethodUncommittedChars = uncommittedChars(e.getText());
	// 	}
	// }

	private static String uncommittedChars(AttributedCharacterIterator text) {
		if (text == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();

		for (char c = text.first(); c != CharacterIterator.DONE; c = text.next()) {
			if (c >= 0x20 && c != 0x7F) { // Hack just like in javax.swing.text.DefaultEditorKit.DefaultKeyTypedAction
				sb.append(c);
			}
		}

		return sb.toString();
	}

	// @Override
	// public InputMethodRequests getInputMethodRequests() {
	// 	return new MyInputMethodRequests();
	// }

	// private class MyInputMethodRequests implements InputMethodRequests {
	// 	@Override
	// 	public Rectangle getTextLocation(TextHitInfo offset) {
	// 		Rectangle r = new Rectangle(myCursor.getCoordX() * myCharSize.width + getInsetX(), (myCursor.getCoordY() + 1) * myCharSize.height,
	// 						0, 0);
	// 		Point p = TerminalPanel.this.getLocationOnScreen();
	// 		r.translate(p.x, p.y);
	// 		return r;
	// 	}

	// 	@Override
	// 	public TextHitInfo getLocationOffset(int x, int y) {
	// 		return null;
	// 	}

	// 	@Override
	// 	public int getInsertPositionOffset() {
	// 		return 0;
	// 	}

	// 	@Override
	// 	public AttributedCharacterIterator getCommittedText(int beginIndex, int endIndex, AttributedCharacterIterator.Attribute[] attributes) {
	// 		return null;
	// 	}

	// 	@Override
	// 	public int getCommittedTextLength() {
	// 		return 0;
	// 	}

	// 	@Override
	// 	public AttributedCharacterIterator cancelLatestCommittedText(AttributedCharacterIterator.Attribute[] attributes) {
	// 		return null;
	// 	}

	// 	@Override
	// 	public AttributedCharacterIterator getSelectedText(AttributedCharacterIterator.Attribute[] attributes) {
	// 		return null;
	// 	}

	// }

	public void dispose() {
		myRepaintTimer.stop();
	}
}
