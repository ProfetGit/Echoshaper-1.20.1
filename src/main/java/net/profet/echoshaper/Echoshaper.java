package net.profet.echoshaper;

import net.fabricmc.api.ModInitializer;

import net.profet.echoshaper.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Echoshaper implements ModInitializer {
	
	public static final String MOD_ID = "echoshaper";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		ModItems.registerModItems();
	}
}