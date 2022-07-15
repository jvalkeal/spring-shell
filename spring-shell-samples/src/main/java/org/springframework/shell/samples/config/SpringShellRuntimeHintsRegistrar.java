package org.springframework.shell.samples.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

public class SpringShellRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		hints
			.proxies()
				.registerJdkProxy(b -> {
					b.proxiedInterfaces(TypeReference.of("com.sun.jna.Library"));
				});
	}

}
