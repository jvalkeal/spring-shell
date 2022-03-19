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
package org.springframework.shell.xxx;

import java.util.function.Function;

import org.springframework.messaging.handler.annotation.Header;

public abstract class AbstractCommandTests {

	protected Pojo1 pojo1 = new Pojo1();

	protected Function<CommandContext, String> function1 = ctx -> {
		String arg1 = ctx.getOptionValue("--arg1");
		return "hi" + arg1;
	};

	protected static class Pojo1 {

		public void method1() {
		}

		public String method2() {
			return null;
		}

		public String method3(@Header("--arg1") String arg1) {
			return "hi" + arg1;
		}

		public String method4(String arg1) {
			return null;
		}
	}

}
