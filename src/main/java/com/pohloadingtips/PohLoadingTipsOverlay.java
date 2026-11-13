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
 * Draws a meme card over the house loading screen, on a classic OSRS
 * dark brown stone panel.
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
}
