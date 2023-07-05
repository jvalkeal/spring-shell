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
package org.springframework.shell.component.view.event;

import java.util.EnumSet;

import org.jline.terminal.MouseEvent;
import org.jline.terminal.MouseEvent.Button;
import org.jline.terminal.MouseEvent.Modifier;
import org.jline.terminal.MouseEvent.Type;

public record MouseBinding(Type type, Button button, EnumSet<Modifier> modifiers) {

	public static MouseBinding of(MouseEvent event) {
		return new MouseBinding(event.getType(), event.getButton(), event.getModifiers());
	}
}
