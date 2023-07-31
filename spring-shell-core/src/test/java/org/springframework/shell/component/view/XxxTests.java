package org.springframework.shell.component.view;

import org.jline.style.MemoryStyleSource;
import org.jline.style.StyleResolver;
import org.jline.utils.AttributedStyle;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class XxxTests {

	// 65793
	// 0x00010101 = 00000000 00000001 00000001 00000001

	// 549755781377
	//
	// F_BOLD = 0x00000001
	// GREEN = 2;
    // BLUE = 4;
	//
	//
	//

    static final long F_FOREGROUND_IND = 0x00000100;
    static final long F_FOREGROUND_RGB = 0x00000200;
    static final long F_FOREGROUND = F_FOREGROUND_IND | F_FOREGROUND_RGB;
    static final long F_BACKGROUND_IND = 0x00000400;
    static final long F_BACKGROUND_RGB = 0x00000800;
    static final long F_BACKGROUND = F_BACKGROUND_IND | F_BACKGROUND_RGB;

    static final int FG_COLOR_EXP = 15;
    static final int BG_COLOR_EXP = 39;
    static final long FG_COLOR = 0xFFFFFFL << FG_COLOR_EXP;
    static final long BG_COLOR = 0xFFFFFFL << BG_COLOR_EXP;

	@Test
	void test1() {
		MemoryStyleSource source = new MemoryStyleSource();
		StyleResolver styleResolver = new StyleResolver(source, "test");
		AttributedStyle attributedStyle = styleResolver.resolve("bold,fg:green,bg:blue");
		long style = attributedStyle.getStyle();

		long bold = style & ~(F_FOREGROUND | F_BACKGROUND);
		bold = (bold & 0x00007FFF);
		assertThat(bold).isEqualTo(1);

		long fg = (style & FG_COLOR) >> 15;
		assertThat(fg).isEqualTo(AttributedStyle.GREEN);

		long bg = (style & BG_COLOR) >> 39;
		assertThat(bg).isEqualTo(AttributedStyle.BLUE);

		// Long.toBinaryString(2199023322369l)
		// 0000000000000000000000100000000000000000000000010000010100000001
	}

	@Test
	void boldStyle() {
		// Long.toBinaryString(8191l) MASK = 0x00001FFF
		// 0000000000000000000000000000000000000000000000000001111111111111
		AttributedStyle s = new AttributedStyle().bold();
		long style = s.getStyle();
		long mask = s.getMask();
		// long color = (style & mask);
		long bold = (style & 0x00007FFF);
		assertThat(bold).isEqualTo(1);
	}

	@Test
	void foregroundColor() {
		// Long.toBinaryString(549755781120l) FG_COLOR
		// 0000000000000000000000000111111111111111111111111000000000000000
		AttributedStyle s = new AttributedStyle().foreground(AttributedStyle.BLUE);
		long style = s.getStyle();
		long mask = s.getMask();
		// long color = (style & mask) >> 15;
		long color = (style & FG_COLOR) >> 15;
		assertThat(color).isEqualTo(AttributedStyle.BLUE);
	}

	@Test
	void backgroundColor() {
		// Long.toBinaryString(9223371487098961920l) BG_COLOR
		// 0111111111111111111111111000000000000000000000000000000000000000
		AttributedStyle s = new AttributedStyle().background(AttributedStyle.BLUE);
		long style = s.getStyle();
		long mask = s.getMask();
		long color = (style & BG_COLOR) >> 39;
		assertThat(color).isEqualTo(AttributedStyle.BLUE);
	}

	@Test
	void test2() {
		AttributedStyle attributedStyle = new AttributedStyle();
		attributedStyle = attributedStyle.foreground(AttributedStyle.GREEN);
		long style = attributedStyle.getStyle();
		long mask = attributedStyle.getMask();
		long s1 = style &= mask;
		long s2 = style |= mask;
		long s3 = style ^= mask;
		assertThat(attributedStyle).isNotNull();
	}

	// bold
	// 0 | 1 = 1		0 | 1 = 1
	// boldOff
	// 0 & ~1 = 0		0 | 1 = 1
	//
	//
	//
	// Long.toBinaryString(549755781120l) FG_COLOR
	// 0000000000000000000000000111111111111111111111111000000000000000
	//
	// Long.toBinaryString(9223371487098961920l) BG_COLOR
	// 0111111111111111111111111000000000000000000000000000000000000000
	//
	//1000000000000
	//
	//
	//
	//
	//

}
