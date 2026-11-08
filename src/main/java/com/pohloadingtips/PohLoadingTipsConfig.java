package com.pohloadingtips;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(PohLoadingTipsConfig.GROUP)
public interface PohLoadingTipsConfig extends Config
{
	String GROUP = "pohloadingtips";

	enum Content
	{
		MEMES("Meme cards"),
		TIPS("Tips & facts"),
		MIX("Mix of both");

		private final String label;

		Content(String label)
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	@ConfigItem(
		keyName = "content",
		name = "Show",
		description = "What to show on the house loading screen: OSRS meme cards, tips & 'Did you know?' facts, or a mix",
		position = 0
	)
	default Content content()
	{
		return Content.MEMES;
	}

	@ConfigItem(
		keyName = "showTips",
		name = "Show quick tips",
		description = "Show useful gameplay tips on the house loading screen",
		position = 1
	)
	default boolean showTips()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showFacts",
		name = "Show 'Did you know?' facts",
		description = "Show surprising OSRS trivia on the house loading screen",
		position = 2
	)
	default boolean showFacts()
	{
		return true;
	}

	@Range(min = 16, max = 40)
	@ConfigItem(
		keyName = "textSize",
		name = "Text size",
		description = "Size of the tip text. 16 is the normal in-game size; 16 and 32 are the crispest. Text shrinks automatically if it won't fit.",
		position = 3
	)
	default int textSize()
	{
		return 24;
	}

	@ConfigItem(
		keyName = "sendToChat",
		name = "Also post tip in chat",
		description = "Post each tip as a game message so you can read it again later",
		position = 5
	)
	default boolean sendToChat()
	{
		return false;
	}
}
