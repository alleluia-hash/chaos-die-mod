package com.chaosdie;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ChaosDieMod implements ModInitializer {
    public static final String MOD_ID = "chaosdie";

    public static final Item CHAOS_DIE =
            new ChaosDieItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC));

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "chaos_die"), CHAOS_DIE);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(CHAOS_DIE));
    }
}
