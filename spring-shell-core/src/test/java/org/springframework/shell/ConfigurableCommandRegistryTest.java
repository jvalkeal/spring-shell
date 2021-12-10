/*
 * Copyright 2017 the original author or authors.
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

package org.springframework.shell;

import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;
// import static org.junit.Assert.*;

// import org.junit.Rule;
// import org.junit.Test;
// import org.junit.rules.ExpectedException;

/**
 * Unit tests for {@link ConfigurableCommandRegistry}.
 *
 * @author Eric Bottard
 */
public class ConfigurableCommandRegistryTest {

	// @Rule
	// public ExpectedException thrown= ExpectedException.none();

	// @Test
	// public void testRegistration() {
	// 	ConfigurableCommandRegistry registry = new ConfigurableCommandRegistry();
	// 	registry.register("foo", MethodTarget.of("toString", this, new Command.Help("some command")));

	// 	assertThat(registry.listCommands(), hasKey("foo"));
	// }

	// @Test
	// public void testDoubleRegistration() {
	// 	ConfigurableCommandRegistry registry = new ConfigurableCommandRegistry();
	// 	registry.register("foo", MethodTarget.of("toString", this, new Command.Help("some command")));

	// 	thrown.expect(IllegalArgumentException.class);
	// 	thrown.expectMessage("foo");
	// 	thrown.expectMessage("toString");
	// 	thrown.expectMessage("hashCode");

	// 	registry.register("foo", MethodTarget.of("hashCode", this, new Command.Help("some command")));
	// }

}
