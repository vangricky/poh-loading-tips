package com.pohloadingtips;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Draws the replacement loading screen, styled after the classic OSRS
 * interfaces: dark brown stone panel, orange titles and yellow text with
 * a black drop shadow.
 */
class PohLoadingTipsOverlay extends Overlay
{
	// Classic OSRS interface palette
	private static final Color SCREEN_TOP = new Color(0x1E1912);
	private static final Color SCREEN_BOTTOM = new Color(0x0B0906);
	private static final Color PANEL_FILL = new Color(0x3E3529);
	private static final Color PANEL_FILL_DARK = new Color(0x2E271E);
	private static final Color BORDER_OUTER = new Color(0x0E0E0C);
	private static final Color BORDER_LIGHT = new Color(0x5A5245);
	private static final Color BORDER_DARK = new Color(0x241F18);
	private static final Color ORANGE = new Color(0xFF981F);   // titles
	private static final Color YELLOW = new Color(0xFFFF00);   // body text
	private static final Color MUTED_GOLD = new Color(0xC8AA6E); // labels
	private static final Color SHADOW = Color.BLACK;

	private static final int MARGIN = 20;
	private static final int PANEL_MAX_WIDTH = 620;

	private final Client client;
	private final PohLoadingTipsPlugin plugin;
	private final PohLoadingTipsConfig config;

	private final ItemManager itemManager;

	@Inject
	PohLoadingTipsOverlay(Client client, PohLoadingTipsPlugin plugin, PohLoadingTipsConfig config, ItemManager itemManager)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.itemManager = itemManager;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(PRIORITY_HIGHEST);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		Meme meme = plugin.getCurrentMeme();
		if (meme != null)
		{
			renderMemeCard(g, meme);
			return null;
		}

		Tip tip = plugin.getCurrentTip();
		if (tip == null)
		{
			return null;
		}

		if (plugin.isLoadingScreenVisible())
		{
			Widget w = plugin.getPohLoadingWidget();
			Rectangle area = w != null ? w.getBounds() : null;
			if (area == null || area.width < 50 || area.height < 50)
			{
				area = viewport();
			}
			renderLoadingScreen(g, area, tip);
		}
		return null;
	}

	// ---------------------------------------------------------------- memes

	private static final Color MEME_TEXT = Color.WHITE;

	private void renderMemeCard(Graphics2D g, Meme meme)
	{
		if (System.currentTimeMillis() < plugin.getPreviewUntil())
		{
			renderMemeScreen(g, viewport(), meme); // ::pohmeme preview
			return;
		}
		if (plugin.isLoadingScreenVisible())
		{
			Widget w = plugin.getPohLoadingWidget();
			Rectangle area = w != null ? w.getBounds() : null;
			if (area == null || area.width < 50 || area.height < 50)
			{
				area = viewport();
			}
			renderMemeScreen(g, area, meme);
		}
	}

	private void renderMemeScreen(Graphics2D g, Rectangle area, Meme meme)
	{
		Paint oldPaint = g.getPaint();
		g.setPaint(new GradientPaint(0, area.y, SCREEN_TOP, 0, area.y + area.height, SCREEN_BOTTOM));
		g.fillRect(area.x, area.y, area.width, area.height);
		g.setPaint(oldPaint);

		int panelW = Math.min(area.width - MARGIN * 2, PANEL_MAX_WIDTH);
		int pad = 16;
		int textW = panelW - pad * 2;
		int n = meme.getItemIds().size();

		// Sprites are 36x32; scale up by a whole number so the pixel art stays crisp.
		int scale = 4;
		while (scale > 1 && (n * 36 * scale + (n - 1) * 12 > textW || 32 * scale > area.height / 3))
		{
			scale--;
		}
		int spriteW = 36 * scale;
		int spriteH = 32 * scale;

		int size = Math.max(16, config.textSize() + 4);
		Font font;
		List<String> top;
		List<String> bottom;
		FontMetrics fm;
		do
		{
			font = FontManager.getRunescapeBoldFont().deriveFont(Font.PLAIN, (float) size);
			fm = g.getFontMetrics(font);
			top = wrap(meme.getTop(), fm, textW);
			bottom = wrap(meme.getBottom(), fm, textW);
			size -= 2;
		}
		while ((top.size() + bottom.size()) * fm.getHeight() + spriteH + pad * 4 > area.height - MARGIN * 2 && size >= 14);

		int panelH = pad + top.size() * fm.getHeight() + pad + spriteH + pad + bottom.size() * fm.getHeight() + pad;
		int px = area.x + (area.width - panelW) / 2;
		int py = area.y + (area.height - panelH) / 2;
		drawStonePanel(g, px, py, panelW, panelH);

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setFont(font);
		int y = py + pad;
		for (String line : top)
		{
			drawOutlined(g, line, px, panelW, y + fm.getAscent(), scale > 2 ? 2 : 1);
			y += fm.getHeight();
		}
		y += pad;

		Object oldInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		int rowW = n * spriteW + (n - 1) * 12;
		int sx = px + (panelW - rowW) / 2;
		for (int id : meme.getItemIds())
		{
			BufferedImage img = itemManager.getImage(id);
			if (img != null)
			{
				g.drawImage(img, sx, y, spriteW, spriteH, null);
			}
			sx += spriteW + 12;
		}
		if (oldInterp != null) g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterp);
		y += spriteH + pad;

		for (String line : bottom)
		{
			drawOutlined(g, line, px, panelW, y + fm.getAscent(), scale > 2 ? 2 : 1);
			y += fm.getHeight();
		}
	}


	/** Classic meme text: white with a black outline, centred. */
	private static void drawOutlined(Graphics2D g, String s, int x, int w, int baseline, int outline)
	{
		FontMetrics fm = g.getFontMetrics();
		int sx = x + (w - fm.stringWidth(s)) / 2;
		g.setColor(Color.BLACK);
		for (int dx = -outline; dx <= outline; dx++)
		{
			for (int dy = -outline; dy <= outline; dy++)
			{
				if (dx != 0 || dy != 0) g.drawString(s, sx + dx, baseline + dy);
			}
		}
		g.setColor(MEME_TEXT);
		g.drawString(s, sx, baseline);
	}

	private Rectangle viewport()
	{
		return new Rectangle(client.getViewportXOffset(), client.getViewportYOffset(),
			client.getViewportWidth(), client.getViewportHeight());
	}

	// ---------------------------------------------------------------- full screen

	private void renderLoadingScreen(Graphics2D g, Rectangle area, Tip tip)
	{
		Paint oldPaint = g.getPaint();

		// Cover the broken-house art completely.
		g.setPaint(new GradientPaint(0, area.y, SCREEN_TOP, 0, area.y + area.height, SCREEN_BOTTOM));
		g.fillRect(area.x, area.y, area.width, area.height);
		g.setPaint(oldPaint);

		int panelW = Math.min(area.width - MARGIN * 2, PANEL_MAX_WIDTH);
		int pad = 18;
		int textW = panelW - pad * 2;
		int availH = area.height - MARGIN * 2;

		// Shrink text until everything fits (matters in fixed mode).
		int size = config.textSize();
		Layout layout;
		do
		{
			layout = new Layout(g, tip, size, textW);
			size -= 2;
		}
		while (layout.height(pad) > availH && size >= 12);

		int panelH = Math.min(layout.height(pad), availH);
		int px = area.x + (area.width - panelW) / 2;
		int py = area.y + (area.height - panelH) / 2;
		drawStonePanel(g, px, py, panelW, panelH);

		int y = py + pad;
		// Small label: "Construction"
		g.setFont(layout.labelFont);
		drawCentered(g, tip.getCategory(), px, panelW, y + layout.labelFm.getAscent(), MUTED_GOLD);
		y += layout.labelFm.getHeight() + 2;

		// Heading: "Did you know?" / "Quick Tip"
		g.setFont(layout.headFont);
		drawCentered(g, tip.getKind().getHeading(), px, panelW, y + layout.headFm.getAscent(), ORANGE);
		y += layout.headFm.getHeight() + 6;

		// Divider
		g.setColor(BORDER_DARK);
		g.drawLine(px + pad, y, px + panelW - pad, y);
		g.setColor(BORDER_LIGHT);
		g.drawLine(px + pad, y + 1, px + panelW - pad, y + 1);
		y += 12;

		// Body
		g.setFont(layout.bodyFont);
		for (String line : layout.lines)
		{
			drawCentered(g, line, px, panelW, y + layout.bodyFm.getAscent(), YELLOW);
			y += layout.bodyFm.getHeight();
		}
	}


	// ---------------------------------------------------------------- helpers

	/** OSRS-style beveled stone panel. */
	private static void drawStonePanel(Graphics2D g, int x, int y, int w, int h)
	{
		Paint old = g.getPaint();
		g.setPaint(new GradientPaint(0, y, PANEL_FILL, 0, y + h, PANEL_FILL_DARK));
		g.fillRect(x, y, w, h);
		g.setPaint(old);

		g.setColor(BORDER_OUTER);
		g.drawRect(x, y, w - 1, h - 1);
		g.setColor(BORDER_LIGHT);
		g.drawLine(x + 1, y + 1, x + w - 2, y + 1);
		g.drawLine(x + 1, y + 1, x + 1, y + h - 2);
		g.setColor(BORDER_DARK);
		g.drawLine(x + 1, y + h - 2, x + w - 2, y + h - 2);
		g.drawLine(x + w - 2, y + 1, x + w - 2, y + h - 2);
		g.setColor(BORDER_OUTER);
		g.drawRect(x + 3, y + 3, w - 7, h - 7);
	}

	private static void drawCentered(Graphics2D g, String s, int x, int w, int baseline, Color color)
	{
		FontMetrics fm = g.getFontMetrics();
		int sx = x + (w - fm.stringWidth(s)) / 2;
		int off = Math.max(1, g.getFont().getSize() / 16);
		g.setColor(SHADOW);
		g.drawString(s, sx + off, baseline + off);
		g.setColor(color);
		g.drawString(s, sx, baseline);
	}

	private static List<String> wrap(String text, FontMetrics fm, int maxWidth)
	{
		List<String> lines = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		for (String word : text.split("\\s+"))
		{
			String candidate = line.length() == 0 ? word : line + " " + word;
			if (fm.stringWidth(candidate) <= maxWidth || line.length() == 0)
			{
				line.setLength(0);
				line.append(candidate);
			}
			else
			{
				lines.add(line.toString());
				line.setLength(0);
				line.append(word);
			}
		}
		if (line.length() > 0)
		{
			lines.add(line.toString());
		}
		return lines;
	}

	/** Fonts, metrics and wrapped lines for one text size. */
	private static final class Layout
	{
		final Font labelFont;
		final Font headFont;
		final Font bodyFont;
		final FontMetrics labelFm;
		final FontMetrics headFm;
		final FontMetrics bodyFm;
		final List<String> lines;

		Layout(Graphics2D g, Tip tip, int bodySize, int width)
		{
			// The RuneScape fonts are pixel fonts: crisp at multiples of 16,
			// smoother with antialiasing at other sizes.
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, bodySize % 16 == 0
				? RenderingHints.VALUE_TEXT_ANTIALIAS_OFF
				: RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			labelFont = FontManager.getRunescapeSmallFont().deriveFont(Font.PLAIN, Math.max(16f, bodySize * 0.75f));
			headFont = FontManager.getRunescapeBoldFont().deriveFont(Font.PLAIN, Math.round(bodySize * 1.33f));
			bodyFont = FontManager.getRunescapeFont().deriveFont(Font.PLAIN, (float) bodySize);
			labelFm = g.getFontMetrics(labelFont);
			headFm = g.getFontMetrics(headFont);
			bodyFm = g.getFontMetrics(bodyFont);
			lines = wrap(tip.getText(), bodyFm, width);
		}

		int height(int pad)
		{
			return pad
				+ labelFm.getHeight() + 2
				+ headFm.getHeight() + 6
				+ 12
				+ lines.size() * bodyFm.getHeight()
				+ pad;
		}
	}
}
