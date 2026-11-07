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
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;

/**
 * Loads tips from tips.txt and deals them out like a shuffled deck, so you
 * never see the same tip twice until you've seen them all.
 *
 * File format, one per line:  TIP|Category|Text   or   FACT|Category|Text
 */
@Slf4j
class TipRepository
{
	private final List<Tip> all = new ArrayList<>();
	private final Deque<Tip> deck = new ArrayDeque<>();

	TipRepository()
	{
		try (InputStream in = TipRepository.class.getResourceAsStream("tips.txt"))
		{
			if (in == null)
			{
				log.warn("tips.txt not found");
				return;
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			String line;
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				String[] parts = line.split("\\|", 3);
				if (parts.length != 3)
				{
					log.warn("Bad tip line: {}", line);
					continue;
				}
				try
				{
					all.add(new Tip(Tip.Kind.valueOf(parts[0].trim()), parts[1].trim(), parts[2].trim()));
				}
				catch (IllegalArgumentException e)
				{
					log.warn("Bad tip kind: {}", line);
				}
			}
		}
		catch (IOException e)
		{
			log.warn("Unable to load tips", e);
		}
	}

	int size()
	{
		return all.size();
	}

	/**
	 * @return the next tip allowed by the filter, or null if none are allowed.
	 */
	Tip next(Predicate<Tip> allowed)
	{
		// Two passes: finish the current deck, then reshuffle once if needed.
		for (int pass = 0; pass < 2; pass++)
		{
			while (!deck.isEmpty())
			{
				Tip tip = deck.poll();
				if (allowed.test(tip))
				{
					return tip;
				}
			}
			List<Tip> shuffled = new ArrayList<>(all);
			Collections.shuffle(shuffled);
			deck.addAll(shuffled);
		}
		return null;
	}
}
