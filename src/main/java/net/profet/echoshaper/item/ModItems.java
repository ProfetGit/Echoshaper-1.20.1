package net.profet.echoshaper.item;

// Importing necessary libraries and classes
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.profet.echoshaper.Echoshaper;

// This class is for creating and registering new items in the game
public class ModItems {
    // Creating a new item called ECHOSHAPER
    public static final Item ECHOSHAPER = registerItem("echoshaper", new Item(new FabricItemSettings()));

    // This method adds the ECHOSHAPER item to the Tools item group
    private static void addItemsToToolsItemGroup(FabricItemGroupEntries entries) {
        entries.add(ECHOSHAPER);
    }

    // This method registers the item with a unique name so the game can recognize it
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(Echoshaper.MOD_ID, name), item);
    }

    // This method logs a message and registers all mod items
    public static void registerModItems() {
        // Print a message to the console
        Echoshaper.LOGGER.info("Registering Mod Items for " + Echoshaper.MOD_ID);

        // Add the ECHOSHAPER item to the Tools item group
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(ModItems::addItemsToToolsItemGroup);
    }
}