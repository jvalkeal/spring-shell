package com.jediterm.terminal.ui;

import java.util.Collections;
import java.util.List;

import javax.swing.KeyStroke;

public class TerminalActionPresentation {
	private final String myName;
	private final List<KeyStroke> myKeyStrokes;

	public TerminalActionPresentation(String name, KeyStroke keyStroke) {
		this(name, Collections.singletonList(keyStroke));
	}

	public TerminalActionPresentation(String name, List<KeyStroke> keyStrokes) {
		myName = name;
		myKeyStrokes = keyStrokes;
	}

	public String getName() {
		return myName;
	}

	public List<KeyStroke> getKeyStrokes() {
		return myKeyStrokes;
	}
}
