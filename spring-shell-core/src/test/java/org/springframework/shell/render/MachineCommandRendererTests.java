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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class MachineCommandRendererTests {

	@Mock
	Terminal terminal;

	@Mock
	PrintWriter writer;

	TypeDescriptor stringType = TypeDescriptor.valueOf(String.class);

	@Test
	void doesNotRenderWithPty() {
		MachineCommandRenderer renderer = new MachineCommandRenderer();
		CommandRenderContext ctx = CommandRenderContext.of(null, null, true, null);
		assertThat(renderer.render(ctx)).isFalse();
	}


	@Test
	void doesRenderWithNoPty() {
		Mockito.when(terminal.writer()).thenReturn(writer);

		DefaultConversionService conversionService = new DefaultConversionService();
		MachineCommandRenderer renderer = new MachineCommandRenderer();
		renderer.conversionService = conversionService;
		CommandRenderContext ctx = CommandRenderContext.of("data", stringType, false, terminal);

		assertThat(renderer.render(ctx)).isTrue();
		Mockito.verify(writer).write("data");
		Mockito.verify(writer).flush();
	}

}
