package com.jediterm.terminal.model.hyperlinks;

import com.jediterm.terminal.ui.TerminalAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author traff
 */
public class LinkInfo {
  private final Runnable myNavigateCallback;
  private final PopupMenuGroupProvider myPopupMenuGroupProvider;
  private final HoverConsumer myHoverConsumer;

  public LinkInfo( Runnable navigateCallback) {
    this(navigateCallback, null, null);
  }

  private LinkInfo( Runnable navigateCallback,
                    PopupMenuGroupProvider popupMenuGroupProvider,
                    LinkInfo.HoverConsumer hoverConsumer) {
    myNavigateCallback = navigateCallback;
    myPopupMenuGroupProvider = popupMenuGroupProvider;
    myHoverConsumer = hoverConsumer;
  }

  public void navigate() {
    myNavigateCallback.run();
  }

  public  PopupMenuGroupProvider getPopupMenuGroupProvider() {
    return myPopupMenuGroupProvider;
  }

  public  LinkInfo.HoverConsumer getHoverConsumer() {
    return myHoverConsumer;
  }

  public interface PopupMenuGroupProvider {
     List<TerminalAction> getPopupMenuGroup( MouseEvent event);
  }

  public interface HoverConsumer {
    /**
     * Gets called when the mouse cursor enters the link's bounds.
     * @param hostComponent terminal/console component containing the link
     * @param linkBounds link's bounds relative to {@code hostComponent}
     */
    void onMouseEntered( JComponent hostComponent,  Rectangle linkBounds);
    /**
     * Gets called when the mouse cursor exits the link's bounds.
     */
    void onMouseExited();
  }

  public static final class Builder {
    private Runnable myNavigateCallback;
    private PopupMenuGroupProvider myPopupMenuGroupProvider;
    private HoverConsumer myHoverConsumer;

    public  Builder setNavigateCallback( Runnable navigateCallback) {
      myNavigateCallback = navigateCallback;
      return this;
    }

    public  Builder setPopupMenuGroupProvider( PopupMenuGroupProvider popupMenuGroupProvider) {
      myPopupMenuGroupProvider = popupMenuGroupProvider;
      return this;
    }

    public  Builder setHoverConsumer( LinkInfo.HoverConsumer hoverConsumer) {
      myHoverConsumer = hoverConsumer;
      return this;
    }

    public  LinkInfo build() {
      return new LinkInfo(myNavigateCallback, myPopupMenuGroupProvider, myHoverConsumer);
    }
  }
}
