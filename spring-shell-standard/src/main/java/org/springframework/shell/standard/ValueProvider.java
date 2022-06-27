/*
 * Copyright 2016-2022 the original author or authors.
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
package org.springframework.shell.standard;

import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.command.CommandOption;

/**
 * Beans implementing this interface are queried during TAB completion to gather possible values of a parameter.
 *
 * @author Eric Bottard
 * @author Janne Valkealahti
 */
public interface ValueProvider {

	boolean supports(CommandOption option, CompletionContext completionContext);
	List<CompletionProposal> complete(CommandOption option, CompletionContext completionContext);

	// boolean supports(MethodParameter parameter, CompletionContext completionContext);
	// List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext, String[] hints);
}
