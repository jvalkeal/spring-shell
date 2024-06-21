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

import java.util.List;

import org.springframework.shell.ResultHandlerService;
import org.springframework.util.Assert;

/**
 * Default implementation of a {@link CommandRenderService}.
 *
 * Handling rendering using {@link CommandRenderer} and falling back to
 * {@link ResultHandlerService} until we've fully transitioned away from it.
 *
 * @author Janne Valkealahti
 */
public class DefaultCommandRenderService implements CommandRenderService {

	private ResultHandlerService resultHandlerService;
	private List<CommandRenderer> renderers;

	public DefaultCommandRenderService(ResultHandlerService resultHandlerService, List<CommandRenderer> renderers) {
		Assert.notNull(resultHandlerService, "resultHandlerService must be set");
		Assert.notNull(renderers, "renderers must be set with atleast empty list");
		this.resultHandlerService = resultHandlerService;
		this.renderers = renderers;
	}

	@Override
	public void render(CommandRenderContext ctx) {
		boolean handled = false;
		for (CommandRenderer renderer : renderers) {
			if (renderer.render(ctx)) {
				handled = true;
				break;
			}
		}
		if (!handled) {
			resultHandlerService.handle(ctx.result());
		}
	}

}
