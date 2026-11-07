package com.pohloadingtips;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PohLoadingTipsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PohLoadingTipsPlugin.class);
		RuneLite.main(args);
	}
}
