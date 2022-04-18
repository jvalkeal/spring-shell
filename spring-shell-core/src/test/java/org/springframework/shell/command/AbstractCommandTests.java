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
package org.springframework.shell.command;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;

import org.springframework.messaging.handler.annotation.Header;

public abstract class AbstractCommandTests {

	protected Pojo1 pojo1;

	protected Function<CommandContext, String> function1 = ctx -> {
		String arg1 = ctx.getOptionValue("arg1");
		return "hi" + arg1;
	};

	@BeforeEach
	public void setupAbstractCommandTests() {
		pojo1 = new Pojo1();
	}

	protected static class Pojo1 {

		public int method1Count;
		public CommandContext method1Ctx;
		public int method2Count;
		public int method3Count;
		public int method4Count;
		public String method4Arg1;
		public Boolean method5ArgA;
		public Boolean method5ArgB;
		public Boolean method5ArgC;

		public void method1(CommandContext ctx) {
			method1Ctx = ctx;
			method1Count++;
		}

		public String method2() {
			method2Count++;
			return "hi";
		}

		public String method3(@Header("arg1") String arg1) {
			method3Count++;
			return "hi" + arg1;
		}

		public String method4(String arg1) {
			method4Arg1 = arg1;
			method4Count++;
			return "hi" + arg1;
		}

		public void method5(@Header("a") boolean a, @Header("b") boolean b, @Header("c") boolean c) {
			method5ArgA = a;
			method5ArgB = b;
			method5ArgC = c;
		}
	}

}
