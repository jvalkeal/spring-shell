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
package org.springframework.shell.component.view.listener;

import java.util.List;

public interface CompositeListener<T> {

	/**
	 * Sets the list of listeners. This clears
	 * all existing listeners.
	 *
	 * @param listeners the new listeners
	 */
	public void setListeners(List<? extends T> listeners);

	/**
	 * Register a new listener.
	 *
	 * @param listener the listener
	 */
	public void register(T listener);

	/**
	 * Unregister a listener.
	 *
	 * @param listener the listener
	 */
	public void unregister(T listener);

	/**
	 * Gets the listeners.
	 *
	 * @return the listeners
	 */
	public OrderedComposite<T> getListeners();
}
