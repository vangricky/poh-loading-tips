package com.pohloadingtips;

import java.util.List;
import lombok.Value;

/**
 * A meme card: one to three OSRS item sprites (drawn by the player's own
 * RuneLite client, so no images are bundled or downloaded) with an original
 * top and bottom caption.
 */
@Value
public class Meme
{
	List<Integer> itemIds;
	String top;
	String bottom;
}
