package net.profet.echoshaper.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.profet.echoshaper.Echoshaper;
import net.profet.echoshaper.item.custom.EchoshaperItem;

public class ModItems {
    // Using Rarity.RARE from net.minecraft.util.Rarity
    public static final Item ECHOSHAPER = registerItem("echoshaper",
            new EchoshaperItem(new FabricItemSettings().maxCount(1).rarity(Rarity.RARE)));

    private static void addItemsToToolsItemGroup(FabricItemGroupEntries entries) {
        entries.add(ECHOSHAPER);
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(Echoshaper.MOD_ID, name), item);
    }

    public static void registerModItems() {
        Echoshaper.LOGGER.info("Registering Mod Items for " + Echoshaper.MOD_ID);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(ModItems::addItemsToToolsItemGroup);
    }
}