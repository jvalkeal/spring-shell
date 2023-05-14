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
package org.springframework.shell.component.xxx;

public class Box extends AbstractViewComponent {

	private String title = null;
	private boolean showBorder = false;

	public void drawInternal(VirtualScreen virtualScreen) {
		if (getWidth() <= 0 || getHeight() <= 0) {
			return;
		}

		if (showBorder && getWidth() >= 2 && getHeight() >= 2) {

			for (int i = getX() + 1; i < getX() + getWidth() - 1; i++) {
				virtualScreen.setContent(i, getY(), '-');
				virtualScreen.setContent(i, getY() + getHeight() - 1, '-');
			}


			for (int i = getY() + 1; i < getY() + getHeight() - 1; i++) {
				virtualScreen.setContent(getX(), i, '|');
				virtualScreen.setContent(getX() + getWidth() - 1, i, '|');
			}



		}
	}

	public boolean isShowBorder() {
		return showBorder;
	}

	public void setShowBorder(boolean showBorder) {
		this.showBorder = showBorder;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
