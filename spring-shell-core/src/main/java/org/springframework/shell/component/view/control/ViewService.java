/*
 * Copyright 2023 the original author or authors.
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
package org.springframework.shell.component.view.control;

import org.springframework.shell.component.view.event.EventLoop;

public interface ViewService {

	EventLoop getEventLoop();

	View getModal();

	void setModal(View view);

	static ViewService of(EventLoop eventLoop) {
		return new DefaultViewService(eventLoop);
	}

	static class DefaultViewService implements ViewService {

		private EventLoop eventLoop;

		DefaultViewService(EventLoop eventLoop) {
			this.eventLoop = eventLoop;
		}

		@Override
		public EventLoop getEventLoop() {
			return eventLoop;
		}

		@Override
		public View getModal() {
			return null;
		}

		@Override
		public void setModal(View view) {
		}
	}
}
