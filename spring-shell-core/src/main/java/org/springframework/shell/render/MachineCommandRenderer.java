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

import java.io.PrintWriter;

import org.jline.terminal.Terminal;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

/**
 * {@code CommandRenderer} rendering into format which is easier for machine
 * programs to read. Essentially this format is something like json, yaml or
 * anything else what can be easily parsed.
 *
 * @author Janne Valkealahti
 */
public class MachineCommandRenderer implements CommandRenderer {

	ConversionService conversionService;

	@Override
	public boolean render(CommandRenderContext ctx) {
		if (ctx.hasPty()) {
			return false;
		}
		else {
			renderMachine(ctx);
			return true;
		}
	}

	private void renderMachine(CommandRenderContext ctx) {
		Object result = ctx.result();
		TypeDescriptor sourceType = TypeDescriptor.valueOf(result.getClass());
		TypeDescriptor targetType = ctx.resultType();

		if (conversionService.canConvert(sourceType, targetType)) {
			Object convert = conversionService.convert(result, sourceType, targetType);
			TypeDescriptor outType = TypeDescriptor.valueOf(String.class);
			if (conversionService.canConvert(targetType, outType)) {
				String out = conversionService.convert(convert, String.class);
				write(ctx.terminal(), out);
			}
		}
	}

	protected void write(Terminal terminal, String out, boolean flush) {
		PrintWriter writer = terminal.writer();
		writer.write(out);
		if (flush) {
			writer.flush();
		}
	}

	protected void write(Terminal terminal, String out) {
		write(terminal, out, true);
	}

}
