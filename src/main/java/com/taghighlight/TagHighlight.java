// TagHighlight.java
package com.taghighlight;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagHighlight implements ModInitializer {
	public static final String MOD_ID = "tag-highlight";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Tag Highlight mod initialized!");
	}
}