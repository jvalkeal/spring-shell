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

import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Base implementation of a {@link View}.
 *
 * @author Janne Valkealahti
 */
public abstract class AbstractView implements View {

	private int x = 0;
	private int y = 0;
	private int width = 0;
	private int height = 0;
	private Consumer<String> inputConsumer;
	private BiFunction<VirtualDisplay, Rectangle, Rectangle> drawFunction;

	@Override
	public void setRect(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public Rectangle getRect() {
		return new Rectangle(x, y, width, height);
	}

	@Override
	public final void draw(VirtualDisplay display) {
		drawInternal(display);
	}

	@Override
	public void setInputConsumer(Consumer<String> inputConsumer) {
		this.inputConsumer = inputConsumer;
	}

	@Override
	public Consumer<String> getInputConsumer() {
		return inputConsumer;
	}

	public void setDrawFunction(BiFunction<VirtualDisplay, Rectangle, Rectangle> drawFunction) {
		this.drawFunction = drawFunction;
	}

	public BiFunction<VirtualDisplay, Rectangle, Rectangle> getDrawFunction() {
		return drawFunction;
	}

	protected void drawInternal(VirtualDisplay display) {
	}
}
