package com.pohloadingtips;

import lombok.Value;

@Value
public class Tip
{
	public enum Kind
	{
		TIP("Quick Tip"),
		FACT("Did you know?");

		final String heading;

		Kind(String heading)
		{
			this.heading = heading;
		}

		public String getHeading()
		{
			return heading;
		}
	}

	Kind kind;
	String category;
	String text;
}
