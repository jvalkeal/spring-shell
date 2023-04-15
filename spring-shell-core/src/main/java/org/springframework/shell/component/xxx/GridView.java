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

/**
 * {@code GridView} is a layout container with no initial {@link View views}.
 *
 * Loosely based on ideas from other grid layouts having features like rows and
 * columns, column and row spanning, dynamic layouts based on overall container
 * size using "CSS media queries" type of structure.
 *
 * @author Janne Valkealahti
 */
public class GridView extends AbstractView {

	private BoxView boxView;
	private List<GridItem> items = new ArrayList<>();

	public GridView() {
		this.boxView = new BoxView();
	}

	public record GridItem(View view, int row, int column, int width, int height) {
	}
}
