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

public interface HandlingResult {

	String message();

	public boolean isPresent();

	public boolean isEmpty();

	public static HandlingResult empty() {
		return of(null);
	}

	public static HandlingResult of(String message) {
		return new DefaultHandlingResult(message);
	}

	static class DefaultHandlingResult implements HandlingResult {

		private final String message;

		DefaultHandlingResult(String message) {
			this.message = message;
		}

		@Override
		public String message() {
			return message;
		}

		@Override
		public boolean isPresent() {
			return message != null;
		}

		@Override
		public boolean isEmpty() {
			return !isPresent();
		}
	}
}
