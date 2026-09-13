package com.mcextractors.init;

import com.mcextractors.MCExtractors;
import com.mcextractors.blocks.IronExtractorBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, MCExtractors.MOD_ID);

    // Iron Extractor
    public static final RegistryObject<Block> IRON_EXTRACTOR = BLOCKS.register("iron_extractor",
            () -> new IronExtractorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    // Block items
    public static final RegistryObject<Item> IRON_EXTRACTOR_ITEM =
        ModItems.ITEMS.register("iron_extractor",
            () -> new BlockItem(IRON_EXTRACTOR.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
