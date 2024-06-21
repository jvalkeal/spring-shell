/*
 * Copyright 2024 the original author or authors.
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
package org.springframework.shell.render;

import org.jline.terminal.Terminal;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.Nullable;

/**
 * Context interface used in {@link CommandRenderService} and
 * {@link CommandRenderer} to render command result.
 *
 * @author Janne Valkealahti
 */
public interface CommandRenderContext {

	@Nullable
	Object result();

	@Nullable
	TypeDescriptor resultType();

	boolean hasPty();

	Terminal terminal();

	record DefaultCommandRenderContext(Object result, TypeDescriptor resultType, boolean hasPty, Terminal terminal)
			implements CommandRenderContext {
	}

	// static class DefaultCommandRenderContext implements CommandRenderContext {

	// 	private Object result;
	// 	private TypeDescriptor type;
	// 	private boolean hasPty;
	// 	private Terminal terminal;

	// 	DefaultCommandRenderContext(Object result, TypeDescriptor type, boolean hasPty, Terminal terminal) {
	// 		this.result = result;
	// 		this.type = type;
	// 		this.hasPty = hasPty;
	// 		this.terminal = terminal;
	// 	}

	// 	@Override
	// 	public Object result() {
	// 		return result;
	// 	}

	// 	@Override
	// 	public TypeDescriptor resultType() {
	// 		return type;
	// 	}

	// 	@Override
	// 	public boolean hasPty() {
	// 		return hasPty;
	// 	}

	// 	@Override
	// 	public Terminal terminal() {
	// 		return terminal;
	// 	}
	// }

	static CommandRenderContext of(Object result, TypeDescriptor typeDescriptor, boolean hasPty, Terminal terminal) {
		// return new DefaultCommandRenderContext(result, typeDescriptor, hasPty, terminal);
		return new DefaultCommandRenderContext(result, typeDescriptor, hasPty, terminal);
	}
}
