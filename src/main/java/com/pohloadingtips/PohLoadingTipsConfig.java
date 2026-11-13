package com.pohloadingtips;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(PohLoadingTipsConfig.GROUP)
public interface PohLoadingTipsConfig extends Config
{
	String GROUP = "pohloadingtips";

	@Range(min = 16, max = 40)
	@ConfigItem(
		keyName = "textSize",
		name = "Text size",
		description = "Size of the meme captions. Text shrinks automatically if it won't fit.",
		position = 0
	)
	default int textSize()
	{
		return 24;
	}
}
