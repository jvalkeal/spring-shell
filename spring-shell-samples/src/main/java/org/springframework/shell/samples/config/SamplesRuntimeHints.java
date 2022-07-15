/*
 * Copyright 2022 the original author or authors.
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
package org.springframework.shell.samples.config;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.aot.hint.ProxyHints;
import org.springframework.aot.hint.ResourceHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

public class SamplesRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		ResourceHints resource = hints.resources();
		ProxyHints proxy = hints.proxies();
		registerResources(resource);
		registerProxies(proxy, "com.sun.jna.Library", "com.sun.jna.Callback",
				"org.jline.terminal.impl.jna.win.Kernel32", "org.jline.terminal.impl.jna.linux.CLibrary");
	}

	private void registerResources(ResourceHints resource) {
		resource.registerPattern("com/sun/jna/win32-x86-64/jnidispatch.dll");
	}

	private void registerProxies(ProxyHints proxy, String... classNames) {
		typeReferences(classNames)
			.forEach(tr -> proxy.registerJdkProxy(jdkProxyHint -> jdkProxyHint.proxiedInterfaces(tr)));
	}

	private Iterable<TypeReference> typeReferences(String... classNames) {
		return Stream.of(classNames).map(TypeReference::of).collect(Collectors.toList());
	}

}
