package com.pohloadingtips;

import com.google.inject.Provides;
import java.awt.Color;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ClientTick;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "POH Loading Tips",
	description = "Replaces the house loading screen with quick OSRS tips and 'Did you know?' facts",
	tags = {"poh", "house", "construction", "loading", "tips", "trivia", "facts"}
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

	private final TipRepository tips = new TipRepository();

	/** Tip for the current (or most recent) loading screen. */
	@Getter
	private Tip currentTip;

	/** Whether the POH loading screen is up right now. */
	@Getter
	private boolean loadingScreenVisible;

	/** When the loading screen came down (System.currentTimeMillis). */
	@Getter
	private long closedAt;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		log.debug("POH Loading Tips started with {} tips", tips.size());
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		loadingScreenVisible = false;
		currentTip = null;
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
			currentTip = tips.next(t -> t.getKind() == Tip.Kind.TIP ? config.showTips() : config.showFacts());
			if (currentTip != null && config.sendToChat())
			{
				postToChat(currentTip);
			}
		}
		else
		{
			closedAt = System.currentTimeMillis();
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
