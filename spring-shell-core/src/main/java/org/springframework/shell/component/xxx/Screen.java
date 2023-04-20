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
package org.springframework.shell.component.xxx;

import java.util.ArrayList;
import java.util.List;

import org.jline.utils.AttributedString;

import org.springframework.shell.component.xxx.View.Dimension;
import org.springframework.util.Assert;

/**
 * {@code VirtualScreen} is a buffer bound by size of rows and columns where
 * arbitraty data can be written before it is translated to more meaninful
 * representation used in a shell.
 *
 * @author Janne Valkealahti
 */
public class Screen {

	private int rows = 0;
	private int columns = 0;
	private char[][] data;

	public Screen() {
		this(0, 0);
	}

	public Screen(int rows, int columns) {
		resize(rows, columns);
	}

	public void resize(int rows, int columns) {
		Assert.isTrue(rows > -1, "Cannot have negative rows size");
		Assert.isTrue(columns > -1, "Cannot have negative columns size");
		this.rows = rows;
		this.columns = columns;
		reset();
	}

	public Dimension getSize() {
		return new Dimension(columns, rows);
	}

	public void reset() {
		this.data = new char[rows][columns];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				data[i][j] = ' ';
			}
		}
	}

	public void setContent(int x, int y, char c) {
		data[y][x] = c;
	}

	public char[][] getData() {
		return data;
	}

	public void print(String text, int x, int y, int width) {
		for (int i = 0; i < text.length() && i < width; i++) {
			char c = text.charAt(i);
			setContent(x + i, y, c);
		}
	}

	public List<AttributedString> getScreenLines() {
		List<AttributedString> newLines = new ArrayList<>();
		for (int i = 0; i < data.length; i++) {
			newLines.add(new AttributedString(new String(data[i])));
		}
		return newLines;
	}
}
