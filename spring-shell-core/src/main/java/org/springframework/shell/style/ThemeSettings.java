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
package org.springframework.shell.style;

public abstract class ThemeSettings {

	private StyleSettings styleSettings;
	private FigureSettings figureSettings;

	public ThemeSettings(StyleSettings styleSettings, FigureSettings figureSettings) {
		this.styleSettings = styleSettings;
		this.figureSettings = figureSettings;
	}

	public StyleSettings styles() {
		return this.styleSettings;
	}

	public FigureSettings figures() {
		return this.figureSettings;
	}

	public static ThemeSettings themeSettings() {
		return new DefaultThemeSettings(StyleSettings.styleSettings(), FigureSettings.figureSettings());
	}

	public static ThemeSettings dump() {
		return new DefaultThemeSettings(StyleSettings.dump(), FigureSettings.dump());
	}

	private static class DefaultThemeSettings extends ThemeSettings {
		DefaultThemeSettings(StyleSettings styleSettings, FigureSettings figureSettings) {
			super(styleSettings, figureSettings);
		}
	}
}
