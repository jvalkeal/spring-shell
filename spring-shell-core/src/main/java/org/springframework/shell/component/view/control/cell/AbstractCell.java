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
package org.springframework.shell.component.view.control.cell;

import org.springframework.shell.component.view.control.Cell;

public abstract class AbstractCell<T> extends AbstractControl implements Cell<T> {

	private boolean selected;
	private T item;
	private int backgroundColor = -1;

	@Override
	public T getItem() {
		return item;
	}

	@Override
	public void setItem(T item) {
		this.item = item;
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void updateSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}
}
