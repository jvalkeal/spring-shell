package com.jediterm.terminal.ui;

import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicBoolean;

import com.jediterm.terminal.Terminal;
import com.jediterm.terminal.TerminalDisplay;
import com.jediterm.terminal.TerminalMode;
import com.jediterm.terminal.TerminalStarter;
import com.jediterm.terminal.TtyBasedArrayDataStream;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.model.JediTerminal;
import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.model.hyperlinks.HyperlinkFilter;
import com.jediterm.terminal.model.hyperlinks.TextProcessing;
import com.jediterm.terminal.ui.settings.SettingsProvider;
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
	protected final SettingsProvider mySettingsProvider;
	private final TextProcessing myTextProcessing;

	public JediTermWidget(SettingsProvider settingsProvider) {
		this(80, 24, settingsProvider);
	}

	public JediTermWidget(Dimension dimension, SettingsProvider settingsProvider) {
		this(dimension.width, dimension.height, settingsProvider);
	}

	public JediTermWidget(int columns, int lines, SettingsProvider settingsProvider) {
		mySettingsProvider = settingsProvider;

		StyleState styleState = createDefaultStyle();

		myTextProcessing = new TextProcessing(settingsProvider.getHyperlinkColor(),
			settingsProvider.getHyperlinkHighlightingMode());

		TerminalTextBuffer terminalTextBuffer = new TerminalTextBuffer(columns, lines, styleState,
				settingsProvider.getBufferMaxLinesCount(), myTextProcessing);
		myTextProcessing.setTerminalTextBuffer(terminalTextBuffer);

		myTerminalPanel = createTerminalPanel(mySettingsProvider, styleState, terminalTextBuffer);
		myTerminal = new JediTerminal(myTerminalPanel, terminalTextBuffer, styleState);

		myTerminal.setModeEnabled(TerminalMode.AltSendsEscape, mySettingsProvider.altSendsEscape());

		myTerminalPanel.setCoordAccessor(myTerminal);

		mySessionRunning.set(false);
	}

	protected StyleState createDefaultStyle() {
		StyleState styleState = new StyleState();
		styleState.setDefaultStyle(mySettingsProvider.getDefaultStyle());
		return styleState;
	}

	protected TerminalPanel createTerminalPanel(SettingsProvider settingsProvider, StyleState styleState, TerminalTextBuffer terminalTextBuffer) {
		return new TerminalPanel(settingsProvider, terminalTextBuffer, styleState);
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
					myTerminalPanel.addCustomKeyListener(myTerminalPanel.getTerminalKeyListener());
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
				myTerminalPanel.removeCustomKeyListener(myTerminalPanel.getTerminalKeyListener());
			}
		}
	}

	@Override
	public TerminalStarter getTerminalStarter() {
		return myTerminalStarter;
	}


	public void addHyperlinkFilter(HyperlinkFilter filter) {
		myTextProcessing.addHyperlinkFilter(filter);
	}
}
