package com.pohloadingtips;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Loads meme cards from memes.txt ("itemId,itemId|TOP|BOTTOM" per line, built by
 * scripts/resolve-memes.mjs) and deals them like a shuffled deck.
 */
@Slf4j
class MemeRepository
{
	private final List<Meme> all = new ArrayList<>();
	private final Deque<Meme> deck = new ArrayDeque<>();

	MemeRepository()
	{
		try (InputStream in = MemeRepository.class.getResourceAsStream("memes.txt"))
		{
			if (in == null)
			{
				log.warn("memes.txt not found");
				return;
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			String line;
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) continue;
				String[] parts = line.split("\\|", 3);
				if (parts.length != 3) continue;
				List<Integer> ids = new ArrayList<>();
				for (String id : parts[0].split(","))
				{
					try
					{
						ids.add(Integer.parseInt(id.trim()));
					}
					catch (NumberFormatException e)
					{
						log.warn("Bad item id in meme: {}", line);
					}
				}
				if (!ids.isEmpty()) all.add(new Meme(ids, parts[1].trim(), parts[2].trim()));
			}
		}
		catch (IOException e)
		{
			log.warn("Unable to load memes", e);
		}
	}

	int size()
	{
		return all.size();
	}

	Meme next()
	{
		if (all.isEmpty()) return null;
		if (deck.isEmpty())
		{
			List<Meme> shuffled = new ArrayList<>(all);
			Collections.shuffle(shuffled);
			deck.addAll(shuffled);
		}
		return deck.poll();
	}
}
