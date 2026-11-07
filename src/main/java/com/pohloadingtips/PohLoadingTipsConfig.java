package com.pohloadingtips;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(PohLoadingTipsConfig.GROUP)
public interface PohLoadingTipsConfig extends Config
{
	String GROUP = "pohloadingtips";

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

	@Range(max = 10)
	@Units(Units.SECONDS)
	@ConfigItem(
		keyName = "lingerSeconds",
		name = "Keep tip after loading",
		description = "The loading screen only lasts a second or two. Keep the tip on a small banner for this many seconds afterwards so you can finish reading it (0 = off).",
		position = 4
	)
	default int lingerSeconds()
	{
		return 4;
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
