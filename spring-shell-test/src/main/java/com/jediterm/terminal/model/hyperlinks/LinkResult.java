package com.jediterm.terminal.model.hyperlinks;

import java.util.List;

/**
 * @author traff
 */
public class LinkResult {
  private final List<LinkResultItem> myItemList;

  public LinkResult( LinkResultItem item) {
    this(List.of(item));
  }

  public LinkResult( List<LinkResultItem> itemList) {
    myItemList = itemList;
  }

  public List<LinkResultItem> getItems() {
    return myItemList;
  }
}
