/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.shell.component.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * {@code GridView} is a layout container with no initial {@link View views}.
 *
 * Loosely based on ideas from other grid layouts having features like rows and
 * columns, column and row spanning, dynamic layouts based on container size
 * using "CSS media queries" type of structure.
 *
 * @author Janne Valkealahti
 */
public class GridView extends BoxView {

	private List<GridItem> gridItems = new ArrayList<>();
	private int[] columnSize;
	private int[] rowSize;
	private int minWidth;
	private int minHeight;
	private int gapRows;
	private int gapColumns;
	private int rowOffset;
	private int columnOffset;
	public boolean borders;

	public GridView setColumnSize(int... columns) {
		this.columnSize = columns;
		return this;
	}

	public GridView setRowSize(int... rows) {
		this.rowSize = rows;
		return this;
	}

	public GridView addItem(View view, int row, int column, int rowSpan, int colSpan, int minGridHeight,
			int minGridWidth) {
		GridItem gridItem = new GridItem(view, row, column, colSpan, rowSpan, minGridHeight,
				minGridWidth, false);
		gridItems.add(gridItem);
		return this;
	}

	@Override
	protected void drawInternal(Screen screen) {
		Rectangle rect = getInnerRect();
		int x = rect.x();
		int y = rect.y();
		int width = rect.width();
		int height = rect.height();
		Dimension size = screen.getSize();
		int screenWidth = size.width();
		int screenHeight = size.height();

		Map<View, GridItem> items = new HashMap<>();
		for (GridItem item : gridItems) {
			item.visible = false;
			if (item.width <= 0 || item.height <= 0 || width < item.minGridWidth || height < item.minGridHeight) {
				continue;
			}
			GridItem previousItem = items.get(item.view);
			if (previousItem != null && item.minGridWidth < previousItem.minGridWidth
					&& item.minGridHeight < previousItem.minGridHeight) {
				continue;
			}
			items.put(item.view, item);
		}

		int rows = rowSize.length;
		int columns = columnSize.length;
		for (GridItem item : items.values()) {
			int rowEnd = item.row + item.height;
			if (rowEnd > rows) {
				rows = rowEnd;
			}
			int columnEnd = item.column + item.width;
			if (columnEnd > columns) {
				columns = columnEnd;
			}
		}
		if (rows == 0 || columns == 0) {
			return;
		}

		// Where are they located?

		int[] rowPos = new int[rows];
		int[] rowHeight = new int[rows];
		int[] columnPos = new int[columns];
		int[] columnWidth = new int[columns];

		// How much space do we distribute?

		int remainingWidth = width;
		int remainingHeight = height;
		int proportionalWidth = 0;
		int proportionalHeight = 0;


		for (int index = 0; index < rowSize.length; index++) {
			int row = rowSize[index];
			if (row > 0) {
				if (row < this.minHeight) {
					row = this.minHeight;
				}
				remainingHeight -= row;
				rowHeight[index] = row;
			}
			else if (row == 0) {
				proportionalHeight++;
			}
			else {
				proportionalHeight += -row;
			}
		}

		for (int index = 0; index < columnSize.length; index++) {
			int column = columnSize[index];
			if (column > 0) {
				if (column < this.minWidth) {
					column = this.minWidth;
				}
				remainingWidth -= column;
				columnWidth[index] = column;
			}
			else if (column == 0) {
				proportionalWidth++;
			}
			else {
				proportionalWidth += -column;
			}
		}

		if (this.borders) {
			remainingHeight -= rows + 1;
			remainingWidth -= columns + 1;
		}
		else {
			remainingHeight -= (rows - 1) * this.gapRows;
			remainingWidth -= (columns - 1) * this.gapColumns;
		}
		if (rows > this.rowSize.length) {
			proportionalHeight += rows - this.rowSize.length;
		}
		if (columns > this.columnSize.length) {
			proportionalWidth += columns - this.columnSize.length;
		}

		// Distribute proportional rows/columns.
		for (int index = 0; index < rows; index++) {
			int row = 0;
			if (index < this.rowSize.length) {
				row = this.rowSize[index];
			}
			if (row > 0) {
				continue;
			}
			else if (row == 0) {
				row = 1;
			}
			else {
				row = -row;
			}
			int rowAbs = row * remainingHeight / proportionalHeight;
			remainingHeight -= rowAbs;
			proportionalHeight -= row;
			if (rowAbs < this.minHeight) {
				rowAbs = this.minHeight;
			}
			rowHeight[index] = rowAbs;
		}

		for (int index = 0; index < columns; index++) {
			int column = 0;
			if (index < this.columnSize.length) {
				column = this.columnSize[index];
			}
			if (column > 0) {
				continue;
			}
			else if (column == 0) {
				column = 1;
			}
			else {
				column = -column;
			}
			int columnAbs = column * remainingWidth / proportionalWidth;
			remainingWidth -= columnAbs;
			proportionalWidth -= column;
			if (columnAbs < this.minWidth) {
				columnAbs = this.minWidth;
			}
			columnWidth[index] = columnAbs;
		}

		// Calculate row/column positions.
		int columnX = 0, rowY = 0;
		if (this.borders) {
			columnX++;
			rowY++;
		}
		for (int index = 0; index < rowHeight.length; index++) {
			int row = rowHeight[index];
			rowPos[index] = rowY;
			int gap = this.gapRows;
			if (this.borders) {
				gap = 1;
			}
			rowY += row + gap;
		}
		for (int index = 0; index < columnWidth.length; index++) {
			int column = columnWidth[index];
			columnPos[index] = columnX;
			int gap = this.gapColumns;
			if (this.borders) {
				gap = 1;
			}
			columnX += column + gap;
		}

		// Calculate primitive positions.
		GridItem focus = null;
		for (Entry<View, GridItem> entry : items.entrySet()) {
			View primitive = entry.getKey();
			GridItem item = entry.getValue();
			int px = columnPos[item.column];
			int py = rowPos[item.row];
			int pw = 0, ph = 0;
			for (int index = 0; index < item.height; index++) {
				ph += rowHeight[item.row + index];
			}
			for (int index = 0; index < item.width; index++) {
				pw += columnWidth[item.column + index];
			}
			if (this.borders) {
				pw += item.width - 1;
				ph += item.height - 1;
			}
			else {
				pw += (item.width - 1) * this.gapColumns;
				ph += (item.height -1) * this.gapRows;
			}
			item.x = px;
			item.y = py;
			item.w = pw;
			item.h = ph;
			item.visible = true;
			if (primitive.hasFocus()) {
				focus = item;
			}
		}

		// Calculate screen offsets.
		int offsetX = 0;
		int offsetY = 0;
		int add = 1;
		if (!this.borders) {
			add = this.gapRows;
		}
		for (int index = 0; index < rowHeight.length; index++) {
			int height2 = rowHeight[index];
			if (index >= this.rowOffset) {
				break;
			}
			offsetY += height2 + add;
		}
		if (!this.borders) {
			add = this.gapColumns;
		}
		for (int index = 0; index < columnWidth.length; index++) {
			int width2 = columnWidth[index];
			if (index >= this.columnOffset) {
				break;
			}
			offsetX += width2 + add;
		}

		// Line up the last row/column with the end of the available area.
		int border = 0;
		if (this.borders) {
			border = 1;
		}
		int last = rowPos.length - 1;
		if (rowPos[last] + rowHeight[last] + border - offsetY < height) {
			offsetY = rowPos[last] - height + rowHeight[last] + border;
		}
		last = columnPos.length - 1;
		if (columnPos[last] + columnWidth[last] + border - offsetX < width) {
			offsetX = columnPos[last] - width + columnWidth[last] + border;
		}

		// The focused item must be within the visible area.
		if (focus != null) {
			if (focus.y + focus.h - offsetY >= height) {
				offsetY = focus.y - height + focus.h;
			}
			if (focus.y - offsetY < 0) {
				offsetY = focus.y;
			}
			if (focus.x + focus.w - offsetX >= width) {
				offsetX = focus.x - width + focus.w;
			}
			if (focus.x - offsetX < 0) {
				offsetX = focus.x;
			}
		}

		// Adjust row/column offsets based on this value.
		int from = 0;
		int to = 0;
		for (int index = 0; index < rowPos.length; index ++) {
			int pos = rowPos[index];
			if (pos - offsetY < 0) {
				from = index + 1;
			}
			if (pos - offsetY < height) {
				to = index;
			}
		}
		if (this.rowOffset < from) {
			this.rowOffset = from;
		}
		if (this.rowOffset > to) {
			this.rowOffset = to;
		}

		from = 0;
		to = 0;
		for (int index = 0; index < columnPos.length; index ++) {
			int pos = columnPos[index];
			if (pos - offsetX < 0) {
				from = index + 1;
			}
			if (pos - offsetX < width) {
				to = index;
			}
		}
		if (this.columnOffset < from) {
			this.columnOffset = from;
		}
		if (this.columnOffset > to) {
			this.columnOffset = to;
		}

		// Draw primitives and borders.
		for (Entry<View, GridItem> entry : items.entrySet()) {
			View primitive = entry.getKey();
			GridItem item = entry.getValue();

			// Final primitive position.
			if (!item.visible) {
				continue;
			}

			item.x -= offsetX;
			item.y -= offsetY;
			if (item.x >= width || item.x + item.w <= 0 || item.y >= height || item.y + item.h <= 0) {
				item.visible = false;
				continue;
			}

			if (item.x + item.w > width) {
				item.w = width - item.x;
			}
			if (item.y + item.h > height) {
				item.h = height - item.y;
			}
			if (item.x < 0) {
				item.w += item.x;
				item.x = 0;
			}
			if (item.y < 0) {
				item.h += item.y;
				item.y = 0;
			}
			if (item.w <= 0 || item.h <= 0) {
				item.visible = false;
				continue;
			}

			item.x += x;
			item.y += y;
			primitive.setRect(item.x, item.y, item.w, item.h);

			// Draw primitive.
			// 	if item == focus {
			// 		defer primitive.Draw(screen)
			// 	} else {
			// 		primitive.Draw(screen)
			// 	}
			primitive.draw(screen);

			// Draw border around primitive.
			if (this.borders) {
				screen.printBorder(item.x - 1, item.y - 1, item.w + 2, item.h + 2);
			}
		}
	}

	private static class GridItem {
		View view;
		int row;
		int column;
		int width;
		int height;
		int minGridHeight;
		int minGridWidth;
		boolean visible;
		// The last position of the item relative to the top-left
		// corner of the grid. Undefined if visible is false.
		int x, y, w, h;

		GridItem(View view, int row, int column, int width, int height, int minGridHeight, int minGridWidth,
				boolean visible) {
			this.view = view;
			this.row = row;
			this.column = column;
			this.width = width;
			this.height = height;
			this.minGridHeight = minGridHeight;
			this.minGridWidth = minGridWidth;
			this.visible = visible;
		}
	}
}
