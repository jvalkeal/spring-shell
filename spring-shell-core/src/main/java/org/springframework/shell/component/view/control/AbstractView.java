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
package org.springframework.shell.component.view.control;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.jline.style.MemoryStyleSource;
import org.jline.style.StyleResolver;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.Disposables;

import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.shell.component.view.event.EventLoop;
import org.springframework.shell.component.view.event.KeyBindingConsumer;
import org.springframework.shell.component.view.event.KeyBindingConsumerArgs;
import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.component.view.event.KeyHandler;
import org.springframework.shell.component.view.event.MouseBindingConsumerArgs;
import org.springframework.shell.component.view.event.MouseBindingConsumer;
import org.springframework.shell.component.view.event.MouseEvent;
import org.springframework.shell.component.view.event.MouseHandler;
import org.springframework.shell.component.view.geom.Rectangle;
import org.springframework.shell.component.view.message.ShellMessageBuilder;
import org.springframework.shell.component.view.screen.Screen;
import org.springframework.shell.style.Theme;
import org.springframework.shell.style.ThemeResolver;
import org.springframework.shell.style.ThemeResolver.ResolvedValues;

/**
 * Base implementation of a {@link View} and its parent interface
 * {@link Control} providing some common functionality for implementations.
 *
 * @author Janne Valkealahti
 */
public abstract class AbstractView implements View {

	private final static Logger log = LoggerFactory.getLogger(AbstractView.class);
	private final Disposable.Composite disposables = Disposables.composite();
	private int x = 0;
	private int y = 0;
	private int width = 0;
	private int height = 0;
	private BiFunction<Screen, Rectangle, Rectangle> drawFunction;
	private boolean hasFocus;
	private int layer;
	private EventLoop eventLoop;
	private Map<Integer, KeyBindingValue> keyBindings = new HashMap<>();
	private Map<Integer, MouseBindingValue> mouseBindings = new HashMap<>();

	public AbstractView() {
		init();
	}

    // static final long F_FOREGROUND_IND = 0x00000100;
    // static final long F_FOREGROUND_RGB = 0x00000200;
    // static final long F_FOREGROUND = F_FOREGROUND_IND | F_FOREGROUND_RGB;
    // static final long F_BACKGROUND_IND = 0x00000400;
    // static final long F_BACKGROUND_RGB = 0x00000800;
    // static final long F_BACKGROUND = F_BACKGROUND_IND | F_BACKGROUND_RGB;

    // static final int FG_COLOR_EXP = 15;
    // static final int BG_COLOR_EXP = 39;
    // static final long FG_COLOR = 0xFFFFFFL << FG_COLOR_EXP;
    // static final long BG_COLOR = 0xFFFFFFL << BG_COLOR_EXP;

	// private Theme theme;
	// public void setTheme(Theme theme) {
	// 	this.theme = theme;
	// }
	// public Theme getTheme() {
	// 	return theme;
	// }
	// protected int themeStyle(String tag, int defaultStyle) {
	// 	if (theme == null) {
	// 		return defaultStyle;
	// 	}
	// 	String styleExp = theme.getSettings().styles().resolveTag(tag);
	// 	MemoryStyleSource source = new MemoryStyleSource();
	// 	StyleResolver styleResolver = new StyleResolver(source, "test");
	// 	AttributedStyle attributedStyle = styleResolver.resolve(styleExp);
	// 	long style = attributedStyle.getStyle();
	// 	long s = style & ~(F_FOREGROUND | F_BACKGROUND);
	// 	s = (s & 0x00007FFF);
	// 	return (int)s;
	// }

	private ThemeResolver themeResolver;
	private String themeName;
	public void setThemeResolver(ThemeResolver themeResolver) {
		this.themeResolver = themeResolver;
	}
	protected ThemeResolver getThemeResolver() {
		return themeResolver;
	}
	public void setThemeName(String themeName) {
		this.themeName = themeName;
	}
	protected String getThemeName() {
		return themeName;
	}
	protected int resolveThemeStyle(String tag, int defaultStyle) {
		if (themeResolver != null) {
			String styleTag = themeResolver.resolveStyleTag(tag, themeName);
			AttributedStyle attributedStyle = themeResolver.resolveStyle(styleTag);
			ResolvedValues resolvedValues = themeResolver.resolveValues(attributedStyle);
			return resolvedValues.style();
		}
		return defaultStyle;
	}
	protected int resolveThemeForeground(String tag, int defaultColor) {
		if (themeResolver != null) {
			AttributedStyle attributedStyle = themeResolver.resolveStyle(tag);
			ResolvedValues resolvedValues = themeResolver.resolveValues(attributedStyle);
			return resolvedValues.foreground();
		}
		return defaultColor;
	}

	/**
	 * Register {@link Disposable} to get disposed when view terminates.
	 *
	 * @param disposable a disposable to dispose
	 */
	protected void onDestroy(Disposable disposable) {
		disposables.add(disposable);
	}

	/**
	 * Cleans running state of a {@link View} so that it can be left to get garbage
	 * collected.
	 */
	public void destroy() {
		disposables.dispose();
	}

	/**
	 * Initialize a view. Mostly reserved for future use and simply calls
	 * {@link #initInternal()}.
	 *
	 * @see #initInternal()
	 */
	protected final void init() {
		initInternal();
	}

	/**
	 * Internal init method called from {@link #init()}. Override to do something
	 * usefull. Typically key and mousebindings are registered from this method.
	 */
	protected void initInternal() {
	}

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
	public void setLayer(int index) {
		this.layer = index;
	}

	protected int getLayer() {
		return layer;
	}

	@Override
	public final void draw(Screen screen) {
		drawInternal(screen);
	}

	/**
	 * Component internal drawing method. Implementing classes needs to define this
	 * method to draw something into a {@link Screen}.
	 *
	 * @param screen the screen
	 */
	protected abstract void drawInternal(Screen screen);

	@Override
	public void focus(View view, boolean focus) {
		log.debug("Focus view={} focus={}", view, focus);
		if (view == this && focus) {
			hasFocus = true;
		}
		if (!focus) {
			hasFocus = false;
		}
	}

	@Override
	public boolean hasFocus() {
		return hasFocus;
	}

	/**
	 * Handles mouse events by dispatching registered consumers into an event loop.
	 * Override to change default behaviour.
	 */
	@Override
	public MouseHandler getMouseHandler() {
		log.trace("getMouseHandler() {}", this);
		MouseHandler handler = args -> {
			MouseEvent event = args.event();
			int mouse = event.mouse();
			View view = null;
			boolean consumed = false;
			MouseBindingValue mouseBindingValue = getMouseBindings().get(mouse);
			if (mouseBindingValue != null) {
				if (mouseBindingValue.mousePredicate().test(event)) {
					view = this;
					consumed = dispatchMouseRunCommand(event, mouseBindingValue);
				}
			}
			return MouseHandler.resultOf(args.event(), consumed, view, this);
		};
		return handler;
	}

	/**
	 * Handles keys by dispatching registered command runnable into an event loop.
	 * Override to change default behaviour.
	 */
	@Override
	public KeyHandler getKeyHandler() {
		log.trace("getKeyHandler() {}", this);
		KeyHandler handler = args -> {
			KeyEvent event = args.event();
			boolean consumed = false;
			Integer key = event.key();
			if (key != null) {
				KeyBindingValue keyBindingValue = getKeyBindings().get(key);
				if (keyBindingValue != null) {
					consumed = dispatchRunCommand(event, keyBindingValue);
				}

			}
			return KeyHandler.resultOf(event, consumed, null);
		};
		return handler;
	}

	/**
	 * Sets a callback function which is invoked after a {@link View} has been
	 * drawn.
	 *
	 * @param drawFunction the draw function
	 */
	public void setDrawFunction(BiFunction<Screen, Rectangle, Rectangle> drawFunction) {
		this.drawFunction = drawFunction;
	}

	/**
	 * Gets a draw function.
	 *
	 * @return null if function is not set
	 * @see #setDrawFunction(BiFunction)
	 */
	public BiFunction<Screen, Rectangle, Rectangle> getDrawFunction() {
		return drawFunction;
	}

	/**
	 * Set an {@link EventLoop}.
	 *
	 * @param eventLoop the event loop
	 */
	public void setEventLoop(@Nullable EventLoop eventLoop) {
		this.eventLoop = eventLoop;
	}

	/**
	 * Get an {@link EventLoop}.
	 *
	 * @return event loop
	 */
	protected EventLoop getEventLoop() {
		return eventLoop;
	}

	protected void registerKeyBinding(Integer keyType, String keyCommand) {
		registerKeyBinding(keyType, keyCommand, null, null);
	}

	protected void registerKeyBinding(Integer keyType, KeyBindingConsumer keyConsumer) {
		registerKeyBinding(keyType, null, keyConsumer, null);
	}

	protected void registerKeyBinding(Integer keyType, Runnable keyRunnable) {
		registerKeyBinding(keyType, null, null, keyRunnable);
	}

	private void registerKeyBinding(Integer keyType, String keyCommand, KeyBindingConsumer keyConsumer, Runnable keyRunnable) {
		keyBindings.compute(keyType, (key, old) -> {
			return KeyBindingValue.of(old, keyCommand, keyConsumer, keyRunnable);
		});
	}

	record KeyBindingValue(String keyCommand, KeyBindingConsumer keyConsumer, Runnable keyRunnable) {
		static KeyBindingValue of(KeyBindingValue old, String keyCommand, KeyBindingConsumer keyConsumer,
				Runnable keyRunnable) {
			if (old == null) {
				return new KeyBindingValue(keyCommand, keyConsumer, keyRunnable);
			}
			return new KeyBindingValue(keyCommand != null ? keyCommand : old.keyCommand(),
					keyConsumer != null ? keyConsumer : old.keyConsumer(),
					keyRunnable != null ? keyRunnable : old.keyRunnable());
		}
	}

	/**
	 * Get key bindings.
	 *
	 * @return key bindings
	 */
	protected Map<Integer, KeyBindingValue> getKeyBindings() {
		return keyBindings;
	}

	record MouseBindingValue(String mouseCommand, MouseBindingConsumer mouseConsumer, Runnable mouseRunnable,
			Predicate<MouseEvent> mousePredicate) {
		static MouseBindingValue of(MouseBindingValue old, String mouseCommand, MouseBindingConsumer mouseConsumer,
				Runnable mouseRunnable, Predicate<MouseEvent> mousePredicate) {
			if (old == null) {
				return new MouseBindingValue(mouseCommand, mouseConsumer, mouseRunnable, mousePredicate);
			}
			return new MouseBindingValue(mouseCommand != null ? mouseCommand : old.mouseCommand(),
					mouseConsumer != null ? mouseConsumer : old.mouseConsumer(),
					mouseRunnable != null ? mouseRunnable : old.mouseRunnable(),
					mousePredicate != null ? mousePredicate : old.mousePredicate());
		}
	}

	/**
	 * Get mouse bindings.
	 *
	 * @return mouse bindings
	 */
	protected Map<Integer, MouseBindingValue> getMouseBindings() {
		return mouseBindings;
	}

	protected void registerMouseBinding(Integer keyType, String mouseCommand) {
		registerMouseBinding(keyType, mouseCommand, null, null);
	}

	protected void registerMouseBinding(Integer keyType, MouseBindingConsumer mouseConsumer) {
		registerMouseBinding(keyType, null, mouseConsumer, null);
	}

	protected void registerMouseBinding(Integer keyType, Runnable mouseRunnable) {
		registerMouseBinding(keyType, null, null, mouseRunnable);
	}

	private void registerMouseBinding(Integer mouseType, String mouseCommand, MouseBindingConsumer mouseConsumer, Runnable mouseRunnable) {
		Predicate<MouseEvent> mousePredicate = event -> {
			int x = event.x();
			int y = event.y();
			return getRect().contains(x, y);
		};
		mouseBindings.compute(mouseType, (key, old) -> {
			return MouseBindingValue.of(old, mouseCommand, mouseConsumer, mouseRunnable, mousePredicate);
		});
	}

	/**
	 * Dispatch a {@link Message} into an event loop.
	 *
	 * @param message the message to dispatch
	 */
	protected void dispatch(Message<?> message) {
		if (eventLoop != null) {
			eventLoop.dispatch(message);
		}
		else {
			log.warn("Can't dispatch message {} as eventloop is not set", message);
		}
	}

	protected boolean dispatchRunnable(Runnable runnable) {
		if (eventLoop == null) {
			return false;
		}
		Message<Runnable> message = ShellMessageBuilder
			.withPayload(runnable)
			.setEventType(EventLoop.Type.TASK)
			.build();
		dispatch(message);
		return true;
	}

	protected boolean dispatchRunCommand(KeyEvent event, KeyBindingValue command) {
		if (eventLoop == null) {
			return false;
		}
		Runnable runnable = command.keyRunnable();
		if (runnable != null) {
			Message<Runnable> message = ShellMessageBuilder
				.withPayload(runnable)
				.setEventType(EventLoop.Type.TASK)
				.build();
			dispatch(message);
			return true;
		}
		KeyBindingConsumer keyConsumer = command.keyConsumer();
		if (keyConsumer != null) {
			Message<KeyBindingConsumerArgs> message = ShellMessageBuilder
				.withPayload(new KeyBindingConsumerArgs(keyConsumer, event))
				.setEventType(EventLoop.Type.TASK)
				.build();
			dispatch(message);
			return true;
		}
		return false;
	}

	protected boolean dispatchMouseRunCommand(MouseEvent event, MouseBindingValue command) {
		if (eventLoop == null) {
			return false;
		}
		Runnable runnable = command.mouseRunnable();
		if (runnable != null) {
			Message<Runnable> message = ShellMessageBuilder
				.withPayload(runnable)
				.setEventType(EventLoop.Type.TASK)
				.build();
			dispatch(message);
			return true;
		}
		MouseBindingConsumer mouseConsumer = command.mouseConsumer();
		if (mouseConsumer != null) {
			Message<MouseBindingConsumerArgs> message = ShellMessageBuilder
				.withPayload(new MouseBindingConsumerArgs(mouseConsumer, event))
				.setEventType(EventLoop.Type.TASK)
				.build();
			dispatch(message);
			return true;
		}
		return false;
	}
}
