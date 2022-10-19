package com.jediterm.terminal.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.concurrent.atomic.AtomicInteger;

import com.jediterm.terminal.CursorShape;
import com.jediterm.terminal.TerminalDisplay;
import com.jediterm.terminal.TerminalOutputStream;
import com.jediterm.terminal.TerminalStarter;
import com.jediterm.terminal.model.LinesBuffer;
import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalLine;
import com.jediterm.terminal.model.TerminalLineIntervalHighlighting;
import com.jediterm.terminal.model.TerminalSelection;
import com.jediterm.terminal.model.TerminalTextBuffer;

public class TerminalPanel implements TerminalDisplay {

	public static final double SCROLL_SPEED = 0.05;

	protected Dimension myCharSize = new Dimension();
	protected Dimension myTermSize = new Dimension(80, 24);
	private TerminalStarter myTerminalStarter = null;
	private TerminalSelection mySelection = null;
	private TerminalPanelListener myTerminalPanelListener;
	private final TerminalTextBuffer myTerminalTextBuffer;
	protected int myClientScrollOrigin;
	private String myWindowTitle = "Terminal";
	private AtomicInteger scrollDy = new AtomicInteger(0);
	private int myBlinkingPeriod = 500;
	private TerminalCoordinates myCoordsAccessor;

	public TerminalPanel(TerminalTextBuffer terminalTextBuffer, StyleState styleState) {
		myTerminalTextBuffer = terminalTextBuffer;
		myTermSize.width = terminalTextBuffer.getWidth();
		myTermSize.height = terminalTextBuffer.getHeight();
	}

	public TerminalPanelListener getTerminalPanelListener() {
		return myTerminalPanelListener;
	}

	public void setBlinkingPeriod(int blinkingPeriod) {
		myBlinkingPeriod = blinkingPeriod;
	}

	public void setCoordAccessor(TerminalCoordinates coordAccessor) {
		myCoordsAccessor = coordAccessor;
	}

	public void setTerminalStarter(final TerminalStarter terminalStarter) {
		myTerminalStarter = terminalStarter;
	}

	public void setTerminalPanelListener(final TerminalPanelListener terminalPanelListener) {
		myTerminalPanelListener = terminalPanelListener;
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

	public enum TerminalCursorState {
		SHOWING, HIDDEN, NO_FOCUS;
	}

	public class TerminalCursor {

		// cursor state
		protected Point myCursorCoordinates = new Point();

		// terminal modes
		private boolean myBlinking = true;

		public void setX(int x) {
			myCursorCoordinates.x = x;
			cursorChanged();
		}

		public void setY(int y) {
			myCursorCoordinates.y = y;
			cursorChanged();
		}

		public int getCoordX() {
			return myCursorCoordinates.x;
		}

		public int getCoordY() {
			return myCursorCoordinates.y - 1 - myClientScrollOrigin;
		}

		public void setShouldDrawCursor(boolean shouldDrawCursor) {
			// myShouldDrawCursor = shouldDrawCursor;
		}

		public void setBlinking(boolean blinking) {
			myBlinking = blinking;
		}

		public boolean isBlinking() {
			return myBlinking && (getBlinkingPeriod() > 0);
		}

		public void cursorChanged() {
			// myCursorHasChanged = true;
			// myLastCursorChange = System.currentTimeMillis();
			// repaint();
		}

		public void changeStateIfNeeded() {
		}

		void setShape(CursorShape shape) {
			// this.myShape = shape;
		}
	}

	private int getBlinkingPeriod() {
		if (myBlinkingPeriod != 505) {
			setBlinkingPeriod(505);
		}
		return myBlinkingPeriod;
	}

	// Called in a background thread with myTerminalTextBuffer.lock() acquired
	public void scrollArea(final int scrollRegionTop, final int scrollRegionSize, int dy) {
		scrollDy.addAndGet(dy);
		mySelection = null;
	}

	public void beep() {
		// if (mySettingsProvider.audibleBell()) {
		// 	Toolkit.getDefaultToolkit().beep();
		// }
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
		return false;
	}

	@Override
	public void setBracketedPasteMode(boolean enabled) {
		// myBracketedPasteMode = enabled;
	}

	public LinesBuffer getScrollBuffer() {
		return myTerminalTextBuffer.getHistoryBuffer();
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
						// myCursor.setY(1);
						myTerminalTextBuffer.addLine(lastLine);
					}
				}
				else {
					myTerminalTextBuffer.clearAll();
					myCoordsAccessor.setX(0);
					myCoordsAccessor.setY(1);
					// myCursor.setX(0);
					// myCursor.setY(1);
				}
			}

			// myBoundedRangeModel.setValue(0);
			// updateScrolling(true);

			// myClientScrollOrigin = myBoundedRangeModel.getValue();
		}
	}

	public void dispose() {
	}
}
