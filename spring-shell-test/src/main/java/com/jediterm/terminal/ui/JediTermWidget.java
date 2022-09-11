package com.jediterm.terminal.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicArrowButton;

import com.jediterm.terminal.SubstringFinder.FindResult;
import com.jediterm.terminal.SubstringFinder.FindResult.FindItem;
import com.jediterm.terminal.Terminal;
import com.jediterm.terminal.TerminalDisplay;
import com.jediterm.terminal.TerminalMode;
import com.jediterm.terminal.TerminalStarter;
import com.jediterm.terminal.TtyBasedArrayDataStream;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.model.JediTermDebouncerImpl;
import com.jediterm.terminal.model.JediTermTypeAheadModel;
import com.jediterm.terminal.model.JediTerminal;
import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.model.hyperlinks.HyperlinkFilter;
import com.jediterm.terminal.model.hyperlinks.TextProcessing;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.jediterm.typeahead.TerminalTypeAheadManager;
import com.jediterm.typeahead.TypeAheadTerminalModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JediTermWidget implements TerminalSession, TerminalWidget {

	private static final Logger LOG = LoggerFactory.getLogger(JediTermWidget.class);

	protected final TerminalPanel myTerminalPanel;
	protected final JediTerminal myTerminal;
	protected final AtomicBoolean mySessionRunning = new AtomicBoolean();
	private final JediTermTypeAheadModel myTypeAheadTerminalModel;
	private final TerminalTypeAheadManager myTypeAheadManager;
	private SearchComponent myFindComponent;
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

		myTypeAheadTerminalModel = new JediTermTypeAheadModel(myTerminal, terminalTextBuffer, settingsProvider);
		myTypeAheadManager = new TerminalTypeAheadManager(myTypeAheadTerminalModel);
		JediTermDebouncerImpl typeAheadDebouncer =
			new JediTermDebouncerImpl(myTypeAheadManager::debounce, TerminalTypeAheadManager.MAX_TERMINAL_DELAY);
		myTypeAheadManager.setClearPredictionsDebouncer(typeAheadDebouncer);
		myTerminalPanel.setTypeAheadManager(myTypeAheadManager);

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

	public TerminalTypeAheadManager getTypeAheadManager() {
		return myTypeAheadManager;
	}

	public void setTtyConnector(TtyConnector ttyConnector) {
		myTtyConnector = ttyConnector;

		TypeAheadTerminalModel.ShellType shellType = TypeAheadTerminalModel.ShellType.Unknown;
		myTypeAheadTerminalModel.setShellType(shellType);
		myTerminalStarter = createTerminalStarter(myTerminal, myTtyConnector);
		myTerminalPanel.setTerminalStarter(myTerminalStarter);
	}

	protected TerminalStarter createTerminalStarter(JediTerminal terminal, TtyConnector connector) {
		return new TerminalStarter(terminal, connector,
			new TtyBasedArrayDataStream(connector, myTypeAheadManager::onTerminalStateChanged), myTypeAheadManager);
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

	protected SearchComponent createSearchComponent() {
		return new SearchPanel();
	}

	protected interface SearchComponent {
		String getText();

		boolean ignoreCase();

		JComponent getComponent();

		void addDocumentChangeListener(DocumentListener listener);

		void addKeyListener(KeyListener listener);

		void addIgnoreCaseListener(ItemListener listener);

		void onResultUpdated(FindResult results);

		void nextFindResultItem(FindItem selectedItem);

		void prevFindResultItem(FindItem selectedItem);
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

	public class SearchPanel extends JPanel implements SearchComponent {

		private final JTextField myTextField = new JTextField();
		private final JLabel label = new JLabel();
		private final JButton prev;
		private final JButton next;
		private final JCheckBox ignoreCaseCheckBox = new JCheckBox("Ignore Case", true);

		public SearchPanel() {
			next = createNextButton();
			next.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					nextFindResultItem(myTerminalPanel.selectNextFindResultItem());
				}
			});

			prev = createPrevButton();
			prev.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					prevFindResultItem(myTerminalPanel.selectPrevFindResultItem());
				}
			});

			myTextField.setPreferredSize(new Dimension(
				myTerminalPanel.myCharSize.width * 30,
				myTerminalPanel.myCharSize.height + 3));
			myTextField.setEditable(true);

			updateLabel(null);

			add(myTextField);
			add(ignoreCaseCheckBox);
			add(label);
			add(next);
			add(prev);

			setOpaque(true);
		}

		protected JButton createNextButton() {
			return new BasicArrowButton(SwingConstants.NORTH);
		}

		protected JButton createPrevButton() {
			return new BasicArrowButton(SwingConstants.SOUTH);
		}

		@Override
		public void nextFindResultItem(FindItem selectedItem) {
			updateLabel(selectedItem);
		}

		@Override
		public void prevFindResultItem(FindItem selectedItem) {
			updateLabel(selectedItem);
		}

		private void updateLabel(FindItem selectedItem) {
			FindResult result = myTerminalPanel.getFindResult();
			label.setText(((selectedItem != null) ? selectedItem.getIndex() : 0)
				+ " of " + ((result != null) ? result.getItems().size() : 0));
		}

		@Override
		public void onResultUpdated(FindResult results) {
			updateLabel(null);
		}

		@Override
		public String getText() {
			return myTextField.getText();
		}

		@Override
		public boolean ignoreCase() {
			return ignoreCaseCheckBox.isSelected();
		}

		@Override
		public JComponent getComponent() {
			return this;
		}

		public void requestFocus() {
			myTextField.requestFocus();
		}

		@Override
		public void addDocumentChangeListener(DocumentListener listener) {
			myTextField.getDocument().addDocumentListener(listener);
		}

		@Override
		public void addKeyListener(KeyListener listener) {
			myTextField.addKeyListener(listener);
		}

		@Override
		public void addIgnoreCaseListener(ItemListener listener) {
			ignoreCaseCheckBox.addItemListener(listener);
		}

	}

	public void addHyperlinkFilter(HyperlinkFilter filter) {
		myTextProcessing.addHyperlinkFilter(filter);
	}
}
