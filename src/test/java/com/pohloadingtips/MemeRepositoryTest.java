package com.pohloadingtips;

import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class MemeRepositoryTest
{
	@Test
	public void allMemesLoadWithItemsAndCaptions()
	{
		MemeRepository repo = new MemeRepository();
		assertEquals(100, repo.size());
		Set<String> seen = new HashSet<>();
		for (int i = 0; i < repo.size(); i++)
		{
			Meme m = repo.next();
			assertNotNull(m);
			assertTrue("1-3 sprites", m.getItemIds().size() >= 1 && m.getItemIds().size() <= 3);
			assertTrue("valid item ids", m.getItemIds().stream().allMatch(id -> id > 0 && id < 40000));
			assertTrue(!m.getTop().isEmpty() && !m.getBottom().isEmpty());
			seen.add(m.getTop() + m.getBottom());
		}
		assertEquals("a full deck shows every meme once before repeating", 100, seen.size());
	}
}
