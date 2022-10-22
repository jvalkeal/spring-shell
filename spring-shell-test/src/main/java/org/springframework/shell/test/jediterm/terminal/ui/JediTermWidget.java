package org.springframework.shell.test.jediterm.terminal.ui;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.shell.test.jediterm.terminal.Terminal;
import org.springframework.shell.test.jediterm.terminal.TerminalDisplay;
import org.springframework.shell.test.jediterm.terminal.TerminalStarter;
import org.springframework.shell.test.jediterm.terminal.TextStyle;
import org.springframework.shell.test.jediterm.terminal.TtyBasedArrayDataStream;
import org.springframework.shell.test.jediterm.terminal.TtyConnector;
import org.springframework.shell.test.jediterm.terminal.model.JediTerminal;
import org.springframework.shell.test.jediterm.terminal.model.LinesBuffer;
import org.springframework.shell.test.jediterm.terminal.model.StyleState;
import org.springframework.shell.test.jediterm.terminal.model.TerminalTextBuffer;

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

	public JediTermWidget(int columns, int lines) {

		StyleState styleState = createDefaultStyle();

		TerminalTextBuffer terminalTextBuffer = new TerminalTextBuffer(columns, lines, styleState,
				LinesBuffer.DEFAULT_MAX_LINES_COUNT);

		myTerminalPanel = createTerminalPanel(styleState, terminalTextBuffer);
		myTerminal = new JediTerminal(myTerminalPanel, terminalTextBuffer, styleState);

		// myTerminal.setModeEnabled(TerminalMode.AltSendsEscape, true);

		myTerminalPanel.setCoordAccessor(myTerminal);

		mySessionRunning.set(false);
	}

	protected StyleState createDefaultStyle() {
		StyleState styleState = new StyleState();
		// styleState.setDefaultStyle(new TextStyle(TerminalColor.BLACK, TerminalColor.WHITE));
		styleState.setDefaultStyle(new TextStyle());
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
