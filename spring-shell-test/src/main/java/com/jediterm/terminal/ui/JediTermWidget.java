package com.jediterm.terminal.ui;

import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicBoolean;

import com.jediterm.terminal.Terminal;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TerminalDisplay;
import com.jediterm.terminal.TerminalMode;
import com.jediterm.terminal.TerminalStarter;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.TtyBasedArrayDataStream;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.model.JediTerminal;
import com.jediterm.terminal.model.LinesBuffer;
import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalTextBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JediTermWidget implements TerminalSession, TerminalWidget {

	private static final Logger LOG = LoggerFactory.getLogger(JediTermWidget.class);

	protected final TerminalPanel myTerminalPanel;
	protected final JediTerminal myTerminal;
	protected final AtomicBoolean mySessionRunning = new AtomicBoolean();
	private TtyConnector myTtyConnector;
	private TerminalStarter myTerminalStarter;
	private Thread myEmuThread;

	public JediTermWidget() {
		this(80, 24);
	}

	public JediTermWidget(Dimension dimension) {
		this(dimension.width, dimension.height);
	}

	public JediTermWidget(int columns, int lines) {

		StyleState styleState = createDefaultStyle();

		TerminalTextBuffer terminalTextBuffer = new TerminalTextBuffer(columns, lines, styleState,
				LinesBuffer.DEFAULT_MAX_LINES_COUNT);

		myTerminalPanel = createTerminalPanel(styleState, terminalTextBuffer);
		myTerminal = new JediTerminal(myTerminalPanel, terminalTextBuffer, styleState);

		myTerminal.setModeEnabled(TerminalMode.AltSendsEscape, true);

		myTerminalPanel.setCoordAccessor(myTerminal);

		mySessionRunning.set(false);
	}

	protected StyleState createDefaultStyle() {
		StyleState styleState = new StyleState();
		styleState.setDefaultStyle(new TextStyle(TerminalColor.BLACK, TerminalColor.WHITE));
		return styleState;
	}

	protected TerminalPanel createTerminalPanel(StyleState styleState, TerminalTextBuffer terminalTextBuffer) {
		return new TerminalPanel(terminalTextBuffer, styleState);
	}

	public TerminalDisplay getTerminalDisplay() {
		return getTerminalPanel();
	}

	public TerminalPanel getTerminalPanel() {
		return myTerminalPanel;
	}

	public void setTtyConnector(TtyConnector ttyConnector) {
		myTtyConnector = ttyConnector;

		myTerminalStarter = createTerminalStarter(myTerminal, myTtyConnector);
		myTerminalPanel.setTerminalStarter(myTerminalStarter);
	}

	protected TerminalStarter createTerminalStarter(JediTerminal terminal, TtyConnector connector) {
		return new TerminalStarter(terminal, connector,
			new TtyBasedArrayDataStream(connector));
	}

	@Override
	public TtyConnector getTtyConnector() {
		return myTtyConnector;
	}

	@Override
	public Terminal getTerminal() {
		return myTerminal;
	}

	@Override
	public String getSessionName() {
		if (myTtyConnector != null) {
			return myTtyConnector.getName();
		} else {
			return "Session";
		}
	}

	public void start() {
		if (!mySessionRunning.get()) {
			myEmuThread = new Thread(new EmulatorTask());
			myEmuThread.start();
		} else {
			LOG.error("Should not try to start session again at this point... ");
		}
	}

	public void stop() {
		if (mySessionRunning.get() && myEmuThread != null) {
			myEmuThread.interrupt();
		}
	}

	public boolean isSessionRunning() {
		return mySessionRunning.get();
	}

	@Override
	public TerminalTextBuffer getTerminalTextBuffer() {
		return myTerminalPanel.getTerminalTextBuffer();
	}

	public boolean canOpenSession() {
		return !isSessionRunning();
	}

	@Override
	public void setTerminalPanelListener(TerminalPanelListener terminalPanelListener) {
		myTerminalPanel.setTerminalPanelListener(terminalPanelListener);
	}

	@Override
	public TerminalSession getCurrentSession() {
		return this;
	}

	@Override
	public JediTermWidget createTerminalSession(TtyConnector ttyConnector) {
		setTtyConnector(ttyConnector);
		return this;
	}

	@Override
	public void close() {
		stop();
		if (myTerminalStarter != null) {
			myTerminalStarter.close();
		}
		myTerminalPanel.dispose();
	}

	class EmulatorTask implements Runnable {
		public void run() {
			try {
				mySessionRunning.set(true);
				Thread.currentThread().setName("Connector-" + myTtyConnector.getName());
				if (myTtyConnector.init()) {
					// myTerminalPanel.addCustomKeyListener(myTerminalPanel.getTerminalKeyListener());
					myTerminalStarter.start();
				}
			} catch (Exception e) {
				LOG.error("Exception running terminal", e);
			} finally {
				try {
					myTtyConnector.close();
				} catch (Exception e) {
				}
				mySessionRunning.set(false);
				// myTerminalPanel.removeCustomKeyListener(myTerminalPanel.getTerminalKeyListener());
			}
		}
	}

	@Override
	public TerminalStarter getTerminalStarter() {
		return myTerminalStarter;
	}
}
