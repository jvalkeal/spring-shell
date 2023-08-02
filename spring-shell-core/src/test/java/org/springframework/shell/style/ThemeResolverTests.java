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

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.shell.component.view.screen.Color;
import org.springframework.shell.style.ThemeResolver.ResolvedValues;

import static org.assertj.core.api.Assertions.assertThat;

public class ThemeResolverTests {

	private ThemeResolver themeResolver;

	@BeforeEach
	public void setup() {
		ThemeRegistry themeRegistry = new ThemeRegistry();
		themeRegistry.register(new Theme() {
			@Override
			public String getName() {
				return "default";
			}

			@Override
			public ThemeSettings getSettings() {
				return ThemeSettings.defaults();
			}
		});
		themeResolver = new ThemeResolver(themeRegistry, "default");
	}

	@Test
	public void test() {
		assertThat(themeResolver.resolveStyleTag(StyleSettings.TAG_TITLE)).isEqualTo("bold");
		assertThat(themeResolver.resolveStyle("bold")).isEqualTo(AttributedStyle.BOLD);
		assertThat(themeResolver.evaluateExpression("@{bold foo}"))
				.isEqualTo(new AttributedString("foo", AttributedStyle.BOLD));
	}

	@Test
	public void resolveValues1() {
		// AttributedStyle s1 = AttributedStyle.DEFAULT.background(AttributedStyle.BLUE);
		// AttributedStyle s2 = AttributedStyle.DEFAULT.backgroundRgb(Color.BLUE);
		// AttributedStyle s3 = AttributedStyle.DEFAULT.background(AttributedStyle.BLUE).backgroundRgb(Color.BLUE);
		AttributedStyle s = AttributedStyle.DEFAULT.background(AttributedStyle.BLUE);
		ResolvedValues resolvedValues = themeResolver.resolveValues(s);
		assertThat(resolvedValues.background()).isEqualTo(AttributedStyle.BLUE);
	}

	@Test
	public void resolveValues2() {
		AttributedStyle s = AttributedStyle.DEFAULT.backgroundRgb(Color.BLUE);
		ResolvedValues resolvedValues = themeResolver.resolveValues(s);
		assertThat(resolvedValues.background()).isEqualTo(Color.BLUE);
	}

	@Test
	public void resolvexxx1() {
		AttributedStyle s = themeResolver.resolveStyle("bg-rgb:#0000FF");
		ResolvedValues resolvedValues = themeResolver.resolveValues(s);
		assertThat(resolvedValues.background()).isEqualTo(Color.BLUE);
	}

	@Test
	public void resolvexxx2() {
		AttributedStyle s = themeResolver.resolveStyle("bg:blue");
		ResolvedValues resolvedValues = themeResolver.resolveValues(s);
		assertThat(resolvedValues.background()).isEqualTo(AttributedStyle.BLUE);
	}

}
