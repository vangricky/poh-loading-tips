package com.pohloadingtips;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "POH Loading Screen",
	description = "Replaces the house loading screen with original OSRS meme cards",
	tags = {"poh", "house", "construction", "loading", "memes"}
)
public class PohLoadingTipsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PohLoadingTipsOverlay overlay;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ItemManager itemManager;

	private final MemeRepository memes = new MemeRepository();

	/** Meme for the current (or most recent) loading screen. */
	@Getter
	private Meme currentMeme;

	/** Picked ahead of time so its sprites are already loaded when the screen appears. */
	private Meme nextMeme;

	/** "::pohmeme" shows the next meme card on screen until this time (System.currentTimeMillis). */
	@Getter
	private long previewUntil;

	/** Whether the POH loading screen is up right now. */
	@Getter
	private boolean loadingScreenVisible;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		prepareNextMeme();
		log.debug("POH Loading Screen started with {} memes", memes.size());
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		loadingScreenVisible = false;
		currentMeme = null;
		nextMeme = null;
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted e)
	{
		if (!"pohmeme".equalsIgnoreCase(e.getCommand()) || nextMeme == null) return;
		currentMeme = nextMeme;
		previewUntil = System.currentTimeMillis() + 8000;
		prepareNextMeme();
	}

	/** Picks the next meme and asks RuneLite to load its sprites now, not mid-loading-screen. */
	private void prepareNextMeme()
	{
		nextMeme = memes.next();
		Meme m = nextMeme;
		if (m != null)
		{
			clientThread.invokeLater(() -> m.getItemIds().forEach(itemManager::getImage));
		}
	}

	/**
	 * Polled every client frame rather than relying on widget load/close events,
	 * so we always know exactly whether the house loading screen is on screen.
	 */
	@Subscribe
	public void onClientTick(ClientTick tick)
	{
		boolean visible = getPohLoadingWidget() != null;
		if (visible == loadingScreenVisible)
		{
			return;
		}

		loadingScreenVisible = visible;
		if (visible && nextMeme != null)
		{
			currentMeme = nextMeme;
			prepareNextMeme();
		}
	}

	/** The widget area the loading screen covers, or null if it isn't showing. */
	Widget getPohLoadingWidget()
	{
		Widget w = client.getWidget(InterfaceID.PohLoading.UNIVERSE);
		if (w == null || w.isHidden())
		{
			w = client.getWidget(InterfaceID.PohLoading.UNIVERSE_RECT0);
		}
		return w != null && !w.isHidden() ? w : null;
	}

	@Provides
	PohLoadingTipsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PohLoadingTipsConfig.class);
	}
}
