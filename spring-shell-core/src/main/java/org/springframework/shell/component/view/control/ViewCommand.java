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

/**
 * Commands which can be performed by the application or bound to keys in a
 * {@link View}. This class is a placeholder for constants of types usually
 * needed in a views. We've chosen this not to be enumerable so that there
 * would not be restrictions in an api's to use these types.
 *
 * @author Janne Valkealahti
 */
public final class ViewCommand {

	/**
	 * Move line up. Generic use in something where selection needs to be moved up.
	 */
	public static String LINE_UP = "LineUp";

	/**
	 * Move line down. Generic use in something where selection needs to be moved
	 * down.
	 */
	public static String LINE_DOWN = "LineDown";

	/**
	 * Open selected item. In a some sort of view where something can be selected
	 * and that active selected should be opened.
	 */
	public static String OPEN_SELECTED_ITEM = "OpenSelectedItem";

}
