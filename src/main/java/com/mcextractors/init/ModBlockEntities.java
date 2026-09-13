package com.mcextractors.init;

import com.mcextractors.MCExtractors;
import com.mcextractors.blockentities.IronExtractorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MCExtractors.MOD_ID);

    public static final RegistryObject<BlockEntityType<IronExtractorBlockEntity>> IRON_EXTRACTOR =
        BLOCK_ENTITIES.register("iron_extractor", () ->
            BlockEntityType.Builder.of(IronExtractorBlockEntity::new,
                ModBlocks.IRON_EXTRACTOR.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
