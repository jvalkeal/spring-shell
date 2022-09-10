package com.jediterm.terminal.ui;

import java.awt.event.KeyEvent;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * @author traff
 */
public class TerminalAction {
  private final String myName;
  private final KeyStroke[] myKeyStrokes;
  private final Predicate<KeyEvent> myRunnable;

  private Character myMnemonic = null;
  private Supplier<Boolean> myEnabledSupplier = () -> true;
  private Integer myMnemonicKey = null;
  private boolean mySeparatorBefore = false;
  private boolean myHidden = false;

  public TerminalAction(TerminalActionPresentation presentation, Predicate<KeyEvent> runnable) {
    this(presentation.getName(), presentation.getKeyStrokes().toArray(new KeyStroke[0]), runnable);
  }

  public TerminalAction(TerminalActionPresentation presentation) {
    this(presentation, keyEvent -> true);
  }

  public TerminalAction(String name, KeyStroke[] keyStrokes, Predicate<KeyEvent> runnable) {
    myName = name;
    myKeyStrokes = keyStrokes;
    myRunnable = runnable;
  }

  public boolean matches(KeyEvent e) {
    for (KeyStroke ks : myKeyStrokes) {
      if (ks.equals(KeyStroke.getKeyStrokeForEvent(e))) {
        return true;
      }
    }
    return false;
  }

  public boolean isEnabled(KeyEvent e) {
    return myEnabledSupplier.get();
  }

  public boolean actionPerformed(KeyEvent e) {
    return myRunnable.test(e);
  }

  public int getKeyCode() {
    for (KeyStroke ks : myKeyStrokes) {
      return ks.getKeyCode();
    }
    return 0;
  }

  public int getModifiers() {
    for (KeyStroke ks : myKeyStrokes) {
      return ks.getModifiers();
    }
    return 0;
  }

  public String getName() {
    return myName;
  }

  public TerminalAction withMnemonic(Character ch) {
    myMnemonic = ch;
    return this;
  }

  public TerminalAction withMnemonicKey(Integer key) {
    myMnemonicKey = key;
    return this;
  }

  public TerminalAction withEnabledSupplier(Supplier<Boolean> enabledSupplier) {
    myEnabledSupplier = enabledSupplier;
    return this;
  }

  public TerminalAction separatorBefore(boolean enabled) {
    mySeparatorBefore = enabled;
    return this;
  }

  public JMenuItem toMenuItem() {
    JMenuItem menuItem = new JMenuItem(myName);

    if (myMnemonic != null) {
      menuItem.setMnemonic(myMnemonic);
    }
    if (myMnemonicKey != null) {
      menuItem.setMnemonic(myMnemonicKey);
    }

    if (myKeyStrokes.length > 0) {
      menuItem.setAccelerator(myKeyStrokes[0]);
    }

    menuItem.addActionListener(actionEvent -> actionPerformed(null));
    menuItem.setEnabled(isEnabled(null));

    return menuItem;
  }

  public boolean isSeparated() {
    return mySeparatorBefore;
  }

  public boolean isHidden() {
    return myHidden;
  }

  public TerminalAction withHidden(boolean hidden) {
    myHidden = hidden;
    return this;
  }

  @Override
  public String toString() {
    return "'" + myName + "'";
  }
}
