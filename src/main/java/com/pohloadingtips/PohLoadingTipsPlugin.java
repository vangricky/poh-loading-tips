package com.pohloadingtips;

import com.google.inject.Provides;
import java.awt.Color;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.game.ItemManager;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "POH Loading Screen",
	description = "Replaces the house loading screen with OSRS meme cards, quick tips and 'Did you know?' facts",
	tags = {"poh", "house", "construction", "loading", "tips", "trivia", "facts", "memes"}
)
public class PohLoadingTipsPlugin extends Plugin
{
	private static final Color CHAT_ORANGE = new Color(0xFF981F);

	@Inject
	private Client client;

	@Inject
	private PohLoadingTipsConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private PohLoadingTipsOverlay overlay;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ItemManager itemManager;

	private final TipRepository tips = new TipRepository();
	private final MemeRepository memes = new MemeRepository();

	/** Tip for the current (or most recent) loading screen, or null if a meme is showing. */
	@Getter
	private Tip currentTip;

	/** Meme for the current (or most recent) loading screen, or null if a tip is showing. */
	@Getter
	private Meme currentMeme;

	/** "::pohmeme" shows the next meme card on screen until this time (System.currentTimeMillis). */
	@Getter
	private long previewUntil;

	@Subscribe
	public void onCommandExecuted(CommandExecuted e)
	{
		if (!"pohmeme".equalsIgnoreCase(e.getCommand()) || nextMeme == null) return;
		currentMeme = nextMeme;
		currentTip = null;
		previewUntil = System.currentTimeMillis() + 8000;
		prepareNextMeme();
	}

	/** Picked ahead of time so its sprites are already loaded when the screen appears. */
	private Meme nextMeme;
	private boolean memeTurn = true;

	/** Whether the POH loading screen is up right now. */
	@Getter
	private boolean loadingScreenVisible;


	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		prepareNextMeme();
		log.debug("POH Loading Screen started with {} tips and {} memes", tips.size(), memes.size());
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		loadingScreenVisible = false;
		currentTip = null;
		currentMeme = null;
		nextMeme = null;
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

	private boolean showMemeNow()
	{
		switch (config.content())
		{
			case TIPS:
				return false;
			case MIX:
				memeTurn = !memeTurn;
				return memeTurn;
			default:
				return true;
		}
	}

	/**
	 * Polled every client frame rather than relying on widget load/close events,
	 * so we always know exactly whether the house loading screen is on screen.
	 */
	@Subscribe
	public void onClientTick(ClientTick tick)
	{
		boolean visible = isPohLoadingVisible();
		if (visible == loadingScreenVisible)
		{
			return;
		}

		loadingScreenVisible = visible;
		if (visible)
		{
			if (showMemeNow() && nextMeme != null)
			{
				currentMeme = nextMeme;
				currentTip = null;
				prepareNextMeme();
				return;
			}
			currentMeme = null;
			currentTip = tips.next(t -> t.getKind() == Tip.Kind.TIP ? config.showTips() : config.showFacts());
			if (currentTip != null && config.sendToChat())
			{
				postToChat(currentTip);
			}
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

	private boolean isPohLoadingVisible()
	{
		return getPohLoadingWidget() != null;
	}

	private void postToChat(Tip tip)
	{
		String msg = new ChatMessageBuilder()
			.append(CHAT_ORANGE, tip.getKind().getHeading() + " ")
			.append(tip.getText())
			.build();
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage(msg)
			.build());
	}

	@Provides
	PohLoadingTipsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PohLoadingTipsConfig.class);
	}
}
